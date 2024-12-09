package cn.bctools.design.workflow.model.properties;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.design.workflow.model.enums.PurviewGroupEnum;
import cn.bctools.design.workflow.model.enums.PurviewPersonTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 权限配置
 */
@Data
public class Purview {

    /**
     * 权限组
     */
    private PurviewGroupEnum group;

    /**
     * 成员类型
     */
    private PurviewPersonTypeEnum personType;

    /**
     * 可审批人对象集合
     */
    private List<PersonnelDto> personnels;


    /**
     * 根据人员类型获取id集合
     * @param type 类型
     * @return id集合
     */
    public List<String> getPersonnelIdByType(TargetObjectTypeEnum type) {
        if (CollectionUtils.isEmpty(this.getPersonnels())) {
            return Collections.emptyList();
        }
        return this.getPersonnels().stream().filter(r -> type.getValue().equals(r.getType().getValue()))
                .map(PersonnelDto::getId)
                .collect(Collectors.toList());
    }

    /**
     * 默认的权限
     * @return
     */
    public static Purview defaultPurview() {
        Purview purview = new Purview();
        purview.setGroup(PurviewGroupEnum.send_flow);
        purview.setPersonType(PurviewPersonTypeEnum.all);
        purview.setPersonnels(Collections.emptyList());
        return purview;
    }

}
