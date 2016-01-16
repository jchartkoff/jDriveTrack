package jdrivetrack;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class Arrow extends JPanel {
	private static final long serialVersionUID = 7321724047688618755L;
	
	private Point.Double upperLeftPoint = new Point.Double();
	private Point.Double lowerRightPoint = new Point.Double();
	private double arrowSize;
	private int angle;
	private Color color;
	private Point.Double point = new Point.Double();

	public Arrow(Point.Double upperLeftPoint, Point.Double lowerRightPoint, double arrowSize, 
			Dimension mapSize) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		this.arrowSize = arrowSize;
		setSize(mapSize);
		setLayout(null);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(false);
	}

	public void setLocation(Point.Double point, int angle) {
		this.point = point;
		this.angle = angle;
		repaint();
	}

	public void setArrowSize(double size) {
		this.arrowSize = size;
		repaint();
	}

	public void setColor(Color color) {
		this.color = color;
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
        	if (point != null && upperLeftPoint != null && lowerRightPoint != null) {
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        	Line2D.Double arrow = new Line2D.Double(
	        			longitudeToX(point.x) - Math.sin(angle * Math.PI / 180.0) * arrowSize, 
	        			latitudeToY(point.y) + Math.cos(-angle * Math.PI / 180.0) * arrowSize,
						longitudeToX(point.x), 
						latitudeToY(point.y));
	        	Line2D.Double rtip = new Line2D.Double(
	        			longitudeToX(point.x) - Math.sin((angle + 40) * Math.PI / 180.0) * arrowSize, 
	        			latitudeToY(point.y) + Math.cos((angle + 40) * Math.PI / 180.0) * arrowSize,
						longitudeToX(point.x), 
						latitudeToY(point.y));
	        	Line2D.Double ltip = new Line2D.Double(
	        			longitudeToX(point.x) - Math.sin((angle - 40) * Math.PI / 180.0) * arrowSize, 
	        			latitudeToY(point.y) + Math.cos((angle - 40) * Math.PI / 180.0) * arrowSize,
						longitudeToX(point.x), 
						latitudeToY(point.y));
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(color);
				Composite originalComposite = g.getComposite();
				g.setComposite(makeComposite(0.3F));
				g.setStroke(new BasicStroke(2));
				g.setComposite(originalComposite);
				g.draw(arrow);
				g.draw(rtip);
				g.draw(ltip);
        	}
        } finally {
        	g.dispose();
        }
	}
}

