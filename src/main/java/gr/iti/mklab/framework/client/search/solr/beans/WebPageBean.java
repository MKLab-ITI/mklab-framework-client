package gr.iti.mklab.framework.client.search.solr.beans;

import gr.iti.mklab.framework.common.domain.WebPage;

import java.util.Date;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author etzoannos - e.tzoannos@atc.gr
 */
public class WebPageBean extends Bean {
	
	@Field(value = "url")
	private String url;

	@Field(value = "expandedUrl")
	private String expandedUrl;
	
	@Field(value = "domain")
	private String domain;
	
	@Field(value = "title")
	private String title;
	
	@Field(value = "text")
	private String text;
	
	@Field(value = "date")
	private Date date;
	
	public WebPageBean() {
		
	}

	public WebPageBean(WebPage webPage) {
		id = webPage.getUrl();
		
        url = webPage.getUrl();
        expandedUrl = webPage.getExpandedUrl();
        domain = webPage.getDomain();
        title = webPage.getTitle();
        text = webPage.getText();
        date = webPage.getDate();     
    }
	
    public String getUrl() {
    	return url;
	}
    
    public String getExpandedUrl() {
    	return expandedUrl;
	}
    
    public String getDomain() {
    	return domain;
	}
    
    public String getTitle() {
    	return title;
	}
    
    public Date getDate() {
    	return date;
	}
	
}
