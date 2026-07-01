package cn.bctools.rule.tools.network;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 网络检测是否能通
 *
 * @author guojing
 */
@Slf4j
public class NetWorkUtils {

    public static final String HTTP = "http";
    static final int I = 2;

    public static Boolean monitoring(String e) {
        //判断是否包含http,如果包含，则直接使用url检测，如果不包含，使用ping或net检测
        if (e.trim().startsWith(HTTP)) {
            return url(e);
        }
        String[] split = e.split(":");

        if (split.length == I) {
            return telnet(split[0], Integer.valueOf(split[1]));
        }
        return ping(split[0]);
    }

    /**
     * 测试telnet 机器端口的连通性
     *
     * @param hostname
     * @param port
     * @return
     */
    protected static boolean telnet(String hostname, int port) {
        Socket socket = new Socket();
        boolean isConnected = false;
        try {
            socket.connect(new InetSocketAddress(hostname, port));
            // 建立连接
            isConnected = socket.isConnected();
            // 通过现有方法查看连通状态
            // true为连通
        } catch (IOException e) {
            // 当连不通时，直接抛异常，异常捕获即可
        } finally {
            try {
                socket.close();
                // 关闭连接
            } catch (IOException ignored) {
            }
        }
        return isConnected;
    }

    /**
     * 比对所有的请求地址，判断URL地址是不是返回200，如果是200,则直接返回正常，如果不是则返回失败
     *
     * @param url 带有http 或https的请求地址
     * @return
     */
    protected static boolean url(String url) {
        int status;
        try {
            status = HttpUtil.createGet(url).execute().getStatus();
        } catch (Exception exception) {
            log.error("网络检测失败,检测地址为:{}", url);
            return false;
        }
        log.info("请求体检测 状态码为:{},请求地址:{}", status, url);
        List<Integer> http = Arrays.stream(HttpURLConnection.class.getDeclaredFields()).filter(e -> e.getName().startsWith("HTTP_")).map(e -> {
            try {
                return e.getInt(HttpURLConnection.class);
            } catch (IllegalAccessException illegalAccessException) {
                return null;
            }
        }).filter(Objects::nonNull).filter(e -> !e.equals(200)).collect(Collectors.toList());
        //所有的错误码进行比对
        return !http.contains(status);
    }

    /**
     * 判断主机地址能不能 ping 通，此处主机地址可以域名或者IP
     *
     * @param ip
     * @return
     */
    protected static boolean ping(String ip) {
        log.debug("ip地址为：" + ip);
        if (ip == null) {
            return false;
        }
        int i = 3;
        BufferedReader in = null;
        try {
            Process pro = Runtime.getRuntime().exec("ping " + ip + " -n " + i + " -w 5000");
            //GB2312  解决InputStreamReader乱码问题
            in = new BufferedReader(new InputStreamReader(pro.getInputStream(), "GB2312"));
            //逐行检查输出，计算类似出现=23ms TTL=62字样的次数
            int connectedCount = 0;
            String line;
            while ((line = in.readLine()) != null) {
                // 如果出现类似=23ms TTL=62这样的字样,出现的次数=测试次数则返回真
                int checkResult = getCheckResult(line);
                connectedCount += checkResult;
            }
            return connectedCount == i;
        } catch (Exception ex) {
            // 出现异常则返回假
            return false;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("逻辑引擎关网络检测关闭流异常,可以忽略");
            }
        }
    }

    static Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * 若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
     *
     * @param line
     * @return
     */
    private static int getCheckResult(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return 1;
        } else {
            return 0;
        }
    }


}
