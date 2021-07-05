package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.SaleChanceMapper;
import com.xxxx.crm.query.SaleChanceQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.vo.SaleChance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleChanceService extends BaseService<SaleChance,Integer> {

    @Resource
    private SaleChanceMapper saleChanceMapper;

    /**
     * 多条件分页查询营销机会
     * @param saleChanceQuery
     * @return
     */
    public Map<String,Object> querySaleChanceByParams(SaleChanceQuery saleChanceQuery){

        Map<String,Object> map = new HashMap<>();
        // 开启分页 获取getPage 和 getLimit (SaleChanceQuery的父类BaseQuery中的属性)
        PageHelper.startPage(saleChanceQuery.getPage(),saleChanceQuery.getLimit());
        // 查询
        List<SaleChance> list = saleChanceMapper.selectByParams(saleChanceQuery);
        // 根据SaleChanceQuery 获取 t_sale_chance表中的信息并分页处理
        PageInfo<SaleChance> pageInfo = new PageInfo<>(list);
        map.put("code",0);
        map.put("msg","");
        // 获取总记录数
        map.put("count",pageInfo.getTotal());
        // 结果集
        map.put("data",pageInfo.getList());
        return map;
    }
// -------------------------------------------------------------------------------------------------------------------------------------------
    /**
     * 营销机会数据添加
     *   1.参数校验
     *      customerName:非空
     *      linkMan:非空
     *      linkPhone:非空 11位手机号
     *   2.设置相关参数默认值
     *      state:默认未分配  如果选择分配人  state 为已分配
     *      assignTime:如果  如果选择分配人   时间为当前系统时间
     *      devResult:默认未开发 如果选择分配人devResult为开发中 0-未开发 1-开发中 2-开发成功 3-开发失败
     *      isValid:默认有效数据(1-有效  0-无效)
     *      createDate updateDate:默认当前系统时间
     *   3.执行添加 判断结果
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveSaleChance(SaleChance saleChance){
        // 1.参数校验
        checkParams(saleChance.getCustomerName(),saleChance.getLinkMan(),saleChance.getLinkPhone());
        // 2.设置相关参数信息
        // 如果 : 未选择分配人 AssignMan(分配人)
        if (StringUtils.isBlank(saleChance.getAssignMan())){
            saleChance.setState(0); // 0--未分配 , 1--已分配
            // 开发结果
            saleChance.setDevResult(0); //devResult(开发结果):默认未开发 如果选择分配人devResult为开发中 0-未开发 1-开发中 2-开发成功 3-开发失败
            // 分配时间
            saleChance.setAssignTime(null);
        }
        // 如果 : 已经选择分配人
        if(StringUtils.isNotBlank(saleChance.getAssignMan())){
            saleChance.setState(1); // 0--未分配 , 1--已分配
            // 开发结果
            saleChance.setDevResult(1); //devResult(开发结果):默认未开发 如果选择分配人devResult为开发中 0-未开发 1-开发中 2-开发成功 3-开发失败
            // 分配时间saleChance.setAssignTime(new Date());

        }
        // 创建时间和更新时间
        saleChance.setCreateDate(new Date());
        saleChance.setUpdateDate(new Date());
        // 添加是否成功
        AssertUtil.isTrue(saleChanceMapper.insertSelective(saleChance)<1,"添加失败");

    }

    /**
     * 基本参数校验
     * @param customerName 客户名
     * @param linkMan   联系人
     * @param linkPhone 手机号
     */
    private void checkParams(String customerName, String linkMan, String linkPhone) {
        AssertUtil.isTrue(StringUtils.isBlank(customerName),"请输入客户名!");
        AssertUtil.isTrue(StringUtils.isBlank(linkMan),"联系人不能为空!");
        AssertUtil.isTrue(StringUtils.isBlank(linkPhone),"手机号不能为空!");
        AssertUtil.isTrue(!PhoneUtil.isMobile(linkPhone),"手机号码格式不正确!");

    }
// -------------------------------------------------------------------------------------------------------------------------------------------
    /**
     * 营销机会数据更新
     *  1.参数校验
     *      id:记录必须存在
     *      customerName:非空
     *      linkMan:非空
     *      linkPhone:非空，11位手机号
     *  2. 设置相关参数值
     *      updateDate:系统当前时间
     *         原始记录 未分配 修改后改为已分配(由分配人决定)
     *            state 0->1
     *            assginTime 系统当前时间
     *            devResult 0-->1
     *         原始记录  已分配  修改后 为未分配
     *            state  1-->0
     *            assignTime  待定  null
     *            devResult 1-->0
     *  3.执行更新 判断结果
     */
   /* @Transactional(propagation = Propagation.REQUIRED)*/
    public void updateSaleChance(SaleChance saleChance){
        // 1.参数校验 id:记录必须存在
        SaleChance temp = saleChanceMapper.selectByPrimaryKey(saleChance.getId());
        // 判断是否为空
        AssertUtil.isTrue(null == temp,"待更新记录不存在");
        // 校验其他参数 customerName linkMan linkPhone
        checkParams(saleChance.getCustomerName(),saleChance.getLinkMan(),saleChance.getLinkPhone());
        // 2. 设置相关参数值
        // temp.getAssignMan() 原纪录没有分配人  ||  saleChance.getAssignMan()新纪录有分配人  原始记录 未分配 修改后改为已分配(由分配人决定)
        if (StringUtils.isBlank(temp.getAssignMan()) && StringUtils.isNotBlank(saleChance.getAssignMan())){
            // 分配状态
            saleChance.setState(1);
            // 开发状态
            saleChance.setDevResult(1);
            // 分配时间
            saleChance.setAssignTime(new Date());

            // temp.getAssignMan() 原纪录有分配人  ||  saleChance.getAssignMan()新纪录没有分配人  原始记录  已分配  修改后 为未分配
        }else if (StringUtils.isNotBlank(temp.getAssignMan()) && StringUtils.isBlank(saleChance.getAssignMan())){
            // 分配状态
            saleChance.setState(0);
            // 开发状态
            saleChance.setDevResult(0);
            // 分配时间
            saleChance.setAssignTime(null);
        }

        saleChance.setUpdateDate(new Date());
        // 判断修改是否成功
        AssertUtil.isTrue(saleChanceMapper.updateByPrimaryKeySelective(saleChance)<1,"更新失败");
    }

    /**
     * 营销机会数据删除
     * @param ids
     */
    public  void deleteSaleChance(Integer[] ids){
        // 判断要删除的id是否为空
        AssertUtil.isTrue(null == ids || ids.length == 0, "请选择需要删除的数据！");
        // 删除数据
        AssertUtil.isTrue(saleChanceMapper.deleteBatch(ids) < 0, "营销机会数据删除失败！");
    }



    /**
     * 更新营销机会的状态
     *      成功 = 2
     *      失败 = 3
     * @param id
     * @param devResult
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateSaleChanceDevResult(Integer id, Integer devResult) {
        AssertUtil.isTrue( null ==id,"待更新记录不存在!");
        SaleChance temp =selectByPrimaryKey(id);
        AssertUtil.isTrue( null ==temp,"待更新记录不存在!");
        temp.setDevResult(devResult);
        AssertUtil.isTrue(updateByPrimaryKeySelective(temp)<1,"机会数据更新失败!");
    }


}
