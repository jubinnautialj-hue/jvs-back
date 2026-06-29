package cn.bctools.document.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.MessageVariableBinDing;
import cn.bctools.document.vo.MessageVariableValueVo;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MessageVariableBinDingService extends IService<MessageVariableBinDing> {
    /**
     * 获取变量对应的值集合
     *
     * @param messageId 消息模板id
     * @param dcId      文档id
     * @param user  操作的用户信息
     */
    List<MessageVariableValueVo> getVariable(String messageId, String dcId, UserDto user);

    /**
     * 修改消息模板绑定的变量数据
     * @param messageId 消息模板id
     * @param binDingList
     */
    void batchSaveOrUpdate(String messageId,List<MessageVariableBinDing> binDingList);

}
