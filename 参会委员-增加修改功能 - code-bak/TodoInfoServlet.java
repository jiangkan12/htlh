package com.lhzq.ibms.todoinfo.web;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.commons.logging.*;
import com.lhzq.bpm.api.process.ws.*;
import com.lhzq.bpm.api.process.ws.support.*;
import com.lhzq.ibms.commons.*;
import com.lhzq.ibms.commons.util.*;
import com.lhzq.ibms.processes.model.*;
import com.lhzq.ibms.processes.service.*;
import com.lhzq.ibms.project.model.*;
import com.lhzq.ibms.project.service.*;
import com.lhzq.ibms.steerdocprogress.model.*;
import com.lhzq.ibms.steerdocprogress.service.*;
import com.lhzq.ibms.todoinfo.dao.*;
import com.lhzq.ibms.todoinfo.model.*;
import com.lhzq.ibms.todoinfo.service.*;
import com.lhzq.iweb.entity.*;
import com.lhzq.iweb.service.*;
import com.lhzq.leap.core.config.*;
import com.lhzq.leap.core.service.support.*;
import com.lhzq.leap.core.utils.*;
import com.lhzq.leap.core.web.*;

/**
 * 门户网站获取任务信息
 * User: 葛方旭
 * Date: 12-11-28
 * Time: 下午10:12
 */
public class TodoInfoServlet extends HttpServlet {

    final private static Log log= LogFactory.getLog(TodoInfoServlet.class);
    final private String moreTaskUrl="/todoinfo/init.do";//更多待办rul

