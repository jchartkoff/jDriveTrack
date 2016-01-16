package jdrivetrack;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

public class Dot extends JPanel {
	private static final long serialVersionUID = -3730064920971859688L;
	
	private Point.Double upperLeftLonLat;
	private Point.Double lowerRightLonLat;
	private Point.Double point;
	private double diameter;
	private Color color;
	private float opacity = 0.5F;

	public Dot(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, double diameter, Dimension mapSize) {
		this.diameter = diameter;
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		setSize(mapSize);
		setLayout(new BorderLayout());
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
	
	public void setOpacity(float opacity) {
		this.opacity = opacity;
		repaint();
	}
	
	public void setDiameter(double diameter) {
		this.diameter = diameter;
		repaint();
	}

	private AlphaComposite makeComposite(float alpha) {
	    int type = AlphaComposite.SRC_OVER;
	    return(AlphaComposite.getInstance(type, alpha));
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

	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig;
        try {
        	if (point != null && upperLeftLonLat != null && lowerRightLonLat != null && diameter != 0) {
        		Composite originalComposite = g.getComposite();
	        	Ellipse2D.Double dot = new Ellipse2D.Double(longitudeToX(point.x) - (diameter / 2.0),
					latitudeToY(point.y) - (diameter / 2.0), diameter, diameter);	
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(color);
				g.setComposite(makeComposite(opacity));
				g.draw(dot);
				g.fill(dot);
				g.setComposite(originalComposite);
        	}
        } finally {
        	g.dispose();
        }
	}
}

