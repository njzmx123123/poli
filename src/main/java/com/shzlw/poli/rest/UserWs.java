package com.shzlw.poli.rest;

import com.shzlw.poli.constant.UserConstant;
import com.shzlw.poli.dao.CannedReportDao;
import com.shzlw.poli.dao.SharedReportDao;
import com.shzlw.poli.dao.UserDao;
import com.shzlw.poli.dao.UserFavouriteDao;
import com.shzlw.poli.dto.BaseResponse;
import com.shzlw.poli.model.User;
import com.shzlw.poli.model.UserAttribute;
import com.shzlw.poli.service.SharedReportService;
import com.shzlw.poli.service.UserService;
import com.shzlw.poli.service.ZhizhiUserSSOService;
import com.shzlw.poli.util.Constants;
import com.zhizhi.uc.sdk.result.SSOReulstExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ws/user")
public class UserWs {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserWs.class);

    @Autowired
    UserDao userDao;

    @Autowired
    UserService userService;

    @Autowired
    CannedReportDao cannedReportDao;

    @Autowired
    UserFavouriteDao userFavouriteDao;

    @Autowired
    SharedReportDao sharedReportDao;

    @Autowired
    SharedReportService sharedReportService;

    @Autowired
    ZhizhiUserSSOService ssoService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<User> findAll(HttpServletRequest request) {
        User myUser = (User) request.getAttribute(Constants.HTTP_REQUEST_ATTR_USER);
        List<User> users = new ArrayList<>();
        if (Constants.SYS_ROLE_ADMIN.equals(myUser.getSysRole())) {
            users = userDao.findNonAdminUsers(myUser.getId());
        } else if (Constants.SYS_ROLE_DEVELOPER.equals(myUser.getSysRole())) {
            users = userDao.findViewerUsers(myUser.getId());
        }
        return users;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public User one(@PathVariable("id") long userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            return null;
        }
        List<Long> userGroups = userDao.findUserGroups(userId);
        user.setUserGroups(userGroups);
        List<UserAttribute> userAttributes = userDao.findUserAttributes(userId);
        user.setUserAttributes(userAttributes);
        return user;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public BaseResponse add(@RequestBody User user,
                                    HttpServletRequest request) {
        //判断新增加的用户类型
        final Integer userType = user.getType();

        User myUser = (User) request.getAttribute(Constants.HTTP_REQUEST_ATTR_USER);
        if (!isDeveloperOperationValid(myUser.getSysRole(), user)) {
            return BaseResponse.refuse("操作者无新增用户的权限！");
        }

        //去UC查询用户信息
        if(UserConstant.ZHIZHI_USER_CENTER_ACCOUNT_TYPE.equals(userType)) {
            SSOReulstExt ssoReulstExt = ssoService.getUserByAccount("ZZ_REPORT_"+System.currentTimeMillis(),user.getUsername());
            if(null == ssoReulstExt) {
                return BaseResponse.bizError("无法根据用户名 ->"+user.getUsername()+"<- 找到B2B用户，请检查用户名!");
            }
        }

        long userId = userDao.insertUser(user.getUsername(), user.getName(), user.getTempPassword(), user.getSysRole(), user.getType());
        userDao.insertUserGroups(userId, user.getUserGroups());
        userDao.insertUserAttributes(userId, user.getUserAttributes());
        return BaseResponse.success("创建账号成功！");
    }

    @RequestMapping(method = RequestMethod.PUT)
    @Transactional
    public ResponseEntity<?> update(@RequestBody User user,
                                    HttpServletRequest request) {
        User myUser = (User) request.getAttribute(Constants.HTTP_REQUEST_ATTR_USER);
        if (!isDeveloperOperationValid(myUser.getSysRole(), user)) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        long userId = user.getId();
        User savedUser = userDao.findById(userId);
        userService.invalidateSessionUserCache(savedUser.getSessionKey());

        userDao.updateUser(user);
        userDao.deleteUserGroups(userId);
        userDao.insertUserGroups(userId, user.getUserGroups());
        userDao.deleteUserAttributes(userId);
        userDao.insertUserAttributes(userId, user.getUserAttributes());
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity<?> delete(@PathVariable("id") long userId,
                                    HttpServletRequest request) {
        User myUser = (User) request.getAttribute(Constants.HTTP_REQUEST_ATTR_USER);
        User savedUser = userDao.findById(userId);
        if (!isDeveloperOperationValid(myUser.getSysRole(), savedUser)) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        userService.invalidateSessionUserCache(savedUser.getSessionKey());
        sharedReportService.invalidateSharedLinkInfoCacheByUserId(userId);
        sharedReportDao.deleteByUserId(userId);
        userFavouriteDao.deleteByUserId(userId);
        cannedReportDao.deleteByUserId(userId);
        userDao.deleteUserAttributes(userId);
        userDao.deleteUserGroups(userId);
        userDao.deleteUser(userId);
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(
            value = "/account",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public User findAccountBySessionKey(HttpServletRequest request) {
        User myUser = (User) request.getAttribute(Constants.HTTP_REQUEST_ATTR_USER);
        User myAccount = userDao.findAccount(myUser.getId());
        return myAccount;
    }

    @RequestMapping(
            value = "/account",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void updateUserBySessionKey(@RequestBody User user,
                                       HttpServletRequest request) {
        User myUser = (User) request.getAttribute(Constants.HTTP_REQUEST_ATTR_USER);
        userService.invalidateSessionUserCache(myUser.getSessionKey());
        userDao.updateUserAccount(myUser.getId(), user.getName(), user.getPassword());
    }

    private boolean isDeveloperOperationValid(String mySysRole, User targetUser) {
        if (Constants.SYS_ROLE_DEVELOPER.equals(mySysRole)
                && !Constants.SYS_ROLE_VIEWER.equals(targetUser.getSysRole())) {
            return false;
        }
        return true;
    }
}