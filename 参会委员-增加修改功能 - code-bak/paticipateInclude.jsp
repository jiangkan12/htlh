<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/common/path_header.jsp" %>

<html>
    <head>
        <%@include file="/common/grid_header.jsp" %>
        <%@include file="/common/index_header.jsp" %>
        <%@include file="/common/ibms_header.jsp" %>
        <script type="text/javascript">
            window.onload=function (){
                var _params={};
                _params["STATE"]="1";
                _params["SOURCE_TYPE"]="inner_parter_gq_lx"; //股权参会委员
                meetingpaticipateGrid.load(_params);
            }

            /**
             * 查询
             */
            function query() {
                var _params={};
                var queryType = $("#queryType").val();
                var queryName = $.trim($("#queryName").val());
                var queryCompany = $.trim($("#queryCompany").val());
                _params["STATE"]="1";
                _params["SOURCE_TYPE"]=queryType;
                _params["REAL_NAME"]=queryName;
                _params["COMPANY"]=queryCompany;
                $("#queryTypeHidden").val($("#queryType").find("option:selected").text());
                $("#queryTypeValueHidden").val(queryType);
                meetingpaticipateGrid.reload(_params);
            }

            function createPaticipate(){
                var _url=path+"/meetingpaticipate/showAdd.do?category=1&flag=0";
                var _params={"callback":function (){
                    meetingpaticipateGrid.reload();
                }};
                rims.window.showWindow(_url,700,400,_params);
            }
            /**
             * 编辑参会委员
             */
            function editPaticipate(){

                rims.grid.doWithSelectedRow(meetingpaticipateGrid,function (row){
                    var _url=path+"/meetingpaticipate/showAdd.do?category=1&flag=1&type="+$("#queryTypeValueHidden").val()+"&id="+row.UUID;
                    var _params={"callback":function (){
                        meetingpaticipateGrid.reload();
                    }};
                    rims.window.showWindow(_url,700,400,_params);
                });
            }

            function deletePaticipate(){
                rims.grid.doWithSelectedRow(meetingpaticipateGrid,function (row){
                    var queryType = $("#queryTypeHidden").val();
                    var queryTypeValue = $("#queryTypeValueHidden").val();
                    if(confirm("确认从"+queryType+"中删除"+row.REAL_NAME+"？")){
                        dwrMeetingPaticipate.deletePaticipate(row.UUID,queryTypeValue,{callback:function (){
                            alert("删除成功!");
                            meetingpaticipateGrid.reload();
                        },
                            errorHandler:function (errormsg,exception){
                                alert("删除出错!");
                            }
                        });
                    }
                });
            }
        </script>
    </head>
    <body>
    <jsp:include flush="true" page="/modules/comm/inc/window_page_header.jsp">
        <jsp:param name="title" value="股权参会委员管理"/>
    </jsp:include>
        <input type="hidden" id="queryTypeHidden" value="立项内部参会委员" />
        <input type="hidden" id="queryTypeValueHidden" value="inner_parter_gq_lx" />
        <div>
            <div  style="margin:10px; " align="right">
                <span>类型：
                    <select id="queryType" style="width: 125px">
                        <option value="inner_parter_gq_lx" selected="selected">立项内部参会委员</option>
                        <option value="out_parter_gq_lx">立项外部参会委员</option>
                        <option value="inner_parter_gq_nh">内核内部参会委员</option>
                        <option value="out_parter_gq_nh">内核外部参会委员</option>
                    </select>
                </span>&nbsp;&nbsp;
                <span>姓名：<input type="text" id="queryName"/></span>&nbsp;&nbsp;
                <span>公司：<input type="text" id="queryCompany"/></span>&nbsp;&nbsp;
                <span>
                    <a href="javascript:query();" class="but-sty2">
                        <span>查询</span><b class="br"></b>
                    </a>
                </span>
<c:if test="${rims_org_fn:hasRight('editequitycommittee')}"> <%--【*数据权限*】--%>
                <span style="">
                    <a href="javascript:createPaticipate();" class="but-sty2">
                        <span>新增</span><b class="br"></b>
                    </a>
                </span>
                <span style="">
                    <a href="javascript:editPaticipate();" class="but-sty2">
                        <span>修改</span><b class="br"></b>
                    </a>
                </span>
                <span style="">
                    <a href="javascript:deletePaticipate();" class="but-sty2">
                        <span>删除</span><b class="br"></b>
                    </a>
                </span>
</c:if>
            </div>
            <div id='gridContain'></div>
            <g:grid gridId="meetingpaticipateGrid" container="gridContain" limit="15" sqlId="project.meetingpaticipate.queryByCondition" dataSource="spring:lhzq_css"
                    dbType="oracle" pageSizes="[15,20,30,100]" getCountType="asyn"  columnDisplayHidden="false" loadforstart="true"  ignoreSpecialChar="true">
                <!-- 列表展现 -->
                <%--<g:column key="FOMAT_SOURCE_TYPE" label="类型" width="30%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>--%>
                <g:column key="REAL_NAME" label="姓名" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull" align="center"></g:column>
                <g:column key="COMPANY" label="公司" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>
                <g:column key="PHONE" label="办公电话" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>
                <g:column key="MOBILE" label="手机" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>
                <g:column key="EMAIL" label="邮箱" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>
                <g:column key="ADDRESS" label="地址" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>
                <g:column key="MODIFY_USER" label="操作人" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>
                <g:column key="MODIFY_DATETIME" label="操作日期" width="10%" query="false"  sortable="true" expression="rims.grid.renderNull"></g:column>
            </g:grid>
        </div>
    </body>
</html>

