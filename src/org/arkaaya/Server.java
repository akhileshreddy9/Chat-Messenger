package org.arkaaya;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	public static ArrayList<ClientThread> liveClientList = new ArrayList<ClientThread>();
	// if I am in a GUI
	private ServerGUI sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepRunning;
	
	public HashMap<String, HashSet<String>> groupsMapFinal = new HashMap<String, HashSet<String>>(); 
	
//	public ArrayList<String> onlineStatusClients ;//= new ArrayList<String>();
	
	

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		//liveClientList = new ArrayList<ClientThread>();
	}
	
	// this method to start the server and make it as run & wait for clients
	public void start() {
		// read the user configuration from the "password.txt" file to configuration object
		// if read fails exit from process
		AuthenticationUtil authenticationUtil=new AuthenticationUtil();
		
		keepRunning = true;
		/* create socket server and wait for connection requests */
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);
			// load on startup actions
			//AuthenticationUtil.readAuthDatafromTxtFile();
			//System.out.println("server auth map size"+AuthenticationUtil.authenticationMap.size());

			// infinite loop to wait for connections
			while(keepRunning) 
			{
			
				// format message saying we are waiting
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();  	// accept connection
				// if I was asked to stop
				if(!keepRunning) {break;}
				ClientThread t = new ClientThread(socket);  // make a thread of it
				liveClientList.add(t);	// save it in the ArrayList
				//onlineStatusClients.add(t.getUsername());
				
				//////
				// create ServerResponse
			    ServerResponse serRes = new ServerResponse();
			    serRes.setRespType("UGT&UCL"); // UGT for update Group Tree  & UCL for update clientList
			    serRes.setMessage(null);
			    // add groupsMapFinal
			    serRes.setUpdatedGroupList(groupsMapFinal);
			    // add updated client List
			    serRes.setUpdatedClientList(getUpdatedOnlineClientList());
			    // write to all clients
			     writeMsg2AllClients(serRes); // sends the update list to all clients
				
				//////
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < liveClientList.size(); ++i) {
					ClientThread tc = liveClientList.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
    /*
     * For the GUI to stop the server
     */
	protected void stop() {
		keepRunning = false;
		// connect to myself as Client to exit statement 
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
			
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}
	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	/*
	 *  to broadcast a message to all Clients
	 */
	private synchronized void broadcast(ChatMessage chatMsg) {
		// add HH:mm:ss and \n to the message
		//String time = sdf.format(new Date());
		String messageLf = chatMsg.getClientName() + " : " + chatMsg.getMessage() + "\n";
		// display message on console or GUI
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window
		
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = liveClientList.size(); --i >= 0;) {
			ClientThread ct = liveClientList.get(i);
			if(!ct.isVisibility()) continue;
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf)) {
				liveClientList.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}
	
	// to unicast message to respective client
	private synchronized void unicast(ChatMessage chatMsg) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = chatMsg.getClientName() + " : " + chatMsg.getMessage() + "\n";
		// display message on server GUI or java console
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window
		
		String FromClientName = chatMsg.getClientName();
		
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		
		
		for(int i = liveClientList.size(); --i >= 0;) {
			ClientThread ct = liveClientList.get(i);
			// looking for the unicast target client and the from client (who send msg)
			if( (ct.getUsername().equals(chatMsg.getTargetValue())) || (ct.getUsername().equals(chatMsg.getClientName())) ){
				
			if(!ct.writeMsg(messageLf)) {
					liveClientList.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
			// try to write to the Client if it fails remove it from the list
			
		}
	}

	
	//Multi casting to groups
	
	// to unicast message to respective client
		private synchronized void multicast(ChatMessage chatMsg) {
			// add HH:mm:ss and \n to the message
			//String time = sdf.format(new Date());
			
			String messageLf = chatMsg.getClientName() + " : " + chatMsg.getMessage() + "\n";
			// display message on server GUI or java console
			if(sg == null)
				System.out.print(messageLf);
			else
				sg.appendRoom(messageLf);     // append in the room window
			
			String FromClientName = chatMsg.getClientName();
			
			// we loop in reverse order in case we would have to remove a Client
			// because it has disconnected
			
			
			for(int i = liveClientList.size(); --i >= 0;) {
				ClientThread ct = liveClientList.get(i);
				// looking for the unicast target client and the from client (who send msg)
				if( (ct.getMemberGroupList().contains(chatMsg.getTargetValue())) || (ct.getOwnGroupList().contains(chatMsg.getTargetValue()))   ){// || (ct.getUsername().equals(chatMsg.getClientName())) ){
					
				if(!ct.writeMsg(messageLf)) {
						liveClientList.remove(i);
						display("Disconnected Client " + ct.username + " removed from list.");
					}
				}
				// try to write to the Client if it fails remove it from the list
				
			}
		}

	
	
	
	
	public ClientThread getClientThread(String client_Name){
		
		for(int i = 0; i < liveClientList.size(); ++i) {
			ClientThread ct = liveClientList.get(i);
			if(ct.getUsername().equals(client_Name)){
				return ct;
			}
					} // eof loop
		return null;
	}
	
	
	// update client status
	public ClientThread updateClientStatus(String client_Name, boolean status){
		
		for(int i = 0; i < liveClientList.size(); ++i) {
			ClientThread ct = liveClientList.get(i);
			if(ct.getUsername().equals(client_Name)){
				ct.setVisibility(status);
				liveClientList.remove(i);
				liveClientList.add(i, ct);
			}// if
			
					} // eof loop
		return null;
	}
	
	
	// creating a new group
	public void createGroup(ChatMessage chtMsg){
		
		display(chtMsg.getClientName());
		display(chtMsg.getCastingType());
		display(chtMsg.getGroupName());
		display(chtMsg.getTargetValue());
		// verify the client is live or not
		ClientThread presentClient = getClientThread(chtMsg.getClientName());
		if(presentClient == null) { System.out.println("Client is not in liveClientList to create group");return;}
		
		// verify the group exists or not
		if(presentClient.getOwnGroupList().contains(chtMsg.getGroupName())){
			System.out.println("Group is already created and existed! plz try another groupname");return;
		}
		
		
		// add group to the clientThread avtive clients list
		presentClient.getOwnGroupList().add(chtMsg.getGroupName()); // updating with client
            HashSet groupMembersSet = new HashSet<String>();
            groupMembersSet.add(chtMsg.getClientName());
		    groupsMapFinal.put(chtMsg.getGroupName(),groupMembersSet); // updating with final members map
		// join this client to group
		
		// return updated jtree obj through response
		   // create ServerResponse
		    ServerResponse serRes = new ServerResponse();
		    serRes.setRespType("UGT"); // UGT for update Group Tree
		    serRes.setMessage(null);
		    
		   
		   // add groupsMapFinal
		    serRes.setUpdatedGroupList(groupsMapFinal);
		 // write to all clients
		 writeMsg2AllClients(serRes); // sends the update list to all clients
		
		
	}
	
	// Join to an existed group
		public void joinGroup(ChatMessage chtMsg){
			
			System.out.println(chtMsg.getClientName());
			System.out.println(chtMsg.getCastingType());
			System.out.println(chtMsg.getGroupName());
			System.out.println(chtMsg.getTargetValue());
			
			// verify the client is live or not
			ClientThread presentClient = getClientThread(chtMsg.getClientName());
			if(presentClient == null) { System.out.println("Client is not in liveClientList to join group");return;}
			
			// verify the group exists or not
			if(presentClient.getMemberGroupList().contains(chtMsg.getGroupName())){
				System.out.println("User is already joined to this Group");return;
			}
			
			// add group to the clientThread avtive clients list
			   presentClient.getMemberGroupList().add(chtMsg.getGroupName()); // updating with client
	            HashSet groupMembersSet =groupsMapFinal.get(chtMsg.getGroupName());
	            if(groupMembersSet == null){ System.out.println("Selected Group doesnot exist to join");return;}
	            //get previous members set and add 
	            groupMembersSet.add(chtMsg.getClientName());
	         // join the client to group
	            groupsMapFinal.put(chtMsg.getGroupName(),groupMembersSet); // updating with final members map
			
			// return updated jtree obj through response
			
	            // create ServerResponse
			    ServerResponse serRes = new ServerResponse();
			    serRes.setRespType("UGT"); // UGT for update Group Tree
			    serRes.setMessage(null);
			    
			   
			   // add groupsMapFinal
			    serRes.setUpdatedGroupList(groupsMapFinal);
			 // write to all clients
			 writeMsg2AllClients(serRes); // sends the update list to all clients
	            
		}
		
		
		// creating a new group
		public void leaveGroup(ChatMessage chtMsg){
			
			System.out.println(chtMsg.getClientName());
			System.out.println(chtMsg.getCastingType());
			System.out.println(chtMsg.getGroupName());
			System.out.println(chtMsg.getTargetValue());
			
			        // verify the client is live or not
						ClientThread presentClient = getClientThread(chtMsg.getClientName());
						if(presentClient == null) { System.out.println("Client is not in liveClientList to leave group");return;}
						
						// verify the group exists or not
						if(presentClient.getMemberGroupList().contains(chtMsg.getGroupName())){
							// leaving a member group
							presentClient.getMemberGroupList().remove(chtMsg.getGroupName());
							
							// leaving from finalmap group as member
							HashSet groupMembersSet = groupsMapFinal.get(chtMsg.getGroupName());
							
							 if(groupMembersSet == null){ System.out.println("You are not a member of this group to leave");return;}
							boolean res = groupMembersSet.remove(chtMsg.getClientName());
							 if(res == true){ System.out.println("Leaved the group successfully");}//return;} 
							 groupsMapFinal.put(chtMsg.getGroupName(),groupMembersSet); // updating with final members map
							
							
							//System.out.println("User is already joined to this Group");return;
						}
						
						else if(presentClient.getOwnGroupList().contains(chtMsg.getGroupName())){
							// leaving own group
							
							// remove from client grouplist
							presentClient.getOwnGroupList().remove(chtMsg.getGroupName());
							// remove from finalmap group
							groupsMapFinal.remove(chtMsg.getGroupName());
							
						} 
						else {System.out.println("Group is not existed to leave"); return;}
						
						
						// return updated jtree obj through response
						
						 ServerResponse serRes = new ServerResponse();
						    serRes.setRespType("UGT"); // UGT for update Group Tree
						    serRes.setMessage(null);
						    
						   
						   // add groupsMapFinal
						    serRes.setUpdatedGroupList(groupsMapFinal);
						 // write to all clients
						 writeMsg2AllClients(serRes); // sends the update list to all clients
			
		}
	
	
	
	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < liveClientList.size(); ++i) {
			ClientThread ct = liveClientList.get(i);
			// found it
			if(ct.id == id) {
				liveClientList.remove(i);
				return;
			}
		}
	}
	
	private boolean writeMsg2AllClients(ServerResponse serResp) {
		// if Client is still connected send the message to it
		
		for(int i = 0; i < liveClientList.size(); ++i) {
			ClientThread ct = liveClientList.get(i);
			
			
		if(!ct.getSocket().isConnected()) {
			ct.close();
			return false;
		}
		// write the message to the stream
		try {
			ct.sOutput.reset();
			ct.sOutput.writeObject(serResp);
		//	ct.sOutput.flush();
			
		}
		// if an error occurs, do not abort just inform the user
		catch(IOException e) {
			display("Error sending message to " + ct.getUsername());
			display(e.toString());
		}
		}
		return true;
	}
	
	// return the client list whose are online
	
	public ArrayList<String> getUpdatedOnlineClientList(){
		
		if(liveClientList.size() == 0) { return null;} // if no live client
		int numberOfClients = liveClientList.size();
		ArrayList<String> updatedVisibleList = new ArrayList<String>();
		
		for(int i=0;i<numberOfClients;i++){
			ClientThread ct =liveClientList.get(i);
			if(ct.isVisibility()){ // means if a client is visible
				updatedVisibleList.add(ct.getUsername().trim());
				}
			// discard the ones who is not visible
		}
		
		
		return updatedVisibleList;
		
	}
	
	//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$//
	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;
		//boolean status; // to maintain online and offline status
		
		

		HashSet<String> ownGroupList = new HashSet<String>();
		HashSet<String> memberGroupList=new HashSet<String>();;
		
		/*
		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}
*/
		public Set<String> getOwnGroupList() {
			return ownGroupList;
		}

		public void setOwnGroupList(HashSet<String> ownGroupList) {
			this.ownGroupList = ownGroupList;
		}

		public HashSet<String> getMemberGroupList() {
			return memberGroupList;
		}

		public void setMemberGroupList(HashSet<String> memberGroupList) {
			this.memberGroupList = memberGroupList;
		}

		boolean visibility=true;
		
		
		

		public Socket getSocket() {
			return socket;
		}

		public void setVisibility(boolean visibility) {
			this.visibility = visibility;
		}
		
		public boolean isVisibility() {
			return visibility;
		}
		
		public void setSocket(Socket socket) {
			this.socket = socket;
		}

		public ObjectInputStream getsInput() {
			return sInput;
		}

		public void setsInput(ObjectInputStream sInput) {
			this.sInput = sInput;
		}

		public ObjectOutputStream getsOutput() {
			return sOutput;
		}

		public void setsOutput(ObjectOutputStream sOutput) {
			this.sOutput = sOutput;
		}

		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public ChatMessage getCm() {
			return cm;
		}

		public void setCm(ChatMessage cm) {
			this.cm = cm;
		}

		
		// Constructor
		ClientThread(Socket socket) {
			
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected.");
				// update group info and members info to client window at very begin
				
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
			
			// a unique id
						id = ++uniqueId;
						this.socket = socket;
            date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
			boolean keepRunning = true;
			while(keepRunning) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception Reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();

				// Switch on the type of message receive
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					String castType = cm.getCastingType();
					String targetVal = cm.getTargetValue();
					     switch(castType){
					     // for uni casting
					     case "U": unicast(cm);break;
					     case "M": multicast(cm);break;
					     case "B": broadcast(cm); break;
					     case "CG": createGroup(cm); break;
					     case "JG": joinGroup(cm); break;
					     case "LG": leaveGroup(cm); break;
					      
					     }
					
					//broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepRunning = false;
					break;
				case ChatMessage.OFFLINE:
					// scan liveClientList the users connected
					//ClientThread targetClientThread = getClientThread();
					if (cm.getClientName() != null){
						
						updateClientStatus(cm.getClientName(), false);
						//targetClientThread.setVisibility(false);  // means setting the status as offline
						// update all clients.
						
						ServerResponse serRes = new ServerResponse();
					    serRes.setRespType("UGT&UCL"); // UGT for update Group Tree  & UCL for update clientList
					    serRes.setMessage(null);
					    // add groupsMapFinal
					    serRes.setUpdatedGroupList(groupsMapFinal);
					    // add updated client List
					    serRes.setUpdatedClientList(getUpdatedOnlineClientList());
					    // write to all clients
					     writeMsg2AllClients(serRes); // sends the update list to all clients
						
					}
					break;
					
				case ChatMessage.ONLINE:
					
					if (cm.getClientName() != null){
						
						//targetClientThread1.setVisibility(true);  // means setting the status as offline
						// update all clients.
						updateClientStatus(cm.getClientName(), true);
						ServerResponse serRes = new ServerResponse();
					    serRes.setRespType("UGT&UCL"); // UGT for update Group Tree  & UCL for update clientList
					    serRes.setMessage(null);
					    // add groupsMapFinal
					    serRes.setUpdatedGroupList(groupsMapFinal);
					    // add updated client List
					    serRes.setUpdatedClientList(getUpdatedOnlineClientList());
					    // write to all clients
					     writeMsg2AllClients(serRes); // sends the update list to all clients
						
					}
					break;	
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(ServerResponse serResp) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(serResp);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
		
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			/*
			if(!socket.isConnected()) {
				close();
				return false;
			}
			*/
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}

