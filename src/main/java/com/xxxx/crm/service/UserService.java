package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.UserMapper;
import com.xxxx.crm.dao.UserRoleMapper;
import com.xxxx.crm.model.UserModel;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.Md5Util;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.utils.UserIDBase64;
import com.xxxx.crm.vo.User;
import com.xxxx.crm.vo.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName,String userPwd){
        //1.验证参数
        checkLoginParams(userName,userPwd);
        // 2.根据用户名,查询用户对象
        User user = userMapper.queryUserByUserName(userName);
        // 3.判断用户是否存在(用户对象为空,记录不存在,方法结束)
        AssertUtil.isTrue(null == user,"用户不存在或已注销!");
        // 4.用户对象不为空(用户存在,校验密码.密码不正确,方法结束)
        checkLoginPwd(userPwd,user.getUserPwd());
        // 5.密码正确(用户登录成功,返回用户的相关信息)
        return buildUserInfo(user);
    }

    /**
     * 构建返回的用户信息
     * @param user
     * @return
     */
    private UserModel buildUserInfo(User user) {
        UserModel userModel = new UserModel();
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        return userModel;
    }

    /**
     * 验证用户登录参数
     * @param userName
     * @param userPwd
     */
    private void checkLoginParams(String userName, String userPwd) {
        // 判断姓名
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空!");
        // 判断密码
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"密码不能为空!");
    }


    /**
     * 验证登录密码
     * @param userPwd 前台传递的密码
     * @param uPwd  数据库中查询到的密码
     */
    private void checkLoginPwd(String userPwd, String uPwd) {
        //数据库中的密码是经过加密的,将前台传递的密码先加密,再与数据库中的密码作比较
        userPwd = Md5Util.encode(userPwd);
        // 比较密码
        AssertUtil.isTrue(!userPwd.equals(uPwd),"用户密码不正确!");
    }

    /**
     * 用户密码修改 :
     *      1. 参数校验
     *          用户ID : userId 非空 用户对象必须存在
     *          原始密码 : oldPassword 非空 与数据库中加密过后的密码保持一致
     *          新密码 : newPassword 非空 与原始密码不能相同
     *          确认密码 : confirmPassword 非空 与新密码保持一致
     *      2. 设置用户新密码
     *          新密码加密处理
     *      3. 执行更新操作
     *          受影响的行数小于1, 则表示修改失败
     *
     *      注 : 在对应的更新方法上,添加事务控制
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUserPassword(Integer userId,String oldPassword,String newPassword,String confirmPassword){

        //通过userId获取用户对象
        User user = userMapper.selectByPrimaryKey(userId);
        // 1.参数校验
        checkPasswordParams(user,oldPassword,newPassword,confirmPassword);
        // 2.设置用户新密码
        user.setUserPwd(Md5Util.encode(newPassword));
        // 3.执行更新操作
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"用户密码更新失败!");
    }

    /**
     * 验证用户密码修改参数
     *      用户ID : userId 非空 用户对象必须存在
     *      原始密码 : oldPassword 非空 与数据库中加密过后的密码保持一致
     *      新密码 : newPassword 非空 与原始密码不能相同
     *      确认密码 : confirmPassword 非空 与新密码保持一致
     * @param user
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     */
    private void checkPasswordParams(User user, String oldPassword, String newPassword, String confirmPassword) {
        // user 对象 非空验证
        AssertUtil.isTrue(null == user,"用户未登录或不存在");
        // 原始密码 非空验证
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"请输入原始密码");
        // 原始密码要与数据库中的加密过后的密码保持一致
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))),"原始密码不正确!");
        // 新密码 非空校验
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"请输入新密码!");
        // 新密码与原始密码不能相同
        AssertUtil.isTrue(oldPassword.equals(newPassword),"新密码不能和原始密码相同!");
        // 确认密码,非空校验
        AssertUtil.isTrue(StringUtils.isBlank(confirmPassword),"请确认密码!");
        // 新密码要和确认密码一致
        AssertUtil.isTrue(!(newPassword.equals(confirmPassword)),"两次输入的密码不一致!");
    }

    /**
     * 查询所有的销售人员
     * @return
     */
    public List<Map<String,Object>> queryAllSales(){
        return userMapper.queryAllSales();
    }

    /**
     * 多条件分页查询用户记录
     * @param userQuery
     * @return
     */
    public Map<String,Object> queryUserByParams(UserQuery userQuery){
        Map<String,Object> map = new HashMap<>();
        // 开启分页
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        // 根据SaleChanceQuery 获取 t_sale_chance表中的信息并分页处理
        PageInfo<User> pageInfo = new PageInfo<>(userMapper.selectByParams(userQuery));

        map.put("code",0);
        map.put("msg","success");
        // 获取总记录数
        map.put("count",pageInfo.getTotal());
        // 结果集
        map.put("data",pageInfo.getList());
        return map;
    }

    /**
     * 添加用户
     * @param user
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveUser(User user){
        // 验证
        checkParams(user.getUserName(),user.getEmail(),user.getPhone());
        // 设置默认参数
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        user.setUserPwd(Md5Util.encode("123456"));
        // 执行添加,判断结果
        AssertUtil.isTrue(userMapper.insertUserByReturnKey(user) == null,"添加失败");

        relaionUserRole(user.getId(), user.getRoleIds());
    }

    /**
     * 参数校验
     * @param userName
     * @param email
     * @param phone
     */
    private void checkParams(String userName, String email, String phone) {
        // 校验用户名
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空!");
        // 验证用户名是否存在
        User temp = userMapper.queryUserByUserName(userName);
        AssertUtil.isTrue(null != temp,"用户名已经存在!");
        AssertUtil.isTrue(StringUtils.isBlank(email),"请输入邮箱地址!");
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"手机号码格式不正确!");
    }

    /**
     * 更新用户
     *  1. 参数校验
     *      id  非空  记录必须存在
     *      用户名 非空  唯一性
     *      email 非空
     *      手机号 非空 格式合法
     *  2. 设置默认参数
     *      updateDate
     *  3. 执行更新，判断结果
     * @param user
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUser(User user){
        // 1.参数校验
        // 通过id查询用户对象
        User temp = userMapper.selectByPrimaryKey(user.getId());
        // 判断用户对象是否存在
        AssertUtil.isTrue(temp == null,"待更新记录不存在!");

        // 校验用户名
        AssertUtil.isTrue(StringUtils.isBlank(user.getUserName()),"用户名不能为空!");
        User temp1 = userMapper.queryUserByUserName(user.getUserName());
        
        AssertUtil.isTrue(StringUtils.isBlank(user.getEmail()),"请输入邮箱地址!");
        AssertUtil.isTrue(!PhoneUtil.isMobile(user.getPhone()),"手机号码格式不正确!");

        // 2.设置默认参数
        user.setUpdateDate(new Date());
        // 3.执行更新,判断结果
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"用户更新失败!");

        Integer userId = userMapper.queryUserByUserName(user.getUserName()).getId();
        relaionUserRole(userId, user.getRoleIds());
    }

    private void relaionUserRole(Integer id, String roleIds) {
        //统计角色
        int count = userRoleMapper.countUserRoles(id);
        if(count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRolesByUid(id)!=count,"角色分配失败");
        }
        if(StringUtils.isNotBlank(roleIds)){
            //准备一个List
            List<UserRole> urlist=new ArrayList<UserRole>();
            //遍历
            for (String rid: roleIds.split(",")) {
                UserRole ur=new UserRole();
                ur.setUserId(id);
                ur.setRoleId(Integer.parseInt(rid));
                ur.setCreateDate(new Date());
                ur.setUpdateDate(new Date());
                //添加容器
                urlist.add(ur);
            }
            //insert
            AssertUtil.isTrue(userRoleMapper.insertBatch(urlist)!=urlist.size(),"角色分配失败了");
        }
    }


    /**
     * 删除用户
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(Integer userId) {
        User user = selectByPrimaryKey(userId);
        AssertUtil.isTrue(null == userId || null == user, "待删除记录不存在!");
        int count = userRoleMapper.countUserRoles(userId);
        if (count > 0) {
            AssertUtil.isTrue(userRoleMapper.deleteUserRolesByUid(userId) != count, "用户角色删除失败!");
        }
        user.setIsValid(0);
        AssertUtil.isTrue(updateByPrimaryKeySelective(user) < 1, "用户记录删除失败!");

    }
}


















