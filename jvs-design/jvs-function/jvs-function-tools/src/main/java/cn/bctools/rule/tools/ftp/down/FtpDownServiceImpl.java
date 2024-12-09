package cn.bctools.rule.tools.ftp.down;

import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.oss.cons.OssSystemCons;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.tools.ftp.FtpSelectedOption;
import cn.hutool.extra.ftp.Ftp;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author guojing
 */
@Service
@AllArgsConstructor
@Rule(value = "FTP文件下载",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 1,
//        iconUrl = "rule-FTPpeizhi",
        explain = "将ftp文件下载到本地,上传到服务器返回为url地址，不上传返回byte[]"
)
public class FtpDownServiceImpl implements BaseCustomFunctionInterface<FtpDownFunctionDto> {

    OssTemplate ossTemplate;

    @Override
    public Object execute(FtpDownFunctionDto dto, Map<String, Object> params) {
        String dtoFtp = dto.getFtp();
        //获取的数据源
        FtpSelectedOption option = JSONObject.parseObject(PasswordUtil.decodedPassword(dtoFtp, SpringContextUtil.getKey()), FtpSelectedOption.class);
        Ftp ftp = new Ftp(option.getIp(), option.getPort(), option.getUserName(), option.getPassWord());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ftp.download(dto.getDestPath(), dto.getFileName(), out);
        byte[] bytes = out.toByteArray();
        if (dto.getOnOff()) {
            BaseFile baseFile = ossTemplate.putFile(OssSystemCons.OSS_BUCKET_NAME, dto.getFileName(), new ByteArrayInputStream(bytes), "rule", "ftp");
            String fileLink = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
            return fileLink;
        } else {
            //直接返回byte数组
            return bytes;
        }
    }

}
