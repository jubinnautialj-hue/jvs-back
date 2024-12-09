package cn.bctools.design.use.api.dto;

import cn.bctools.design.use.api.enums.DataModelQueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The type Data model search dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class DataModelSearchDto {

    /**
     * The Id.
     */
    String id;

    /**
     * 模式
     */
    String mode;

    /**
     * The Group.
     */
    List<SearchGroup> group;

    /**
     * The Size.
     */
    long size;

    /**
     * The Current.
     */
    long current;


    /**
     * The type Search group.
     */
    @Data
    @Accessors(chain = true)
    public static class SearchGroup {

        /**
         * The Items.
         */
        List<SearchItem> items;

        /**
         * The And or.
         */
        @ApiModelProperty("true and false or")
        Boolean andOr;
    }

    /**
     * The type Search item.
     */
    @Data
    @Accessors(chain = true)
    public static class SearchItem {
        /**
         * The Key.
         */
        String key;

        /**
         * The Value.
         */
        Object value;

        /**
         * The Type.
         */
        String type;

        /**
         * The Query type.
         */
        DataModelQueryType queryType;

        /**
         * The And or.
         */
        @ApiModelProperty("true and false or")
        Boolean andOr;
    }
}
