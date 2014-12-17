package gr.iti.mklab.framework.client.search;

import gr.iti.mklab.framework.common.domain.JSONable;

/**
 *
 * @author etzoannos
 */
public class Bucket extends JSONable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 9024691192712839896L;
	
	private String name;
	
	private long count;
    
	private String query;
    
	private String facet;

    public Bucket() {
    	
    }
        
    public Bucket(String name, long count, String query, String facet) {
        this.name = name;
        this.count = count;
        this.query = query;
        this.facet = facet;
    }

    public String getFacet() {
        return facet;
    }

    public void setFacet(String facet) {
        this.facet = facet;
    }
    
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    
    
}