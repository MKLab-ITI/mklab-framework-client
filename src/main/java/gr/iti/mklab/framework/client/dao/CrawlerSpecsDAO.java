package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Keyword;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.SocialNetworkSource;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import java.util.List;

/**
 *
 * @author etzoannos
 */
public interface CrawlerSpecsDAO {

    public List<SocialNetworkSource> getSources();

    public List<Keyword> getTopKeywords(int count, SocialNetworkSource sourceType);

    public List<Source> getTopSources(int count);
    
    public List<Source> getTopSources(int count, SocialNetworkSource sourceType);
    
    public List<Dysco> getTopDyscos(int count);
    
    public void setKeywords(List<Keyword> keywords, SocialNetworkSource sourceType);
    
    public void setSources(List<Source> sources, SocialNetworkSource sourceType);
    
    public void setLocations(List<Location> locations, SocialNetworkSource sourceType);
    
    public void removeKeywords(List<Keyword> keywords,SocialNetworkSource sourceType);
    
    public void removeSources(List<Source> sources, SocialNetworkSource sourceType);
    
    public void removeLocations(List<Location> locations, SocialNetworkSource sourceType);
}
