package cn.bctools.design.crud.service.impl;

import cn.bctools.auth.api.api.*;
import cn.bctools.auth.api.dto.EnvironmentVariableDto;
import cn.bctools.auth.api.dto.SysRoleDto;
import cn.bctools.auth.api.dto.SysTenantDto;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.dto.PrintTemplateDto;
import cn.bctools.design.crud.entity.PrintTemplate;
import cn.bctools.design.crud.entity.enums.DesignTypeEnum;
import cn.bctools.design.crud.mapper.PrintTemplateMapper;
import cn.bctools.design.crud.service.PrintTemplateService;
import cn.bctools.design.crud.vo.PrintOtherField;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.impl.container.TabFieldHandler;
import cn.bctools.design.data.fields.impl.container.TabGenerateFieldHandler;
import cn.bctools.design.data.fields.impl.container.TableFormFieldHandler;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.progress.ProgressPrintResDto;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.service.impl.FlowDynamicDataServiceImpl;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.SysParamEnums;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.handler.IJvsFunction;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.impl.SysParamImpl;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.web.utils.WebUtils;
import cn.bctools.word.utils.WordPdfUtil;
import cn.bctools.word.utils.WordVariableReplaceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 打印模板 服务实现类
 */
@Slf4j
@Service
@AllArgsConstructor
public class PrintTemplateServiceImpl extends ServiceImpl<PrintTemplateMapper, PrintTemplate> implements PrintTemplateService, IJvsDesigner {

    private final DynamicDataService dynamicDataService;
    private final Map<String, IDataFieldHandler> fieldHandlerMap;
    private final DataFieldService dataFieldService;
    private final ExpressionHandler handler;
    private final FlowTaskService flowTaskService;
    private final FlowDynamicDataService flowDynamicDataService;
    private OssProperties ossProperties;
    private final MapperMethodHandler mapperMethodHandler;
    private final AuthUserServiceApi authUserServiceApi;
    private final UserExtensionServiceApi extensionServiceApi;
    private final EnvironmentVariableApi environmentVariableApi;
    private final AuthRoleServiceApi authRoleServiceApi;
    private final AuthTenantServiceApi authTenantServiceApi;

    @Override
    public List<PrintTemplate> getDesignAll(String appId, String designId) {
        return list(Wrappers.<PrintTemplate>lambdaQuery()
                .eq(PrintTemplate::getDesignId, designId)
                .eq(PrintTemplate::getJvsAppId, appId)
                .select(PrintTemplate.class, t -> Boolean.FALSE.equals(t.getColumn().equals(Get.name(PrintTemplate::getDesign))))
                .orderByAsc(PrintTemplate::getCreateTime)
        );
    }

    @Override
    public List<PrintTemplate> getDesignAvailableAll(String appId, String designId) {
        return list(Wrappers.<PrintTemplate>lambdaQuery()
                .eq(PrintTemplate::getDesignId, designId)
                .eq(PrintTemplate::getJvsAppId, appId)
                .eq(PrintTemplate::getEnableFlag, Boolean.TRUE)
                .select(PrintTemplate.class, t -> Boolean.FALSE.equals(t.getColumn().equals(Get.name(PrintTemplate::getDesign))))
                .orderByAsc(PrintTemplate::getCreateTime)
        );
    }

    @SneakyThrows
    @Override
    public void filePreview(String appId, String id, String dataModelId, String designId, String dataId) {
        PrintTemplate printTemplate = Optional.ofNullable(getOne(Wrappers.<PrintTemplate>lambdaQuery()
                .eq(PrintTemplate::getId, id)
                .eq(PrintTemplate::getJvsAppId, appId)
                .ne(PrintTemplate::getDesignType, DesignTypeEnum.CUSTOMIZE))).orElseThrow(() -> new BusinessException("打印模板不存在"));
        if (Boolean.FALSE.equals(printTemplate.getEnableFlag())) {
            throw new BusinessException("该模板未启用");
        }
        if (StringUtils.isBlank(printTemplate.getFileUrl())) {
            throw new BusinessException("未上传打印模板文件");
        }
        // 获取数据
        Map<String, Object> data = getPrintData(appId, dataModelId, designId, dataId, printTemplate);
        // 获取模板文件转pdf
        byte[] fileBytes = HttpUtil.downloadBytes(printTemplate.getFileUrl());
        HttpServletResponse response = WebUtils.getResponse();
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        ServletOutputStream outputStream = response.getOutputStream();
        long l = System.currentTimeMillis();
        WordprocessingMLPackage template;
        // 当前仅支持word模板
        if (DesignTypeEnum.WORD.equals(printTemplate.getDesignType())) {
            // 得到word并替换数据
            template = WordVariableReplaceUtil.template(fileBytes, data);
            // word转pdf
            WordPdfUtil.convertDocx2Pdf(template, outputStream);
        } else {
            throw new BusinessException("不支持的打印模板");
        }
        log.debug("数据填写并转换pdf,耗时: " + (System.currentTimeMillis() - l));
    }

