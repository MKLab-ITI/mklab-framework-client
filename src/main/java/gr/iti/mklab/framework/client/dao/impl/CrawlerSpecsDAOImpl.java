package gr.iti.mklab.framework.client.dao.impl;

import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.Keyword;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.client.dao.AccountDAO;
import gr.iti.mklab.framework.client.dao.CrawlerSpecsDAO;
import gr.iti.mklab.framework.client.dao.KeywordDAO;
import gr.iti.mklab.framework.client.dao.LocationDAO;

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
		
		keywords.add(new Keyword("champions league"));
		dao.setKeywords(keywords, Source.Twitter);
		
	}
	
	KeywordDAO keywordDAO;
	LocationDAO locationDAO;
	AccountDAO sourceDAO;

	
	public CrawlerSpecsDAOImpl(String host, String db, 
			String keywords, String users, String locations) {
		keywordDAO = new KeywordDAOImpl(host, db, keywords);
		locationDAO = new LocationDAOImpl(host, db, locations);
		sourceDAO = new AccountDAOImpl(host, db, users);
	}
	
	public CrawlerSpecsDAOImpl(String host, String db) {
		this(host, db, "keywords", "users", "locations");
	}
	
//	public CrawlerSpecsDAOImpl() {
//		this("", "CrawlerSpecs", "keywords", "users", "locations");
//	}
	
    @Override
    public List<Keyword> getTopKeywords(int count, Source sourceType) {
    	return keywordDAO.findTopKeywords(count);
    }
    
    @Override
    public List<Account> getTopAccounts(int count) {
    	return sourceDAO.findTopAccounts(count);
    }
    
    @Override
    public List<Account> getTopAccounts(int count, Source sourceType) {
    	return sourceDAO.findTopAccounts(count);
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
    public List<Source> getSources() {
        return null;
    }

	@Override
	public void setKeywords(List<Keyword> keywords, Source sourceType) {
		for(Keyword keyword : keywords) {
			keywordDAO.insertKeyword(keyword.getName(), 0, sourceType);
		}
		
	}

	@Override
	public void removeKeywords(List<Keyword> keywords, Source sourceType) {
		for(Keyword keyword : keywords) {
			keywordDAO.removeKeyword(keyword.getName(), sourceType);
		}
	}
	
	@Override
	public void setAccounts(List<Account> accounts, Source sourceType) {
		for(Account account : accounts) {
			sourceDAO.insertAccount(account, sourceType);
		}
	}

	@Override
	public void removeAccounts(List<Account> accounts, Source sourceType) {
		for(Account account : accounts) {
			sourceDAO.removeAccount(account, sourceType);
		}
	}
	
	@Override
	public void setLocations(List<Location> locations, Source sourceType) {
		for(Location location : locations) {
			locationDAO.insertLocation(location, sourceType);
		}
	}

	@Override
	public void removeLocations(List<Location> locations, Source sourceType) {
		for(Location location : locations) {
			locationDAO.removeLocation(location,  sourceType);
		}
	}
	
}
