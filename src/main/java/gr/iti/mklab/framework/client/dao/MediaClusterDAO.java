package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.MediaCluster;

public interface MediaClusterDAO {

	
	public void addMediaCluster(MediaCluster mediaCluster);
	
	public MediaCluster getMediaCluster(String clusterId);
	
	public void addMediaItemInCluster(String clusterId, String memberId);

}
