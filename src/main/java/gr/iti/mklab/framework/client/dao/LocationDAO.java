package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.Source;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public interface LocationDAO {

    public void insertLocation(String name, double latitude, double longitude);
    public void insertLocation(Location location, Source sourceType);
    
    public void removeLocation(Location keyword, Source sourceType);
    public void removeLocation(Location keyword);
}

