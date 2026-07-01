package cn.bctools.design.project.service;

import cn.bctools.design.project.dto.AppVersionSubmitBetaDto;
import cn.bctools.design.project.dto.VersionIterationBaseDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppTemplate;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * The interface Jvs app template service.
 *
 * @author Auto Generator
 */
public interface JvsAppTemplateService extends IService<JvsAppTemplate> {

    /**
     * 根据前端用户选择的无租户的模板ID  创建应用
     * 应用发布到模板，模板是无租户的，方便一些数据的演示
     *
     * @param templateId 模板id
     */
    void createByTemplateId(String templateId);

    /**
     * 校验是否可以迭代
     *
     * @param affiliationApp    所属应用唯一标识
     * @param targetVersionType 目标版本类型
     * @return 锁key string
     */
    String checkCanAppIterator(String affiliationApp, AppVersionTypeEnum targetVersionType);

    /**
     * 同步发布版本
     * 根据版本的设计模板，替换指定版本类型的设计
     *
     * @param versionIterationType the version iteration type
     * @param templateTaskProgress 任务进度
     * @param sourceVersion        待发布的版本
     * @param targetVersionType    目标版本类型
     */
    void submitVersion(VersionIterationTypeEnum versionIterationType, JvsAppTemplateTaskProgress templateTaskProgress, JvsAppVersion sourceVersion, AppVersionTypeEnum targetVersionType);

    /**
     * 异步发布版本
     * 根据版本的设计模板，替换指定版本类型的设计
     *
     * @param lockKey              锁
     * @param versionIterationType the version iteration type
     * @param templateTaskProgress 任务进度
     * @param sourceVersion        待发布的版本
     * @param targetVersionType    目标版本类型
     */
    void asyncSubmitVersion(String lockKey, VersionIterationTypeEnum versionIterationType, JvsAppTemplateTaskProgress templateTaskProgress, JvsAppVersion sourceVersion, AppVersionTypeEnum targetVersionType);

    /**
     * 提交到测试版
     *
     * @param lockKey 锁
     * @param jvsApp  应用
     * @param dto     版本信息
     */
    void submitBeta(String lockKey, JvsApp jvsApp, AppVersionSubmitBetaDto dto);


    /**
     * 发布到正式版
     *
     * @param lockKey     锁
     * @param jvsApp      测试版本应用
     * @param betaVersion 测试版
     * @param dto         the dto
     */
    void submitGa(String lockKey, JvsApp jvsApp, JvsAppVersion betaVersion, VersionIterationBaseDto dto);

    /**
     * 上传模板文件创建应用
     *
     * @param jvsAppTemplate 模板
     * @param fileName       模板文件名
     */
    void uploadTemplateCreateApp(JvsAppTemplate jvsAppTemplate, String fileName);


    /**
     * 根据模板创建应用
     *
     * @param jvsAppTemplate 应用ID
     * @return 应用数据 design data
     */
    String getDesignData(JvsAppTemplate jvsAppTemplate);

    /**
     * 获取数据集
     *
     * @param template 应用模板
     * @return 模板数据 data
     */
    String getData(JvsAppTemplate template);

    /**
     * 保存模板
     * 查询应用数据生成模板并保存
     *
     * @param jvsAppTemplate 应用模板
     */
    void saveTemplate(JvsAppTemplate jvsAppTemplate);
    void saveTemplateAsync(JvsAppTemplate jvsAppTemplate, String userId, String realName, String tenantId);


    /**
     * 保存模板
     * 查询应用数据生成模板并保存
     *
     * @param templateId     模板id不为空则修改，为空则新增
     * @param jvsAppTemplate 应用模板
     */
    void saveTemplate(String templateId, JvsAppTemplate jvsAppTemplate);

    /**
     * 保存模板
     * 保存上传的模板
     *
     * @param jvsAppTemplate 应用模板
     */
    void saveUploadTemplate(JvsAppTemplate jvsAppTemplate);

    /**
     * 初始化开发版本模板
     *
     * @param jvsApp 应用信息
     * @return 版本信息 jvs app version
     */
    JvsAppVersion initVersionDevTemplate(JvsApp jvsApp);

    /**
     * 保存版本
     * 版本类型维度版本号唯一
     *
     * @param appId                应用id
     * @param sourceVersion        来源版本
     * @param targetCurrentVersion 目标版本
     * @param templateId           模板id
     */
    void saveAppVersion(String appId, JvsAppVersion sourceVersion, JvsAppVersion targetCurrentVersion, String templateId);

    /**
     * 删除模板数据
     *
     * @param template 模板
     */
    void removeTemplateData(JvsAppTemplate template);
}
