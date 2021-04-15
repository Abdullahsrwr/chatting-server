package sample;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerThread extends Thread {
	protected Socket socket = null;
	protected PrintWriter out = null;
	protected BufferedReader in = null;

	// our server's secret code to connect
	// Ps we could read this code at the creation of server so becomes a private
	// chat room for those with the code only
	// based on the server code, you could also store the history of conversations,
	// which could be restored in a future session
	protected String strPasswords = "password";

	protected boolean bLoggedIn = false;
	protected String strUserID = null;
	protected String strPassword = null;

	protected Vector messages = null;

	public ChatServerThread(Socket socket, Vector messages) {
		super();
		this.socket = socket;
		this.messages = messages;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.err.println("IOEXception while opening a read/write connection");
		}
	}

	public void run() {
		// initialize interaction
		out.println("Connected to Chat Server");
		out.println("200 Ready For Chat");

		boolean endOfSession = false;
		while (!endOfSession) {
			endOfSession = processCommand();
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean processCommand() {
		String message = null;
		try {
			message = in.readLine();
		} catch (IOException e) {
			System.err.println("Error reading command from socket.");
			return true;
		}
		if (message == null) {
			return true;
		}
		StringTokenizer st = new StringTokenizer(message);
		String command = st.nextToken();
		String args = null;
		if (st.hasMoreTokens()) {
			args = message.substring(command.length() + 1, message.length());
		}
		return processCommand(command, args);
	}

	protected boolean processCommand(String command, String arguments) {
		if (command.equalsIgnoreCase("UID")) {
			// Store the userID, Ask for password
			strUserID = arguments;
			out.println("200 Please Enter the Password");
			return false;
		} else if (command.equalsIgnoreCase("PWD")) {
			// Check the password
			strPassword = arguments;
			boolean loginCorrect = false;

			if (strPasswords.equalsIgnoreCase(strPassword)) {
				loginCorrect = true;
			}

			if (loginCorrect) {
				out.println("200 Login Successful");
			} else {
				out.println("500 Login Incorrect");
				strUserID = null;
				strPassword = null;
			}
			return false;
		} else {
			if (strPassword == null) {
				// they are not logged in
				// they cannot issue any other commands
				out.println("500 Unauthenticated Client:  Please Log In");
				return false;
			}
		}

		// these are the other possible commands
		if (command.equalsIgnoreCase("LASTMSG")) {
			out.println("200 LastMessage: " + (messages.size() - 1));
			return false;
		} else if (command.equalsIgnoreCase("GETMSG")) {
			int id = (new Integer(arguments)).intValue();
			if (id < messages.size()) {
				String msg = (String) messages.elementAt(id);
				out.println("200 Message #" + id + ": " + msg);
			} else {
				out.println("400 Message Does Not Exist");
			}
			return false;
		} else if (command.equalsIgnoreCase("ADDMSG")) {
			int id = -1;
			synchronized (this) {
				messages.addElement("[" + strUserID + "]: " + arguments);
				id = messages.size() - 1;
			}
			out.println("200 Message Sent: " + id);
			return false;
		} else if (command.equalsIgnoreCase("LOGOUT")) {
			out.println("200 Client Logged Out");
			return true;
		} else {
			out.println("400 Unrecognized Command: " + command);
			return false;
		}
	}

}