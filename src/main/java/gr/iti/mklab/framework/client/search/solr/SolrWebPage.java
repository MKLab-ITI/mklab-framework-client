package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.WebPage;

import java.net.MalformedURLException;
import java.util.Date;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author etzoannos - e.tzoannos@atc.gr
 */
public class SolrWebPage {
	
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

	@Field(value = "reference")
	private String reference;
	
	public SolrWebPage() {
		
	}

	public SolrWebPage(WebPage webPage) {
        url = webPage.getUrl();
        expandedUrl = webPage.getExpandedUrl();
        domain = webPage.getDomain();
        title = webPage.getTitle();
        text = webPage.getText();
        date = webPage.getDate();
        reference = webPage.getReference();
    }

    public WebPage toWebPage() throws MalformedURLException {
    	WebPage webPage = new WebPage(url, reference);
    	webPage.setExpandedUrl(expandedUrl);
    	webPage.setTitle(title);
    	webPage.setText(text);
    	webPage.setDate(date);
    	webPage.setDomain(domain);
        return webPage;
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
    
    public String getReference() {
    	return reference;
	}
	
}
