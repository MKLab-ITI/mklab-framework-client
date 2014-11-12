package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.influencers.Influencer;
import java.util.List;

/**
 *
 * @author etzoannos
 */
public interface InfluencerDAO {

    public void addInfluencersForKeyword(String keyword, List<Influencer> influencers);

    public List<Influencer> getInfluencersForKeyword(String keyword);

    public List<Influencer> getInfluencersForKeywords(List<String> keywords);
}
