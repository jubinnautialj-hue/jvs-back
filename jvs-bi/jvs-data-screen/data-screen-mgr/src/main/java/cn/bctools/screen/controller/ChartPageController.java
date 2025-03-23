package cn.bctools.screen.controller;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.screen.dto.ChartMoveDto;
import cn.bctools.screen.dto.CopyDto;
import cn.bctools.screen.dto.ExportScreenDto;
import cn.bctools.screen.entity.ChartPage;
import cn.bctools.screen.entity.JvsChartPageCanvas;
import cn.bctools.screen.entity.SysMenu;
import cn.bctools.screen.service.ChartPageService;
import cn.bctools.screen.service.JvsChartPageCanvasService;
import cn.bctools.screen.service.SysMenuService;
import cn.bctools.screen.utils.AuthUtils;
import cn.bctools.screen.utils.DataUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 页面配置
 *
 * @author zqs
 */
@Api(tags = "大屏-页面配置")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/chart/design")
public class ChartPageController {

    ChartPageService chartPageService;
    JvsChartPageCanvasService jvsChartPageCanvasService;
    SysMenuService sysMenuService;

    @ApiOperation("创建大屏设计")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<ChartPage> save(@RequestBody ChartPage page) {
        if (ObjectNull.isNull(page.getName())) {
            page.setName("未命名大屏");
        }
        SysMenu sysMenu = sysMenuService.getById(page.getType());
        AuthUtils.check(sysMenu, OperationType.新增);

        page.setIsDeploy(Boolean.TRUE);

        //获取此类型的所有数据永远排序
        long count = chartPageService.count(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getId, page.getId()));
        int sort = new BigDecimal(count + 1).intValue();
        page.setSort(sort);

