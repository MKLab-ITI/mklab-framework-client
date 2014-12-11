package gr.iti.mklab.framework.client.dao;

import java.util.List;

import gr.iti.mklab.framework.common.domain.SocialNetwork;
import gr.iti.mklab.framework.common.domain.Account;

public interface AccountDAO {
	
	public void insertAccount(String source, float score);

	public void insertAccount(Account source);
	
    public void insertAccount(String source, float score, SocialNetwork sourceType);
    
    public void insertAccount(Account source,  SocialNetwork sourceType);
    
    public void removeAccount(Account source);

    public void removeAccount(Account source, SocialNetwork sourceType);
    
    public void instertDyscoAccount(String dyscoId, String source, float score);

	public List<Account> findTopAccounts(int n);

	public List<Account> findTopAccounts(int n, SocialNetwork sourceType);
	
	public List<Account> findAllAccounts();
	
	public List<Account> findListAccounts(String listId);

}
