package cn.bctools.document.util;

import cn.hutool.core.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import org.jsoup.Jsoup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimHashUtil {
    public static final BigInteger BIGINT_0 = BigInteger.valueOf(0);
    public static final BigInteger BIGINT_1 = BigInteger.valueOf(1);
    public static final BigInteger BIGINT_2 = BigInteger.valueOf(2);
    public static final BigInteger BIGINT_1000003 = BigInteger.valueOf(1000003);
    public static final BigInteger BIGINT_2E64M1 = BIGINT_2.pow(64).subtract(BIGINT_1);


    /**
     * 获取文本的simHash值 会被拆分为4份16位的short
     *
     * @param content 文本内容
     */
    public static List<Short> getSimHashShort(String content) {
        String simHash = get(content);
        if (StrUtil.isBlank(simHash)) {
            return new ArrayList<>();
        }
        String[] cut = StrUtil.cut(simHash, 16);
        List<Short> list = Arrays.asList(cut).parallelStream().map(e -> Integer.valueOf(e, 2).shortValue()).collect(Collectors.toList());
        return list;
    }

    /**
     * 获取simHash
     *
     * @param text 文本内容
     */
    public static String get(String text) {
        //获取文字
        text = text.replaceAll("[^\\u4e00-\\u9fa5]", "");
        if (text == null) {
            return null;
        }
        // return Jsoup.parse(text).text();
        text = Jsoup.parse(text).text();
        int sumWeight = 0;
        int maxWeight = 0;
        int[] bits = new int[64];
        List<Term> termList = HanLP.segment(text);
        for (Term term : termList) {
            String word = term.word;
            if (CoreStopWordDictionary.contains(word)) {
                // 去除标点符号
                continue;
            }
            BigInteger wordHash = getWordHash(word);
            int wordWeight = getWordWeight(word);
            if (wordWeight == 0) {
                continue;
            }
            sumWeight += wordWeight;
            if (maxWeight < wordWeight) {
                maxWeight = wordWeight;
            }
            // 逐位将计算好的词哈希乘以权重，记录到保存用的数组上。
            // 如果该位哈希为1，则加上对应的权重，反之减去对应的权重。
            for (int i = 0; i < 64; i++) {
                BigInteger bitMask = BIGINT_1.shiftLeft(63 - i);
                if (wordHash.and(bitMask).signum() != 0) {
                    bits[i] += wordWeight;
                } else {
                    bits[i] -= wordWeight;
                }
            }
        }
        if (3 * maxWeight >= sumWeight || sumWeight < 20) {
            // 文本太短导致哈希不充分，拒绝返回结果（否则可能会有太多碰撞的文档，导致查询性能低下）
            // 暂时定为至少需要凑齐3个大词才允许返回结果
            return null;
        }

        // 将保存的位统计结果降维，处理成0/1字符串并返回
        StringBuilder simHashBuilder = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            if (bits[i] > 0) {
                simHashBuilder.append("1");
            } else {
                simHashBuilder.append("0");
            }
        }
        return simHashBuilder.toString();
    }

    /**
     * 获取一个单词的哈希值
     * 警告：修改该方法会导致计算出的SimHash发生变化。
     *
     * @param word 输入的单词
     * @return 返回哈希
     */
    private static BigInteger getWordHash(String word) {
        if (StrUtil.isBlank(word)) {
            return BIGINT_0;
        }
        char[] sourceArray = word.toCharArray();
        // 经过调优，发现左移位数为11-12左右最优
        // 在哈希词语主要为长度2的中文词时，可以避免高位哈希出现明显偏向
        // 反之，如果左移位数太大，则低位哈希将只和词语最后一个字相关
        BigInteger hash = BigInteger.valueOf(((long) sourceArray[0]) << 12);
        for (char ch : sourceArray) {
            BigInteger chInt = BigInteger.valueOf(ch);
            hash = hash.multiply(BIGINT_1000003).xor(chInt).and(BIGINT_2E64M1);
        }
        hash = hash.xor(BigInteger.valueOf(word.length()));
        return hash;
    }

    /**
     * 获取一个单词的权重。
     * 警告：修改该方法会导致计算出的SimHash发生变化。
     *
     * @param word 输入单词
     * @return 输出权重
     */
    private static int getWordWeight(String word) {
        if (StrUtil.isBlank(word)) {
            return 0;
        }
        int length = word.length();
        if (length == 1) {
            // 只有长度为1的词，哈希后位数不够（40位左右），所以权重必须很低，否则容易导致高位哈希全部为0。
            return 1;
        } else if (word.charAt(0) >= 0x3040) {
            if (length == 2) {
                return 8;
            } else {
                return 16;
            }
        } else {
            if (length == 2) {
                return 2;
            } else {
                return 4;
            }
        }
    }

    /**
     * 截取SimHash的一部分，转换为short对象
     *
     * @param simHash 原始SimHash字符串，64位0/1字符
     * @param part    需要截取的部分编号
     * @return 返回Short值
     */
    public static Short toShort(String simHash, int part) {
        if (simHash == null || part < 0 || part > 3) {
            return null;
        }
        int startBit = part * 16;
        int endBit = (part + 1) * 16;
        return Integer.valueOf(simHash.substring(startBit, endBit), 2).shortValue();
    }

    /**
     * 将四段Short格式的SimHash拼接成字符串
     *
     * @param simHashA simHashA，最高位
     * @param simHashB simHashB
     * @param simHashC simHashC
     * @param simHashD simHashD，最低位
     * @return 返回64位0/1格式的SimHash
     */
    public static String toSimHash(Short simHashA, Short simHashB, Short simHashC, Short simHashD) {
        return toSimHash(simHashA) + toSimHash(simHashB) + toSimHash(simHashC) + toSimHash(simHashD);
    }

    /**
     * 将一段Short格式的SimHash拼接成字符串
     *
     * @param simHashX 需要转换的Short格式SimHash
     * @return 返回16位0/1格式的SimHash
     */
    public static String toSimHash(Short simHashX) {
        StringBuilder simHashBuilder = new StringBuilder(Integer.toString(simHashX & 65535, 2));
        int fill0Count = 16 - simHashBuilder.length();
        for (int i = 0; i < fill0Count; i++) {
            simHashBuilder.insert(0, "0");
        }
        return simHashBuilder.toString();
    }

    /**
     * 比较两组SimHash（一组为64位0/1字符串，一组为四组Short），计算汉明距离
     *
     * @param simHash  待比较的SimHash（X），64位0/1字符串
     * @param simHashA 待比较的SimHash（Y），Short格式，最高位
     * @param simHashB 待比较的SimHash（Y），Short格式
     * @param simHashC 待比较的SimHash（Y），Short格式
     * @param simHashD 待比较的SimHash（Y），Short格式，最低位
     * @return 返回汉明距离
     */
    public static int hammingDistance(String simHash, Short simHashA, Short simHashB, Short simHashC, Short simHashD) {
        if (simHash == null || simHashA == null || simHashB == null || simHashC == null || simHashD == null) {
            return -1;
        }
        int hammingDistance = 0;
        for (int part = 0; part < 4; part++) {
            Short simHashX = toShort(simHash, part);
            Short simHashY = null;
            switch (part) {
                case 0:
                    simHashY = simHashA;
                    break;
                case 1:
                    simHashY = simHashB;
                    break;
                case 2:
                    simHashY = simHashC;
                    break;
                case 3:
                    simHashY = simHashD;
                    break;
                default:
            }
            hammingDistance += hammingDistance(simHashX, simHashY);
        }
        return hammingDistance;
    }

    /**
     * 比较两个Short格式的SimHash的汉明距离
     *
     * @param simHashX 待比较的SimHashX
     * @param simHashY 待比较的SimHashY
     * @return 返回汉明距离
     */
    public static int hammingDistance(Short simHashX, Short simHashY) {
        if (simHashX == null || simHashY == null) {
            return -1;
        }
        int hammingDistance = 0;
        int xorResult = (simHashX ^ simHashY) & 65535;

        while (xorResult != 0) {
            xorResult = xorResult & (xorResult - 1);
            hammingDistance += 1;
        }
        return hammingDistance;
    }

    public List<String> searchBySimHash(String indexNames, Short simHashA, short simHashB, short simHashC, short simHashD) {
        String simHash = toSimHash(simHashA, simHashB, simHashC, simHashD);
        return null;
    }
}
