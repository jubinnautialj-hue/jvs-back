package cn.bctools.common.utils;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <拼音工具>
 *
 * @author auto
 **/
public class PinyinUtils {
    private PinyinUtils() {
    }

    /**
     * 获取拼音 驼峰
     *
     * @param name 汉字
     * @return 拼音
     */
    public static String getCameCasePinYin(String name) {
        if (StrUtil.isBlank(name)) {
            throw new UtilException("Illegal resource string");
        }
        return IntStream.range(0, name.length()).mapToObj(i -> StrUtil.upperFirst(PinyinUtil.getPinyin(CharUtil.toString(name.charAt(i))))).collect(Collectors.joining());
    }

}
