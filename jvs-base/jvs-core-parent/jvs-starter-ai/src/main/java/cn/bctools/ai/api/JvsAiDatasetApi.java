package cn.bctools.ai.api;

import cn.bctools.ai.dto.AiDatasetDto;
import cn.bctools.ai.dto.AiDocumentDto;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * The interface Jvs ai api.
 *
 * @author jvs
 */
@FeignClient(value = "jvs-ai", contextId = "default-dataset")
public interface JvsAiDatasetApi {


    /**
     * The constant PREFIX.
     */
    String PREFIX = "/aichat/api/ai/dataset";

    /**
     * 获取所有知识库
     *
     * @param applicationName the application name
     * @return the r
     */
    @GetMapping(PREFIX + "/dataset")
    R<List<AiDatasetDto>> dataset(@RequestHeader(SysConstant.APPLICATION_NAME) String applicationName);

    /**
     * 新增一个知识库
     *
     * @param aiDatasetDto    the ai dataset dto
     * @param applicationName the application name
     * @return the r
     */
    @PostMapping(PREFIX + "/dataset")
    R dataset(@RequestBody AiDatasetDto aiDatasetDto, @RequestHeader(SysConstant.APPLICATION_NAME) String applicationName);

    /**
     * 删除知识库,同时删除内容
     *
     * @param datasetId       the datasetId
     * @param applicationName the application name
     * @return the r
     */
    @DeleteMapping(PREFIX + "/dataset")
    R<List<AiDatasetDto>> delDataset(@RequestParam("id") String datasetId, @RequestHeader(SysConstant.APPLICATION_NAME) String applicationName);

    /**
     * 上传知识和内容
     *
     * @param datasetId       the datasetId
     * @param document        the document
     * @param applicationName the application name
     * @return the r
     */
    @PostMapping(PREFIX + "/document/{dataset}")
    R<List<AiDocumentDto>> documents(@PathVariable("dataset") String datasetId, @RequestBody List<AiDocumentDto> document, @RequestHeader(SysConstant.APPLICATION_NAME) String applicationName);

    /**
     * 删除文档
     *
     * @param datasetId       the dataset id
     * @param document        the document
     * @param applicationName the application name
     * @return r
     */
    @DeleteMapping(PREFIX + "/document/{dataset}")
    R delDocuments(@PathVariable("dataset") String datasetId, @RequestBody List<String> document, @RequestHeader(SysConstant.APPLICATION_NAME) String applicationName);


}
