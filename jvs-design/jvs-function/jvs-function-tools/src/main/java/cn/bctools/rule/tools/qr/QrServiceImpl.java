package cn.bctools.rule.tools.qr;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.oss.cons.OssSystemCons;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.entity.enums.type.OutputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * @author guojing
 * @describe 二维码生成器
 */
@Rule(value = "二维码生成",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.IMG,
        order = 1,
//        iconUrl = "rule-config",
        explain = "生成一个二维码图片对象"
)
@AllArgsConstructor
public class QrServiceImpl implements BaseCustomFunctionInterface<QrDto> {

    OssTemplate ossTemplate;

    @Override
    @SneakyThrows
    public Object execute(QrDto dto, Map<String, Object> params) {
        QrConfig qrConfig = BeanCopyUtil.copy(dto, QrConfig.class);
        qrConfig.setHeight(300);
        qrConfig.setWidth(300);
        byte[] serialize = QrCodeUtil.generatePng(dto.getText(), qrConfig);
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(serialize);
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, RuleConstant.OSS_BUCKET_NAME_PATH + "qr", byteInputStream, IdGenerator.getIdStr() + "rule-qr.png", true);
        String fileUrl = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile()
                .setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setFileName(baseFile.getFileName())
                .setName(baseFile.getName())
                .setOutputType(OutputType.preview)
                .setOriginalName("二维码.png")
                .setUrl(fileUrl);
    }

}
