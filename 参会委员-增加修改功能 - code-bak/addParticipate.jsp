<%@ taglib prefix="x_rt" uri="http://java.sun.com/jstl/xml_rt" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/common/path_header.jsp" %>
<html>
<head>
    <title>${editFlag}参会委员</title>
    <%@include file="/common/grid_header.jsp" %>
    <%@include file="/common/index_header.jsp" %>
    <%@include file="/common/ibms_header.jsp" %>
    <%@include file="/commons/jsp/extjs.jsp" %>
    <%@include file="/commons/jsp/meta.jsp" %>
    <script type="text/javascript" src="${path}/modules/org/js/org.js"></script>
    <script type="text/javascript">
        $(document).ready(function (){
            // 选择单个用户
            rims.org.readyToSelectUser(function (selectusers,args){
                args["userListId"]["hiddenname"]="innerAppUser.userId";
                selectusers["userListId"] = {"UUID":"${innerAppUser.userId}${outerAppUser.userId}", "REALNAME":"${innerAppUser.fullName}${outerAppUser.fullName}"};
            });
            if('${innerAppUser.userId}' != '') {
                document.getElementById("categroyType").options[0].selected = 'selected';
            }
            if('${outerAppUser.userId}' != '') {
                document.getElementById("categroyType").options[1].selected = 'selected';
            }
            //选定“类型”
            $("#innerSourceType").val(${type});

            changeCategoryType();
        });

        function save(){
            var categroyType = $("#categroyType").val();
            if(categroyType == 'inner') {
                saveInner();
            } if(categroyType == 'outer') {
                saveOuter();
            }
        }
        function saveInner() {
            if(document.getElementById("innerAppUser.userId") ==null) {
                alert("请选择用户");
                return;
            }
            var userId = document.getElementById("innerAppUser.userId").value;
            var innerSourceType = $("#innerSourceType").val();

            $.post(path+"/meetingpaticipate/validateInnerExist.do",{"appUser.userId":userId,"appUser.sourceType":innerSourceType},function(result){
                if(result != null && result == 1) {
                    alert("参会委员已经存在，请勿重复配置");
                } else {
                    disabledHref(document.getElementById("saveBtn")); //禁用
                    disabledHref(document.getElementById("cancelBtn")); //禁用
                    document.forms[0].action = "addInnerMeetingParticipate.do";
                    document.forms[0].submit();
                }
            });
        }
        function saveOuter() {
            var userName = $.trim($("#fullName").val());
            var outerSourceType = $("#outerSourceType").val();
            var company = $.trim($("#company").val());
            if(userName == '') {
                alert("请填写姓名");
                return;
            }
            $.post(path+"/meetingpaticipate/validateOuterExist.do",{"appUser.fullName":userName,"appUser.sourceType":outerSourceType,"appUser.company":company},function(result){
                if(result != null && result == 1) {
                    alert("参会委员已经存在，请勿重复配置");
                } else {
                    disabledHref(document.getElementById("saveBtn")); //禁用
                    disabledHref(document.getElementById("cancelBtn")); //禁用
                    document.forms[0].action = "addOuterMeetingParticipate.do";
                    document.forms[0].submit();
                }
            });
        }

        function cancel(){
            if(confirm("取消操作？")){
                window.close();
            }
        }

        function changeCategoryType() {
            var categroyType = $("#categroyType").val();
            if(categroyType == 'inner') {
                $("tr[name='inner_tr']").each(function(){
                    $(this).css("display","");
                });

                $("tr[name='outer_tr']").each(function(){
                    $(this).css("display","none");
                });
            } if(categroyType == 'outer') {
                $("tr[name='inner_tr']").each(function(){
                    $(this).css("display","none");
                });

                $("tr[name='outer_tr']").each(function(){
                    $(this).css("display","");
                });
            }
        }

    </script>
