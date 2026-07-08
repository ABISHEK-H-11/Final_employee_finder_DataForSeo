package com.employeeFinderByDataForSEO.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataForSeoResponse {

    private int status_code;
    private String status_message;
    private double cost;
    private List<Task> tasks;
    

    public int getStatus_code() {
		return status_code;
	}

	public void setStatus_code(int status_code) {
		this.status_code = status_code;
	}

	public String getStatus_message() {
		return status_message;
	}

	public void setStatus_message(String status_message) {
		this.status_message = status_message;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Task {

        private int status_code;
        private String status_message;
        private List<Result> result;
		public int getStatus_code() {
			return status_code;
		}
		public void setStatus_code(int status_code) {
			this.status_code = status_code;
		}
		public String getStatus_message() {
			return status_message;
		}
		public void setStatus_message(String status_message) {
			this.status_message = status_message;
		}
		public List<Result> getResult() {
			return result;
		}
		public void setResult(List<Result> result) {
			this.result = result;
		}
        
    }	

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        private List<Item> items;

		public List<Item> getItems() {
			return items;
		}

		public void setItems(List<Item> items) {
			this.items = items;
		}
        
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        private String type;
        private int rank_group;
        private String domain;
        private String title;
        private String description;
        private String url;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public int getRank_group() {
			return rank_group;
		}
		public void setRank_group(int rank_group) {
			this.rank_group = rank_group;
		}
		public String getDomain() {
			return domain;
		}
		public void setDomain(String domain) {
			this.domain = domain;
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
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
        
    }
    
    
}