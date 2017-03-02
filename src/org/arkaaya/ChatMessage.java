package org.arkaaya;

import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no 
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Server
	static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, ONLINE = 3, OFFLINE = 4;
	private int type;
	private String message;
	private String clientName;
	private String targetValue;
	private String castingType;
	private String groupName;
	
	
	public ChatMessage() {
		// TODO Auto-generated constructor stub
	}
	// constructor
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getCastingType() {
		return castingType;
	}
	public void setCastingType(String castingType) {
		this.castingType = castingType;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public static int getWhoisin() {
		return WHOISIN;
	}
	public static int getLogout() {
		return LOGOUT;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	// getters
	int getType() {
		return type;
	}
	String getMessage() {
		return message;
	}
}
