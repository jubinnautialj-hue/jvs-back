package cn.bctools.remote.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DataSourceDetail {

    /**
     * 数据源结构id
     */
    private String id;

    /**
     * 执行时的入参 例如表名称或者url 如果是智仓或者低代码的数据就自己把id 赋值到这个字段,这个名字应该改为 execute_obj好点
     */
    private String executeName;

    /**
     * 名称
     */
    private String name;

    /**
     * 具体结构数据
     */
    private List<Structure> structure;
}
