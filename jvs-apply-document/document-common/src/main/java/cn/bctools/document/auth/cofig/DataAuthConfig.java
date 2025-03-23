package cn.bctools.document.auth.cofig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaohui
 */
@Configuration
@ConfigurationProperties(prefix = "data.auth")
@Data
public class DataAuthConfig {
    /**
     * 需要进行权限拦截的接口
     */
    private List<String> checkUrl = new ArrayList<String>() {{
        //获取目录树
        add("/dcLibrary/tree");
        //获取文件内容 预览
        add("/dcLibrary/preview/document/**");
        //onlyoffice获取url
        add("/dcLibrary/file/get/url/**");
        //获取文档详细信息
        add("/dcLibrary/info/**");
    }};

    /**
     * 获取文库接口需要进行权限认证的url
     */
    private List<String> libraryUrl = new ArrayList<String>() {{
        //查询用户有权限的知识库
        add("/dcLibrary/knowledges");
        //查询用户自己创建的知识库
        add("/dcLibrary/knowledges/owner");
        //查询常用文库
        add("/dcLibrary/log/frequently/knowledge");
    }};
}
