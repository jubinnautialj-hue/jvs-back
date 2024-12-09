package cn.bctools.common.utils.jvs;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author czy
 */
@Data
@Accessors(chain = true)
public class Information {
    String name;
    String link;
}
