package cn.bctools.auth.service;

import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.gateway.entity.SysConfigs;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zhuxiaokang
 */
public interface SysConfigsService extends IService<SysConfigs> {
    /**
     * 获取配置根据对象做强制转换
     * 需要自行判断是否启用
     *
     * @param typeEnum
     * @return
     */
    <T extends SysConfigBase> T getConfig(ConfigsTypeEnum typeEnum);

}
