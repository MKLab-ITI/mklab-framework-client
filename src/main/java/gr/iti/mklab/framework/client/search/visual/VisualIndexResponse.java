package gr.iti.mklab.framework.client.search.visual;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class VisualIndexResponse {

	private static DecimalFormat df = new DecimalFormat("#.###");
	
	public List<JsonResult> results = new ArrayList<JsonResult>();
	
	public void addResult(String id, int rank, double distance) {
		JsonResult result = new JsonResult(id, rank, distance);
		results.add(result);
	}
	
	public List<JsonResult> getResults() {
		return results;
	}
	
	public static class JsonResult {

		private String id;
		 
		private int rank;
		
		private String score;
	
		public JsonResult(String id, int rank, double distance) {
			this.id = id;
			this.rank = rank;
        
			// transform distance into similarity
			double similarity = (2.0 - Math.sqrt(distance)) / 2.0;
        
			// format the score
			this.score = df.format(similarity);
		}
	
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}
	
		public String getScore() {
			return score;
		}

		public void setScore(String score) {
			this.score = score;
		}
	}
	
	public static void main(String...args) {
		VisualIndexResponse s = new VisualIndexResponse();
		
		s.addResult("X", 1, 0.2);
		s.addResult("Y", 2, 0.4);
		s.addResult("Z", 3, 0.8);

	}
}
