package cn.bctools.common.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author jvs The type Compress test.
 */
public class CompressTest {

    /**
     * 压缩
     *
     * @param str 要压缩的字符串
     * @return 压缩后的字符串 string
     * @throws Exception the exception
     */
    public static String compress(String str) throws Exception {
        // 0 ~ 9 压缩等级 低到高 推荐9
        Deflater deflater = new Deflater(9);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256)) {
            deflater.setInput(str.getBytes());
            deflater.finish();
            final byte[] bytes = new byte[256];
            while (!deflater.finished()) {
                int length = deflater.deflate(bytes);
                outputStream.write(bytes, 0, length);
            }
            return java.util.Base64.getMimeEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw e;
        } finally {
            deflater.end();
        }
    }

    /**
     * Uncompress string.
     *
     * @param encodeStr 待解压缩的字符串
     * @return 解压缩后的字节数组 string
     * @throws IOException the io exception
     */
    public static String uncompress(String encodeStr) throws IOException {
        int len = 0;
        Inflater infl = new Inflater();
        infl.setInput(Base64.decodeBase64(encodeStr));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outByte = new byte[1024];
        try {
            while (!infl.finished()) {
                // 解压缩并将解压缩后的内容输出到字节输出流bos中
                len = infl.inflate(outByte);
                if (len == 0) {
                    break;
                }
                bos.write(outByte, 0, len);
            }
            infl.end();
        } catch (Exception e) {
            //
        } finally {
            bos.close();
        }
        return new String(bos.toByteArray());
    }
    //
    // /**
    // * The entry point of application.
    // *
    // * @param args the input arguments
    // * @throws Exception the exception
    // */
    // public static void main(String[] args) throws Exception {
    // StringBuilder sb = new StringBuilder();
    // sb.append("[{\"id\":\"5af213c91ca9b2255f1f78d51849525b\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"https://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=bbf432c1fa5843c323b91a03fcebc573&security=false&key=83b69b670ff1ff0952f7e5677a029854\",\"label\":\"help-center\",\"type\":\"jvs-ui-help-url\",\"description\":\"个人中心-帮助\",\"sort\":0,\"createTime\":\"2022-03-24T10:24:58\",\"updateTime\":\"2022-03-24T10:24:58\",\"delFlag\":false},{\"id\":\"1909092c1d71bfc49c1d78499df28da3\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"https://guanwang-2022.oss-cn-chengdu.aliyuncs.com/%E9%9B%86%E6%88%90%E8%87%AA%E5%8A%A8%E5%8C%96%E4%BB%8B%E7%BB%8D.mp4\",\"label\":\"video-rule-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"逻辑引擎-帮助\",\"sort\":1,\"createTime\":\"2022-03-24T10:24:58\",\"updateTime\":\"2022-03-24T10:24:58\",\"delFlag\":false},{\"id\":\"679ee2e28abd0a1e5012ee5088e2f073\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=26bd7660855ed737a75967e90184819d&security=false&key=965edbc5c9a70ebefb56f4e2d0143984\",\"label\":\"chart-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"图表引擎-帮助\",\"sort\":2,\"createTime\":\"2022-03-24T10:24:59\",\"updateTime\":\"2022-03-24T10:24:59\",\"delFlag\":false},{\"id\":\"ba0c73d345feeecfe2527640a0731885\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=0bdb047fc8107bd5d744f5d62837ea9d&security=false&key=620ddf1d1d023cbb878c4d8dffd89e00\",\"label\":\"flow-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"工作流-帮助\",\"sort\":3,\"createTime\":\"2022-03-24T10:24:59\",\"updateTime\":\"2022-03-24T10:24:59\",\"delFlag\":false},{\"id\":\"568f1af4ba06e8609d2e1d2de3939e86\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=5238ee25ed12f270baf36d1ff8bbefd3&security=false&key=25b2f8dd017a2a8d77ef562e601dcfe4\",\"label\":\"all-formula\",\"type\":\"jvs-ui-help-url\",\"description\":\"所有公式-帮助\",\"sort\":4,\"createTime\":\"2022-03-24T10:24:59\",\"updateTime\":\"2022-03-24T10:24:59\",\"delFlag\":false},{\"id\":\"8954c7616b404dcdd3ad840d198fb6db\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"https://guanwang-2022.oss-cn-chengdu.aliyuncs.com/%E5%8A%A8%E6%80%81%E6%95%B0%E6%8D%AE%E6%A8%A1%E5%9E%8B%E8%AE%B2%E8%A7%A3.mp4\",\"label\":\"video-learn-data-model\",\"type\":\"jvs-ui-help-url\",\"description\":\"快速创建-了解数据模型-帮助\",\"sort\":5,\"createTime\":\"2022-03-24T10:24:59\",\"updateTime\":\"2022-03-24T10:24:59\",\"delFlag\":false},{\"id\":\"69549842347e80768ac91e2be3d7afeb\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=a095c3478700cb037dfb686566f0bd9f&security=false&key=8e9823c4a7f7a4f4410cf0e6856ea1b3\",\"label\":\"event-auto-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"事件集成&自动化-帮助\",\"sort\":6,\"createTime\":\"2022-03-24T10:24:59\",\"updateTime\":\"2022-03-24T10:24:59\",\"delFlag\":false},{\"id\":\"5e0fda57afcd00f6530cf796e5ce2067\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=61a4ce69b3d935843ca978a151ad8d9b&security=false&key=9ba209811e1fd5873abd428e6acec96c\",\"label\":\"permission-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"权限设置-应用管理员-帮助\",\"sort\":7,\"createTime\":\"2022-03-24T10:25:13\",\"updateTime\":\"2022-03-24T10:25:13\",\"delFlag\":false},{\"id\":\"46290f2c7838da0ee5b4d62065664f41\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=f19f95f1c308023e5db79c7b8ea49626&security=false&key=cb3ffafa0f85ceb445a9e0200e8e7f22\",\"label\":\"create-page-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"开始创建一个页面-帮助\",\"sort\":8,\"createTime\":\"2022-03-24T10:25:13\",\"updateTime\":\"2022-03-24T10:25:13\",\"delFlag\":false},{\"id\":\"2784a26a8e80fc3cc56b3f57541dea55\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=6ee425a21459cde0092f2bf31c333974&security=false&key=cb36cb6724853ef5ff941b7378be36de\",\"label\":\"jvs-app-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"JVS快速应用搭建！JVS轻应用\",\"sort\":9,\"createTime\":\"2022-03-24T10:30:42\",\"updateTime\":\"2022-03-24T10:30:42\",\"delFlag\":false},{\"id\":\"5b18924fd3d6130fce577f3fd3bc1dbb\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=406eceed44b295821ffa0519025b6b7c&security=false&key=aa288bec7b9d3a2050a6f94c2644c8ad\",\"label\":\"jvs-app-setup\",\"type\":\"jvs-ui-help-url\",\"description\":\"JVS快速应用搭建！搭建应用\",\"sort\":10,\"createTime\":\"2022-03-24T10:30:42\",\"updateTime\":\"2022-03-24T10:30:42\",\"delFlag\":false},{\"id\":\"1513cd601218ca292482149b26da42d8\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=f19f95f1c308023e5db79c7b8ea49626&security=false&key=cb3ffafa0f85ceb445a9e0200e8e7f22\",\"label\":\"jvs-app-create\",\"type\":\"jvs-ui-help-url\",\"description\":\"JVS快速应用搭建！创建应用\",\"sort\":11,\"createTime\":\"2022-03-24T10:30:42\",\"updateTime\":\"2022-03-24T10:30:42\",\"delFlag\":false},{\"id\":\"5afbdc147499cf0e3baba0758ef2d816\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"http://knowledge.bctools.cn/#/detail?knowledgeId=d728e1ed49b66706fd4f9f7a64349841&type=document_html&id=6c0df56b6ec9926c2ee5e2ab4d5cf636&security=false&key=745d9aa28e172cc1a94531e8b0941df3\",\"label\":\"jvs-app-view\",\"type\":\"jvs-ui-help-url\",\"description\":\"JVS快速应用搭建！访问应用\",\"sort\":12,\"createTime\":\"2022-03-24T10:30:42\",\"updateTime\":\"2022-03-24T10:30:42\",\"delFlag\":false},{\"id\":\"516d965390f746a8e37524eeed079f0a\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"https://guanwang-2022.oss-cn-chengdu.aliyuncs.com/%E8%BD%BB%E5%BA%94%E7%94%A8%E6%93%8D%E4%BD%9C%E8%AE%B2%E8%A7%A3.mp4\",\"label\":\"video-form-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"表单引擎-帮助\",\"sort\":13,\"createTime\":\"2022-03-24T11:10:32\",\"updateTime\":\"2022-03-24T11:10:32\",\"delFlag\":false},{\"id\":\"2c00dd44480456bc0e2b0711dfdaf203\",\"dictId\":\"3ed776b1fb5e3a73c134c803074e6ad0\",\"value\":\"https://guanwang-2022.oss-cn-chengdu.aliyuncs.com/%E5%88%97%E8%A1%A8%E9%A1%B5%E8%A7%86%E9%A2%91%E8%AE%B2%E8%A7%A3.mp4\",\"label\":\"video-list-help\",\"type\":\"jvs-ui-help-url\",\"description\":\"列表引擎-帮助\",\"sort\":14,\"createTime\":\"2022-03-24T11:10:32\",\"updateTime\":\"2022-03-24T11:10:32\",\"delFlag\":false}]");
    // String str = sb.toString();
    // System.out.println(str.length());
    // System.out.println(PasswordUtil.encodePassword(str.toString(),
    // SpringContextUtil.getKey()).length());
    // String eos = compress(str);
    // System.out.println(eos.length());
    // System.out.println(PasswordUtil.encodePassword(eos,
    // SpringContextUtil.getKey()).length());
    // String deos = uncompress(eos);
    // System.out.println(deos.length());
    // System.out.println(eos.length()*100 / deos.length());
    // }
}
