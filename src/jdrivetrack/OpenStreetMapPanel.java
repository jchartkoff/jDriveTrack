package jdrivetrack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLayeredPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import org.openstreetmap.fma.jtiledownloader.views.main.JTileDownloaderMainView;

import events.JMVCommandEvent.Command;
import events.JMVCommandEvent;

import interfaces.MapPolygon;
import interfaces.MapPolyline;
import interfaces.MapRectangle;
import interfaces.ICoordinate;
import interfaces.JMapViewerEventListener;
import interfaces.MapInterface;
import interfaces.MapMarker;
import interfaces.TileLoader;
import interfaces.TileLoaderListener;
import interfaces.TileSource;
import interfaces.MapMarker.STYLE;

import tilesources.OsmTileSource;
import types.Coordinate;
import types.GeoTile;
import types.MapDimension;
import types.StaticMeasurement;
import types.Style;
import types.TestTile;
import types.Tile;

public class OpenStreetMapPanel extends JLayeredPane implements MapInterface, Cloneable, TileLoaderListener {
	private static final long serialVersionUID = -1154235901605771509L;
	
	private static final int MAX_TILES_ACROSS_SCREEN = 80;
	private static final Cursor DEFAULT_MAP_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	private static final Point.Double DEFAULT_MAP_CENTER_LON_LAT = new Point.Double(-86,35);
    private static final Dimension PREFERRED_SIZE = new Dimension(800,600);
    private static final int DEFAULT_ZOOM = 6;
    private static final Dimension DEFAULT_PRINTER_PAGE_SIZE = new Dimension(1035,800);
    private static final String DEFAULT_TILE_CACHE_PATH = System.getProperty("user.home") + 
		File.separator + "drivetrack" + File.separator + "cache";
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};
    
    public static boolean debug = false;
    
    private boolean displayShapes = true;
    private Color[] arcColors = null;
    private double arcTraceRadius;
	private Color arcCursorColor;
	private Color arcTraceColor;
	private Color arcAsymptoteColor;
	private int zoom = 6;
    private Point.Double mouseLonLat = new Point.Double(0.0,0.0);
	private Point.Double upperLeftPoint = null;
	private Point.Double lowerRightPoint = null;
	private boolean showTestTiles = false;
	private boolean showQuads = false;
	private boolean showSignalMarkers = false;
	private boolean showLines = false;
	private boolean showRings = false;
	private boolean showIcons = false;
	private boolean showGrid = false;
	private boolean showArcs = false;
	private boolean showArcTraces = false;
	private boolean traceEqualsFlightColor = false;
	private boolean showArcIntersectPoints = false;
	private boolean showArcAsymptotes = false;
	private boolean showArcCursors = false;
	private boolean showIconLabels = false;
	private boolean showGpsSymbol = false;
	private boolean showTargetRing = false;
	private boolean showMapImage = true;
	private boolean awaitingArcColorSet = false;
	private double arcCursorRadius;
	private double arcIntersectPointRadius;
	private Color arcIntersectPointColor;
	private MapPolylineImpl gpsArrow;
	private MapMarkerCircle targetRing;
	private MapMarkerDot gpsDot;
	
	private List<HyperbolicProjection> arcList;
	private List<MapMarkerCircle> arcIntersectList;
	private List<MapPolylineImpl> lineList;	
	private List<MapMarkerCircle> signalMarkerList;
	private List<MapMarkerCircle> ringList;
	private List<MapRectangleImpl> quadList;
	private List<MapPolygonImpl> testTileList;
	private List<MapRectangleImpl> gridLines;
	private List<Icon> iconList = new ArrayList<Icon>(128);

	private Point mousePosition = new Point(0,0);
    private ProgressMonitor progressMonitor;
    private Dimension frameSize = PREFERRED_SIZE;
    private Dimension fsInsets;
    private boolean tileGridVisible = true;
    private Point.Double centerLonLat;
    private TileSource tileSource;
    private Point center = new Point(0,0);
    private boolean scrollWrapEnabled = false;
    private TileController tileController;
    private TileLoader tileLoader;
    private TileCache tileCache;
    private AttributionSupport attribution = new AttributionSupport();
    private EventListenerList evtListenerList = new EventListenerList();
    private Point.Double gridReference = null;
    private Point.Double tileSize = null;
    private Point.Double gridSize = null;
    private Color gridColor = Color.RED;
    
    private int ringIndex = -1;
    private int quadIndex = -1;
    private int lineIndex = -1;
    private int arcIndex = -1;
    private int signalMarkerIndex = -1;
    private int iconIndex = -1;
    
    public OpenStreetMapPanel() {
        this(DEFAULT_MAP_CENTER_LON_LAT, DEFAULT_ZOOM, PREFERRED_SIZE, DEFAULT_TILE_CACHE_PATH);
    }

    public OpenStreetMapPanel(Dimension frameSize) {
    	this(DEFAULT_MAP_CENTER_LON_LAT, DEFAULT_ZOOM, frameSize, DEFAULT_TILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point.Double centerLonLat, int zoom) {
    	this(centerLonLat, zoom, PREFERRED_SIZE, DEFAULT_TILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point.Double centerLonLat, int zoom, Dimension frameSize) {
    	this(centerLonLat, zoom, frameSize, DEFAULT_TILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point.Double centerLonLat, int zoom, Dimension frameSize, String tileCachePath) {
    	this.zoom = zoom;
        this.frameSize = frameSize;
        this.centerLonLat = centerLonLat;
        
        setIgnoreRepaint(true);
        setSize(frameSize);
        setLayout(null); 
        setDoubleBuffered(true);
        setOpaque(true);
        setBackground(Color.BLACK);
        setFocusable(true);
		setFocusTraversalKeysEnabled(true);
		setVisible(false);
		setCursor(DEFAULT_MAP_CURSOR);
		fsInsets = new Dimension(this.frameSize.width - 2, this.frameSize.height - 2);
		tileSource = new OsmTileSource.Mapnik();
		tileCache = new TileCache(new File(tileCachePath));
		tileController = new TileController(tileSource, tileCache, this);
		tileLoader = new OsmTileLoader(this);
		tileController.setTileLoader(tileLoader);
        attribution.initialize(tileSource);
        addComponentLayers();
        new DefaultMapController(this);
    }
    
    @Override
    public void restoreCache() {
    	
        tileCache.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if (TileCache.Progress.RESTORED.toString().equals(event.getPropertyName())) {
	            	tileCacheReady(event);
	            }
	            if (TileCache.Progress.UPDATE.toString().equals(event.getPropertyName())) {
	            	tileCacheProgress(event);
	            }
        	}
        });

        progressMonitor = new ProgressMonitor(SwingUtilities.getWindowAncestor(this), 
        		"Retreiving Cached Map Tiles From Disk", String.format("Completed %d%% of restore", 0),0, 100);
        
        progressMonitor.setMillisToPopup(0);

        tileCache.restoreDiskCache();
    }
    
    @Override
    public void clearCache() {
    	tileCache.clear();
    }
    
    private void tileCacheReady(PropertyChangeEvent event) {
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressMonitor.setProgress(100);
		    	progressMonitor.close();
		    	setDisplayPosition(new Coordinate(centerLonLat), zoom);
		    	setVisible(true);
		    	firePropertyChange(MAP_READY, null, true);
		        redimensionMap();
            }
        });
    	
    }

    private void tileCacheProgress(PropertyChangeEvent event) {
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
		    	if (progressMonitor.isCanceled()) {
		    		tileCache.cancel();
					progressMonitor.close();
		    	} else {
		            int progress = (Integer) event.getNewValue();
		            progressMonitor.setProgress(progress);
		            progressMonitor.setNote(String.format("Completed %d%% of restore", progress));
		    	}
            }
    	});
    }

    private void addComponentLayers() {
		Style gpsDotStyle = new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, 64), new BasicStroke(), null);
		gpsDot = new MapMarkerDot(new Coordinate(centerLonLat), 2, gpsDotStyle);
		
		Style gpsArrowStyle = new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, 64), new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER), null);
		gpsArrow = new MapPolylineImpl(null, null, null, gpsArrowStyle);

		Style targetRingStyle = new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, 16), new BasicStroke(), null);
        targetRing = new MapMarkerCircle(null, null, new Coordinate(centerLonLat), 100, STYLE.FIXED, targetRingStyle);
        
        arcList = Collections.synchronizedList(new LinkedList<HyperbolicProjection>());
        arcIntersectList = Collections.synchronizedList(new LinkedList<MapMarkerCircle>());
        signalMarkerList = Collections.synchronizedList(new LinkedList<MapMarkerCircle>());
		ringList = Collections.synchronizedList(new LinkedList<MapMarkerCircle>());
		quadList = Collections.synchronizedList(new LinkedList<MapRectangleImpl>());
		testTileList = Collections.synchronizedList(new LinkedList<MapPolygonImpl>());
		gridLines = Collections.synchronizedList(new LinkedList<MapRectangleImpl>());
		lineList = Collections.synchronizedList(new LinkedList<MapPolylineImpl>());
    }
    
    @Override
    public void setGridColor(Color color) {
    	this.gridColor = color;
    	if (gridLines == null) return;
    	Style style = new Style(color, color, new BasicStroke(), new Font("Calibri", Font.BOLD, 12));
		for (MapRectangleImpl gridLine : gridLines) {
			gridLine.setStyle(style);
		}
    	repaint();
    }

    private void setDisplayPosition(ICoordinate to, int zoom) {
        setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), to, zoom);
    }

    private void setDisplayPosition(Point mapPoint, ICoordinate to, int zoom) {
        Point p = tileSource.latLonToXY(to, zoom);
        setDisplayPosition(mapPoint, p.x, p.y, zoom);
    }

    private void setDisplayPosition(Point mapPoint, int x, int y, int zoom) {
        if (zoom > tileController.getTileSource().getMaxZoom()) return;

        Point p = new Point();
        p.x = x - mapPoint.x + getWidth() / 2;
        p.y = y - mapPoint.y + getHeight() / 2;
        center = p;
        setIgnoreRepaint(true);
        try {
            int oldZoom = this.zoom;
            this.zoom = zoom;
            if (oldZoom != zoom) {
                zoomChanged(oldZoom);
            }
        } finally {
            setIgnoreRepaint(false);
            repaint();
        }
    }

    private ICoordinate getPosition() {
        return tileSource.xyToLatLon(center, zoom);
    }

    private ICoordinate getPosition(Point mapPoint) {
        return getPosition(mapPoint.x, mapPoint.y);
    }

    private ICoordinate getPosition(int mapPointX, int mapPointY) {
        int x = center.x + mapPointX - getWidth() / 2;
        int y = center.y + mapPointY - getHeight() / 2;
        return tileSource.xyToLatLon(x, y, zoom);
    }
    
    private void redimensionMap() {
    	setIgnoreRepaint(true);
    	ICoordinate tempUL = getPosition(0,0);     	
    	ICoordinate tempLR = getPosition(fsInsets.width, fsInsets.height);

    	if (tempUL == null || tempLR == null) return;

    	upperLeftPoint = new Point.Double(tempUL.getLon(), tempUL.getLat());
    	lowerRightPoint = new Point.Double(tempLR.getLon(), tempLR.getLat());

    	for (Icon tempIcon : iconList) {
    		tempIcon.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	}

    	setIgnoreRepaint(false);
    	repaint();
    }

    @Override
    public void displayShapes(boolean displayShapes) {
    	this.displayShapes = displayShapes;
    	
    	gpsDot.setVisible(displayShapes && showGpsSymbol);
    	gpsArrow.setVisible(displayShapes && showGpsSymbol);
    	targetRing.setVisible(displayShapes && showTargetRing);

    	showArcIntersectPoints(displayShapes && showArcIntersectPoints);
    	
    	for (Icon tempIcon : iconList) {
    		tempIcon.setVisible(displayShapes && showIcons);
    		if (displayShapes && showIcons) tempIcon.showIconLabel(displayShapes && showIconLabels);
    	}
    	
    	repaint();
    }

    @Override
    public int getMinZoom() {
    	return tileController.getTileSource().getMinZoom();
    }
    
    @Override
    public int getMaxZoom() {
    	return tileController.getTileSource().getMaxZoom();
    }

    @Override
    public int getZoom() {
        return zoom;
    }

    @Override
    public void zoomIn() {
        setZoom(zoom + 1);
    }

    @Override
    public void zoomIn(Point mapPoint) {
        setZoom(zoom + 1, mapPoint);
    }

    @Override
    public void zoomOut() {
        setZoom(zoom - 1);
    }

    @Override
    public void zoomOut(Point mapPoint) {
        setZoom(zoom - 1, mapPoint);
    }

    @Override
    public void setZoom(int zoom, Point mapPoint) {
        if (zoom > tileController.getTileSource().getMaxZoom() || zoom < tileController.getTileSource().getMinZoom()
                || zoom == this.zoom)
            return;
        ICoordinate zoomPos = getPosition(mapPoint);
        tileController.cancelOutstandingJobs();
        setDisplayPosition(mapPoint, zoomPos, zoom);
        redimensionMap();
        this.fireJMVEvent(new JMVCommandEvent(Command.ZOOM, this, zoom));
    }

    @Override
    public void setZoom(int zoom) {
        setZoom(zoom, new Point(getWidth() / 2, getHeight() / 2));
    }

    private void zoomChanged(int oldZoom) {
    	this.fireJMVEvent(new JMVCommandEvent(Command.ZOOM_OUT_DISABLED, this, zoom <= tileController.getTileSource().getMinZoom()));
    	this.fireJMVEvent(new JMVCommandEvent(Command.ZOOM_IN_DISABLED, this, zoom >= tileController.getTileSource().getMaxZoom()));
    }

    @Override
    public Point.Double getCenterLonLat() {
    	ICoordinate c = getPosition();
    	return new Point.Double(c.getLon(), c.getLat());
    }

    @Override
    public void setCenterLonLat(Point.Double point) {
    	setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), new Coordinate(point), zoom);
    	redimensionMap();
    }

    @Override
	public Point getMousePosition() {
		return mousePosition;
	}
    
    @Override
	public Point.Double getMouseCoordinates() {
		return mouseLonLat;
	}

	@Override
	public void setTileSize(Point.Double tileSize) {
		this.tileSize = tileSize;;
		if (showGrid & tileSize != null && gridReference != null && gridSize != null && gridColor != null) {
			gridLines.clear();
			gridLines.addAll(buildGrid(tileSize, gridReference, gridSize, gridColor));
			repaint();
		}
		repaint();
	}

	@Override
	public Point.Double getGridSize() {
		return gridSize;
	}

	@Override
	public void showMapImage(boolean showMapImage) {
		this.showMapImage = showMapImage;
		 repaint();
	}
	
	@Override
	public void showGrid(boolean showGrid) {
		this.showGrid = showGrid;
		if (showGrid & tileSize != null && gridReference != null && gridSize != null && gridColor != null) {
			gridLines.clear();
			gridLines.addAll(buildGrid(tileSize, gridReference, gridSize, gridColor));
			repaint();
		}
		repaint();
	}
	
	@Override
	public boolean isShowTargetRing() {
		return targetRing.isVisible();
	}

	@Override
	public boolean isShowGrid() {
		return showGrid;
	}
	
	@Override
	public boolean isShowMapImage() {
		return showMapImage;
	}
	
	@Override
	public boolean isShowGPSDot() {
		return gpsDot.isVisible();
	}
	
	@Override
	public boolean isShowGPSArrow() {
		return gpsArrow.isVisible();
	}
	
	@Override
	public double getMapLeftEdgeLongitude() {
		if (upperLeftPoint == null || lowerRightPoint == null) return 0;
		return upperLeftPoint.x;
	}
	
	@Override
	public double getMapRightEdgeLongitude() {
		if (upperLeftPoint == null || lowerRightPoint == null) return 0;
		return lowerRightPoint.x;
	}
	
	@Override
	public double getMapTopEdgeLatitude() {
		if (upperLeftPoint == null || lowerRightPoint == null) return 0;
		return upperLeftPoint.y;
	}
	
	@Override
	public double getMapBottomEdgeLatitude() {
		if (upperLeftPoint == null || lowerRightPoint == null) return 0;
		return lowerRightPoint.y;
	}

	@Override
	public void deleteAllArcIntersectPoints() {
		arcIntersectList.clear();
		repaint();
	}
	
	@Override
	public void deleteAllRings() {
		ringList.clear();
		ringIndex = -1;
        repaint();
	}
	
	@Override
	public void deleteAllSignalMarkers() {
		signalMarkerList.clear();
		signalMarkerIndex = -1;
        repaint();
	}
	
	@Override
	public void deleteAllIcons() {
		iconList.subList(0, iconList.size()).clear();
		iconIndex = -1;
		repaint();
	}
	
	@Override
	public void deleteAllLines() {
		lineList.subList(0, lineList.size()).clear();
		lineIndex = -1;
		repaint();
	}

	@Override
	public void deleteAllArcs() {
		arcList.clear();
		arcIndex = -1;
		repaint();
	}

	@Override
	public void showGpsSymbol(boolean showGpsSymbol) {
		if (this.showGpsSymbol != showGpsSymbol) {
			this.showGpsSymbol = showGpsSymbol;
			repaint();
		}
	}
	
	@Override
	public void setGpsSymbolColor(Color color) {
		gpsDot.setColor(color);
		gpsDot.setBackColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64));
		gpsArrow.setColor(color);
		gpsArrow.setBackColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64));
		repaint();
	}
	
	private List<ICoordinate> buildArrow(Point.Double point, double angle, double length) {
		List<ICoordinate> coords = new ArrayList<ICoordinate>(5);
		double meters = length * getMeterPerPixel();
		Point.Double arrowTail = Vincenty.getVincentyDirect(point, angle + 180, meters).point;
		Point.Double arrowLeft = Vincenty.getVincentyDirect(point, angle + 150, meters / 2).point;
		Point.Double arrowRight = Vincenty.getVincentyDirect(point, angle + 210, meters / 2).point;
		
		coords.add(new Coordinate(arrowTail));
		coords.add(new Coordinate(point));
		coords.add(new Coordinate(arrowLeft));
		coords.add(new Coordinate(point));
		coords.add(new Coordinate(arrowRight));
		coords.add(new Coordinate(point));
		
		return coords;
	}
	
	@Override
	public void setGpsSymbol(Point.Double point, double radius, Color color, int angle) {
		if (angle == 360) {
			gpsDot.setVisible(true);
			gpsDot.setLon(point.x);
			gpsDot.setLat(point.y);
			gpsDot.setRadius(radius / 20);
			gpsDot.setColor(color);
			gpsDot.setBackColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64));
			gpsArrow.setVisible(false);
		} else {
			gpsArrow.setVisible(true);
			gpsArrow.setPoints(buildArrow(point, angle, radius / 5));
			gpsArrow.setColor(color);
			gpsDot.setVisible(false);
		}
		repaint();
	}
	
	@Override
	public void showTargetRing(boolean showTargetRing) {
		this.showTargetRing = showTargetRing;
		if (targetRing.isVisible() != showTargetRing) {
			targetRing.setVisible(showTargetRing && displayShapes);
			repaint();
		}
	}

	@Override
	public void setGpsDotRadius(double radius) {
		gpsDot.setRadius(radius / 20);
		repaint();
	}
	
	@Override
	public void setGpsDotColor(Color color) {
		gpsDot.setColor(color);
		gpsDot.setBackColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64));
		repaint();
	}
	
	@Override
	public void setGpsArrowColor(Color color) {
		gpsArrow.setColor(color);
		repaint();
	}

	@Override
	public void setTargetRingPosition(Point.Double point) {
		targetRing.setLon(point.x);
		targetRing.setLat(point.y);
		repaint();
	}
	
	@Override
	public void setTargetRingRadius(double radius) {
		targetRing.setRadius(radius);
		repaint();
	}

	@Override
	public void setTargetRingColor(Color color) {
		targetRing.setColor(color);
		repaint();
	}

	@Override
	public void setTargetRing(Point.Double point, double radius) {
		targetRing.setLon(point.x);
		targetRing.setLat(point.y);
		targetRing.setRadius(radius);
		repaint();
	}
	
	@Override
	public void setTargetRing(Point.Double point, double radius, Color color) {
		targetRing.setLon(point.x);
		targetRing.setLat(point.y);
		targetRing.setRadius(radius);
		targetRing.setColor(color);
		repaint();
	}

	@Override
	public void addArcIntersectPoint(Point.Double point, double radius, Color color) {
		Style style = new Style(color, color, new BasicStroke(), null);
		MapMarkerCircle mmc = new MapMarkerCircle(null, null, new Coordinate(point), radius, STYLE.FIXED, style);
		arcIntersectList.add(mmc);
		repaint();
	}

	@Override
	public void addLine(Point.Double p1, double angle, double distance, Color color) {
		Point.Double p2 = Vincenty.getVincentyDirect(p1, angle, distance).point;
		addLine(p1, p2, color);
	}
	
	@Override
	public void addLine(Point.Double p1, Point.Double p2, Color color) {
		List<ICoordinate> coords = new ArrayList<ICoordinate>(2);
		coords.add(new Coordinate(p1));
		coords.add(new Coordinate(p2));
		Style style = new Style(color, color, new BasicStroke(), null);
		MapPolylineImpl line = new MapPolylineImpl(null, null, coords, style);
		lineList.add(line);
		lineIndex = lineList.size() - 1;
	}
	
	@Override
	public void addArc(ConicSection cone) {
		addArc(cone.getSMA(), cone.getSMB(), cone.getUnit());
	}
	
	@Override
	public void addArc(StaticMeasurement sma, StaticMeasurement smb, int unit) {
		Color traceColor;
		Color arcColor;
		if (arcColors != null && traceEqualsFlightColor) {
			traceColor = arcColors[unit];
			arcColor = arcColors[unit];
		} else {
			if (arcColors == null) awaitingArcColorSet = true;
			traceColor = arcTraceColor;
			arcColor = arcTraceColor;
		}
		HyperbolicProjection hyperbola = new HyperbolicProjection(sma, smb, unit, showArcs, arcColor, 
				showArcAsymptotes, arcAsymptoteColor, showArcCursors, arcCursorColor, arcCursorRadius,  
				showArcTraces, arcTraceColor, arcTraceRadius);
		arcList.add(hyperbola);
		arcIndex = arcList.size() - 1;
		hyperbola.setTraceColor(traceColor);
	}

	@Override
	public void setArcAsymptoteColor(Color arcAsymptoteColor) {
		this.arcAsymptoteColor = arcAsymptoteColor;
	}
    
	@Override
    public void setArcColors(Color[] arcColors) {
    	for (int i = 0; i < arcColors.length; i++) {
    		for (int a = 0; a < arcList.size(); a++) {
    			if (arcList.get(a).getUnit() == i) {
    				arcList.get(a).setArcColor(arcColors[i]);
    			}
    		}
    	}
    	this.arcColors = arcColors;
    	if (awaitingArcColorSet) {
    		setTraceEqualsFlightColor(true);
    		awaitingArcColorSet = false;
    	}
    }
    
	@Override
    public void setTraceEqualsFlightColor(boolean traceEqualsFlightColor) {
		this.traceEqualsFlightColor = traceEqualsFlightColor;
    	if (arcColors == null) {
    		awaitingArcColorSet = true;
    		return;
    	}
		for (int i = 0; i < arcColors.length; i++) {
    		for (int a = 0; a < arcList.size(); a++) {
    			if (arcList.get(a).getUnit() == i) {
    				if (traceEqualsFlightColor) {
    					arcList.get(a).setTraceColor(arcColors[i]);
    				} else {
    					arcList.get(a).setTraceColor(arcTraceColor);
    				}
    			}
    		}
    	}
    }
	
	@Override
	public void setArcCursorColor(Color arcCursorColor) {
		for (HyperbolicProjection arc : arcList) {
			arc.setCursorColor(arcCursorColor);
		}
		repaint();
	}
	
	@Override
	public void setArcTraceColor(Color arcTraceColor) {
		for (HyperbolicProjection arc : arcList) {
			arc.setTraceColor(arcTraceColor);
		}
		repaint();
	}
	
    @Override
    public void setArcIntersectPointColor(Color color) {
    	for (HyperbolicProjection arc : arcList) {
			arc.setCursorColor(arcCursorColor);
		}
		repaint();
    }

	@Override
	public void removeArc(int index) {
		try {
			arcList.remove(index);
			arcIndex = index - 1;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void addIcon(Point.Double point, String iconPath, String identifier) {
		if (point == null || upperLeftPoint == null || lowerRightPoint == null) return;
		Icon icon = new Icon(upperLeftPoint, lowerRightPoint, point, iconPath, identifier, frameSize);
		iconList.add(icon);
		iconIndex = iconList.size() - 1;
	}

	@Override
	public void moveIcon(int index, Point.Double point) {
		try {
			iconList.get(index).setLocation(point);
			iconIndex = index;
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
    public void addRing(Point.Double coord, double radius, Color color) {
		Style style = new Style(color, new Color(0,0,0,0), 
				new BasicStroke(), new Font("Calibri", Font.BOLD, 12));
		MapMarkerCircle ring = new MapMarkerCircle(null, null, new Coordinate(coord), radius, STYLE.FIXED, style);
		ringList.add(ring);
		ringIndex = ringList.size() - 1;
        repaint();
    }
	
	@Override
    public void addSignalMarker(Point.Double coord, Color color) {
		addSignalMarker(coord, 2, color);
    }
	
	@Override
	public void addSignalMarker(Point.Double point, double signalMarkerRadius, Color color) {
		Style style = new Style(color, color, new BasicStroke(), null);
		MapMarkerCircle signalMarker = new MapMarkerCircle(null, null, new Coordinate(point), signalMarkerRadius, STYLE.FIXED, style);
		signalMarkerList.add(signalMarker);
		signalMarkerIndex = signalMarkerList.size() - 1;
        repaint();
    }
	
	@Override
    public void removeRing(int index) {
	    try {
			ringList.remove(index);
	        ringIndex = index - 1;
	        repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
    }

	@Override
	public void deleteAllQuads() {
		quadList.clear();
		quadIndex = -1;
		repaint();
	}

	@Override
	public void addQuad(Point.Double point, Point.Double size, Color color) {
		Style style = new Style(color, color, new BasicStroke(), new Font("Calibri", Font.BOLD, 12));
		MapRectangleImpl quad = new MapRectangleImpl(null, null, new Coordinate(point), 
				new Coordinate(point.y - size.y, point.x + size.x), style);
		quadList.add(quad);
		quadIndex = quadList.size() - 1;
	}
	
	@Override
	public void changeQuadColor(int index, Color color) {
		try {
			Style style = new Style(new Color(color.getRed(), color.getGreen(), color.getBlue(), 128), 
					new Color(color.getRed(), color.getGreen(), color.getBlue(), 128), 
					new BasicStroke(), new Font("Calibri", Font.BOLD, 12));
			quadList.get(index).setStyle(style);
			quadIndex = index;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void deleteTestTile(TestTile testTile) {
		Iterator<MapPolygonImpl> iter = testTileList.iterator();
    	while(iter.hasNext()) {
    		MapPolygonImpl testTileList = iter.next();
    		if (testTile.getID() == testTileList.getID()) iter.remove();
    	}
		repaint();
	}

	@Override 
	public void deleteCurrentArc() {
		if (arcIndex > -1) {
			deleteArc(arcIndex);
		}
	}
	
	@Override
	public void deleteArc(int index) {
		try {
			arcList.remove(index);
			arcIndex = index - 1;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override 
	public void deleteCurrentIcon() {
		if (iconIndex > -1) {
			deleteIcon(iconIndex);
		}
	}
	
	@Override
	public void deleteIcon(int index) {
		try {
			iconList.remove(index);
			iconIndex = index - 1;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override 
	public void deleteCurrentRing() {
		if (ringIndex > -1) {
			deleteRing(ringIndex);
		}
	}
	
	@Override
	public void deleteRing(int index) {
		try {
			ringList.remove(index);
			ringIndex = index - 1;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override 
	public void deleteCurrentQuad() {
		if (quadIndex > -1) {
			deleteQuad(quadIndex);
		}
	}
	
	@Override
	public void deleteQuad(int index) {
		try {
			quadList.remove(index);
			quadIndex = index - 1;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override 
	public void deleteCurrentLine() {
		if (lineIndex > -1) {
			deleteLine(lineIndex);
		}
	}
	
	@Override
	public void deleteLine(int index) {
		try {
			lineList.remove(index);
			lineIndex = index - 1;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public int getCurrentLineIndex() {
		return lineIndex;
	}
	
	@Override
	public int getCurrentRingIndex() {
		return ringIndex;
	}
	
	@Override
	public int getCurrentQuadIndex() {
		return quadIndex;
	}
	
	@Override
	public int getCurrentIconIndex() {
		return iconIndex;
	}
	
	@Override
	public int getCurrentSignalMarkerIndex() {
		return signalMarkerIndex;
	}
	
	@Override
	public int numberOfIcons() {
		return iconList.size();
	}

	@Override
	public void showArcs(boolean showArcs) {
		this.showArcs = showArcs; 
		for (HyperbolicProjection arc : arcList) {
			arc.showArc(displayShapes && showArcs);
    	}
	}
	
	@Override
	public void showArcTrace(boolean showArcTraces) {
		this.showArcTraces = showArcTraces; 
		for (HyperbolicProjection arc : arcList) {
    		arc.showTrace(displayShapes && showArcTraces);
    	}
	}

	@Override
	public void showArcAsymptotes(boolean showArcAsymptotes) {
		this.showArcAsymptotes = showArcAsymptotes; 
		for (HyperbolicProjection arc : arcList) {
			arc.showAsymptote(displayShapes && showArcAsymptotes);
    	}
	}
	
	@Override
	public void showArcCursors(boolean showArcCursors) {
		this.showArcCursors = showArcCursors; 
		for (HyperbolicProjection arc : arcList) {
    		arc.showCursor(displayShapes && showArcCursors);
    	}
	}

	@Override
	public void showArcIntersectPoints(boolean showArcIntersectPoints) {
		this.showArcIntersectPoints = showArcIntersectPoints;
		for (MapMarkerCircle point : arcIntersectList) {
			point.setVisible(displayShapes && showArcIntersectPoints);
		}
	}
	
	@Override
	public void showIconLabels(boolean showIconLabels) {
		this.showIconLabels = showIconLabels;
		for (Icon tempIcon : iconList) {
    		tempIcon.showIconLabel(displayShapes && showIconLabels);
    	}
	}
	
	@Override
	public void showLines(boolean showLines) {
		this.showLines = showLines;
		repaint();
	}

	@Override
	public void showQuads(boolean showQuads) {
		this.showQuads = showQuads;
		repaint();
	}
	
	@Override
	public void showRings(boolean showRings) {
		this.showRings = showRings;
		repaint();
	}
	
	@Override
	public void showSignalMarkers(boolean showSignalMarkers) {
		this.showSignalMarkers = showSignalMarkers;
		repaint();
	}
	
	@Override
	public void showIcons(boolean showIcons) {
		this.showIcons = showIcons;
		for (Icon tempIcon : iconList) {
    		tempIcon.setVisible(displayShapes && showIcons);
    	}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
	    OpenStreetMapPanel clone = (OpenStreetMapPanel)super.clone();
	    return clone;
	}
	
	@Override
	public BufferedImage getScreenShot() {
		Dimension initialSize = frameSize;
		setSize(DEFAULT_PRINTER_PAGE_SIZE);
		BufferedImage image = new BufferedImage(frameSize.width, frameSize.height, BufferedImage.TYPE_INT_RGB);
		paint(image.createGraphics());
		setSize(initialSize);
		return image;
	}

	@Override
	public void setScale(double scale) {
		if (zoom - 3.0 != scale) {
			zoom = (int) (scale + 3.0);
			redimensionMap();
		}
	}

	@Override
	public double getScale() {
		return zoom - 3;
	}

	@Override
	public void deleteSignalMarker(int index) {
		try {
			signalMarkerList.remove(index);
			signalMarkerIndex = index - 1;
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void setGpsSymbolPosition(Point.Double point, int angle) {
		gpsDot.setLon(point.x);
		gpsDot.setLat(point.y);
		gpsArrow.setPoints(buildArrow(point, angle, gpsDot.getRadius() * 4));
		repaint();
	}

	@Override
	public void setArcCursorRadius(double arcCursorRadius) {
		this.arcCursorRadius = arcCursorRadius;
	}

	@Override
	public void setGpsSymbolAngle(int angle) {
		if (angle == 360) {
			gpsDot.setVisible(showGpsSymbol);
			gpsArrow.setVisible(false);
		} else {
			gpsArrow.setVisible(showGpsSymbol);
			gpsArrow.setPoints(buildArrow(centerLonLat, angle, gpsDot.getRadius() * 4));
			gpsDot.setVisible(false);
		}
		repaint();
	}

	@Override
	public void showStatusBar(boolean showStatusBar) {
		this.showStatusBar(showStatusBar);
	}

	@Override
	public void showBulkDownloaderPanel() {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {}
                new JTileDownloaderMainView().setVisible(true);
            }
        });
	}

	@Override
	public void showStatisticsPanel() {
		
	}

	@Override
	public void showLayerSelectorPanel() {
		
	}

	@Override
	public void addArcIntersectPoints(List<Double> arcIntersectPoints) {
		addArcIntersectPoints(arcIntersectPoints, arcIntersectPointRadius, arcIntersectPointColor);
	}

	@Override
	public void setArcIntersectPoints(List<Double> arcIntersectPoints) {
		arcIntersectList.clear();
		addArcIntersectPoints(arcIntersectPoints, arcIntersectPointRadius, arcIntersectPointColor);
	}

	@Override
	public void addArcIntersectPoints(List<Double> arcIntersectPoints, double radius, Color color) {
		Style style = new Style(color, color, new BasicStroke(), null);
		for (Double ip : arcIntersectPoints) {
			MapMarkerCircle mmc = new MapMarkerCircle(null, null, new Coordinate(ip), radius, STYLE.FIXED, style);
			arcIntersectList.add(mmc);
	        repaint();
		}
	}

	@Override
	public void setArcIntersectPoints(List<Double> arcIntersectPoints, double radius, Color color) {
		arcIntersectList.clear();
		addArcIntersectPoints(arcIntersectPoints, radius, color);;
	}

	@Override
	public void setQuadVisible(int index, boolean isVisible) {
		quadList.get(index).setVisible(isVisible);
		quadIndex = index;
	}

	@Override
	public void addTestTile(GeoTile geoTile, Color color, int id) {
		Style style = new Style(color, color, new BasicStroke(), null);
		MapPolygonImpl testTile = new MapPolygonImpl(null, null, geoTile.getCoordinates(), style, id);
		testTileList.add(testTile);
		repaint();
	}

	@Override
	public void changeTestTileColor(TestTile testTile, Color color) throws IndexOutOfBoundsException {
		try {
			Style style = new Style(color, color, new BasicStroke(), null);
			for (int i = 0; i < testTileList.size(); i++) {
				if (testTileList.get(i).getID() == testTile.getID()) {
					testTileList.get(i).setStyle(style);
				}
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteAllTestTiles() {
		testTileList.clear();
		repaint();
	}

	@Override
	public void showTestTiles(boolean showTestTiles) {
		this.showTestTiles = showTestTiles;
	}
	
	@Override
	public boolean isShowSignalMarkers() {
		return showSignalMarkers;
	}

	@Override
	public void setSignalMarkerRadius(double signalMarkerRadius) {
		for (int i = 0; i < signalMarkerList.size(); i++) {
			signalMarkerList.get(i).setRadius(signalMarkerRadius);
		}

	}

	@Override
	public void setArcTraceRadius(double arcTraceRadius) {
		for (HyperbolicProjection arc : arcList) {
			arc.setCursorRadius(arcTraceRadius);
		}
		repaint();
	}

	@Override
	public void setArcIntersectPointRadius(double arcIntersectPointRadius) {
		this.arcIntersectPointRadius = arcIntersectPointRadius;
	}

	@Override
	public boolean isShowTestTiles() {
		return showTestTiles;
	}

	@Override
	public boolean isShowLines() {
		return showLines;
	}

	@Override
	public boolean isShowRings() {
		return showRings;
	}

	@Override
	public boolean isShowArcIntersectPoints() {
		return showArcIntersectPoints;
	}

	@Override
	public void shutDown() {
		
	}

	@Override
	public Rectangle2D.Double getMapRectangle() {
		return new Rectangle2D.Double(upperLeftPoint.x, upperLeftPoint.y, getWidth(), getHeight());
	}

	@Override
	public MapDimension getMapDimension() {
		return new MapDimension(upperLeftPoint.y, lowerRightPoint.x, lowerRightPoint.y, upperLeftPoint.x);
	}

	@Override
	public Double getMapLowerRightCorner() {
		return lowerRightPoint;
	}

	@Override
	public void redraw() {
		repaint();
	}
    
	@Override
    public void moveMap(int x, int y) {
        tileController.cancelOutstandingJobs();
        center.x += x;
        center.y += y;
        repaint();
        this.fireJMVEvent(new JMVCommandEvent(Command.MOVE, this, true));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (center == null) return;
        Graphics2D g2d = (Graphics2D) g.create();
        try {
	        int iMove = 0;
	
	        int tilesize = tileSource.getTileSize();
	        int tilex = center.x / tilesize;
	        int tiley = center.y / tilesize;
	        int off_x = center.x % tilesize;
	        int off_y = center.y % tilesize;
	
	        int w2 = getWidth() / 2;
	        int h2 = getHeight() / 2;
	        int posx = w2 - off_x;
	        int posy = h2 - off_y;
	
	        int diff_left = off_x;
	        int diff_right = tilesize - off_x;
	        int diff_top = off_y;
	        int diff_bottom = tilesize - off_y;
	
	        boolean start_left = diff_left < diff_right;
	        boolean start_top = diff_top < diff_bottom;
	
	        if (start_top) {
	            if (start_left) {
	                iMove = 2;
	            } else {
	                iMove = 3;
	            }
	        } else {
	            if (start_left) {
	                iMove = 1;
	            } else {
	                iMove = 0;
	            }
	        } // calculate the visibility borders
	        int x_min = -tilesize;
	        int y_min = -tilesize;
	        int x_max = getWidth();
	        int y_max = getHeight();
	
	        // calculate the length of the grid (number of squares per edge)
	        int gridLength = 1 << zoom;
	
	        // paint the tiles in a spiral, starting from center of the map
	        boolean painted = true;
	        int x = 0;
	        while (painted) {
	            painted = false;
	            for (int i = 0; i < 4; i++) {
	                if (i % 2 == 0) {
	                    x++;
	                }
	                for (int j = 0; j < x; j++) {
	                    if (x_min <= posx && posx <= x_max && y_min <= posy && posy <= y_max) {
	                        // tile is visible
	                        Tile tile;
	                        if (scrollWrapEnabled) {
	                            // in case tilex is out of bounds, grab the tile to use for wrapping
	                            int tilexWrap = ((tilex % gridLength) + gridLength) % gridLength;
	                            tile = tileController.getTile(tilexWrap, tiley, zoom);
	                        } else {
	                            tile = tileController.getTile(tilex, tiley, zoom);
	                        }
	                        if (tile != null) {
	                            tile.paint(g2d, posx, posy, tilesize, tilesize);
	                            if (tileGridVisible) {
	                                g2d.drawRect(posx, posy, tilesize, tilesize);
	                            }
	                        }
	                        painted = true;
	                    }
	                    Point p = move[iMove];
	                    posx += p.x * tilesize;
	                    posy += p.y * tilesize;
	                    tilex += p.x;
	                    tiley += p.y;
	                }
	                iMove = (iMove + 1) % move.length;
	            }
	        }
	        // outer border of the map
	        int mapSize = tilesize << zoom;
	        if (scrollWrapEnabled) {
	            g2d.drawLine(0, h2 - center.y, getWidth(), h2 - center.y);
	            g2d.drawLine(0, h2 - center.y + mapSize, getWidth(), h2 - center.y + mapSize);
	        } else {
	            g2d.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);
	        }
	
	        // keep x-coordinates from growing without bound if scroll-wrap is enabled
	        if (scrollWrapEnabled) {
	            center.x = center.x % mapSize;
	        }
	        
	        if (showQuads && quadList != null && displayShapes) {
	            synchronized (quadList) {
	                for (MapRectangle quad : quadList) {
	                    if (quad.isVisible())
	                        paintRectangle(g2d, quad);
	                }
	            }
	        }
	        
	        if (showTestTiles && testTileList != null && displayShapes) {
	            synchronized (testTileList) {
	                for (MapPolygon testTile : testTileList) {
	                    if (testTile.isVisible())
	                        paintPolygon(g2d, testTile);
	                }
	            }
	        }
	
	        if (showGrid && gridLines != null && tilesAcrossScreen(gridSize) < MAX_TILES_ACROSS_SCREEN && displayShapes) {
	            synchronized (gridLines) {
	                for (MapRectangle gridLine : gridLines) {
	                    paintRectangle(g2d, gridLine);
	                }
	            }
	        }
	        
	        if (showRings && ringList != null && displayShapes) {
	            synchronized (ringList) {
	                for (MapMarker marker : ringList) {
	                    if (marker.isVisible())
	                        paintMarker(g2d, marker);
	                }
	            }
	        }
	        
	        if (showSignalMarkers && signalMarkerList != null && displayShapes) {
	            synchronized (signalMarkerList) {
	                for (MapMarker marker : signalMarkerList) {
	                    if (marker.isVisible())
	                        paintMarker(g2d, marker);
	                }
	            }
	        }
	        
	        if (showArcs && arcList != null && displayShapes) {
	            synchronized (arcList) {
	                for (HyperbolicProjection arc : arcList) {
	                    paintHyperbola(g2d, arc);
	                }
	            }
	        }
	
	        if (showGpsSymbol && gpsDot != null && displayShapes) {
	        	synchronized (gpsDot) {
	                if (gpsDot.isVisible()) paintMarker(g2d, gpsDot);
	                if (gpsArrow.isVisible()) paintPolyline(g2d, gpsArrow);
	            }
	        }
	        
	        if (showTargetRing && targetRing != null && displayShapes) {
	        	synchronized (targetRing) {
	                if (targetRing.isVisible()) paintMarker(g2d, targetRing);
	            }
	        }
	        
	        if (showLines && lineList != null && displayShapes) {
	            synchronized (lineList) {
	                for (MapPolylineImpl polyline : lineList) {
	                    if (polyline.isVisible())
	                        paintPolyline(g2d, polyline);
	                }
	            }
	        }
	        
	        attribution.paintAttribution(g2d, getWidth(), getHeight(), getPosition(0, 0), getPosition(getWidth(), getHeight()), zoom, this);
	    
        } finally {
	    	g2d.dispose();
	    }
        
    }
    
	@Override
	public void tileLoadingFinished(Tile tile, boolean success) {
		tile.setLoaded(success);
		if (success) tileCache.saveToFile(tile);
		redimensionMap();
	}

	@Override
    public void addJMVListener(JMapViewerEventListener listener) {
        evtListenerList.add(JMapViewerEventListener.class, listener);
    }
	
	@Override
    public void removeJMVListener(JMapViewerEventListener listener) {
        evtListenerList.remove(JMapViewerEventListener.class, listener);
    }

    private void fireJMVEvent(JMVCommandEvent evt) {
        Object[] listeners = evtListenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == JMapViewerEventListener.class) {
                ((JMapViewerEventListener) listeners[i + 1]).processCommand(evt);
            }
        }
    }

	@Override
	public AttributionSupport getAttribution() {
		return attribution;
	}
	
	@Override
    public double getMeterPerPixel() {
        Point origin = new Point(5, 5);
        Point center = new Point(getWidth() / 2, getHeight() / 2);

        double pDistance = center.distance(origin);

        ICoordinate originCoord = getPosition(origin);
        ICoordinate centerCoord = getPosition(center);

        double mDistance = tileSource.getDistance(originCoord.getLat(), originCoord.getLon(),
                centerCoord.getLat(), centerCoord.getLon());

        return mDistance / pDistance;
    }
	
	private void paintMarkers(Graphics2D g, List<MapMarkerCircle> markers) {
		for (MapMarkerCircle marker : markers) {
			paintMarker(g, marker);
		}
	}
	
    private void paintMarker(Graphics2D g, MapMarker marker) {
        Point p = getMapPosition(marker.getLat(), marker.getLon(), marker.getMarkerStyle() == MapMarker.STYLE.FIXED);
        Integer radius = getRadius(marker, p);
        if (scrollWrapEnabled) {
            int tilesize = tileSource.getTileSize();
            int mapSize = tilesize << zoom;
            if (p == null) {
                p = getMapPosition(marker.getLat(), marker.getLon(), false);
                radius = getRadius(marker, p);
            }
            marker.paint(g, p, radius);
            int xSave = p.x;
            int xWrap = xSave;
            // overscan of 15 allows up to 30-pixel markers to gracefully scroll off the edge of the panel
            while ((xWrap -= mapSize) >= -15) {
                p.x = xWrap;
                marker.paint(g, p, radius);
            }
            xWrap = xSave;
            while ((xWrap += mapSize) <= getWidth() + 15) {
                p.x = xWrap;
                marker.paint(g, p, radius);
            }
        } else {
            if (p != null) {
                marker.paint(g, p, radius);
            }
        }
    }
    
    protected void paintRectangle(Graphics2D g, MapRectangle rectangle) {
        Coordinate topLeft = rectangle.getTopLeft();
        Coordinate bottomRight = rectangle.getBottomRight();
        if (topLeft != null && bottomRight != null) {
            Point pTopLeft = getMapPosition(topLeft, false);
            Point pBottomRight = getMapPosition(bottomRight, false);
            if (pTopLeft != null && pBottomRight != null) {
                rectangle.paint(g, pTopLeft, pBottomRight);
                if (scrollWrapEnabled) {
                    int tilesize = tileSource.getTileSize();
                    int mapSize = tilesize << zoom;
                    int xTopLeftSave = pTopLeft.x;
                    int xTopLeftWrap = xTopLeftSave;
                    int xBottomRightSave = pBottomRight.x;
                    int xBottomRightWrap = xBottomRightSave;
                    while ((xBottomRightWrap -= mapSize) >= 0) {
                        xTopLeftWrap -= mapSize;
                        pTopLeft.x = xTopLeftWrap;
                        pBottomRight.x = xBottomRightWrap;
                        rectangle.paint(g, pTopLeft, pBottomRight);
                    }
                    xTopLeftWrap = xTopLeftSave;
                    xBottomRightWrap = xBottomRightSave;
                    while ((xTopLeftWrap += mapSize) <= getWidth()) {
                        xBottomRightWrap += mapSize;
                        pTopLeft.x = xTopLeftWrap;
                        pBottomRight.x = xBottomRightWrap;
                        rectangle.paint(g, pTopLeft, pBottomRight);
                    }
                }
            }
        }
    }

    protected void paintPolygon(Graphics2D g, MapPolygon testTile) {
        List<? extends ICoordinate> coords = testTile.getPoints();
        if (coords != null && coords.size() >= 3) {
            List<Point> points = new LinkedList<>();
            for (ICoordinate c : coords) {
                Point p = getMapPosition(c, false);
                if (p == null) {
                    return;
                }
                points.add(p);
            }
            testTile.paint(g, points);
            if (scrollWrapEnabled) {
                int tilesize = tileSource.getTileSize();
                int mapSize = tilesize << zoom;
                List<Point> pointsWrapped = new LinkedList<>(points);
                boolean keepWrapping = true;
                while (keepWrapping) {
                    for (Point p : pointsWrapped) {
                        p.x -= mapSize;
                        if (p.x < 0) {
                            keepWrapping = false;
                        }
                    }
                    testTile.paint(g, pointsWrapped);
                }
                pointsWrapped = new LinkedList<>(points);
                keepWrapping = true;
                while (keepWrapping) {
                    for (Point p : pointsWrapped) {
                        p.x += mapSize;
                        if (p.x > getWidth()) {
                            keepWrapping = false;
                        }
                    }
                    testTile.paint(g, pointsWrapped);
                }
            }
        }
    }
    
    protected void paintHyperbola(Graphics2D g, HyperbolicProjection hyperbola) {
    	if (hyperbola.showArc()) paintPolyline(g, hyperbola.getArcPolyline());
    	if (hyperbola.showTrace()) paintPolyline(g, hyperbola.getTracePolyline());
    	if (hyperbola.showAsymptote()) paintPolyline(g, hyperbola.getAsymptotePolyline()); 
    	if (hyperbola.showTrace()) paintMarkers(g, hyperbola.getTraceMarkers());
    	if (hyperbola.showCursors()) paintMarkers(g, hyperbola.getCursorMarkers());
    }
    
    protected void paintPolyline(Graphics2D g, MapPolyline polyline) {
        List<? extends ICoordinate> coords = polyline.getPoints();
        if (coords != null && coords.size() >= 3) {
            List<Point> points = new LinkedList<>();
            for (ICoordinate c : coords) {
                Point p = getMapPosition(c, false);
                if (p == null) {
                    return;
                }
                points.add(p);
            }
            polyline.paint(g, points);
            if (scrollWrapEnabled) {
                int tilesize = tileSource.getTileSize();
                int mapSize = tilesize << zoom;
                List<Point> pointsWrapped = new LinkedList<>(points);
                boolean keepWrapping = true;
                while (keepWrapping) {
                    for (Point p : pointsWrapped) {
                        p.x -= mapSize;
                        if (p.x < 0) {
                            keepWrapping = false;
                        }
                    }
                    polyline.paint(g, pointsWrapped);
                }
                pointsWrapped = new LinkedList<>(points);
                keepWrapping = true;
                while (keepWrapping) {
                    for (Point p : pointsWrapped) {
                        p.x += mapSize;
                        if (p.x > getWidth()) {
                            keepWrapping = false;
                        }
                    }
                    polyline.paint(g, pointsWrapped);
                }
            }
        }
    }
    
    private Point getMapPosition(double lat, double lon, boolean checkOutside) {
        Point p = tileSource.latLonToXY(lat, lon, zoom);
        p.translate(-(center.x - getWidth() / 2), -(center.y - getHeight() /2));

        if (checkOutside && (p.x < 0 || p.y < 0 || p.x > getWidth() || p.y > getHeight())) {
            return null;
        }
        return p;
    }

    private Integer getLatOffset(double lat, double lon, double offset, boolean checkOutside) {
        Point p = tileSource.latLonToXY(lat, lon, zoom);
        int y = p.y - center.y - getHeight() / 2;
        if (checkOutside && (y < 0 || y > getHeight())) {
            return null;
        }
        return y;
    }

    private Integer getRadius(MapMarker marker, Point p) {
        if (marker.getMarkerStyle() == MapMarker.STYLE.FIXED)
            return (int) marker.getRadius();
        else if (p != null) {
            Integer radius = getLatOffset(marker.getLat(), marker.getLon(), marker.getRadius(), false);
            radius = radius == null ? null : p.y - radius.intValue();
            return radius;
        } else
            return null;
    }

    private Point getMapPosition(ICoordinate coord, boolean checkOutside) {
        if (coord != null)
            return getMapPosition(coord.getLat(), coord.getLon(), checkOutside);
        else
            return null;
    }

    @Override
	public void handlePosition(MouseEvent e) {
        ICoordinate mouseCoords = getPosition(e.getPoint());  
        mouseLonLat = new Point.Double(mouseCoords.getLon(), mouseCoords.getLat());
    }
	
	@Override
	public void adviseMouseOffGlobe() {
		firePropertyChange(MOUSE_OFF_GLOBE, null, true);
	}

	@Override
	public void setGridReference(Point.Double gridReference) {
		this.gridReference = gridReference;
		if (showGrid && tileSize != null && gridReference != null && gridSize != null && gridColor != null) {
			gridLines.clear();
			gridLines.addAll(buildGrid(tileSize, gridReference, gridSize, gridColor));
			repaint();
		}
	}
	
	@Override
	public void setGridSize(Point.Double gridSize) {
		this.gridSize = gridSize;
		if (showGrid && tileSize != null && gridReference != null && gridSize != null && gridColor != null) {
			gridLines.clear();
			gridLines.addAll(buildGrid(tileSize, gridReference, gridSize, gridColor));
			repaint();
		}
	}
	
	private int tilesAcrossScreen(Point.Double gridSize) {
		ICoordinate ul = getPosition(0,0);     	
    	ICoordinate lr = getPosition(fsInsets.width, fsInsets.height);
		double w = Math.abs(lr.getLat() - ul.getLat());
		return (int) (w / (gridSize.x / 3600d));
	}
	
	private List<? extends MapRectangleImpl> buildGrid(Point.Double tileSize, Point.Double gridReference, Point.Double gridSize, Color color) {
		double width = Vincenty.milesToDegrees(gridSize.x, 90.0, getPosition().getLat());
		double height = Vincenty.milesToDegrees(gridSize.y, 0, getPosition().getLat());
		List<MapRectangleImpl> grid = Collections.synchronizedList(new LinkedList<MapRectangleImpl>());
		Style style = new Style(new Color(color.getRed(), color.getGreen(), color.getBlue(), 192), 
				new Color(color.getRed(), color.getGreen(), color.getBlue(), 192), new BasicStroke(), null);
		for (double v = gridReference.x; v < gridReference.x + width; v = v + (tileSize.x / 3600.0)) {
			MapRectangleImpl gridLine = new MapRectangleImpl(null, null, new Coordinate(gridReference.y, v), 
					new Coordinate(gridReference.y - height, v), style);
			grid.add(gridLine);
		}
		for (double h = gridReference.y; h > gridReference.y - height; h = h - (tileSize.y / 3600.0)) {
			MapRectangleImpl gridLine = new MapRectangleImpl(null, null, new Coordinate(h, gridReference.x), 
					new Coordinate(h, gridReference.x + width), style);
			grid.add(gridLine);
		}
		return grid;
	}
	
    /**
     * Sets the displayed map pane and zoom level so that all chosen map elements are visible.
     * @param markers whether to consider markers
     * @param rectangles whether to consider rectangles
     * @param testTiles whether to consider testTiles
     */
	@Override
    public void setDisplayToFitMapElements(boolean gpsMarker, boolean signalMarkers, boolean testTiles, boolean rings) {
        int nbElemToCheck = 0;
        if (gpsMarker && gpsDot!= null) nbElemToCheck += 1;
        if (signalMarkers && signalMarkerList != null) nbElemToCheck += signalMarkerList.size();
        if (testTiles && testTileList != null) nbElemToCheck += testTileList.size();
        if (rings && ringList != null) nbElemToCheck += ringList.size();
        if (nbElemToCheck == 0) return;

        int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMax = Integer.MIN_VALUE;
    //    int mapZoomMax = tileController.getTileSource().getMaxZoom();
        int mapZoomMax = 15;

        if (gpsMarker && gpsDot != null) {
            synchronized (gpsDot) {
                if (gpsDot.isVisible()) {
                    Point p = tileSource.latLonToXY(gpsDot.getCoordinate(), mapZoomMax);
                    xMax = Math.max(xMax, p.x);
                    yMax = Math.max(yMax, p.y);
                    xMin = Math.min(xMin, p.x);
                    yMin = Math.min(yMin, p.y);
                }
            }
        }

        if (signalMarkers && signalMarkerList != null) {
            synchronized (signalMarkerList) {
                for (MapMarker signalMarker : signalMarkerList) {
                    if (signalMarker.isVisible()) {
                        Point p = tileSource.latLonToXY(signalMarker.getCoordinate(), mapZoomMax);
                        xMax = Math.max(xMax, p.x);
                        yMax = Math.max(yMax, p.y);
                        xMin = Math.min(xMin, p.x);
                        yMin = Math.min(yMin, p.y);
                    }
                }
            }
        }
        
        if (rings && ringList != null) {
            synchronized (ringList) {
                for (MapMarker ring : ringList) {
                    if (ring.isVisible()) {
                        Point p = tileSource.latLonToXY(ring.getCoordinate(), mapZoomMax);
                        xMax = Math.max(xMax, p.x);
                        yMax = Math.max(yMax, p.y);
                        xMin = Math.min(xMin, p.x);
                        yMin = Math.min(yMin, p.y);
                    }
                }
            }
        }

        if (testTiles && testTileList != null) {
            synchronized (testTileList) {
                for (MapPolygon testTile : testTileList) {
                    if (testTile.isVisible()) {
                        for (ICoordinate c : testTile.getPoints()) {
                            Point p = tileSource.latLonToXY(c, mapZoomMax);
                            xMax = Math.max(xMax, p.x);
                            yMax = Math.max(yMax, p.y);
                            xMin = Math.min(xMin, p.x);
                            yMin = Math.min(yMin, p.y);
                        }
                    }
                }
            }
        }

        int height = Math.max(0, getHeight());
        int width = Math.max(0, getWidth());
        int newZoom = mapZoomMax;
        int x = xMax - xMin;
        int y = yMax - yMin;
        while (x > width || y > height) {
            newZoom--;
            x >>= 1;
            y >>= 1;
        }
        x = xMin + (xMax - xMin) / 2;
        y = yMin + (yMax - yMin) / 2;
        int z = 1 << (mapZoomMax - newZoom);
        x /= z;
        y /= z;
        setDisplayPosition(x, y, newZoom);
    }
    
    public void setDisplayPosition(int x, int y, int zoom) {
        setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), x, y, zoom);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
    	return false;
    }

}

		    	 