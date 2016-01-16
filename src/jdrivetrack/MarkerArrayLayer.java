package jdrivetrack;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class MarkerArrayLayer extends JPanel {
	private static final long serialVersionUID = -3730064920971859688L;
	
	private static final double DEFAULT_DIAMETER = 10;
	private static final Color DEFAULT_COLOR = Color.RED;
	
	private Point.Double upperLeftPoint = null;
	private Point.Double lowerRightPoint = null;
	private List<Point2D.Double> markerList = new ArrayList<Point2D.Double>(1024);
	private List<Color> colorList = new ArrayList<Color>(1024);
	private List<Double> diameterList = new ArrayList<Double>(1024);
	private double diameter;
	private Color color;

	public MarkerArrayLayer(Point.Double upperLeftPoint, Point.Double lowerRightPoint, 
			Dimension mapSize) {
		this(upperLeftPoint, lowerRightPoint, mapSize, DEFAULT_DIAMETER, DEFAULT_COLOR);
	}
	
	public MarkerArrayLayer(Point.Double upperLeftPoint, Point.Double lowerRightPoint, 
			Dimension mapSize, double diameter, Color color) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		this.diameter = diameter;
		this.color = color;
		setSize(mapSize);
		setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(false);
	}
	
	public void addMarker(Point.Double point, Color color) {	
		diameterList.add(DEFAULT_DIAMETER);
		markerList.add(point);
		colorList.add(color);
		repaint();
	}
	
	public void addMarker(Point.Double point, double diameter, Color color) {	
		diameterList.add(diameter);
		markerList.add(point);
		colorList.add(color);
		repaint();
	}
	
	public void addMarkers(List<Point.Double> point, ArrayList<Color> color) {
		for (int i = 0; i < point.size(); i++) {
			diameterList.add(DEFAULT_DIAMETER);
		}
		markerList.addAll(point);
		colorList.addAll(color);
		repaint();
	}
	
	public void addMarkers(List<Point.Double> point, double diameter, ArrayList<Color> color) {
		for (int i = 0; i < point.size(); i++) {
			diameterList.add(diameter);
		}
		markerList.addAll(point);
		colorList.addAll(color);
		repaint();
	}
	
	public void addMarkers(List<Point.Double> point, double diameter, Color color) {
		for (int i = 0; i < point.size(); i++) {
			diameterList.add(diameter);
			colorList.add(color);
		}
		markerList.addAll(point);
		repaint();
	}
	
	public void addMarkers(List<Point.Double> point, ArrayList<Double> diameter, ArrayList<Color> color) {	
		diameterList.addAll(diameter);
		markerList.addAll(point);
		colorList.addAll(color);
		repaint();
	}
	
	public void setDiameter(int index, double diameter) {
		diameterList.set(index, diameter);
		repaint();
	}
	
	public void setColor(int index, Color color) {
		colorList.set(index, color);
		repaint();
	}
	
	public void setAllDiameters(double diameter) {
		this.diameter = diameter;
		diameterList.subList(0, diameterList.size()).clear();
		for (int i = 0; i < markerList.size(); i++) {
			diameterList.add(this.diameter);
		}
		repaint();
	}
	
	public void setAllColors(Color color) {
		this.color = color;
		colorList.subList(0, colorList.size()).clear();
		for (int i = 0; i < markerList.size(); i++) {
			colorList.add(this.color);
		}
		repaint();
	}

	public void deleteMarker(int index) {
		markerList.remove(index);
		colorList.remove(index);
		diameterList.remove(index);
		repaint();
	}
	
	public void deleteAllMarkers() {
		markerList.subList(0, markerList.size()).clear();
		colorList.subList(0, colorList.size()).clear();
		diameterList.subList(0, diameterList.size()).clear();
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
        Graphics2D g = (Graphics2D) gOrig;
        if (upperLeftPoint == null || lowerRightPoint == null || markerList.size() == 0) return;
        try {
        	for (int i = 0; i < markerList.size(); i++) {
	        	Ellipse2D.Double marker = new Ellipse2D.Double(longitudeToX(markerList.get(i).x) - 
	        		(diameterList.get(i) / 2.0), latitudeToY(markerList.get(i).y) - (diameterList.get(i) / 2.0), 
	        		diameterList.get(i), diameterList.get(i));	
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(colorList.get(i));
				g.draw(marker);
				Composite originalComposite = g.getComposite();
				g.setComposite(makeComposite(0.3F));
				g.fill(marker);
				g.setComposite(originalComposite);
        	}
        } finally {
        	g.dispose();
        }
	}
}

