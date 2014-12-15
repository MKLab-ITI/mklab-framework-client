package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.dysco.CustomDysco;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.common.domain.dysco.Dysco.DyscoType;
import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.Query;
import gr.iti.mklab.framework.client.search.SearchEngineResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author etzoannos
 */
public class SolrMediaItemHandler {

    private SolrServer server;
	private Logger logger;
    
	private static Map<String, SolrMediaItemHandler> INSTANCES = new HashMap<String, SolrMediaItemHandler>();
    private static int commitPeriod = 5000;

    // Private constructor prevents instantiation from other classes
    private SolrMediaItemHandler(String collection) throws Exception {
    	server = new HttpSolrServer(collection);
    	logger = Logger.getLogger(SolrMediaItemHandler.class);
    }
    
    public void checkServerStatus() throws Exception {
    	server.ping();
    }

    // implementing Singleton pattern
    public static SolrMediaItemHandler getInstance(String collection) throws Exception {
        SolrMediaItemHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrMediaItemHandler(collection);
            
            INSTANCES.put(collection, INSTANCE);
        }
        
        return INSTANCE;
    }

    public boolean insertMediaItem(MediaItem item) {

        boolean status = true;
        try {
            SolrMediaItem solrItem = new SolrMediaItem(item);
            server.addBean(solrItem, commitPeriod);
        } catch (Exception e) {
            logger.error(e.getMessage());
            status = false;
        } 
        
        return status;
    }

    public boolean insertMediaItems(List<MediaItem> mediaItems) {

        boolean status = true;
        try {
            List<SolrMediaItem> solrMediaItems = new ArrayList<SolrMediaItem>();
            for (MediaItem mediaItem : mediaItems) {
                SolrMediaItem solrMediaItem = new SolrMediaItem(mediaItem);
                solrMediaItems.add(solrMediaItem);
            }
            server.addBeans(solrMediaItems, commitPeriod);
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            status = false;
        }
            
        return status;
    }

    public SearchEngineResponse<MediaItem> addFilterAndSearchItems(Query query, String fq) {
        SolrQuery solrQuery = new SolrQuery(query.getQueryString());
        solrQuery.addFilterQuery(fq);
        return search(solrQuery);
    }

    public SearchEngineResponse<MediaItem> removeFilterAndSearchItems(Query query, String fq) {
        SolrQuery solrQuery = new SolrQuery(query.getQueryString());
        return removeFilterAndSearch(solrQuery, fq);
    }

    public boolean deleteMediaItem(String mediaItemId) {
        boolean status = false;
        try {
        	String query = "id:" + mediaItemId;
        	UpdateResponse response  = server.deleteByQuery(query, commitPeriod);
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

    public boolean deleteItems(Query query) {
        boolean status = false;
        try {
        	UpdateResponse response = server.deleteByQuery(query.getQueryString(), commitPeriod);
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

    public boolean isIndexed(String id) {
        SolrQuery solrQuery = new SolrQuery("id:" + id);
		try {
			QueryResponse rsp = server.query(solrQuery);
			long nunFound = rsp.getResults().getNumFound();
			if (nunFound > 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
    }

    public List<MediaItem> findLatestItems(String query, int n) {
    	SolrQuery solrQuery = new SolrQuery(query);
    	solrQuery.addSort("publicationTime", SolrQuery.ORDER.desc);
    	solrQuery.setRows(n);
    	
    	 SearchEngineResponse<MediaItem> response = search(solrQuery);
    	 
         List<MediaItem> items = response.getResults();
         return items;
     
    }
    
    public List<MediaItem> findLatestItems(int n) {
    	return findLatestItems("*:*", n);
    }
    
    public SearchEngineResponse<MediaItem> findItems(SolrQuery query) {
        return search(query);
    }
    
    public Map<MediaItem,Float> findMediaItemsWithScore(String query){
    	Map<MediaItem,Float> mitemsByScore = new HashMap<MediaItem,Float>();
    	
    	SolrQuery solrQuery = new SolrQuery(query);
    	solrQuery.setFields("id","title","description","publicationTime","score");
		solrQuery.addSort("score", ORDER.desc);
		
        QueryResponse rsp = null;
       
        
        try {
            rsp = server.query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
            Logger.getRootLogger().info(e.getMessage());
            
        }
        System.out.println("Found "+rsp.getResults().getNumFound()+" results");
        List<SolrDocument> retrievedItems = rsp.getResults();
        
        for(SolrDocument sDoc : retrievedItems) {
        	Float score = (Float) sDoc.getFieldValue("score");
        	String title = (String) sDoc.getFieldValue("title");
        	String description = (String) sDoc.getFieldValue("description");
        	String id = (String) sDoc.getFieldValue("id");
        	Long publicationTime = (Long) sDoc.getFieldValue("publicationTime");
        	
        	System.out.println("Solr Document #"+id);
        	System.out.println("Solr Document Title : "+title);
        	System.out.println("Solr Document Score : "+description);
        	System.out.println("Solr Document Score : "+score);
        	
        	System.out.println();
        	MediaItem mitem = new MediaItem();
        	mitem.setId(id);
        	mitem.setTitle(title);
        	mitem.setDescription(description);
        	mitem.setPublicationTime(publicationTime);
        	
        	mitemsByScore.put(mitem, score);
        }
        
        return mitemsByScore;
    }

    public SearchEngineResponse<MediaItem> findItemsWithSocialSearch(SolrQuery query) {
        //query.setRequestHandler("/socialsearch");
        query.set("qt","/socialsearch");
        return search(query);
    }

    public List<MediaItem> findAllMediaItemsByKeywords(List<String> keywords, String type, int size) {

        List<MediaItem> mediaItems = new ArrayList<MediaItem>(size);
        boolean first = true;

        for (String key : keywords) {
            System.out.println("key : " + key);
        }

        String query = "(";

        if (keywords.size() == 1) {
            if (keywords.get(0).split(" ").length > 1) {
                query += "feedKeywordsString:\"" + keywords.get(0) + "\"";
            } else {
                query += "feedKeywords:" + keywords.get(0);
            }
        } else {
            List<String> wordEntities = new ArrayList<String>();
            List<String> simpleWords = new ArrayList<String>();

            query += "feedKeywordsString:(";

            //split keywords into two categories
            for (int i = 0; i < keywords.size(); i++) {
                if (keywords.get(i).split(" ").length > 1) {
                    wordEntities.add(keywords.get(i));
                } else {
                    simpleWords.add(keywords.get(i));
                }
            }
            //feedKeywordsString matches words that are entities (names,organizations,locations)
            for (int i = 0; i < wordEntities.size(); i++) {
                if (!first) {
                    query += " OR ";
                }
                query += "\"" + wordEntities.get(i) + "\"";

                int j = i + 1;
                //else for all other keywords create combinations ((key_1ANDkey_2) OR (key_1ANDkey_3) OR ..)
                while (j < wordEntities.size()) {

                    query += " OR ";

                    String oneQuery = "(\"" + wordEntities.get(i) + "\" AND \"" + wordEntities.get(j) + "\")";

                    query += oneQuery;

                    int k = j + 1;
                    while (k < wordEntities.size()) {
                        query += " OR ";

                        String secQuery = "(\"" + wordEntities.get(i) + "\" AND \"" + wordEntities.get(j) + "\" AND \"" + wordEntities.get(k) + "\")";

                        query += secQuery;

                        k++;
                    }

                    j++;
                }

                first = false;
            }
            if (first && simpleWords.size() > 0) {
                query = "(feedKeywords:(";
            } else if (simpleWords.size() > 0) {
                first = true;
                query += ") OR feedKeywords:(";
            }
            for (int i = 0; i < simpleWords.size(); i++) {
                int j = i + 1;
                // for all other keywords create combinations ((key_1ANDkey_2) OR (key_1ANDkey_3) OR ..)
                while (j < simpleWords.size()) {
                    if (!first) {
                        query += " OR ";
                    }

                    String oneQuery = "(" + simpleWords.get(i) + " AND " + simpleWords.get(j) + ")";

                    query += oneQuery;

                    int k = j + 1;
                    while (k < simpleWords.size()) {
                        query += " OR ";

                        String secQuery = "(" + simpleWords.get(i) + " AND " + simpleWords.get(j) + " AND " + simpleWords.get(k) + ")";

                        query += secQuery;

                        k++;
                    }

                    j++;

                    first = false;
                }
                j = 0;
                while (j < wordEntities.size()) {
                    if (!first) {
                        query += " OR ";
                    }

                    String oneQuery = "(" + simpleWords.get(i) + " AND " + wordEntities.get(j) + ")";

                    query += oneQuery;

                    int k = j + 1;
                    while (k < wordEntities.size()) {
                        query += " OR ";

                        String secQuery = "(" + simpleWords.get(i) + " AND " + wordEntities.get(j) + " AND " + wordEntities.get(k) + ")";

                        query += secQuery;

                        k++;
                    }
                    k = i + 1;
                    while (k < simpleWords.size()) {
                        query += " OR ";

                        String secQuery = "(" + simpleWords.get(i) + " AND " + wordEntities.get(j) + " AND " + simpleWords.get(k) + ")";

                        query += secQuery;

                        k++;
                    }

                    j++;

                    first = false;
                }

            }
            query += ")";
        }
        /*
         //OLD VERSION
         String query = "feedKeywords:(";
         //If only one keyword query with that
         if(keywords.size() == 1){
         query += keywords.get(0);
         }
         else{
         for(int i=0;i<keywords.size();i++){
         //If keyword is a name (two words) make it a stand-alone term for query
         if(keywords.get(i).split(" ").length >1){
         if (!first) {
         query += " OR ";
         }
	        		
         query += "("+keywords.get(i)+")";
         first = false;
         }
	        	
         int j=i+1;
         //else for all other keywords create combinations ((key_1ANDkey_2) OR (key_1ANDkey_3) OR ..)
         while(j<keywords.size()){
	        		
         if (!first) {
         query += " OR ";
         }
	
         String oneQuery = "("+keywords.get(i)+" AND "+keywords.get(j)+")";
	        		
         query += oneQuery;
	        		
         first = false;
         j++;
         }
	        	
         }
         }*/


        //Set to the query the type of media item we want to be retrieved from solr (image - video)
        query += ") AND type : " + type;

        //escape "/" character in Solr Query
        query = query.replace("/", "\\/");

        SolrQuery solrQuery = new SolrQuery(query);
        Logger.getRootLogger().info("query: " + query);
        solrQuery.setRows(size);
        SearchEngineResponse<MediaItem> response = search(solrQuery);

        if (response != null) {
            List<MediaItem> results = response.getResults();
            Set<String> urls = new HashSet<String>();
            for (MediaItem mi : results) {
                if (!urls.contains(mi.getUrl()) && !mi.getThumbnail().contains("sddefault") && !mi.getUrl().contains("photo_unavailable")) {
                    mediaItems.add(mi);
                    urls.add(mi.getUrl());
                }
            }
        }
        return mediaItems;
    }

    public SearchEngineResponse<MediaItem> findAllDyscoItemsLightByTime(
            String dyscoId) {
        SolrQuery solrQuery = new SolrQuery("dyscoId:" + dyscoId);
        solrQuery.addSort("publicationTime", SolrQuery.ORDER.asc);
        solrQuery.setRows(200);
        return search(solrQuery);
    }

    public SearchEngineResponse<MediaItem> findAllDyscoItems(String dyscoId) {
        SolrQuery solrQuery = new SolrQuery("dyscoId:" + dyscoId);
        solrQuery.setRows(200);
        return search(solrQuery);
    }

    private SearchEngineResponse<MediaItem> removeFilterAndSearch(
            SolrQuery query, String fq) {

        query.removeFilterQuery(fq);
        return search(query);
    }

    public MediaItem getSolrMediaItem(String id) {

        SolrQuery solrQuery = new SolrQuery("id:" + id);
        SearchEngineResponse<MediaItem> mi = search(solrQuery);

        List<MediaItem> results = mi.getResults();

        if (results == null || results.size() == 0) {
            return null;
        }

        MediaItem mediaItem = results.get(0);
        mediaItem.setId(id);
        return mediaItem;

    }

    private SearchEngineResponse<MediaItem> search(SolrQuery query) {

        SearchEngineResponse<MediaItem> response = new SearchEngineResponse<MediaItem>();
        QueryResponse rsp;
        

        query.setFields("* score");
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            e.printStackTrace();
            Logger.getRootLogger().info(e.getMessage());
            return null;
        }

        
        response.setNumFound(rsp.getResults().getNumFound());
       
        List<SolrMediaItem> solrItems = new ArrayList<SolrMediaItem>();
        
        SolrDocumentList docs = rsp.getResults();
        for(SolrDocument doc : docs){
        	SolrMediaItem solrMediaItem = new SolrMediaItem(doc);
        	solrItems.add(solrMediaItem);
        }
      
        if (solrItems != null) {
            Logger.getRootLogger().info("got: " + solrItems.size() + " media items from Solr - total results: " + response.getNumFound());
        }
        
        List<MediaItem> mediaItems = new ArrayList<MediaItem>();
        for (SolrMediaItem solrMediaItem : solrItems) {
            try {
                MediaItem mediaItem = solrMediaItem.toMediaItem();
                String id = mediaItem.getId();
               
                mediaItem.setId(id);

                mediaItems.add(mediaItem);
            } catch (MalformedURLException ex) {
                Logger.getRootLogger().error(ex.getMessage());
            }
        }

        response.setResults(mediaItems);
        List<Facet> facets = new ArrayList<Facet>();
        List<FacetField> solrFacetList = rsp.getFacetFields();
        FacetField solrFacet;


        if (solrFacetList != null) {

            //populate all non-zero facets

            for (int i = 0; i < solrFacetList.size(); i++) {

                Facet facet = new Facet(); //initialize for Arcomem JSF UI
                List<Bucket> buckets = new ArrayList<Bucket>();
                solrFacet = solrFacetList.get(i); //get the ones returned from Solr
                List<FacetField.Count> values = solrFacet.getValues();
                String solrFacetName = solrFacet.getName();
                boolean validFacet = false;

                //populate Valid Facets
                for (int j = 0; j < solrFacet.getValueCount(); j++) {

                    Bucket bucket = new Bucket();
                    long bucketCount = values.get(j).getCount();
//                    if ((bucketCount > 0) && (bucketCount != solrItems.size())) { //bucket is neither non-zero length nor the whole set 
                    validFacet = true; //facet contains at least one non-zero length bucket
                    bucket.setCount(bucketCount);
                    bucket.setName(values.get(j).getName());
                    bucket.setQuery(values.get(j).getAsFilterQuery());
                    bucket.setFacet(solrFacetName);
                    buckets.add(bucket);
//                    }
                }
                if (validFacet) { //add the facet only if it is contains at least one non-zero length - excludes the whole set result
                    facet.setBuckets(buckets);
                    facet.setName(solrFacetName);
                    facets.add(facet);
                }
            }

            Collections.sort(facets, new Comparator<Facet>() { //anonymous inner class used for sorting
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

        return response;
    }

    public SearchEngineResponse<MediaItem> findImages(String query, List<String> filters, List<String> facets, String orderBy, int size) {
        return collectMediaItemsByQuery(query, "image", filters, facets, orderBy, size);
    }


    public SearchEngineResponse<MediaItem> findImages(Dysco dysco, List<String> filters, List<String> facets, String orderBy, int size) {

    	SearchEngineResponse<MediaItem> mediaItems;
        if (dysco.getDyscoType().equals(DyscoType.TRENDING)) {

        	List<gr.iti.mklab.framework.common.domain.Query> queries = dysco.getSolrQueries();
            
        	mediaItems = collectMediaItemsByQueries(queries, "image", filters, facets, orderBy, size);
        } else {
            CustomDysco customDysco = (CustomDysco) dysco;
            List<gr.iti.mklab.framework.common.domain.Query> queries = customDysco.getSolrQueries();

            List<String> twitterMentions = customDysco.getMentionedUsers();
            List<String> twitterUsers = customDysco.getTwitterUsers();
            List<String> wordsToExclude = customDysco.getWordsToAvoid();

            Map<String, Double> hashtags = dysco.getHashtags();
            if(hashtags != null) {
            	for(Entry<String, Double> hashtag : hashtags.entrySet()) {
            		gr.iti.mklab.framework.common.domain.Query q = new gr.iti.mklab.framework.common.domain.Query();
            		q.setName(hashtag.getKey());
            		q.setScore(hashtag.getValue());
            	
            		queries.add(q);
            	}
            }
            
            mediaItems = collectMediaItems(queries, twitterMentions, twitterUsers, wordsToExclude, "image", filters, facets, orderBy, size);
        }

        return mediaItems;
    }
    
    private SearchEngineResponse<MediaItem> collectMediaItemsByQuery(String query, String type, List<String> filters, List<String> facets, String orderBy, int size) {

        List<MediaItem> mediaItems = new LinkedList<MediaItem>();
        SearchEngineResponse<MediaItem> response = new SearchEngineResponse<MediaItem>();

        if (query.equals("")) {
            return response;
        }

        // TEST CODE FOR MEDIA RETRIEVAL
        query = query.replaceAll("[\"()]", " ");
        query = query.trim();
        
        // Join query parts with AND 
        String[] queryParts = query.split("\\s+");
        query = StringUtils.join(queryParts, " AND ");
        
        //Retrieve multimedia content that is stored in solr
        if (!query.contains("title") && !query.contains("description")) {
            query = "((title : " + query + ") OR (description:" + query + "))";
        }
        // ==============================
        
        
        
        //Set filters in case they exist exist
        for (String filter : filters) {
            query += " AND " + filter;
        }

        query += " AND (type : " + type + ")";

        SolrQuery solrQuery = new SolrQuery(query);

        solrQuery.setRows(size);

        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);

        }

        Logger.getRootLogger().info("orderBy: " + orderBy);

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        Logger.getRootLogger().info("Solr Query : " + query);

        response = findItems(solrQuery);
        if (response != null) {
            List<MediaItem> results = response.getResults();
            Set<String> urls = new HashSet<String>();
            Set<String> clusterIds = new HashSet<String>();
            for (MediaItem mi : results) {
                System.out.println("Fetched media item: " + mi.getId() + " : " + mi.getSolrScore());
                if (!urls.contains(mi.getUrl())) {
                	String clusterId = mi.getClusterId();
                	if(clusterId == null) {
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                	else if(!clusterIds.contains(clusterId)) {
                		clusterIds.add(clusterId);
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                }

                if ((mediaItems.size() >= size)) {
                    break;
                }

            }
        }

        response.setResults(mediaItems);
        return response;
    }

    private SearchEngineResponse<MediaItem> collectMediaItemsByQueries(List<gr.iti.mklab.framework.common.domain.Query> queries, 
    		String type, List<String> filters, List<String> facets, String orderBy, int size) {

        List<MediaItem> mediaItems = new ArrayList<MediaItem>();

        SearchEngineResponse<MediaItem> response = new SearchEngineResponse<MediaItem>();

        if (queries.isEmpty()) {
            return response;
        }

        //Retrieve multimedia content that is stored in solr
        String allQueriesToOne = Utils.buildKeywordSolrQuery(queries, "OR");
        //String queryForRequest = "(title : (" + allQueriesToOne + ") OR description:(" + allQueriesToOne + "))";
        
        String queryForRequest = "(title : (" + allQueriesToOne + ") OR description:(" + allQueriesToOne + ")"
        		+ " OR tags : (" + allQueriesToOne + "))";

        //Set filters in case they exist exist
        for (String filter : filters) {
            queryForRequest += " AND " + filter;
        }

        queryForRequest += " AND (type : " + type + ")";

        SolrQuery solrQuery = new SolrQuery(queryForRequest);
        Logger.getRootLogger().info("Solr Query: " + queryForRequest);

        solrQuery.setRows(2*size);
        solrQuery.addSort("score", ORDER.desc);
        if (orderBy != null) {
            solrQuery.addSort(orderBy, ORDER.desc);
        }

        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);

        }

        response = findItems(solrQuery);
        if (response != null) {
            List<MediaItem> results = response.getResults();
            Set<String> urls = new HashSet<String>();
            Set<String> clusterIds = new HashSet<String>();
            
            for (MediaItem mi : results) {
                if (!urls.contains(mi.getUrl())) {
                	String clusterId = mi.getClusterId();
                	if(clusterId == null) {
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                	else if(!clusterIds.contains(clusterId)) {
                		clusterIds.add(clusterId);
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                }
                
                if ((mediaItems.size() >= size)) {
                    break;
                }
            }
        }

        response.setResults(mediaItems);
        return response;
    }

    private SearchEngineResponse<MediaItem> collectMediaItems(List<gr.iti.mklab.framework.common.domain.Query> queries, List<String> mentions,
            List<String> users, List<String> wordsToExclude, String type, List<String> filters, List<String> facets, String orderBy, int size) {

        List<MediaItem> mediaItems = new ArrayList<MediaItem>();
        SearchEngineResponse<MediaItem> response = new SearchEngineResponse<MediaItem>();

        if (queries == null && mentions == null && users == null) {
            return response;
        }

        String query = "";
        
        //Retrieve multimedia content that is stored in solr
        String textQuery = Utils.buildKeywordSolrQuery(queries, "OR");

        //set mentions
        if (mentions != null && !mentions.isEmpty()) {
        	String mentionsQuery = StringUtils.join(mentions, " OR ");
        	 if (textQuery.isEmpty()) {
        		 textQuery = mentionsQuery;
             } else {
            	 textQuery += " OR " + mentionsQuery;
             }
        }

        if (textQuery != null && !textQuery.isEmpty()) {
            query += "(title : (" + textQuery + ") OR description:(" + textQuery + "))";
        }


        //set Twitter users
        if (users != null && !users.isEmpty()) {
            String usersQuery = StringUtils.join(users, " OR ");
                if (query.isEmpty()) {
                	query = " author: (" + usersQuery + ")";
                } else {
                	query += " OR (author: (" + usersQuery + "))";
                }
            
        }

        if (query.isEmpty()) {
            return response;
        }

        //add words to exclude in query
        if (wordsToExclude != null && !wordsToExclude.isEmpty()) {
        	String exclude = StringUtils.join(wordsToExclude, " OR ");
        	query += " NOT (title : (" + exclude + ") OR description:(" + exclude + "))";
        }
        
        //Set source filters in case they exist exist
        if(filters!=null && !filters.isEmpty()) {
        	String filtersQuery = StringUtils.join(filters, " AND ");
        	 if (query.isEmpty()) {
             	query = filtersQuery;
             } else {
             	query = "(" + query + ") AND " + filtersQuery;
             }
        }

        query += " AND type : " + type;

        SolrQuery solrQuery = new SolrQuery(query);
        Logger.getRootLogger().info("Solr Query: " + query);

        solrQuery.setRows(size);

        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);
        }

        //solrQuery.addFilterQuery("publicationTime:["+86400000+" TO *]");
        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        response = findItems(solrQuery);
        if (response != null) {
            List<MediaItem> results = response.getResults();
            Set<String> urls = new HashSet<String>();
            Set<String> clusterIds = new HashSet<String>();
            for (MediaItem mi : results) {

                if (!urls.contains(mi.getUrl())) {
                	String clusterId = mi.getClusterId();
                	if(clusterId == null) {
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                	else if(!clusterIds.contains(clusterId)) {
                		clusterIds.add(clusterId);
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                }

                if ((mediaItems.size() >= size)) {
                    break;
                }
            }
        }

        response.setResults(mediaItems);
        return response;
    }
    
    public void forceCommitPending() {

        try {

            server.commit();
        } catch (SolrServerException ex) {
            ex.printStackTrace();
            Logger.getRootLogger().error(ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getRootLogger().error(ex.getMessage());
        }
    }
    
    
}