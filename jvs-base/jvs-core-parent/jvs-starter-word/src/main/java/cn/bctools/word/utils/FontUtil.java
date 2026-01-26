package cn.bctools.word.utils;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;

import java.awt.*;

/**
 * @author zhuxiaokang
 * 字体
 */
public class FontUtil {

    public static Mapper fontMapper = new IdentityPlusMapper();

    public static void init() {
        // 设置为 0.001，允许更高的压缩比
        ZipSecureFile.setMinInflateRatio(0.001);
        //加载系统字体。
        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font allFont : allFonts) {
            String psName = allFont.getPSName();
            fontMapper.put(allFont.getFamily(), PhysicalFonts.get(psName));
        }

        fontMapper.put("等线", PhysicalFonts.get("simSun"));
        fontMapper.put("等线Light", PhysicalFonts.get ("simSun"));
        fontMapper.put("隶书", PhysicalFonts.get("LiSu"));
        fontMapper.put("微软雅黑", PhysicalFonts.get("Microsoft Yahei"));
        fontMapper.put("黑体", PhysicalFonts.get("simHei"));
        fontMapper.put("楷体", PhysicalFonts.get("KaiTi"));
        fontMapper.put("宋体", PhysicalFonts.get("simSun"));
        fontMapper.put("仿宋", PhysicalFonts.get("FangSong"));
        fontMapper.put("新宋体", PhysicalFonts.get("NSimSun"));
        fontMapper.put("宋体扩展", PhysicalFonts.get("simsun-extB"));
        fontMapper.put("仿宋_GB2312" , PhysicalFonts.get("FangSong_GB2312"));
        fontMapper.put("幼圆", PhysicalFonts.get( "YouYuan"));
        fontMapper.put("华文行楷", PhysicalFonts.get("STXingkai"));
        fontMapper.put("华文仿宋", PhysicalFonts.get("STFangsong"));
        fontMapper.put("华文宋体", PhysicalFonts.get("STSong"));
        fontMapper.put("华文中宋", PhysicalFonts.get("STZhongsong"));
        fontMapper.put( "华文琥珀", PhysicalFonts.get("STHupo"));
        fontMapper.put( "华文隶书", PhysicalFonts.get("STLiti"));
        fontMapper.put( "华文新魏", PhysicalFonts.get("STXinwei"));
        fontMapper.put( "华文彩云", PhysicalFonts.get("STCaiyun"));
        fontMapper.put( "方正姚体", PhysicalFonts.get("FZYaoti"));
        fontMapper.put( "方正舒体", PhysicalFonts.get("FZShuTi"));
        fontMapper.put( "华文细黑", PhysicalFonts.get("STXihei"));
        fontMapper.put("新细明醴", PhysicalFonts.get("simSun"));
        fontMapper.put("SimSun", PhysicalFonts.get("SimSun"));
        PhysicalFonts.put("新细明醴", PhysicalFonts.get("SimSun"));
        PhysicalFonts.put("PMingLiU", PhysicalFonts.get("SimSun"));
    }
}
