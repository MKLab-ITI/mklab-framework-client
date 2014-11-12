package gr.iti.mklab.framework.client.dao;

import java.util.List;

import gr.iti.mklab.framework.common.domain.Expert;

public interface ExpertDAO {
	
	public void insertExpert(Expert expert);
    
    public void removeExpert(Expert expert);

	public List<Expert> getExperts();

	public Expert getExpert(String id);

}