    /** Destruction of the servlet. <br> */
    public void destroy(){
        super.destroy();
    }
    /**
     * The doGet method of the servlet. <br>
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
    /**
     * The doPost method of the servlet. <br>
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String loginname=request.getParameter("loginname"); //登录工号
        LeapService leapService=(LeapService)SpringFactory.getBean("leapService");
        AppUser appUser = leapService.getAppUserByUserName( loginname );
        log.info( "门户网站访问，用户工号：" + loginname + ", 姓名：" + appUser.getFullName() );
        int num=0;//获取待办数量
        String _num=request.getParameter("num");
        if(StringUtils.isNotBlank(_num)){
            num=Integer.valueOf(_num);
        }
        //0=查看所有待办和待阅，1=查看待办，2=查看待阅
        int category=0;
        String _category=request.getParameter("category");
        if(StringUtils.isNotBlank(_category)){
            category=Integer.valueOf(_category);
        }
        String callback=request.getParameter("callback");//回调函数名
        String loginIndex=CommonConfig.getString("cas.index", null);//登录rul
        String moreTaskId=CommonUtil.getUUID();
        boolean status=false;
        ProjectService projectService=(ProjectService)SpringFactory.getBean("projectService");
        ToDoInfoService toDoInfoService=(ToDoInfoService)SpringFactory.getBean("toDoInfoService");
        BpmWebService bpmWebService=(BpmWebService)SpringFactory.getBean("bpmWebService");
        ProcessesService processesService=(ProcessesService)SpringFactory.getBean("processesService");
        TodoTempInfoDao todoTempInfoDao=(TodoTempInfoDao)SpringFactory.getBean("todoTempInfoDao");
        Long userId=0L;
        try{
            //流程待办数据
            List workFlowTaskList=new LinkedList();
            //周报、其他、待阅任务数据
            List todoList=new LinkedList();
            //存放返回待办数据
            List allTaskData=new LinkedList();
            //存放本地待办临时数据
            List todoTempInfoList=new ArrayList();
            //创建待办临时信息
            TodoTempInfo todoTempInfo=null;
            if(appUser!=null&&num>0){
                userId=appUser.getUserId();
                //存放返回待办信息
                Map taskInfo=null;
                //查看所有或查看待办
                if(category==0||category==1){
                    Map<String,String> processNameMap=projectService.findProjectProcessNameMapByCategory(0);
                    //根据人员id查询流程待办
                    List wsList=bpmWebService.getTaskInfo(userId,0,100).getWsTaskInfos();
                    int listSize=0;
                    if(wsList.size()>6){
                        listSize=6;
                    } else{
                        listSize=wsList.size();
                    }
                    for(int i=0;i<listSize;i++){
                        //存放返回待办信息
                        taskInfo=new LinkedHashMap();
                        StringBuffer title=new StringBuffer();
                        WsTaskInfo wsTaskInfo=(WsTaskInfo)wsList.get(i);
                        ProcessInfo processInfo=processesService.findProcessInfoByRunId(wsTaskInfo.getRunId());
                        if(processInfo!=null){
                            String taskId=CommonUtil.getUUID();
                            Project project=projectService.findProjectById(processInfo.getProjectId());
                            if(project != null) {
                                wsTaskInfo.setProjectId(String.valueOf(project.getId()));
                                title.append("请审批").append(CommonUtil.getPrjShortName(project)).append("项目的").append(processNameMap.get(processInfo.getProcessCode()));//如果项目简称为空则显示项目代号
                                taskInfo.put("title",title.toString());
                                taskInfo.put("createTime", DateUtil.formatDate(wsTaskInfo.getCreateTime(), "HH:mm:ss"));
                                taskInfo.put("url",loginIndex+"?taskId="+taskId);
                                taskInfo.put("createDate",DateUtil.formatDate(wsTaskInfo.getCreateTime()));
                                taskInfo.put("type", processNameMap.get(processInfo.getProcessCode()));
                                taskInfo.put("createrName",processInfo.getCreatorName());
                                workFlowTaskList.add(taskInfo);
                                //任务查看url
                                StringBuffer taskUrl=new StringBuffer();
                                if(project.getCategory()==1){
                                    taskUrl.append("/processes/approve.do?projectId=");
                                }else if(project.getCategory()==2){
                                    taskUrl.append("/processes/debtprocess/approve.do?projectId=");
                                }else if(project.getCategory()==3){
                                    taskUrl.append("/processes/mergerprocess/approve.do?projectId=");
                                }
                                taskUrl.append(project.getId()).append("&taskId=").append(wsTaskInfo.getTaskId()).append("&runId=").
                                        append(processInfo.getRunId()).append("&activityName=").append(EncodeUtils.urlEncode(wsTaskInfo.getActivityName()));
                                //本地待办任务临时数据
                                todoTempInfo=new TodoTempInfo();
                                todoTempInfo.setTaskId(taskId);
                                todoTempInfo.setUserId(userId.toString());
                                todoTempInfo.setTaskUrl(taskUrl.toString());
                                todoTempInfo.setTaskName(title.toString());
                                todoTempInfoList.add(todoTempInfo);
                            }
                        }
                    }
                }

                List<ToDoInfo> todoinfoList=new ArrayList<ToDoInfo>();
                //查询周报、其他、待阅任务数据
                List todoinfos=toDoInfoService.indexList(Integer.valueOf(userId.toString()));
                List<ToDoInfo> otherList = new ArrayList<ToDoInfo>();
                SteerdocDefinitionService steerdocDefinitionService = (SteerdocDefinitionService) SpringFactory.getBean("steerdocDefinitionService");
                Calendar cal = Calendar.getInstance();
                Integer calYear=cal.get(Calendar.YEAR);
                Integer calMonth=cal.get(Calendar.MONTH);
                Integer objMonthNumber=Integer.valueOf("" + calYear + (calMonth + 1));
                List steerdocDefinitions = steerdocDefinitionService.getObjByMonthNumber(objMonthNumber+"",userId.toString(),"0");
                //otherList
                if(steerdocDefinitions!=null&&steerdocDefinitions.size()>0){
                    SteerdocDefinition sd=null;
                    WsTaskInfo wti=null;
                    for(int i=0;i<steerdocDefinitions.size();i++){
                        sd=(SteerdocDefinition)steerdocDefinitions.get(0);
                        wti = new WsTaskInfo();
                        wti.setTaskId(Long.valueOf(sd.getId()));
                        wti.setActivityName(sd.getMonthNumberAlias() + "(" + DateUtil.formatDate(sd.getStartdate()) + "~" + DateUtil.formatDate(sd.getEnddate()) + ")" + "持续督导填报");
                        wti.setTaskName("/steerdocProgresstodo/initShow.do?steerdocdefinitionId="+sd.getId());
                        wti.setProjectName(sd.getMonthNumberAlias() + "持续督导填报");
                        wti.setCreateTime(sd.getCreateTime());
                        wti.setTaskType("其它");
                        ToDoInfo td=new ToDoInfo();
                        td.setContent(wti.getProjectName());
                        td.setCreateTimestamp(wti.getCreateTime());
                        td.setUrl(wti.getTaskName());
                        td.setType(-1);
                        otherList.add(td);
                    }
                }

                SteerdocProgressService steerdocProgressService = (SteerdocProgressService) SpringFactory.getBean("steerdocProgressService");
                List<SteerdocProgress> spList=steerdocProgressService.selecResInfotByUserId(userId.toString(), String.valueOf(objMonthNumber), "1");
                if(spList!=null&&spList.size()>0){
                    SteerdocProgress sp=null;
                    WsTaskInfo wti=null;
                    wti = new WsTaskInfo();
                    sp=spList.get(0);
                    String monthNumber = String.valueOf(sp.getMonthNumber());
                    if(monthNumber!=null&&!"".equals(monthNumber)){

                    }
                    wti.setTaskId(Long.valueOf(monthNumber));
                    String year=monthNumber.substring(0, 4);
                    String month = monthNumber.replace(year, "");
                    wti.setActivityName(year+"年第" + month+"期("+year+"-"+((month.length()==1)?("0"+month):month)+"-01)持续督导填报汇总");
                    wti.setProjectName(year + "年第" + month + "期(" + year + "-" + ((month.length() == 1) ? ("0" + month) : month) + "-01)持续督导填报汇总");
                    wti.setTaskName("/steerdocProgresstodo/initShowCondition.do?MONTH_NUMBER="+monthNumber);
                    wti.setCreateTime(sp.getCreatetime());
                    wti.setTaskType("其它");
                    ToDoInfo td=new ToDoInfo();
                    td.setContent(wti.getProjectName());
                    td.setCreateTimestamp(wti.getCreateTime());
                    td.setUrl(wti.getTaskName());
                    td.setType(-1);
                    otherList.add(td);

                }
                todoinfos.addAll(otherList);
//                int todoinfosSize=5-workFlowTaskList.size();
//                if(todoinfosSize>todoinfos.size()){
//                    todoinfosSize=todoinfos.size();
//                }
                //周报、其他、待阅任务数据
                List _weeklyTodoList = new ArrayList();
                List _otherTodoList = new ArrayList();
                List _readerTodoList=new ArrayList();
                ToDoInfo tdi = null;
//                for(int i= 0;i<todoinfos.size();i++){
//                    tdi=(ToDoInfo)todoinfos.get(i);
//                    //查看所有或查看待办
//                    if(category==0||category==1){
//                        if(tdi.getType()>=2&&tdi.getType()<=11){
//                            tdi.setTypeName("周报");
//                            _weeklyTodoList.add(tdi);
//                        }
//                    }
//                    //查看所有或查看待阅
//                    if(category==0||category==2){
//                        if(tdi.getType()==1||tdi.getType()==20||tdi.getType()==21){
//                            tdi.setTypeName("待阅");
//                            _otherTodoList.add(tdi);
//                        }
//                    }
//                    //查看所有或查看待办
//                    if(category==0||category==1){
//                        if(tdi.getType()==-1){
//                            tdi.setTypeName("其它");
//                            _readerTodoList.add(tdi);
//                        }
//                    }
//                }

                for(int i= 0;i<todoinfos.size();i++){
                    tdi=(ToDoInfo)todoinfos.get(i);
                    switch (tdi.getType()){
                        case GlobsAttributes.PROJECT_TO_DO://项目待办
                        case GlobsAttributes.PROJECT_PROCESS_READ_TO_DO://项目流程阅读
                        case GlobsAttributes.PROJECT_PROCESS_SUSPENSION_TO_DO://项目流程阅读暂停
                        case GlobsAttributes.OTHER_PROCESS_READ_TO_DO://非项目类流程阅读
                            //查看所有或查看待阅
                            if(category==0||category==2){
                                tdi.setTypeName("待阅");
                                _readerTodoList.add(tdi);
                            }
                            break;
                        case GlobsAttributes.PERSONAL_WEEKLY_TO_DO://个人周报待办
                        case GlobsAttributes.PROJECT_WEEKLY_TO_DO://项目负责人人周报待办
                        case GlobsAttributes.DUTY_WEEKLY_TO_DO://部门负责人周报待办
                        case GlobsAttributes.MANAGEMENT_WEEKLY_TO_DO://支持部门负责人周报待办
                        case GlobsAttributes.AUDIT_PROJECT_WEEKLY_TO_DO://审核部项目负责人周报待办
                        case GlobsAttributes.AUDIT_DUTY_WEEKLY_TO_DO: //审核部部门负责人周报待办
                        case GlobsAttributes.MARKET_PROJECT_WEEKLY_TO_DO: //市场部项目负责人周报待办
                        case GlobsAttributes.MARKET_DUTY_WEEKLY_TO_DO: //市场部部门负责人周报待办
                        case GlobsAttributes.MERGER_ACQUISITIONS_WEEKLY_TO_DO://购并私募融资总部部门负责人周报待办
                        case GlobsAttributes.PROJECT_AUDIT_TRAIL_TO_DO://填写项目审核跟踪表的人员提醒
                            //查看所有或查看待办
                            if(category==0||category==1){
                                tdi.setTypeName("周报");
                                _weeklyTodoList.add(tdi);
                            }
                            break;
                        default:
                            //查看所有或查看待办
                            if(category==0||category==1){
                                tdi.setTypeName("其它");
                                _otherTodoList.add(tdi);//持续督导填报
                            }
                            break;
                    }
                }

                todoinfoList.addAll(_weeklyTodoList);
                todoinfoList.addAll(_otherTodoList);
                todoinfoList.addAll(_readerTodoList);
                //返回待办数量,如果待办总条数大于请求条数，只取num条
                int todoinfoSize=(workFlowTaskList.size()+todoinfoList.size())>num?(num-workFlowTaskList.size()):todoinfoList.size();
                for(int i=0;i<todoinfoSize;i++){
                    String taskId=CommonUtil.getUUID();
                    ToDoInfo toDoInfo=todoinfoList.get(i);
                    //创建本地待办任务临时数据
                    todoTempInfo=new TodoTempInfo();
                    todoTempInfo.setTaskId(taskId);
                    todoTempInfo.setUserId(userId.toString());
                    todoTempInfo.setTaskUrl(toDoInfo.getUrl());
                    todoTempInfo.setTaskName(toDoInfo.getContent());
                    todoTempInfoList.add(todoTempInfo);
                    taskInfo=new LinkedHashMap();
                    taskInfo.put("title",toDoInfo.getContent());
                    taskInfo.put("createTime",DateUtil.formatDate(toDoInfo.getCreateTimestamp(), "HH:mm:ss"));
                    taskInfo.put("url",loginIndex+"?taskId="+taskId);
                    taskInfo.put("createDate",DateUtil.formatDate(toDoInfo.getCreateTimestamp()));
                    taskInfo.put("type",toDoInfo.getTypeName());
                    taskInfo.put("createrName", GlobsAttributes.SYSTEM_OP_NAME);
                    todoList.add(taskInfo);
                }
                //流程待办
                allTaskData.addAll(workFlowTaskList);
                //周报、其他、待阅
                allTaskData.addAll(todoList);
                status=true;
            }
            Map _param=new LinkedHashMap();
            //数据为空或异常时返回false
            if(allTaskData.size()==0||!status){
                _param.put("success","false");
                _param.put("num","0");
            }else{
                _param.put("success","true");
                _param.put("num",String.valueOf(allTaskData.size()));
            }
            _param.put("moreurl",loginIndex+"?taskId="+moreTaskId);
            _param.put("dataList",allTaskData);
            //待办数据条数大于0同时无异常设置值
            if(allTaskData.size()>0&&status){
                _param.put("userCode",appUser.getUserName());
                _param.put("userName",appUser.getFullName());
            }
            //创建本地待办任务临时数据
            todoTempInfo=new TodoTempInfo();
            todoTempInfo.setTaskId(moreTaskId);
            todoTempInfo.setUserId(userId.toString());
            todoTempInfo.setTaskUrl(this.moreTaskUrl);
            todoTempInfo.setTaskName("查看更多");
            todoTempInfoList.add(todoTempInfo);
            //删除两天前的记录
            todoTempInfoDao.deleteTodoTempInfoByUserId(userId.toString(),2);
            //批量创建待办临时数据
            if(todoTempInfoList.size()>0){
                todoTempInfoDao.batchCreateTodoTempInfo(todoTempInfoList);
            }
            String jsonStr=Struts2Utils.getJson(_param);
            StringBuffer returnStr=new StringBuffer();
            returnStr.append(callback).append("(").append(jsonStr).append(")");
            log.info("返回数据："+EncodeUtils.urlDecode(jsonStr));
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(returnStr.toString());
        }catch (Exception e){
            try{
                //异常时返回数据
                Map _param=new LinkedHashMap();
                _param.put("success","false");
                _param.put("num","0");
                _param.put("moreurl",loginIndex+"?taskId="+moreTaskId);
                _param.put("dataList",new ArrayList());
                //创建本地待办任务临时数据
                TodoTempInfo _todoTempInfo=new TodoTempInfo();
                _todoTempInfo.setTaskId(moreTaskId);
                _todoTempInfo.setUserId(userId.toString());
                _todoTempInfo.setTaskUrl(this.moreTaskUrl);
                _todoTempInfo.setTaskName("查看更多");
                //新增记录
                todoTempInfoDao.createTodoTempInfo(_todoTempInfo);
                String jsonStr=Struts2Utils.getJson(_param);
                StringBuffer returnStr=new StringBuffer();
                returnStr.append(callback).append("(").append(jsonStr).append(")");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(returnStr.toString());
            }catch (Exception e2){
                log.error("转换数据出错！", e2);
            }
            log.error("获取任务箱数据出错！", e);
        }
    }
    /**
     * Initialization of the servlet. <br>
     * @throws javax.servlet.ServletException if an error occure
     */
    public void init() throws ServletException{
    }

}
