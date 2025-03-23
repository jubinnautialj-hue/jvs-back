package cn.bctools.document.message.aspect;

import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.message.MessageFactory;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * 消息推送
 *
 * @author My_gj
 */
@Slf4j
@Aspect
@Data
@Configuration
@Order(4)
public class MessagePushAspect {
    private static final String HEADER_VALUE = "MessageDcLibraryId";
    @Resource
    DcLibraryService dcLibraryService;
    @Resource
    MessageFactory messageFactory;


    @SneakyThrows
    @Around("@annotation(messagePush)")
    public Object around(ProceedingJoinPoint point, MessagePush messagePush) {
        Object obj;
        //执行业务逻辑 防止消息模块报错
         try {
            obj = point.proceed();
        } catch (Throwable throwable) {
            log.error("AOP拦截到错误", throwable);
            //报错直接抛出去不推送消息
            throw throwable;
        }
        //获取数据业务id
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String dcLibraryId = request.getHeader(HEADER_VALUE);
        DcLibrary library;
        if (messagePush.returnValueIs()) {
            library = ((R<DcLibrary>) obj).getData();
            //新增知识库直接返回
            if (library.getType().equals(DcLibraryTypeEnum.knowledge) && messagePush.messagePushTye().equals(DcLibraryLogOperationTypeEnum.ADD)) {
                return obj;
            }
        } else {
            library = dcLibraryService.getAllId(dcLibraryId);
        }
        Boolean readNotify = library.getReadNotify();
        if (!library.getType().equals(DcLibraryTypeEnum.knowledge)) {
            readNotify = dcLibraryService.getById(library.getKnowledgeId()).getReadNotify();
        }
        if (readNotify) {
            messageFactory.messagePush(messagePush.messagePushTye(), library, UserCurrentUtils.getCurrentUser());
        }

        return obj;
    }


}
