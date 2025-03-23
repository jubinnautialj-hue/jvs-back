package cn.bctools.document.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：admin
 * [description]：文件处理工具
 */
@Slf4j
public class FileTreeUtil {

    /**
     * 生成文件夹到本地 并上传到文件服务器
     *
     * @param list    原始数据
     * @param nodeKey 需要获取的节点id
     * @return 所有子级的key集合
     */
    public static byte[] getTreeFile(List<DcLibrary> list, String nodeKey) {
        //获取指定节点的数据
        DcLibrary dcLibrary = list.stream().filter(e -> e.getId().equals(nodeKey)).findFirst().orElseThrow(() -> new BusinessException("没有找到此知识库!"));
        //文件路径
        StringBuffer path = new StringBuffer();
        path.append(File.separator).append(dcLibrary.getName());
        File dir = new File(path.toString());
        //生成文件夹
        FileUtil.mkdir(dir);
        //生成该文件夹下面的所有文件与文件夹
        boolean sonFile = getSonFile(list, dcLibrary.getId(), path);
        if (sonFile) {
            File zip = ZipUtil.zip(dir);
            //文件最大不能超过Integer.MAX_VALUE
            if (zip.length() > Integer.MAX_VALUE) {
                //删除压缩包与文件夹
                FileUtil.del(dir);
                FileUtil.del(zip);
                throw new BusinessException("文件超过2GB无法下载");
            }
            //直接返回二进制数据
            byte[] bytes = FileUtil.readBytes(zip);
            //删除压缩包与文件夹
            FileUtil.del(dir);
            FileUtil.del(zip);
            return bytes;
        }
        FileUtil.del(dir);
        throw new BusinessException("生成文件失败");
    }


    /***
     * 功能描述: <br>
     * 〈获取子级数据〉
     * @param list
     * @param nodeKey
     * @param filePath
     * @author ：admin
     */
    private static Boolean getSonFile(List<DcLibrary> list, String nodeKey, StringBuffer filePath) {
        List<DcLibrary> files = list.stream().filter(e -> e.getParentId().equals(nodeKey)).collect(Collectors.toList());
        if (!files.isEmpty()) {
            OssTemplate bean = SpringContextUtil.getBean(OssTemplate.class);
            //写入文件
            files.parallelStream().filter(e -> !e.getType().equals(DcLibraryTypeEnum.directory)).peek(e -> {
                //防止路径被修改
                StringBuffer stringBuffer = new StringBuffer(filePath.toString());
                //原始的文件名称
                stringBuffer.append(File.separator).append(e.getName());
                try {
                    if (StrUtil.isNotBlank(e.getFilePath()) && StrUtil.isNotBlank(e.getBucketName())) {
                        //获取文件url
                        String s = bean.fileLink(e.getFilePath(), e.getBucketName());
                        InputStream inputStream = new URL(s).openStream();
                        File file = new File(stringBuffer.toString());
                        FileUtil.writeFromStream(inputStream, file);
                        inputStream.close();
                    }
                } catch (Exception exception) {
                    log.error("文件写入失败", exception);
                }
            }).collect(Collectors.toList());
            //创建文件夹
            files.parallelStream().filter(e -> e.getType().equals(DcLibraryTypeEnum.directory)).peek(e -> {
                StringBuffer stringBuffer = new StringBuffer(filePath.toString());
                stringBuffer.append(File.separator).append(e.getName());
                File file = new File(stringBuffer.toString());
                FileUtil.mkdir(file);
                getSonFile(list, e.getId(), stringBuffer);
            }).collect(Collectors.toList());
        }
        return Boolean.TRUE;
    }

    /***
     * 功能描述: <br>
     * 〈输出流文件〉
     * @Param: [response]
     * @Return: void
     * @Author:
     * @Date: 2021/11/18 21:54
     */
    @SneakyThrows
    public static void fileOutput(String fileName, byte[] bytes) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        IoUtil.close(outputStream);
    }
}
