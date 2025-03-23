package cn.bctools.document.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageVariableBinDing;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.mapper.MessageVariableBinDingMapper;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.document.vo.MessageVariableValueVo;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MessageVariableBinDingServiceImpl extends ServiceImpl<MessageVariableBinDingMapper, MessageVariableBinDing> implements MessageVariableBinDingService {
    private final DcLibraryService dcLibraryService;

    @Override
    public List<MessageVariableValueVo> getVariable(String messageId, String dcId, UserDto user) {
        List<MessageVariableBinDing> list = this.list(new LambdaQueryWrapper<MessageVariableBinDing>().eq(MessageVariableBinDing::getMessageId, messageId));
        DcLibrary byId = dcLibraryService.getAllId(dcId);
        //变量数据
        List<MessageVariableValueVo> dictList = new ArrayList<>();
        //如果是文档需要获取文库信息、
        DcLibrary knowledge = byId;
        if (!byId.getType().equals(DcLibraryTypeEnum.knowledge)) {
            knowledge = dcLibraryService.getAllId(byId.getKnowledgeId());
        }
        DcLibrary dcLibrary = JSONObject.parseObject(JSONObject.toJSONString(knowledge), DcLibrary.class);
        list.forEach(e -> {
            MessageVariableValueVo messageVariableValueVo = new MessageVariableValueVo();
            switch (e.getVariableName()) {
                //文库创建时间
                case "libraryCreateTime":
                    messageVariableValueVo.setValue(DateUtil.format(dcLibrary.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN));
                    break;
                //文库创建人
                case "libraryCreateUser":
                    messageVariableValueVo.setValue(dcLibrary.getCreateBy());
                    break;
                //文库名称
                case "libraryName":
                    messageVariableValueVo.setValue(dcLibrary.getName());
                    break;
                //文档名称
                case "name":
                    messageVariableValueVo.setValue(byId.getName());
                    break;
                //操作人
                case "operator":
                    messageVariableValueVo.setValue(user.getRealName());
                    break;
                //操作时间
                case "updateTime":
                    messageVariableValueVo.setValue(DateUtil.now());
                    break;
                //文档创建时间
                case "createTime":
                    messageVariableValueVo.setValue(DateUtil.format(byId.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN));
                    break;
                //文档创建时间
                case "createUser":
                    messageVariableValueVo.setValue(byId.getCreateBy());
                    break;
                //如果没有系统内置的变量 直接给一个空值
                default:
                    messageVariableValueVo.setValue("");

            }
            messageVariableValueVo.setColor(e.getColour())
                    .setName(e.getMessageVariableName());
            dictList.add(messageVariableValueVo);
        });
        return dictList;
    }

    @Override
    public void batchSaveOrUpdate(String messageId, List<MessageVariableBinDing> binDingList) {
        this.remove(Wrappers.lambdaQuery(MessageVariableBinDing.class).eq(MessageVariableBinDing::getMessageId, messageId));
        if (CollectionUtil.isNotEmpty(binDingList)) {
            binDingList.stream().peek(e -> e.setMessageId(messageId)).collect(Collectors.toList());
            this.saveOrUpdateBatch(binDingList);
        }
    }
}
