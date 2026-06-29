# 公式逻辑抽象

由于不同项目都需要使用到公式,将公式抽象为starter,
项目通过使用groovy脚本语言快速扩展服务能力,通过结合业务代码和功能场景生成groovy脚本,并将脚本语言保存到数据库中,在业务使用时获取并执行.


功能描述
* 公式数据库操作.根据场景分类,不同业务中使用不同场景或根据自行定义公式使用场景
* 公式关联解析
* 根据公式获取基础用户登陆信息
* 解析公式关联关系
* 执行公式`cn.bctools.function.handler.ExpressionHandler.calculate(java.lang.String, java.util.Map<java.lang.String,java.lang.Object>, java.lang.String)`
