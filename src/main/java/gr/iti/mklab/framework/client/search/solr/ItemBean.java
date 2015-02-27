package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.Item;

import java.lang.reflect.Array;
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
public class ItemBean {

	@Field(value = "id")
    private String id;
    
    @Field(value = "source")
    private String source;
    
    @Field(value = "title")
    private String title;
    
    @Field(value = "description")
    private String description;
    
    @Field(value = "tags")
    private String[] tags;
    
    @Field(value = "uid")
    private String uid;
    
    @Field(value = "publicationTime")
    private long publicationTime;
    
    @Field(value = "latitude")
    private Double latitude;
    
    @Field(value = "longitude")
    private Double longitude;
    
    @Field(value = "location")
    private String location;
    
    @Field(value = "mediaIds")
    private List<String> mediaIds;
    
    @Field(value = "language")
    private String language;
    
    @Field(value = "original")
    private boolean original;
    
    @Field(value = "labels")
    private List<String> labels;
    
    public ItemBean() {
    	
    }

    public ItemBean(Item item) {

        id = item.getId();
        source = item.getSource();
        title = item.getTitle();
        description = item.getDescription();
        tags = item.getTags();
    
        uid = item.getUserId();

        //this is long
        publicationTime = item.getPublicationTime();

        latitude = item.getLatitude();
        longitude = item.getLongitude();
        location = item.getLocationName();
        language = item.getLanguage();

        //this is a map
        mediaIds = new ArrayList<String>();
        if (item.getMediaIds() != null) {
        	mediaIds.addAll(item.getMediaIds());
        }
        
        original = item.isOriginal();

        labels = new ArrayList<String>();
        if (item.getList() != null) {
        	labels.addAll(Arrays.asList(item.getList()));
        }
        
    }

    /*
    public Item toItem() throws MalformedURLException {

        Item item = new Item();

        item.setComments(comments);
        item.setLikes(likes);
        item.setShares(shares);

        item.setId(id);
        item.setSource(source);
        item.setTitle(title);
        item.setDescription(description);
        item.setTags(tags);
        item.setOriginal(original);

        if (links != null) {
            URL[] _links = new URL[links.size()];
            for (int i = 0; i < links.size(); i++) {
                _links[i] = new URL(links.get(i));
            }
            item.setLinks(_links);
        }

        item.setPublicationTime(publicationTime);

        if (latitude != null && longitude != null) {
            item.setLocation(new Location(latitude, longitude, location));
        } else {
            item.setLocation(new Location(location));
        }
        
        if (mediaIds != null) {
            item.setMediaIds(mediaIds);
        }
        
        item.setLanguage(language);

        return item;
    }
    */

    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(List<String> mediaIds) {
        this.mediaIds = mediaIds;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void getLabels(List<String> labels) {
        this.labels = labels;
    }

}
