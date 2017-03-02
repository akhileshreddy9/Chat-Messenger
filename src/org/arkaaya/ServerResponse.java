package org.arkaaya;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ServerResponse implements Serializable {

	protected static final long serialVersionUID = 1112122201L;

	private ArrayList<String> UpdatedClientList;
	private HashMap<String, HashSet<String>> updatedGroupList;
	private String respType;
	private String message;
	
	
	public ServerResponse() {
		// TODO Auto-generated constructor stub
	}


	


	public ArrayList<String> getUpdatedClientList() {
		return UpdatedClientList;
	}





	public void setUpdatedClientList(ArrayList<String> updatedClientList) {
		UpdatedClientList = updatedClientList;
	}





	public HashMap<String, HashSet<String>> getUpdatedGroupList() {
		return updatedGroupList;
	}


	public void setUpdatedGroupList(HashMap<String, HashSet<String>> updatedGroupList) {
		this.updatedGroupList = updatedGroupList;
	}


	public String getRespType() {
		return respType;
	}


	public void setRespType(String respType) {
		this.respType = respType;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
	
	}
