package org.jim.server.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jim.core.ImChannelContext;
import org.jim.core.ImConst;
import org.jim.core.ImSessionContext;
import org.jim.core.packets.BusinessBody;
import org.jim.core.packets.User;
import org.jim.core.session.id.impl.UUIDSessionIdGenerator;
import org.jim.core.utils.JsonKit;

/**
 * @Author: ZhuXiaoKang
 * @Description: IM业务自定义消息工具类
*/
public class BusinessKit {

    private static Logger log = Logger.getLogger(BusinessKit.class);

    private static final String PREFIX = "business_";

    /**
     * 拼装唯一标识
     * @return
     */
    public static String markId(String id) {
        return new StringBuilder().append(PREFIX).append(id).toString();
    }

    /**
     * 判断是否是业务自定义消息
     * @param mark
     * @return
     */
    public static Boolean checkBusiness(String mark) {
        return mark.contains(PREFIX);
    }

    /**
     * 转换为业务自定义消息结构;
     * @param body
     * @param imChannelContext
     * @return
     */
    public static BusinessBody toBusinessBody(byte[] body, ImChannelContext imChannelContext){
        BusinessBody businessBody = parseBusinessBody(body);
        if(businessBody != null){
            if(StringUtils.isEmpty(businessBody.getFrom())){
                ImSessionContext imSessionContext = imChannelContext.getSessionContext();
                User user = imSessionContext.getImClientNode().getUser();
                if(user != null){
                    businessBody.setFrom(user.getUserId());
                }else{
                    businessBody.setFrom(imChannelContext.getId());
                }
            }
        }
        return businessBody;
    }

    /**
     * 判断是否属于指定格式聊天消息;
     * @param body
     * @return
     */
    private static BusinessBody parseBusinessBody(byte[] body){
        if(body == null) {
            return null;
        }
        BusinessBody businessBody = null;
        try{
            String text = new String(body, ImConst.CHARSET);
            businessBody = JsonKit.toBean(text, BusinessBody.class);
            if(businessBody != null){
                if(businessBody.getCreateTime() == null) {
                    businessBody.setCreateTime(System.currentTimeMillis());
                }
                if(StringUtils.isEmpty(businessBody.getId())){
                    businessBody.setId(UUIDSessionIdGenerator.instance.sessionId(null));
                }
                return businessBody;
            }
        }catch(Exception e){
            log.error(e.toString());
        }
        return businessBody;
    }
}
