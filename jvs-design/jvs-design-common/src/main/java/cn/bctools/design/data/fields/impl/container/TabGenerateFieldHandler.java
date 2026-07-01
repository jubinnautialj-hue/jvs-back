package cn.bctools.design.data.fields.impl.container;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.form.item.TabGenerateItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.function.entity.dto.TableType;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.bctools.design.data.fields.impl.container.TableFormFieldHandler.TABLE_TYPE;

/**
 * 表单字段: 选项卡,开启了数据脱离生生成的字段组件类型处理器
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "选项卡生成组件", type = DataFieldType.tabGenerate)
public class TabGenerateFieldHandler implements IDataFieldHandler<TabGenerateItemHtml> {

    @Override
    public void filterOrDataLinkage(String appId, Map<String, ? extends FieldBasicsHtml> fieldMap, String key, TabGenerateItemHtml e, Map<String, Object> map, Integer index, String... parentPath) {
        Map<String, IDataFieldHandler> fieldHandlerMap = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);
        //其它容器类型,如果内部设计结构里面包含此控件，需要进行递归处理
        //如果只有一个上级，并且只是赋予表格整体字段的时候，则可以复制,不能是行内操作
        boolean tableTypeBoolean = ObjectNull.isNotNull(index) && SystemThreadLocal.get(TABLE_TYPE).equals(TableType.add);
        if (ObjectNull.isNull(key)) {
            return;
        }
        //判断设计和 key是否不为空
        boolean notNull = ObjectNull.isNotNull(e.getDesignJson(), key);
        //设计中是否存在 Key
        boolean contains = false;
        if (notNull) {
            //如果为空直接退出
            contains = e.getDesignJson().toString().contains(key);
        }
        //都满足的情况下
        boolean jsonKey = notNull && contains;
        //匹配都满足的情况下
        if (jsonKey || tableTypeBoolean) {
            Map<String, FieldBasicsHtml> fieldPublicHtmls = e.getColumn().stream().collect(Collectors.toMap(FieldPublicHtml::getProp, Function.identity()));
            for (FieldBasicsHtml publicHtml : fieldPublicHtmls.values()) {
                IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(publicHtml.getType().getDesc());
                FieldBasicsHtml html = iDataFieldHandler.toHtml(publicHtml.getDesignJson());
                //根据不同的tab，可能两个tab下的不同表格都会发生变化，所以路径，需要重置一下，就new 新数组
                String[] parentPath1 = new String[]{e.getProp(), html.getProp()};
                iDataFieldHandler.filterOrDataLinkage(appId, fieldPublicHtmls, key, html, map, index, parentPath1);
            }
        }
    }

    @Override
    public void next(List<ElementVo> list, TabGenerateItemHtml html, Map<String, IDataFieldHandler> handlerMap, ElementVo e) {
        //选项卡的下级
        //获取下级的字段控件
        List<FieldBasicsHtml> publicHtmls = html.getColumn();
        if (ObjectNull.isNull(publicHtmls)) {
            //如果为空直接跳过
            return;
        }
        //添加tab级
        ElementVo elementVo = new ElementVo()
                .setFieldType(html.getType().name()).setId(e.getId())
                .setType("请求入参").setName(e.getName())
                .setShortName(e.getShortName())
                .setJvsParamType(JvsParamType.object);
        list.add(elementVo);
        for (FieldPublicHtml baseItemHtml : publicHtmls) {
            //添加组件
            ElementVo paramType = new ElementVo()
                    .setId(elementVo.getId() + StrUtil.DOT + baseItemHtml.getProp())
                    .setType("请求入参").setName(elementVo.getName() + StrUtil.DOT + baseItemHtml.getLabel())
                    .setFieldType(baseItemHtml.getType().name())
                    .setShortName(elementVo.getShortName() + StrUtil.DOT + baseItemHtml.getLabel())
                    .setJvsParamType(JvsParamType.object);
            list.add(paramType);

            IDataFieldHandler iDataFieldHandler = handlerMap.get(baseItemHtml.getType().getDesc());
            FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(baseItemHtml.getDesignJson());
            //判断有没有下级
            iDataFieldHandler.next(list, publicHtml, handlerMap, paramType);
        }
    }

    @Override
    public void next(List<ElementVo> list, TabGenerateItemHtml html, Function<FieldBasicsHtml, ElementVo> function, Map<String, IDataFieldHandler> handlerMap, String name, String prop) {
        throw new BusinessException("生成容器组件不支持公式");
    }

    @Override
    public void tableSetData(Map<String, IDataFieldHandler> handlerMap, Map<String, FieldBasicsHtml> fieldsMap, TabGenerateItemHtml html, Map<String, Object> data, String... parentPath) {
        //选项卡的下级
        List<FieldBasicsHtml> baseItemHtmls = html.getColumn();
        if (ObjectNull.isNull(baseItemHtmls)) {
            //如果为空直接跳过
            return;
        }
        for (FieldBasicsHtml baseItemHtml : baseItemHtmls) {
            if (baseItemHtml.getType().equals(DataFieldType.tableForm)) {
                //下一层，其它的不处理
                IDataFieldHandler iDataFieldHandler = handlerMap.get(baseItemHtml.getType().getDesc());
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(baseItemHtml.getDesignJson());
                List<String> strings = new ArrayList<>();
                strings.add(baseItemHtml.getProp());
                String[] parentPath1 = strings.toArray(new String[0]);
                //处理下级
                iDataFieldHandler.tableSetData(handlerMap, fieldsMap, publicHtml, data, parentPath1);
            }
        }
    }

    @Override
    public Object getEchoValue(TabGenerateItemHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        Map<String, IDataFieldHandler> dataFieldHandlerMap = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);
        //判断是否添加了数据脱离 如果打开了数据脱离,获取的数据将不根据属性判断
        Map<String, FieldPublicHtml> fieldPublicHtmls = fieldDto.getColumn()
                .stream()
                .collect(Collectors.toMap(FieldPublicHtml::getProp, Function.identity()));
        for (Map.Entry<String, FieldPublicHtml> entry : fieldPublicHtmls.entrySet()) {
            FieldPublicHtml fieldPublicHtml = entry.getValue();
            IDataFieldHandler iDataFieldHandler = dataFieldHandlerMap.get(fieldPublicHtml.getType().getDesc());
            if (ObjectNull.isNotNull(iDataFieldHandler, fieldPublicHtml.getDesignJson())) {
                FieldBasicsHtml html = iDataFieldHandler.toHtml(fieldPublicHtml.getDesignJson());
                //直接处理此字段类型
                String path = html.getProp();
                Object eval = JvsJsonPath.read(data, path);
                //获取子集转换
                Object echoValue = iDataFieldHandler.getEcho(html, eval, override, lineData, path);
                //如果是表格，也直接进行替换
                if (override || iDataFieldHandler instanceof TableFormFieldHandler) {
                    JSONPath.set(data, path, echoValue);
                } else {
                    JSONPath.set(data, path + DynamicDataUtils.SUFFIX_ECHO, echoValue);
                }
            }
        }
        lineData.put(fieldDto.getProp(), data);
        //将源数据返回,tab返回和不返回都是一样的
        return data;
    }
}
