package cn.bctools.design.jvslog.service;

import cn.bctools.design.jvslog.entity.JvsLog;
import cn.bctools.log.annotation.LogCallBack;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author guojing
 */
public interface JvsLogService extends IService<JvsLog>, LogCallBack {

}
