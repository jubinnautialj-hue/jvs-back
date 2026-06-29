package cn.bctools.ai.api;

import cn.bctools.ai.dto.AiDatasetDto;
import cn.bctools.ai.dto.AiDocumentDto;
import cn.bctools.ai.dto.AiSearchDto;
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
     */
    @GetMapping(PREFIX + "/dataset")
    R<List<AiDatasetDto>> dataset();

    /**
     * 新增一个知识库
     *
     * @param aiDatasetDto the ai dataset dto
     * @return the r
     */
    @PostMapping(PREFIX + "/dataset")
    R dataset(@RequestBody AiDatasetDto aiDatasetDto);

    /**
     * 删除知识库,同时删除内容
     *
     * @param datasetId the datasetId
     * @return the r
     */
    @DeleteMapping(PREFIX + "/dataset")
    R<List<AiDatasetDto>> delDataset(@RequestParam("id") String datasetId);

    /**
     * 上传知识和内容
     *
     * @param document the document
     * @return the r
     */
    @PostMapping(PREFIX + "/document")
    R<List<AiDocumentDto>> documents(@RequestBody List<AiDocumentDto> document);

    /**
     * 根据知识库查询所有的文档
     *
     * @param datasetId
     * @return
     */
    @GetMapping(PREFIX + "/document/{dataset}")
    R<List<AiDocumentDto>> documents(@PathVariable("dataset") String datasetId);

    /**
     * 删除文档
     *
     * @param document the document
     * @return r
     */
    @DeleteMapping(PREFIX + "/document")
    R delDocuments(@RequestBody List<String> document);

    /**
     * 文档搜索
     *
     * @param aisearchDto
     * @return
     */
    @PostMapping(PREFIX + "/document/search")
    R search(@RequestBody AiSearchDto aisearchDto);


}
