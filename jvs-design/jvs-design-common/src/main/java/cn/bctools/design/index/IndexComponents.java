package cn.bctools.design.index;

import cn.bctools.index.design.component.*;
import cn.bctools.index.design.component.service.*;
import cn.bctools.index.design.enums.ComponentType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


/**
 * 首页配置化组件声明
 *
 * @author jvs
 */
@Slf4j
@Service
public class IndexComponents {
    /**
     * oa组件
     *
     * @return
     */
//    @Bean
//    ComponentOaService oaService() {
//        return new ComponentOaService() {
//        };
//    }

    /**
     * 流程组件
     *
     * @return
     */
    @Bean
    ComponentOaTaskService taskService() {
        return new ComponentOaTaskService() {
        };
    }



    /**
     * 项目导航
     *
     * @return
     */
    @Bean
    ComponentProjectNavigationService projectNavigationService() {
        return new ComponentProjectNavigationService() {

            @Override
            public ComponentProjectNavigation generate() {
                ComponentProjectNavigation component = new ComponentProjectNavigation();
                component.setType(ComponentType.ProjectNavigation);
                component.setName(component.getType().getDesc());
                //根据启动的服务确定有哪些服务
                return component;
            }
        };
    }

    /**
     * 链接导航
     * @return
     */
    @Bean
    LinkComponentNavigationService linkComponentNavigationService(){
        return new LinkComponentNavigationService(){

        };
    }

    /**
     * 流程组件
     * @return
     */
    @Bean
    ComponentProcessManagementService managementService(){
        return new ComponentProcessManagementService(){

        };
    }


//
//    /**
//     * 列表组件
//     *
//     * @return
//     */
//    @Bean
//    ComponentCrudService crudService() {
//        return new ComponentCrudService() {
//
//            @Override
//            public ComponentCrudRender fillData(Map<String, FormQueryParamsDto> map, Map<String, Object> componentMetaData) {
//                return ComponentCrudService.super.fillData(map, componentMetaData);
//            }
//
//            @Override
//            public Object linkProp(String prop, Map<String, Object> paramsDtoMap) {
//                return ComponentCrudService.super.linkProp(prop, paramsDtoMap);
//            }
//
//            @Override
//            public ComponentCrud generate() {
//                ComponentCrud componentOa = new ComponentCrud();
//                componentOa.setType(ComponentType.CRUD).setName("列表页").setIcon("icon-lvzhou_gailan");
//                return componentOa;
//            }
//        };
//    }
//

    /**
     * 功能快捷导航当前这个用户常用于前10条功能
     */
//    @Bean
//    ComponentQuickNavigationService componentQuickNavigationService() {
//        final List<QuickNavigationItem> quickNavigationItems = new ArrayList<QuickNavigationItem>() {{
//            add(new QuickNavigationItem().setDescription("数据配置").setImage("http://jvsoss.bctools.cn/jvs-public/1/knowledge/dynamic/2023/05/12/2023-05-12841743852996825088-%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20230512182829.png").setJumpSettings(new JumpSettings().setTargetUrl("http://doc.bctools.cn/#/share/zQz2iq《数据源配置》(JVS产品文档)").setJumpMethod(JumpMethod._blank)));
//        }};
//        return new ComponentQuickNavigationService() {
//
//            @Override
//            public <S extends FormQueryParamsBase> ComponentQuickNavigationRender fillData(S body) {
//                TestFormQueryParamsBase base = (TestFormQueryParamsBase) body;
//                //加载菜单，根据当前用户的菜单进行渲染前 10条
//                ComponentQuickNavigationRender componentQuickNavigationRender = new ComponentQuickNavigationRender();
//                componentQuickNavigationRender.setItemList(quickNavigationItems);
//                return componentQuickNavigationRender;
//            }
//
//            @Override
//            public Class<? extends FormQueryParamsBase> generateClass() {
//                return TestFormQueryParamsBase.class;
//            }
//
//        };
//    }


}
