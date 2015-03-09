package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.SearchResponse;
import gr.iti.mklab.framework.client.search.solr.beans.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;

public abstract class SolrHandler<K extends Bean> {
	
    private static int commitPeriod = 10000;
    
	protected Logger logger;
	protected SolrServer server;
    
    public boolean insert(K bean) {
        boolean status = true;
        try {
            server.addBean(bean, commitPeriod);
        } 
        catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (Exception ex) {
        	logger.error(ex.getMessage());
            status = false;
        } 
       
        return status;
    }

    public boolean insert(List<K> beans) {
        boolean status = true;
        try {
            server.addBeans(beans, commitPeriod);
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            status = false;
        }
        
        return status;
    }
	
    public boolean deleteById(String itemId) {
        boolean status = false;
        try {
            server.deleteByQuery("id:" + itemId);
            UpdateResponse response = server.commit();
            int statusId = response.getStatus();
            if (statusId == 0) {
                status = true;
            }
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        
        return status;
    }
	
    public boolean delete(String query) {
        boolean status = false;
        try {
            server.deleteByQuery(query);
            UpdateResponse response = server.commit();
            int statusId = response.getStatus();
            if (statusId == 0) {
                status = true;
            }
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
            
        return status;
    }
	
	public abstract SearchResponse<K> find(SolrQuery query);
    
	public List<Facet> getFacets(QueryResponse rsp) {
		
		List<Facet> facets = new ArrayList<Facet>();
        
		List<FacetField> facetFields = rsp.getFacetFields();
        if (facetFields != null) {
            for (FacetField solrFacet : facetFields) {
            	
                Facet facet = new Facet(); 
                List<Bucket> buckets = new ArrayList<Bucket>();
                
                List<FacetField.Count> values = solrFacet.getValues();
                if(!values.isEmpty()) {
                	//populate Valid Facets
                	for (FacetField.Count facetCount : values) {
                    	Bucket bucket = new Bucket();
                   
                    	bucket.setCount(facetCount.getCount());
                    	bucket.setName(facetCount.getName());
                    	bucket.setQuery(facetCount.getAsFilterQuery());
                    	
                    	buckets.add(bucket);
                	}
                	facet.setBuckets(buckets);
                	facet.setName(solrFacet.getName());
                	facets.add(facet);
                }

            }

            // Sort
            Collections.sort(facets, new Comparator<Facet>() {
                @Override
                public int compare(Facet f1, Facet f2) {

                    String value1 = f1.getName();
                    String value2 = f2.getName();

                    if (value1.compareTo(value2) > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
        }
    
        return facets;
	}
	
}
