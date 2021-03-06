package sample;

import java.io.*;
import java.net.*;
import java.util.Vector;

public class ChatServer {
	protected Socket clientSocket = null;
	protected ServerSocket serverSocket = null;
	protected ChatServerThread[] threads = null;
	protected int numClients = 0;
	protected Vector messages = new Vector();

	public static int SERVER_PORT = 8000;
	public static int MAX_CLIENTS = 25;

	public ChatServer() {//chat server for the project
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			System.out.println("---------------------------");
			System.out.println("Chat Server Application is running");
			System.out.println("---------------------------");
			System.out.println("Listening to port: " + SERVER_PORT);
			threads = new ChatServerThread[MAX_CLIENTS];
			while (true) {
				clientSocket = serverSocket.accept();
				System.out.println("Client #" + (numClients + 1) + " connected.");
				threads[numClients] = new ChatServerThread(clientSocket, messages);
				threads[numClients].start();
				numClients++;
			}
		} catch (IOException e) {
			System.err.println("IOException while creating server connection");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChatServer app = new ChatServer();
	}
}
