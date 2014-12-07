package com;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

public class Ring extends JPanel {
	private static final long serialVersionUID = 4806005617499210593L;
	
	private Point.Double upperLeftPoint = new Point.Double();
	private Point.Double lowerRightPoint = new Point.Double();
	private Point.Double point = new Point.Double();
	private double diameter;
	private Color color = Color.RED;

	public Ring() {
		this(new Point.Double(), new Point.Double(), new Point.Double(), 20, new Dimension(800,600), Color.RED);
	}
	
	public Ring(Dimension mapSize) {
		this(new Point.Double(), new Point.Double(), new Point.Double(), 20, mapSize, Color.RED);
	}
	
	public Ring(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double point, 
			double diameter, Dimension mapSize, Color color) {
		this.point = point;
		this.color = color;
		this.diameter = diameter;
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
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
	
	public void setColor(Color color) {
		this.color = color;
		repaint();
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
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

	public void setCornerLonLat(Point.Double upperLeftPoint, Point.Double lowerRightPoint) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		repaint();
	}

	private double metersToPixels(double m, double t) {
		Point2D.Double c = upperLeftPoint;
		Point2D.Double d = Vincenty.metersToDegrees(m, c.y);
		double dv = d.y - d.x;
		double f = ((100.0 / 90.0) * Math.abs(Math.abs(t - 180) - 90)) / 100;
		double dt = (f * dv) + d.x;
		return pixelsPerDegree() * dt;
	}
	
	private double pixelsPerDegree() {
		return getWidth() / Math.abs(upperLeftPoint.x - lowerRightPoint.x);
	}
	
	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (point != null && upperLeftPoint != null && lowerRightPoint != null && diameter != 0) {
	        	double d = metersToPixels(diameter, 90);
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        	Ellipse2D ring = new Ellipse2D.Double(longitudeToX(point.x) - (d / 2.0),
						latitudeToY(point.y) - (d / 2.0), d, d);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(color);
				g.draw(ring);
        	}
        } finally {
        	g.dispose();
        }
	}

}


