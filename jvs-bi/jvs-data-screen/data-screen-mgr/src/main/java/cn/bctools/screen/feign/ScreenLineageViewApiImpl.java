package cn.bctools.screen.feign;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.screen.api.ScreenLineageViewApi;
import cn.bctools.screen.api.params.JvsChartPageCanvasDTO;
import cn.bctools.screen.entity.ChartPage;
import cn.bctools.screen.entity.JvsChartPageCanvas;
import cn.bctools.screen.service.ChartPageService;
import cn.bctools.screen.service.JvsChartPageCanvasService;
import cn.bctools.screen.utils.AuthUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "[feign] 血缘视图 ")
@RequiredArgsConstructor
@RestController
public class ScreenLineageViewApiImpl implements ScreenLineageViewApi {

    private final ChartPageService chartPageService;
    private final JvsChartPageCanvasService jvsChartPageCanvasService;

    @Override
    public R<Boolean> check(String id,String subId,String isTask) {
        String errorMsg = "此大屏已经被删除";
        ChartPage byId = chartPageService.getById(id);
        if (byId == null) {
            return R.failed(errorMsg);
        }
        if (!StrUtil.equals(isTask,"1")) {
            List<OperationType> operationList = AuthUtils.getOperationList(byId);
            if(CollectionUtil.isEmpty(operationList)){
                return R.failed("您暂未无此大屏权限");
            }
        }

        List<JvsChartPageCanvas> canvasList = jvsChartPageCanvasService.list(Wrappers.lambdaQuery(JvsChartPageCanvas.class).eq(JvsChartPageCanvas::getChartId, id));

        boolean has = canvasList.stream()
                .filter(e -> StrUtil.isNotBlank(e.getDataJson()))
                .anyMatch(e -> JSONObject.parseArray(e.getDataJson(), JSONObject.class).stream().anyMatch(v -> v.getString("id").equals(subId)));
        if (!has) {
            return R.failed(errorMsg);
        }
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<JSONObject> downFile(String id, Boolean isMock) {
        JSONObject jsonObject = new JSONObject();
        ChartPage baseInfo = chartPageService.getById(id);
        baseInfo.clear();
        jsonObject.put("base",baseInfo);
        List<JvsChartPageCanvas> canvasList = jvsChartPageCanvasService.list(Wrappers.lambdaQuery(JvsChartPageCanvas.class).eq(JvsChartPageCanvas::getChartId,id));

        List<JvsChartPageCanvasDTO> newCanvasList = canvasList.stream().map(e -> {
            JvsChartPageCanvasDTO canvas = BeanCopyUtil.copy(e, JvsChartPageCanvasDTO.class);
            String dataJson = e.getDataJson();
            JSONArray components = JSONObject.parseArray(dataJson);
            if (isMock) {
                List<Object> newCanvasDesign = components.stream().map(JSONUtil::parseObj).peek(v -> {
                    v.remove("dataSource");
                    v.set("fieldList", new ArrayList<>());
                    v.set("logicSetting", new JSONObject());
                    v.set("sortList", new ArrayList<>());
                    v.set("dataType", "mockData");
                }).collect(Collectors.toList());
                canvas.setDataJson(newCanvasDesign);
            }else {
                canvas.setDataJson(components);
            }
            return canvas;
        }).collect(Collectors.toList());
        jsonObject.put("canvas",newCanvasList);
        return R.ok(jsonObject);
    }

    @Override
    public R<Boolean> up(JSONObject data, String id) {
        UserDto userDto = data.getJSONObject("userDto").toJavaObject(UserDto.class);
        JSONObject jsonObject = data.getJSONObject("data");
        JSONObject base = jsonObject.getJSONObject("base");

        ChartPage baseInfo = JSONObject.toJavaObject(base, ChartPage.class);
        baseInfo.setType(id);
        baseInfo.setCreateBy(userDto.getRealName());
        baseInfo.setCreateById(userDto.getId());
        baseInfo.setUpdateBy(userDto.getRealName());

        long sortCount = chartPageService.count(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getType, id));
        baseInfo.setSort((int) (sortCount + 1));

        /*
         * 判断是否有重复id的数据
         */
        String tenantId = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();

        //删除基础设计
        chartPageService.forcedDeletion(baseInfo.getId());
        //删除所有重复画布
        jvsChartPageCanvasService.forcedDeletion(baseInfo.getId());

        TenantContextHolder.setTenantId(tenantId);
        JSONArray canvasArr = jsonObject.getJSONArray("canvas");
        List<JvsChartPageCanvas> canvasList = new ArrayList<>();
        for (int i = 0; i < canvasArr.size(); i++) {
//            JvsChartPageCanvas canvas = canvasArr.getJSONObject(i).toJavaObject(JvsChartPageCanvas.class);
            JvsChartPageCanvasDTO canvasDTO = canvasArr.getJSONObject(i).toJavaObject(JvsChartPageCanvasDTO.class);
            JvsChartPageCanvas canvas = BeanCopyUtil.copy(canvasDTO, JvsChartPageCanvas.class);
            String dataJson = JSONUtil.toJsonStr(canvasDTO.getDataJson());
            if (StrUtil.isNotBlank(dataJson)&& JSONUtil.isTypeJSONArray(dataJson)) {
                List<cn.hutool.json.JSONObject> newDataJson = JSONUtil.parseArray(dataJson)
                        .stream()
                        .map(JSONUtil::parseObj)
                        .peek(v -> v.set("id", IdGenerator.getIdStr(16)))
                        .collect(Collectors.toList());
                canvas.setDataJson(JSONUtil.toJsonStr(newDataJson));
            }
            canvas.setTenantId(tenantId);
            canvas.setChartId(baseInfo.getId());
            canvasList.add(canvas);
        }
        if(CollectionUtil.isNotEmpty(canvasList)){
            jvsChartPageCanvasService.saveBatch(canvasList);
        }
        return R.ok( chartPageService.save(baseInfo));
    }

    @Override
    public R<List<String>> getDataFactoryId(String id) {
        List<JvsChartPageCanvas> canvasList = jvsChartPageCanvasService.list(Wrappers.lambdaQuery(JvsChartPageCanvas.class).eq(JvsChartPageCanvas::getChartId,id));
        List<String> collect = canvasList.stream()
                .map(JvsChartPageCanvas::getDataJson)
                .filter(ObjectUtil::isNotNull)
                .map(JSONObject::parseArray)
                .filter(CollectionUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .map(JSONUtil::parseObj)
                .map(e -> e.getJSONObject("dataSource"))
                .filter(ObjectUtil::isNotNull)
                .map(e -> e.getStr("executeName"))
                .filter(ObjectUtil::isNotNull)
                .distinct()
                .collect(Collectors.toList());
        return R.ok(collect);
    }
}
