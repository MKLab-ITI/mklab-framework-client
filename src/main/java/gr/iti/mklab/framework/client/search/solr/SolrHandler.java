package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.SearchResponse;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;

public interface SolrHandler<K> {

	public boolean insert(K k);
	
	public boolean insert(List<K> list);
	
	public boolean deleteById(String id);
	
	public boolean delete(String query);
	
	public SearchResponse<String> find(SolrQuery query);
    
}
