package com.shzlw.poli.rest;

import com.shzlw.poli.constant.UserConstant;
import com.shzlw.poli.dao.UserDao;
import com.shzlw.poli.dto.LoginResponse;
import com.shzlw.poli.model.SharedReport;
import com.shzlw.poli.model.User;
import com.shzlw.poli.service.UserService;
import com.shzlw.poli.service.ZhizhiUserSSOService;
import com.shzlw.poli.util.Constants;
import com.shzlw.poli.util.PasswordUtil;
import com.zhizhi.uc.sdk.result.SSOReulstExt;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthWs {

    @Autowired
    UserDao userDao;

    @Autowired
    UserService userService;

    @Autowired
    private ZhizhiUserSSOService ssoService;

    public static final String USER_ACCOUNT_ERROR = "User account error!";
    public static final String INVALID_USERNAME_PASSWORD = "Invalid username or password.";
    public static final String USE_MORE_CHARACTERS = "Use 8 or more characters.";
    public static final String INVALID_API_KEY = "Invalid api key.";
    public static final String INVALID_SHARE_KEY = "Invalid share key.";

    @RequestMapping(value="/login/user", method = RequestMethod.POST)
    @Transactional
    public LoginResponse loginByUser(@RequestBody User loginInfo, HttpServletResponse response) {
        final String username = loginInfo.getUsername();
        final String password = loginInfo.getPassword();

        //先请求本地库在查询uc库
        List<User> users = authLocal(username);
        if(null == users) {
            return LoginResponse.ofError(INVALID_USERNAME_PASSWORD);
        }

        //根据用户信息对密码进行校验
        if(users.size()>1) {
            return LoginResponse.ofError(USER_ACCOUNT_ERROR);
        }

        final User user = users.get(0);
        final String newSessionKey = getNewSessionKey();

        //校验密码
        if(UserConstant.LOCAL_USER_ACCOUNT_TYPE.equals(user.getType())) {
            if(!user.getPassword().equals(password)) {
                return LoginResponse.ofError(INVALID_USERNAME_PASSWORD);
            }
        }else if(UserConstant.ZHIZHI_USER_CENTER_ACCOUNT_TYPE.equals(user.getType())) {
            SSOReulstExt ssoReulstExt = ssoService.login("TI_"+newSessionKey,username,password,newSessionKey);
            if(null == ssoReulstExt) {
                return LoginResponse.ofError(INVALID_USERNAME_PASSWORD);
            }
        }

        String oldSessionKey = user.getSessionKey();
        userDao.updateSessionKey(user.getId(), newSessionKey);
        user.setUserAttributes(userDao.findUserAttributes(user.getId()));
        userService.newOrUpdateUser(user, oldSessionKey, newSessionKey);

        Cookie sessionKeyCookie = new Cookie(Constants.SESSION_KEY, newSessionKey);
        sessionKeyCookie.setMaxAge(Constants.COOKIE_TIMEOUT);
        sessionKeyCookie.setPath("/");
        response.addCookie(sessionKeyCookie);

        return LoginResponse.ofSuccess(user.getUsername(), user.getSysRole(), false);
    }

    @NotNull
    private List<User> authLocal(String username) {
        return userDao.findByUsername(username);
    }

    @RequestMapping(value="/login/cookie", method= RequestMethod.POST)
    @Transactional
    public LoginResponse loginBySessionKey(@CookieValue(value = Constants.SESSION_KEY, defaultValue = "") String sessionKey) {
        if (sessionKey.isEmpty()) {
            return LoginResponse.ofError(INVALID_USERNAME_PASSWORD);
        }

        User user = userDao.findBySessionKey(sessionKey);
        if (user == null) {
            return LoginResponse.ofError(INVALID_USERNAME_PASSWORD);
        }

        user.setUserAttributes(userDao.findUserAttributes(user.getId()));
        userService.newOrUpdateUser(user, user.getSessionKey(), sessionKey);
        return LoginResponse.ofSuccess(user.getUsername(), user.getSysRole(), false);
    }

    @RequestMapping(value="/login/apikey", method= RequestMethod.POST)
    @Transactional
    public LoginResponse loginByApiKey(@RequestBody User loginInfo) {
        String apiKey = loginInfo.getApiKey();
        User user = userDao.findByApiKey(apiKey);
        if (user == null) {
            return LoginResponse.ofError(INVALID_API_KEY);
        }

        return LoginResponse.ofSuccess(user.getUsername(), user.getSysRole(), false);
    }

    @RequestMapping(value="/login/sharekey", method= RequestMethod.POST)
    @Transactional
    public LoginResponse loginByShareKey(@RequestBody SharedReport sharedReport) {
        String shareKey = sharedReport.getShareKey();
        User user = userDao.findByShareKey(shareKey);
        if (user == null) {
            return LoginResponse.ofError(INVALID_SHARE_KEY);
        }

        return LoginResponse.ofSuccess(user.getUsername(), user.getSysRole(), false);
    }

    @RequestMapping(value="/logout", method= RequestMethod.GET)
    @Transactional
    public void logout(@CookieValue(Constants.SESSION_KEY) String sessionKey, HttpServletResponse response) throws IOException {
        User user = userDao.findBySessionKey(sessionKey);
        if (user != null) {
            userService.invalidateSessionUserCache(sessionKey);
            userDao.updateSessionKey(user.getId(), null);

            Cookie sessionKeyCookie = new Cookie(Constants.SESSION_KEY, "");
            sessionKeyCookie.setMaxAge(0);
            sessionKeyCookie.setPath("/");
            response.addCookie(sessionKeyCookie);
        }
    }

    @RequestMapping(value="/login/change-password", method= RequestMethod.POST)
    @Transactional
    public ResponseEntity<LoginResponse> changeTempPassword(@CookieValue(value = Constants.SESSION_KEY, defaultValue = "") String sessionKey,
        @RequestBody User user) {
        String password = user.getPassword();
        if (password.length() < 8) {
            return new ResponseEntity<>(LoginResponse.ofError(USE_MORE_CHARACTERS), HttpStatus.OK);
        }

        User existUser = userDao.findBySessionKey(sessionKey);
        if (existUser == null) {
            return new ResponseEntity<>(LoginResponse.ofError("Invalid session."), HttpStatus.OK);
        }

        userDao.updateTempPassword(existUser.getId(), user.getPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value="/generate-apikey", method= RequestMethod.GET)
    @Transactional
    public ResponseEntity<String> generateApiKey(@CookieValue(value = Constants.SESSION_KEY, defaultValue = "") String sessionKey) {
        User user = userDao.findAccountBySessionKey(sessionKey);
        userService.invalidateApiKeyUserCache(user.getApiKey());
        String apiKey = Constants.API_KEY_PREFIX + PasswordUtil.getUniqueId();
        userDao.updateApiKey(user.getId(), apiKey);
        return new ResponseEntity<>(apiKey, HttpStatus.OK);
    }

    private static String getNewSessionKey() {
        return Constants.SESSION_KEY_PREFIX + PasswordUtil.getUniqueId();
    }
}
