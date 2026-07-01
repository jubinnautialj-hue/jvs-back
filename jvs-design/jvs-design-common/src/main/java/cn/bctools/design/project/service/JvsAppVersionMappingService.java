package cn.bctools.design.project.service;

import cn.bctools.design.project.entity.JvsAppVersionMapping;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 */
public interface JvsAppVersionMappingService extends IService<JvsAppVersionMapping> {

    /**
     * 获取应用版本设计id映射集合
     *
     * @param affiliationApp 所属应用唯一标识
     * @return id映射集合
     */
    List<JvsAppVersionMapping> getIdMappings(String affiliationApp);
}
