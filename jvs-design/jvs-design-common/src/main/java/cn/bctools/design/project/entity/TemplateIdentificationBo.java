package cn.bctools.design.project.entity;

import cn.bctools.design.identification.entity.Identification;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class TemplateIdentificationBo {

    List<Identification> identifications;
}
