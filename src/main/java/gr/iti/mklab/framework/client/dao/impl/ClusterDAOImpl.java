package gr.iti.mklab.framework.client.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import gr.iti.mklab.framework.common.domain.Cluster;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.ClusterDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;

public class ClusterDAOImpl implements ClusterDAO {

	private List<String> indexes = new ArrayList<String>();
	private MongoHandler mongoHandler;
	
	public ClusterDAOImpl(String host, String db, String collection) {
		indexes.add("id");
		
        try {
			mongoHandler = new MongoHandler(host, db, collection, indexes);
		} catch (Exception e) {
			e.printStackTrace();
		}
   
    }

	@Override
	public void addCluster(Cluster mediaCluster) {
		mongoHandler.insert(mediaCluster);
	}

	@Override
	public Cluster getCluster(String clusterId) {
		String json = mongoHandler.findOne("id", clusterId);
		Cluster mediaCluster = ObjectFactory.createCluster(json);
		return mediaCluster;
	}
	
	@Override
	public void addItemInCluster(String clusterId, String memberId) {
		DBObject update = new BasicDBObject();
		update.put("$addToSet", new BasicDBObject("members", memberId));
		update.put("$inc", new BasicDBObject("count", 1));
	
		mongoHandler.update("id", clusterId, update);
	}

	public static void main(String...args) {
		
		String q = "4c423695-823e-45ae-ba15-877488fd7dfb";
		
		ClusterDAO dao = new ClusterDAOImpl("xxx.xxx.xxx.xxx","Prototype","MediaClusters");
		
		Cluster mediaCluster = dao.getCluster(q);
		System.out.println(mediaCluster.toJSONString());
		
	}

}
