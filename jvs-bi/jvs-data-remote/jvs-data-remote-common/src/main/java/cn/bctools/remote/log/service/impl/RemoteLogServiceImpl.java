package cn.bctools.remote.log.service.impl;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.OrderField;
import cn.bctools.data.factory.enums.SortTypeEnums;
import cn.bctools.remote.dto.JvsRemoteServerQueryDto;
import cn.bctools.remote.log.entity.RemoteLog;
import cn.bctools.remote.log.service.RemoteLogService;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteLogServiceImpl implements RemoteLogService {
    private final static String INSERT_SQL = "INSERT INTO data_remote_log (`id`,`serverId`,`serverName`,`secret`,`secretRemark`,`invoker`,`ip`,`callStatus`,`dataGetStatus`) VALUES(?,?,?,?,?,?,?,?,?)";
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final static String DELETE_SQL = "DELETE FROM data_remote_log where serverId='{}'";
    private final static String QUERY_SQL = "select * from data_remote_log where serverId=?";

    @Override
    public void saveLog(RemoteLog remoteLog) {
        log.info("入参为{}", JSONObject.toJSONString(remoteLog));
        dorisJdbcTemplate.update(INSERT_SQL, remoteLog.getId(), remoteLog.getServerId(), remoteLog.getServerName(), remoteLog.getSecret(), remoteLog.getSecretRemark(),  remoteLog.getInvoker(), remoteLog.getIp(), remoteLog.getCallStatus(), remoteLog.getDataGetStatus());
    }

    @Override
    public Page<RemoteLog> queryByCondition(Page<RemoteLog> page, JvsRemoteServerQueryDto queryDto) {
        List<Object> sqlParameter = new ArrayList<>();
        StringBuilder sql = new StringBuilder(QUERY_SQL);
        sqlParameter.add(queryDto.getServerId());
        if (StrUtil.isNotBlank(queryDto.getInvoker())) {
            sql.append(" and invoker = ? ");
            sqlParameter.add(queryDto.getInvoker());
        }
        if (ObjectUtil.isNotNull(queryDto.getCallStatus())) {
            sql.append(" and callStatus = ? ");
            sqlParameter.add(queryDto.getCallStatus());
        }
        if (ObjectUtil.isNotNull(queryDto.getDataGetStatus())) {
            sql.append(" and dataGetStatus = ? ");
            sqlParameter.add(queryDto.getDataGetStatus());
        }
        if (ObjectUtil.isNotNull(queryDto.getQueryStartTime()) && ObjectUtil.isNotNull(queryDto.getQueryEndTime())) {
            sql.append(" and (callDate>=? and callDate<=?) ");
            sqlParameter.add(queryDto.getQueryStartTime());
            sqlParameter.add(queryDto.getQueryEndTime());
        }
        OrderField orderField = new OrderField();
        orderField.setFieldKey("callDate")
                .setSortType(SortTypeEnums.desc);
        List<Map<String, Object>> list = dorisJdbcTemplate.getDataPage(page.getSize(), page.getCurrent(), sql.toString(), sqlParameter, Arrays.asList(orderField));
        List<RemoteLog> remoteLogs = JSONArray.parseArray(JSONObject.toJSONString(list), RemoteLog.class);
        page.setRecords(remoteLogs);
        return page;
    }

    @Override
    public void remove(String id) {
        dorisJdbcTemplate.execute(StrUtil.format(DELETE_SQL, id));
    }

}
