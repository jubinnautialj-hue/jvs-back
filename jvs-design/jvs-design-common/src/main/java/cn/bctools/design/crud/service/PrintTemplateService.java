package cn.bctools.design.crud.service;

import cn.bctools.design.crud.entity.PrintTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 打印模板 服务类
 */
public interface PrintTemplateService extends IService<PrintTemplate> {


    /**
     * 获取指定设计所有打印模板
     *
     * @param appId
     * @param designId 设计id
     * @return 打印模板设计集合
     */
    List<PrintTemplate> getDesignAll(String appId, String designId);

    /**
     * 获取指定设计可用模板设计
     *
     * @param appId
     * @param designId 设计id
     * @return 可用模板设计集合
     */
    List<PrintTemplate> getDesignAvailableAll(String appId, String designId);

    /**
     * 文件模板预览
     *
     * @param appId       应用ID
     * @param id          模版ID
     * @param dataModelId 模型id
     * @param designId    设计id
     * @param dataId      数据id
     */
    void filePreview(String appId, String id, String dataModelId, String designId, String dataId);

}
