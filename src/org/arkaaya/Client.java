package org.arkaaya;

import java.net.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client  {

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;

	// if I use a GUI or not
	private ClientGUI cg;
	
	// the server, the port and the username
	private String server, username, password;
	private int port;
    
	/*
	 *  Constructor called by console mode
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	Client(String server, int port, String username, String password) {
		// which calls the common constructor with the GUI set to null
		this(server, port, username, password,null);
	}

	/*
	 * Constructor call when used from a GUI
	 * in console mode the ClienGUI parameter is null
	 */
	Client(String server, int port, String username,String password, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
	    this.password = password;
		// save if we are in GUI mode or not
		this.cg = cg;
	}
	
	/*
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		// if it failed not much I can so
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		/* Creating both Data Stream */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			sOutput.reset();
			sOutput.writeObject(username);
			//sOutput.flush();
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		if(cg == null)
			System.out.println(msg);      // println in console mode
		else
			cg.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
		
		// inform the GUI
		if(cg != null)
			cg.connectionFailed();
			
	}
	
	// creating a dynamic JList with update online members information
	
	public DefaultListModel<String> updatedOnlineClientsList (ArrayList<String> updatedOnlineClientList){
		DefaultListModel<String> updatedOnlineClientListModel = new DefaultListModel<String>();
		int updatedListSize = updatedOnlineClientList.size();
		for(int i=0;i<updatedListSize;i++){
			// transforming list elements to model elements
			updatedOnlineClientListModel.addElement(updatedOnlineClientList.get(i));
					}
		return updatedOnlineClientListModel;
	}
	
	// creating a dynamic Jtree with updated Groups Information
	
	public JTree UpdateGroupList(HashMap<String, HashSet<String>> currentGroupList){
		
		if(currentGroupList.size()<1) {return null;}

		 DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Multicast Groups");
		
		 DefaultMutableTreeNode tempGroupNode;
		 DefaultMutableTreeNode tempMemberNode;
		Set<String> groupNameSet = currentGroupList.keySet();
		Iterator groupNameSetIterator = groupNameSet.iterator();
		
		while(groupNameSetIterator.hasNext()){
			String groupName =(String) groupNameSetIterator.next();
			// add groupName to JTree
			tempGroupNode = new DefaultMutableTreeNode(groupName);
			   rootNode.add(tempGroupNode);
						
			 HashSet<String> groupMembersSet =  currentGroupList.get(groupName);
			 
			 Iterator groupMembersSetIterator = groupMembersSet.iterator();
			 
			    while(groupMembersSetIterator.hasNext()){
			    	
			    	String groupMemberName = (String)groupMembersSetIterator.next(); 
			    	// add GroupMemberName to a JTree
			    	tempGroupNode.add(new DefaultMutableTreeNode(groupMemberName));
			    } // inner while
		}// outer while
		
		final JTree updatedmulticastTree = new JTree(rootNode);
		
		/////////
		//multicastTree = new JTree(multicastGroups);
		//multicastTree.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		//////////////////////////
		MouseListener myMouseTreeListener = new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 1) {

                    Object treenode =  updatedmulticastTree.getLastSelectedPathComponent();
                    
                    String selectedGroupName = treenode.toString();
		           //String targetUnicastClient = (String) unicastL.getSelectedValue();
		           // add selectedItem to your second list.
		           cg.targetTF.setText(selectedGroupName);
		          

		         }
		    }
		};
		
		updatedmulticastTree.addMouseListener(myMouseTreeListener);
		///////
		updatedmulticastTree.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		return updatedmulticastTree;
		
	}
	
	
	
	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					
					
					Object  resObj =  sInput.readObject();
					
					if(resObj.getClass()==String.class){
						// if console mode print the message and add back the prompt
						if(cg == null) {
							System.out.println((String)resObj);
							System.out.print("> ");
						}
						else {
							cg.append((String)resObj);
						}
						
			}else if(resObj.getClass() == ServerResponse.class){
				ServerResponse sresp = (ServerResponse)resObj;
				System.out.println("Server Response saying group size is = "+sresp.getUpdatedGroupList().size());
						
						if(sresp.getRespType().equalsIgnoreCase("UGT")){ // selected update Group tree
							
							
							JTree newJtree = UpdateGroupList(sresp.getUpdatedGroupList());
							if(newJtree != null){ // to prevent failures from first client window open 
							
							DefaultTreeModel oldTreemodel = (DefaultTreeModel)cg.multicastTree.getModel();
							oldTreemodel.setRoot((DefaultMutableTreeNode)newJtree.getModel().getRoot());
							oldTreemodel.reload((DefaultMutableTreeNode)oldTreemodel.getRoot());
							
							}
							
													
						}else if(sresp.getRespType().equalsIgnoreCase("UGT&UCL")) {
							// for updating list and update group tree
							
							// first updating group tree
							JTree newJtree = UpdateGroupList(sresp.getUpdatedGroupList());
							if(newJtree != null){ // to prevent failures from first client window open 
							DefaultTreeModel oldTreemodel = (DefaultTreeModel)cg.multicastTree.getModel();
							oldTreemodel.setRoot((DefaultMutableTreeNode)newJtree.getModel().getRoot());
							oldTreemodel.reload((DefaultMutableTreeNode)oldTreemodel.getRoot());
							 }
						
						   // update list
							
							
							DefaultListModel<String> updatedClientListModel  = updatedOnlineClientsList (sresp.getUpdatedClientList());
							cg.unicastList.setModel(updatedClientListModel);
						
						
						
						
						
						}
						
					}else {}
					
					
					
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(cg != null) 
						cg.connectionFailed();
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
