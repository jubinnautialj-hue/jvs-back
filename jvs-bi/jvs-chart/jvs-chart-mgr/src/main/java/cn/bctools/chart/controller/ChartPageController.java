package cn.bctools.chart.controller;

import cn.bctools.chart.chart.bo.ChartSettingBo;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.util.AuthUtil;
import cn.bctools.chart.dto.ChartMoveDto;
import cn.bctools.chart.dto.CopyDto;
import cn.bctools.chart.entity.ChartPage;
import cn.bctools.chart.enums.OperationEnum;
import cn.bctools.chart.service.ChartPageService;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 页面配置
 *
 * @author zqs
 */
@Api(tags = "[chart]图表页面配置")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/chart/design")
public class ChartPageController {

    ChartPageService chartPageService;
    AuthUtil<OperationEnum, ChartPage> authUtil;

    @ApiOperation("创建了图表")
    @PostMapping
    public R<ChartPage> save(@RequestBody ChartPage page) {
        if (ObjectNull.isNull(page.getName())) {
            page.setName("未命名图表");
        }
        page.setIsDeploy(Boolean.TRUE);
        //获取此类型的所有数据用于排序
        long count = chartPageService.count(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getType, page.getType()));
        page.setSort(count + 1);
        chartPageService.save(page);
        return R.ok(page);
    }

    @Log
    @ApiOperation("基础信息修改")
    @PutMapping("/baseInfo")
    public R<ChartPage> updateBaseInfo(@RequestBody ChartPage chartPage) {
        chartPageService.updateById(chartPage);
        return R.ok(chartPage);
    }


    @Log
    @ApiOperation("删除数据")
    @DeleteMapping("/del/{id}")
    public R<Boolean> del(@ApiParam("id") @PathVariable("id") String id) {
        return R.ok(chartPageService.removeById(id));
    }

    @Log
    @ApiOperation("移动不管是调整顺序还是移动到其它目录")
    @PutMapping("/move")
    @Transactional(rollbackFor = Exception.class)
    public R<List<ChartPage>> move(@RequestBody ChartMoveDto chartMoveDto) {
        //获取数据
        ChartPage byId = chartPageService.getById(chartMoveDto.getId());
        List<OperationEnum> check = authUtil.check(OperationEnum.移动, byId.getCreateById(), byId.getRole(), Arrays.asList(OperationEnum.values()), byId.getRoleType());
        if (check.isEmpty()) {
            return R.failed("暂无权限,请联系此图表创建者");
        }
        //获取当前目录下面的所有文档
        List<ChartPage> list = chartPageService.list(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getType, chartMoveDto.getParentId())
                .ne(ChartPage::getId, chartMoveDto.getId())
                .orderByAsc(ChartPage::getSort));
        int indexOf = 0;
        if (StrUtil.isNotBlank(chartMoveDto.getFrontId())) {
            indexOf = list.stream().map(ChartPage::getId).collect(Collectors.toList()).indexOf(chartMoveDto.getFrontId()) + 1;
        }
        //插入数据
        byId.setType(chartMoveDto.getParentId());
        list.add(indexOf, byId);
        list = list.subList(indexOf, list.size());
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            ChartPage chartPage = list.get(i);
            int i1 = indexOf + i;
            chartPage.setSort((long) i1);
        }
        chartPageService.updateBatchById(list);
        return R.ok(list);
    }

    @Log
    @ApiOperation("基础信息获取")
    @GetMapping("/detail/{id}")
    public R<ChartPage> detail(@PathVariable String id) {
        ChartPage byId = chartPageService.getById(id);
        if (byId == null) {
            return R.failed("未找到此数据");
        }
        return R.ok(authUtil.auth(byId, null, Arrays.asList(OperationEnum.values())));
    }

    @Log
    @ApiOperation("修改图表页面")
    @PutMapping
    public R<ChartPage> updateById(@RequestBody ChartPage page) {
        ChartPage page2 = chartPageService.getById(page.getId());
        if (page2 == null) {
            return R.failed("设计不存在");
        }
        if (StrUtil.isNotBlank(page.getDataJson())) {
            JSONArray.parseArray(page.getDataJson())
                    .forEach(e->{
                        JSONObject jsonObject = JSONObject.parseObject(e.toString());
                        ChartSettingBo chartSettingBo = JSONObject.parseObject(e.toString(), ChartSettingBo.class);
                        ChartDesignInParameter.DataFactoryObj dataFactoryObj = chartSettingBo.getLogicSetting().getDataFactoryObj();
                        boolean b = ObjectUtil.isNotNull(dataFactoryObj) && ("mqtt".equals(dataFactoryObj.getDataFactoryType()) || "api".equals(dataFactoryObj.getDataFactoryType()));
                        if (b) {
                            //发送消息 用于血缘视图记录
                            ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse()
                                    .setDataFactoryId(dataFactoryObj.getDataFactoryId())
                                    .setDesignName(page.getName())
                                    .setTenantId(TenantContextHolder.getTenantId())
                                    .setDesignId(page.getId())
                                    .setDesignDetailId(jsonObject.getString("i"))
                                    .setDesignDetailName(jsonObject.getJSONObject("setting").getString("name"));
                            chartPageService.send(consanguinityAnalyse);
                        }
                    });

        }
        chartPageService.updateById(page);
        return R.ok(page);
    }

    @ApiOperation("复制图表")
    @PostMapping("/copy/{id}/{typeId}")
    public R<ChartPage> byId(@ApiParam("大屏id") @PathVariable("id") String id
            , @ApiParam("目录id") @PathVariable("typeId") String typeId
            , @RequestBody CopyDto dto) {
        ChartPage byId = chartPageService.getById(id);
        if (ObjectUtil.isNull(byId)) {
            return R.failed("图表不存在");
        }
        List<OperationEnum> check = authUtil.check(OperationEnum.复制, byId.getCreateById(), byId.getRole(), Arrays.asList(OperationEnum.values()), byId.getRoleType());
        if (CollectionUtil.isEmpty(check)) {
            return R.failed("未拥有复制权限");
        }
        byId.setId(null).setType(typeId).setName(dto.getName());
        byId.setCreateBy(null);
        byId.setCreateTime(null);
        byId.setCreateById(null);
        byId.setUpdateBy(null);
        byId.setUpdateTime(null);
        chartPageService.save(byId);
        return R.ok(byId);
    }


}
