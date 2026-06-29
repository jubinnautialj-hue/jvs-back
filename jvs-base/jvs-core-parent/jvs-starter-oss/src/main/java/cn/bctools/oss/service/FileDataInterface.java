package cn.bctools.oss.service;

/**
 * 文件上传和下载读取的数据存储
 *
 * @author Administrator
 */
public interface FileDataInterface extends DataInterface {

    /**
     * 创建一个新的桶
     *
     * @param bucketName 桶名
     */
    void newBucket(String bucketName);

}
