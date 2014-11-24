package com.lhzq.ibms.meetingpaticipate.dao;

import org.apache.commons.lang.xwork.*;
import org.springframework.stereotype.*;
import com.lhzq.ibms.commons.dao.*;
import com.lhzq.iweb.entity.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-11-29
 * Time: 下午2:21
 */
@Repository
public class MeetingPaticipateDao extends EntityDao {

    /**
     * 新增外部委员
     */
    public void addOuterPaticipate(AppUser appUser) {
        String insertSql="insert into qx.leap_user(uuid,email,phone,mobile,address,state,title,real_name,source_type, company,modify_user,modify_datetime) values (qx.S_LEAP_USER.nextval, ?,?,?,?,?,?,?,?,?,?,SYSDATE)";
        jdbcTemplate.update(insertSql, new Object[]{appUser.getEmail(),appUser.getPhone(),appUser.getMobile(), appUser.getAddress(),appUser.getDelFlag(), appUser.getTitle(),
                        appUser.getFullName(), appUser.getSourceType(), appUser.getCompany(), appUser.getModifyUser()});
    }

    /**
     * 检测外部委员是否已经存在
     */
    public int validateOuterPaticipate(String fullName, String sourceType, String company) {
        String updatesql="select count(1) num from qx.leap_user lu where 1=1 and lu.state=1 and lu.real_name=? and lu.source_type=? ";
        if(StringUtils.isNotBlank(company)) {
            updatesql = updatesql+" and lu.company=?";
            return jdbcTemplate.queryForInt(updatesql, fullName, sourceType, company);
        } else {
            return jdbcTemplate.queryForInt(updatesql, fullName, sourceType);
        }
    }


    public void updateAppUserSourceType(Long uuid, String updateSoureType, String modifyUser) {
        String updatesql="update qx.leap_user lu set lu.source_type=?,lu.modify_user=?,lu.modify_datetime=SYSDATE where lu.uuid=?";
        jdbcTemplate.update(updatesql,new Object[]{updateSoureType, modifyUser, uuid});
    }


    public void deleteAppUser(Long uuid, String modifyUser) {
        String updatesql="update qx.leap_user lu set lu.state=0,lu.modify_user=?,lu.modify_datetime=SYSDATE where lu.uuid=?";
        jdbcTemplate.update(updatesql,new Object[]{modifyUser, uuid});
    }

}
