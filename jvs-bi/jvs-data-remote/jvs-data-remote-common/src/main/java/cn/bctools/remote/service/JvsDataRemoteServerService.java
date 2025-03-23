package cn.bctools.remote.service;

import cn.bctools.remote.po.JvsDataRemoteServer;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * api服务 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-20
 */
public interface JvsDataRemoteServerService extends IService<JvsDataRemoteServer> {

    /**
     * 检查参数设置是否合格
     * @param server
     * @return
     */
    String checkSettings(JvsDataRemoteServer server);
}
