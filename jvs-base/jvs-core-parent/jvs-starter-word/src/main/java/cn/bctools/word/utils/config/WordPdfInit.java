package cn.bctools.word.utils.config;

import cn.bctools.word.utils.FontUtil;
import org.docx4j.jaxb.Context;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author zhuxiaokang
 * 初始化字体
 */
@Component
public class WordPdfInit{

    @PostConstruct
    private void init() {
        // 提前初始化docx4j相关数据，提升第一次生成模板时处理速度
        FontUtil.init();
        Context.getXslFoContext();
    }
}
