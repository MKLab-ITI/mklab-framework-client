package gr.iti.mklab.framework.client.dao.impl;

import gr.iti.mklab.framework.common.domain.Keyword;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.SocialNetworkSource;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.client.dao.CrawlerSpecsDAO;
import gr.iti.mklab.framework.client.dao.KeywordDAO;
import gr.iti.mklab.framework.client.dao.LocationDAO;
import gr.iti.mklab.framework.client.dao.SourceDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author etzoannos - e.tzoannos@atc.gr
 */
public class CrawlerSpecsDAOImpl implements CrawlerSpecsDAO {
	
	
	public static void main(String[] args) {
		String host = null;
		String db = "testDB";
		
		CrawlerSpecsDAO dao = new CrawlerSpecsDAOImpl(host, db, "keywords", "Feeds", "locations");
		
		List<Keyword> keywords = new ArrayList<Keyword>();
		
		keywords.add(new Keyword("champions league", 0.79f));
		dao.setKeywords(keywords, SocialNetworkSource.Twitter);
		
	}
	
	KeywordDAO keywordDAO;
	LocationDAO locationDAO;
	SourceDAO sourceDAO;

	
	public CrawlerSpecsDAOImpl(String host, String db, 
			String keywords, String users, String locations) {
		keywordDAO = new KeywordDAOImpl(host, db, keywords);
		locationDAO = new LocationDAOImpl(host, db, locations);
		sourceDAO = new SourceDAOImpl(host, db, users);
	}
	
	public CrawlerSpecsDAOImpl(String host, String db) {
		this(host, db, "keywords", "users", "locations");
	}
	
//	public CrawlerSpecsDAOImpl() {
//		this("", "CrawlerSpecs", "keywords", "users", "locations");
//	}
	
    @Override
    public List<Keyword> getTopKeywords(int count, SocialNetworkSource sourceType) {
    	return keywordDAO.findTopKeywords(count);
    }
    
    @Override
    public List<Source> getTopSources(int count) {
    	return sourceDAO.findTopSources(count);
    }
    
    @Override
    public List<Source> getTopSources(int count, SocialNetworkSource sourceType) {
    	return sourceDAO.findTopSources(count);
    }

    @Override
    public List<Dysco> getTopDyscos(int count) {
//        SearchEngineHandler solrHandler = new SolrHandler();
//        SearchEngineResponse<Dysco> searchResponse =  solrHandler.findDyscosLight("*:*", "1DAY", 10);
//        List<Dysco> dyscosLight = searchResponse.getResults();
//        return dyscosLight;
    	return null;
    }
    
    

    @Override
    public List<SocialNetworkSource> getSources() {
        return null;
    }

	@Override
	public void setKeywords(List<Keyword> keywords, SocialNetworkSource sourceType) {
		for(Keyword keyword : keywords) {
			keywordDAO.insertKeyword(keyword.getName(), keyword.getScore(), sourceType);
		}
		
	}

	@Override
	public void removeKeywords(List<Keyword> keywords, SocialNetworkSource sourceType) {
		for(Keyword keyword : keywords) {
			keywordDAO.removeKeyword(keyword.getName(), sourceType);
		}
	}
	
	@Override
	public void setSources(List<Source> sources, SocialNetworkSource sourceType) {
		for(Source source : sources) {
			sourceDAO.insertSource(source, sourceType);
		}
	}

	@Override
	public void removeSources(List<Source> sources, SocialNetworkSource sourceType) {
		for(Source source : sources) {
			sourceDAO.removeSource(source, sourceType);
		}
	}
	
	@Override
	public void setLocations(List<Location> locations, SocialNetworkSource sourceType) {
		for(Location location : locations) {
			locationDAO.insertLocation(location, sourceType);
		}
	}

	@Override
	public void removeLocations(List<Location> locations, SocialNetworkSource sourceType) {
		for(Location location : locations) {
			locationDAO.removeLocation(location,  sourceType);
		}
	}
	
}
