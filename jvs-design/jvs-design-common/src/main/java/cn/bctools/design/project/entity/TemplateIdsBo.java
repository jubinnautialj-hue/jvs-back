package cn.bctools.design.project.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class TemplateIdsBo {

    List<String> ids;
    List<String> identifiers;

}
