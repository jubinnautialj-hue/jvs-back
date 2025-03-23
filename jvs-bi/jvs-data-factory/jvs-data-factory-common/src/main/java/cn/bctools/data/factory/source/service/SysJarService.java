package cn.bctools.data.factory.source.service;

import cn.bctools.data.factory.source.entity.SysJar;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.File;

/**
 * @author admin
 * @description jar包信息
 */
public interface SysJarService extends IService<SysJar> {

    /**
     * 获取jar包文件
     *
     * @param jarId id
     */
    File getJarFile(String jarId);

}
