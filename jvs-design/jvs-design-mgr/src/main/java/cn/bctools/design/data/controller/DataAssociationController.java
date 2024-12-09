package cn.bctools.design.data.controller;


import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.crud.entity.CrudAssociationPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.service.JvsCrudAssociationService;
import cn.bctools.design.data.fields.dto.form.AssociationHtml;
import cn.bctools.design.data.fields.dto.form.item.AssociationItemHtml;
import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.log.annotation.Log;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "[data]规则处理")
@RestController
@AllArgsConstructor
@RequestMapping("/app/use/{appId}/data/association")
public class DataAssociationController {

    private static final String ID = "id";

    FormService formService;
    CrudPageService pageService;
    DataFieldService dataFieldService;
    DynamicDataService dynamicDataService;
    JvsCrudAssociationService associationService;
    RuleRunService ruleRunService;

    /**
     * 根据设计和应用触发规则
     *
     * @param dataModelId    数据模型id
     * @param designId       设计id
     * @param appId          应用Id
     * @param permissionFlag 按钮标识
     * @param ruleKey        逻辑的Key
     * @return 数据集合
     */
    @Log
    @ApiOperation("触发规则")
    @PostMapping("/{dataModelId}/{designId}/{permissionFlag}")
    @Transactional(rollbackFor = Exception.class)
    public R dataAssociation(@PathVariable String dataModelId, @PathVariable String designId, @PathVariable String permissionFlag,
                             @RequestParam(value = "ruleKey", defaultValue = "") String ruleKey, @RequestBody Map<String, Object> map, @PathVariable String appId) {
        //判断应用是否启用
        CrudAssociationPo one = associationService.getOne(Wrappers.query(new CrudAssociationPo().setDesignId(designId).setJvsAppId(appId).setDataModelId(dataModelId).setPermissionFlag(permissionFlag)));
        if (ObjectNull.isNull(one)) {
            return R.ok();
        }
        if (ObjectNull.isNotNull(ruleKey)) {
            ruleRunService.run(ruleKey, map);
        }
        //添加权限
        DynamicDataUtils.checkPermit();
        //如果有id直接修改如果没有就更新
        if (map.containsKey(ID)) {
            String id = map.get(ID).toString();
            dynamicDataService.update(appId, dataModelId, id, map);
        } else {
            dynamicDataService.checkDataModel(map, dataModelId);
            dynamicDataService.save(appId, dataModelId, map);
        }
        //判断逻辑是否为空，如果不为空，则操作完数据后，再运行逻辑
        //获取配置的规则
        //判断设计和模型是否一致
        //根据标识， 设计，应用获取 库里面的规则
        List<AssociationHtml> list = JSONUtil.toList(JSONObject.toJSONString(one.getData()), AssociationHtml.class)
                .stream()
                .filter(e -> ObjectNull.isNotNull(e.getRuleType()))
                .filter(e -> ObjectNull.isNotNull(e.getDataModelId()))
                .filter(e -> ObjectNull.isNotNull(e.getFieldList()))
                .collect(Collectors.toList());

        for (AssociationHtml html : list) {
            String htmlDataModelId = html.getDataModelId();
            //获取色孩子值
            //prop是表单的，
            //fieldKey 是模型
            Map<String, Object> filterData = html.getConditions()
                    .stream()
                    .filter(h -> StringUtils.isNotBlank(h.getFieldKey()) && StringUtils.isNotBlank(h.getProp()))
                    .collect(Collectors.toMap(BaseItemHtml::getFieldKey, e -> map.get(e.getProp())));
            Map<String, Object> setData = html.getFieldList()
                    .stream()
                    .filter(e -> StringUtils.isNotBlank(e.getFieldKey()) && map.containsKey(e.getProp()))
                    .collect(Collectors.toMap(AssociationItemHtml::getFieldKey, e -> {
                        switch (e.getType()) {
                            case formula:
                                // 根据公式得到值
                                String expression = e.getFormulaContent();
                                List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                                Object result = null;
                                if (Boolean.FALSE.equals(expressionParams.isEmpty())) {
                                    //处理特殊符
                                    result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, map, EnvConstant.FORM_ITEM_VALUE);
                                }
                                return result;
                            case prop:
                                // 根据表单字段得到值
                                return map.get(e.getProp());
                            case value:
                                return e.getValue();
                            default:
                                return null;
                        }
                    }));

            switch (html.getRuleType()) {
                case add:
                    //将data操作赋值给每一条内容里面进行处理
                    dynamicDataService.checkDataModel(setData, htmlDataModelId);
                    dynamicDataService.save(appId, html.getDataModelId(), setData);
                    continue;
                case edit:
                    DynamicDataUtils.clearEcho(setData);
                    dynamicDataService.updateMulti(htmlDataModelId, filterData, setData);
                    continue;
                case delete:
                    //先查询所有数据，然后再根据id删除
                    dynamicDataService.removeMulti(htmlDataModelId, filterData);
                    continue;
                default:
                    break;
            }
        }
        return R.ok();
    }

}
