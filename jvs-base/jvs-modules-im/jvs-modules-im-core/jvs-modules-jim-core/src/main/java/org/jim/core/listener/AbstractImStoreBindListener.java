/**
 * 
 */package org.jim.core.listener;

import org.jim.core.ImConst;
import org.jim.core.config.ImConfig;
import org.jim.core.message.MessageHelper;

/**
 * @author WChao
 * 2018/08/26
 */
public abstract class AbstractImStoreBindListener implements ImStoreBindListener, ImConst {
	
	protected ImConfig imConfig;

	protected MessageHelper messageHelper;

	protected MessageHelper dbHelper;

	public AbstractImStoreBindListener(ImConfig imConfig, MessageHelper messageHelper, MessageHelper dbHelper){
		this.imConfig = imConfig;
		this.messageHelper = messageHelper;
		this.dbHelper = dbHelper;
	}

	public ImConfig getImConfig() {
		return imConfig;
	}

	public void setImConfig(ImConfig imConfig) {
		this.imConfig = imConfig;
	}

	public MessageHelper getMessageHelper() {
		return messageHelper;
	}

	public void setMessageHelper(MessageHelper messageHelper) {
		this.messageHelper = messageHelper;
	}

	public MessageHelper getDbHelper() {
		return dbHelper;
	}

	public void setDbHelper(MessageHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
}
