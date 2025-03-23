package cn.bctools.common.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class DeptDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门Id
     */
    private String deptId;
    /**
     * 部门编号
     */
    private String deptCode;
    /**
     * 部门名称
     */
    private String deptName;

}
