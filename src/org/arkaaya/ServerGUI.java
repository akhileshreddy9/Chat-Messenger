package org.arkaaya;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * The server as a GUI
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;
	// the stop and start buttons
	private JButton stopStart;
	// JTextArea for the chat room and the events
	private JTextArea chat, event;
	// The port number
	private JTextField tPortNumber;
	// my server
	private Server server;
	
	
	// server constructor that receive the port to listen to for connection as parameter
	ServerGUI(int port) {
		super("Chat Server");
		server = null;
		// in the NorthPanel the PortNumber the Start and Stop buttons
		JPanel north = new JPanel();
		north.add(new JLabel("Server Host :: "));
		JTextField hostName = new JTextField("localhost");
		hostName.setEditable(false);
		north.add(hostName);
		
		north.add(new JLabel("Port :: "));
		tPortNumber = new JTextField(" " + port);
		north.add(tPortNumber);
		// to stop or start the server, we start with "Start"
		stopStart = new JButton("Start Server");
		stopStart.addActionListener(this);
		north.add(stopStart);
		add(north, BorderLayout.NORTH);
		
		// the event and chat room
		JPanel center = new JPanel(new GridLayout(2,1));
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("Chat room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));	
		add(center);
		
		// need to be informed when the user click the close button on the frame
		addWindowListener(this);
		setSize(400, 600);
		setResizable(false);
		setVisible(true);
	}		

	// append message to the two JTextArea
	// position at the end
	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);
		
	}
	
	// Server Start or Stop options
	public void actionPerformed(ActionEvent e) {
		// if running we have to stop
		if(server != null) {
			server.stop();
			server = null;  //--
			tPortNumber.setEditable(true);
			stopStart.setText("Start Server");
			return;
		}
      	// OK start the server	
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Invalid port number");
			return;
		}
		// ceate a new Server and passing the port number and GUI values
		server = new Server(port, this);
		// and start it as a thread
		new ServerRunning().start();
		stopStart.setText("Stop Server");
		tPortNumber.setEditable(false);
	}

	/* --------------------------------------------         ------------------------------------------------------------------------ */
	// This main method starts only ServerGUI but not server
	// to start server we have to click on start/stop button
	// This UI executes and display if user entered the port number
	public static void main(String[] cmdArgs) {
		// validating the cmdArgs for port number
		if(cmdArgs.length == 1){
			
			String portNumberStr = cmdArgs[0];
			try{
			int port = Integer.parseInt(portNumberStr);
			new ServerGUI(port);
			}catch(NumberFormatException nfe){
				System.err.println("Invalid Port Number\n Plz restart the server with valid port number");
				System.exit(ERROR);
			}
		}else {
			System.err.println("Port Number is expected as a CMD arg! \nSo plz provide that to avoid this error by next run");
		}
		
		
	}
	
/* --------------------------------------------         ------------------------------------------------------------------------ */	

	/*
	 * If the user click the X button to close the application
	 * I need to close the connection with the server to free the port
	 */
	public void windowClosing(WindowEvent e) {
		// if my Server exist
		if(server != null) {
			try {
				server.stop();			// ask the server to close the conection
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		// dispose the frame
		dispose();
		System.exit(0);
	}
	// I can ignore the other WindowListener method
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	/*
	 * A thread to run the Server
	 */
	class ServerRunning extends Thread {
		public void run() {
			server.start();         // should execute until if fails
			// the server failed
			stopStart.setText("Start Server");
			tPortNumber.setEditable(true);
			appendEvent("Server Stopped\n");
			server = null;
		}
	}

}
