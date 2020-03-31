package com.shzlw.poli.support;


import com.zhizhi.common.content.ContentEnums;

/**
 * GlobalSysContents.java
 *
 * @author hui.ye@guoran100.com
 **/
public class GlobalSysContents {

	/**
	 * 系统
	 */
	public enum SysContent implements ContentEnums {

		SUCCESS_CODE("success", 0),

		ERROR_CODE("error", -1),

		SESSION_INVALIDATION("登录信息失效,请重新登录", 401),

		REQUSET_HANDLING("请求处理中，请稍后再试", 403),

		DEV_WEB("web", 1),
		DEV_APP("app", 2),
		DEV_DZC("dzc", 3),
		DEV_WX_MINI("wx_mini", 4),

		;

		private String content;

		private Integer value;

		private SysContent(String content, Integer value) {
			this.content = content;
			this.value = value;
		}

		public static SysContent valueOf(Integer value) {
			SysContent[] entities = SysContent.values();
			for (SysContent entity : entities) {
				if (entity.getValue().equals(value)) {
					return entity;
				}
			}
			return null;
		}

		@Override
		public String getContent() {
			return this.content;
		}

		@Override
		public Integer getValue() {
			return this.value;
		}

	}


	/**
	 * key-value
	 */
	public enum StringContent implements ContentEnums {

        REQ_LOCK_KEY("频繁请求锁", "fbp:request:lock:"),
        REQ_UN_LOCK_KEY("排除频繁请求锁", "fbp:request:unlock"),
		SESSION_KEY_PREFIX("session", "fbp:session:"),
		;

		private String content;

		private String value;

		private StringContent(String content, String value) {
			this.content = content;
			this.value = value;
		}

		public static StringContent valueOf(Integer value) {
			StringContent[] entities = StringContent.values();
			for (StringContent entity : entities) {
				if (entity.getValue().equals(value)) {
					return entity;
				}
			}
			return null;
		}

		@Override
		public String getContent() {
			return this.content;
		}

		@Override
		public String getValue() {
			return this.value;
		}

	}

}
