package jdrivetrack;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Arrays;
import javax.swing.JPanel;

public class SignalMeterArray extends JPanel {
	private static final long serialVersionUID = -5011237089698705502L;
	
	private Rectangle[] meter = new Rectangle[10];
	private Rectangle[] frame = new Rectangle[10];
	private Integer[] meterLevel = new Integer[10];
	private Color[] meterColor = new Color[10];
	
	public SignalMeterArray() {
		for (int i = 0; i < meter.length; i++) {
			meter[i] = new Rectangle();
			frame[i] = new Rectangle();
			meterLevel[i] = 0;
			meterColor[i] = Color.LIGHT_GRAY;
		}
	}

	public void setMeterLevels(Integer[] percent) {
		for (int i = 0; i < 10; i++) {
			meterLevel[i] = Math.max(Math.min(percent[i], 100), 0);
		}
		repaint();
	}
	
	public void setMeterLevels(Integer percent) {
		Arrays.fill(meterLevel, Math.max(Math.min(percent, 100), 0));
		repaint();
	}
	
	public void setMeterLevel(int index, int percent) {
		meterLevel[index] = Math.max(Math.min(percent, 100), 0);
		repaint();
	}

	public void setMeterColors(Color[] color) {
		for (int i = 0; i < meter.length; i++) {
			meterColor[i] = color[i];
		}
		repaint();
	}
	
	public void setMeterColor(Color color) {
		for (int i = 0; i < meter.length; i++) {
			meterColor[i] = color;
		}
		repaint();
	}		

	public void setMeterColor(int index, Color color) {
		meterColor[Math.max(Math.min(index, 9), 0)] = color;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int i = 0; i < meter.length; i++) {
				meter[i].setSize(getWidth() / 15, meterLevel[i] * (getHeight() - 30) / 100);
				meter[i].setLocation((getWidth() / 11 * (i+1)) - 4, 18 + (getHeight() - 30) - (meterLevel[i] * (getHeight() - 30) / 100));
				g2.setColor(meterColor[i]);
				g2.fill(meter[i]);
				g2.draw(meter[i]);
				frame[i].setSize(getWidth() / 15, getHeight() - 30);
				frame[i].setLocation((getWidth() / 11 * (i+1)) - 4, getHeight() - (100 * (getHeight() - 18) / 100));
				g2.setColor(Color.lightGray);
				g2.draw(frame[i]);	
			}
		} finally {
			g2.dispose();
		}
	}
	
}