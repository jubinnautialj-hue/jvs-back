package cn.bctools.data.factory.service.impl;

import cn.bctools.data.factory.entity.SysSetting;
import cn.bctools.data.factory.mapper.SysSettingMapper;
import cn.bctools.data.factory.service.SysSettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统配置
 *
 * @author zqs
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService {


}
