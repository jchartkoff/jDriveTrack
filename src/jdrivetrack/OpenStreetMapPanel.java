package jdrivetrack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLayeredPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import org.openstreetmap.fma.jtiledownloader.views.main.JTileDownloaderMainView;

import events.JMVCommandEvent.COMMAND;
import events.JMVCommandEvent;

import interfaces.MapPolygon;
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

public class OpenStreetMapPanel extends JLayeredPane implements MapInterface, Cloneable, TileLoaderListener {
	private static final long serialVersionUID = -1154235901605771509L;
	
	private static final Cursor DEFAULT_MAP_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	private static final Point.Double DEFAULT_MAP_CENTER_LON_LAT = new Point.Double(-86,35);
    private static final Dimension PREFERRED_SIZE = new Dimension(800,600);
    private static final int DEFAULT_ZOOM = 6;
    private static final Dimension DEFAULT_TILE_DIMENSIONS = new Dimension(256,256);
    private static final Dimension DEFAULT_PRINTER_PAGE_SIZE = new Dimension(1035,800);
    private static final String DEFAULT_TILE_CACHE_PATH = System.getProperty("user.home") + 
		File.separator + "drivetrack" + File.separator + "cache";
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};
    
    public static boolean debug = false;
    
    private boolean displayShapes = true;
    private Color[] arcColors = null;
    private List<String> compIndex = new ArrayList<String>();
	private int zoom = 6;
    private Grid grid;
    private Point.Double mouseLonLat = new Point.Double(0.0,0.0);
	private Point.Double upperLeftPoint = null;
	private Point.Double lowerRightPoint = null;
	private boolean showPolygons = false;
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
	private double gpsArrowSize = 30;
	private double arcCursorRadius;
	private double arcTraceRadius;
	private double arcIntersectPointRadius;
	private double signalMarkerRadius;
	private Color arcCursorColor;
	private Color arcTraceColor;
	private Color arcAsymptoteColor;
	private Color arcIntersectPointColor;
	private Arrow gpsArrow;
	private MarkerArrayLayer signalMarkerLayer;
	private MarkerArrayLayer arcIntersectPointLayer;
	private PolygonArrayLayer polygonLayer;
	private List<Line> lineList = new ArrayList<Line>(128);
	private MapMarkerCircle targetRing;
	private MapMarkerDot gpsDot;
	private List<MapMarker> ringList;
	private List<Quad> quadList;
	private List<Icon> iconList = new ArrayList<Icon>(128);
	private List<HyperbolicProjection> arcList = new ArrayList<HyperbolicProjection>(128);
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
    private Double gridReferencePoint = new Point.Double(0,0);
    
    public OpenStreetMapPanel() {
        this(DEFAULT_MAP_CENTER_LON_LAT, DEFAULT_ZOOM, PREFERRED_SIZE, DEFAULT_TILE_DIMENSIONS, DEFAULT_TILE_CACHE_PATH);
    }

    public OpenStreetMapPanel(Dimension frameSize) {
    	this(DEFAULT_MAP_CENTER_LON_LAT, DEFAULT_ZOOM, frameSize, DEFAULT_TILE_DIMENSIONS, DEFAULT_TILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point.Double centerLonLat, int zoom) {
    	this(centerLonLat, zoom, PREFERRED_SIZE, DEFAULT_TILE_DIMENSIONS, DEFAULT_TILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point.Double centerLonLat, int zoom, Dimension frameSize) {
    	this(centerLonLat, zoom, frameSize, DEFAULT_TILE_DIMENSIONS, DEFAULT_TILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point.Double centerLonLat, int zoom, Dimension frameSize, Dimension tileSize, String tileCachePath) {
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
		tileCache = new TileCache();
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
	            if (TileCache.RESTORED.equals(event.getPropertyName())) {
	            	tileCacheReady(event);
	            }
	            if (TileCache.PROGRESS.equals(event.getPropertyName())) {
	            	tileCacheProgress(event);
	            }
        	}
        });

        progressMonitor = new ProgressMonitor(SwingUtilities.getWindowAncestor(this), 
        		"Retreiving Cached Map Tiles From Disk", String.format("Completed %d%% of restore", 0),0, 100);
        
        progressMonitor.setMillisToPopup(0);

        tileCache.restoreDiskCache();
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
    	if (progressMonitor.isCanceled()) {
    		tileCache.cancel(false);
			progressMonitor.close();
    	} else {
            int progress = (Integer) event.getNewValue();
            progressMonitor.setProgress(progress);
            progressMonitor.setNote(String.format("Completed %d%% of restore", progress));
    	}
    }

    private void addComponentLayers() {
		Style gpsDotStyle = new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, 64), new BasicStroke(), new Font("Calibri", Font.BOLD, 12));
		gpsDot = new MapMarkerDot(new Coordinate(centerLonLat), 2, gpsDotStyle);
		Style targetRingStyle = new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, 16), new BasicStroke(), new Font("Calibri", Font.BOLD, 12));
        targetRing = new MapMarkerCircle(null, null, new Coordinate(centerLonLat), 100, STYLE.FIXED, targetRingStyle);
		
		ringList = Collections.synchronizedList(new LinkedList<MapMarker>());
		quadList = Collections.synchronizedList(new LinkedList<Quad>());
        
        grid = new Grid(upperLeftPoint, lowerRightPoint, fsInsets, gridReferencePoint);

        gpsArrow = new Arrow(upperLeftPoint, lowerRightPoint, gpsArrowSize, fsInsets);

        signalMarkerLayer = new MarkerArrayLayer(upperLeftPoint, lowerRightPoint, fsInsets);
    	arcIntersectPointLayer = new MarkerArrayLayer(upperLeftPoint, lowerRightPoint, fsInsets, 
    		arcIntersectPointRadius, arcIntersectPointColor);
    	polygonLayer = new PolygonArrayLayer(upperLeftPoint, lowerRightPoint, fsInsets);

        compIndex.add("GPS_ARROW");
        add(gpsArrow, 0);
        
        compIndex.add("SAMPLE_GRID");
		add(grid, 1);

		compIndex.add("SIGNAL_MARKERS");
		add(signalMarkerLayer, 2);
		
		compIndex.add("ARC_INTERSECT_POINTS");
		add(arcIntersectPointLayer, 3);
		
		compIndex.add("POLYGONS");
		add(polygonLayer, 4);
    }
    
    @Override
    public void setGridColor(Color color) {
    	grid.setColor(color);
    }

    private void setDisplayPosition(ICoordinate to, int zoom) {
        setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), to, zoom);
    }

    private void setDisplayPosition(Point mapPoint, ICoordinate to, int zoom) {
        Point p = tileSource.latLonToXY(to, zoom);
        setDisplayPosition(mapPoint, p.x, p.y, zoom);
    }

    private void setDisplayPosition(Point mapPoint, int x, int y, int zoom) {
        if (zoom > tileController.getTileSource().getMaxZoom())
            return;

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
    	
    	grid.setCornerLonLat(upperLeftPoint, lowerRightPoint);

    	gpsArrow.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	signalMarkerLayer.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	arcIntersectPointLayer.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	polygonLayer.setCornerLonLat(upperLeftPoint, lowerRightPoint);

    	for (Icon tempIcon : iconList) {
    		tempIcon.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	}
    	
    	for (Line tempLine : lineList) {
    		tempLine.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	}
    	
    	for (HyperbolicProjection tempArc : arcList) {
    		tempArc.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	}
    	
    	for (Quad tempQuad : quadList) {
    		tempQuad.setCornerLonLat(upperLeftPoint, lowerRightPoint);
    	}
    	
    	setIgnoreRepaint(false);
    	repaint();
    }

    @Override
    public void displayShapes(boolean displayShapes) {
    	this.displayShapes = displayShapes;
    	grid.setVisible(showGrid && displayShapes);
    	gpsDot.setVisible(displayShapes && showGpsSymbol);
    	gpsArrow.setVisible(displayShapes && showGpsSymbol);
    	targetRing.setVisible(displayShapes && showTargetRing);
    	signalMarkerLayer.setVisible(displayShapes && showSignalMarkers);
    	arcIntersectPointLayer.setVisible(displayShapes && showArcIntersectPoints);
    	polygonLayer.setVisible(displayShapes && showPolygons);
    	
    	for (Icon tempIcon : iconList) {
    		tempIcon.setVisible(displayShapes && showIcons);
    		if (displayShapes && showIcons) tempIcon.showIconLabel(displayShapes && showIconLabels);
    	}
    	
    	for (Line tempLine : lineList) {
    		tempLine.setVisible(displayShapes && showLines);
    	}

    	for (HyperbolicProjection tempArc : arcList) {
			tempArc.showArc(displayShapes && showArcs);
			tempArc.showAsymptote(displayShapes && showArcAsymptotes);
    		tempArc.showCursor(displayShapes && showArcCursors);
    		tempArc.showTrace(displayShapes && showArcTraces);
    	}
    	
    	for (Quad tempQuad : quadList) {
    		tempQuad.setVisible(displayShapes && showQuads);
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
        this.fireJMVEvent(new JMVCommandEvent(COMMAND.ZOOM, this, zoom));
    }

    @Override
    public void setZoom(int zoom) {
        setZoom(zoom, new Point(getWidth() / 2, getHeight() / 2));
    }

    private void zoomChanged(int oldZoom) {
    	this.fireJMVEvent(new JMVCommandEvent(COMMAND.ZOOM_OUT_DISABLED, this, zoom <= tileController.getTileSource().getMinZoom()));
    	this.fireJMVEvent(new JMVCommandEvent(COMMAND.ZOOM_IN_DISABLED, this, zoom >= tileController.getTileSource().getMaxZoom()));
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
	public void setGridSize(Point.Double gridSize) {
		grid.setGridSize(gridSize);
		polygonLayer.setTileSize(gridSize);
	}

	@Override
	public Point.Double getGridSize() {
		return grid.getGridSize();
	}

	@Override
	public void showMapImage(boolean showMapImage) {
		this.showMapImage = showMapImage;
		 repaint();
	}
	
	@Override
	public void showGrid(boolean showGrid) {
		this.showGrid = showGrid;
		grid.setVisible(showGrid && displayShapes);
	}
	
	@Override
	public boolean isShowTargetRing() {
		return targetRing.isVisible();
	}
	
	@Override
	public boolean isShowGrid() {
		return grid.isVisible();
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
		arcIntersectPointLayer.deleteAllMarkers();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("ARC_INTERSECT")) compIndex.remove(i);
		}
	}
	
	@Override
	public void deleteAllRings() {
		ringList.clear();
        repaint();
	}
	
	@Override
	public void deleteAllIcons() {
		iconList.subList(0, iconList.size()).clear();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("ICON")) compIndex.remove(i);
		}
	}
	
	@Override
	public void deleteAllLines() {
		lineList.subList(0, lineList.size()).clear();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("LINE")) compIndex.remove(i);
		}
	}

	@Override
	public void deleteAllArcs() {
		arcList.subList(0, arcList.size()).clear();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("ARC")) compIndex.remove(i);
		}
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
		repaint();
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
			gpsArrow.setLocation(point, angle);
			gpsArrow.setArrowSize(radius);
			gpsArrow.setColor(color);
			gpsDot.setVisible(false);
		}
		repaint();
	}
	
	@Override
	public void showTargetRing(boolean showTargetRing) {
		this.showTargetRing = showTargetRing;
		if (targetRing.isVisible() != showTargetRing) targetRing.setVisible(showTargetRing && displayShapes);
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
		arcIntersectPointLayer.addMarker(point, radius, color);
		repaint();
	}

	@Override
	public void addLine(Point.Double p1, double angle, Color color) {
		if (p1 == null || upperLeftPoint == null || lowerRightPoint == null || Math.abs(angle) >= 360 || color == null) return;
		Line line = new Line(upperLeftPoint, lowerRightPoint, p1, angle, frameSize, color);
		lineList.add(line);
		compIndex.add("LINE " + Integer.toString(lineList.size() - 1));
		add(line, compIndex.size() - 1);
	}
	
	@Override
	public void addLine(Point.Double p1, Point.Double p2, Color color) {
		if (p1 == null || p2 == null || upperLeftPoint == null || lowerRightPoint == null || color == null) return;
		Line line = new Line(upperLeftPoint, lowerRightPoint, p1, p2, frameSize, color);
		lineList.add(line);
		compIndex.add("LINE " + Integer.toString(lineList.size() - 1));
		add(line, compIndex.size() - 1);
	}

	@Override
	public void addArc(ConicSection cone) {
		Color traceColor;
		Color arcColor;
		if (arcColors != null && traceEqualsFlightColor) {
			traceColor = arcColors[cone.getUnit()];
			arcColor = arcColors[cone.getUnit()];
		} else {
			if (arcColors == null) awaitingArcColorSet = true;
			traceColor = arcTraceColor;
			arcColor = arcTraceColor;
		}
		HyperbolicProjection hyperbola = new HyperbolicProjection(upperLeftPoint, lowerRightPoint, 
			cone, frameSize, arcColor, showArcs, arcAsymptoteColor, showArcAsymptotes, 
			arcCursorRadius, arcCursorColor, showArcCursors, arcTraceRadius, showArcTraces);
		arcList.add(hyperbola);
		hyperbola.setTraceColor(traceColor);
		compIndex.add("ARC " + Integer.toString(arcList.size() - 1));
		add(hyperbola, compIndex.size() - 1);
	}
	
	@Override
	public void setArcAsymptoteColor(Color arcAsymptoteColor) {
		this.arcAsymptoteColor = arcAsymptoteColor;
	}
    
	@Override
    public void setArcColors(Color[] arcColors) {
    	for (int i = 0; i < arcColors.length; i++) {
    		for (int a = 0; a < arcList.size(); a++) {
    			if (arcList.get(a).getConicSection().getUnit() == i) {
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
    			if (arcList.get(a).getConicSection().getUnit() == i) {
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
		this.arcCursorColor = arcCursorColor;
	}
	
	@Override
	public void setArcTraceColor(Color arcTraceColor) {
		this.arcTraceColor = arcTraceColor;
		if (arcColors!= null && !traceEqualsFlightColor) {
			for (int i = 0; i < arcList.size(); i++) {
	    		arcList.get(i).setTraceColor(arcTraceColor);
	    	}
		}
	}
	
    @Override
    public void setArcIntersectPointColor(Color color) {
    	arcIntersectPointLayer.setAllColors(color);
    }

	@Override
	public void removeArc(int index) {
		try {
			arcList.remove(index);
			remove(compIndex.indexOf("ARC " + Integer.toString(index)));
			compIndex.remove("ARC " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void addIcon(Point.Double point, String iconPath, String identifier) {
		if (point == null || upperLeftPoint == null || lowerRightPoint == null) return;
		Icon icon = new Icon(upperLeftPoint, lowerRightPoint, point, iconPath, identifier, frameSize);
		iconList.add(icon);
		compIndex.add("ICON " + Integer.toString(iconList.size() - 1));
		add(icon, compIndex.size() - 1);
	}

	@Override
	public void moveIcon(int index, Point.Double point) {
		try {
			iconList.get(index).setLocation(point);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void hideIcon(int index) {
		try {
			iconList.remove(index);
			remove(compIndex.indexOf("ICON " + Integer.toString(index)));
			compIndex.remove("ICON " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
    public void addRing(Point.Double coord, double radius, Color color) {
		Style style = new Style(new Color(color.getRed(), color.getGreen(), color.getBlue(), 128), new Color(0,0,0,0), new BasicStroke(), new Font("Calibri", Font.BOLD, 12));
		MapMarker ring = new MapMarkerCircle(null, null, new Coordinate(coord), radius, STYLE.FIXED, style);
		ringList.add(ring);
        repaint();
    }
	
	@Override
    public void removeRing(int index) {
        ringList.remove(index);
        repaint();
    }

	@Override
	public void deleteAllQuads() {
		quadList.subList(0, quadList.size()).clear();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("QUAD")) compIndex.remove(i);
		}
	}

	@Override
	public void addQuad(Point.Double point, Point.Double size, Color color) {
		Quad quad = new Quad(upperLeftPoint, lowerRightPoint, point, size, frameSize, color);
		quadList.add(quad);
		compIndex.add("QUAD " + Integer.toString(quadList.size() - 1));
		add(quad, compIndex.size() - 1);		
	}

	@Override
	public void changeQuadColor(int index, Color color) {
		try {
			quadList.get(index).setQuadColor(color);
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void deletePolygon(int index) {
		try {
			polygonLayer.deletePolygon(index);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public int numberOfIcons() {
		return iconList.size();
	}

	@Override
	public void showArcs(boolean showArcs) {
		this.showArcs = showArcs; 
		for (HyperbolicProjection tempArc : arcList) {
			tempArc.showArc(displayShapes && showArcs);
    	}
	}
	
	@Override
	public void showArcTrace(boolean showArcTraces) {
		this.showArcTraces = showArcTraces; 
		for (HyperbolicProjection tempArc : arcList) {
    		tempArc.showTrace(displayShapes && showArcTraces);
    	}
	}

	@Override
	public void showArcAsymptotes(boolean showArcAsymptotes) {
		this.showArcAsymptotes = showArcAsymptotes; 
		for (HyperbolicProjection tempArc : arcList) {
			tempArc.showAsymptote(displayShapes && showArcAsymptotes);
    	}
	}
	
	@Override
	public void showArcCursors(boolean showArcCursors) {
		this.showArcCursors = showArcCursors; 
		for (HyperbolicProjection tempArc : arcList) {
    		tempArc.showCursor(displayShapes && showArcCursors);
    	}
	}
	
	@Override
	public void showQuads(boolean showQuads) {
		this.showQuads = showQuads;
		for (Quad tempQuad : quadList) {
    		tempQuad.setVisible(displayShapes && showQuads);
    	}
	}

	@Override
	public void showArcIntersectPoints(boolean showArcIntersectPoints) {
		this.showArcIntersectPoints = showArcIntersectPoints;
		arcIntersectPointLayer.setVisible(showArcIntersectPoints);
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
		for (Line tempLine : lineList) {
    		tempLine.setVisible(displayShapes && showLines);
    	}
	}

	@Override
	public void showRings(boolean showRings) {
		this.showRings = showRings;
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
	public void addSignalMarker(Double point, double signalMarkerRadius, Color color) {
		signalMarkerLayer.addMarker(point, signalMarkerRadius, color);
	}

	@Override
	public void deleteAllSignalMarkers() {
		signalMarkerLayer.deleteAllMarkers();
	}

	@Override
	public void deleteSignalMarker(int index) throws IndexOutOfBoundsException {
		signalMarkerLayer.deleteMarker(index);
	}

	@Override
	public void hideLine(int index) throws IndexOutOfBoundsException {
		lineList.remove(index);
		 repaint();
	}

	@Override
	public void deleteQuad(int index) throws IndexOutOfBoundsException {
		quadList.remove(index);
		 repaint();
	}

	@Override
	public void showSignalMarkers(boolean showSignalMarkers) {
		this.showSignalMarkers = showSignalMarkers;
	}

	@Override
	public void setGpsSymbolPosition(Double point, int angle) {
		gpsArrow.setLocation(point, angle);
	}

	@Override
	public void setArcCursorRadius(double arcCursorRadius) {
		this.arcCursorRadius = arcCursorRadius;
	}

	@Override
	public void setGpsSymbolAngle(int gpsAngle) {
		if (gpsAngle == 360) {
			gpsDot.setVisible(showGpsSymbol);
			gpsArrow.setVisible(false);
		} else {
			gpsArrow.setVisible(showGpsSymbol);
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
	public void addArcIntersectPoints(List<Double> arcIntersectList) {
		arcIntersectPointLayer.addMarkers(arcIntersectList, arcIntersectPointRadius, arcIntersectPointColor);
	}

	@Override
	public void setArcIntersectPoints(List<Double> arcIntersectList) {
		arcIntersectPointLayer.deleteAllMarkers();
		addArcIntersectPoints(arcIntersectList);
	}

	@Override
	public void addArcIntersectPoints(List<Double> iplist, double radius, Color color) {
		arcIntersectPointLayer.addMarkers(iplist, radius, color);
	}

	@Override
	public void setArcIntersectPoints(List<Double> iplist, double radius, Color color) {
		arcIntersectPointLayer.deleteAllMarkers();
		arcIntersectPointLayer.addMarkers(iplist, radius, color);
	}

	@Override
	public void setQuadVisible(int index, boolean isVisible) {
		quadList.get(index). setVisible(isVisible);
	}

	@Override
	public void addPolygon(GeoTile geotile) {
		polygonLayer.addPolygon(geotile.getPoint());
	}

	@Override
	public void setPolygonVisible(int index, boolean isVisible) {
		polygonLayer.setPolygonVisible(index, isVisible);
	}

	@Override
	public void changePolygonColor(int index, Color color) throws IndexOutOfBoundsException {
		polygonLayer.setColor(index, color);
	}

	@Override
	public void deleteAllPolygons() {
		polygonLayer.deleteAllPolygons();
	}

	@Override
	public void showPolygons(boolean showPolygons) {
		this.showPolygons = showPolygons;
		polygonLayer.setVisible(showPolygons);
	}
	
	@Override
	public boolean isShowSignalMarkers() {
		return showSignalMarkers;
	}

	@Override
	public void setSignalMarkerRadius(double signalMarkerRadius) {
		this.signalMarkerRadius = signalMarkerRadius;
		signalMarkerLayer.setAllDiameters(signalMarkerRadius * 2);
	}

	@Override
	public void setArcTraceRadius(double arcTraceRadius) {
		this.arcTraceRadius = arcTraceRadius;
	}

	@Override
	public void setArcIntersectPointRadius(double arcIntersectPointRadius) {
		this.arcIntersectPointRadius = arcIntersectPointRadius;
	}

	@Override
	public boolean isShowPolygons() {
		return showPolygons;
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
	public void addSignalMarker(Point.Double point, Color color) {
		signalMarkerLayer.addMarker(point, signalMarkerRadius, color);
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
        this.fireJMVEvent(new JMVCommandEvent(COMMAND.MOVE, this, true));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (center == null) return;
        
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
                            tile.paint(g, posx, posy, tilesize, tilesize);
                            if (tileGridVisible) {
                                g.drawRect(posx, posy, tilesize, tilesize);
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
            g.drawLine(0, h2 - center.y, getWidth(), h2 - center.y);
            g.drawLine(0, h2 - center.y + mapSize, getWidth(), h2 - center.y + mapSize);
        } else {
            g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);
        }

        // keep x-coordinates from growing without bound if scroll-wrap is enabled
        if (scrollWrapEnabled) {
            center.x = center.x % mapSize;
        }
        
        if (showRings && ringList != null) {
            synchronized (ringList) {
                for (MapMarker marker : ringList) {
                    if (marker.isVisible())
                        paintMarker(g, marker);
                }
            }
        }
        
        if (showGpsSymbol && gpsDot != null) {
        	synchronized (gpsDot) {
                if (gpsDot.isVisible()) paintMarker(g, gpsDot);
            }
        }
        
        if (showTargetRing && targetRing != null) {
        	synchronized (targetRing) {
                if (targetRing.isVisible()) paintMarker(g, targetRing);
            }
        }

        attribution.paintAttribution(g, getWidth(), getHeight(), getPosition(0, 0), getPosition(getWidth(), getHeight()), zoom, this);
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
	
    private void paintMarker(Graphics g, MapMarker marker) {
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
    
    protected void paintRectangle(Graphics g, MapRectangle rectangle) {
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

    protected void paintPolygon(Graphics g, MapPolygon polygon) {
        List<? extends ICoordinate> coords = polygon.getPoints();
        if (coords != null && coords.size() >= 3) {
            List<Point> points = new LinkedList<>();
            for (ICoordinate c : coords) {
                Point p = getMapPosition(c, false);
                if (p == null) {
                    return;
                }
                points.add(p);
            }
            polygon.paint(g, points);
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
                    polygon.paint(g, pointsWrapped);
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
                    polygon.paint(g, pointsWrapped);
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
	public void setGridReference(Double gridReference) {
		grid.setReferencePoint(gridReference);
	}
}

		    	 