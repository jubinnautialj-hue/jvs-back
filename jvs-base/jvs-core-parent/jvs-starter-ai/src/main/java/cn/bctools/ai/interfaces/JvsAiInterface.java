package cn.bctools.ai.interfaces;

import cn.bctools.ai.dto.AiDocumentDto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jvs
 */
public interface JvsAiInterface {

    /**
     * 根据 token 获取用户有哪些知识库的权限或文档的权限,这里现在只有文档的权限
     *
     * @param token 用户请求的 token， 可能为空。
     * @return
     */
    default List<AiDocumentDto> documents(String token) {
        return new ArrayList<>();
    }

}
