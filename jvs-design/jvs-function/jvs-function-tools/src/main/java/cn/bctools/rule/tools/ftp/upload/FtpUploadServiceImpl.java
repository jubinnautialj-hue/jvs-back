package cn.bctools.rule.tools.ftp.upload;

import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.tools.ftp.FtpSelectedOption;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
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

    @Override
    public Object execute(FtpUploadFunctionDto dto, Map<String, Object> params) {
        String dtoFtp = dto.getFtp();
        //获取的数据源
        FtpSelectedOption option = JSON.parseObject(PasswordUtil.decodedPassword(dtoFtp, SpringContextUtil.getKey()), FtpSelectedOption.class);
        Ftp ftp = new Ftp(option.getIp(), option.getPort(), option.getUserName(), option.getPassWord());
        byte[] bytes = HttpUtil.downloadBytes(dto.getFileUrl());
        return ftp.upload(dto.getDestPath(), dto.getFileName(), new ByteArrayInputStream(bytes));
    }

}
