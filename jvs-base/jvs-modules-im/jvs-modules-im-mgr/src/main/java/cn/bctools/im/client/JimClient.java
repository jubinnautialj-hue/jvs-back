/**
 * 
 */package cn.bctools.im.client;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.im.client.config.ImClientConfig;
import cn.bctools.im.client.handler.DefaultImClientHandler;
import org.jim.core.ImConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientTioConfig;
import org.tio.client.TioClient;
import org.tio.core.Node;

import java.util.Objects;

/**
 * J-IM客户端连接类
 * @author WChao
 *
 */
public class JimClient {

	private static Logger log = LoggerFactory.getLogger(JimClient.class);

	private TioClient tioClient = null;
	private ImClientConfig imClientConfig;

	public JimClient(){
		this.imClientConfig = ImClientConfig.newBuilder().clientHandler(new DefaultImClientHandler()).build();
	}

	public JimClient(ImClientConfig imClientConfig){
		this.imClientConfig = imClientConfig;
	}


	public ImClientChannelContext connect(Integer port) throws Exception {
		//服务器节点
		Node serverNode = new Node("localhost", port);
		return connect(serverNode, null);
	}

	public ImClientChannelContext connect(Node serverNode) throws Exception {
		return connect(serverNode, null);
	}

	public ImClientChannelContext connect(Node serverNode, Integer timeout) throws Exception {
		tioClient = new TioClient((ClientTioConfig) imClientConfig.getTioConfig());
		ClientChannelContext clientChannelContext = tioClient.connect(serverNode, imClientConfig.getBindIp(), imClientConfig.getBindPort(), timeout);
		if(ObjectNull.isNotNull(clientChannelContext)){
			ImClientChannelContext im = null;
			while (im == null) {
				im = (ImClientChannelContext)clientChannelContext.get(ImConst.Key.IM_CHANNEL_CONTEXT_KEY);
			}
			log.info("JIM客户端连接成功 serverAddress:[{}], bind localAddress:[{}]", serverNode.getIp(), imClientConfig.getBindPort());
			return im;
		}
		return null;
	}

	public void stop(){
		tioClient.stop();
	}

}
