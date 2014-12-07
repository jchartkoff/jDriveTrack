package com;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

public class Quad extends JPanel {
	private static final long serialVersionUID = 1L;
	private Point.Double upperLeftPoint = new Point.Double();
	private Point.Double lowerRightPoint = new Point.Double();
	private Point.Double point = new Point.Double();
	private Point.Double size = new Point.Double();
	private Color color = Color.YELLOW;

	public Quad(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double point, Point.Double size, Dimension mapSize, Color color) {
		this.point = point;
		this.color = color;
		this.size = size;
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		setSize(mapSize);
		setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(false);
	}

	public void setQuadColor(Color color) {
		this.color = color;
		repaint();
	}

	public void setCornerLonLat(Point.Double upperLeftPoint, Point.Double lowerRightPoint) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
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

    @Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {	
        	if (size != null && point != null && upperLeftPoint != null && lowerRightPoint != null) {
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        	
	        	double vp = upperLeftPoint.y / (size.y / 3600.0); 
				double hp = upperLeftPoint.x / (size.x / 3600.0);
				
				double ve = (int) hp * (size.x / 3600.0);
				double he = (int) vp * (size.y / 3600.0);
				
				double xReference = longitudeToX(ve);
		        double yReference = latitudeToY(he);
	
	        	double pixelsPerDegreeLon = getSize().width / Math.abs(upperLeftPoint.x - lowerRightPoint.x);
				double pixelsPerTileLon = Math.max(pixelsPerDegreeLon * (size.x / 3600.0), 5.0);
	
				double pixelsPerDegreeLat = getSize().height / Math.abs(upperLeftPoint.y - lowerRightPoint.y);
				double pixelsPerTileLat = Math.max(pixelsPerDegreeLat * (size.y / 3600.0), 5.0);
	
				double ulx = Math.round(longitudeToX(this.point.x) - (pixelsPerTileLon / 2.0));
				double uly = Math.round(latitudeToY(this.point.y) - (pixelsPerTileLat / 2.0));
				
				double modx = ulx % pixelsPerTileLon;
				double subx = ulx - modx;
				double px = subx + xReference;
				
				double mody = uly % pixelsPerTileLat;
				double suby = uly - mody;
				double py = suby + yReference;
	
	        	Rectangle2D.Double quad = new Rectangle2D.Double(px, py, pixelsPerTileLon, pixelsPerTileLat);
	        	
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(color);
				g.draw(quad);
				Composite originalComposite = g.getComposite();
				g.setComposite(makeComposite(0.3F));
				g.fill(quad);
				g.setComposite(originalComposite);
        	}
        } finally {
        	g.dispose();
        }
	}
}
