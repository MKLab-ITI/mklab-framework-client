package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.Location;

import java.net.MalformedURLException;
import java.net.URL;
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
public class SolrItem {

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
    
    @Field(value = "links")
    private List<String> links;
    
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
    
    @Field(value = "sentiment")
    private String sentiment;
    
    @Field(value = "language")
    private String language;
    
    @Field(value = "original")
    private boolean original;
    
    @Field(value = "lists")
    private List<String> lists;
    
    //popularity fields
    @Field(value = "likes")
    private Long likes = 0L;
    
    @Field(value = "shares")
    private Long shares = 0L;
    
    @Field(value = "comments")
    private Long comments = 0L;

    public SolrItem() {
    	
    }

    public SolrItem(Item item) {

        id = item.getId();
        source = item.getSource();
        title = item.getTitle();
        description = item.getDescription();
        tags = item.getTags();
    
        uid = item.getUserId();

        links = new ArrayList<String>();
        if (item.getLinks() != null) {
            for (URL link : item.getLinks()) {
                links.add(link.toString());
            }
        }

        //this is long
        publicationTime = item.getPublicationTime();

        latitude = item.getLatitude();
        longitude = item.getLongitude();
        location = item.getLocationName();
        sentiment = item.getSentiment();
        language = item.getLanguage();

        //this is a map
        mediaIds = new ArrayList<String>();
        if (item.getMediaIds() != null) {
        	mediaIds.addAll(item.getMediaIds());
        }
        
        original = item.isOriginal();

        lists = new ArrayList<String>();
        if (item.getList() != null) {
            lists.addAll(Arrays.asList(item.getList()));
        }

        comments = item.getComments();
        shares = item.getShares();
        likes = item.getLikes();
        
    }

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
    
    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getShares() {
        return shares;
    }

    public void setShares(Long shares) {
        this.shares = shares;
    }

    public Long getComments() {
        return comments;
    }

    public void setComments(Long comments) {
        this.comments = comments;
    }

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

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
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

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public List<String> getLists() {
        return lists;
    }

    public void setLists(List<String> lists) {
        this.lists = lists;
    }

}
