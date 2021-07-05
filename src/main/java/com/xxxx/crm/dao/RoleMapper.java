package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.Role;

import java.util.List;
import java.util.Map;

public interface RoleMapper extends BaseMapper<Role,Integer> {
    // 查询角色列表
    public List<Map<String,Object>> selectAllRoles(Integer userId);
    Role selectRoleByName(String roleName);
}