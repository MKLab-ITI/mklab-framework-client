package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.SocialNetworkSource;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public interface LocationDAO {

    public void insertLocation(String name, double latitude, double longitude);
    public void insertLocation(Location location, SocialNetworkSource sourceType);
    
    public void removeLocation(Location keyword, SocialNetworkSource sourceType);
    public void removeLocation(Location keyword);
}

