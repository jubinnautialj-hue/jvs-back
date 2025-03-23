package cn.bctools.screen.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.permission.utils.AuthVerifyUtils;
import cn.bctools.screen.entity.ChartPage;
import cn.bctools.screen.entity.SysMenu;
import cn.bctools.screen.model.BaseAuthPo;
import cn.hutool.core.collection.CollectionUtil;
import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wl
 * 权限鉴定
 */
public class AuthUtils {

    private static final List<OperationType> SCREEN = new ArrayList<OperationType>(){{
        add(OperationType.查看);
        add(OperationType.设计);
        add(OperationType.下载);
        add(OperationType.删除);
        add(OperationType.移动);
        add(OperationType.复制);
    }};

    private static final List<OperationType> MENU = new ArrayList<OperationType>(){{
        add(OperationType.编辑);
        add(OperationType.删除);
        add(OperationType.移动);
        add(OperationType.查看);
        add(OperationType.新增);
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
     * 过滤
     * @param list
     * @return
     */
    public static <T extends BaseAuthPo> List<T> filter(List<T> list){
        return list.stream().peek(AuthUtils::setOperation).filter(e -> CollectionUtil.isNotEmpty(e.getOperationList())).collect(Collectors.toList());
    }

    /**
     * 获取报表拥有的权限集
     * @return 权限集
     */
    public static List<OperationType> getScreen(){
        return SCREEN;
    }

    /**
     * 获取目录拥有的权限集
     * @return 权限集
     */
    public static List<OperationType> getMenu(){
        return MENU;
    }


    /**
     * 检查是否有权限
     * @param menu 菜单
     * @param operationEnum 操作类型
     */
    public static void check(SysMenu menu, OperationType operationEnum){
        boolean verify = verify(menu, operationEnum);
        if(!verify){
            throw new BusinessException("未拥有当前操作权限");
        }
    }

    /**
     * 获取权限
     * @param obj
     * @return
     * @param <T>
     */
    private static <T extends BaseAuthPo> List<OperationType> getPermissionList(T obj){
        if(obj instanceof ChartPage){
            return SCREEN;
        }
        return MENU;
    }

}
