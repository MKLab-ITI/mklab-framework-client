package eu.socialsensor.framework.client.dao.impl;

import eu.socialsensor.framework.client.dao.DyscoDAO;
import eu.socialsensor.framework.client.dao.DyscoRequestDAO;
import eu.socialsensor.framework.client.dao.MediaItemDAO;
import eu.socialsensor.framework.client.search.Query;
import eu.socialsensor.framework.client.search.SearchEngineHandler;
import eu.socialsensor.framework.client.search.SearchEngineResponse;
import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.client.search.solr.SolrHandler;
import eu.socialsensor.framework.client.search.solr.SolrItemHandler;
import eu.socialsensor.framework.client.search.solr.SolrMediaItemHandler;
import eu.socialsensor.framework.client.search.solr.SolrWebPageHandler;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.WebPage;
import eu.socialsensor.framework.common.domain.dimension.Dimension;
import eu.socialsensor.framework.common.domain.dysco.Dysco;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public class DyscoDAOImpl implements DyscoDAO {

    SearchEngineHandler searchEngineHandler;
    private MediaItemDAO mediaItemDAO;
    private DyscoRequestDAO dyscoRequestDAO;
    private SolrItemHandler solrItemHandler;
    private SolrDyscoHandler handler;
    private SolrMediaItemHandler solrMediaItemHandler;
    private SolrWebPageHandler solrWebPageHandler;

    public DyscoDAOImpl(String mongoHost, String dyscoCollection, String itemCollection, String mediaItemCollection) throws Exception {
    	searchEngineHandler = new SolrHandler(dyscoCollection, itemCollection);
    	
    	try {
    		mediaItemDAO = new MediaItemDAOImpl(mongoHost,"Streams","MediaItems");
        	dyscoRequestDAO = new DyscoRequestDAOImpl(mongoHost,"Streams","Dyscos");
			solrItemHandler = SolrItemHandler.getInstance(itemCollection);
			handler = SolrDyscoHandler.getInstance(dyscoCollection);
	    	solrMediaItemHandler = SolrMediaItemHandler.getInstance(mediaItemCollection);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @Override
    public boolean insertDysco(Dysco dysco) {
        return searchEngineHandler.insertDysco(dysco);
    }

    @Override
    public boolean updateDysco(Dysco dysco) {

        return searchEngineHandler.updateDysco(dysco);

    }

    @Override
    public boolean destroyDysco(String id) {

        // TODO: check if this is actually string or long - try to unify it
        return searchEngineHandler.deleteDysco(id);
    }

    @Override
    public List<Item> findDyscoItems(String id) {

        SearchEngineResponse<Item> response = searchEngineHandler
                .findAllDyscoItems(id);
        List<Item> items = response.getResults();
        return items;
    }

    @Override
    public SearchEngineResponse findNDyscoItems(String id, int size) {

        SearchEngineResponse<Item> response = searchEngineHandler
                .findNDyscoItems(id, size);
        return response;
    }

    @Override
    public List<Item> findSortedDyscoItems(String id, String fieldToSort,
            ORDER order, int rows, boolean original) {

        SearchEngineResponse<Item> response = searchEngineHandler
                .findSortedItems(id, fieldToSort, order, rows, original);
        List<Item> items = response.getResults();

        return items;
    }

    @Override
    public List<Item> findSortedDyscoItemsByQuery(Query query, String fieldToSort,
            ORDER order, int rows, boolean original) {

        SearchEngineResponse<Item> response = searchEngineHandler
                .findSortedItems(query, fieldToSort, order, rows, original);
        List<Item> items = response.getResults();

        return items;
    }

    @Override
    public Dysco findDysco(String id) {
        return searchEngineHandler.findDysco(id);
    }

    @Override
    public SearchEngineResponse<Dysco> findDyscosLight(Query query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Dysco findDyscoLight(String id) {
        return searchEngineHandler.findDysco(id);
    }

    @Override
    public SearchEngineResponse<Item> findLatestItems(int count) {
        return searchEngineHandler.findLatestItems(count);
    }

    @Override
    public List<Dysco> findDyscoByTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Dysco> findDyscoByContainingItem(Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Dysco> findDyscoByDimension(Dimension dim) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Dysco> findCommunityRelatedDyscos(Dysco queryDysco) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Dysco> findContentRelatedDyscos(Dysco queryDysco) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SearchEngineResponse<Item> findItems(Query query) {
        return searchEngineHandler.findItems(query);
    }

    @Override
    public boolean updateDyscoWithoutItems(Dysco dysco) {
        return searchEngineHandler.updateDyscoWithoutItems(dysco);
    }

    @Override
    public SearchEngineResponse findNDyscoItems(String id, int size, boolean original) {

        SearchEngineResponse<Item> response = searchEngineHandler
                .findNDyscoItems(id, size, original);
        return response;
    }

    @Override
    public List<Item> findTotalItems(String _query) {

        List<Item> _totalItems;
        SolrQuery totalQuery = new SolrQuery(_query);
        //TODO: searchVar... maybe this should be -1 to get all
        totalQuery.setRows(1000);
        //TODO: see if we could minimize the fields returned for totalItems: 
        //links are needed for sure, maybe also sentiment which is used as facet
        //totalQuery.setFields("links","sentiment");
        _totalItems = solrItemHandler.findItems(totalQuery).getResults();

        return _totalItems;

    }

    @Override
    public List<Item> findTotalItems(List<String> dyscoIdsOfGroup) {

        List<Item> _totalItems;

        //getting items of the whole dysco group 
        //formulating the "find all items of dysco group" query

        String totalItemsQuery;

        //this means it's a trending Dysco
        int count = 0;
        String itemsOfGroupQuery = "dyscoId:(";

        for (String dyscoIdToSearch : dyscoIdsOfGroup) {

            if (count == 0) {
                itemsOfGroupQuery = itemsOfGroupQuery + dyscoIdToSearch;
            } else {
                itemsOfGroupQuery = itemsOfGroupQuery + " OR " + dyscoIdToSearch;
            }

            count++;
        }

        itemsOfGroupQuery = itemsOfGroupQuery + ")";

        totalItemsQuery = itemsOfGroupQuery;

        SolrQuery totalQuery = new SolrQuery(totalItemsQuery);
        //TODO: searchVar... maybe this should be -1 to get all
        totalQuery.setRows(1000);
        //TODO: see if we could minimize the fields returned for totalItems: 
        //links are needed for sure, maybe also sentiment which is used as facet
        totalQuery.setFields("links","sentiment");
 
        _totalItems = solrItemHandler.findItems(totalQuery).getResults();

        return _totalItems;

    }

    @Override
    //TODO: I think we can get this directly from Solr
    public List<String> findTotalUrls(List<Item> totalItems) {

        //convert HashSet to ArrayList

        Set<String> totalItemsUrls = new HashSet<String>();

        for (Item totalItem : totalItems) {
            URL[] totalItemLinks = totalItem.getLinks();
            if (totalItemLinks != null) {
                for (int i = 0; i < totalItemLinks.length; i++) {
                    totalItemsUrls.add(totalItemLinks[i].toString());
                }
            }

        }
        List totalUrlsToSearch = new ArrayList<String>(totalItemsUrls);

        return totalUrlsToSearch;

    }

    @Override
    public List<Dysco> findRelatedTopics(Dysco dysco) {

        List<Dysco> _relatedTopics = new ArrayList<Dysco>();
//        if ((dysco.getDyscoGroup() != null) && (!dysco.getDyscoGroup().equals(""))) {
//
////          uncomment the following line for getting only the "deprecated" Dyscos 
////          String relatedDyscosQuery = "dyscoGroup:" + dysco.getDyscoGroup() + " AND evolution:old";
//
//            String relatedDyscosQuery = "dyscoGroup:" + dysco.getDyscoGroup();
//
//            SolrQuery _solrQuery = new SolrQuery(relatedDyscosQuery);
//            _solrQuery.setFields("id", "title", "creationDate");
//            _solrQuery.addSortField("creationDate", SolrQuery.ORDER.desc);
//            _solrQuery.setRows(4);
//
//            _relatedTopics = handler.findDyscosLight(_solrQuery).getResults();
//
//            List<Dysco> tempTopics = new ArrayList<Dysco>();
//
//            //remove itself since it's included in the results (think of uncommenting the line above)
//            for (Dysco relatedTopic : _relatedTopics) {
//                if (!dysco.getId().equals(relatedTopic.getId())) {
//                    tempTopics.add(relatedTopic);
//                }
//            }
//            _relatedTopics = tempTopics;
//
//        }
        return _relatedTopics;
    }

   
    @Override
    public List<MediaItem> findVideos(String query, int size){
    	List<MediaItem> mediaItems = new ArrayList<MediaItem>();
    	
    	mediaItems.addAll(collectMediaItems(query,"video",size));
    	return mediaItems;	
    	
    }
    
    @Override
    public List<MediaItem> findVideos(Dysco dysco, int size) {
    	List<MediaItem> mediaItems = new ArrayList<MediaItem>();
    	
    	String query = dysco.getSolrQueryString();
    	
    	mediaItems.addAll(collectMediaItems(query,"video",size));
    	return mediaItems;
    }
    
    
    @Override
    public List<MediaItem> findImages(String query, int size){
    	List<MediaItem> mediaItems = new ArrayList<MediaItem>();
    	
    	mediaItems.addAll(collectMediaItems(query,"image",size));
    	return mediaItems;	
    	
    }

    @Override
    public List<MediaItem> findImages(Dysco dysco, int size) {
    	List<MediaItem> mediaItems = new ArrayList<MediaItem>();
    	
    	String query = dysco.getSolrQueryString();
    	//List<eu.socialsensor.framework.common.domain.Query> queries = dysco.getSolrQueries();
    	mediaItems.addAll(collectMediaItems(query,"image",size));
    	return mediaItems;
    }
    

    @Override
    public List<WebPage> findHealines(Dysco dysco, int size) {
    	
    	List<WebPage> webPages = new LinkedList<WebPage>();
    	
    	String query = dysco.getSolrQueryString();
    	
    	if(query.equals(""))
    		return webPages;
    
    	if(!query.contains("title") && !query.contains("description")) {
    		query = "(title : "+query+") OR (text:"+query+")";
    	}
    	
    	SolrQuery solrQuery = new SolrQuery(query);
    	solrQuery.setRows(200);
    	solrQuery.setSortField("score", ORDER.desc);
    	Logger.getRootLogger().info("final query : " + query);
    	
    	SearchEngineResponse<WebPage> response = solrWebPageHandler.findItems(solrQuery);
    	if(response != null){
    		List<WebPage> results = response.getResults();
    		Set<String> urls = new HashSet<String>();
	        for(WebPage wp : results) {
		        	if(!urls.contains(wp.getExpandedUrl())) {
		        		webPages.add(wp);
		        		urls.add(wp.getExpandedUrl());
		        	}
		        	
		        	if(webPages.size() >= size)
		        		break;
	        }    
    	}
    	
    	return webPages;
    }
    
    /**
     * Collect media items (images/videos) of a certain size based on a solr query.
     * Prioritize them according to a hashmap of network priorities. If no hashmap
     * is provided use default.
     * @param query
     * @param networkPriorities
     * @param size
     * @return
     */
    private Queue<MediaItem> collectMediaItems(String query, String type , int size){
    	
    	Queue<MediaItem> mediaItems = new LinkedList<MediaItem>();
    	
    	if(query.equals(""))
    		return mediaItems;
    
    	//Retrieve multimedia content that is stored in solr
    	
    	if(!query.contains("title") && !query.contains("description"))
    		query = "(title : "+query+") OR (description:"+query+") OR (tags:"+query+")";
    
    	SolrQuery solrQuery = new SolrQuery(query);
    	solrQuery.setRows(200);
    	solrQuery.setSortField("score", ORDER.desc);
    	Logger.getRootLogger().info("final query : " + query);
    	
    	SearchEngineResponse<MediaItem> response = solrMediaItemHandler.findItems(solrQuery);
    	if(response != null){
    		List<MediaItem> results = response.getResults();
    		Set<String> urls = new HashSet<String>();
	        for(MediaItem mi : results) {
	        	
	        	if(mi.getType().equals(type)){
		        	if(!urls.contains(mi.getUrl())) {
		        		
		        		mediaItems.add(mi);
		        	
		        		urls.add(mi.getUrl());
		        	}
		        	
		        	if(mediaItems.size() >= size)
		        		break;
	        	}
	        }
    	}
    	
    	
    	return mediaItems;
    }
    
    private Queue<MediaItem> collectMediaItems(List<eu.socialsensor.framework.common.domain.Query> queries, String type , int size){
    	
    	Queue<MediaItem> mediaItems = new LinkedList<MediaItem>();
    	
    	if(queries.isEmpty())
    		return mediaItems;
    
    	//Retrieve multimedia content that is stored in solr
    	for(eu.socialsensor.framework.common.domain.Query query : queries){
    		String queryForRequest = "(title : ("+query.getName()+")) OR (description:("+query.getName()+")) OR (tags:("+query.getName()+"))";
    	
    		SolrQuery solrQuery = new SolrQuery(queryForRequest);
        	solrQuery.setRows(200);
        	solrQuery.setSortField("score", ORDER.desc);
        	Logger.getRootLogger().info("Final query for request: " + queryForRequest);
        	
        	SearchEngineResponse<MediaItem> response = solrMediaItemHandler.findItems(solrQuery);
        	if(response != null){
        		List<MediaItem> results = response.getResults();
        		Set<String> urls = new HashSet<String>();
    	        for(MediaItem mi : results) {
    	        	
    	        	if(mi.getType().equals(type)){
    		        	if(!urls.contains(mi.getUrl())) {
    		        		
    		        		mediaItems.add(mi);
    		        	
    		        		urls.add(mi.getUrl());
    		        	}
    		        	
    		        	if(mediaItems.size() >= size)
    		        		break;
    	        	}
    	        }
        	}
    	}
    	
    	
    	//print media items
    	for(MediaItem mItem : mediaItems){
    		System.out.println("# mitem : "+mItem.getUrl());
    	}
    	
    	return mediaItems;
    }
    
    
    public List<MediaItem> requestThumbnails(Dysco dysco, int size){
    	
    	
    	return null;
    }
   
    public static void main(String[] args) {
    	
    }
}
