package cn.bctools.document.service;

import cn.bctools.document.dto.AddAuthConfigDto;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.vo.req.DcLocationOtherDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Auto Generator
 */
public interface DcAuthConfigService extends IService<DcAuthConfig> {


    /**
     * 查询用户可以查看的知识库或者查询某个知识库具体的权限
     *
     * @param dcIds 文档id 如果为空表示查询用户可以查看的知识库 不为空就查询具体的权限
     * @return DcAuthConfig.class 权限数据
     */
    List<DcAuthConfig> getAuthConfigById(List<String> dcIds);

    /**
     * 移动文档时的权限变更
     *
     * @param dcLocationOtherDto 入参
     * @param pathIds            需要修改的数据
     */
    void moveUpdateAuthConfig(DcLocationOtherDto dcLocationOtherDto, List<String> pathIds);

    /**
     * 添加权限
     *
     * @param dcLibrary      当前文档对象
     * @param list           权限
     * @param isAddPossessor 是否添加所有者
     */
    void addOrUpdateAuthConfig(DcLibrary dcLibrary, List<AddAuthConfigDto> list, Boolean isAddPossessor);

    /**
     * 获取整个文库拥有某个权限的数据
     *
     * @param identifyingKeyEnum 权限标识
     * @return DcAuthConfig.class 权限数据
     * @
     */
    List<DcAuthConfig> getAuthConfig(IdentifyingKeyEnum identifyingKeyEnum);

    /**
     * 通过文库或者文件id获取有权限的用户信息
     *
     * @param id 文库或者文件id
     * @return 用户信息
     */
    List<AddAuthConfigDto.UserInfo> getUser(String id);

    /**
     * 通过文件夹id或者文件id 查询所有有权限的子集数据
     *
     * @param id     文库id或者文件夹id
     * @param fileIs 是否只需要文件 不需要文件夹与知识库
     * @return DcAuthConfig.class 权限数据
     */
    List<String> parentLevelToGetAuth(String id, Boolean fileIs);

    /**
     * 通过上级id查询 此目录下面是否有当前用户的权限用于确认是否隐藏文件夹
     *
     * @param id 文件夹id
     * @return DcAuthConfig.class 权限数据
     */
    Boolean childrenIsAuth(String id);


}
