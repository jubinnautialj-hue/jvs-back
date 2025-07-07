package cn.bctools.rule.tools.similarity;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "相似度对比",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.数字,
        testShowEnum = TestShowEnum.TEXT,
        order = 10,
        explain = "根据提供的地址解析 pdf文件"

)
public class SimilarityServiceImpl implements BaseCustomFunctionInterface<SimilarityDto> {

    @Override
    public Object execute(SimilarityDto dto, Map<String, Object> params) {
        long hash1 = generateSimHash(dto.getText1());
        long hash2 = generateSimHash(dto.getText2());
        return similarity(hash1, hash2);
    }


    // 生成 SimHash 值（64位）
    public static long generateSimHash(String text) {
        // 1. 分词（HanLP 分词，保留词性）
        List<Term> termList = HanLP.segment(text);

        // 2. 初始化特征向量（64位，每个位置存储权重总和）
        int[] vector = new int[64];

        // 3. 遍历每个词，加权计算哈希
        for (Term term : termList) {
            String word = term.word;
            // 根据词性计算权重
            int weight = getWeight(term);

            // 计算词哈希值（64位）
            long hash = hash64(word);

            // 4. 更新特征向量（按位累加权重）
            for (int i = 0; i < 64; i++) {
                // 获取哈希值的第i位（0或1）
                int bit = (int) ((hash >> i) & 1);
                // 根据权重调整特征向量
                vector[i] += (bit == 1) ? weight : -weight;
            }
        }

        // 5. 降维生成最终 SimHash（根据特征向量符号决定位值）
        long simHash = 0L;
        for (int i = 0; i < 64; i++) {
            if (vector[i] > 0) {
                simHash |= (1L << i); // 该位为1
            }
        }
        return simHash;
    }

    // 根据词性计算权重（示例：名词权重更高）
    private static int getWeight(Term term) {
        String nature = term.nature.toString();
//        .natureStr; // 词性标注，如 "n" 表示名词
        switch (nature) {
            case "n":  // 名词
            case "vn": // 动名词
                return 3;
            case "v":  // 动词
                return 2;
            default:
                return 1;
        }
    }

    // 计算字符串的 64 位哈希值（使用 Murmur3）
    private static long hash64(String text) {
        return com.google.common.hash.Hashing.murmur3_128()
                .hashBytes(text.getBytes())
                .asLong();
    }

    // 计算汉明距离
    public static int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    // 计算相似度（0~1，1表示完全相同）
    public static double similarity(long hash1, long hash2) {
        int distance = hammingDistance(hash1, hash2);
        return 1 - (double) distance / 64;
    }

}
