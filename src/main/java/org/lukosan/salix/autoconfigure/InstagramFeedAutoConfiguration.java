package org.lukosan.salix.autoconfigure;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lukosan.salix.SalixService;
import org.lukosan.salix.feed.SalixFeedSource;
import org.lukosan.salix.feed.instagram.InstagramProperties;
import org.lukosan.salix.feed.instagram.SalixFeedInstagram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InstagramProperties.class)
@AutoConfigureBefore(FeedAutoConfiguration.class)
@ConditionalOnProperty(prefix = "salix.instagram", name = "enabled", matchIfMissing = true)
public class InstagramFeedAutoConfiguration {

	@Configuration
	@ConditionalOnClass({ Servlet.class })
	@ConditionalOnWebApplication
	@ComponentScan(basePackages="org.lukosan.salix.feed.instagram")
	public static class InstagramFeedConfiguration {
		
		private static final Log logger = LogFactory.getLog(InstagramFeedConfiguration.class);
		
		@Autowired
		private InstagramProperties properties;
		@Autowired
		private SalixService salixService;

		@Bean
		@ConditionalOnProperty(prefix="salix.instagram", name="accessToken")
		public SalixFeedInstagram salixFeedInstagram() {
			return new SalixFeedInstagram(properties.getApiKey(), properties.getApiSecret(), 
					properties.getBaseCallbackUrl(), properties.getAccessToken(), properties.getSalixScope(), -1);
		}
		
		@Bean
		public SalixFeedSource feeder() {
			return new SalixFeedSource(salixInstagramFeeds());
		}
		
		@Bean
		public List<SalixFeedInstagram> salixInstagramFeeds() {
			return salixService.configurationsFor(SalixFeedInstagram.class.getName()).stream()
					.map(config -> new SalixFeedInstagram(config)).collect(Collectors.toList());
		}
		
		@PostConstruct
		public void postConstruct() {
			if(logger.isInfoEnabled())
				logger.info("PostConstruct " + getClass().getSimpleName());
		}
	}
	
}