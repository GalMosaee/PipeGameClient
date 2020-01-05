package view;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
//import java.beans.EventHandler;
import java.io.BufferedReader;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
//import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
//import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
//import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
//import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
//import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class MainWindowController implements Initializable {

	@FXML
	PipeDisplayer pipeDisplayer;

	char[][] pipeData = { { 's', 'L', '-', ' ' }, { '|', 'F', 'J', '|' }, { ' ', '7', '-', '-' },
			{ ' ', '7', 'L', 'g' }, };
	int moves = 0;
	int secondsPlay = 0;
	Timeline timer;

	private SettingsController ServerSettings = new SettingsController();

	@FXML
	private TextField TextMoves = new TextField();

	@FXML
	private TextField TextSeconds = new TextField();
	
	@FXML
	private Label TextConnectionStatus = new Label();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		pipeDisplayer.setPipeData(pipeData);
		TextMoves.setText(Integer.toString(moves));
		TextConnectionStatus.setText("Disconnected");
		initTime();
		UpdateMouseSync();
		Task task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				while(true) {
					if (ServerSettings != null && ServerSettings.IsConnected())
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								TextConnectionStatus.setText("Connected");	
							}
						});
					else
						Platform.runLater(new Runnable() {@Override
							public void run() {
							// TODO Auto-generated method stub
							TextConnectionStatus.setText("Disconnected");
							}
						});
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}};
		new Thread(task).start();
	}

	private void UpdateMouseSync() {
		pipeDisplayer.setOnMouseClicked((MouseEvent t) -> {
			if (pipeData == null)
				return;
			double w = pipeDisplayer.getWidth() / pipeData[0].length;
			double h = pipeDisplayer.getHeight() / pipeData.length;
			int i = (int) (t.getX() / w);
			int j = (int) (t.getY() / h);
			if (pipeDisplayer.rotate(i, j)) {
				pipeDisplayer.redraw();
				moves++;
				TextMoves.setText(Integer.toString(moves));
			}
		});
	}

	@FXML
	private void antTheme() {
		if (pipeDisplayer.getTheme() == "./resources/Ant")
			return;
		pipeDisplayer.setTheme("./resources/Ant");
		pipeDisplayer.loadImages();
		pipeDisplayer.stopMusic();
		pipeDisplayer.loadMusic();
		pipeDisplayer.redraw();
		pipeDisplayer.playMusic();
	}

	@FXML
	private void carTheme() {
		if (pipeDisplayer.getTheme() == "./resources/Car")
			return;
		pipeDisplayer.setTheme("./resources/Car");
		pipeDisplayer.loadImages();
		pipeDisplayer.stopMusic();
		pipeDisplayer.loadMusic();
		pipeDisplayer.redraw();
		pipeDisplayer.playMusic();
	}

	public void changeTheme(String theme) {
		this.pipeDisplayer.setTheme(theme);
	}

	public void initTime() {
		if (timer != null) {
			timer.stop();
		}
		TextSeconds.setText(Integer.toString(secondsPlay));
		timer = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
			secondsPlay++;
			TextSeconds.setText(Integer.toString(secondsPlay));
		}));
		timer.setCycleCount(Animation.INDEFINITE);
		timer.play();
	}

	public void checkCompletion() {
		Alert completionStatus;
		if (pipeDisplayer.isGoal()) {
			completionStatus = new Alert(AlertType.INFORMATION);
			completionStatus.setTitle("Completion Status");
			completionStatus.setHeaderText(null);
			completionStatus.getDialogPane().setPrefSize(300, 130);
			completionStatus.setContentText("Congratulations!\nYou successfully finished this stage." + "\nMoves: "
					+ moves + "\nTime: " + secondsPlay);
		} else {
			completionStatus = new Alert(AlertType.ERROR);
			completionStatus.setTitle("Completion Status");
			completionStatus.setHeaderText(null);
			completionStatus.getDialogPane().setPrefSize(300, 100);
			completionStatus.setContentText("You did not finish this stage yet.");
		}
		completionStatus.showAndWait();
	}

	public void solveProblem() {
		int[][] solution = null;
		if (ServerSettings != null)
			solution = ServerSettings.solveProblem(pipeData);
		if (solution != null)
			rotateSolutionAnimation(solution);
	}

	public void rotateSolutionAnimation(int[][] solution) {
		Task task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				for (int i = 0; i < solution.length; i++) {
					for (int j = 0; j < solution[i][2]; j++) {
						System.out.println(String.format("%d%d%d",solution[i][0],solution[i][1],solution[i][2]));
						pipeDisplayer.rotate(solution[i][1], solution[i][0]);
						pipeDisplayer.redraw();
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
		new Thread(task).start();
	}

	public void openFile() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Open Stage File");
		fc.setInitialDirectory(new File("./resources/Stages"));
		FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
		fc.getExtensionFilters().add(txtFilter);
		fc.setSelectedExtensionFilter(txtFilter);
		File chosen = fc.showOpenDialog(null);
		if (chosen != null) {
			if (chosen.getName().endsWith(".txt")) {
				loadStage(chosen.getPath());
			}
		}
	}

	public void saveFile() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save Stage File");
		fc.setInitialDirectory(new File("./resources/Stages"));
		FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
		fc.getExtensionFilters().add(txtFilter);
		fc.setSelectedExtensionFilter(txtFilter);
		File chosen = fc.showSaveDialog(null);

		if (chosen != null) {
			saveStage(chosen);
		}
	}

	public void openSettingssWindow() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("ConnectionSettingsWindow.fxml"));
			Scene scene = new Scene(fxmlLoader.load(), 400, 150);
			ServerSettings = fxmlLoader.getController();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Connection Settings");
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadStage(String file) {
		List<char[]> boardBuilder = new ArrayList<char[]>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			boolean movesFlag = false;
			boolean secondsFlag = false;
			while ((line = reader.readLine()) != null) {
				if (line.contains("Seconds:")) {
					this.secondsPlay = Integer.parseInt(line.split(":")[1]);
					secondsFlag = true;
				} else if (line.contains("Moves:")) {
					this.moves = Integer.parseInt(line.split(":")[1]);
					movesFlag = true;
				} else {
					boardBuilder.add(line.toCharArray());
				}
			}
			if (!movesFlag) {
				moves = 0;
			}
			if (!secondsFlag) {
				secondsPlay = 0;
			}
			pipeData = boardBuilder.toArray(new char[boardBuilder.size()][]);
			initialize(null, null);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveStage(File file) {
		try {
			PrintWriter outFile = new PrintWriter(file);
			outFile.println("Moves:" + moves);
			outFile.println("Seconds:" + secondsPlay);
			for (int i = 0; i < pipeDisplayer.getPipeDataHeight(); i++) {
				outFile.println(new String(pipeDisplayer.getPipeData()[i]));
			}
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
