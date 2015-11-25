package org.lukosan.salix.feed.instagram;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.lukosan.salix.ResourceWriter;
import org.lukosan.salix.SalixResourceJson;
import org.lukosan.salix.SalixResourceType;

import com.fasterxml.jackson.databind.ObjectMapper;

public class InstagramResource implements SalixResourceJson {

	private static final Log logger = LogFactory.getLog(InstagramResource.class);
	
	private static final long serialVersionUID = 1L;

	private String scope;
	private MediaFeedData data;
	
	public InstagramResource(String scope, MediaFeedData data) {
		this.scope = scope;
		this.data = data;
	}
	
	public String getSourceId() {
		return String.format("instagram_%s.json", data.getId());
	}
	
	public String getScope() {
		return scope;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap() {
		ObjectMapper MAPPER = new ObjectMapper();
		try {
			return MAPPER.readValue(MAPPER.writeValueAsString(data), HashMap.class);
		} catch (IOException e) {
			logger.error(e);
			return new HashMap<String, Object>();
		}
	}

	@Override
	public String getSourceUri() {
		return data.getLink();
	}

	@Override
	public String getResourceId() {
		return null;
	}

	@Override
	public String getResourceUri() {
		return null;
	}

	@Override
	public SalixResourceType getResourceType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeTo(ResourceWriter writer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean exists() {
		return true;
	}
}
