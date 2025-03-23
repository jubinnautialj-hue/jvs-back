package cn.bctools;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;

public class Demo {

    public static void main(String[] args) {
        //api服务编号
        String serverId = "xxxxxx";
        //api服务逻辑凭证 若服务为公开或当前ip在白名单内，可不传
        String secret = "xxxxxx";
        //服务地址 服务网关
        String host = "http://bi.bctools.cn";
        //调用人名称
        String invoker = "xxx";

        HashMap<Object, Object> hashMap = new HashMap<>(2);
        hashMap.put("age", 1);
        hashMap.put("name", "张三");
        //同步获取数据
        run(host, appId, secret, hashMap,invoker);
        //回调地址
        String callbackAddr = "http://xxx/xx";
        //异步获取数据
        asyncRun(host, appId, secret, callbackAddr, hashMap,invoker);
    }

    /**
     * 同步api调用
     * @param host
     * @param secret
     * @param serverId
     * @param hashMap
     * @return
     */
    private static Object run(String host,String secret, String serverId, HashMap<Object, Object> hashMap,String invoker) {
        String url = host + "/mgr/jvs-data-factory/api/run/" + serverId;
        String body = HttpUtil.createPost(url)
                .header("secret", secret)
                .header("invoker", invoker)
                .body(JSONUtil.toJsonStr(hashMap))
                .execute().body();

        JSONObject jsonObject = JSONUtil.parseObj(body);
        Integer code = jsonObject.getInt("code");
        if (code.equals(0)) {
            //获取执行结果
            Object data = jsonObject.get("data");
            System.out.println(JSONUtil.toJsonStr(data));
        }
        return "";
    }

    /**
     * 异步api调用：api服务设置为异步调用时，才会异步访问
     * 异步调用会将本次执行的批次号放到响应头中：EXECUTE_NO
     * 执行完成后会将数据发送到设置的回调地址 请求类型：application/json 参数结构：{"executeNo":xxxx,"data":[]}
     * 由于传输数据量可能会便较大因此回调地址访问方式约定为post
     * @param host
     * @param secret
     * @param serverId
     * @param hashMap
     * @return
     */
    private static Object asyncRun(String host,String secret, String serverId, String callBackAddr, HashMap<Object, Object> hashMap,String invoker) {
        String url = host + "/mgr/jvs-data-factory/api/run/" + serverId;
        HttpResponse response = HttpUtil.createPost(url)
                .header("secret", secret)
                .header("invoker", invoker)
                .header("callback_addr", callBackAddr)
                .body(JSONUtil.toJsonStr(hashMap))
                .execute();
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        Integer code = jsonObject.getInt("code");
        if (code.equals(0)) {
            String executorNo = execute.header("EXECUTE_NO");
            System.out.println("执行批次："+executorNo);
        }
        return "";
    }
}
