package com.shzlw.poli.service;

import com.alibaba.fastjson.JSONObject;
import com.shzlw.poli.AppProperties;
import com.shzlw.poli.support.GlobalSysContents;
import com.zhizhi.common.utils.StringUtils;
import com.zhizhi.uc.sdk.config.OperationTypeContant;
import com.zhizhi.uc.sdk.model.SSOClientLogin;
import com.zhizhi.uc.sdk.model.SSOClientUserAccount;
import com.zhizhi.uc.sdk.model.SSOClientUserBaseinfo;
import com.zhizhi.uc.sdk.model.SSOClientUserForId;
import com.zhizhi.uc.sdk.result.SSOResult;
import com.zhizhi.uc.sdk.result.SSOResultUserInfo;
import com.zhizhi.uc.sdk.result.SSOReulstExt;
import com.zhizhi.uc.sdk.spi.SSOClietProvider;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * UserSSOService.java sso桥接服务
 *
 * @author hui.ye@guoran100.com
 **/
@Slf4j
@Service
public class ZhizhiUserSSOService {

	private AppProperties appProperties;

	private static final Byte CHANNEL = 1;

	public ZhizhiUserSSOService(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	/**
	 * 登录
	 *
	 * @param tracedId
	 *           跟踪ID
	 * @param account
	 *           账户
	 * @param pwd
	 *           密码
	 */
	public SSOReulstExt login(String tracedId, String account, String pwd, String sessionId) {

		String ssoUrl = appProperties.getSsoUrl();
		String clientCode = appProperties.getSsoClientCode();
		String clientSecret = appProperties.getSsoClientSecret();

		SSOClientLogin login = SSOClientLogin.getInstanceSSOClientLogin(clientCode, clientSecret, CHANNEL,
		      OperationTypeContant.LOGIN, tracedId, account, pwd, sessionId);

		return getSSOResultMsg(SSOClietProvider.login(StringUtils.append(ssoUrl, login.getUrl()), login));
	}

	/**
	 * 根据账户查询用户信息
	 *
	 * @param tracedId
	 * @param account
	 *           账户信息
	 * @return
	 */
	public SSOReulstExt getUserByAccount(String tracedId, String account) {

		String ssoUrl = appProperties.getSsoUrl();
		String clientCode = appProperties.getSsoClientCode();
		String clientSecret = appProperties.getSsoClientSecret();

		SSOClientUserAccount userAccount = SSOClientUserAccount.getInstance(clientCode, clientSecret, CHANNEL,
		      OperationTypeContant.GET_INFO, tracedId, account);

		return getSSOResultMsg(
		      SSOClietProvider.getUserByAccount(StringUtils.append(ssoUrl, userAccount.getUrl()), userAccount));
	}

	/**
	 * 根据账户ID查询用户信息
	 *
	 * @param tracedId
	 * @param userId
	 *           用户中心ID
	 * @return
	 */
	public SSOReulstExt getUserByUserId(String tracedId, String userId) {
		String ssoUrl = appProperties.getSsoUrl();
		String clientCode = appProperties.getSsoClientCode();
		String clientSecret = appProperties.getSsoClientSecret();

		SSOClientUserForId userAccount = SSOClientUserForId
			.getInstance(clientCode, clientSecret, CHANNEL, OperationTypeContant.GET_INFO, tracedId, userId);

		return getSSOResultMsg(
			SSOClietProvider.getUserByUserId(StringUtils.append(ssoUrl, userAccount.getUrl()), userAccount));
	}

	/**
	 * 获取用户基本信息
	 *
	 * @param tracedId
	 * @param userId
	 * @param ticket
	 * @return
	 */
	public SSOResultUserInfo getUserInfo(String tracedId, String userId, String ticket, String sessionId) {

		String ssoUrl = appProperties.getSsoUrl();
		String clientCode = appProperties.getSsoClientCode();
		String clientSecret = appProperties.getSsoClientSecret();

		SSOClientUserBaseinfo baseinfo = SSOClientUserBaseinfo.getInstance(clientCode, clientSecret, CHANNEL,
		      OperationTypeContant.GET_INFO, tracedId, userId, ticket, sessionId);

		return getSSOResultUserInfoMsg(
		      SSOClietProvider.getUserInfo(StringUtils.append(ssoUrl, baseinfo.getUrl()), baseinfo));
	}

	/**
	 * SSO 返回处理
	 *
	 * @param str
	 * @return
	 */
	private SSOReulstExt getSSOResultMsg(String str) {

		Object data = getSsoResultData(str);
		if (data == null) {
			return null;
		}

		return JSONObject.parseObject(data.toString(), SSOReulstExt.class);
	}

	/**
	 * SSO 返回处理
	 *
	 * @param str
	 *
	 * @return
	 */
	private SSOResultUserInfo getSSOResultUserInfoMsg(String str) {

		Object data = getSsoResultData(str);

		if (data == null) {
			return null;
		}

		return JSONObject.parseObject(data.toString(), SSOResultUserInfo.class);
	}

	@Nullable
	private Object getSsoResultData(String str) {
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		SSOResult viewResult = JSONObject.parseObject(str, SSOResult.class);

		if (!viewResult.getCode().equals(GlobalSysContents.SysContent.SUCCESS_CODE.getValue())) {

			log.error("sso service error code:{}, msg:{}", viewResult.getCode(), viewResult.getMessage());

			return null;
		}

		return viewResult.getData();
	}
}
