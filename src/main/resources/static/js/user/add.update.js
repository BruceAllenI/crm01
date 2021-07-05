layui.use(['form', 'jquery', 'jquery_cookie','formSelects'], function () {
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery;
    // 引入 formSelects 模块
    formSelects = layui.formSelects;


    /**
     * 加载下拉框数据
     */
    var userId=$("input[name='id']").val();
    formSelects.config('selectId',{
        type:"post",
        searchUrl:ctx+"/role/queryAllRoles?userId="+userId,
        keyName: 'roleName', //⾃定义返回数据中name的key, 默认 name
        keyVal: 'id' //⾃定义返回数据中value的key, 默认 value
    },true);



    /**
     * 添加或更新用户
     */
    form.on("submit(addOrUpdateUser)", function (data) {
        // 弹出loading层
        var index = top.layer.msg('数据提交中,请稍后', {icon: 16, time: 500, shade: 0.8});
        var url = ctx + "/user/save";
        if ($("input[name='id']").val()){
            url = ctx + "/user/update";
        }
        //发送ajax
        $.post(url,data.field,function(obj){
            if(obj.code== 200){
                window.setTimeout(function(){
                    //关闭弹出层
                    top.layer.close(index);
                    //关闭ifream
                    layer.closeAll("iframe");
                },500)
                //加载一下数据
                parent.location.reload();
            }else{
                //添加失败了
                layer.msg(obj.msg,{icon:5 });
            }
        },"json");
        return false;
    });
    /**
     * 关闭弹出层
     */
    $("#closeBtn").click(function () {
        var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
        parent.layer.close(index); //再执行关闭
    });


});