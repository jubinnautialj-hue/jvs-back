package org.jim.server.protocol;
import java.util.Properties;

/**
 * @author WChao
*/
public class ProtocolHandlerConfiguration {
	
	private  String name ;
	private  String serverHandler ;
	
	public ProtocolHandlerConfiguration(){}
	
	public ProtocolHandlerConfiguration(String name,Properties prop){
		this.name = name;
		this.serverHandler = prop.getProperty(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerHandler() {
		return serverHandler;
	}

	public void setServerHandler(String serverHandler) {
		this.serverHandler = serverHandler;
	}

	
}
