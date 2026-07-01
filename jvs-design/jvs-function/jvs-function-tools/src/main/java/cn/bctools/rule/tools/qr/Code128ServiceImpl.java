package cn.bctools.rule.tools.qr;

import cn.bctools.common.exception.BusinessException;
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
import cn.hutool.core.img.ImgUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.google.zxing.BarcodeFormat;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: ZhuXiaoKang
 * @Description: 条码生成器
 */
@Rule(value = "一维码生成",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.IMG,
        order = 27,
//        iconUrl = "rule-config",
        explain = "生成一维码，可定义生成一维码图片的宽高及方向"
)
@AllArgsConstructor
public class Code128ServiceImpl implements BaseCustomFunctionInterface<Code128Dto> {
    OssTemplate ossTemplate;

    @Override
    @SneakyThrows
    public Object execute(Code128Dto dto, Map<String, Object> params) {
        QrConfig qrConfig = BeanCopyUtil.copy(dto, QrConfig.class);
        byte[] serialize;
        try {
            qrConfig.setBackColor(null);
            BufferedImage bufferedImage = QrCodeUtil.generate(dto.getText(), BarcodeFormat.CODE_128, qrConfig);
            Image rotate = ImgUtil.rotate(bufferedImage, Optional.ofNullable(dto.getRotate()).orElse(0));
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImgUtil.write(rotate, ImgUtil.IMAGE_TYPE_PNG, out);
            serialize = out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("条码只支持数字字母和符号字符");
        }
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(serialize);
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, RuleConstant.OSS_BUCKET_NAME_PATH + "code128", byteInputStream, IdGenerator.getIdStr() + "rule-code-128.png", true);
        String fileUrl = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile()
                .setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setFileName(baseFile.getFileName())
                .setName(baseFile.getName())
                .setOutputType(OutputType.preview)
                .setOriginalName("code.png")
                .setUrl(fileUrl);
    }
}
