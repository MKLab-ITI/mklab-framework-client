package gr.iti.mklab.framework.client.dao;

import java.util.List;

import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.Account;

public interface AccountDAO {
	
	public void insertAccount(String source, float score);

	public void insertAccount(Account source);
	
    public void insertAccount(String source, float score, Source sourceType);
    
    public void insertAccount(Account source,  Source sourceType);
    
    public void removeAccount(Account source);

    public void removeAccount(Account source, Source sourceType);

	public List<Account> findTopAccounts(int n);

	public List<Account> findTopAccounts(int n, Source sourceType);
	
	public List<Account> findAllAccounts();
	
	public List<Account> findListAccounts(String listId);

}
