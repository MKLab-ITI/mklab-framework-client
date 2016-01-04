package gr.iti.mklab.framework.client.search.visual;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.client.mongo.DAOFactory;
import gr.iti.mklab.framework.client.search.visual.VisualIndexResponse.JsonResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

/**
 * Client for Visual Indexer.
 *
 * @author Schinas Manos - manosetro@iti.gr
 */
public class VisualIndexClient {

    private static double default_threshold = 0.75;
    
    private Logger _logger = LogManager.getLogger(VisualIndexClient.class);
    
    //private String webServiceHost;
    //private String collectionName;

    public VisualIndexClient(String webServiceHost, String collectionName) {
        //this.webServiceHost = webServiceHost;
        //this.collectionName = collectionName;
    }

    public VisualIndexResponse getSimilarImages(String imageId) {
        return getSimilarImages(imageId, default_threshold);
    }

    public VisualIndexResponse getSimilarImages(String imageId, double threshold) {
    	VisualIndexResponse response = new VisualIndexResponse();
    	try {
           //"/rest/visual/query_id/" + collectionName
        } catch (Exception e) {
        	_logger.error("Exception for ID: " + imageId, e);
        }
        return response;
    }

    public VisualIndexResponse getSimilarImages(String imageId, int page, int numResults) {
    	VisualIndexResponse response = new VisualIndexResponse();
        try {
            //"/rest/visual/query_id/" + collectionName
        } catch (Exception e) {
        	_logger.error("Exception for ID: " + imageId, e);
        }
        return response;
    }
    
    public VisualIndexResponse getSimilarImages(URL url) {
    	return getSimilarImages(url, default_threshold);
    }
    
    public VisualIndexResponse getSimilarImages(URL url, double threshold) {
    	VisualIndexResponse similar = new VisualIndexResponse();
        try {
            //queryMethod = new GetMethod(webServiceHost + "/rest/visual/query_url/" + collectionName);        
        } catch (Exception e) {
        	_logger.error("Exception for URL: " + url, e);
        }
        return similar;
    }

    public VisualIndexResponse getSimilarImagesAndIndex(String id, URL url) {
    	return getSimilarImagesAndIndex(id, url, default_threshold);
    }
    
    public VisualIndexResponse getSimilarImagesAndIndex(String id, URL url, double threshold) {
    	VisualIndexResponse similar = new VisualIndexResponse();
        try {
            //queryMethod = new PostMethod(webServiceHost + "/rest/visual/qindex_url/" + collectionName);
        } catch (Exception e) {
        	_logger.error("Exception for URL: " + url, e);
        } 
        
        return similar;
    }
    
    public VisualIndexResponse getSimilarImagesAndIndex(String id, double[] vector, double threshold) {
    	VisualIndexResponse similar = new VisualIndexResponse();
        try {
        	//queryMethod = new PostMethod(webServiceHost + "/rest/visual/qindex/" + collectionName);
        } catch (Exception e) {
        	_logger.error("Exception for vector of length " + vector.length, e);
        }
        
        return similar;
    }
    
    public VisualIndexResponse getSimilarImages(double[] vector, double threshold) {
    	VisualIndexResponse similar = new VisualIndexResponse();
        try {
            //queryMethod = new PostMethod(webServiceHost + "/rest/visual/query_vector/" + collectionName);
        } catch (Exception e) {
        	_logger.error("Exception for vector of length " + vector.length, e);
        } 
        return similar;
    }

    public boolean index(String id, double[] vector) {
        boolean success = false;
        try {  
            //indexMethod = new PostMethod(webServiceHost + "/rest/visual/index/" + collectionName);
            //indexMethod.setRequestEntity(new MultipartRequestEntity(parts, indexMethod.getParams()));
        } catch (Exception e) {
        	_logger.error("Exception for id: " + id, e);
        }
        return success;
    }

    public Double[] getVector(String id) {
        Double[] vector = null;
        try {
            //queryMethod = new GetMethod(webServiceHost + "/rest/visual/vector/" + collectionName);   
            //queryMethod.setQueryString("id="+id);
        } catch (Exception e) {
        	_logger.error("Exception for id: " + id, e);
        } 
        
        return vector;
    }

    public static VisualIndexResponse parseResponse(String response) {
        try {
        	//BasicDBObject object = (BasicDBObject) JSON.parse(response);
        	//VisualIndexResponse indexResults = null;
        	//if (indexResults == null) {
            	return new VisualIndexResponse();
        	//}
        	//return (indexResults.results==null)?(new VisualIndexResponse()):indexResults;
        }
        catch(Exception e) {
        	e.printStackTrace();
        	System.out.println(response);
        	return new VisualIndexResponse();
        }
       
    }

    public static void main(String[] args) throws IOException {
    	
    	
    	VisualIndexClient client = new VisualIndexClient("http://xxx.xxx.xxx.xxx:8080/VisualIndexService", "Prototype");
    	
		BasicDAO<MediaItem, String> dao;
		try {
			dao = new DAOFactory().getDAO("xxx.xxx.xxx.xxx", "Prototype", MediaItem.class);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		
		
    	Query<MediaItem> q = dao.getDatastore().createQuery(MediaItem.class);
    	q.limit(100);
    	q.order("publicationTIme");
    	
		List<MediaItem> mediaItems = dao.find(q).asList();
    	
    	int k = 0;
    	for(MediaItem mediaItem : mediaItems) {
    		String id = mediaItem.getId();
    		String url = mediaItem.getUrl();

    		try {
    			VisualIndexResponse results = client.getSimilarImages(id, 0.8);
    			List<JsonResult> list = results.results;
    			if(list.size()>0) {
    				System.out.println(results);
    				System.out.println("============================");
    			}
    			
    			results = client.getSimilarImages(new URL(url));
    			list = results.results;
    			if(list.size()>0) {
    				System.out.println(results);
    				System.out.println("============================");
    			}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	System.out.println("Total items with results: " + k);
    	
    }
   
}
