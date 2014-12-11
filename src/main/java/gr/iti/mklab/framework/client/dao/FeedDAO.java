package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.feeds.Feed;

/**
 * Data Access Object for Feed
 *
 * @author ailiakop
 * @email ailiakop@iti.gr
 */
public interface FeedDAO {
	
	public void insertFeed(Feed feed);
	
	public boolean deleteFeed(Feed feed);
	
	public Feed getFeed(String id);

}
