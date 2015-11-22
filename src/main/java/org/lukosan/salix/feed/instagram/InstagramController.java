package org.lukosan.salix.feed.instagram;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.exceptions.InstagramException;
import org.lukosan.salix.feed.SalixFeed;
import org.lukosan.salix.feed.SalixFeedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(InstagramController.PATH)
public class InstagramController {

	public static final String PATH = "/salix/feed/instagram";
	public static final String CALLBACK = "/callback";
	
	@Autowired(required=false)
	private List<SalixFeedSource> sources;
	
	private List<SalixFeedInstagram> salixFeeds = new ArrayList<SalixFeedInstagram>();
	
	@Autowired(required=false)
	private InstagramProperties properties;
	
	@PostConstruct
	public void filterFeeds() {
		if(null != sources) {
			for(SalixFeedSource source : sources) {
				for(SalixFeed feed : source.getFeeds()) {
					if(SalixFeedInstagram.class.isAssignableFrom(feed.getClass()))
						salixFeeds.add((SalixFeedInstagram) feed);
				}
			}
		}
	}
	
	private SalixFeedInstagram findFeed(String scope) {
		for(SalixFeedInstagram feed : salixFeeds)
			if(feed.getSalixScope().equalsIgnoreCase(scope))
				return feed;
		SalixFeedInstagram feed = new SalixFeedInstagram(properties.getApiKey(), properties.getApiSecret(), 
				properties.getBaseCallbackUrl(), null, scope, -1);
		salixFeeds.add(feed);
		return feed;
	}
	
	@RequestMapping("/{scope}/")
	public String index(@PathVariable String scope, Model model) throws InstagramException {
		SalixFeedInstagram instagram = findFeed(scope);
		if(! instagram.isAuthed()) {
			String authorizationUrl = instagram.getAuthorizationUrl();
			return "redirect:" + authorizationUrl;
		}
		model.addAttribute("secret", instagram.api().getAccessToken().getSecret());
		MediaFeed feed = instagram.api().getRecentMediaFeed(instagram.api().getCurrentUserInfo().getData().getId(), 50, null, null, null, null);
		model.addAttribute("feed", feed.getData());
		return "salix/feed/instagram/index";
	}
	
	@RequestMapping(InstagramController.CALLBACK)
	public String callback(@RequestParam String code, @RequestParam String salixScope) {
		SalixFeedInstagram instagram = findFeed(salixScope);
		instagram.verify(code);
		// TODO encrypt the accessToken and store it somewhere, along with a scope
		return String.format("redirect:%s/%s/", PATH, salixScope );
	}
}