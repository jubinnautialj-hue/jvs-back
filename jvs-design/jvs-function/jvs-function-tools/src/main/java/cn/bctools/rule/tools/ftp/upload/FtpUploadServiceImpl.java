package cn.bctools.rule.tools.ftp.upload;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.bctools.rule.tools.ftp.FtpSelectedOption;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * @author guojing
 */
@Service
@AllArgsConstructor
@Rule(value = "FTP文件上传",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 20,
//        iconUrl = "rule-FTPpeizhi",
        explain = "将文件上传到指定ftp服务器。"
)
public class FtpUploadServiceImpl implements BaseCustomFunctionInterface<FtpUploadFunctionDto> {
    ModelInterface modelInterface;

    @Override
    public Object execute(FtpUploadFunctionDto dto, Map<String, Object> params) {
        Object byKey = modelInterface.getByKey(dto.getFtp());
        //获取的数据源
        FtpSelectedOption option = BeanCopyUtil.copy(FtpSelectedOption.class, byKey);
        //获取的数据源
        Ftp ftp = new Ftp(option.getIp(), option.getPort(), option.getUserName(), option.getPassWord());
        byte[] bytes = HttpUtil.downloadBytes(dto.getFileUrl());
        return ftp.upload(dto.getDestPath(), dto.getFileName(), new ByteArrayInputStream(bytes));
    }

}
