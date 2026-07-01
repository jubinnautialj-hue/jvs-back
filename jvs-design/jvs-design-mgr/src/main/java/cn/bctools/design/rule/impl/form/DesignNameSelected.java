package cn.bctools.design.rule.impl.form;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jvs
 * 获取设计的名称下拉
 * The type Design name selected.
 */
@Slf4j
@Service
@AllArgsConstructor
public class DesignNameSelected implements ParameterSelected<String> {

    /**
     * The Form service.
     */
    FormService formService;
    /**
     * The Crud page service.
     */
    CrudPageService crudPageService;

    @Override
    public String key() {
        return "jvsAppId";
    }

    @Override
    public List<ParameterOption<String>> getOptions() {

        //应用ID 必须要从前端 获取 如果获取 不到，即返回错误不可使用
        String appId = SystemThreadLocal.get(key());
        List<ParameterOption<String>> collect = new ArrayList<>();
        if (ObjectNull.isNull(appId)) {
            return new ArrayList<>();
        }
        formService.list(Wrappers.query(new FormPo().setJvsAppId(appId)).lambda().select(FormPo::getId, FormPo::getName))
                .stream()
                .map(e -> new ParameterOption<String>().setLabel(e.getName()).setValue(e.getId()))
                .forEach(collect::add);

        crudPageService.list(Wrappers.query(new CrudPage().setJvsAppId(appId)).lambda().select(CrudPage::getId, CrudPage::getName))
                .stream()
                .map(e -> new ParameterOption<String>().setLabel(e.getName()).setValue(e.getId()))
                .forEach(collect::add);
        return collect;
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }


}
