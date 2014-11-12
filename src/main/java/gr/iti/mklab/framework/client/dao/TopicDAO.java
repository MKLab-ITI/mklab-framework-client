package gr.iti.mklab.framework.client.dao;

import java.util.List;

import eu.socialsensor.framework.common.domain.Topic;



public interface TopicDAO {
	 
	 public void updateTopic(Topic topic);
	 
	 public List<Topic> readTopicsByStatus();
	 
	 public boolean deleteDB();
}
