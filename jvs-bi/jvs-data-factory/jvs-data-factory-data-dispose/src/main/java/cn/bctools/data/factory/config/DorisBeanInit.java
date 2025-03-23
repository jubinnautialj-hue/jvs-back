package cn.bctools.data.factory.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

public class DorisBeanInit implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 创建类扫描器
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));

        // 扫描指定的包
        String basePackage = "cn.bctools.data.factory";
        for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
            // 创建BeanDefinition
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClassName(candidate.getBeanClassName());
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            beanDefinition.setAutowireCandidate(true);

            // 注册Bean
            registry.registerBeanDefinition(candidate.getBeanClassName(), beanDefinition);
        }
    }
}
