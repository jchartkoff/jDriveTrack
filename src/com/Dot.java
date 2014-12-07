package com;

import java.awt.AlphaComposite;
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
	
	private Point.Double upperLeftPoint = null;
	private Point.Double lowerRightPoint = null;
	private Point.Double point = null;
	private double diameter;
	private Color color = Color.GREEN;

	public Dot() {
		this(null, null, null, 10, new Dimension(800,600), Color.GREEN);
	}
	
	public Dot(Dimension mapSize) {
		this(null, null, null, 10, mapSize, Color.GREEN);
	}
	
	public Dot(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double point, Dimension mapSize) {
		this(upperLeftPoint, lowerRightPoint, null, 10, mapSize, Color.GREEN);
	}
	
	public Dot(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double point, double diameter, Dimension mapSize,  
			Color color) {
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

	private AlphaComposite makeComposite(float alpha) {
	    int type = AlphaComposite.SRC_OVER;
	    return(AlphaComposite.getInstance(type, alpha));
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

	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (point != null && upperLeftPoint != null && lowerRightPoint != null && diameter != 0) {
	        	Ellipse2D.Double dot = new Ellipse2D.Double(longitudeToX(point.x) - (diameter / 2.0),
						latitudeToY(point.y) - (diameter / 2.0), diameter, diameter);	
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(color);
				g.draw(dot);
				Composite originalComposite = g.getComposite();
				g.setComposite(makeComposite(0.3F));
				g.fill(dot);
				g.setComposite(originalComposite);
        	}
        } finally {
        	g.dispose();
        }
	}
}

