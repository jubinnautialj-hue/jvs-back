package cn.bctools.data.factory.source.service.impl;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.source.data.excel.ExcelSysFieldEnum;
import cn.bctools.data.factory.source.data.po.ExcelReadDataPo;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.ExcelCommitLog;
import cn.bctools.data.factory.source.enums.ExcelUpdateType;
import cn.bctools.data.factory.source.mapper.DataSourceMapper;
import cn.bctools.data.factory.source.mapper.DataSourceStructureMapper;
import cn.bctools.data.factory.source.mapper.ExcelCommitLogMapper;
import cn.bctools.data.factory.source.service.ExcelCommitLogService;
import cn.bctools.oss.dto.FileNameDto;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * excel 上传历史 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-06-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelCommitLogServiceImpl extends ServiceImpl<ExcelCommitLogMapper, ExcelCommitLog> implements ExcelCommitLogService {

    private final DataSourceMapper dataSourceMapper;

    private final DataSourceStructureMapper dataSourceStructureMapper;

    private final DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public void save(ExcelReadDataPo readData, String lotNo) {
        DataSource dataSource = dataSourceMapper.selectById(readData.getDataSourceId());
        FileNameDto baseFile = dataSource.getCustomJson().toJavaObject(FileNameDto.class);
        //设置原始名称
        String originalFileName = dataSource.getCustomJson().getString("originalFileName");
        baseFile.setOriginalFileName(originalFileName);

        ExcelCommitLog excelUpdateLog = new ExcelCommitLog()
                .setDataSourceId(readData.getDataSourceId())
                .setFileName(baseFile.getOriginalFileName())
                .setLotNo(lotNo)
                .setFileConfig(readData.getBaseFile())
                .setFileUrl(readData.getUrl())
                .setUploadType(readData.getAddIs() ? ExcelUpdateType.append : ExcelUpdateType.overwrite);
        //如果是覆盖上传 则以前的记录都无法操作
        if (!readData.getAddIs()) {
            this.update(Wrappers.lambdaUpdate(ExcelCommitLog.class).set(ExcelCommitLog::getOperateStatus, Boolean.FALSE).eq(ExcelCommitLog::getDataSourceId, dataSource.getId()));
        }
        this.save(excelUpdateLog);
    }

    @Override
    @SneakyThrows
    public void delByLotNo(String documentName, String lotNo) {
        log.info("---------删除：{}，批次：{}-----------", documentName, lotNo);
        dorisJdbcTemplate.delByLotNo(documentName, ExcelSysFieldEnum.批次号.getFieldName(), lotNo);
    }
}
