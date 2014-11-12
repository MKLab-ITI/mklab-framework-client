package gr.iti.mklab.framework.client.dao.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import gr.iti.mklab.framework.common.domain.Expert;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.ExpertDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;
import gr.iti.mklab.framework.client.mongo.Selector;

public class ExpertDAOImpl implements ExpertDAO {

	List<String> indexes = new ArrayList<String>();

    private static String db = "Streams";
    private static String collection = "Experts";
    private MongoHandler mongoHandler;

    
    public ExpertDAOImpl(String host) {
    	this(host, db, collection);
    }

    public ExpertDAOImpl(String host, String db, String collection) {
        try {
            indexes.add("id");
            indexes.add("category");
            mongoHandler = new MongoHandler(host, db, collection, indexes);
        } catch (UnknownHostException ex) {
            org.apache.log4j.Logger.getRootLogger().error(ex.getMessage());
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
	public void insertExpert(Expert expert) {
		mongoHandler.insert(expert);
	}

	@Override
	public void removeExpert(Expert expert) {
		mongoHandler.delete("id", expert.getId());
	}

	@Override
	public List<Expert> getExperts() {
		List<Expert> experts = new ArrayList<Expert>();
		List<String> res = mongoHandler.findMany(-1);
		for(String json : res) {
			experts.add(ObjectFactory.createExpert(json));
		}
		return experts;
	}

	@Override
	public Expert getExpert(String id) {
		Selector query = new Selector();
		query.select("id", id);
		String json = mongoHandler.findOne(query);
		return ObjectFactory.createExpert(json);
	}

	public static void main(String...args) {
		ExpertDAO dao = new ExpertDAOImpl("social1.atc.gr");
		
		List<Expert> experts = dao.getExperts();
		for(Expert ex : experts) {
			System.out.println(ex.toJSONString());
		}
	}
}
