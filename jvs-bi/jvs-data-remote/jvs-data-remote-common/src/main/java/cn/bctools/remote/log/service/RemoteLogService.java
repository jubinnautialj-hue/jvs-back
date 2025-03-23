package cn.bctools.remote.log.service;

import cn.bctools.common.utils.IdGenerator;
import cn.bctools.remote.dto.JvsDataRemoteSecretDto;
import cn.bctools.remote.dto.JvsRemoteServerQueryDto;
import cn.bctools.remote.log.entity.RemoteLog;
import cn.bctools.remote.po.JvsDataRemoteServer;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;

public interface RemoteLogService {


    /**
     * 保存
     *
     * @param server        数据服务
     * @param secret        使用的凭证
     * @param status        调用状态
     * @param dataGetStatus 数据获取状态
     * @param invoker       调用人
     * @param ip            调用方ip
     */
    default void simpleSave(JvsDataRemoteServer server, JvsDataRemoteSecretDto secret, Boolean status, Boolean dataGetStatus, String invoker, String ip) {
        RemoteLog remoteLog = new RemoteLog()
                .setId(IdGenerator.getIdStr())
                .setInvoker(invoker)
                .setServerId(server.getId())
                .setServerName(server.getName())
                .setServerAttr(JSONUtil.toJsonStr(server))
                .setCallDate(LocalDateTime.now())
                .setCallStatus(status)
                .setIp(ip)
                .setSecret(secret.getSecret())
                .setSecretRemark(secret.getRemark())
                .setDataGetStatus(dataGetStatus);
        this.saveLog(remoteLog);
    }

    ;

    /**
     * 保存数据服务调用日志
     *
     * @param log 日志
     */
    void saveLog(RemoteLog log);

    /**
     * 查询日志
     *
     * @param page     分页数据
     * @param queryDto 查询条件
     * @return 日志
     */
    Page<RemoteLog> queryByCondition(Page<RemoteLog> page, JvsRemoteServerQueryDto queryDto);

    /**
     * 删除索引
     *
     * @param id 索引名称(数据服务id)
     */
    void remove(String id);

}
