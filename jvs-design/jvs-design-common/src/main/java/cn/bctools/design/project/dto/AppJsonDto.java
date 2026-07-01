package cn.bctools.design.project.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The type App json dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class AppJsonDto {

    /**
     * The System.
     */
    @ApiModelProperty("系统名称")
    String system;
    /**
     * The Children.
     */
    @ApiModelProperty("菜单")
    List<AppMenusDto> children;

    /**
     * The type App menus dto.
     */
    @Data
    @Accessors(chain = true)
    public static class AppMenusDto {
        /**
         * The Menu.
         */
        @ApiModelProperty("菜单名称")
        String menu;
        /**
         * The Fields.
         */
        @ApiModelProperty("字段")
        List<AppJsonField> fields;
    }

    /**
     * The type App json field.
     */
    @Data
    @Accessors(chain = true)
    public static class AppJsonField {
        /**
         * The Field.
         */
        @ApiModelProperty("字段名")
        String field;
        /**
         * The Name.
         */
        @ApiModelProperty("中文名")
        String name;
        /**
         * The Type.
         */
        @ApiModelProperty("字段类型")
        String type;
        /**
         * The Field type.
         */
        @ApiModelProperty("字段类型")
        DataFieldType fieldType;
        /**
         * The Dic data.
         */
        @ApiModelProperty("可选项")
        List<String> dicData;
    }
}
