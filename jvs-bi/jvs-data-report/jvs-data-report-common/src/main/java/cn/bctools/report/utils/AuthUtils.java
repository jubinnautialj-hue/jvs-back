package cn.bctools.report.utils;

import cn.bctools.permission.enums.OperationType;
import cn.bctools.permission.utils.AuthVerifyUtils;
import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.model.auth.BaseAuthPo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wl
 * 权限鉴定
 */
public class AuthUtils {

    private static final List<OperationType> REPORT = new ArrayList<OperationType>(){{
        add(OperationType.查看);
        add(OperationType.移动);
        add(OperationType.复制);
        add(OperationType.设计);
        add(OperationType.下载);
        add(OperationType.打印);
        add(OperationType.删除);
    }};

    private static final List<OperationType> MENU = new ArrayList<OperationType>(){{
        add(OperationType.编辑);
        add(OperationType.删除);
        add(OperationType.移动);
        add(OperationType.新增);
        add(OperationType.查看);
    }};

    private static final List<OperationType> DEFAULT = new ArrayList<OperationType>(){{
        add(OperationType.查看);
    }};

    /**
     * 设置权限
     * @param obj 实体类
     */
    public static <T extends BaseAuthPo> void setOperation(T obj){
        if(obj==null){
            return;
        }
        List<OperationType> operationList = getOperationList(obj);
        obj.setOperationList(operationList);
    }

    /**
     * 获取权限
     * @param obj 实体类
     * @return 权限列表
     */
    public static <T extends BaseAuthPo> List<OperationType> getOperationList(T obj){
        List<OperationType> permissionList = getPermissionList(obj);
        return AuthVerifyUtils.getOperationList(obj.getRoleType(), obj.getRole(), obj.getCreateById(),permissionList,DEFAULT);
    }

    /**
     * 校验权限
     * @param obj 实体类
     * @param type 权限类型
     * @return true 有权限， false无权限
     */
    public static <T extends BaseAuthPo> boolean verify(T obj,OperationType type){
        List<OperationType> permissionList = getPermissionList(obj);
        return AuthVerifyUtils.verify(obj.getRoleType(), obj.getRole(), obj.getCreateById(),permissionList, type,DEFAULT);
    }

    /**
     * 获取报表拥有的权限集
     * @return 权限集
     */
    public static List<OperationType> getReport(){
        return REPORT;
    }

    /**
     * 获取目录拥有的权限集
     * @return 权限集
     */
    public static List<OperationType> getMenu(){
        return MENU;
    }

    /**
     * 获取权限
     * @param obj
     * @return
     * @param <T>
     */
    private static <T extends BaseAuthPo> List<OperationType> getPermissionList(T obj){
        if(obj instanceof JvsDataReport){
            return REPORT;
        }
        return MENU;
    }

}
