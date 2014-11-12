package gr.iti.mklab.framework.client.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */

public class Facet implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7222978342717761726L;
	
	String name;
    List<Bucket> buckets = new ArrayList<Bucket>();

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

}
