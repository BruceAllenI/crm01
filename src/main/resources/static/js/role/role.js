layui.use(['table','layer'],function(){
    var layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        table = layui.table;
    //角色列表展示
    var  tableIns = table.render({
        elem: '#roleList',
        url : ctx+'/role/list',
        cellMinWidth : 95,
        page : true,
        height : "full-125",
        limits : [10,15,20,25],
        limit : 10,
        toolbar: "#toolbarDemo",
        id : "roleListTable",
        cols : [[
            {type: "checkbox", fixed:"left", width:50},
            {field: "id", title:'编号',fixed:"true", width:80},
            {field: 'roleName', title: '角色名', minWidth:50, align:"center"},
            {field: 'roleRemark', title: '角色备注', minWidth:100, align:'center'},
            {field: 'createDate', title: '创建时间', align:'center',minWidth:150},
            {field: 'updateDate', title: '更新时间', align:'center',minWidth:150},
            {title: '操作', minWidth:150, templet:'#roleListBar',fixed:"right",align:"center"}
        ]]
    });

    // 多条件搜索
    $(".search_btn").on("click",function(){
        table.reload("roleListTable",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                roleName: $("input[name='roleName']").val()
            }
        })
    });

    //头工具栏事件
    table.on('toolbar(roles)', function(obj){
        var checkStatus = table.checkStatus(obj.config.id);
        switch(obj.event){
            case "add":
                // 打开添加/更新角色的对话框
                openAddOrUpdateRoleDialog();
                break;
            case "grant" :
                // (checkStatus.data) : 获取数据表格选中的记录数据
                // 打开授权的对话框
                openAddGrantDailog(checkStatus.data);
                break;
        };
    });

    /**
     * 行监听
     */
    table.on("tool(roles)", function(obj){
        var layEvent = obj.event;
        if(layEvent === "edit") {
            openAddOrUpdateRoleDialog(obj.data.id);
        }else if(layEvent === "del") {
            layer.confirm('确定删除当前角色？', {icon: 3, title: "角色管理"}, function (index) {
                $.post(ctx+"/role/delete",{roleId:obj.data.id},function (data) {
                    if(data.code==200){
                        layer.msg("操作成功！");
                        tableIns.reload();
                    }else{
                        layer.msg(data.msg, {icon: 5});
                    }
                });
            })
        }
    });

    /**
     * 打开添加/更新角色对话框
     * @param uid
     */
    function openAddOrUpdateRoleDialog(uid){
        var url  =  ctx+"/role/toAddOrUpdatePage";
        var title="角色管理-角色添加";
        if(uid){
            title="角色管理-角色更新";
            url+="?uid="+uid;
        }
        layui.layer.open({
            title : title,
            type : 2,
            area:["600px","280px"],
            maxmin:true,
            content :url
        });
    }


    /**
     * 打开授权页面
     * @param datas
     */
    function openAddGrantDailog(datas){
        // 判断是否选择角色记录
        if (datas.length == 0){
            layer.msg("请选择待授权角色记录",{icon:5});
            return ;
        }
        // 只支持单个角色授权
        if(datas.length > 1){
            layer.msg("暂不支持此类型的授权",{icon:5});
            return ;
        }
        // roleId=   传送数据   datas[0].id 只接收选择数据的第一条
        var url = ctx + "/role/toAddGrantPage?roleId=" + datas[0].id;
        var title="角色管理-角色授权";
        layui.layer.open({
            title : title,
            type : 2,
            area:["600px","280px"],
            maxmin:true,
            content : url
        });

    }



});




















