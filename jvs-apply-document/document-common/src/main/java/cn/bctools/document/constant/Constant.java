package cn.bctools.document.constant;

/**
 * @author : GaoZeXi
 */
public interface Constant {

    /**
     * 知识库链接的 Redis 的 key   knowledge:link:key:文档Id:文档"
     */
    String KNOWLEDGE_LINK_KEY = "knowledge:link:key:%s";


    /**
     * 顶级文档的父级id
     */
    String ROOT_ID_DC = "0";
    /**
     * 前端其他参数
     */
    String OTHER_JSON_WEB = "otherJson";

    /**
     * 锁定系统的redisKey
     */
    String LOCK_COMMON_KEY = "knowledge:lock";
    /**
     * 文档入es队列防止重复读取
     */
    String LOCK_MQ_ES_KEY = "knowledge:lock:mq:es:%s";
    /**
     * 解压文件队列防止重复解压
     */
    String LOCK_MQ_DECOMPRESSION_KEY = "knowledge:lock:mq:decompression:%s";
    /**
     * 执行版本封装的时间 目前不使用
     */
    String KNOWLEDGE_VERSION = "knowledge:version";
    /**
     * MONGODB集合名称
     */
    String KNOWLEDGE_VERSION_COLLECTION_NAME = "yanmao";

    /**
     * 上传文件夹时的文件夹信息 redis key
     */
    String UPDATE_FOLDER = "knowledge:update:folder:%s";

}
