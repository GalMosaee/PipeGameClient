package view;

import java.awt.Button;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

public class SettingsController implements Initializable{

	private static String ip;
	private static String port;
	private static BufferedReader in;
	private static PrintWriter out;
	private static Boolean IsConnected = false;
	private static Socket server;
	
	@FXML
	private Button saveButton = new Button();
	@FXML
	private TextField TextServerIp = new TextField();
	
	@FXML
	private TextField TextPort = new TextField();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		IsConnected = false;
		if (ip != null && !ip.isEmpty())
			TextServerIp.setText(ip);
		if (port != null && !port.isEmpty())
			TextPort.setText(port);
	}
	
	public boolean IsConnected()
	{
		return IsConnected;
	}

	public void connectToServer()
	{
		Alert completionStatus;
		String portPatternCheck = "^[0-9]{0,5}$";
		String serverIpPatternCheck = 
				"^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])[.]){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$";
		Pattern p = Pattern.compile(portPatternCheck);
		Matcher m1 = p.matcher(TextPort.getText());
		p = Pattern.compile(serverIpPatternCheck);
		Matcher m2 = p.matcher(TextServerIp.getText());
		if (!m1.find() || !m2.find())
		{
			completionStatus = new Alert(AlertType.ERROR);
			completionStatus.setTitle("Unavailable Inputs");
			completionStatus.setHeaderText(null);
			completionStatus.getDialogPane().setPrefSize(300, 100);
			completionStatus.setContentText("Please check that the Ip and port\nare in correct syntax and ranges.");
			completionStatus.showAndWait();
		}
		else
		{
			ip = TextServerIp.getText();
			port = TextPort.getText();
			String serverIp = TextServerIp.getText();
			int port = Integer.parseInt(TextPort.getText());
			server=null;
			out=null;
			in=null;
			try{

				server = new Socket("127.0.0.1",port);
				server.setSoTimeout(100*1000);
				out=new PrintWriter(server.getOutputStream());
				in=new BufferedReader(new InputStreamReader(server.getInputStream()));
				
				IsConnected = server.isConnected();
				completionStatus = new Alert(AlertType.INFORMATION);
				completionStatus.setTitle("Connect Information");
				completionStatus.setHeaderText(null);
				completionStatus.getDialogPane().setPrefSize(300, 100);
				completionStatus.setContentText("Connected succesfuly to the server\nNow can click solve!");
				completionStatus.showAndWait();
			}
			catch(Exception e)
			{
				IsConnected = false;
				completionStatus = new Alert(AlertType.ERROR);
				completionStatus.setHeaderText(null);
				completionStatus.setTitle("Connect Information");
				completionStatus.getDialogPane().setPrefSize(300, 100);
				completionStatus.setContentText("Could not connect to the server, check if the server is running.");
				completionStatus.showAndWait();
			}
		}	
	}

	public int[][] solveProblem(char[][] problem)
	{
		if (!IsConnected)
			return null;
		for (int i = 0; i < problem.length; i++) {
			System.out.println(problem[i]);
			out.println(problem[i]);
		}
		out.println("done");

		out.flush();
		ArrayList<String> lines = new ArrayList<>();
		String line = "";
		int maxWaitTimes = 2;
		
		while(line.toLowerCase() != "done" && maxWaitTimes > 0)
		{
			try {
				try {
					server.setSoTimeout(150);
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				line = "";
				line = in.readLine();
				if (line != null && line != "" && line.toLowerCase() != "done")
					lines.add(line);
				else {
					maxWaitTimes--;
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				maxWaitTimes--;

				try {
					Thread.sleep(10);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				e.printStackTrace();
			}
		}

		try {
			server.setSoTimeout(100*1000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int [][] solutionSteps = new int [lines.size()][3];
		for (int i = 0; i < solutionSteps.length; i++) {
			String[] solutionLine = lines.get(i).split(",");
			for (int j = 0; j < solutionLine.length; j++) {
				if (solutionLine[j].toLowerCase() != "done" && !solutionLine[j].toLowerCase().contains("done"))
					solutionSteps[i][j] = Integer.parseInt(solutionLine[j]);
			}
		}
		// reconnect
		try {
			if (server.isConnected())
				server.close();
			server = new Socket(ip, Integer.parseInt(port));
			server.setSoTimeout(100*1000);
			out=new PrintWriter(server.getOutputStream());
			in=new BufferedReader(new InputStreamReader(server.getInputStream()));
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return solutionSteps;
	}
}
