package cn.bctools.rule.data.mongo.custom;//package cn.bctools.design.rule.impl.mongo.custom;
//
//import cn.bctools.common.utils.PasswordUtil;
//import cn.bctools.common.utils.SpringContextUtil;
//import cn.bctools.design.rule.impl.mongo.MongoDBOption;
//import cn.bctools.design.rule.impl.mongo.mongodb.MongoCustomTemplate;
//import cn.bctools.logic.annotations.Rule;
//import cn.bctools.logic.enums.ClassType;
//
//import cn.bctools.logic.enums.TestShowEnum;
//import cn.bctools.logic.function.BaseCustomFunctionInterface;
//import cn.hutool.json.JSONUtil;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoDatabase;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.bson.Document;
//
//import java.util.Map;
//
//@Slf4j
//@RequiredArgsConstructor
//@Rule(value = "自定义语句",
//             group = RuleGroup.数据插件,
//        test = true,
//        enable = false,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 43,
//        explain = "自定义语句"
//)
//public class MongoExecImpl implements BaseCustomFunctionInterface<CustomDto> {
//
//    private final MongoCustomTemplate mongoCustomTemplate;
//
//    @Override
//    public Object execute(CustomDto customDto, Map<String, Object> params) {
//        //解密数据库配置
//        String options = customDto.getOptions();
//        options = options.replace("=", "");
//        String dataBaseStr = PasswordUtil.decodedPassword(options, SpringContextUtil.getKey());
//        MongoDBOption mongoDBOption = JSONUtil.toBean(dataBaseStr, MongoDBOption.class);
//        MongoClient mongoClient = mongoCustomTemplate.getMongoClient(mongoDBOption);
//        try {
//            //获取数据库对象
//            MongoDatabase database = mongoClient.getDatabase(mongoDBOption.getLibraryName());
//            Document document = new Document();
//            document.append("runCommand",customDto.getCustomJson());
//            return database.runCommand(document);
//        } finally {
//            mongoClient.close();
//        }
//    }
//}
