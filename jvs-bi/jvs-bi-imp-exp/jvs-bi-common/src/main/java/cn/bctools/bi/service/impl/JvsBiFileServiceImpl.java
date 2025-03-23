package cn.bctools.bi.service.impl;

import cn.bctools.bi.entity.JvsBiFile;
import cn.bctools.bi.mapper.JvsBiFileMapper;
import cn.bctools.bi.service.JvsBiFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */
@Slf4j
@Service
@AllArgsConstructor
public class JvsBiFileServiceImpl extends ServiceImpl<JvsBiFileMapper, JvsBiFile> implements JvsBiFileService {
}