</head>
<body>
<struts:form action="addInnerMeetingParticipate" method="post" enctype="multipart/form-data" theme="simple" namespace="/meetingpaticipate">
    <jsp:include flush="true" page="/modules/comm/inc/window_page_header.jsp">
        <jsp:param name="title" value="${editFlag}${categoryName}参会委员"/>
    </jsp:include>
    <div class="show-area">
        <div id="baseinfo" class="slide-panel-con baseinfo">
            <table cellpadding="0" cellspacing="0" width="100%" class="baseinfo-table" id="table1">
                <tr>
                    <td class="tta" width="30%">  &nbsp;分类：</td>
                    <td >
                        <select id="categroyType" onchange="changeCategoryType()">
                            <option value="inner" selected="selected">内部</option>
                            <option value="outer">外部</option>
                        </select>
                    </td>
                </tr>
                <tr name="inner_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;类型：</td>
                    <td >
                        <select  name="innerAppUser.sourceType" id="innerSourceType" >
                            <c:choose>
                                <c:when test="${category == 1}">
                                    <option value="inner_parter_gq_lx" selected="selected">立项参会委员</option>
                                    <option value="inner_parter_gq_nh">内核参会委员</option>
                                </c:when>
                                <c:when test="${category == 2}">
                                    <option value="inner_parter_zq_lx" selected="selected">立项参会委员</option>
                                    <option value="inner_parter_zq_nh">内核参会委员</option>
                                </c:when>
                                <c:when test="${category == 3}">
                                    <option value="inner_parter_gb_lx" selected="selected">立项参会委员</option>
                                    <option value="inner_parter_gb_nh">内核参会委员</option>
                                </c:when>
                            </c:choose>
                        </select>
                    </td>
                </tr>
                <tr name="inner_tr" style="width: 100%">
                    <td class="tta" width="30%">
                        <input id="userListId" class="selectuser" type="button"/>
                        &nbsp;<font color="red">*</font>姓名：
                    </td>
                    <td >
                        <div id="cuserListId"><c:out value="${innerAppUser.fullName}" /></div>
                    </td>
                </tr>

                <!-- 新增外部参会委员信息 -->
                <tr style="display: none" name="outer_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;类型：</td>
                    <td >
                        <select name="outerAppUser.sourceType"  id="outerSourceType">
                            <c:choose>
                                <c:when test="${category == 1}">
                                    <option value="out_parter_gq_lx" selected="selected">立项参会委员</option>
                                    <option value="out_parter_gq_nh">内核参会委员</option>
                                </c:when>
                                <c:when test="${category == 2}">
                                    <option value="out_parter_zq_lx" selected="selected">立项参会委员</option>
                                    <option value="out_parter_zq_nh">内核参会委员</option>
                                </c:when>
                                <c:when test="${category == 3}">
                                    <option value="out_parter_gb_lx" selected="selected">立项参会委员</option>
                                    <option value="out_parter_gb_nh">内核参会委员</option>
                                </c:when>
                            </c:choose>
                        </select>
                    </td>
                </tr>
                <tr  name="outer_tr" style="width: 100%">
                    <td class="tta"  width="30%">  &nbsp;<font color="red">*</font>姓名：</td>
                    <td ><input id="fullName" type="text" name="outerAppUser.fullName" value="${outerAppUser.fullName}" /></td>
                </tr>
                <tr  name="outer_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;性别：</td>
                    <td >
                        <select name="outerAppUser.title">
                            <option value="1" selected="selected">先生</option>
                            <option value="0">女士</option>
                        </select>
                    </td>
                </tr>
                <tr name="outer_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;公司：</td>
                    <td ><input type="text" name="outerAppUser.company" id="company" value="${outerAppUser.company}" /></td>
                </tr>
                <tr  name="outer_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;办公电话：</td>
                    <td ><input type="text" name="outerAppUser.phone" value="${outerAppUser.phone}" /></td>
                </tr>
                <tr  name="outer_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;手机：</td>
                    <td ><input type="text" name="outerAppUser.mobile" value="${outerAppUser.mobile}" /></td>
                </tr>
                <tr name="outer_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;E-mail：</td>
                    <td ><input type="text" name="outerAppUser.email" value="${outerAppUser.email}" /></td>
                </tr>
                <tr  name="outer_tr" style="width: 100%">
                    <td class="tta" width="30%">  &nbsp;地址：</td>
                    <td ><input type="text" name="outerAppUser.address" value="${outerAppUser.address}" /></td>
                </tr>
            </table>
        </div>
    </div>
    <div class="operation">
        <a href="javascript:save();" class="but-sty2" id="saveBtn"> <span>提交</span><b class="br"></b></a>
        <a href="javascript:cancel();" class="but-sty2" id="cancelBtn"> <span>取消</span><b class="br"></b></a>
    </div>
</struts:form>
<jsp:include flush="true" page="/modules/comm/inc/page_footer.jsp"/>
</body>
</html>