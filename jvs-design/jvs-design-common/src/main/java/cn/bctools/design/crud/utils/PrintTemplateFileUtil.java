package cn.bctools.design.crud.utils;

import cn.bctools.common.exception.BusinessException;

import java.util.Arrays;

/**
 * @author zhuxiaokang
 * 文件打印模板工具
 */
public class PrintTemplateFileUtil {

    /**
     * 允许上传的文件
     */
    private static String[] ALLOW_FILE_TYPE = {"docx"};


    public static String getFileTypeByName(String fileName) {
        String[] arr = fileName.split("\\.");
        return arr[arr.length - 1];
    }

    public static String getFileName(String fileName) {
        String[] arr = fileName.split("\\.");
        return arr[0];
    }

    public static void checkFileType(String type) {
        if (Boolean.FALSE.equals(Arrays.asList(ALLOW_FILE_TYPE).contains(type))) {
            throw new BusinessException("不支持的文件类型");
        }
    }
}
