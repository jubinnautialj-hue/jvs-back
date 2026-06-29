package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.enums.DynamicKeyword;
import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 用于构造条件
 */
public class DynamicCriteria {
    private Boolean trueCriteria;
    private DynamicKeyword logic;
    private String key;
    private Object value;
    private LinkedList<DynamicCriteria> chain = new LinkedList<>();

    public DynamicCriteria eq(String key, Object value) {
        addCondition(key, DynamicKeyword.EQ, value);
        return this;
    }

    public DynamicCriteria isNull(String key) {
        addCondition(key, DynamicKeyword.ISNULL, StringPool.EMPTY);
        return this;
    }

    public DynamicCriteria ne(String key, Object value) {
        addCondition(key, DynamicKeyword.NE, value);
        return this;
    }

    public DynamicCriteria gt(String key, Object value) {
        addCondition(key, DynamicKeyword.GT, value);
        return this;
    }

    public DynamicCriteria gte(String key, Object value) {
        addCondition(key, DynamicKeyword.GTE, value);
        return this;
    }

    public DynamicCriteria lt(String key, Object value) {
        addCondition(key, DynamicKeyword.LT, value);
        return this;
    }

    public DynamicCriteria lte(String key, Object value) {
        addCondition(key, DynamicKeyword.LTE, value);
        return this;
    }

    public DynamicCriteria in(String key, Collection<?> values) {
        addCondition(key, DynamicKeyword.IN, values);
        return this;
    }

    public DynamicCriteria notIn(String key, Collection<?> values) {
        addCondition(key, DynamicKeyword.NOTIN, values);
        return this;
    }

    public DynamicCriteria like(String key, Object value) {
        addCondition(key, DynamicKeyword.LIKE, value);
        return this;
    }

    public DynamicCriteria between(String key, Object value1, Object value2) {
        addCondition(key, DynamicKeyword.BETWEEN, Arrays.asList(value1, value2));
        return this;
    }

    public DynamicCriteria jsonLike(String key, Object value) {
        addCondition(key, DynamicKeyword.JSON_LIKE, value);
        return this;
    }

    public DynamicCriteria and() {
        return this;
    }

    public DynamicCriteria andOperator(DynamicCriteria... criteria) {
        chain.add(new DynamicCriteria().setLogic(DynamicKeyword.AND).setChain(criteria));
        return this;
    }

    public DynamicCriteria or() {
        return this;
    }

    public DynamicCriteria orOperator(DynamicCriteria... criteria) {
        chain.add(new DynamicCriteria().setLogic(DynamicKeyword.OR).setChain(criteria));
        return this;
    }

    private void addCondition(String key, DynamicKeyword keyword, Object value) {
        chain.add(new DynamicCriteria().setKey(key).setLogic(keyword).setValue(value));
    }

    protected DynamicCriteria setKey(String key) {
        this.key = key;
        return this;
    }

    protected DynamicCriteria setValue(Object value) {
        this.value = value;
        return this;
    }

    protected DynamicCriteria setLogic(DynamicKeyword logic) {
        this.logic = logic;
        return this;
    }

    protected DynamicCriteria setChain(DynamicCriteria... criteria) {
        this.chain.addAll(Arrays.asList(criteria));
        return this;
    }

    private DynamicCriteria setTrueCriteria(Boolean value) {
        this.trueCriteria = value;
        return this;
    }

    public DynamicKeyword getLogic() {
        return logic;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Boolean getTrueCriteria() {
        return trueCriteria;
    }

    public LinkedList<DynamicCriteria> getChain() {
        return this.chain;
    }

    /**
     * 构造一个必然为真的条件
     *
     * @return
     */
    public static DynamicCriteria trueCriteria() {
        return new DynamicCriteria().setChain(new DynamicCriteria().setTrueCriteria(Boolean.TRUE));
    }

    /**
     * 构造一个必然为假的条件
     *
     * @return
     */
    public static DynamicCriteria falseCriteria() {
        return new DynamicCriteria().setChain(new DynamicCriteria().setTrueCriteria(Boolean.FALSE));
    }

    /**
     * 获取所有条件字段
     *
     * @return
     */
    public List<String> getAllKey() {
        LinkedList<DynamicCriteria> chains = getChain();
        List<String> keys = new ArrayList<>();
        while (ObjectNull.isNotNull(chains)) {
            LinkedList<DynamicCriteria> nextChains = new LinkedList<>();
            chains.forEach(chain -> {
                nextChains.addAll(chain.getChain());
                if (ObjectNull.isNotNull(chain.getKey())) {
                    keys.add(chain.getKey());
                }
            });
            chains = nextChains;
        }
        return keys.stream().distinct().collect(Collectors.toList());
    }

}
