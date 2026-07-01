package cn.bctools.rule.service;

/**
 * @author jvs
 * 某些节点执行完成后需要将此节点的信息进行记录，并在下一次执行时，将此节点的相关信息给其它节或逻辑使用,
 * 如调用三方平台上传资源，但需要管理部分资源文件，并将此资源文件保存起来
 */
public interface ResourceManagementInterface {

    /**
     * 保存此节点的资源服务功能
     *
     * @param name       资源名称
     * @param type       资源分类  逻辑用于分类不同使用场景
     * @param resourceId 资源id   三方平台返回的资源id
     * @param map        资源内容  逻辑节点中资源对象
     */
    void saveNodeResource(String name, ResourceType type, String resourceId, Object map);

}
