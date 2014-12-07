package com;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;

public class SignalMeterArray extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private Rectangle meter0 = new Rectangle();
	private Rectangle meter1 = new Rectangle();
	private Rectangle meter2 = new Rectangle();
	private Rectangle meter3 = new Rectangle();
	private Rectangle meter4 = new Rectangle();
	private Rectangle meter5 = new Rectangle();
	private Rectangle meter6 = new Rectangle();
	private Rectangle meter7 = new Rectangle();
	private Rectangle meter8 = new Rectangle();
	private Rectangle meter9 = new Rectangle();
	private Rectangle frame0 = new Rectangle();
	private Rectangle frame1 = new Rectangle();
	private Rectangle frame2 = new Rectangle();
	private Rectangle frame3 = new Rectangle();
	private Rectangle frame4 = new Rectangle();
	private Rectangle frame5 = new Rectangle();
	private Rectangle frame6 = new Rectangle();
	private Rectangle frame7 = new Rectangle();
	private Rectangle frame8 = new Rectangle();
	private Rectangle frame9 = new Rectangle();
	private int level0 = 0;
	private int level1 = 0;
	private int level2 = 0;
	private int level3 = 0;
	private int level4 = 0;
	private int level5 = 0;
	private int level6 = 0;
	private int level7 = 0;
	private int level8 = 0;
	private int level9 = 0;
	private Color color0 = Color.BLUE;
	private Color color1 = Color.BLUE;
	private Color color2 = Color.BLUE;
	private Color color3 = Color.BLUE;
	private Color color4 = Color.BLUE;
	private Color color5 = Color.BLUE;
	private Color color6 = Color.BLUE;
	private Color color7 = Color.BLUE;
	private Color color8 = Color.BLUE;
	private Color color9 = Color.BLUE;
	
	public SignalMeterArray() {
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(true);
	}

	public void setLevelMeter0(int newLevel) {
		level0 = newLevel;
		repaint();
	}
	public void setLevelMeter1(int newLevel) {
		level1 = newLevel;
		repaint();
	}
	public void setLevelMeter2(int newLevel) {
		level2 = newLevel;
		repaint();
	}	
	public void setLevelMeter3(int newLevel) {
		level3 = newLevel;
		repaint();
	}	
	public void setLevelMeter4(int newLevel) {
		level4 = newLevel;
		repaint();
	}	
	public void setLevelMeter5(int newLevel) {
		level5 = newLevel;
		repaint();
	}
	public void setLevelMeter6(int newLevel) {
		level6 = newLevel;
		repaint();
	}
	public void setLevelMeter7(int newLevel) {
		level7 = newLevel;
		repaint();
	}	
	public void setLevelMeter8(int newLevel) {
		level8 = newLevel;
		repaint();
	}	
	public void setLevelMeter9(int newLevel) {
		level9 = newLevel;
		repaint();
	}		
	public void setColorMeter0(Color newColor) {
		color0 = newColor;
		repaint();
	}
	public void setColorMeter1(Color newColor) {
		color1 = newColor;
		repaint();
	}
	public void setColorMeter2(Color newColor) {
		color2 = newColor;
		repaint();
	}	
	public void setColorMeter3(Color newColor) {
		color3 = newColor;
		repaint();
	}	
	public void setColorMeter4(Color newColor) {
		color4 = newColor;
		repaint();
	}	
	public void setColorMeter5(Color newColor) {
		color5 = newColor;
		repaint();
	}
	public void setColorMeter6(Color newColor) {
		color6 = newColor;
		repaint();
	}
	public void setColorMeter7(Color newColor) {
		color7 = newColor;
		repaint();
	}	
	public void setColorMeter8(Color newColor) {
		color8 = newColor;
		repaint();
	}	
	public void setColorMeter9(Color newColor) {
		color9 = newColor;
		repaint();
	}	

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		meter0.setSize(getWidth() / 15, level0 * (getHeight() - 30) / 100);
		meter0.setLocation((getWidth() / 11 * 1) - 4, 18 + (getHeight() - 30) - (level0 * (getHeight() - 30) / 100));
		g2.setColor(color0);
		g2.fill(meter0);
		g2.draw(meter0);
		meter1.setSize(getWidth() / 15, level1 * (getHeight() - 30) / 100);
		meter1.setLocation((getWidth() / 11 * 2) - 4, 18 + (getHeight() - 30) - (level1 * (getHeight() - 30) / 100));
		g2.setColor(color1);
		g2.fill(meter1);
		g2.draw(meter1);
		meter2.setSize(getWidth() / 15, level2 * (getHeight() - 30) / 100);
		meter2.setLocation((getWidth() / 11 * 3) - 4, 18 + (getHeight() - 30) - (level2 * (getHeight() - 30) / 100));
		g2.setColor(color2);
		g2.fill(meter2);
		g2.draw(meter2);
		meter3.setSize(getWidth() / 15, level3 * (getHeight() - 30) / 100);
		meter3.setLocation((getWidth() / 11 * 4) - 4, 18 + (getHeight() - 30) - (level3 * (getHeight() - 30) / 100));
		g2.setColor(color3);
		g2.fill(meter3);
		g2.draw(meter3);
		meter4.setSize(getWidth() / 15, level4 * (getHeight() - 30) / 100);
		meter4.setLocation((getWidth() / 11 * 5) - 4, 18 + (getHeight() - 30) - (level4 * (getHeight() - 30) / 100));
		g2.setColor(color4);
		g2.fill(meter4);
		g2.draw(meter4);
		meter5.setSize(getWidth() / 15, level5 * (getHeight() - 30) / 100);
		meter5.setLocation((getWidth() / 11 * 6) - 4, 18 + (getHeight() - 30) - (level5 * (getHeight() - 30) / 100));
		g2.setColor(color5);
		g2.fill(meter5);
		g2.draw(meter5);		
		meter6.setSize(getWidth() / 15, level6 * (getHeight() - 30) / 100);
		meter6.setLocation((getWidth() / 11 * 7) - 4, 18 + (getHeight() - 30) - (level6 * (getHeight() - 30) / 100));
		g2.setColor(color6);
		g2.fill(meter6);
		g2.draw(meter6);
		meter7.setSize(getWidth() / 15, level7 * (getHeight() - 30) / 100);
		meter7.setLocation((getWidth() / 11 * 8) - 4, 18 + (getHeight() - 30) - (level7 * (getHeight() - 30) / 100));
		g2.setColor(color7);
		g2.fill(meter7);
		g2.draw(meter7);
		meter8.setSize(getWidth() / 15, level8 * (getHeight() - 30) / 100);
		meter8.setLocation((getWidth() / 11 * 9) - 4, 18 + (getHeight() - 30) - (level8 * (getHeight() - 30) / 100));
		g2.setColor(color8);
		g2.fill(meter8);
		g2.draw(meter8);
		meter9.setSize(getWidth() / 15, level9 * (getHeight() - 30) / 100);
		meter9.setLocation((getWidth() / 11 * 10) - 4, 18 + (getHeight() - 30) - (level9 * (getHeight() - 30) / 100));
		g2.setColor(color9);
		g2.fill(meter9);
		g2.draw(meter9);
		frame0.setSize(getWidth() / 15, getHeight() - 30);
		frame0.setLocation((getWidth() / 11 * 1) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame0);
		frame1.setSize(getWidth() / 15, getHeight() - 30);
		frame1.setLocation((getWidth() / 11 * 2) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame1);
		frame2.setSize(getWidth() / 15, getHeight() - 30);
		frame2.setLocation((getWidth() / 11 * 3) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame2);
		frame3.setSize(getWidth() / 15, getHeight() - 30);
		frame3.setLocation((getWidth() / 11 * 4) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame3);
		frame4.setSize(getWidth() / 15, getHeight() - 30);
		frame4.setLocation((getWidth() / 11 * 5) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame4);
		frame5.setSize(getWidth() / 15, getHeight() - 30);
		frame5.setLocation((getWidth() / 11 * 6) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame5);		
		frame6.setSize(getWidth() / 15, getHeight() - 30);
		frame6.setLocation((getWidth() / 11 * 7) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame6);
		frame7.setSize(getWidth() / 15, getHeight() - 30);
		frame7.setLocation((getWidth() / 11 * 8) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame7);
		frame8.setSize(getWidth() / 15, getHeight() - 30);
		frame8.setLocation((getWidth() / 11 * 9) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame8);
		frame9.setSize(getWidth() / 15, getHeight() - 30);
		frame9.setLocation((getWidth() / 11 * 10) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
		g2.setColor(Color.lightGray);
		g2.draw(frame9);
		
	}

}