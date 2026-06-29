package org.jim.core.packets;

/**
 * 版本: [1.0]
 * 功能说明: 客户端信息类
 * 作者: WChao 创建时间: 2017年7月26日 下午3:11:55
 * @author guojing
 */
public class ImClientNode<T extends User> extends Message {

    private static final long serialVersionUID = 6196600593975727155L;
    /**
     * 客户端ip
     */
    private String ip;
    /**
     * 客户端远程port
     */
    private int port;
    /**
     * 如果没登录过，则为null
     */
    private T user;
    /**
     * 地区
     */
    private String region;
    /**
     * 浏览器信息(这里暂时放在这,后面会扩展出比如httpImClientNode、TcpImClientNode等)
     */
    private String useragent;

    private ImClientNode() {
    }

    private ImClientNode(String id, String ip, int port, T user, String region, String useragent) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.region = region;
        this.useragent = useragent;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUseragent() {
        return useragent;
    }

    public void setUseragent(String useragent) {
        this.useragent = useragent;
    }

    public static class Builder<T extends User> extends Message.Builder<ImClientNode, Builder<T>> {
        /**
         * 客户端ip
         */
        private String ip;
        /**
         * 客户端远程port
         */
        private int port;
        /**
         * 如果没登录过，则为null
         */
        private T user;
        /**
         * 地区
         */
        private String region;
        /**
         * 浏览器信息
         */
        private String useragent;

        public Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder user(T user) {
            this.user = user;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder useragent(String useragent) {
            this.useragent = useragent;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public ImClientNode build() {
            return new ImClientNode(id, ip, port, user, region, useragent);
        }
    }

}
