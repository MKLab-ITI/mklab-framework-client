package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.profile.Profile;
import gr.iti.mklab.framework.common.profile.ScoredItem;
import gr.iti.mklab.framework.common.profile.User;
import java.util.List;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public interface UserRecommendationsDAO {

    public void addUserProfiles(User user, List<Profile> profiles);

    public List<Profile> getUserProfiles(User user);

    public void addUserRecommendations(User user, List<ScoredItem> items);

    public List<ScoredItem> getUserRecommendations(User user);
}
