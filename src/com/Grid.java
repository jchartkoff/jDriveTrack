package com;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class Grid extends JPanel {
	private static final long serialVersionUID = -5683391400792635138L;
	
	private Point.Double upperLeftPoint = null;
	private Point.Double lowerRightPoint = null;
	private Point.Double gridSize = null;
	private Color color = Color.RED;
	
	public Grid(Dimension mapSize) {
		setSize(mapSize);
		setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setLayout(null);
		setVisible(false);
	}
	
	public Grid(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double gridSize, Dimension mapSize, Color color) {
		this.gridSize = gridSize;
		this.color = color;
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		setSize(mapSize);
		setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setLayout(null);
		setVisible(false);
	}

	public void setGridSize(Point.Double gridSize) {
		this.gridSize = gridSize;
		repaint();
	}
	
	public Point.Double getGridSize() {
		return gridSize;
	}

	public void setColor(Color color) {
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
	
	private double longitudeToX(double longitude) throws IllegalArgumentException {
		double leftToRightDegrees = Math.abs(upperLeftPoint.x - lowerRightPoint.x);
		return getSize().width - ((lowerRightPoint.x - longitude) * 
				(getSize().width / leftToRightDegrees));
	}

	private double latitudeToY(double latitude) throws IllegalArgumentException {
		double topToBottomDegrees = Math.abs(upperLeftPoint.y - lowerRightPoint.y);
		return getSize().height + ((lowerRightPoint.y - latitude) * 
				(getSize().height / topToBottomDegrees));
	}

	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (upperLeftPoint != null && lowerRightPoint != null && gridSize != null) {
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        	
	        	double verticalPosition = upperLeftPoint.y / (gridSize.y / 3600.0); 
				double horizontalPosition = upperLeftPoint.x / (gridSize.x / 3600.0);
				
				double verticalEdge = (int) horizontalPosition * (gridSize.x / 3600.0);
				double horizontalEdge = (int) verticalPosition * (gridSize.y / 3600.0);
		        
				double xReference = longitudeToX(verticalEdge);
		        double yReference = latitudeToY(horizontalEdge);
				
		        Composite originalComposite = g.getComposite();
				
		        g.setComposite(makeComposite(0.3F));
				
				double pixelsPerDegreeLon = getSize().width / Math.abs(upperLeftPoint.x - lowerRightPoint.x);
				double pixelsPerTileLon = Math.max(pixelsPerDegreeLon * (gridSize.x / 3600.0), 5.0);
	
				for (double i = xReference; i <= getSize().width; i = i + pixelsPerTileLon) {
					Line2D.Double line = new Line2D.Double(i, 0, i, getSize().height);
					g.setColor(color);
					g.draw(line);
				}
				
				double pixelsPerDegreeLat = getSize().height / Math.abs(upperLeftPoint.y - lowerRightPoint.y);
				double pixelsPerTileLat = Math.max(pixelsPerDegreeLat * gridSize.y / 3600.0, 5.0);
				
				for (double i = yReference; i <= getSize().height; i = i + pixelsPerTileLat) {
					Line2D.Double line = new Line2D.Double(0, i, getSize().width, i);
					g.setColor(color);
					g.draw(line);
				}
				
				g.setComposite(originalComposite);
        	}
		} finally {
			g.dispose();
		}
	}
}
