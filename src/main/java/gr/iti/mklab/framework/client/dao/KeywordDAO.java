package gr.iti.mklab.framework.client.dao;

import java.util.List;

import gr.iti.mklab.framework.common.domain.Keyword;
import gr.iti.mklab.framework.common.domain.SocialNetwork;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public interface KeywordDAO {

    public void insertKeyword(String keyword, double score);

    public void insertKeyword(String keyword, double score, SocialNetwork sourceType);
    
    public void insertKeyword(Keyword keyword, SocialNetwork sourceType);
    
    public void removeKeyword(String keyword);

    public void removeKeyword(String keyword, SocialNetwork sourceType);
    
    public void instertDyscoKeyword(String dyscoId, String keyword, float score);

	public List<Keyword> findTopKeywords(int n);
	
	public List<Keyword> findKeywords(SocialNetwork sourceType);
    
}
