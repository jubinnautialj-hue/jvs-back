package cn.bctools.document.log.aspect;

import cn.bctools.common.entity.dto.UserInfoDto;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryLog;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.log.DocumentLog;
import cn.bctools.document.service.DcLibraryLogService;
import cn.bctools.document.service.DcLibraryService;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
 * 注解日志拦截处理，将所有的拦截日志和业务日志通过注入的Bean保存到数据库中
 *
 * @author My_gj
 */
@Slf4j
@Aspect
@Data
@Configuration
@Order(3)
public class SysLogAspect {
    private static final String HEADER_VALUE = "dcLibraryId";
    @Resource
    DcLibraryLogService dcLibraryLogService;
    @Resource
    DcLibraryService dcLibraryService;

    @SneakyThrows
    @Around("@annotation(documentLog)")
    public Object around(ProceedingJoinPoint point, DocumentLog documentLog) {
        //获取request对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String dcLibraryId = request.getHeader(HEADER_VALUE);
        Object obj = null;
        try {
            obj = point.proceed();
        } catch (Throwable throwable) {
            log.error("AOP拦截到错误", throwable);
            //如果异常直接抛出去 不进行日志的保存
            throw throwable;
        }
        DcLibrary byId = new DcLibrary();
        if (StrUtil.isNotEmpty(dcLibraryId)) {
            byId = dcLibraryService.getAllId(dcLibraryId);
        }
        //获取当前登录用户
        UserInfoDto userInfoDto = SystemThreadLocal.get("user");
        //当前用户为空直接不执行
        if (ObjectUtil.isNull(userInfoDto)) {
            return obj;
        }
        boolean equalsAdd = documentLog.operationType().equals(DcLibraryLogOperationTypeEnum.ADD);
        if (StrUtil.isNotEmpty(dcLibraryId) || equalsAdd) {
            //如果是新增就通过返回值获取 并且只统计文档或文件夹
            if (equalsAdd) {
                DcLibrary data = ((R<DcLibrary>) obj).getData();
                if (data.getType().equals(DcLibraryTypeEnum.knowledge)) {
                    return obj;
                }
                byId = data;
                dcLibraryId = data.getId();
            }
            if (byId == null) {
                log.info("未找到数据{}", dcLibraryId);
                return obj;
            }
            if (!byId.getType().equals(DcLibraryTypeEnum.knowledge)) {
                DcLibraryLog libraryLog = new DcLibraryLog()
                        .setDcLibraryId(dcLibraryId)
                        .setOperationType(documentLog.operationType().getValue())
                        .setKnowledgeId(byId.getKnowledgeId())
                        .setType(byId.getType())
                        .setNameSuffix(byId.getNameSuffix())
                        .setDcName(byId.getName())
                        .setParentId(byId.getParentId())
                        .setUserId(userInfoDto.getUserDto().getId())
                        .setUserName(userInfoDto.getUserDto().getRealName());
                //文库信息设置
                DcLibrary dcLibrary = dcLibraryService.getById(byId.getKnowledgeId());
                libraryLog.setKnowledgeName(dcLibrary.getName());
                dcLibraryLogService.save(libraryLog);
            }
        }
        return obj;
    }


}
