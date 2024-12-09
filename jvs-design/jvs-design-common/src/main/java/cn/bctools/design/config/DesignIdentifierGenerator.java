package cn.bctools.design.config;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.project.enums.DesignIdPrefixEnum;
import cn.bctools.design.project.handler.Design;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.ImadcnIdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.imadcn.framework.idworker.common.SerializeStrategy;
import com.imadcn.framework.idworker.config.ApplicationConfiguration;
import com.imadcn.framework.idworker.config.ZookeeperConfiguration;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

/**
 * 如果启动了 zk， 使用 zk获取 workId 否则使用默认值,
 * 如果是设计类的，id都会有固定的前缀
 *
 * @author zhuxiaokang
 */
@Component
public class DesignIdentifierGenerator implements IdentifierGenerator, Closeable {
    private final IdentifierGenerator iden;

    public DesignIdentifierGenerator() {
        String zkServerLists = SpringContextUtil.getApplicationContext().getEnvironment().getProperty("mybatis-plus.zookeeper.serverLists");
        if (ObjectNull.isNotNull(zkServerLists)) {
            // 创建idworker实例
            ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
            applicationConfiguration.setSerialize(SerializeStrategy.SERIALIZE_JSON_FASTJSON);
            ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration();
            zookeeperConfiguration.setServerLists(zkServerLists);
            iden = new ImadcnIdentifierGenerator(zookeeperConfiguration, applicationConfiguration);
        } else {
            DefaultIdentifierGenerator defaultIdentifierGenerator = new DefaultIdentifierGenerator();
            IdWorker.setIdentifierGenerator(defaultIdentifierGenerator);
            iden = defaultIdentifierGenerator;
        }
        //设置 id 生成器
        IdWorker.setIdentifierGenerator(this);
    }


    @Override
    public Long nextId(Object entity) {
        return iden.nextId(entity).longValue();
    }

    @Override
    public String nextUUID(Object entity) {
        if (ObjectNull.isNull(entity)) {
            return iden.nextUUID(entity);
        }
        // 获取前缀
        String prefix = Optional.ofNullable(entity.getClass().getAnnotation(Design.class))
                .map(e -> DesignIdPrefixEnum.getPrefix(e.value()))
                .orElseGet(() -> "");
        return prefix + iden.nextUUID(entity);
    }

    /**
     * 根据设计类型神生成id
     *
     * @param designType
     * @return
     */
    public String nextUUID(DesignType designType) {
        if (ObjectNull.isNull(designType)) {
            return iden.nextUUID(null);
        }
        return DesignIdPrefixEnum.getPrefix(designType) + iden.nextUUID(null);
    }

    @Override
    public void close() throws IOException {
        if (iden instanceof ImadcnIdentifierGenerator) {
            ((ImadcnIdentifierGenerator) iden).close();
        }
    }
}
