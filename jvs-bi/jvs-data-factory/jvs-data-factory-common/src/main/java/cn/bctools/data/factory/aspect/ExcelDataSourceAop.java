package cn.bctools.data.factory.aspect;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.data.factory.annotation.ExcelDataSource;
import cn.bctools.data.factory.enums.ExcelClassifyType;
import cn.bctools.data.factory.enums.ExcelInterceptType;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.ExcelCommitLog;
import cn.bctools.data.factory.source.entity.ExcelOperationLog;
import cn.bctools.data.factory.source.service.ExcelCommitLogService;
import cn.bctools.data.factory.source.service.ExcelOperationLogService;
import cn.bctools.oss.dto.FileNameDto;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class ExcelDataSourceAop {

    private static final String DATA_ADD = "新增(追加)";
    private static final String DATA_COVER = "新增(覆盖)";
    private static final String EDIT = "编辑基本信息";

    private final ExcelOperationLogService excelOperationLogService;
    private final ExcelCommitLogService excelCommitLogService;

    @Around("@annotation(excelDataSource)")
    public Object around(ProceedingJoinPoint point, ExcelDataSource excelDataSource) {
        ExcelInterceptType type = excelDataSource.type();
        Object[] args = point.getArgs();
        //构建日志主体
        ExcelOperationLog log = build(type, args);
        String err;
        Object resp = null;
        Boolean status = Boolean.FALSE;
        try {
            resp = point.proceed(args);
            R r = (R) resp;
            status = r.is();
            return resp;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            err = throwable.getMessage();
        } finally {
            expend(type, resp, status, log);
            excelOperationLogService.asyncSave(log);
        }
        throw new BusinessException(err);
    }

    private ExcelOperationLog build(ExcelInterceptType type, Object[] args) {
        ExcelOperationLog excelOperationLog = null;
        switch (type) {
            case 新增:
                excelOperationLog = add(args);
                break;
            case 删除数据片段:
                excelOperationLog = delSplicing(args);
                break;
            case 删除提交记录:
                excelOperationLog = delSubmit(args);
                break;
            default:
        }
        return excelOperationLog;
    }

    /**
     * 保存数据源
     *
     * @param args
     * @return
     */
    private ExcelOperationLog add(Object[] args) {
        boolean updateFile = Boolean.parseBoolean(StrUtil.toString(args[2]));

        DataSource dataSource = BeanCopyUtil.copy(args[0], DataSource.class);
        FileNameDto baseFile = dataSource.getCustomJson().toJavaObject(FileNameDto.class);
        ExcelOperationLog excelOperationLog = new ExcelOperationLog().setDataSourceId(dataSource.getId()).setFileName(baseFile.getOriginalFileName());

        if (updateFile) {
            boolean b = Boolean.parseBoolean(String.valueOf(args[1]));
            String type = b ? DATA_ADD : DATA_COVER;
            ExcelClassifyType classIfy = b ? ExcelClassifyType.新增 : ExcelClassifyType.覆盖;
            excelOperationLog.setOperationType(type)
                    .setOperationClassify(classIfy);
        } else {
            excelOperationLog.setOperationType(EDIT)
                    .setOperationClassify(ExcelClassifyType.编辑基本信息);
        }
        return excelOperationLog;
    }

    /**
     * 删除数据片段
     *
     * @param args
     * @return
     */
    private ExcelOperationLog delSplicing(Object[] args) {
        String dataSourceId = String.valueOf(args[0]);
        String submit = String.valueOf(args[1]);

        ExcelCommitLog excelUpdateLog = excelCommitLogService.getById(submit);
        String fileName = Optional.ofNullable(excelUpdateLog).map(ExcelCommitLog::getFileName).orElse("");

        return new ExcelOperationLog()
                .setDataSourceId(dataSourceId)
                .setFileName(fileName)
                .setOperationClassify(ExcelClassifyType.数据片段)
                .setOperationType("删除数据片段");
    }

    /**
     * 删除提交记录
     *
     * @param args
     * @return
     */
    private ExcelOperationLog delSubmit(Object[] args) {
        String submit = String.valueOf(args[1]);
        ExcelCommitLog excelUpdateLog = excelCommitLogService.getById(submit);
        String fileName = Optional.ofNullable(excelUpdateLog).map(ExcelCommitLog::getFileName).orElse("");

        String dataSourceId = String.valueOf(args[0]);
        return new ExcelOperationLog()
                .setDataSourceId(dataSourceId)
                .setFileName(fileName)
                .setOperationClassify(ExcelClassifyType.提交记录)
                .setOperationType("删除提交记录");
    }

    /**
     * 拓展数据
     *
     * @param type
     * @param resp
     * @param status
     * @param log
     */
    private void expend(ExcelInterceptType type, Object resp, Boolean status, ExcelOperationLog log) {
        if (ExcelInterceptType.新增.equals(type) && resp != null) {
            R r = BeanCopyUtil.copy(resp, R.class);
            if (r.is()) {
                DataSource dataSource = BeanCopyUtil.copy(r.getData(), DataSource.class);
                log.setDataSourceId(dataSource.getId());
            }
        }
        log.setStatus(status);
    }

    ;

}
