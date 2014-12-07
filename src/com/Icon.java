package com;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Icon extends JPanel {
	private static final long serialVersionUID = 1L;
	private Point.Double upperLeftPoint = new Point.Double();
	private Point.Double lowerRightPoint = new Point.Double();
	private Point.Double point = new Point.Double();
	private boolean showIconLabel;
	private String iconPathName;
	private String iconLabel;
	BufferedImage iconImage;

	public Icon(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double point, 
			String iconPathName, String iconLabel, Dimension mapSize) {
		this.point = point;
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		this.point = point;
		this.iconPathName = iconPathName;
		this.iconLabel = iconLabel;
		setSize(mapSize);
		setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(false);
	}

	public void setLocation(Point.Double point) {
		this.point = point;
		repaint();
	}

	public void showIconLabel(boolean showIconLabel) {
		this.showIconLabel = showIconLabel;
		repaint();
	}

	public String getIconPathName() {
		return iconPathName;
	}
	
	public String getIconLabel() {
		return iconLabel;
	}
	
	public void setCornerLonLat(Point.Double upperLeftPoint, Point.Double lowerRightPoint) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		repaint();
	}

	private int longitudeToX(double longitude) {
		double leftToRightDegrees = Math.abs(upperLeftPoint.x - lowerRightPoint.x);
		return (int) Math.round(getSize().width - ((lowerRightPoint.x - longitude) * 
				(getSize().width / leftToRightDegrees)));
	}

	private int latitudeToY(double latitude) {
		double topToBottomDegrees = Math.abs(upperLeftPoint.y - lowerRightPoint.y);
		return (int) Math.round(getSize().height + ((lowerRightPoint.y - latitude) * 
				(getSize().height / topToBottomDegrees)));
	}

	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			iconImage = ImageIO.read(new File(iconPathName));
			g.drawImage(iconImage, longitudeToX(point.x),
					latitudeToY(point.y), null);
			if (showIconLabel) {
				g.drawString(iconLabel, longitudeToX(point.x), latitudeToY(point.y));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			g.dispose();
		}
	}
}

