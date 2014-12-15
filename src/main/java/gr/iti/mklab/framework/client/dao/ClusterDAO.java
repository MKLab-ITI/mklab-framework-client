package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Cluster;

public interface ClusterDAO {

	
	public void addCluster(Cluster cluster);
	
	public Cluster getCluster(String clusterId);
	
	public void addItemInCluster(String clusterId, String memberId);

}
