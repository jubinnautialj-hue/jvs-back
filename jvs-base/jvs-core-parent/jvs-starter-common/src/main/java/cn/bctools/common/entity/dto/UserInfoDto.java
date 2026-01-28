package cn.bctools.common.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aa876
 */
@Data
@Accessors(chain = true)
public class UserInfoDto<T extends UserDto> implements Serializable {

    private static final long serialVersionUID = 1L;
    private T userDto;
    private List<String> permissions = new ArrayList<>();
    /**
     * 授权信息,用于前端判断
     */
    private Map<String, Object> license = new HashMap<>(1);
    private List<String> roles = new ArrayList<>();
    private List<String> childDeptIds = new ArrayList<>();
    private List<DataScopeDto> dataScope = new ArrayList<>();
    private String jvs;

}
