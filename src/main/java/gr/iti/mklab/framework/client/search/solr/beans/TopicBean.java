package gr.iti.mklab.framework.client.search.solr.beans;

import org.apache.solr.client.solrj.beans.Field;

import gr.iti.mklab.framework.common.domain.Topic;

public class TopicBean extends Bean {

    @Field(value = "topic")
    private String topic;
    
    @Field(value = "score")
    private double score;
    
    
	public TopicBean() {
	}
	
	public TopicBean(Topic topic) {
		this.topic = topic.getTopic();
		this.score = topic.getScore();
	}
	
}
