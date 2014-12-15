package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.Query;
import gr.iti.mklab.framework.client.search.SearchEngineResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public class SolrDyscoHandler implements SolrHandler<Dysco> {

    public final Logger logger = Logger.getLogger(SolrDyscoHandler.class);

    private SolrServer server;
    private static final Map<String, SolrDyscoHandler> INSTANCES = new HashMap<String, SolrDyscoHandler>();

    // Private constructor prevents instantiation from other classes
    private SolrDyscoHandler(String collection) {
        try {
        	server = new HttpSolrServer(collection);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    
    //implementing Singleton pattern
    public static SolrDyscoHandler getInstance(String collection) {
        SolrDyscoHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrDyscoHandler(collection);
            INSTANCES.put(collection, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public boolean insert(Dysco dysco) {
        boolean status = false;
        try {
            SolrDysco solrDysco = new SolrDysco(dysco);
            server.addBean(solrDysco);

            UpdateResponse response = server.commit();
            int statusId = response.getStatus();
            if (statusId == 0) {
                status = true;
            }

        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return status;
    }

    public boolean insert(List<Dysco> dyscos) {
        boolean status = false;
        try {
            List<SolrDysco> finalDyscos = new ArrayList<SolrDysco>();
            for (Dysco dysco : dyscos) {
                SolrDysco solrDysco = new SolrDysco(dysco);
                finalDyscos.add(solrDysco);
            }
            server.addBeans(finalDyscos);

            UpdateResponse response = server.commit();
            int statusId = response.getStatus();
            if (statusId == 0) {
                status = true;
            }

        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
            
        return status;
    }

    public boolean delete(String id) {
        boolean status = false;
        try {
            server.deleteById(id);
            UpdateResponse response = server.commit();
            int statusId = response.getStatus();
            if (statusId == 0) {
                status = true;
            }

        } catch (SolrServerException ex) {
            Logger.getRootLogger().error(ex.getMessage());
        } catch (IOException ex) {
            Logger.getRootLogger().error(ex.getMessage());
        }
        return status;
    }

    public boolean delete(Query query) {
        boolean status = false;
        try {
            server.deleteById(query.getQueryString());
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
    
    public Dysco get(String id) {
        SolrQuery solrQuery = new SolrQuery("id:" + id);
        SearchEngineResponse<Dysco> response = find(solrQuery);

        List<Dysco> dyscos = response.getResults();
        Dysco dysco = null;
        if (dyscos != null) {
            if (dyscos.size() > 0) {
                dysco = dyscos.get(0);
            }
        }
        return dysco;
    }
    
    public SearchEngineResponse<Dysco> find(SolrQuery query) {
        SearchEngineResponse<Dysco> response = new SearchEngineResponse<Dysco>();
        try {
        	QueryResponse rsp = server.query(query);
        	List<SolrDysco> resultList = rsp.getBeans(SolrDysco.class);
         
            List<Dysco> dyscos = new ArrayList<Dysco>();
            for (SolrDysco dysco : resultList) {
            	dyscos.add(dysco.toDysco());
            }

            response.setResults(dyscos);
            
            List<Facet> facets = new ArrayList<Facet>();
            List<FacetField> solrFacetList = rsp.getFacetFields();
            if (solrFacetList != null) {
                for (int i = 0; i < solrFacetList.size(); i++) {
                    Facet facet = new Facet();
                    List<Bucket> buckets = new ArrayList<Bucket>();
                    FacetField solrFacet = solrFacetList.get(i);
                    List<FacetField.Count> values = solrFacet.getValues();
                    String solrFacetName = solrFacet.getName();
                    boolean validFacet = false;

                    for (int j = 0; j < solrFacet.getValueCount(); j++) {
                        Bucket bucket = new Bucket();
                        long bucketCount = values.get(j).getCount();
                        if (bucketCount > 0) { 
                            validFacet = true;
                            bucket.setCount(bucketCount);
                            bucket.setName(values.get(j).getName());
                            bucket.setQuery(values.get(j).getAsFilterQuery());
                            bucket.setFacet(solrFacetName);
                            buckets.add(bucket);
                        }
                    }
                    
                    if (validFacet) {
                        facet.setBuckets(buckets);
                        facet.setName(solrFacetName);
                        facets.add(facet);
                    }
                }
                
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

            response.setFacets(facets);
            
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
        }

        return response;
    }

}
