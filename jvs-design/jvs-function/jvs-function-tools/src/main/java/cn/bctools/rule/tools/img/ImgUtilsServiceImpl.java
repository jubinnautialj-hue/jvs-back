package cn.bctools.rule.tools.img;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oss.cons.OssSystemCons;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author jvs
 * @describe 图片裁剪
 */
@Rule(value = "图片缩放",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 49,
        iconUrl = "rule-dian",
        explain = "根据设置的宽度和高度进行图片缩放"

)
@AllArgsConstructor
public class ImgUtilsServiceImpl implements BaseCustomFunctionInterface<ImgDto> {

    OssTemplate ossTemplate;

    @Override
    @SneakyThrows
    public Object execute(ImgDto dto, Map<String, Object> params) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream outputStream = new MemoryCacheImageOutputStream(byteArrayOutputStream);
        if (ObjectNull.isNotNull(dto.getScale())) {
            BufferedImage bufferedImage = ImgUtil.read(URLUtil.url(dto.getFileUrl()));
            Image scale = ImgUtil.scale(bufferedImage,
                    ((float) dto.getScale()) / 100
            );
            ImgUtil.write(scale, "jpg", outputStream, 0.0f, Color.WHITE);
        } else {
            Img.from(new ByteArrayInputStream(HttpUtil.downloadBytes(dto.getFileUrl())))
                    .setTargetImageType("jpg")
                    .scale(dto.getWidth(), dto.getHeight(), Color.WHITE)//
                    .write(byteArrayOutputStream);
        }
        byte[] serialize = byteArrayOutputStream.toByteArray();
        InputStream byteArrayInputStream = new ByteArrayInputStream(serialize);
        String module = RuleConstant.OSS_BUCKET_NAME_PATH + "file";
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, module, byteArrayInputStream, dto.getFileName() + ".jpg", true);
        String s = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return s;
    }

}
