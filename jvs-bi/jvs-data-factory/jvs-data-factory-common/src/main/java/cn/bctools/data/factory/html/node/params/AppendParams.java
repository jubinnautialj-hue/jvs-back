package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.node.dto.AppendParam;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class AppendParams extends NodeHtml<AppendParams> {

    private AppendObj appendObj;

    @Data
    @Accessors(chain = true)
    public static class AppendObj {
        List<AppendParam> fields;
    }
}
