package cn.bctools.design.data.entity;

import cn.bctools.auth.api.dto.PersonnelDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 数据模型唯一校验
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DataSettingBo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 公式id
     */
    String formula;

    /**
     * 公式的内容如果公式为空,则直接获取值，如果公式不为空，直接执行公式
     */
    private String formulaContent;
    /**
     * 用户是否只能提交一次
     */
    boolean once;
    /**
     * 是否开启数据脱敏
     */
    Boolean encryption;
    /**
     * 具体的哪一些人不需要数据脱敏，如果那个设计中没有数据脱敏，将不用获取处理这个数据值
     */
    List<PersonnelDto> userList;
    /**
     * 脱敏字段和处理方式
     */
    List<EncryptionFieldsPo> encryptionFields;

}