    /**
     * 获取待打印的数据
     *
     * @return
     */
    private Map<String, Object> getPrintData(String appId, String dataModelId, String designId, String dataId, PrintTemplate printTemplate) {
        DynamicDataUtils.dataModelScope(dataModelId);
        //查询单条数据
        Map<String, Object> data = dynamicDataService.querySingle(appId, dataModelId, dataId, false);
        //查询所有字段
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, dataModelId, designId, true, false)
                .stream()
                .filter(e -> ObjectNull.isNotNull(e.getType()))
                //过滤出只有这个表单的字段
                .collect(Collectors.toList());
        Map<String, FieldBasicsHtml> fieldsMap = fields.stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity()));

        //转换数据
        //筛选出字段中存在数据关联的表格字段,进行第一次数据初始化的查询，并过滤出列表中关联的数据
        for (FieldBasicsHtml e : fields) {
            IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(e.getType().getDesc());
            if (ObjectNull.isNotNull(e.getDesignJson(), iDataFieldHandler)) {
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(e);
                //如果是表格类型,或选项卡类型，需要进行数据筛选进行处理
                if (iDataFieldHandler instanceof TableFormFieldHandler || iDataFieldHandler instanceof TabFieldHandler || iDataFieldHandler instanceof TabGenerateFieldHandler) {
                    iDataFieldHandler.tableSetData(fieldHandlerMap, fieldsMap, publicHtml, data, publicHtml.getProp());
                }
            }
        };
        data = dynamicDataService.echo(data, fields, true);

        //获取表单字段
        List<ElementVo> formParams = getFormParams(printTemplate.getDesignId());
        // 封装系统参数信息
        packSys(data);
        // 封装富文本数据
        packHtmlData(formParams, data);
        // 封装工作流数据
        packWorkFlowData(data);
        return data;
    }

    /**
     * 封装数据创建人信息
     *
     * @param data 数据
     */
    private void packSys(Map<String, Object> data) {
        String createUserId = (String) data.get(Get.name(DynamicDataPo::getCreateById));
        if (ObjectNull.isNull(createUserId)) {
            return;
        }
        //查询创建人相关信息
        UserDto userDto = authUserServiceApi.getById(createUserId).getData();
        if (ObjectNull.isNull(userDto)) {
            return;
        }

        List<EnvironmentVariableDto> list = environmentVariableApi.getAll(ModeTypeEnum.getType(ModeUtils.getMode().getValue())).getData();
        list.forEach(e -> data.put("variable" + e.getLabel(), e.getValue()));

        Map<String, Object> exceptions = userDto.getExceptions();
        Map<String, String> extensionMap = extensionServiceApi.field().getData();
        extensionMap = MapUtil.reverse(extensionMap);
        String prefix = "SYS";
        if (ObjectNull.isNotNull(extensionMap) && ObjectNull.isNotNull(exceptions)) {
            extensionMap.forEach((k, v) -> {
                data.put(prefix + SysParamImpl.USER_EXTENSION + k, exceptions.getOrDefault(v, ""));
            });
        }


        for (SysParamEnums sysParam : SysParamEnums.values()) {
            switch (sysParam) {
                case RealName:
                    data.put(prefix + SysParamEnums.RealName.getId(), userDto.getRealName());
                    break;
                case UserId:
                    data.put(prefix + SysParamEnums.UserId.getId(), userDto.getId());
                    break;
                case AccountName:
                    data.put(prefix + SysParamEnums.AccountName.getId(), userDto.getAccountName());
                    break;
                case Phone:
                    data.put(prefix + SysParamEnums.Phone.getId(), userDto.getPhone());
                    break;
                case Email:
                    data.put(prefix + SysParamEnums.Email.getId(), userDto.getEmail());
                    break;
                case HeadImg:
                    data.put(prefix + SysParamEnums.HeadImg.getId(), userDto.getHeadImg());
                    break;
                case DeptId:
                    data.put(prefix + SysParamEnums.DeptId.getId(), userDto.getDeptId());
                    break;
                case DeptName:
                    data.put(prefix + SysParamEnums.DeptName.getId(), userDto.getDeptName());
                    break;
                case DeptCode:
                    data.put(prefix + SysParamEnums.DeptCode.getId(), userDto.getDeptCode());
                    break;
                case UserRole:
                    String roleName = authRoleServiceApi.getByIds(userDto.getRoleIds()).getData().stream()
                            .map(SysRoleDto::getRoleName)
                            .collect(Collectors.joining(","));
                    data.put(prefix + SysParamEnums.UserRole.getId(), roleName);
                    break;
                case UserEmployeeNo:
                    data.put(prefix + SysParamEnums.UserEmployeeNo.getId(), userDto.getEmployeeNo());
                    break;
                case TenantCode:
                    SysTenantDto sysTenant = Optional.ofNullable(authTenantServiceApi.getById(userDto.getTenantId()).getData()).orElseGet(SysTenantDto::new);
                    data.put(prefix + SysParamEnums.TenantCode.getId(), sysTenant.getName());
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 获取表单字段
     *
     * @param designId 设计id
     * @return 表单字段
     */
    private List<ElementVo> getFormParams(String designId) {
        // 记录设计id
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, designId);
        Map<String, Map<String, List<ElementVo>>> params = handler.getAllParamElement(EnvConstant.PRINT_FILE_FORM_ITEM_VALUE, "");
        if (MapUtils.isEmpty(params)) {
            return Collections.emptyList();
        }
        Map<String, List<ElementVo>> param = params.get(IJvsParam.NAME);
        if (MapUtils.isEmpty(params)) {
            return Collections.emptyList();
        }
        // 获取表单字段(key为PrintFormItemParam的groupName)
        return param.get("表单字段");
    }

    /**
     * 获取富文本数据
     *
     * @param formParams 表单字段
     * @return 富文本数据
     */
    private void packHtmlData(List<ElementVo> formParams, Map<String, Object> data) {
        if (CollectionUtils.isEmpty(formParams)) {
            return;
        }
        List<String> htmlKeys = formParams.stream().filter(p -> DataFieldType.htmlEditor.name().equals(p.getFieldType()))
                .map(p -> WordVariableReplaceUtil.getPureKey(p.getId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(htmlKeys)) {
            return;
        }
        // 封装富文本数据
        String dataJson = JSON.toJSONString(data);
        htmlKeys.forEach(key -> {
            Object value = JvsJsonPath.read(dataJson, key);
            if (ObjectNull.isNotNull(value)) {
                String htmlContent = value.toString();
                Document document = Jsoup.parse(htmlContent);
                // 处理富文本中的图片：将相对地址改为绝对地址
                Elements imgs = document.select("img");
                for (Element img : imgs) {
                    String url = img.attr("src");
                    if (ObjectNull.isNull()) {
                        img.remove();
                    }
                    if (url.startsWith("http") || url.startsWith("data:image/")) {
                        continue;
                    }
                    url = "http://" + ossProperties.getEndpoint() + url;
                    img.attr("src", url);
                }
                JSONPath.set(data, key, document.html());
            }
        });
    }

    /**
     * 封装工作流数据
     *
     * @param data
     */
    private void packWorkFlowData(Map<String, Object> data) {
        // 封装审批过程
        List<ProgressPrintResDto> progress = new ArrayList<>();
        FlowDynamicDataServiceImpl.FlowTaskModelData flowTaskModelData = flowDynamicDataService.getFlowTaskData(String.valueOf(data.get("id")));
        if (StringUtils.isNotBlank(flowTaskModelData.getId())) {
            progress = flowTaskService.getProgressPrint(flowTaskModelData.getId());
        }

        data.put(PrintOtherField.Flow.WORKFLOW_PROGRESS.getKey(), JSON.parseArray(JSON.toJSONString(progress)));
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<PrintTemplate>lambdaQuery().eq(PrintTemplate::getJvsAppId, appId));
    }
}
