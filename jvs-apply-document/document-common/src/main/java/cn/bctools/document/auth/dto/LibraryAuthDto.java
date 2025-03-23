package cn.bctools.document.auth.dto;

import cn.bctools.document.dto.IdentifyingAuthDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiaohui
 */
@Getter
@Accessors(chain = true)
@ApiModel("数据权限")
public class LibraryAuthDto {
    @ApiModelProperty("所有知识库id")
    private List<String> allIds;
    @ApiModelProperty("个人的知识库")
    private List<String> ids;
    @Setter
    @ApiModelProperty("权限标识")
    Map<String, List<IdentifyingAuthDto>> authSign;

    public LibraryAuthDto() {
        List<String> list = new ArrayList<>();
        list.add("-1");
        this.allIds = list;
        this.ids = list;
    }

    public void setAllIds(List<String> allIds) {
        this.allIds = defaultValue(allIds);
    }

    public void setIds(List<String> ids) {
        this.ids = defaultValue(ids);
    }

    /**
     * 防止为空数据
     */
    private List<String> defaultValue(List<String> list) {
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            list.add("-1");

        }
        return list;
    }
}

