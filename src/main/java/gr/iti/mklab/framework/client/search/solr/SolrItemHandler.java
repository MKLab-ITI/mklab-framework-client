package gr.iti.mklab.framework.client.search.solr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.dysco.CustomDysco;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.common.domain.dysco.Dysco.DyscoType;
import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.Query;
import gr.iti.mklab.framework.client.search.SearchEngineResponse;

/**
 *
 * @author etzoannos
 */
public class SolrItemHandler implements SolrHandler<Item> {

    private Logger logger = Logger.getLogger(SolrItemHandler.class);

    private SolrServer server;
    
    private static Map<String, SolrItemHandler> INSTANCES = new HashMap<String, SolrItemHandler>();
    private static int commitPeriod = 10000;

    private SolrItemHandler(String collection) throws Exception {
        server = new HttpSolrServer(collection);
    }

    public void checkServerStatus() throws Exception {
        server.ping();
    }

    //implementing Singleton pattern
    public static SolrItemHandler getInstance(String collection) throws Exception {
        SolrItemHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrItemHandler(collection);

            INSTANCES.put(collection, INSTANCE);
        }
        return INSTANCE;
    }

    public boolean insert(Item item) {
        boolean status = true;
        try {
            SolrItem solrItem = new SolrItem(item);
            server.addBean(solrItem, commitPeriod);
        } 
        catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        } 
       
        return status;
    }

    public boolean insert(List<Item> items) {

        boolean status = true;
        try {
            List<SolrItem> solrItems = new ArrayList<SolrItem>();
            for (Item item : items) {
                SolrItem solrItem = new SolrItem(item);
                solrItems.add(solrItem);
            }
            server.addBeans(solrItems, commitPeriod);
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            status = false;
        }
        
        return status;
    }

    public SearchEngineResponse<Item> addFilterAndSearchItems(Query query, String fq) {

        SolrQuery solrQuery = new SolrQuery(query.getQueryString());
        solrQuery.addFilterQuery(fq);

        return search(solrQuery);
    }

    public boolean delete(String itemId) {
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

    public boolean delete(Query query) {
        boolean status = false;
        try {
            server.deleteByQuery(query.getQueryString());
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

    public Item get(String itemId) {
        SolrQuery solrQuery = new SolrQuery("id:" + itemId);
        solrQuery.setRows(1);
        SearchEngineResponse<Item> response = search(solrQuery);
        List<Item> items = response.getResults();
        if (!items.isEmpty()) {
            return items.get(0);
        } else {
            logger.info("no tweet for this id found!!");
            return null;
        }
    }
    
    public List<String> getTopHashtags(int size) {
        List<String> hashtags = new ArrayList<String>();

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFacetField("tags");
        solrQuery.setRows(1);
        solrQuery.setFacetLimit(size);

        SearchEngineResponse<Item> response = search(solrQuery);

        List<Facet> facets = response.getFacets();

        for (Facet facet : facets) {

            if (facet.getName().equals("tags")) {
                List<Bucket> buckets = facet.getBuckets();
                for (Bucket bucket : buckets) {
                    if (bucket.getCount() > 0) {
                        hashtags.add(bucket.getName());
                    }
                }
            }
        }
        return hashtags;
    }
    
    public SearchEngineResponse<Item> find(SolrQuery query) {
        return searchWithoutFacet(query);
    }

    public Map<Item, Float> findItemsWithScore(String query) {
        Map<Item, Float> itemsByScore = new HashMap<Item, Float>();

        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setFields("id", "title", "description", "publicationTime", "score");
        solrQuery.addSort("score", ORDER.desc);

        solrQuery.setRows(100);

        QueryResponse rsp = null;

        try {
            rsp = server.query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
            Logger.getRootLogger().info(e.getMessage());

        }

        List<SolrDocument> retrievedItems = rsp.getResults();

        for (SolrDocument sDoc : retrievedItems) {

            Float score = (Float) sDoc.getFieldValue("score");
            String title = (String) sDoc.getFieldValue("title");
            String description = (String) sDoc.getFieldValue("description");
            String id = (String) sDoc.getFieldValue("id");
            Long publicationTime = (Long) sDoc.getFieldValue("publicationTime");

            Item item = new Item();
            item.setId(id);
            item.setTitle(title);
            item.setDescription(description);
            item.setPublicationTime(publicationTime);

            itemsByScore.put(item, score);
        }

        return itemsByScore;
    }

    public Item findLatestItemByAuthor(String uid) {

        SolrQuery solrQuery = new SolrQuery("uid:" + uid);
        solrQuery.addSort("publicationTime", SolrQuery.ORDER.desc);
        solrQuery.setRows(1);
        SearchEngineResponse<Item> response = search(solrQuery);
        List<Item> items = response.getResults();
        if (!items.isEmpty()) {
            return items.get(0);
        } else {
            Logger.getRootLogger().info("no tweet for this user found!!");
            return null;
        }
    }

    public Item findLatestItem() {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addSort("publicationTime", SolrQuery.ORDER.desc);
        solrQuery.setRows(1);

        SearchEngineResponse<Item> response = search(solrQuery);
        List<Item> items = response.getResults();
        if (!items.isEmpty()) {
            return items.get(0);
        } else {
            Logger.getRootLogger().info("no solr found!!");
            return null;
        }
    }

    public List<Item> findLatestItemsByAuthor(String authorId) {

        SolrQuery solrQuery = new SolrQuery("author:" + authorId);
        solrQuery.addSort("publicationTime", SolrQuery.ORDER.desc);
        solrQuery.setRows(6);
        SearchEngineResponse<Item> response = searchWithoutFacet(solrQuery);
        List<Item> items = response.getResults();
        if (!items.isEmpty()) {
            return items;
        } else {
            //no tweets found, return empty list (to avoid null pointer exceptions)
            return new ArrayList<Item>();
        }
    }

    public List<Item> findItemsRangeTime(long lowerBound, long upperBound) {
        SolrQuery solrQuery = new SolrQuery("publicationTime: {" + lowerBound + " TO " + upperBound + "]");
        solrQuery.setRows(2000000);
        SearchEngineResponse<Item> response = search(solrQuery);
        List<Item> items = response.getResults();
        if (!items.isEmpty()) {
            return items;
        } else {
            Logger.getRootLogger().info("no tweet for this range of time found!!");
            return null;
        }
    }

    private SearchEngineResponse<Item> search(SolrQuery query) {

        Long t1 = System.currentTimeMillis();

        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();

        query.setFacet(true);
        query.addFacetField("sentiment");
        query.addFacetField("location");
//        query.setFacetLimit(4);

//        query.set(FacetParams.FACET_DATE, "creationDate");
//        query.set(FacetParams.FACET_DATE_START, "NOW/DAY-5YEARS");
//        query.set(FacetParams.FACET_DATE_END, "NOW/DAY");
//        query.set(FacetParams.FACET_DATE_GAP, "+1YEAR");
        QueryResponse rsp;

        System.out.println("query:  " + query.toString());
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        }

        Long t2 = System.currentTimeMillis();

        response.setNumFound(rsp.getResults().getNumFound());

        List<SolrItem> solrItems = rsp.getBeans(SolrItem.class);
        if (solrItems != null) {
            Logger.getRootLogger().info("got: " + solrItems.size() + " items from Solr - total results: " + response.getNumFound());
        }

        Long t3 = System.currentTimeMillis();

        List<Item> items = new ArrayList<Item>();
        for (SolrItem solrItem : solrItems) {
            try {
                items.add(solrItem.toItem());
            } catch (MalformedURLException ex) {
                logger.error(ex.getMessage());
            }
        }

        Long t4 = System.currentTimeMillis();

        response.setResults(items);

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

        Long t5 = System.currentTimeMillis();

        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: fetching: " + (t2 - t1));
        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: getting Beans: " + (t3 - t2));
        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: converting to domain object: " + (t4 - t3));
        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: calculating facets: " + (t5 - t4));

        return response;
    }

    private SearchEngineResponse<Item> searchWithoutFacet(SolrQuery query) {

        Long t1 = System.currentTimeMillis();

        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();

        QueryResponse rsp;

        System.out.println("query:  " + query.toString());
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        }

        Long t2 = System.currentTimeMillis();

        response.setNumFound(rsp.getResults().getNumFound());

        List<SolrItem> solrItems = rsp.getBeans(SolrItem.class);
        if (solrItems != null) {
            Logger.getRootLogger().info("got: " + solrItems.size() + " items from Solr - total results: " + response.getNumFound());
        }

        Long t3 = System.currentTimeMillis();

        List<Item> items = new ArrayList<Item>();
        for (SolrItem solrItem : solrItems) {
            try {
                items.add(solrItem.toItem());
            } catch (MalformedURLException ex) {
                logger.error(ex.getMessage());
            }
        }

        Long t4 = System.currentTimeMillis();

        response.setResults(items);

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
                    
                    validFacet = true; //facet contains at least one non-zero length bucket
                    bucket.setCount(bucketCount);
                    bucket.setName(values.get(j).getName());
                    bucket.setQuery(values.get(j).getAsFilterQuery());
                    bucket.setFacet(solrFacetName);
                    buckets.add(bucket);
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

        Long t5 = System.currentTimeMillis();

        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: fetching: " + (t2 - t1));
        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: getting Beans: " + (t3 - t2));
        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: converting to domain object: " + (t4 - t3));
        Logger.getRootLogger().info("SOLR ITEM HANDLER DURATION: calculating facets: " + (t5 - t4));

        return response;
    }

    public SearchEngineResponse<Item> findItems(String query, List<String> filters, List<String> facets, String orderBy, Map<String, String> params, int size) {
        return collectItemsByQuery(query, filters, facets, orderBy, params, size);
    }

    public SearchEngineResponse<Item> findItems(Dysco dysco, List<String> filters, List<String> facets, String orderBy, Map<String, String> params, int size) {

        if (dysco.getDyscoType().equals(DyscoType.TRENDING)) {
    		List<gr.iti.mklab.framework.common.domain.Query> queries = dysco.getSolrQueries();

    		return collectItemsByQueries(queries, filters, facets, orderBy, params, size);
        } else {

            CustomDysco customDysco = (CustomDysco) dysco;
            List<gr.iti.mklab.framework.common.domain.Query> queries = customDysco.getSolrQueries();

            Map<String, Double> hashtags = dysco.getHashtags();
            if(hashtags != null) {
            	for(Entry<String, Double> hashtag : hashtags.entrySet()) {
            		gr.iti.mklab.framework.common.domain.Query q = new gr.iti.mklab.framework.common.domain.Query();
            		q.setName(hashtag.getKey());
            		q.setScore(hashtag.getValue());
            	
            		queries.add(q);
            	}
            }
            
            List<String> twitterMentions = customDysco.getMentionedUsers();
            List<String> twitterUsers = customDysco.getTwitterUsers();
            List<String> wordsToExclude = customDysco.getWordsToAvoid();

            return collectItems(queries, hashtags, twitterMentions, twitterUsers, wordsToExclude, filters, facets, orderBy, params, size);
        }

    }
    
    private SearchEngineResponse<Item> collectItemsByQuery(String query, List<String> filters, List<String> facets, String orderBy, Map<String, String> params, int size) {

        List<Item> items = new ArrayList<Item>();
        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();

        if (query == null || query.isEmpty() || query.equals("")) {
            return response;
        }

        query = query.replaceAll("[\"()]", " ");
        query = query.trim();
        
        // Join query parts with AND 
        String[] queryParts = query.split("\\s+");
        query = StringUtils.join(queryParts, " AND ");
        
        //Retrieve multimedia content that is stored in solr
        if (!query.contains("title") && !query.contains("description")) {
            query = "((title : " + query + ") OR (description:" + query + "))";
        }

        //Set source filters in case they exist exist
        for (String filter : filters) {
            query += " AND " + filter;
        }

        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(size);

        for (Map.Entry<String, String> param : params.entrySet()) {
            solrQuery.add(param.getKey(), param.getValue());
        }

        //Set facets if necessary
        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);

        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        Logger.getRootLogger().info("Solr Query : " + query);

        response = find(solrQuery);
        if (response != null) {
            List<Item> results = response.getResults();

            for (Item it : results) {
                items.add(it);
                if ((items.size() >= size)) {
                    break;
                }
            }
        }

        response.setResults(items);
        return response;
    }

    private SearchEngineResponse<Item> collectItemsByQueries(List<gr.iti.mklab.framework.common.domain.Query> queries, List<String> filters, List<String> facets, String orderBy, Map<String, String> params, int size) {

        List<Item> items = new ArrayList<Item>();
        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();

        if (queries.isEmpty()) {
            return response;
        }

        //Retrieve multimedia content that is stored in solr
        String allQueriesToOne = Utils.buildKeywordSolrQuery(queries, "OR");
        String queryForRequest = "(title : (" + allQueriesToOne + ") OR description:(" + allQueriesToOne + "))";
        
        //Set source filters in case they exist exist
        for (String filter : filters) {
            queryForRequest += " AND " + filter;
        }

        SolrQuery solrQuery = new SolrQuery(queryForRequest);
        solrQuery.setRows(size);


        for (Map.Entry<String, String> param : params.entrySet()) {
            solrQuery.add(param.getKey(), param.getValue());
        }

        //Set facets if necessary
        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);

        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }
        
        Logger.getRootLogger().info("Solr Query: " + queryForRequest);

        response = find(solrQuery);
        if (response != null) {
            List<Item> results = response.getResults();
            for (Item it : results) {
                items.add(it);
                if (items.size() >= size) {
                    break;
                }
            }
        }

        response.setResults(items);
        return response;
    }

    private SearchEngineResponse<Item> collectItems(List<gr.iti.mklab.framework.common.domain.Query> queries, Map<String, Double> hashtags, List<String> mentions,
            List<String> users, List<String> wordsToExclude, List<String> filters, List<String> facets, String orderBy, Map<String, String> params, int size) {

    	List<Item> items = new ArrayList<Item>();
        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();    	 
    	 
        if (queries == null && mentions == null && users == null) {
            return response;
        }
        
        String query = "";

        // Create a Solr Query

        String textQuery = Utils.buildKeywordSolrQuery(queries, "OR");
        
        //set Twitter mentions
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
            	query = "author : (" + usersQuery + ")";
            } else {
            	query += " OR (author : (" + usersQuery + "))";
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
        
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(size);
        for (Map.Entry<String, String> param : params.entrySet()) {
            solrQuery.add(param.getKey(), param.getValue());
        }

        //Set facets if necessary
        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);
        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        Logger.getRootLogger().info("Solr Query: " + query);

        response = find(solrQuery);
        if (response != null) {
            List<Item> results = response.getResults();

            for (Item it : results) {
                items.add(it);
                if (items.size() >= size) {
                    break;
                }
            }
        }

        response.setResults(items);
        return response;
    }
    
    public static void main(String... args) throws Exception {

        SolrItemHandler handler =  SolrItemHandler.getInstance("http://socialsensor.atc.gr/solr/items");

        List<String> hashtags = handler.getTopHashtags(100);
        System.out.println("count: " + hashtags.size());

        for (String hashtag : hashtags) {
            Logger.getRootLogger().info("hashtag: " + hashtag);
        }
    }

}
