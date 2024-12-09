# 通过定义组件的数据获取和参数名称个数类型等信息，在首页门户网页上配置不同的组件组装为一个整体的页面提供给用户访问.

* 组件的基础接口`cn.bctools.index.design.component.service.ComponentBaseService`

简单添加一个文本组件
```
    /**
     * 文本组件
     *
     * @return
     */
    @Bean
    public ComponentLabelService labelService() {
        return new ComponentLabelService() {
        };
    }
```
