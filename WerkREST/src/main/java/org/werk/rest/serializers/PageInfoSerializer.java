package org.werk.rest.serializers;

import org.json.JSONObject;
import org.werk.service.PageInfo;

public class PageInfoSerializer {
	public JSONObject serializePageInfo(PageInfo pageInfo) {
		JSONObject pageInfoJSON = new JSONObject();
		
		pageInfoJSON.put("itemsPerPage", pageInfo.getItemsPerPage());
		pageInfoJSON.put("pageNumber", pageInfo.getPageNumber());
		
		return pageInfoJSON;
	}

	public PageInfo deserializePageInfo(JSONObject pageInfoJSON) {
		PageInfo pageInfo = new PageInfo(pageInfoJSON.getLong("itemsPerPage"), 
				pageInfoJSON.getLong("pageNumber"));
		return pageInfo;
	}
}
