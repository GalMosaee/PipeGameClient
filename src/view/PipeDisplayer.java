package view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.awt.*;

public class PipeDisplayer extends Canvas {
	private char[][] pipeData;
	private StringProperty theme;
	private Image _bg; //background
	private Image _s; //start
	private Image _g; //goal
	private Image _L;
	private Image _F;
	private Image _7;
	private Image _J;
	private Image _P; //Pipeline "|"
	private Image _M; //Minus "-"
	private MediaPlayer mediaPlayer;
	
	public PipeDisplayer() {
		theme=new SimpleStringProperty();
		_bg = null;
		_s = null;
		_g = null;
		_L = null;
		_F = null;
		_7 = null;
		_J = null;
		_P = null;
		_M = null;
		mediaPlayer = null;
	}
	public void loadImages() {	
		try {
			_bg = new Image(new FileInputStream(theme.get()+ "/Background.png"));
			_s = new Image(new FileInputStream(theme.get()+ "/s.png"));
			_g = new Image(new FileInputStream(theme.get()+ "/g.png"));
			_L = new Image(new FileInputStream(theme.get()+ "/L.png"));
			_F = new Image(new FileInputStream(theme.get()+ "/F.png"));
			_7 = new Image(new FileInputStream(theme.get()+ "/7.png"));
			_J = new Image(new FileInputStream(theme.get()+ "/J.png"));
			_M = new Image(new FileInputStream(theme.get()+ "/Minus.png"));
			_P = new Image(new FileInputStream(theme.get()+ "/Pipeline.png"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void loadMusic() {
		try {
			Media media = new Media(new File(theme.get()+"/Music.mp3").toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setCycleCount(Integer.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stopMusic() {
		mediaPlayer.stop();
	}
	
	public void playMusic() {
		mediaPlayer.play();
	}
	 @Override
    public double minHeight(double width) {
        return 64;
    }

    @Override
    public double maxHeight(double width) {
        return 1000;
    }

    @Override
    public double prefHeight(double width) {
        return minHeight(width);
    }

    @Override
    public double minWidth(double height) {
        return 0;
    }

    @Override
    public double maxWidth(double height) {
        return 10000;
    }

    @Override
    public void resize(double width, double height) {
        super.setWidth(width);
        super.setHeight(height);
        this.redraw();
    }

	@Override
	public boolean isResizable() {
	    return true;
	}
	
	public int getPipeDataHeight() {
		return pipeData.length;
	}
	
	public int getPipeDataWidth() {
		return pipeData[0].length;
	}

	public char[][] getPipeData() {
		return pipeData;
	}
	public String getTheme() {
		return theme.get();
	}

	public void setTheme(String theme) {
		this.theme.set(theme);
	}

	public void setPipeData(char[][] pipeData) {
		this.pipeData = pipeData;
		redraw();
		if(mediaPlayer != null) {
			stopMusic();
		}
		loadMusic();
		playMusic();
	}
	
	public void redraw() {
		if(pipeData != null) {
			double W = getWidth();
			double H = getHeight();
			double w = W / pipeData[0].length;
			double h = H / pipeData.length;
			GraphicsContext gc = getGraphicsContext2D();
			loadImages();
			gc.drawImage(_bg, 0, 0, W, H);
			for(int i=0;i<pipeData.length;i++) {
				for(int j=0;j<pipeData[i].length;j++) {
					if(pipeData[i][j] =='s')
						gc.drawImage(_s, j*w, i*h, w, h);
					if(pipeData[i][j] =='g')
						gc.drawImage(_g, j*w, i*h, w, h);
					if(pipeData[i][j] =='L')
						gc.drawImage(_L, j*w, i*h, w, h);
					if(pipeData[i][j] =='F')
						gc.drawImage(_F, j*w, i*h, w, h);
					if(pipeData[i][j] =='7')
						gc.drawImage(_7, j*w, i*h, w, h);
					if(pipeData[i][j] =='J')
						gc.drawImage(_J, j*w, i*h, w, h);
					if(pipeData[i][j] =='|')
						gc.drawImage(_P, j*w, i*h, w, h);
					if(pipeData[i][j] =='-')
						gc.drawImage(_M, j*w, i*h, w, h);
					else
						gc.drawImage(null, j*w, i*h, w, h);
				}
			}
		}
	}
	
	public boolean rotate(int i, int j)
	{
		boolean flag = false;
		if (pipeData == null || i < 0 || j < 0 || i > pipeData[0].length || j > pipeData.length)
			return flag;
		char pipe = pipeData[j][i];
		if (pipe == '|') {
			pipe = '-';
			flag = true;
		}		
		else if (pipe == '-') {
			pipe = '|';
			flag = true;
		}
		else if (pipe == 'L') {
			pipe = 'F';
			flag = true;
		}
		else if (pipe == 'F') {
			pipe = '7';
			flag = true;
		}
		else if (pipe == '7') {
			pipe = 'J';
			flag = true;
		}
		else if (pipe == 'J') {
			pipe = 'L';
			flag = true;
		}
		pipeData[j][i] = pipe;
		return flag;
	}
	//Check if the board is in Goal state (Possible path from s to g). Using recIsGoal().
	public boolean isGoal()
	{
		int i=0;
		int j=0;
		outerLoop:
		for (i=0; i<getPipeDataHeight(); i++) {
			for (j=0; i<getPipeDataWidth(); j++) {
				if (pipeData[i][j] == 's')
				{
					break outerLoop;
				}
			}
		}
		Point start = new Point(i,j);
		boolean[][] visited = new boolean[getPipeDataHeight()][getPipeDataWidth()];
		return recIsGoal(visited,start);
	}
	//Recurse check for isGoal() (Dynamic Algorithm).
	private boolean recIsGoal(boolean[][] visited,Point p)
	{
		if(pipeData[p.x][p.y] == 'g') { return true; }
		visited[p.x][p.y] = true;
		boolean flag = false;
		if(pipeData[p.x][p.y] == '|') {
			p.x++;
			if ((p.x < getPipeDataHeight())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'J')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x-=2;
			if ((p.x >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == '7')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x++;
		}
		else if(pipeData[p.x][p.y] == '-')
		{
			p.y++;
			if ((p.y < getPipeDataWidth())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'J')||(pipeData[p.x][p.y] == '7')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y-=2;
			if ((p.y >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y++;
		}
		else if(pipeData[p.x][p.y] == 'L')
		{
			p.x--;
			if ((p.x >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == '7')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x++;
			p.y++;
			if ((p.y < getPipeDataWidth())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'J')||(pipeData[p.x][p.y] == '7')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y--;
		}
		else if(pipeData[p.x][p.y] == 'F')
		{
			p.x++;
			if ((p.x < getPipeDataHeight())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'J')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x--;
			p.y++;
			if ((p.y < getPipeDataWidth())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'J')||(pipeData[p.x][p.y] == '7')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y--;
		}
		else if(pipeData[p.x][p.y] == '7')
		{
			p.x++;
			if ((p.x < getPipeDataHeight())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'J')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x--;
			p.y--;
			if ((p.y >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y++;
		}
		else if(pipeData[p.x][p.y] == 'J')
		{
			p.x--;
			if ((p.x >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == '7')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x++;
			p.y--;
			if ((p.y >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y++;
		}
		else if(pipeData[p.x][p.y] == 's')
		{
			p.x--;
			if ((p.x >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == '7')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x+=2;
			if ((p.x < getPipeDataHeight())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'J')||
					(pipeData[p.x][p.y] == '|')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.x--;
			p.y--;
			if ((p.y >= 0)&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'L')||(pipeData[p.x][p.y] == 'F')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y+=2;
			if ((p.y < getPipeDataWidth())&&(!visited[p.x][p.y])&&
					((pipeData[p.x][p.y] == 'J')||(pipeData[p.x][p.y] == '7')||
					(pipeData[p.x][p.y] == '-')||(pipeData[p.x][p.y] == 'g'))) {
				if(recIsGoal(visited,p)) { flag = true; }
			}
			p.y--;
		}
		return flag;
	}
}
