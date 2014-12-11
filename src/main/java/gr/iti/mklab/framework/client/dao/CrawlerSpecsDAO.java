package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.Keyword;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.SocialNetwork;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;

import java.util.List;

/**
 *
 * @author etzoannos
 */
public interface CrawlerSpecsDAO {

    public List<SocialNetwork> getSources();

    public List<Keyword> getTopKeywords(int count, SocialNetwork sourceType);

    public List<Account> getTopAccounts(int count);
    
    public List<Account> getTopAccounts(int count, SocialNetwork sourceType);
    
    public List<Dysco> getTopDyscos(int count);
    
    public void setKeywords(List<Keyword> keywords, SocialNetwork sourceType);
    
    public void setAccounts(List<Account> accounts, SocialNetwork sourceType);
    
    public void setLocations(List<Location> locations, SocialNetwork sourceType);
    
    public void removeKeywords(List<Keyword> keywords,SocialNetwork sourceType);
    
    public void removeAccounts(List<Account> accounts, SocialNetwork sourceType);
    
    public void removeLocations(List<Location> locations, SocialNetwork sourceType);
}
