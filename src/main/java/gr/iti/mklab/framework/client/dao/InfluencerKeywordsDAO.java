package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Keyword;
import java.util.List;

/**
 *
 * @author stzoannos
 */
public interface InfluencerKeywordsDAO {

    public void addKeywordsForInfluencer(String influencer, List<Keyword> keywords);

    public List<Keyword> getKeywordsForInfluencer(String influencer);

}
