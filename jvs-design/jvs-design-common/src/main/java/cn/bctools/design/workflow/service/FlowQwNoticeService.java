package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.dto.ApprovalRecordFieldDto;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
企微消息通知
 */
public interface FlowQwNoticeService {
    String create(Map flowParam) throws Exception;
    String close(List<String> bizTaskAndTaskIds) throws Exception;
}
