package cn.bctools.database.util;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.math.BigInteger;

/**
 * @author zhuxiaokang
 * id生成器
 */
public class IdGenerator extends IdWorker {
    private static IdentifierGenerator IDENTIFIER_GENERATOR = SpringContextUtil.getBean(IdentifierGenerator.class);

    /**
     * 生成UUID
     *
     * @return
     */
    public static String get32UUID(Object entity) {
        if (ObjectNull.isNull(entity)) {
            return get32UUID();
        }
        return IDENTIFIER_GENERATOR.nextUUID(entity);
    }

    /**
     * 使用进制返回
     *
     * @param radix 请填写2-36
     * @return
     */
    public static String getIdStr(int radix) {
        BigInteger s1 = new BigInteger(String.valueOf(getId()));
        return s1.toString(radix);
    }

}
