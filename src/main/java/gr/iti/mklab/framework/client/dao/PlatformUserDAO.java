package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.PlatformUser;

/**
 *
 * @author stzoannos
 */
public interface PlatformUserDAO {

    public void addPlatformUser(PlatformUser user);

    public PlatformUser getPlatformUser(String name);

    public void updatePlatformUser(PlatformUser user);
    
}
