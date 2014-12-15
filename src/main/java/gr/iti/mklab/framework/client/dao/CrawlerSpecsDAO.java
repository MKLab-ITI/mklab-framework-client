package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;

import java.util.List;

/**
 *
 * @author etzoannos
 */
public interface CrawlerSpecsDAO {

    public List<Source> getSources();

    public List<Account> getTopAccounts(int count);
    
    public List<Account> getTopAccounts(int count, Source sourceType);
    
    public List<Dysco> getTopDyscos(int count);
    
    public void setAccounts(List<Account> accounts, Source sourceType);
    
    public void setLocations(List<Location> locations, Source sourceType);
    
    public void removeAccounts(List<Account> accounts, Source sourceType);
    
    public void removeLocations(List<Location> locations, Source sourceType);
}
