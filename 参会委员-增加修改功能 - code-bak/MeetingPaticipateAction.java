package com.lhzq.ibms.meetingpaticipate.web;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.xwork.*;
import org.springframework.beans.factory.annotation.*;
import com.lhzq.ibms.commons.web.*;
import com.lhzq.ibms.meetingpaticipate.service.*;
import com.lhzq.ibms.org.util.*;
import com.lhzq.ibms.sys.mode.*;
import com.lhzq.iweb.entity.*;
import com.lhzq.iweb.service.*;
import com.lhzq.leap.core.web.*;

/**
 * Time: 下午1:36
 * 参会委员管理
 */
public class MeetingPaticipateAction extends DispatchAction {
    @Autowired
    private LeapService leapService;
    @Autowired
    private MeetingPaticipateService meetingPaticipateService;

    private Integer id;
    private Integer category;
    private String flag;
    private String type;
    private AppUser appUser;
    private AppUser innerAppUser;
    private AppUser outerAppUser;
    private String message;

    /**
     * 显示参会委员管理页
     * @return
     */
    public String show() {
        return SUCCESS;
    }

    /**
     * 显示新增/修改
     */
    public String showAdd() {
        if(category != null) {
            if(category == 1) {
                Struts2Utils.setAttribute("categoryName","股权");
            } else if(category == 2) {
                Struts2Utils.setAttribute("categoryName","债权");
            } else if(category == 3) {
                Struts2Utils.setAttribute("categoryName","并购");
            }
        }
        if ( StringUtils.isNotBlank( flag ) ) {
            Struts2Utils.setAttribute( "editFlag", "0".equals( flag ) ? "新增" : "修改" );
            if ( "1".equals( flag ) ) {//修改操作，load数据
                // 显示数据
                if ( null != id ) {
                    if ( StringUtils.isNotBlank( type ) ) {
                        if ( type.contains( "out" ) ) {
                            outerAppUser = leapService.getAppUserByUserId( Long.valueOf( id ) );
                        }
                        else if ( type.contains( "inner" ) ) {
                            innerAppUser = leapService.getAppUserByUserId( Long.valueOf( id ) );
                        }
                    }
                }
            }
        }
        return SUCCESS;
    }


    /**
     * 新增外部参会委员
     * @return
     */
    public String addOuterMeetingParticipate() {
        User currentUser = UserContextUtil.getCurrentUser();
        message = "保存成功";
        if(outerAppUser != null) {
            try {
                outerAppUser.setModifyUser(currentUser.getRealname());
                outerAppUser.setModifyDatetime(new Date());
                outerAppUser.setDelFlag(new Short("1")); //离职状态
                meetingPaticipateService.addOuterPaticipate(outerAppUser);
            }catch ( Exception e) {
                e.printStackTrace();
                message = "保存失败";
            }
        }
        return SUCCESS;
    }

    /**
     * 新增内部参会委员
     * @return
     */
    public String addInnerMeetingParticipate() {
        User currentUser = UserContextUtil.getCurrentUser();
        message = "保存成功";
        if(innerAppUser != null && StringUtils.isNotBlank(innerAppUser.getSourceType())) {
            AppUser user = leapService.getAppUserByUserIdIgnoreState(innerAppUser.getUserId());
            try {
                if(StringUtils.isBlank(user.getSourceType()) ) {
                    meetingPaticipateService.updateAppUserSourceType(innerAppUser.getUserId(), innerAppUser.getSourceType(), currentUser.getRealname());
                } else {
                    if(user.getSourceType().indexOf(innerAppUser.getSourceType()) < 0) {
                        String newSourceType = user.getSourceType()+","+innerAppUser.getSourceType();
                        meetingPaticipateService.updateAppUserSourceType(innerAppUser.getUserId(), newSourceType, currentUser.getRealname());
                    }
                }
            } catch ( Exception e) {
                e.printStackTrace();
                message = "保存失败";
            }
        }
        return SUCCESS;
    }

    /**
     * 检查外部参会委员是否已经存在
     * 根据姓名，类型，公司 检测
     * @return
     */
    public void validateOuterExist() {
        int result = 0;
        if(appUser != null && StringUtils.isNotBlank(appUser.getFullName()) && StringUtils.isNotBlank(appUser.getSourceType())) {
            int exist = meetingPaticipateService.validateOuterPaticipate(appUser.getFullName(), appUser.getSourceType(), appUser.getCompany());
            if(exist > 0) {
                result = 1;
            }
        }
        try{
            Struts2Utils.getResponse().getWriter().print(result);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 检查内部参会委员是否已经存在
     * 根据uuid 和 类型检测
     * @return
     */
    public void validateInnerExist() {
        int result = 0;
        if(appUser != null && appUser.getUserId() != null && StringUtils.isNotBlank(appUser.getSourceType())) {
            AppUser user = leapService.getAppUserByUserIdIgnoreState(appUser.getUserId());
            if(user.getSourceType().indexOf(appUser.getSourceType()) >= 0) {
                result = 1;
            }
        }
        try{
            Struts2Utils.getResponse().getWriter().print(result);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId( Integer id ) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public AppUser getInnerAppUser() {
        return innerAppUser;
    }

    public void setInnerAppUser(AppUser innerAppUser) {
        this.innerAppUser = innerAppUser;
    }

    public AppUser getOuterAppUser() {
        return outerAppUser;
    }

    public void setOuterAppUser(AppUser outerAppUser) {
        this.outerAppUser = outerAppUser;
    }
}
