package cn.bctools.data.factory.service;

import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataFactoryOut;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 数据工厂输出模型字段 服务类
 * </p>
 *
 * @author 作者
 * @since 2022-08-23
 */
public interface JvsDataFactoryOutService extends IService<JvsDataFactoryOut> {


    /**
     * 获取数据集输出数据
     * @param dataFactoryId 数据集id
     * @param size 数量
     * @param current 页数
     * @param consanguinityAnalyse 记录
     * @return
     */
    Page<Map<String, Object>> getData(String dataFactoryId, long size, long current, ConsanguinityAnalyse consanguinityAnalyse);
    /**
     * 获取数据集输出数据
     * @param dataFactoryId 数据集id
     * @param size 数量
     * @param current 页数
     * @return
     */
    Page<Map<String, Object>> getData(String dataFactoryId, long size, long current);

}