        chartPageService.save(page);
        //保存画布
        jvsChartPageCanvasService.saveFormal(page.getId(),page.getCanvasList(),page.getDeleteCanvasIds());
        return R.ok(page);
    }


    @ApiOperation("基础信息修改")
    @PutMapping("/baseInfo")
    @Transactional(rollbackFor = Exception.class)
    public R<ChartPage> updateBaseInfo(@RequestBody ChartPage chartPage) {
        chartPageService.updateById(chartPage);
        return R.ok(chartPage);
    }


    @ApiOperation("获取页面详情")
    @GetMapping("/detail/{id}")
    public R<ChartPage> detail(@ApiParam("页面id") @PathVariable("id") String id) {
        ChartPage byId = chartPageService.getById(id);
        List<JvsChartPageCanvas> list = jvsChartPageCanvasService.list(Wrappers.lambdaQuery(JvsChartPageCanvas.class).select(JvsChartPageCanvas.class, e -> !StrUtil.equals(e.getProperty(), Get.name(JvsChartPageCanvas::getDataJson))).eq(JvsChartPageCanvas::getChartId, id));
        if(CollectionUtil.isNotEmpty(list)){
            list.sort(Comparator.comparing(JvsChartPageCanvas::getSort));
            byId.setCanvasList(list);
        }
        AuthUtils.setOperation(byId);
        return R.ok(byId);
    }

    @ApiOperation("获取画布详情")
    @GetMapping("/detail/{id}/{canvasId}/canvas")
    public R<JvsChartPageCanvas> detail(@ApiParam("大屏或模板id") @PathVariable("id") String id,@ApiParam("页面id") @PathVariable("canvasId") String canvasId) {
        JvsChartPageCanvas one = jvsChartPageCanvasService.getOne(Wrappers.lambdaQuery(JvsChartPageCanvas.class).eq(JvsChartPageCanvas::getId, canvasId));
        return R.ok(one);
    }

    @ApiOperation("修改大屏设计页面")
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public R<ChartPage> updateById(@RequestBody ChartPage page) {
        ChartPage one = chartPageService.getOne(Wrappers.lambdaQuery(ChartPage.class).select(ChartPage::getId).eq(ChartPage::getId, page.getId()));
        if (one == null) {
            return R.failed("设计不存在");
        }
        page.setDataJson(null);
        chartPageService.updateById(page);
        //保存画布
        jvsChartPageCanvasService.saveFormal(page.getId(),page.getCanvasList(), page.getDeleteCanvasIds());
        return R.ok(page);
    }

    @ApiOperation("获取设计结构")
    @PostMapping("/design/{id}/{canvasId}/{isFirst}")
    public R<ChartPage> design(@PathVariable("id") String id,@PathVariable("canvasId") String canvasId,  @PathVariable Boolean isFirst, @RequestBody(required = false) Map<String, String> dataMap) {
        ChartPage page = chartPageService.getById(id);
        if (page == null) {
            return R.failed("设计不存在");
        }
        return R.ok(page);
    }


    @ApiOperation("删除")
    @DeleteMapping("/del/{id}")
    public R<Boolean> design(@PathVariable String id) {
        ChartPage page = chartPageService.getById(id);
        if (page == null) {
            return R.failed("设计不存在");
        }
        jvsChartPageCanvasService.removeByParentId(id);
        return R.ok(chartPageService.removeById(id));
    }


    @ApiOperation("移动不管是调整顺序还是移动到其它目录")
    @PutMapping("/move")
    @Transactional(rollbackFor = Exception.class)
    public R<List<ChartPage>> move(@RequestBody ChartMoveDto chartMoveDto) {

        SysMenu sysMenu = sysMenuService.getById(chartMoveDto.getParentId());
        AuthUtils.check(sysMenu, OperationType.新增);

        //获取数据
        ChartPage byId = chartPageService.getById(chartMoveDto.getId());
        boolean verify = AuthUtils.verify(byId, OperationType.移动);
        if(!verify){
            return R.failed("暂无权限,请联系此大屏创建者");
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
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSort(i);
        }
        chartPageService.updateBatchById(list);
        return R.ok(list);
    }

    @ApiOperation("复制大屏")
    @PostMapping("/copy/{id}/{typeId}")
    public R<ChartPage> byId(@ApiParam("大屏id") @PathVariable("id") String id
            ,@ApiParam("目录id") @PathVariable("typeId") String typeId
            ,@RequestBody CopyDto dto) {

        SysMenu sysMenu = sysMenuService.getById(typeId);
        AuthUtils.check(sysMenu, OperationType.新增);

        ChartPage byId = chartPageService.getById(id);
        if(ObjectUtil.isNull(byId)){
            return R.failed("大屏不存在");
        }
        boolean verify = AuthUtils.verify(byId, OperationType.复制);
        if(!verify){
            return R.failed("未拥有复制权限");
        }
        byId.setId(null).setType(typeId).setName(dto.getName())
                .setLongLink(null)
                .setPassword(null)
                .setShortLink(null)
                .setShortLinkValue(null)
                .setShareIs(Boolean.FALSE);
        byId.setCreateBy(null);
        byId.setCreateTime(null);
        byId.setCreateById(null);
        byId.setUpdateBy(null);
        byId.setUpdateTime(null);
        chartPageService.save(byId);

        List<JvsChartPageCanvas> list = jvsChartPageCanvasService.list(Wrappers.<JvsChartPageCanvas>lambdaQuery().eq(JvsChartPageCanvas::getChartId, id));

        List<JvsChartPageCanvas> canvasList = list.stream().peek(e -> e.setId(null).setChartId(byId.getId())).collect(Collectors.toList());
        jvsChartPageCanvasService.saveBatch(canvasList);
        return R.ok(byId);
    }

    @ApiOperation("导出")
    @GetMapping("/out/{id}")
    public void out(@ApiParam("大屏id") @PathVariable("id") String id, HttpServletResponse response) {
        ChartPage screen = chartPageService.getById(id);
        List<JvsChartPageCanvas> list = jvsChartPageCanvasService.list(Wrappers.lambdaQuery(JvsChartPageCanvas.class).eq(JvsChartPageCanvas::getChartId, id));

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(URLUtil.encode( screen.getName()+ ".screen", StandardCharsets.UTF_8)));
        response.setStatus(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputStream);

            ExportScreenDto template = new ExportScreenDto();
            ExportScreenDto.ScreenDto screenDto = BeanCopyUtil.copy(screen, ExportScreenDto.ScreenDto.class);
            template.setScreen(screenDto);

            if(CollectionUtil.isNotEmpty(list)){
                List<ExportScreenDto.CanvasDto> canvasDtoList = BeanCopyUtil.copys(list, ExportScreenDto.CanvasDto.class);
                template.setCanvasList(canvasDtoList);
            }

            String s = JSONObject.toJSONString(template);
            String encode = DataUtil.encode(s.getBytes());
            jsonGenerator.writeStartObject();

            jsonGenerator.writeFieldName("data");
            jsonGenerator.writeObject(encode);

            jsonGenerator.writeEndObject();
            jsonGenerator.flush();
            jsonGenerator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("导入")
    @PostMapping("/in/{type}")
    public R in(@ApiParam("分类id")@PathVariable("type")String type,@RequestParam("file") MultipartFile file) throws IOException {

        SysMenu sysMenu = sysMenuService.getById(type);
        AuthUtils.check(sysMenu, OperationType.新增);

        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        String s = new String(bytes);
        String data = JSONObject.parse(s).getString("data");
        String decode = DataUtil.decode(data.getBytes());
        ExportScreenDto exportScreenDto = JSONUtil.toBean(decode, ExportScreenDto.class);
        String screenId = IdGenerator.getIdStr();
        ExportScreenDto.ScreenDto screenDto = exportScreenDto.getScreen();
        ChartPage screen = BeanCopyUtil.copy(screenDto, ChartPage.class);
        screen.setId(screenId).setType(type);

        chartPageService.save(screen);
        List<ExportScreenDto.CanvasDto> canvasDtoList = exportScreenDto.getCanvasList();
        if (CollectionUtil.isNotEmpty(canvasDtoList)) {
            List<JvsChartPageCanvas> canvasList = BeanCopyUtil.copys(canvasDtoList, JvsChartPageCanvas.class);
            canvasList.forEach(e -> {
                String dataJson = e.getDataJson();
                if (StrUtil.isNotBlank(dataJson)&&JSONUtil.isTypeJSONArray(dataJson)) {
                    List<cn.hutool.json.JSONObject> newDataJson = JSONUtil.parseArray(dataJson).stream().map(JSONUtil::parseObj).peek(v -> v.set("id", IdUtil.fastSimpleUUID())).collect(Collectors.toList());
                    e.setDataJson(JSONUtil.toJsonStr(newDataJson));
                }
                e.setChartId(screenId);
            });
            jvsChartPageCanvasService.saveBatch(canvasList);
        }

        return R.ok();
    }

}
