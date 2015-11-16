package gr.iti.mklab.framework.client.search.solr.beans;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.NamedEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author 	Manos Schinas
 * @email	manosetro@iti.gr
 * 
 */
public class ItemBean extends Bean {
    
    @Field(value = "source")
    private String source;
    
    @Field(value = "title")
    private String title;
    
    @Field(value = "description")
    private String description;
    
    @Field(value = "tags")
    private String[] tags;
    
    @Field(value = "text")
    private String text;
    
    @Field(value = "uid")
    private String uid;
    
    @Field(value = "reference")
    private String reference;
    
    @Field(value = "publicationTime")
    private long publicationTime;
    
    @Field(value = "latitude")
    private Double latitude;
    
    @Field(value = "longitude")
    private Double longitude;
    
    @Field(value = "latlon")
	private String latlon;
	
    @Field(value = "location")
    private String location;
      
    @Field(value = "language")
    private String language;
    
    @Field(value = "labels")
    private List<String> labels;

    @Field(value = "persons")
    private List<String> persons = new ArrayList<String>();
    
    @Field(value = "locations")
    private List<String> locations = new ArrayList<String>();
    
    @Field(value = "organizations")
    private List<String> organizations = new ArrayList<String>();
    
    @Field(value = "mediaIds")
    private List<String> mediaIds;

    @Field(value = "mentions")
	private List<String> mentions;

	private String referenceUserId;
    
    public ItemBean(Item item) {
        id = item.getId();
        source = item.getSource();
        
        title = item.getTitle();
        description = item.getDescription();
        tags = item.getTags();
    
        text = item.getText();
        
        uid = item.getUserId();
        reference = item.getReference();
        setReferenceUserId(item.getReferencedUserId());
        
        publicationTime = item.getPublicationTime();

        latitude = item.getLatitude();
        longitude = item.getLongitude();
        
        if(latitude != null && longitude != null) {
        	latlon = latitude + "," + longitude;
        }
        
        location = item.getLocationName();
        
        language = item.getLanguage();

        labels = new ArrayList<String>();
        if (item.getLabels() != null) {
        	labels.addAll(item.getLabels());
        }
        
        if(item.getEntities() != null) {
        	for(NamedEntity entity : item.getEntities()) {
        		if(entity.getType().equals(NamedEntity.Type.PERSON)) {
        			persons.add(entity.getName());
        		}
        		if(entity.getType().equals(NamedEntity.Type.LOCATION)) {
        			locations.add(entity.getName());
        		}
        		if(entity.getType().equals(NamedEntity.Type.ORGANIZATION)) {
        			organizations.add(entity.getName());
        		}
        	}
        }
        
        // list of media ids, used as facet to find most shared media items
        mediaIds = item.getMediaIds();
        
        // list of mentions, used as facet to find most mentioned users
        mentions = new ArrayList<String>();
        if(item.getMentions() != null) {
        	mentions.addAll(Arrays.asList(item.getMentions()));
        }
       
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public String getUserId() {
        return uid;
    }

    public void setUserId(String uid) {
        this.uid = uid;
    }

    public Long getPublicationTime() {
        return publicationTime;
    }

    public void setPublicationTime(Long publicationTime) {
        this.publicationTime = publicationTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatLon(Double latitude, Double longitude) {
    	this.latitude = latitude;
        this.longitude = longitude;
        
        this.latlon = latitude + "," + longitude;
    }
    
    public String getLatLon() {
        return latlon;
    }

    public void setLatLon(String latlon) {
        this.latlon = latlon;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void getLabels(List<String> labels) {
        this.labels = labels;
    }

	public String getReferenceUserId() {
		return referenceUserId;
	}

	public void setReferenceUserId(String referenceUserId) {
		this.referenceUserId = referenceUserId;
	}

}
