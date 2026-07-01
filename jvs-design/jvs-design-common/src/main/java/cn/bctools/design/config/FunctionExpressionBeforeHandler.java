package cn.bctools.design.config;

import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.handler.ExpressionBeforeHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author jvs
 * 根据场景判断是表单还是公式，并将对应的版本号放到local中，
 */
@Slf4j
@Service
@AllArgsConstructor
public class FunctionExpressionBeforeHandler implements ExpressionBeforeHandler {

    JvsAppService jvsAppService;
    FormService formService;
    CrudPageService crudPageService;

    @Override
    public ExecDto handler(String designId, String useCase, Boolean init, ExecDto body) {
        try {
            if (EnvConstant.FORM_ITEM_VALUE.equals(useCase) || EnvConstant.FORM_BUTTON_DISPLAY.equals(useCase)) {
                String jvsAppId = formService.getById(designId).getJvsAppId();
                CurrentAppUtils.setApp(jvsAppService.getAppById(jvsAppId));
            }
            if (EnvConstant.PAGE_BUTTON_DISPLAY.equals(useCase) || EnvConstant.LEFT_TREE_BUTTON_DISPLAY.equals(useCase)) {
                String jvsAppId = crudPageService.getById(designId).getJvsAppId();
                CurrentAppUtils.setApp(jvsAppService.getAppById(jvsAppId));
            }
        } catch (Exception e) {

        }
        return body;
    }
}
