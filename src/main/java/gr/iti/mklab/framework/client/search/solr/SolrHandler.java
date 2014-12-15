package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.SearchEngineResponse;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;


public interface SolrHandler<K> {

	public boolean insert(K k);
	
	public boolean insert(List<K> list);
	
	public boolean delete(String id);
	
	public K get(String id);
	
	public SearchEngineResponse<K> find(SolrQuery query);

}
