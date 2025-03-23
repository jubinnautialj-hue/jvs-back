package cn.bctools.bi.util;

import cn.bctools.common.exception.BusinessException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Administrator
 */
@Slf4j
public class ZipUtil {

    /**
     * @param fileURL      下载的url
     * @param saveFilePath 文件地址
     */
    public static void downloadFile(String fileURL, String saveFilePath) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            File saveFile = FileUtil.newFile(saveFilePath);
            try (InputStream inputStream = httpConn.getInputStream();
                 //创建文件路径
                 FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            log.info("url:" + fileURL + ",获取文件错误");
            throw new BusinessException("url:" + fileURL + ",获取文件错误");
        }
        httpConn.disconnect();
    }

    /**
     * @param content      文件内容
     * @param saveFilePath 文件地址
     */
    public static void saveFile(String content, String saveFilePath) {
        File file = FileUtil.file(saveFilePath);
        FileUtil.writeUtf8String(content, file);
    }

    /**
     * 压缩指定路径的文件
     *
     * @param srcFilePath     待压缩文件路径
     * @param zipPathFileName zip文件全路径名
     * @param password        加密密码
     * @return
     */
    public static void zipFile(String srcFilePath, String zipPathFileName, String password) {
        try {
            log.info("需要压缩的文件路径为:{}", srcFilePath);
            // 生成的压缩文件
            ZipFile zipFile = new ZipFile(zipPathFileName);
            ZipParameters parameters = new ZipParameters();
            // 压缩级别
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);

            if (StrUtil.isNotEmpty(password)) {
                zipFile.setPassword(password.toCharArray());
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(EncryptionMethod.AES);
                parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            }

            // 要打包的文件夹
            File file = new File(srcFilePath);
            //历此文件夹下面得 所有文件防止此目录整个打包到zip文件中
            File[] files = file.listFiles();
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    zipFile.addFolder(file1, parameters);
                } else {
                    zipFile.addFile(file1, parameters);
                }
            }
            zipFile.close();
        } catch (Exception e) {
            log.error("导出数据时，压缩文件失败!", e);
            throw new BusinessException("压缩文件失败");
        }
    }

    /**
     * 压缩指定路径的文件
     *
     * @param unzipPath 解压的路径
     * @param zipPath   zip文件路径
     * @param password  加密密码
     */
    public static void unzip(String unzipPath, String zipPath, String password) {
        try {
            ZipFile zipFile = new ZipFile(zipPath);
            zipFile.setPassword(password.toCharArray());
            zipFile.extractAll(unzipPath);
            zipFile.close();
        } catch (Exception exception) {
            log.error("导入时解压文件错误:", exception);
            throw new BusinessException("导入时解压文件错误");
        }
    }
}
