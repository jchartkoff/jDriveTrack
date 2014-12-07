package com;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class Line extends JPanel {
	private static final long serialVersionUID = 1L;
	private Point.Double upperLeftPoint = new Point.Double();
	private Point.Double lowerRightPoint = new Point.Double();
	private Point.Double p1 = new Point.Double();
	private Point.Double p2 = new Point.Double();
	private Color color = Color.RED;
	
	public Line(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double p1, Point.Double p2, Dimension mapSize, Color color) {
		this.p1 = p1;
		this.p2 = p2;
		this.color = color;
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		setSize(mapSize);
		setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(false);
	}

	public Line(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double p1, double angle, Dimension mapSize, Color color) {
		this(upperLeftPoint, lowerRightPoint, p1, new Point.Double((Math.sin(angle * Math.PI / 180) * 500) + p1.x,
				(Math.cos(angle * Math.PI / 180) * 500) + p1.y), mapSize, color);
	}

	public void setCornerLonLat(Point.Double upperLeftPoint, Point.Double lowerRightPoint) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		repaint();
	}

	private double longitudeToX(double longitude) {
		double leftToRightDegrees = Math.abs(upperLeftPoint.x - lowerRightPoint.x);
		return getSize().width - ((lowerRightPoint.x - longitude) * 
				(getSize().width / leftToRightDegrees));
	}

	private double latitudeToY(double latitude) {
		double topToBottomDegrees = Math.abs(upperLeftPoint.y - lowerRightPoint.y);
		return getSize().height + ((lowerRightPoint.y - latitude) * 
				(getSize().height / topToBottomDegrees));
	}

	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (p1 != null && p2 != null && upperLeftPoint != null && lowerRightPoint != null) {
	        	Line2D.Double line = new Line2D.Double(longitudeToX(p1.x), latitudeToY(p1.y),
					longitudeToX(p2.x), latitudeToY(p2.y));
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(color);
				g.draw(line);
        	}
        } finally {
        	g.dispose();
        }
	}
}

