package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.NamedEntity;
import gr.iti.mklab.framework.common.domain.NamedEntity.Type;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public class SolrDysco implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5212027040002311878L;

	public final Logger logger = Logger.getLogger(SolrDysco.class);

    //The id of the dysco
    @Field(value = "id")
    private String id;
    
    //The creation date of the dysco
    @Field(value = "creationDate")
    private Date creationDate;
    
    //The date that the dysco was last updates
    @Field(value = "updateDate")
    private Date updateDate;
    
    //The title of the dysco
    @Field(value = "title")
    private String title;
    
    //The title of the dysco
    @Field(value = "text")
    private String text;
    
    
    //The users that contribute in social networks to dysco's topic
    private List<String> contributors = new ArrayList<String>();
    
    //The extracted keywords from items' content with their assigned weights
    @Field(value = "keywords")
    private List<String> keywords = new ArrayList<String>();
    
    //The extracted hashtags from items' content with their assigned weights
    @Field(value = "tags")
    private List<String> tags = new ArrayList<String>();
    
    //The extracted entities from items' content
    @Field(value = "persons")
    private List<String> persons = new ArrayList<String>();
    @Field(value = "locations")
    private List<String> locations = new ArrayList<String>();
    @Field(value = "organizations")
    private List<String> organizations = new ArrayList<String>();

    @Field(value = "links")
    private List<String> links = new ArrayList<String>();

    //The score that shows how trending the dysco is
    @Field(value = "dyscoScore")
    private Double score = 0d;
    
    @Field(value = "itemsCount")
    private int itemsCount = 0;

    public SolrDysco() {
        id = UUID.randomUUID().toString();
    }

    public SolrDysco(Dysco dysco) {

        id = dysco.getId();
        creationDate = dysco.getCreationDate();
        title = dysco.getTitle();
        score = dysco.getScore();

        List<NamedEntity> dyscoEntities = dysco.getEntities();
        for (NamedEntity entity : dyscoEntities) {
            if (entity.getType().equals(Type.LOCATION)) {
                locations.add(entity.getName());
            }
            if (entity.getType().equals(Type.PERSON)) {
                persons.add(entity.getName());
            }
            if (entity.getType().equals(Type.ORGANIZATION)) {
                organizations.add(entity.getName());
            }
        }

        contributors = dysco.getContributors();

        for (Map.Entry<String, Double> entry : dysco.getKeywords().entrySet()) {
            keywords.add(entry.getKey());
        }

        for (Map.Entry<String, Double> entry : dysco.getTags().entrySet()) {
            tags.add(entry.getKey());
        }

        updateDate = dysco.getUpdateDate();
        
        itemsCount = dysco.getItemsCount();

        if (dysco.getLinks() != null) {
            links = new ArrayList<String>();
            for (Entry<String, Double> e : dysco.getLinks().entrySet()) {
                links.add(e.getValue() + "@#@#" + e.getKey());
            }
        }

    }

    public Dysco toDysco() {

        Dysco dysco = new Dysco(id, creationDate);

        dysco.setTitle(title);
        dysco.setScore(score);

        dysco.setContributors(contributors);

        if (keywords != null) {
            for (String keyword : keywords) {
                dysco.addKeyword(keyword, 0.0);
            }
        }

        if (tags != null) {
            for (String tag : tags) {
                dysco.addHashtag(tag, 0.0);
            }
        }
        
        dysco.setUpdateDate(updateDate);

        if (links != null) {
            Map<String, Double> _links = new HashMap<String, Double>();
            for (String s : links) {
                String[] parts = s.split("@#@#");
                if (parts.length != 2) {
                    continue;
                }

                _links.put(parts[1], new Double(parts[0]));
            }
            dysco.setLinks(_links);
        }


            if (persons != null) {
                for (String person : persons) {
                	NamedEntity dyscoEntity = new NamedEntity(person, 0.0, Type.PERSON);
                    dysco.addEntity(dyscoEntity);
                }
            }
            if (locations != null) {
                for (String location : locations) {
                	NamedEntity dyscoEntity = new NamedEntity(location, 0.0, Type.LOCATION);
                    dysco.addEntity(dyscoEntity);
                }
            }
            if (organizations != null) {
                for (String organization : organizations) {
                	NamedEntity dyscoEntity = new NamedEntity(organization, 0.0, Type.ORGANIZATION);
                    dysco.addEntity(dyscoEntity);
                }
            }


            //new fields
           dysco.setItemsCount(itemsCount);


        return dysco;

    }

    /**
     * Returns the id of the dysco
     *
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the dysco
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the creation date of the dysco
     *
     * @return Date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date of the dysco
     *
     * @param creationDate
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the title of the dysco
     *
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the dysco
     *
     * @param Title
     */
    public void setTitle(String Title) {
        this.title = Title;
    }

    /**
     * Returns the score of the dysco
     *
     * @return Float
     */
    public Double getScore() {
        return score;
    }

    /**
     * Sets the score of the dysco
     *
     * @param score
     */
    public void setScore(Double score) {
        this.score = score;
    }
    
    /**
     * Returns the list of names of the Entities that are Persons inside the
     * dysco
     *
     * @return List of String
     */
    public List<String> getPersons() {
        return persons;
    }

    /**
     * Sets the list of names of the Entities that are Persons inside the dysco
     *
     * @param persons
     */
    public void setPersons(List<String> persons) {
        this.persons = persons;
    }

    /**
     * Returns the list of names of the Entities that are Locations inside the
     * dysco
     *
     * @return
     */
    public List<String> getLocations() {
        return locations;
    }

    /**
     * Sets the list of names of the Entities that are Locations inside the
     * dysco
     *
     * @param locations
     */
    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    /**
     * Returns the list of names of the Entities that are Organizations inside
     * the dysco
     *
     * @return List of String
     */
    public List<String> getOrganizations() {
        return organizations;
    }

    /**
     * Sets the list of names of the Entities that are Organizations inside the
     * dysco
     *
     * @param organizations
     */
    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    /**
     * Returns the list of contributors for the dysco
     *
     * @return List of String
     */
    public List<String> getContributors() {
        return contributors;
    }

    /**
     * Sets the contributors for the dysco
     *
     * @param contributors
     */
    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    /**
     * Returns the keywords of the dysco
     *
     * @return List of String
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Sets the keywords of the dysco
     *
     * @param keywords
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Returns the hashtags of the dysco
     *
     * @return List of String
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the hashtags of the dysco
     *
     * @param hashtags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the date that dysco was last updated.
     *
     * @return
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * Sets the date that dysco was last updated.
     *
     * @return
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
