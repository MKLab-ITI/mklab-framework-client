package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Utils {

	public static String buildKeywordSolrQuery(List<Query> queries, String liaison) {
        
    	Map<String, List<String>> linkedWords = new HashMap<String, List<String>>();
        List<Query> swingQueries = new ArrayList<Query>();

        String solrQuery = null;

        for (Query query : queries) {
        	
            //store these queries for later
            if (query.getName().startsWith("\"") && (query.getName().endsWith("\"") || query.getName().endsWith("\" "))) {
                //System.out.println("entity query : " + query.getName());
                if (query.getName().endsWith("\" ")) {
                    query.setName(query.getName().substring(0, query.getName().length() - 1));
                }
                
                // Handle combined entities
                String[] queryParts = query.getName().split("\"\\s\"");
                if(queryParts != null) {
                	String name = StringUtils.join(queryParts, "\" AND \"");
                	//System.out.println("name : " + name);
                	swingQueries.add(new Query(name, query.getScore()));
                }
            } 
            else {
                List<String> entities = new ArrayList<String>();
                String restQuery = query.getName();
                int start = 0, end = 0;

                while (start != -1 && end != -1) {
                    start = restQuery.indexOf("\"");
                    //System.out.println("start:"+start);
                    if (start == -1) {
                        break;
                    }
                    String temp = restQuery.substring(start + 1);
                    //System.out.println("temp:"+temp);

                    end = temp.indexOf("\"") + start + 1;

                    //System.out.println("end:"+(end));
                    if (end == -1) {
                        break;
                    }
                    end += 1;
                    String entity = restQuery.substring(start, end);
                    
                    if(entity == null)
                    	break;
                    		
                    if((entity.length()>2 && (!entity.startsWith("\"") || !entity.endsWith("\"")))
                    		|| entity.length()<=2) {
                    	restQuery = restQuery.replace(entity, "").trim();
                    	continue;
                    }
                    restQuery = restQuery.replace(entity, "").trim();
                    
                    entities.add(entity);
                }
                restQuery = restQuery.replaceAll(" +", " ");
                restQuery = restQuery.replace("[^A-Za-z0-9 ]", "");

                // Modify (termA termB) to (termA AND termB)
                restQuery = restQuery.trim();
                restQuery = restQuery.replaceAll("\\s+", " AND ");

                for (String entity : entities) {
                    String queryToLink = restQuery;
                    if (!linkedWords.containsKey(entity)) {
                        List<String> alreadyIn = new ArrayList<String>();

                        if (query.getScore() != null) {
                            //queryToLink += "^" + query.getScore();    
                            queryToLink = "("+queryToLink+")^" + query.getScore();
                        }

                        alreadyIn.add(queryToLink);
                        linkedWords.put(entity, alreadyIn);

                    } else {
                        List<String> alreadyIn = linkedWords.get(entity);
                        if (query.getScore() != null) {
                            //queryToLink += "^" + query.getScore();
                            queryToLink = "("+queryToLink+")^" + query.getScore();
                        }
                        if (!alreadyIn.contains(queryToLink)) {
                            alreadyIn.add(queryToLink);
                            linkedWords.put(entity, alreadyIn);
                        }
                    }
                }

                
                if (entities.isEmpty()) {
                    if (solrQuery == null) {
                        if (query.getScore() != null) {
                            solrQuery = "(" + restQuery + ")^" + query.getScore();
                        } else {
                            solrQuery = "(" + restQuery + ")";
                        }
                    }
                    else {
                        if (!solrQuery.contains(restQuery)) {
                            if (query.getScore() != null) {
                                solrQuery += " " + liaison + " (" + restQuery + ")^" + query.getScore();
                            } else {
                                solrQuery += " " + liaison + " (" + restQuery + ")";
                            }
                        }
                    }
                }
       
            }
        }

        for (Map.Entry<String, List<String>> entry : linkedWords.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                String resQuery = entry.getKey() + " AND (";
                boolean first = true;
                for (String lWord : entry.getValue()) {
                    if (first) {
                        resQuery += lWord;
                        first = false;
                    } else {
                        resQuery += " OR " + lWord;
                    }
                }

                resQuery += ")";

                if (solrQuery == null) {
                    solrQuery = "(" + resQuery + ")";
                } else {
                    if (!solrQuery.contains(resQuery)) {
                        solrQuery += " " + liaison + " (" + resQuery + ")";
                    }
                }
            }
        }

        for (Query sQuery : swingQueries) {
            if (solrQuery == null) {
                if (sQuery.getScore() != null) {
                    solrQuery = "(" + sQuery.getName() + ")^" + sQuery.getScore();
                } else {
                    solrQuery = "(" + sQuery.getName() + ")";
                }
            } else {
                if (!solrQuery.contains(sQuery.getName())) {
                    if (sQuery.getScore() != null) {
                        solrQuery += " " + liaison + " (" + sQuery.getName() + ")^" + sQuery.getScore();
                    } else {
                        solrQuery += " " + liaison + " (" + sQuery.getName() + ")";
                    }
                }

            }

        }

        if (solrQuery == null) {
            solrQuery = "";
        }
        
        System.out.println(solrQuery);
        return solrQuery;
    }

}
