package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission,Integer> {

    List<Integer> queryRoleHasAllModuleIdsByRoleId(Integer roleId);

//    根据角色id查询资源的数量集合
    int countPermissionByRoleId(Integer roleId);
//    根据roleId删除其所有的权限信息
    int deletePermissionsByRoleId(Integer roleId);

    List<String> queryUserHasRolesHasPermissions(Integer userId);
}