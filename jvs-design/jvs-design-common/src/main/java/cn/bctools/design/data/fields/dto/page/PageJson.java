package cn.bctools.design.data.fields.dto.page;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
public class PageJson {

    List<LinkedHashMap<String, Object>> records;
    Integer current;
    Integer total;
    Integer size;

}
