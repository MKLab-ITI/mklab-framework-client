package gr.iti.mklab.framework.client.dao;

import java.util.List;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.Source;

public interface SourceDAO {
	
	public void insertSource(String source, float score);

	public void insertSource(Source source);
	
    public void insertSource(String source, float score, SocialNetworkSource sourceType);
    
    public void insertSource(Source source,  SocialNetworkSource sourceType);
    
    public void removeSource(Source source);

    public void removeSource(Source source, SocialNetworkSource sourceType);
    
    public void instertDyscoSource(String dyscoId, String source, float score);

	public List<Source> findTopSources(int n);

	public List<Source> findTopSources(int n, SocialNetworkSource sourceType);
	
	public List<Source> findAllSources();
	
	public List<Source> findListSources(String listId);

}
