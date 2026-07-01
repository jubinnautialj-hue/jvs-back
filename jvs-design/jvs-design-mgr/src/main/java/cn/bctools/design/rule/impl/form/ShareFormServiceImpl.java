package cn.bctools.design.rule.impl.form;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author zjvs
 */
@Service
@AllArgsConstructor
@Rule(value = "分享表单列表",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 5,
//        iconUrl = "rule-slsrizhifuwu",
        explain = "通过传递参数确定是否需要动态添加表单或列表的设计字段，添加的同时自动添加模型的字段,如果存在相同的字段组件类型变化后将自动修改。"
)
public class ShareFormServiceImpl implements BaseCustomFunctionInterface<ShareFormDto> {

    FormService formService;
    CrudPageService crudPageService;
    AuthTenantConfigServiceApi configServiceApi;


    @Override
    public Object execute(ShareFormDto shareFormDto, Map<String, Object> params) {
        FormPo formPo = formService.getById(shareFormDto.getDesignId());
        if (ObjectNull.isNotNull(formPo)) {
            String url = "/page-design-ui/#/form/use?id=%s&dataModelId=%s&jvsAppId=%s";
            url = String.format(url, formPo.getId(), formPo.getDataModelId(), formPo.getJvsAppId());
            if (ObjectNull.isNotNull(shareFormDto.getId())) {
                url += "&dataId=" + shareFormDto.getId();
                CrudPage one = crudPageService.getOne(new LambdaQueryWrapper<CrudPage>()
                        .select(CrudPage::getId)
                        .eq(CrudPage::getDataModelId, formPo.getDataModelId())
                        .eq(CrudPage::getJvsAppId, formPo.getJvsAppId())
                        .like(CrudPage::getViewJson, formPo.getId()));
                if (ObjectNull.isNotNull(one)) {
                    //拼接列表设计 id
                    url += "&pageId=" + one.getId();
                }
            }
            return configServiceApi.domain(TenantContextHolder.getTenantId(), ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION).getData() + url;
        } else {
            CrudPage page = crudPageService.getById(shareFormDto.getDesignId());
            if (ObjectNull.isNotNull(page)) {
                String url = "/page-design-ui/#/list/use?id=%s&dataModelId=%s&jvsAppId=%s";
                url = String.format(url, page.getId(), page.getDataModelId(), page.getJvsAppId());
                //获取租户或域名信息
                return configServiceApi.domain(TenantContextHolder.getTenantId(), ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION).getData() + url;
            } else {
                throw new BusinessException("未找到设计,不支持分享链接");
            }
        }
    }
}
