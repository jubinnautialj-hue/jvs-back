package cn.bctools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;

public class Demo {

    public static void main(String[] args) {

        //应用ID
        String appId = "xxxxxx";
        //应用凭证
        String secret = "xxxxxx";
        //集成&自动化凭证
        String ruleKey = "xxxxxx";
        //服务地址
        String host = "http://frame.bctools.cn";

        HashMap<Object, Object> hashMap = new HashMap<>(2);
        hashMap.put("age", 1);
        hashMap.put("name", "张三");
        //同步获取数据
        Object run = run(host, appId, secret, ruleKey, hashMap);

        //集成&自动化开启异步执行
        //同步执行，直接获取结果 。开启异步执行后，返回数据为运行ID。根据运行ID调用获取API接口
        getRun(host, run.toString());
    }

    private static void getRun(String host, String ruleId) {
        String url = host + "/mgr/jvs-design/api/" + ruleId;
        String body = HttpUtil.createGet(url)
                .execute().body();
        //打印获取结果
        log.info(body);
    }

    private static Object run(String host, String appId, String secret, String ruleKey, HashMap<Object, Object> hashMap) {
        String url = host + "/mgr/jvs-design/api/run/" + ruleKey;
        String body = HttpUtil.createPost(url)
                .header("appId", appId)
                .header("secret", secret)
                .body(JSONUtil.toJsonStr(hashMap))
                .execute().body();

        JSONObject jsonObject = JSONUtil.parseObj(body);
        Integer code = jsonObject.getInt("code");
        if (code.equals(0)) {
            //获取执行结果
            Object data = jsonObject.get("data");
            log.info(JSONUtil.toJsonStr(data));
            return data;
        }
        return "";
    }
}
