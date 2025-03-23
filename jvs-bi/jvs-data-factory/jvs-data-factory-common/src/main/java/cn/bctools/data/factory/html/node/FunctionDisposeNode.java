package cn.bctools.data.factory.html.node;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.SysFunction;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.function.FunctionReturnFieldProcess;
import cn.bctools.data.factory.html.node.params.FunctionDisposeParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.service.SysFunctionService;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author guojing
 * 函数处理
 */
@Slf4j
@Data
@Service
public class FunctionDisposeNode implements Frun<FunctionDisposeParams> {
    @Autowired
    SysFunctionService sysFunctionService;

    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, FunctionDisposeParams functionDisposeParams) {
        //通过部分的数据只会生成基础的
        DorisConfig bean = SpringContextUtil.getBean(DorisConfig.class);
        String targetColumn = targetFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        //格式首尾
        targetColumn = "`" + targetColumn + "`";
        sql.append("INSERT INTO ").append(bean.getLibraryName()).append(".").append(targetTableName).append(" (").append(targetColumn).append(") ");
        String sourceColumn;
        //如果输入源 与 目标源字段一致就直接使用输入源的 减少计算
        sourceColumn = sourceFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        sourceColumn = "`" + sourceColumn + "`";
        sql.append("SELECT ").append(sourceColumn);
        //添加函数
        functionDisposeParams.getFunctionDispose().forEach(e -> {
            String function = e.getFunctionStr().replaceAll("\\$\\{(.*?)}", "$1");
            if (function.isEmpty()) {
                function = "NULL";
            }
            sql.append(",").append(function);
        });
        sql.append(" FROM ").append(bean.getLibraryName()).append(".").append(sourceTable);
        if (addOrderBy) {
            sql.append(" ORDER BY ").append(Constant.DORIS_ODS_KEY).append(" desc");
        }
        return new ArrayList<>();
    }

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, FunctionDisposeParams nodeHtml) {
        String next = linkBody.keySet().iterator().next();
        FData fData = new FData();
        //中间表名称
        String nodeDocumentName = nodeHtml.getTableName();
        fData.setDocumentName(nodeDocumentName);
        //上一个节点的数据库名称
        StringBuffer sql = new StringBuffer();
        String documentName = linkBody.get(next).getDocumentName();
        List<DataSourceField> title = linkBody.get(next).getTitle();
        List<DataSourceField> sourceFieldList = JSONArray.parseArray(JSONObject.toJSONString(title), DataSourceField.class);
        List<FunctionDisposeParams.FunctionDisposeObj> functionDispose = nodeHtml.getSourceData().getFunctionDispose();
        List<FunctionDisposeParams.FunctionDisposeObj> list = functionDispose.stream().filter(e -> StrUtil.isNotBlank(e.getFunctionStr())).collect(Collectors.toList());
        if (!list.isEmpty()) {
            addTitle(title, functionDispose);
            List<Object> objects = createSql(sourceFieldList, title, sql, documentName, nodeDocumentName, Boolean.TRUE, Boolean.FALSE, nodeHtml.getSourceData());
            this.save(sql.toString(), nodeDocumentName, title, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
        } else {
            fData.setDocumentName(documentName);
        }
        return fData.setTitle(title);
    }

    /**
     * 根据函数生成title
     * ${JVS_CONCATENAT}("-",${JVS_CONTRACT}(`990623768667656192`,`990623768596353024`),`990623768940285952`)
     *
     * @param title           上个节点的title
     * @param functionDispose 函数表达式
     */
    private void addTitle(List<DataSourceField> title, List<FunctionDisposeParams.FunctionDisposeObj> functionDispose) {
        functionDispose.forEach(e -> {
            DataSourceField dataSourceField = new DataSourceField()
                    .setDataId(title.get(0).getDataId())
                    .setFieldKey(e.getKey())
                    .setIsShow(Boolean.TRUE)
                    .setFieldName(e.getName());
            String functionStr = e.getFunctionStr();
            if (functionStr.isEmpty()) {
                functionStr = "";
            }
            if (!functionStr.matches("\\$\\{(.*?)}.*")) {
                if (StrUtil.containsAny(functionStr, "+", "-", "*", "/")) {
                    dataSourceField.setLength(18)
                            .setPrecision(5)
                            .setDorisType(DataFieldTypeEnum.DECIMAL.getCreateTable())
                            .setFieldType(DataFieldTypeEnum.DECIMAL)
                            .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.数字);
                } else {
                    dataSourceField
                            .setDorisType(DataFieldTypeEnum.STRING.getCreateTable())
                            .setFieldType(DataFieldTypeEnum.STRING)
                            .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.字符串);
                }
            } else {
                functionStr = functionStr.replaceAll("\\$\\{(.*?)}", "$1");
                //获取第一个函数 通过第一个函数确认返回值类型 并生成 DataSourceField对象
                String functionName = functionStr.substring(0, functionStr.indexOf("("));
                //获取函数的其他信息
                SysFunction function = sysFunctionService.getOne(new LambdaQueryWrapper<SysFunction>().eq(SysFunction::getName, functionName));
                if (ObjUtil.isNull(function)) {
                    throw new BusinessException("字段拓展-未知函数");
                }
                dataSourceField.setDataFieldTypeClassify(function.getReturnDataFieldTypeClassify())
                        .setLength(function.getLength())
                        .setPrecision(function.getFieldPrecision())
                        .setFieldType(function.getReturnFieldType())
                        .setDorisType(function.getDorisReturnType());
                if (StrUtil.isNotBlank(function.getFieldClass())) {
                    try {
                        Class<? extends FunctionReturnFieldProcess> aClass = (Class<? extends FunctionReturnFieldProcess>) Class.forName(function.getFieldClass());
                        FunctionReturnFieldProcess functionReturnFieldProcess = SpringContextUtil.getBean(aClass);
                        functionReturnFieldProcess.setDataSourceField(function, functionStr, dataSourceField);
                    } catch (Exception exception) {
                        log.error("获取函数处理类错误:", exception);
                        throw new BusinessException("获取函数处理类错误");
                    }
                }
                if (function.getReturnDynamic()) {
                    dataSourceField.setDataFieldTypeClassify(e.getDataSourceField().getDataFieldTypeClassify())
                            .setLength(e.getDataSourceField().getLength())
                            .setPrecision(e.getDataSourceField().getPrecision())
                            .setFieldType(e.getDataSourceField().getFieldType())
                            .setDorisType(e.getDataSourceField().getDorisType());
                }
                if (function.getUserSelectType()) {
                    dataSourceField.setDataFieldTypeClassify(e.getDataSourceField().getFieldType().getClassifyEnum())
                            .setDorisType(e.getDataSourceField().getFieldType().getCreateTable())
                            .setLength(e.getDataSourceField().getLength())
                            .setPrecision(e.getDataSourceField().getPrecision())
                            .setFieldType(e.getDataSourceField().getFieldType());
                }
            }
            title.add(dataSourceField);
        });

    }


}
