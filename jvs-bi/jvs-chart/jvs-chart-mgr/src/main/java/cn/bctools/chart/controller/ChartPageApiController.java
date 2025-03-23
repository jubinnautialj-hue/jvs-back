package cn.bctools.chart.controller;

import cn.bctools.chart.api.JvsChartApi;
import cn.bctools.chart.api.dto.ChartPageDto;
import cn.bctools.chart.api.dto.DeleteChartPageDto;
import cn.bctools.chart.api.dto.IsDeleteDto;
import cn.bctools.chart.chart.util.AuthUtil;
import cn.bctools.chart.entity.ChartPage;
import cn.bctools.chart.enums.ChartPageSource;
import cn.bctools.chart.enums.OperationEnum;
import cn.bctools.chart.service.ChartPageService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 页面配置
 *
 * @author zqs
 */
@Slf4j
@Component
@Api(tags = "api接口")
@RestController
@AllArgsConstructor
public class ChartPageApiController implements JvsChartApi {

    ChartPageService chartPageService;
    AuthUtil<OperationEnum, ChartPage> authUtil;

    @Override
    public R<ChartPageDto> save(String jvsAppId) {
        ChartPage chartPage = new ChartPage();
        chartPage.setName("未命名图表");
        chartPage.setIsDeploy(Boolean.TRUE);
        chartPage.setJvsAppId(jvsAppId);
        chartPage.setSource(ChartPageSource.jvs_design);
        chartPageService.save(chartPage);
        return R.ok(BeanCopyUtil.copy(chartPage, ChartPageDto.class));
    }

    @Override
    public R updateByName(String id, String name) {
        chartPageService.update(new UpdateWrapper<ChartPage>().lambda().eq(ChartPage::getId, id).set(ChartPage::getName, name));
        return R.ok();
    }

    @Override
    public R<List<ChartPageDto>> getAll(List<String> jvsAppId) {
        List<ChartPage> list = chartPageService.list(new QueryWrapper<ChartPage>().lambda().select(ChartPage::getId, ChartPage::getName, ChartPage::getRole, ChartPage::getJvsAppId)
                .in(ChartPage::getJvsAppId, jvsAppId));
        return R.ok(BeanCopyUtil.copys(list, ChartPageDto.class));
    }

    @Override
    public R<List<ChartPageDto>> get(String jvsAppId) {
        List<ChartPage> list = chartPageService.list(new QueryWrapper<ChartPage>().lambda().select(ChartPage::getId, ChartPage::getName, ChartPage::getRole, ChartPage::getJvsAppId)
                .eq(ChartPage::getJvsAppId, jvsAppId));
        return R.ok(BeanCopyUtil.copys(list, ChartPageDto.class));
    }


    @Override
    public R<Boolean> up(JSONObject jsonObject, String id) {
        UserDto userDto = jsonObject.getJSONObject("userDto").toJavaObject(UserDto.class);
        ChartPage chartPage = jsonObject.getJSONObject("data").toJavaObject(ChartPage.class);
        //清空 需要初始化的数据
        chartPage.setTenantId(null);
        chartPage.setCreateBy(userDto.getRealName());
        chartPage.setCreateById(userDto.getId());
        chartPage.setCreateTime(null);
        chartPage.setUpdateTime(null);
        chartPage.setUpdateBy(userDto.getRealName());
        String tenantId = TenantContextHolder.getTenantId();
        chartPage.setTenantId(tenantId);
        //重新设置菜单id
        chartPage.setType(id);
        //获取此类型的所有数据用于排序
        long sortCount = chartPageService.count(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getType, id));
        chartPage.setSort(sortCount + 1);
        //这里需要把租户信息清理掉 防止其他租户 存在此数据
        TenantContextHolder.clear();
        long count = chartPageService.upGetOrDataRecover(chartPage.getId());
        if (count > 0) {
            chartPageService.updateById(chartPage);
        } else {
            chartPageService.save(chartPage);
        }
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<JSONObject> downFile(String id, Boolean isMock) {
        ChartPage byId = chartPageService.getById(id);
        if (isMock) {
            String dataJson = byId.getDataJson();
            List<JSONObject> list = JSONArray.parseArray(dataJson).stream().map(e -> {
                JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(e));
                jsonObject.remove("dataSource");
                jsonObject.put("fieldList", new ArrayList<>());
                jsonObject.put("logicSetting", new JSONObject());
                jsonObject.put("sortList", new ArrayList<>());
                jsonObject.put("dataType", "mockData");
                return jsonObject;
            }).collect(Collectors.toList());
            byId.setDataJson(JSONObject.toJSONString(list));
        }
        return R.ok(JSONObject.parseObject(JSONObject.toJSONString(byId)));
    }

    @Override
    public R<Boolean> isDelete(IsDeleteDto isDeleteDto) {
        String errorMsg = "此图表已经被删除";
        ChartPage byId = chartPageService.getById(isDeleteDto.getId());
        if (byId == null) {
            return R.failed(errorMsg);
        }
        String dataJson = byId.getDataJson();
        List<JSONObject> list = JSONObject.parseArray(dataJson, JSONObject.class);
        Optional<JSONObject> first = list.stream()
                .filter(e -> e.getString("i").equals(isDeleteDto.getSubId()))
                .findFirst();
        if (!first.isPresent()) {
            return R.failed(errorMsg);
        }
        Boolean isJobExec = Optional.ofNullable(isDeleteDto.getIsJobExec()).orElse(Boolean.FALSE);
        if (!isJobExec) {
            //校验当前用户是否有权限
            ChartPage chartPage = authUtil.auth(byId, null, Arrays.asList(OperationEnum.values()));
            boolean anyMatch = chartPage.getOperationList().isEmpty();
            if (anyMatch) {
                return R.failed("此图表您暂无权限");
            }
        }
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<List<String>> getDataFactoryId(String chartId) {
        List<String> dataFactoryId = chartPageService.getDataFactoryId(chartId);
        return R.ok(dataFactoryId);
    }

    @Override
    public R delete(DeleteChartPageDto deleteChartPageDto) {
        chartPageService.remove(new LambdaQueryWrapper<ChartPage>()
                .eq(StrUtil.isNotEmpty(deleteChartPageDto.getJvsAppId()), ChartPage::getJvsAppId, deleteChartPageDto.getJvsAppId())
                .in(ObjectUtil.isNotEmpty(deleteChartPageDto.getIds()), ChartPage::getId, deleteChartPageDto.getIds()));
        return R.ok();
    }
}
