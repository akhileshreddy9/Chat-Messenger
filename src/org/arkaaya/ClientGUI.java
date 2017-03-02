package org.arkaaya;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// will first hold "Username:", later on "Enter message"
	private JLabel label;
	// to hold the Username and later on the messages
	private JTextField tf;
	// to hold the server address an the port number
	private JTextField tfServer, tfPort, tfclientName;
	
	// to hold client password
	private JPasswordField tfClientPassword;
	// to Logout and get the list of the users
	private JButton login, logout;
	// for the chat room
	private JTextArea ta;
	// if it is for connection
	private boolean connected;
	// the Client object
	private Client client;
	// the default port number
	private int ServerPort;
	private String serverName;
	private DefaultListModel<String> multicastListModel;
	private DefaultListModel<String> unicastListModel;
	public JTree multicastTree = new JTree();
	public JList unicastList;
	public JTextField targetTF;
	private JButton sendButton;
	private JButton groupOptButton;
	
	public JPanel northPanel;
	public JPanel listPanel; 
	public JButton statusButton;

	// Constructor connection receiving a socket number
	ClientGUI(String serverhostname, int serverPort,String clientName,String clientPassword,int clientPort) {
        
		super("Chat Client - " + clientName);
		AuthenticationUtil authenticationUtil = new AuthenticationUtil();
		ServerPort = serverPort;
		serverName = serverhostname;
		
		// The NorthPanel with:
		 northPanel = new JPanel(new GridLayout(5,1));
		//GridBagConstraints c = new GridBagConstraints();
		//c.fill = GridBagConstraints.HORIZONTAL;
		// the server name anmd the serverPort number
		
		// creating server and its port panel
		JPanel serverAndPort = new JPanel(new GridLayout(1,4));
		//JPanel serverAndPort = new JPanel(new GridBagLayout());
		// the two JTextField with default value for server address and serverPort number
		tfServer = new JTextField(serverName);
		tfPort = new JTextField(ServerPort+"");
		
		//ttfPort.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel serverHostLabel = new JLabel("Server Host :");
		serverHostLabel.setHorizontalAlignment(JLabel.CENTER);
		serverAndPort.add(serverHostLabel);
		serverAndPort.add(tfServer);
		
		JLabel serverPortLabel = new JLabel("Port Number:  ");
		serverPortLabel.setHorizontalAlignment(JLabel.CENTER);
		serverAndPort.add(serverPortLabel);
		serverAndPort.add(tfPort);
		//serverAndPort.add(new JLabel(""));
		// adds the Server an port field to the GUI
		//serverAndPort.setSize(400, 250);
		northPanel.add(serverAndPort);

		// creating client name and password panel
		JPanel clientAndPassword = new JPanel(new GridLayout(1,4));
		///JPanel clientAndPassword = new JPanel(new GridBagLayout());
		// the two JTextField with default value for server address and serverPort number
		tfclientName = new JTextField(clientName);
		tfClientPassword = new JPasswordField(clientPassword);
		//tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JLabel clientNameLabel = new JLabel("Client Name :");
		clientNameLabel.setHorizontalAlignment(JLabel.CENTER);
		clientAndPassword.add(clientNameLabel);
		clientAndPassword.add(tfclientName);
		JLabel clientPasswordLabel = new JLabel("Password :");
		clientPasswordLabel.setHorizontalAlignment(JLabel.CENTER);
		clientAndPassword.add(clientPasswordLabel);
		clientAndPassword.add(tfClientPassword);
		//serverAndPort.add(new JLabel(""));
		// adds the Server an port field to the GUI
		northPanel.add(clientAndPassword);

		
		// login and logout buttons
				login = new JButton("Login");
				login.addActionListener(this);
				logout = new JButton("Logout");
				logout.addActionListener(this);
				logout.setEnabled(false);		// you have to login before being able to logout
				statusButton = new JButton("Status");
				// add action listner to do operations
				statusButton.setEnabled(false);
				
				
		JPanel loginAndLogoutPanel = new JPanel(); // new GridLayout(1,2
		          loginAndLogoutPanel.add(login);
		          loginAndLogoutPanel.add(logout);
		          loginAndLogoutPanel.add(statusButton);
		     northPanel.add(loginAndLogoutPanel);
		          
		
		
		// the Label and the TextField
		label = new JLabel("Enter your Message Here", SwingConstants.CENTER);
		//JLabel samplelabel = new JLabel( "  ", SwingConstants.CENTER);
		
		label.setForeground(Color.LIGHT_GRAY);
		tf = new JTextField();
		//tf.setPreferredSize(new Dimension(500,40));
		tf.setBackground(Color.lightGray);
		tf.setEnabled(false);
		JPanel clientMsgPanel = new JPanel(new GridLayout(2,1));
		//clientMsgPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
		clientMsgPanel.add(label);
		clientMsgPanel.add(tf);
		
		northPanel.add(clientMsgPanel);
		JPanel sendMsgPanel = new JPanel(new GridLayout(2,3));
		JLabel toLabel = new JLabel("To : ",SwingConstants.CENTER);
		sendMsgPanel.add(toLabel);
		targetTF = new JTextField("ALL");
		sendMsgPanel.add(targetTF);
		sendButton = new JButton("send");
		sendMsgPanel.add(sendButton);
		groupOptButton = new JButton("Group Operation");
		
        //////////////////
		
     MouseListener myMouseGroupButtonListener = new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
        	 
        if(!connected){return;}	 
        if (e.getClickCount() == 1) {
            
        	String [] groupOptionsAry = {"Create New Group","Join Group","Leave Group"};
            
            
           String selectedOption = (String) JOptionPane.showInputDialog(null, 
                    "Select a Group Operation",
                    "Group Operation",
                    JOptionPane.INFORMATION_MESSAGE, 
                    null, 
                    groupOptionsAry, 
                    groupOptionsAry[0]);
          System.out.println("user selecte group option = "+selectedOption);

          // perform create , join or leave operations
          switch(selectedOption){
          
          case "Create New Group" :
        	  String newGroupName = JOptionPane.showInputDialog(
        		        null, 
        		        "Enter new Group Name", 
        		        "Create Group", 
        		        JOptionPane.INFORMATION_MESSAGE
        		    );
        	  // do something
        	  //////
        	   
        	   	// creating new group msg
			ChatMessage createGroupMsg = new ChatMessage();
			createGroupMsg.setClientName(tfclientName.getText());
			createGroupMsg.setType(ChatMessage.MESSAGE);
			createGroupMsg.setCastingType("CG");
			createGroupMsg.setGroupName(newGroupName);
			client.sendMessage(createGroupMsg);				
			tf.setText("");
			//return; 
        	    
        	  ///////
        	  
        	  
        	  System.out.println("create new Group : "+newGroupName);
        	  
        	  
        	  break;
         
          case "Join Group" :
        	  
        	  String joinGroupName = JOptionPane.showInputDialog(
      		        null, 
      		        "Enter Group Name to Join", 
      		        "Join Group", 
      		        JOptionPane.INFORMATION_MESSAGE
      		    );
        	  
        	  //do something
        	//////
       	   
      	   	// join group msg
			ChatMessage joinGroupMsg = new ChatMessage();
			joinGroupMsg.setClientName(tfclientName.getText());
			joinGroupMsg.setType(ChatMessage.MESSAGE);
			joinGroupMsg.setCastingType("JG");
			joinGroupMsg.setGroupName(joinGroupName);
			client.sendMessage(joinGroupMsg);				
			tf.setText("ALL");
			//return; 
      	    
      	  ///////
      	  System.out.println("join Group : "+joinGroupName);
        	  
        	  break;
          
          case "Leave Group" :
        	  
        	  String leaveGroupName = JOptionPane.showInputDialog(
        		        null, 
        		        "Enter Group Name to Leave from it", 
        		        "Leave Group", 
        		        JOptionPane.INFORMATION_MESSAGE
        		    );
        	  
        	  //do something
        	//////
          	   
        	   	// join group msg
  			ChatMessage leaveGroupMsg = new ChatMessage();
  			leaveGroupMsg.setClientName(tfclientName.getText());
  			leaveGroupMsg.setType(ChatMessage.MESSAGE);
  			leaveGroupMsg.setCastingType("LG");
  			leaveGroupMsg.setGroupName(leaveGroupName);
  			client.sendMessage(leaveGroupMsg);				
  			tf.setText("ALL");
  			//return; 
        	    
        	  ///////
        	  
        	  System.out.println("Leave Group : "+leaveGroupName);
        	  
        	  break;
          }
          

         }
    }
};
groupOptButton.addMouseListener(myMouseGroupButtonListener);
///////////////////
sendMsgPanel.add(groupOptButton);


		northPanel.add(sendMsgPanel);
		
		
		
		//----------------------------------------------
		
		
		add(northPanel, BorderLayout.NORTH);

		// @@@@@@@@@@@@@@@@@@@@@@@Center Panel@@@@@@@@@@@@@@@@@@@@@@@@@//
		
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		
		 listPanel =  new JPanel(new GridLayout(1,2));
		unicastListModel = new DefaultListModel<String>();
		
		unicastList = new JList(unicastListModel);
		//////////////////
		
		MouseListener myMouseListListener = new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 1) {


		           String targetUnicastClient = (String) unicastList.getSelectedValue();
		           // add selectedItem to your second list.
		           targetTF.setText(targetUnicastClient);
		          

		         }
		    }
		};
		unicastList.addMouseListener(myMouseListListener);
		///////////////////
		
		unicastList.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		unicastList.setSelectedIndex(0);
        unicastList.setVisibleRowCount(3);
        //unicastList.setSize(220, 200);
		listPanel.add(new JScrollPane(unicastList));
		
		DefaultMutableTreeNode multicastGroups = new DefaultMutableTreeNode("Multicast Groups");
		/*
		DefaultMutableTreeNode g1 = new DefaultMutableTreeNode("G1");		
		DefaultMutableTreeNode g2 = new DefaultMutableTreeNode("G2");
		
		DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("C1");
		
		DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("C2");
		DefaultMutableTreeNode c3 = new DefaultMutableTreeNode("C3");
		DefaultMutableTreeNode c4 = new DefaultMutableTreeNode("C4");
		
		multicastGroups.add(g1);
		multicastGroups.add(g2);
		
		g1.add(c1);
		g1.add(c2);
		
		g2.add(c3);
		g2.add(c4);
		*/
		multicastTree = new JTree(multicastGroups);
		multicastTree.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		//////////////////////////
		MouseListener myMouseTreeListener = new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 1) {

                    Object treenode =  multicastTree.getLastSelectedPathComponent();
                    
                    String selectedGroupName = treenode.toString();
		           //String targetUnicastClient = (String) unicastL.getSelectedValue();
		           // add selectedItem to your second list.
		           targetTF.setText(selectedGroupName);
		          

		         }
		    }
		};
		
		multicastTree.addMouseListener(myMouseTreeListener);
		///////////////
		 
		 
		listPanel.add(new JScrollPane(multicastTree));
		
		centerPanel.add(listPanel);
		
		
		add(centerPanel, BorderLayout.CENTER);  
		
		//@@@@@@@@@@@ south panel @@@@@@@@@@@@@@@@@@@@@
		JPanel southPanel = new JPanel(new GridLayout(1,1));
		// The CenterPanel which is the chat room
		ta = new JTextArea("Welcome to the Chat room\n", 10, 80);
		
		southPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(southPanel, BorderLayout.SOUTH);

			
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(400, 600);
		setVisible(true);
		tf.requestFocus();

	}

	// called by the Client to append text in the TextArea 
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	// called by the GUI is the connection failed
	// we reset our buttons, label, textfield
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		statusButton.setEnabled(false);
		//whoIsIn.setEnabled(false);
		label.setForeground(Color.LIGHT_GRAY);
		//label.setText("Enter your username below");
		tf.setText(" ");
		tf.setBackground(Color.lightGray);
		tf.setEnabled(false);
		
		// reset port number and host name as a construction time
		tfPort.setText("" + ServerPort);
		tfServer.setText(serverName);
		// let the user change them
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		// don't react to a <CR> after the username
		tf.removeActionListener(this);
		connected = false;
	}
		
	/*
	* Button or JTextField clicked
	*/
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, "Client is disconnected"));
			return;
		}

		
		// ok it is coming from the JTextField
		if(connected) {
			// creating message
			ChatMessage msg = new ChatMessage();
			msg.setClientName(tfclientName.getText());
			msg.setType(ChatMessage.MESSAGE);
			String tarString = targetTF.getText().trim();
			if(tarString == null) {return;}
			String castString = tarString.charAt(0)+"";
			if(castString.equalsIgnoreCase("c")) {
			msg.setCastingType("U");
			}else if(castString.equalsIgnoreCase("g")) {
				msg.setCastingType("M");
		     }else if(castString.equalsIgnoreCase("a")) {
			msg.setCastingType("B");
	          } else {return;}
			
			msg.setTargetValue(tarString); // this value specifies the unicast, multi cast, broadcast
			msg.setMessage(tf.getText());
			client.sendMessage(msg);				
			tf.setText("");
			return;
		}
		
		
		if(o == login) {
			// updating the user information to authmap
			System.out.println("Size of map is = "+AuthenticationUtil.authenticationMap.size());
			//AuthenticationUtil.updateAuthMap();
			// ok it is a connection request
			String username = tfclientName.getText().trim();
			this.setTitle(username);
			// empty username ignore it
			if(username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			// empty or invalid port numer, ignore it
			String portNumber = tfPort.getText().trim();
			
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   // nothing I can do if port number is not valid
			}
			
			String clientpassword = tfClientPassword.getText().trim();
			if((clientpassword.length())==0) {return;}

			// try creating a new Client with GUI
			client = new Client(server, port, username,clientpassword, this);
			// test if we can start the Client
			
			String clientValidationResult = AuthenticationUtil.validateUser(username, clientpassword);
			
			if(clientValidationResult.equals("user_duplicate")){ // if user is already running
				JOptionPane.showMessageDialog(null, "Client "+username+" is already running");
			}else if(clientValidationResult.equals("valid_exist")){ // in case of existed non duplicate user
				JOptionPane.showMessageDialog(null, "Client "+username+" Logged in Successfully");
				
				if(!client.start()) {
					return;
					}
				
				
				label.setForeground(Color.BLACK);
				tf.setBackground(Color.white);
				tf.setEnabled(true);
				tf.setText("");
				label.setText("Enter your message below");
				connected = true;
				
				// disable login button
				login.setEnabled(false);
				// enable the 2 buttons
				logout.setEnabled(true);
				statusButton.setText("Set Offline");
				/***************/
				statusButton.addActionListener(new ActionListener()
				{
					  public void actionPerformed(ActionEvent e)
					  {
					    JButton tempButton = (JButton) e.getSource();
					    if(tempButton.getText().equalsIgnoreCase("Set Offline")){
					    	// update the client status in all windows
					    	
					    	ChatMessage msg = new ChatMessage();
							msg.setClientName(tfclientName.getText());
							msg.setType(ChatMessage.OFFLINE);
							client.sendMessage(msg);				
							//tf.setText("");
							///return;
					    	// change the text value to "Set Online"
					    	tempButton.setText("Set Online");
							
					    }else {
					    	// implement this for to deal with set online status
					    	
					    	ChatMessage msg = new ChatMessage();
							msg.setClientName(tfclientName.getText());
							msg.setType(ChatMessage.ONLINE);
							client.sendMessage(msg);				
							//tf.setText("");
							///return;
					    	// change the text value to "Set Online"
					    	tempButton.setText("Set Offline");
							
					    	
					    	// change text
					    }
					  }
					});
				/*********************/
				
				statusButton.setEnabled(true);
				//whoIsIn.setEnabled(true);
				// disable the Server and Port JTextField
				tfServer.setEditable(false);
				tfPort.setEditable(false);
				tfclientName.setEditable(false);
				tfClientPassword.setEditable(false);
				// Action listener for when the user enter a message
				//tf.addActionListener(this);
				  sendButton.addActionListener(this);
			
			} else if(clientValidationResult.equals("valid_new")){ // in case of new user new account creation
				JOptionPane.showMessageDialog(null, "Client "+username+" Registered Successfully \n Please login Now");
			} else {return;}
			
			
		}

	}

	// to start the whole thing the server
	public static void main(String[] cmdArgs) {
		
		// kindly enter the cmd args in order as : serverhostname serverportnum clientpassword clientport
		// validating the cmdArgs for port number
				if(cmdArgs.length == 5){
					
					String serverHostName = cmdArgs[0];
					String serverPortNumberStr = cmdArgs[1];
					String clientName = cmdArgs[2];
					String clientPassword = cmdArgs[3];
					String clientPortNumberStr = cmdArgs[4];
					try{
					int serverPort = Integer.parseInt(serverPortNumberStr);
					int clientPort = Integer.parseInt(clientPortNumberStr);
		
					// creating client GUI
					new ClientGUI(serverHostName.trim(),serverPort,clientName.trim(),clientPassword.trim(),clientPort);
					}catch(NumberFormatException nfe){
						System.err.println("Invalid input Port Number\n Plz restart the client with valid port numbers");
						System.exit(ERROR);
					}
				}else {
					System.err.println("kindly enter the cmd args in order as : serverhostname serverportnum clientName clientpassword clientport! \nSo plz provide that to avoid this error by next run");
				}
				

		
		
	}

}

// ##################################### CustomCellRendered for JTree ############################//
/*
class MyCellRenderer extends DefaultTreeCellRenderer
{
    private boolean is_selected;
    
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, 
          boolean expanded, boolean leaf, int row, boolean hasFocus)        
{
super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
String name = (String)node.getUserObject();
if(name.equalsIgnoreCase("c1")) {
this.setEnabled(false);
//this.setDisabledIcon(this.getClosedIcon());  // I used the standard                         
             // icon for closed state. 
             // You can use your own 
             // jpg or gif image for 
             // disabled icon.
}

return this;
}
}
*/

//################################################################################################//
