package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.Bulletin;
import cn.bctools.auth.entity.IndexApplet;
import cn.bctools.auth.mapper.BulletinMapper;
import cn.bctools.auth.mapper.IndexAppletMapper;
import cn.bctools.auth.service.BulletinService;
import cn.bctools.auth.service.IndexAppletService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 * 系统公告 服务实现类
 */
@Service
public class IndexAppletServiceImpl extends ServiceImpl<IndexAppletMapper, IndexApplet> implements IndexAppletService {

}
