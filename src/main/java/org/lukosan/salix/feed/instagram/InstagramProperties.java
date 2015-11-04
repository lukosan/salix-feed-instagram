package org.lukosan.salix.feed.instagram;

import org.lukosan.salix.SalixScope;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("salix.instagram")
public class InstagramProperties {

	private String apiKey;
	private String apiSecret;
	private String baseCallbackUrl;
	private String accessToken;
	private String salixScope = SalixScope.SHARED;
	private int minutesCheckInterval = -1;
	
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getApiSecret() {
		return apiSecret;
	}
	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}
	public String getBaseCallbackUrl() {
		return baseCallbackUrl;
	}
	public void setBaseCallbackUrl(String baseCallbackUrl) {
		this.baseCallbackUrl = baseCallbackUrl;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getSalixScope() {
		return salixScope;
	}
	public void setSalixScope(String salixScope) {
		this.salixScope = salixScope;
	}
	public int getMinutesCheckInterval() {
		return minutesCheckInterval;
	}
	public void setMinutesCheckInterval(int minutesCheckInterval) {
		this.minutesCheckInterval = minutesCheckInterval;
	}
}
