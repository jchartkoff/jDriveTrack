package jdrivetrack;

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
	
	private static final int MAX_TILES_ACROSS_SCREEN = 80;
	
	private Point.Double upperLeftLonLat;
	private Point.Double lowerRightLonLat;
	private Point.Double gridSize;
	private Point.Double referencePoint;
	private Color color;
	
	public Grid(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, Dimension mapSize, Point.Double referencePoint) {
		this(upperLeftLonLat, lowerRightLonLat, null, mapSize, referencePoint, Color.RED);
	}
	
	public Grid(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, Point.Double gridSize, 
			Dimension mapSize, Point.Double referencePoint, Color color) {
		this.gridSize = gridSize;
		this.color = color;
		this.referencePoint = referencePoint;
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		setSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setLayout(null);
		setVisible(false);
	}
	
	public void setReferencePoint(Point.Double referencePoint) {
		this.referencePoint = referencePoint;
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

	public void setCornerLonLat(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat) {
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		repaint();
	}
	
	private int tilesAcrossScreen() {
		double w = Math.abs(lowerRightLonLat.x - upperLeftLonLat.x);
		return (int) (w / (gridSize.x / 3600d));
	}
	
	private AlphaComposite makeComposite(float alpha) {
	    int type = AlphaComposite.SRC_OVER;
	    return(AlphaComposite.getInstance(type, alpha));
	}
	
	private double longitudeToX(double longitude) throws IllegalArgumentException {
		double leftToRightDegrees = Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
		return getSize().width - ((lowerRightLonLat.x - longitude) * 
				(getSize().width / leftToRightDegrees));
	}

	private double latitudeToY(double latitude) throws IllegalArgumentException {
		double topToBottomDegrees = Math.abs(upperLeftLonLat.y - lowerRightLonLat.y);
		return getSize().height + ((lowerRightLonLat.y - latitude) * 
				(getSize().height / topToBottomDegrees));
	}

	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (upperLeftLonLat != null && lowerRightLonLat != null && gridSize != null && 
        			tilesAcrossScreen() <= MAX_TILES_ACROSS_SCREEN) {
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        	
	        	double verticalPosition = upperLeftLonLat.y / (gridSize.y / 3600.0); 
				double horizontalPosition = upperLeftLonLat.x / (gridSize.x / 3600.0);
				
				double verticalEdge = (int) horizontalPosition * (gridSize.x / 3600.0);
				double horizontalEdge = (int) verticalPosition * (gridSize.y / 3600.0);
		        
				double xReference = longitudeToX(verticalEdge);
		        double yReference = latitudeToY(horizontalEdge);
				
		        Composite originalComposite = g.getComposite();
				
		        g.setComposite(makeComposite(0.3F));
				
				double pixelsPerDegreeLon = getSize().width / Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
				double pixelsPerTileLon = Math.max(pixelsPerDegreeLon * (gridSize.x / 3600.0), 5.0);
	
				for (double i = xReference; i <= getSize().width; i = i + pixelsPerTileLon) {
					Line2D.Double line = new Line2D.Double(i, 0, i, getSize().height);
					g.setColor(color);
					g.draw(line);
				}
				
				double pixelsPerDegreeLat = getSize().height / Math.abs(upperLeftLonLat.y - lowerRightLonLat.y);
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
