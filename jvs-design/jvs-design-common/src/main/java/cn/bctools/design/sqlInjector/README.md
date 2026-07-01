# 增加全局自定义扩展Mybatis-plus Mapper扩展方法
## 使用自定义扩展方法的两种方式
1. 将原本继承BaseMapper的类改为继承CustomBaseMapper
2. 不继承CustomBaseMapper, 直接调用MapperMethodHandler