package cn.bctools.data.factory.source.service.impl;

import cn.bctools.data.factory.source.entity.SysJar;
import cn.bctools.data.factory.source.mapper.SysJarMapper;
import cn.bctools.data.factory.source.service.SysJarService;
import cn.bctools.data.factory.source.util.FileOssUtils;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * @author admin
 * @description jar包信息
 */
@Service
@Slf4j
@AllArgsConstructor
public class SysJarServiceImpl extends ServiceImpl<SysJarMapper, SysJar> implements SysJarService {
    private final OssTemplate ossTemplate;

    private final static String JAR_FILE_FORMAT = "{}.jar";

    @Override
    public File getJarFile(String jarId) {
        SysJar byId = this.getById(jarId);
        String fileLink = ossTemplate.fileLink(byId.getFilePath(), byId.getBucketName());
        InputStream inputStream = FileOssUtils.getInputStream(fileLink);
        String newFileName = IdUtil.fastUUID();
        File file = FileUtil.file(StrUtil.format(JAR_FILE_FORMAT, newFileName));
        FileUtil.writeFromStream(inputStream, file);
        return file;
    }
}
