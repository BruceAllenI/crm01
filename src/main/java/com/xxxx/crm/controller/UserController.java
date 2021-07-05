package com.xxxx.crm.controller;

import com.xxxx.crm.annoation.RequiredPermission;
import com.xxxx.crm.base.BaseController;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.exceptions.ParamsException;
import com.xxxx.crm.model.UserModel;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.service.UserService;
import com.xxxx.crm.utils.LoginUserUtil;
import com.xxxx.crm.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param userName
     * @param userPwd
     * @return
     */
    @RequestMapping("user/login")
    @ResponseBody
    public ResultInfo userLogin(String userName,String userPwd){
        ResultInfo resultInfo = new ResultInfo();

        // 调用Service层的登录方法,得到返回的用户对象
        UserModel userModel = userService.userLogin(userName,userPwd);

        //将返回的UserModel对象设置到ResultInfo对象中
        resultInfo.setResult(userModel);

        return resultInfo;
    }

    /**
     * 修改密码
     * @param request
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @return
     */
    @PostMapping("user/updatePassword")
    @ResponseBody
    public ResultInfo updateUserPassword(HttpServletRequest request, String oldPassword, String newPassword, String confirmPassword){
        ResultInfo resultInfo = new ResultInfo();

        // 获取userId
        Integer userId = LoginUserUtil.releaseUserIdFromCookie(request);

        // 调用Service层的密码修改方法
        userService.updateUserPassword(userId,oldPassword,newPassword,confirmPassword);

        return resultInfo;

    }

    /**
     * 修改密码界面跳转
     */
    @RequestMapping("user/toPasswordPage")
    public String toPasswordPage(){
        return "user/password";
    }

    /**
     * 基本资料界面跳转
     */
    @RequestMapping("user/toSettingPage")
    public String toSettingPage(){
        return "user/setting";
    }

    /**
     * 查询所有的销售人员
     * @return
     */
    @RequestMapping("user/queryAllSales")
    @ResponseBody
    public List<Map<String, Object>> queryAllSales() {
        return userService.queryAllSales();
    }

    /**
     * 多条件查询用户数据
     * @param userQuery
     * @return
     */
    @RequestMapping("user/list")
    @ResponseBody
    public Map<String,Object> queryUserByParams(UserQuery userQuery){
        return userService.queryUserByParams(userQuery);
    }
    /**
     * 用户管理界面跳转
     */
    @RequiredPermission(code = "6010")
    @RequestMapping("user/index")
    public String index(){
        return "user/user";
    }

    /**
     * 添加用户
     *  1. 参数校验
     *      用户名 非空 唯一性
     *      邮箱   非空
     *      手机号 非空  格式合法
     *  2. 设置默认参数
     *      isValid 1
     *      creteDate   当前时间
     *      updateDate  当前时间
     *      userPwd 123456 -> md5加密
     *  3. 执行添加，判断结果
     */
    @RequestMapping("user/save")
    @ResponseBody
    public ResultInfo saveUser(User user){
        userService.saveUser(user);
        return success("用户添加成功!");
    }

    /**
     * 更新用户
     * @param user
     * @return
     */
    @RequestMapping("user/update")
    @ResponseBody
    public ResultInfo updateUser(User user){
        userService.updateUser(user);
        return success("用户更新成功!");
    }

    @RequestMapping("user/addOrUpdateUserPage")
    public String addUserPage(Integer id, Model model){
        if (null != id){
            model.addAttribute("user",userService.selectByPrimaryKey(id));
        }
        return "user/add_update";
    }

    @RequestMapping("user/delete")
    @ResponseBody
    public ResultInfo deleteUser(Integer[] ids){
        userService.deleteBatch(ids);
        return success("用户记录删除成功");
    }


}










