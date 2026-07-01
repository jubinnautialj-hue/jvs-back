package cn.bctools.rule.signature;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturechaxunweitianchongbianliangdewenjianmobanliebiaoDto;
import cn.bctools.rule.dto.JunZiQianOption;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import com.alibaba.fastjson2.JSONObject;
import com.junziqian.sdk.bean.ResultInfo;
import com.junziqian.sdk.util.RequestUtils;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Gentleman signaturechaxunweitianchongbianliangdewenjianmobanliebiao.
 *
 * @author jvs
 */
@Rule(value = "查询文件模板列表",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "可查询上传的所有文件模板"
)
@AllArgsConstructor
public class GentlemanSignaturechaxunweitianchongbianliangdewenjianmobanliebiao implements BaseCustomFunctionInterface<GentlemanSignaturechaxunweitianchongbianliangdewenjianmobanliebiaoDto> {

    ModelInterface modelInterface;

    @Override
    public Object execute(GentlemanSignaturechaxunweitianchongbianliangdewenjianmobanliebiaoDto dto, Map<String, Object> params) {
        Object byKey = modelInterface.getByKey(dto.getOptions());
        JunZiQianOption option = BeanCopyUtil.copy(JunZiQianOption.class, byKey);
        Map<String, Object> reqBody = BeanCopyUtil.beanToMap(dto);
        reqBody.remove("options");
        ResultInfo<Object> objectResultInfo = RequestUtils.init(option.getServiceUrl(), option.getAppKey(), option.getAppSecret()).doPost("/v2/tmpl/pdfTpl/list", reqBody);
        if (objectResultInfo.isSuccess()) {
            return objectResultInfo;
        } else {
            LOG.error(JSONObject.toJSONString(objectResultInfo));
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", objectResultInfo.getMsg(), objectResultInfo.getData(), objectResultInfo.getResultCode()));
        }
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturechaxunweitianchongbianliangdewenjianmobanliebiaoDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        list.add(new RuleElementVo().setName("pageNum").setInfo("当前第几页").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc()));
        list.add(new RuleElementVo().setName("pageSize").setInfo("当前页总数").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc()));
        list.add(new RuleElementVo().setName("pages").setInfo("总页数").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc()));
        RuleElementVo e = new RuleElementVo().setName("data").setInfo("模板列表").setJvsParamType(JvsParamType.array).setJvsParamTypeName(JvsParamType.array.getDesc());
        List<RuleElementVo> dataList = new ArrayList<>();
        dataList.add(new RuleElementVo().setName("templateName").setInfo("模板名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        dataList.add(new RuleElementVo().setName("templateNo").setInfo("模板编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        dataList.add(new RuleElementVo().setName("url").setInfo("模版预览访问链接").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        e.setChildren(dataList);
        list.add(e);
        return list;
    }
}
