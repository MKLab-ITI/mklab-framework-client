package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.SearchEngineResponse;
import gr.iti.mklab.framework.common.domain.JSONable;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;

public interface SolrHandler<K extends JSONable> {

	public boolean insert(K k);
	
	public boolean insert(List<K> list);
	
	public boolean deleteById(String id);
	
	public boolean delete(String query);
	
	public K get(String id);
	
	public SearchEngineResponse<K> find(SolrQuery query);
    
}
