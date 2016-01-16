package jdrivetrack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

public class Ring extends JPanel {
	private static final long serialVersionUID = 4806005617499210593L;
	
	private Point.Double upperLeftLonLat = new Point.Double();
	private Point.Double lowerRightLonLat = new Point.Double();
	private Point.Double point = new Point.Double();
	private double diameter;
	private Color color = Color.RED;

	public Ring(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, 
			double diameter, Dimension mapSize, Color color) {
		this.color = color;
		this.diameter = diameter;
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		setSize(mapSize);
		setLayout(null);
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
		double leftToRightDegrees = Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
		return getSize().width - ((lowerRightLonLat.x - longitude) * 
				(getSize().width / leftToRightDegrees));
	}

	private double latitudeToY(double latitude) {
		double topToBottomDegrees = Math.abs(upperLeftLonLat.y - lowerRightLonLat.y);
		return getSize().height + ((lowerRightLonLat.y - latitude) * 
				(getSize().height / topToBottomDegrees));
	}

	public void setCornerLonLat(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat) {
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		repaint();
	}

	private double metersToPixels(double meters, double direction) {
		double degrees = Vincenty.metersToDegrees(meters, direction, upperLeftLonLat.y);
		return pixelsPerDegree() * degrees; 
	}
	
	private double pixelsPerDegree() {
		return getWidth() / Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
	}
	
	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (point != null && upperLeftLonLat != null && lowerRightLonLat != null && diameter != 0) {
        		double d = metersToPixels(diameter, 90);
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        	Ellipse2D ring = new Ellipse2D.Double(longitudeToX(point.x) - (d / 2.0),
					latitudeToY(point.y) - (d / 2.0), d, d);
				g.setColor(color);
				g.draw(ring);
        	}
        } finally {
        	g.dispose();
        }
	}

}


