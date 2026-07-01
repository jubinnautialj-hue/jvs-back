package cn.bctools.design.workflow.model.properties;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.design.workflow.model.enums.PersonnelScopeTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 人员选择范围
 */
@Data
@Accessors(chain = true)
public class PersonnelScope {

    /**
     * 可选人员范围的类型
     */
    private PersonnelScopeTypeEnum type = PersonnelScopeTypeEnum.ALL;

    /**
     * 审批人可选范围
     */
    private List<PersonnelDto> personnelScopes;

    /**
     * 根据人员类型获取id集合
     * @param type 类型
     * @return id集合
     */
    public List<String> getPersonnelIdByType(TargetObjectTypeEnum type) {
        if (CollectionUtils.isEmpty(this.getPersonnelScopes())) {
            return Collections.emptyList();
        }
        return this.getPersonnelScopes().stream().filter(r -> type.getValue().equals(r.getType().getValue()))
                .map(PersonnelDto::getId)
                .collect(Collectors.toList());
    }
}
