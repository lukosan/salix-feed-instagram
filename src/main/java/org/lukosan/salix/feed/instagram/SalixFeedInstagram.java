package org.lukosan.salix.feed.instagram;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinstagram.Instagram;
import org.jinstagram.auth.InstagramAuthService;
import org.jinstagram.auth.model.Token;
import org.jinstagram.auth.model.Verifier;
import org.jinstagram.auth.oauth.InstagramService;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.lukosan.salix.MapUtils;
import org.lukosan.salix.SalixConfiguration;
import org.lukosan.salix.SalixResource;
import org.lukosan.salix.SalixResourceJson;
import org.lukosan.salix.SalixService;
import org.lukosan.salix.feed.SalixFeed;
import org.springframework.util.StringUtils;

public class SalixFeedInstagram implements SalixFeed {

	private static final Log logger = LogFactory.getLog(SalixFeedInstagram.class);
			
	private static final Token EMPTY_TOKEN = null;
	
	private InstagramService service;
	private Instagram instagram = null;
	private Token accessToken = null;
	private int minutesCheckInterval = 10;
	private String salixScope;
	
	public SalixFeedInstagram(String apiKey, String apiSecret, String baseCallbackUrl, String accessToken, String salixScope, int minutesCheckInterval) {
		this.salixScope = salixScope;
		service = new InstagramAuthService()
	    	.apiKey(apiKey)
	    	.apiSecret(apiSecret)
	    	.callback(getCallbackUrl(baseCallbackUrl, salixScope)) 
	    	//.scope("comments")
	    	.build();
		if(StringUtils.hasText(accessToken)) {
			instagram = service.getInstagram(new Token(accessToken, ""));
			try {
				instagram.getCurrentUserInfo();
			} catch (InstagramException e) {
				logger.error("Exception getting CurrentUserInfo", e);
				service = null;
			}
		}
		if(minutesCheckInterval > -1)
			this.minutesCheckInterval = minutesCheckInterval;
	}
	
	public SalixFeedInstagram(SalixConfiguration configuration) {
		this(MapUtils.getString(configuration.getMap(), "apiKey"), MapUtils.getString(configuration.getMap(), "apiSecret"), 
				MapUtils.getString(configuration.getMap(), "baseCallbackUrl"), MapUtils.getString(configuration.getMap(), "accessToken"), 
				configuration.getScope(), MapUtils.getLong(configuration.getMap(), "minutesCheckInterval").intValue());
	}

	private String getCallbackUrl(String baseCallbackUrl, String salixScope) {
		String callback = baseCallbackUrl;
		if(callback.endsWith("/")) callback = callback.substring(0, callback.length() - 1);
		return String.format("%s%s%s?salixScope=%s", callback, InstagramController.PATH, InstagramController.CALLBACK, salixScope);
	}

	public boolean isAuthed() {
		return null != instagram;
	}

	public String getAuthorizationUrl() {
		return service.getAuthorizationUrl(EMPTY_TOKEN);
	}
	
	public void verify(String code) {
		Verifier verifier = new Verifier(code);
        accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
        instagram = new Instagram(accessToken);
	}
	
	public Instagram api() {
		return instagram;
	}

	public int getMinutesCheckInterval() {
		return minutesCheckInterval;
	}

	public void setMinutesCheckInterval(int minutesCheckInterval) {
		this.minutesCheckInterval = minutesCheckInterval;
	}

	private int cStrikes = 0;
	
	@Override
	public synchronized List<SalixResource> process(SalixService salixService) {
		List<SalixResource> resources = new ArrayList<SalixResource>();
		if(isAuthed()) {
			if(logger.isInfoEnabled())
				logger.info("Processing Instagram feed");
			try {
				for(MediaFeedData data : api().getRecentMediaFeed(api().getCurrentUserInfo().getData().getId()).getData()) {
					SalixResourceJson resource = new InstagramResource(salixScope, data);
					if(null == salixService.resource(resource.getSourceId(), resource.getScope())) {
						resources.add(salixService.save(resource.getScope(), resource.getSourceId(), data.getLink(), resource.getMap()));
					} else
						break;
				}
				cStrikes = 0;
			} catch (InstagramException e) {
				logger.error("Could not getRecentMediaFeed, strike " + (++cStrikes));
				if(cStrikes > 2) {
					logger.warn("Disabling SalixFeedInstagram due to 3 strikes");
					instagram = null;
				}
			}
		} else {
			logger.warn("Could not process SalixInstagramFeed because I'm not authenticated.");
		}
		return resources;
	}

	public String getSalixScope() {
		return salixScope;
	}
}