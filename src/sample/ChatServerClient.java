package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.canvas.Canvas;
import java.io.*;
import java.net.*;

public class ChatServerClient extends Application {
	private Scene mainScene;
	private Canvas canvas;
	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter networkOut = null;
	private BufferedReader networkIn = null;

	// we can read this from the user too
	public static String SERVER_ADDRESS = "localhost";
	public static int SERVER_PORT = 8000;

	public ChatServerClient() {
		try {
			socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + SERVER_ADDRESS);
		} catch (IOException e) {
			System.err.println("IOEXception while connecting to server: " + SERVER_ADDRESS);
		}
		if (socket == null) {
			System.err.println("socket is null");
		}
		try {
			networkOut = new PrintWriter(socket.getOutputStream(), true);
			networkIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.err.println("IOEXception while opening a read/write connection");
		}
	}

	/**
	 * @param primaryStage
	 * @throws Exception
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		ChatServerClient client = new ChatServerClient();
		String input = null;
		String message = null;
		int errorCode = 0;

		try {
			message = networkIn.readLine(); // Welcome to chat
			message = networkIn.readLine(); // 200 Message serves is ready
		} catch (IOException e) {
			System.err.println("Error reading initial greeting from socket.");
		}
		primaryStage.setTitle("CSCI2020U - Group Assignment");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);

		// Creating the menu buttons
		Button btApp1 = new Button("Type Username and Click");
		btApp1.setPrefWidth(200);
		TextField text = new TextField();
		text.setPrefWidth(200);
		btApp1.setTextFill(Color.BLUEVIOLET);
		grid.setStyle("-fx-background-color:#808080;");

		// Add the menu buttons to the grid
		grid.add(btApp1, 0, 1);
		grid.add(text, 0, 2);

		// main App Scene

		btApp1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				String txtField = text.getText();
				String input;
				String message;
				input = txtField;
				if (input.equalsIgnoreCase("quit")) {
					System.exit(0);
				}
				networkOut.println("UID " + input);
				try {
					message = networkIn.readLine();
				} catch (IOException e) {
					System.err.println("Error reading response to UID.");
				}

				GridPane grid2 = new GridPane();
				grid2.setAlignment(Pos.CENTER);
				grid2.setHgap(10);
				grid2.setVgap(10);

				// Creating the menu buttons
				Button passButton = new Button("Type Password and Click");
				passButton.setPrefWidth(200);
				TextField passText = new TextField();
				passText.setPrefWidth(200);
				passButton.setTextFill(Color.BLUEVIOLET);
				grid2.setStyle("-fx-background-color:#808080;");

				// Add the menu buttons to the grid
				grid2.add(passButton, 0, 1);
				grid2.add(passText, 0, 2);

				Scene nextScene = new Scene(grid2, 300, 275);
				primaryStage.hide();
				primaryStage.setScene(nextScene);
				primaryStage.show();

				passButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent actionEvent) {
						String txtField = passText.getText();
						String input;
						String message;
						input = txtField;
						networkOut.println("PWD " + input);
						try {
							message = networkIn.readLine();
						} catch (IOException e) {
							System.err.println("Error reading response to UID.");
						}
						if (txtField.equals("password")) {
							GridPane grid2 = new GridPane();
							grid2.setAlignment(Pos.CENTER);
							grid2.setHgap(10);
							grid2.setVgap(10);

							// Creating the menu buttons
							Button sendButton = new Button("Enter Message and Click to Send");
							TextField sendText = new TextField();
							Button refresh = new Button("Click to Refresh Messages");
							TextArea msgArea = new TextArea("");
							msgArea.setMinSize(100, 300);

							sendButton.setTextFill(Color.BLUEVIOLET);
							refresh.setTextFill(Color.BLUEVIOLET);
							grid2.setStyle("-fx-background-color:#808080;");

							// Add the menu buttons to the grid
							grid2.add(msgArea, 0, 1);
							grid2.add(sendButton, 0, 10);
							grid2.add(sendText, 0, 11);
							grid2.add(refresh, 0, 12);

							Scene nextScene2 = new Scene(grid2, 600, 600);
							primaryStage.hide();
							primaryStage.setScene(nextScene2);
							primaryStage.show();

							sendButton.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent actionEvent) {
									String msgField = sendText.getText();
									String text = "";
									String message = null;
									String input = null;
									try {
										input = msgField;
									} finally {
									}
									networkOut.println("ADDMSG " + input);
									// read and ignore response
									try {
										message = networkIn.readLine();
									} catch (IOException e) {
										System.err.println("Error reading from socket.");
									}

									message = null;

									networkOut.println("LASTMSG");

									int id = -1;
									try {
										message = networkIn.readLine();
									} catch (IOException e) {
										System.err.println("Error reading from socket.");
									}
									String strID = message.substring(message.indexOf(':') + 1);
									id = (new Integer(strID.trim())).intValue();
									for (int i = id; i >= 0; i--) {
										networkOut.println("GETMSG " + i);
										try {
											message = networkIn.readLine();
										} catch (IOException e) {
											System.err.println("Error reading from socket.");
										}
										int index = message.indexOf(':') + 1;
										String msg = message.substring(index);
										text = text + "    " + msg + "\n";
									}
									msgArea.setText(text);
									// send message to server and refreshlist
									;
								}
							});
							refresh.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent actionEvent) {
									String message = null;
									String text = "";

									networkOut.println("LASTMSG");

									int id = -1;
									try {
										message = networkIn.readLine();
									} catch (IOException e) {
										System.err.println("Error reading from socket.");
									}
									String strID = message.substring(message.indexOf(':') + 1);
									id = (new Integer(strID.trim())).intValue();
									for (int i = id; i >= 0; i--) {
										networkOut.println("GETMSG " + i);
										try {
											message = networkIn.readLine();
										} catch (IOException e) {
											System.err.println("Error reading from socket.");
										}
										int index = message.indexOf(':') + 1;
										String msg = message.substring(index);
										text = text + "    " + msg + "\n";
									}
									msgArea.setText(text);
									;
								}
							});
						} else {
							primaryStage.hide();
							primaryStage.setScene(nextScene);
							primaryStage.show();
						}
						;
					}
				});
				;
			}
		});
		Scene mainScene = new Scene(grid, 300, 275);
		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
		System.exit(0);
	}
}
