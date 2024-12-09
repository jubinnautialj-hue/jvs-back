package cn.bctools.common.utils;

import cn.bctools.common.exception.BusinessException;

import java.math.BigInteger;

/**
 * 雪花算法生成Id  详情见  ----- 自行百度，实现有所差异
 * 请使用 cn.bctools.database.util.IdGenerator.IdGenerator
 * @author guojing
 * @describe
 */
@Deprecated
public class IdGenerator {

    static IdGenerator ids = new IdGenerator(1001L);
    private long lastTs = 0L;
    private long processId;
    private int processIdBits = 10;
    private long sequence = 0L;
    private int sequenceBits = 12;

    protected IdGenerator(long processId) {
        if (processId > (long) ((1 << this.processIdBits) - 1)) {
            throw new BusinessException("系统异常");
        } else {
            this.processId = processId;
        }
    }


    /**
     * describe 获取唯一ID
     *
     * @param
     * @author guojing
     * @returnType long
     */
    public static long getId() {
        return ids.nextId();
    }

    public static String getIdStr() {
        return String.valueOf(ids.nextId());
    }

    /**
     * 使用进制返回
     *
     * @param radix 请填写2-36
     * @return
     */
    public static String getIdStr(int radix) {
        BigInteger s1 = new BigInteger(String.valueOf(ids.nextId()));
        return s1.toString(radix);
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    private synchronized long nextId() {
        long ts = this.timeGen();
        if (ts < this.lastTs) {
            throw new BusinessException("时间戳顺序错误");
        } else {
            if (ts == this.lastTs) {
                this.sequence = this.sequence + 1L & (long) ((1 << this.sequenceBits) - 1);
                if (this.sequence == 0L) {
                    ts = this.nextTs(this.lastTs);
                }
            } else {
                this.sequence = 0L;
            }

            this.lastTs = ts;
            return ts - 1483200000000L << this.processIdBits + this.sequenceBits | this.processId << this.sequenceBits | this.sequence;
        }
    }

    protected long nextTs(long lastTs) {
        long ts;
        for (ts = this.timeGen(); ts <= lastTs; ts = this.timeGen()) {
        }
        return ts;
    }
}
