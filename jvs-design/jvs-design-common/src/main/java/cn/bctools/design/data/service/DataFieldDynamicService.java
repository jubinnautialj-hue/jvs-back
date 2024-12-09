package cn.bctools.design.data.service;

import cn.bctools.design.data.entity.DataFieldDynamicPo;
import cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 动态数据字段，通过逻辑添加的字段修改了表单设计
 *
 * @Author: GuoZi
 */
public interface DataFieldDynamicService extends IService<DataFieldDynamicPo> {

    /**
     * 保存字段  根据字段信息保存设计字段信息
     * 会根据模型删除所有的字段
     *
     * @param appId    应用 id
     * @param fields   字段
     * @param modelId  数据模型
     * @param designId 设计包含的所有设计 id
     * @return
     */
    void saveOrUpdateFields(String appId, List<DataFieldDynamicPo> fields, String modelId, List<String> designId);

    /**
     * 根据字段和
     *
     * @param appId
     * @param fields
     * @param modelId
     * @param designId
     * @return
     */
    boolean deleteFields(String appId, List<String> fields, String modelId, String designId);

    /**
     * 根据设计设计信息获取列表页需要展示的字段,并需要进行组装
     *
     * @param jvsAppId    应用id
     * @param dataModelId 模型 id
     * @param designId    设计  id
     * @return
     */
    List<DataTableFieldDesignHtml> getPageAutoTableFields(String jvsAppId, String dataModelId, String designId);

    /**
     * 获取表单的动态设计字段
     *
     * @param jvsAppId    应用 id
     * @param dataModelId 模型 id
     * @param designId    设计 id
     * @param prop        已经存在的字段
     * @return
     */
    List<Map> getFormAutoTableFields(String jvsAppId, String dataModelId, String designId, Set<String> prop);

    /**
     * 解析显示设置-关联模型，获取关联显示字段填充到列表页设计
     *
     * @param autoTableFields 列表页字段设计
     */
    void parseModelDisplayFillFields(List<DataTableFieldDesignHtml> autoTableFields);
}
