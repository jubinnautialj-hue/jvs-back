package cn.bctools.design.sqlInjector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * The type Custom sql injector.
 *
 * @author zhuxiaokang  自定义SQL注入器 <p>     所有自定义的sql方法都需要注入，否则不会生效
 */
@Configuration
public class CustomSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        methodList.add(new DeletePhysicalByIds());
        methodList.add(new DeletePhysical());
        return methodList;
    }
}
