package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.ModuleMapper;
import com.xxxx.crm.dao.PermissionMapper;
import com.xxxx.crm.dao.RoleMapper;
import com.xxxx.crm.dao.UserRoleMapper;
import com.xxxx.crm.query.RoleQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.Module;
import com.xxxx.crm.vo.Permission;
import com.xxxx.crm.vo.Role;
import com.xxxx.crm.vo.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RoleService extends BaseService<Role,Integer> {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private ModuleMapper moduleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;


    public List<Map<String,Object>> queryAllRole(Integer userId){
        return roleMapper.selectAllRoles(userId);
    }

    /**
     * 查询所有的角色
     * @return
     */
    public Map<String,Object> queryAllRoles(RoleQuery query){
        //实例化map
        Map<String,Object> map=new HashMap<String,Object>();
//开始分页
        PageHelper.startPage(query.getPage(),query.getLimit());
//查询所有的数据
        List<Role> rlist = roleMapper.selectByParams(query);
//实例化pageInfo
        PageInfo<Role> pageInfo=new PageInfo<>(rlist);

        map.put("code",0);
        map.put("msg","success");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());

        return  map;
    }

    /**
     * 用户添加操作
     * @param role
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveUser(Role role){
        // 参数校验 非空,角色不能重复
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"角色名不能为空!");
        Role temp = roleMapper.selectRoleByName(role.getRoleName());
        AssertUtil.isTrue(temp!=null,"角色已经存在!");
        // 设置相关信息(默认值)
        role.setCreateDate(new Date());
        role.setUpdateDate(new Date());
        role.setIsValid(1);
        // 添加是否成功
        AssertUtil.isTrue(roleMapper.insertSelective(role)<1,"用户添加失败");

    }

    /**
     * 用户修改操作
     * @param role
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUser(Role role){
        // 参数校验 非空
        AssertUtil.isTrue(role.getId()==null || roleMapper.selectByPrimaryKey(role.getId())==null,"待修改角色不存在!");
        // 验证角色名称是否未空
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入角色名称!");
        // 角色名称不能重复
        Role temp = roleMapper.selectRoleByName(role.getRoleName());
        AssertUtil.isTrue(temp!=null && (temp.getId().equals(role.getId())),"角色已经存在!");
        // 默认值
        role.setUpdateDate(new Date());
        // 添加是否成功
        AssertUtil.isTrue(roleMapper.updateByPrimaryKeySelective(role)<1,"修改角色失败!");

    }



    /**
     * 用户删除操作
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteRole(Integer roleId){
        // 根据id查询用户,
        Role temp = roleMapper.selectByPrimaryKey(roleId);
        // 非空判断
        AssertUtil.isTrue(null == temp || null == roleId,"待删除的记录不存在!");
        // 修改is_valid的值为0
        temp.setIsValid(0);
        // 判断删除是否成功
        AssertUtil.isTrue(roleMapper.updateByPrimaryKeySelective(temp)<1,"删除记录失败!");
    }

    /**
     * 权限记录添加
     * @param mids
     * @param roleId
     */
    public void addGrant(Integer[] mids,Integer roleId){
        /**
         * 核心表 t_permission t_role(校验角色存在)
         *  如果角色存在原始权限 删除角色原始权限
         *      然后添加角色新权限 批量添加权限记录到t_permission
         */
        //roleyan验证
        Role temp = roleMapper.selectByPrimaryKey(roleId);
        AssertUtil.isTrue(roleId==null || temp==null,"待授权的角色不存在");
        //统计一下角色的权限数量
        int count=permissionMapper.countPermissionByRoleId(roleId);
        if(count>0){
            AssertUtil.isTrue(permissionMapper.deletePermissionsByRoleId(roleId)!=count,"权限分配失败");
        }
        //收集所有的权限数据
        if(mids!=null && mids.length>0){
            //准备存储权限的集合
            List<Permission> permissions=new ArrayList<Permission>();
            for (Integer mid: mids) {
                //实例化对象Permission
                Permission permission=new Permission();
                permission.setRoleId(roleId);
                permission.setModuleId(mid);
                //默认参数
                permission.setCreateDate(new Date());
                permission.setUpdateDate(new Date());
                //寻找当前module信息，根据mid
                Module module = moduleMapper.selectByPrimaryKey(mid);
                //赋值
                permission.setAclValue(module.getOptValue());
                //存储到集合
                permissions.add(permission);
            }
            //批量添加验证
            AssertUtil.isTrue(permissionMapper.insertBatch(permissions)!=permissions.size(),"授权失败");

        }

    }

}
