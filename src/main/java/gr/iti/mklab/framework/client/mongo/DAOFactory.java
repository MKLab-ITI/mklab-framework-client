package gr.iti.mklab.framework.client.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;

import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 *
 * @author Manos Schinas
 * @email  manosetro@iti.gr
 *
 */
public class DAOFactory {

    public static int ASC = 1;
    public static int DESC = -1;
    
    private static MongoClientOptions options = MongoClientOptions.builder()
            .writeConcern(WriteConcern.UNACKNOWLEDGED).build();
    
    private static Map<String, MongoClient> connections = new HashMap<String, MongoClient>();
    private static Map<String, Datastore> datastores = new HashMap<String, Datastore>();

    public <K> BasicDAO<K, String> getDAO(String hostname, String dbName, Class<K> clazz) throws Exception {
    	
        String connectionKey = hostname + "#" + dbName;
        Datastore ds = datastores.get(connectionKey);
        
        Morphia morphia = new Morphia();
        MongoClient mongoClient = connections.get(hostname);

        if (mongoClient == null) {
        	mongoClient = new MongoClient(hostname, options);
            connections.put(hostname, mongoClient);
        }
        
        if (ds == null) {
            ds = morphia.createDatastore(mongoClient, dbName);
            datastores.put(connectionKey, ds);
        }
		
        return new BasicDAO<K, String>(clazz, mongoClient, morphia, dbName);
    }

}
