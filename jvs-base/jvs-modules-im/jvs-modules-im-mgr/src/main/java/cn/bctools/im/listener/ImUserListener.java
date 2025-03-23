package cn.bctools.im.listener;

import com.alibaba.fastjson.JSON;
import org.jim.core.ImChannelContext;
import org.jim.core.exception.ImException;
import org.jim.core.packets.User;
import org.jim.server.listener.AbstractImUserListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author WChao
 * @Desc
 */
public class ImUserListener extends AbstractImUserListener {

    private static Logger logger = LoggerFactory.getLogger(ImUserListener.class);

    @Override
    public void doAfterBind(ImChannelContext imChannelContext, User user) throws ImException {
        logger.info("绑定用户:{}", JSON.toJSONString(user));
    }

    @Override
    public void doAfterUnbind(ImChannelContext imChannelContext, User user) throws ImException {
        logger.info("解绑用户:{}",JSON.toJSONString(user));
    }

}
