package gr.iti.mklab.framework.client.dao.impl;

import gr.iti.mklab.framework.common.domain.PlatformUser;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.PlatformUserDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 *
 * @author stzoannos
 */
public class PlatformUserDAOImpl implements PlatformUserDAO, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -972157425643431691L;
	
	List<String> indexes = new ArrayList<String>();
    private static String db = "Streams";
    private static String collection = "PlatformUsers";
    private MongoHandler mongoHandler;

    @Override
    public void addPlatformUser(PlatformUser user) {
        mongoHandler.insert(user);
    }

    @Override
    public void updatePlatformUser(PlatformUser user) {
        mongoHandler.update("name", user.getName(), user);
    }

    @Override
    public PlatformUser getPlatformUser(String name) {

        String json = mongoHandler.findOne("name", name);
        PlatformUser user = ObjectFactory.createPlatformUser(json);
        return user;
    }

    public PlatformUserDAOImpl(String host) {
        this(host, db, collection);
    }

    public PlatformUserDAOImpl(String host, String db) {
        this(host, db, collection);
    }

    public PlatformUserDAOImpl(String host, String db, String collection) {
        indexes.add("name");

        try {
            mongoHandler = new MongoHandler(host, db, collection, indexes);
        } catch (Exception ex) {
            Logger.getRootLogger().error(ex.getMessage());
        }
    }
}
