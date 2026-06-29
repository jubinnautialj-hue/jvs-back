package cn.bctools.auth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 将数据多个文件归类到另一个标签中
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FileVo {
    String fileLabel;
    List<String> ids;
}
