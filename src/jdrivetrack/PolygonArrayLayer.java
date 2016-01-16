package jdrivetrack;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class PolygonArrayLayer extends JPanel {
	private static final long serialVersionUID = -1069738920042689330L;
	
	private Point.Double upperLeftPoint = new Point.Double();
	private Point.Double lowerRightPoint = new Point.Double();
	private Point.Double tileSize = new Point.Double();
	
	private List<Point.Double> pointList = new ArrayList<Point.Double>(1024);
	private List<Color> colorList = new ArrayList<Color>(1024);
	private List<Boolean> visibleList = new ArrayList<Boolean>(1024);

	public PolygonArrayLayer(Point.Double upperLeftPoint, Point.Double lowerRightPoint, 
			Dimension mapSize) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		setSize(mapSize);
		setLayout(null);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(false);
	}

	public void addPolygon(Point.Double point) {	
		addPolygon(point, Color.YELLOW);
	}
	
	public void addPolygon(Point.Double point, Color color) {	
		pointList.add(point);
		colorList.add(color);
		visibleList.add(true);
		repaint();
	}

	public void setTileSize(Point.Double tileSize) {
		pointList.subList(0, pointList.size()).clear();
		this.tileSize = tileSize;
		repaint();
	}
	
	public void setColor(int index, Color color) {
		colorList.set(index, color);
		repaint();
	}
	
	public void setPolygonVisible(int index, boolean isVisible) {
		visibleList.set(index, isVisible);
		repaint();
	}
	
	public void deletePolygon(int index) {
		pointList.remove(index);
		colorList.remove(index);
		visibleList.remove(index);
	}
	
	public void deleteAllPolygons() {
		pointList.subList(0, pointList.size()).clear();
		colorList.subList(0, colorList.size()).clear();
		visibleList.subList(0, visibleList.size()).clear();
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
        if (upperLeftPoint == null || lowerRightPoint == null || tileSize == null || pointList.size() == 0) return;
        try {
        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
        	
        	double vp = upperLeftPoint.y / (tileSize.y / 3600.0); 
			double hp = upperLeftPoint.x / (tileSize.x / 3600.0);
			
			double ve = (int) hp * (tileSize.x / 3600.0);
			double he = (int) vp * (tileSize.y / 3600.0);
			
			double xReference = longitudeToX(ve);
	        double yReference = latitudeToY(he);

        	double pixelsPerDegreeLon = getSize().width / Math.abs(upperLeftPoint.x - lowerRightPoint.x);
			double pixelsPerTileLon = Math.max(pixelsPerDegreeLon * (tileSize.x / 3600.0), 5.0);

			double pixelsPerDegreeLat = getSize().height / Math.abs(upperLeftPoint.y - lowerRightPoint.y);
			double pixelsPerTileLat = Math.max(pixelsPerDegreeLat * (tileSize.y / 3600.0), 5.0);

			for (int i = 0; i < pointList.size(); i++) {
				if (visibleList.get(i)) {
					double ulx = Math.round(longitudeToX(pointList.get(i).x));
					double uly = Math.round(latitudeToY(pointList.get(i).y));
					
					double modx = ulx % pixelsPerTileLon;
					double subx = ulx - modx;
					double px = subx + xReference - pixelsPerTileLon;
					
					double mody = uly % pixelsPerTileLat;
					double suby = uly - mody;
					double py = suby + yReference - pixelsPerTileLat;

		        	Rectangle2D.Double polygon = new Rectangle2D.Double(px, py, pixelsPerTileLon, pixelsPerTileLat);

					g.setColor(colorList.get(i));
					g.draw(polygon);
					Composite originalComposite = g.getComposite();
					g.setComposite(makeComposite(0.3F));
					g.fill(polygon);
					g.setComposite(originalComposite);
				}
        	}
        } finally {
        	g.dispose();
        }
	}
}