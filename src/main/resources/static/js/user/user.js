layui.use(['form','jquery','jquery_cookie','table'],function (){
    var form = layui.form,
        table=layui.table,
        layer = layui.layer,
        $ = layui.jquery,
        $ = layui.jquery_cookie($);

    /**
     * 用户列表展示
     */
    var tableIns = table.render({
        elem: '#userList',
        url: ctx + '/user/list',
        cellMinWidth: 95,
        page: true,
        height: "full-125",
        limits: [10, 15, 20, 25],
        limit: 10,
        toolbar: "#toolbarDemo",
        id: "userListTable",
        cols: [[
            {type: "checkbox", fixed: "left", width: 50},
            {field: "id", title: "编号", fixed: "true", width: 80},
            {field: 'userName', title: '用户名', minWidth: 50, align: "center"},
            {field: 'email', title: '用户邮箱', minWidth: 100, align: 'center'},
            {field: 'phone', title: '用户电话', minWidth: 100, align: 'center'},
            {field: 'trueName', title: '真实姓名', align: 'center'},
            {field: 'createDate', title: '创建时间', align: 'center', minWidth: 150},
            {field: 'updateDate', title: '更新时间', align: 'center', minWidth: 150},
            {title: '操作', minWidth: 150, templet: '#userListBar', fixed: "right", align: "center"}
        ]]
    });

    /**
     * 头部工具栏事件
     */
    table.on("toolbar(users)", function (obj) {
        var checkStatus = table.checkStatus(obj.config.id);
        switch (obj.event) {
            case "add":
                openAddOrUpdateUserDialog();
                break;
            case "del":
                deleteUser(checkStatus.data);
                break;
        }
    });
    /**
     * 行监听事件
     */
    table.on("tool(users)", function(obj){
        var layEvent = obj.event;
        // 监听编辑事件
        if(layEvent === "edit") {
            openAddOrUpdateUserDialog(obj.data.id);
        } else if(layEvent === "del") {
            // 监听删除事件
            layer.confirm('确定删除当前用户？', {icon: 3, title: "用户管理"}, function (index) {
                $.post(ctx + "/user/delete",{ids:obj.data.id},function (data) {
                    if(data.code==200){
                        layer.msg("操作成功！");
                        tableIns.reload();
                    }else{
                        layer.msg(data.msg, {icon: 5});
                    }
                });
            });
        }
    });
    /**
     * 打开用户添加或更新对话框
     */
    function openAddOrUpdateUserDialog(userId){
        var url = ctx + "/user/addOrUpdateUserPage";
        var title = "用户管理-用户添加";
        if (userId){
            url = url + "?id=" + userId;
            title = "用户管理-用户更新";
        }
        layui.layer.open({
            title:title,
            type: 2,
            area:["650px","400px"],
            maxmin:true,
            content:url
        });
    }

    /**
     * 批量删除用户
     * @param datas
     */
    /**
     * 批量删除用户
     * @param datas
     */
    function  deleteUser(data){
        //验证
        if(data.length==0){
            layer.msg("请选择要删除的数据?");
            return ;
        }
        //声明数组存储数据
        var ids=[];
        //遍历
        for(var x in data){
            ids.push(data[x].id);
        }
        layer.confirm("你确定要删除数据吗?",{
            btn:["确定","取消"],
        },function(index){
            layer.close(index);
            //发送ajax
            $.ajax({
                type:"post",
                data:{"ids":ids.toString()},
                url:ctx+"/user/delete",
                dataType:"json",
                success:function (obj){
                    if(obj.code == 200){
                        //重新加载表格
                        tableIns.reload();
                    }else{
                        //删除失败
                        layer.msg(obj.msg,{icon: 5 });
                    }
                }
            });

        });


    }

    /**
     * 行监听事件
     */

});










