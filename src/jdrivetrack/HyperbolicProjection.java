package jdrivetrack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

public class HyperbolicProjection extends JPanel {
	private static final long serialVersionUID = -3143479088440322492L;

	private boolean showAsymptote = false;
	private boolean showArc = false;
	private boolean showTrace = false;
	private boolean showCursor = false;
	private Color asymptoteColor;
	private Color arcColor;
	private Color traceColor = Color.GREEN;
	private Color cursorColor;
	private double cursorDiameter;
	private double traceDiameter;
	private Point.Double upperLeftLonLat;
	private Point.Double lowerRightLonLat;
	private ConicSection cone;
	private Asymptote2D asymptote;

	public HyperbolicProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, 
			ConicSection cone, Dimension mapSize, Color arcColor, boolean showArc, 
			Color asymptoteColor, boolean showAsymptote, double cursorDiameter, Color cursorColor, 
			boolean showCursor, double traceDiameter, boolean showTrace) {
		
		this.arcColor = arcColor;
		this.showArc = showArc;
		this.showTrace = showTrace;
		this.showCursor = showCursor;
		this.showAsymptote = showAsymptote;
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		this.asymptoteColor = asymptoteColor;
		this.traceDiameter = traceDiameter;
		this.cursorColor = cursorColor;
		this.cursorDiameter = cursorDiameter;
		this.cone = cone;
		
    	setSize(mapSize);
    	setLayout(null);
		setOpaque(false);
		setBackground(new Color(0,0,0,0));
		setDoubleBuffered(true);
		setVisible(true);
	}

	private double longitudeToX(double longitude) {
		double leftToRightDegrees = Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
		return getSize().width - ((lowerRightLonLat.x - longitude) * (getSize().width / leftToRightDegrees));
	}

	private double latitudeToY(double latitude) {
		double topToBottomDegrees = Math.abs(upperLeftLonLat.y - lowerRightLonLat.y);
		return getSize().height + ((lowerRightLonLat.y - latitude) * (getSize().height / topToBottomDegrees));
	}

	public ConicSection getConicSection() {
		return cone;
	}

	private Point2D.Double lonlatToXY(Point.Double lonlat) {
		return new Point2D.Double(longitudeToX(lonlat.x), latitudeToY(lonlat.y));
	}

	public void showCursor(boolean showCursor) {
		this.showCursor = showCursor;
		repaint();
	}
	
	public void showTrace(boolean showTrail) {
		this.showTrace = showTrail;
		repaint();
	}
	
	public void showAsymptote(boolean showAsymptote) {
		this.showAsymptote = showAsymptote;
		repaint();
	}
	
	public void setAsymptoteColor(Color asymptoteColor) {
		this.asymptoteColor = asymptoteColor;
		repaint();
	}
	
	public void setCursorDiameter(int cursorDiameter) {
		this.cursorDiameter = cursorDiameter;
		repaint();
	}
	
	public void setAsymptoteLength(double asymptoteLength) {
		asymptote.setLength(asymptoteLength);
		repaint();
	}
	
	public void showArc(boolean showArc) {
		this.showArc = showArc;
		repaint();
	}

	public void setArcColor(Color arcColor) {
		this.arcColor = arcColor;
		repaint();
	}

	public void setCursorColor(Color cursorColor) {
		this.cursorColor = cursorColor;
		repaint();
	}
	
	public void setTraceColor(Color traceColor) {
		this.traceColor = traceColor;
		repaint();
	}

    public void setCornerLonLat(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat) {
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		repaint();
	}

    @Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (upperLeftLonLat != null && lowerRightLonLat != null) {
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
	        	
	        	Point.Double[] coord = cone.getHyperbolicPointArray();
	        	MapDimension mapDim = new MapDimension(upperLeftLonLat.y, lowerRightLonLat.x, lowerRightLonLat.y, upperLeftLonLat.x);
	        	Point.Double[] screenPointArray = CoordinateUtils.coordinateArrayToScreenPointArray(coord, mapDim, getSize());
	        	ShapeBuilder hyperbola = new ShapeBuilder(screenPointArray);
	        	
	    		Point.Double[] asymptoteScreenPoints = CoordinateUtils.coordinateArrayToScreenPointArray(cone.getAsymptoteCoordArray(), mapDim, getSize());
	    		
	        	asymptote = new Asymptote2D(asymptoteScreenPoints);
	        	
	        	if (showAsymptote) {
					g.setStroke(new BasicStroke(1.0f));
					g.setColor(asymptoteColor);
					g.draw(asymptote);
				}
				if (showArc) {
					g.setColor(arcColor);
					g.setStroke(new BasicStroke(1.0f));
			    	g.draw(hyperbola);
				}
				if (showTrace) {
					float dash[] = {5.0f,5.0f};
					g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f, dash, 0f));
					g.setColor(traceColor);
					g.draw(new Line2D.Double(lonlatToXY(cone.getSMB().point), lonlatToXY(cone.getSMA().point)));
					g.setStroke(new BasicStroke(1.0f));
					g.draw(new Ellipse2D.Double(longitudeToX(cone.getSMB().point.x) - (traceDiameter / 2), 
						latitudeToY(cone.getSMB().point.y) - (traceDiameter / 2), traceDiameter, traceDiameter));
					g.draw(new Ellipse2D.Double(longitudeToX(cone.getSMA().point.x) - (traceDiameter / 2), 
						latitudeToY(cone.getSMA().point.y) - (traceDiameter / 2), traceDiameter, traceDiameter));
				}
				if (showCursor) {
					g.setStroke(new BasicStroke(1.0f));
					g.setColor(cursorColor);
					g.draw(new Ellipse2D.Double(longitudeToX(cone.getCenter().x) - (cursorDiameter / 2), 
						latitudeToY(cone.getCenter().y) - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(cone.getVertex().x - (cursorDiameter / 2), cone.getVertex().y - 
						(cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(cone.getFocus().x - (cursorDiameter / 2), cone.getFocus().y - 
						(cursorDiameter / 2), cursorDiameter, cursorDiameter));
				    Point2D.Double source = new Point2D.Double(-83.07724, 40.026563);
					g.draw(new CrissCross2D(lonlatToXY(source), 12));
				}
        	}
        } finally {
        	g.dispose();
        }
	}

}
