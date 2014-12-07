package com;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class OpenStreetMapPanel extends JLayeredPane implements Map, Cloneable, MouseMotionListener, MouseWheelListener, MouseListener {
	private static final long serialVersionUID = -1154235901605771509L;
	
	private static final Point DEFAULT_MAP_POSITION = new Point(1000, 1000);
    private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);
    private static final int DEFAULT_ZOOM = 6;
    private static final Dimension DEFAULT_TILE_DIMENSIONS = new Dimension(256,256);
    private static final Dimension DEFAULT_PRINTER_PAGE_SIZE = new Dimension(1035, 800);
    private static final String DEFAULT_FILE_CACHE_PATH = System.getProperty("user.home") + File.separator + 
			"drivetrack" + File.separator + "cache";
    private static final int THREADS = 32;

    private int oldZoom = -1;
    private boolean dragging = false;
    private boolean zooming = false;
    private boolean fullImageRendered = false;
    private boolean displayShapes = false;
    private Color[] arcColors;
    private HyperbolicProjection slp = null;
    private Dimension tileSize = null;
    private int tx, ty;
    private int dx, dy, tw, th;
    private BufferedImage image;
	private List<String> compIndex = new ArrayList<String>();
	private TileCache tileCache = null;
	private String tileCachePath;
    private TileServer tileServer = null;    
    private Point mapPosition = null;
    private int zoom = 1;
    private List<Point.Double> arcPointList = null;
    private Hyperbola2D hyperbola = null;
    private Grid grid;
    private Point.Double mouseLonLat = null;
	private Point.Double upperLeftLonLat = null;
	private Point.Double lowerRightLonLat= null;
	private boolean showQuads = false;
	private boolean showDots = false;
	private boolean showLines = false;
	private boolean showRings = false;
	private boolean showIcons = false;
	private boolean showGrid = false;
	private boolean showArcs = false;
	private boolean showArcTrails = false;
	private boolean trailEqualsFlightColor = false;
	private boolean showArcIntersectPoints = false;
	private boolean showArcAsymptotes = false;
	private boolean showArcCursors = false;
	private boolean showIconLabels = false;
	private boolean showGpsSymbol = false;
	private boolean showTargetRing = false;
	private boolean showMapImage = true;
	private int arcCursorDiameter;
	private Color arcCursorColor;
	private Color arcTrailColor;
	private Color arcAsymptoteColor;
	private Dot gpsDot;
	private Ring targetRing;
	private Arrow gpsArrow;
	private List<Quad> quadList = new ArrayList<Quad>(1000);
	private List<Line> lineList = new ArrayList<Line>(1000);
	private List<Dot> dotList = new ArrayList<Dot>(1000);
	private List<Dot> arcIntersectList = new ArrayList<Dot>(1000);
	private List<Ring> ringList = new ArrayList<Ring>(1000);
	private List<Icon> iconList = new ArrayList<Icon>(1000);
	private List<HyperbolicProjection> arcList = new ArrayList<HyperbolicProjection>(1024);
    private List<ImageRequest> imageRequests = new ArrayList<ImageRequest>();
    private int tilesReturned = 0;
    private Point mouseCoords = null;
    private Point downCoords = null;
    private Point downPosition = null;
    private BlockingQueue<ImageTile> paintEvents = new LinkedBlockingQueue<ImageTile>(1024);
    private ActionListener painter;
    private Timer paintTimer;
    private Point imageLoc;
    private Dimension imageSize;
    private BufferedImage timg;
    private boolean full = false;
    private ProgressMonitor progressMonitor;
    
    public OpenStreetMapPanel() {
        this(DEFAULT_MAP_POSITION, DEFAULT_ZOOM, PREFERRED_SIZE, DEFAULT_TILE_DIMENSIONS, DEFAULT_FILE_CACHE_PATH);
    }

    public OpenStreetMapPanel(Dimension frameSize) {
    	this(DEFAULT_MAP_POSITION, DEFAULT_ZOOM, frameSize, DEFAULT_TILE_DIMENSIONS, DEFAULT_FILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point mapPosition, int zoom) {
    	this(mapPosition, zoom, PREFERRED_SIZE, DEFAULT_TILE_DIMENSIONS, DEFAULT_FILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point.Double centerLonLat, int zoom, Dimension frameSize) {
    	this(centerLonLatToMapPosition(centerLonLat, zoom, frameSize, DEFAULT_TILE_DIMENSIONS), zoom, frameSize, 
    			DEFAULT_TILE_DIMENSIONS, DEFAULT_FILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point mapPosition, int zoom, Dimension frameSize) {
    	this(mapPosition, zoom, frameSize, DEFAULT_TILE_DIMENSIONS, DEFAULT_FILE_CACHE_PATH);
    }
    
    public OpenStreetMapPanel(Point mapPosition, int zoom, Dimension frameSize, Dimension tileSize, String tileCachePath) {
        this.mapPosition = mapPosition;
    	this.zoom = zoom;
        this.tileSize = tileSize;
        this.tileCachePath = tileCachePath;
        setSize(frameSize);
        setPreferredSize(frameSize);
        setDoubleBuffered(true);
        setOpaque(true);
        setBackground(Color.BLACK);
        setFocusable(true);
		setFocusTraversalKeysEnabled(true);
		setVisible(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        configurePanel();
        redimensionMap(mapPosition, zoom, getSize());
        configureInstance();
        displayShapes(true);
    }
    
    private void configureInstance() {
    	tileCache = new TileCache(tileCachePath);
    	
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
        
        tileServer = new TileServer();
        
        tileServer.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if (TileServer.ONLINE.equals(event.getPropertyName())) {
	            	networkInterfaceOnline(event);
	            }
	            if (TileServer.AVAILABLE.equals(event.getPropertyName())) {
	            	tileServerAvailable(event);
	            }
        	}
        });

        painter = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paintJob(e);
			}
        };
        
        paintTimer = new Timer(25, painter);
        paintTimer.setCoalesce(false);

        tileCache.restoreCache();

        progressMonitor = ProgressUtil.createModalProgressMonitor(this, "Loading Tiles...", 
        		SwingConstants.HORIZONTAL, 0, 100, false, 50, 150);
        
        progressMonitor.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if ("CANCEL".equals(event.getPropertyName())) {
	            	cancelProgressMonitor(event);
	            }
        	}
        });
        
        progressMonitor.start(String.format("Completed %d%% of restore\n", 0));
        progressMonitor.setNote("Please Stand By");
        tileServer.adviseOnNetworkInterfaceAvailability();
    }
    
    private void networkInterfaceOnline(PropertyChangeEvent event) {
    	tileServer.adviseOnTileServerAvailability();
    }
    
    private void tileServerAvailable(PropertyChangeEvent event) {
    	
    }

    private void cancelProgressMonitor(PropertyChangeEvent event) {
    	tileCache.cancel(true);
    	tileCacheReady(event);
    }
    
    private void tileCacheReady(PropertyChangeEvent event) {
    	progressMonitor.setProgress(100);
		retrieveMapTiles(mapPosition, zoom);
    }

    private void tileCacheProgress(PropertyChangeEvent event) {
    	int progress = (Integer) event.getNewValue();
    	progressMonitor.setProgress(String.format("Completed %d%% of restore\n", progress), progress);
    }

    private void configurePanel() {
        grid = new Grid(getSize());
        gpsDot = new Dot(getSize()); 
        gpsArrow = new Arrow(getSize());
        targetRing = new Ring(getSize());

        compIndex.add("GPS_SYMBOL");
        add(gpsDot, 0);
        
        compIndex.add("GPS_ARROW");
        add(gpsArrow, 1);
        
        compIndex.add("SAMPLE_GRID");
		add(grid, 2);
	
		compIndex.add("TARGET_RING");
		add(targetRing, 3);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
    }
    
    private void paintTile(ImageTile it) {
		tilesReturned++;
		timg = it.image;
		dx = it.imageLocation.x;
		dy = it.imageLocation.y;
		tw = it.tileSize.width;
		th = it.tileSize.height;
		full = false;
		image.getGraphics().drawImage(it.image, dx - imageLoc.x, dy - imageLoc.y, tw, th, this);
		paintImmediately(dx, dy, tw, th);
		if (tilesReturned == imageRequests.size()) {
			tilesReturned = 0;
			tx = 0;
			ty = 0;
			full = true;
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			fullImageRendered = true;
			redimensionMap(mapPosition, zoom, getSize());
			displayShapes(true);
			imageRequests.subList(0, imageRequests.size()).clear();
			if (oldZoom != zoom) {
				oldZoom = zoom;
				zooming = false;
				firePropertyChange(ZOOM_COMPLETE, null, null);
			}
			firePropertyChange(MAP_IMAGE_COMPLETE, null, null);
		}
    }

    @Override
	protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Graphics2D g2 = (Graphics2D) g.create();
    	try {
    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		if (showMapImage && imageSize != null && imageLoc != null && full) {
    			g2.translate(-tx, -ty);
    			g2.drawImage(image, imageLoc.x, imageLoc.y, imageSize.width, imageSize.height, this);
    		}
    		if (showMapImage && tw > 0 && th > 0 && !full) {
    			g2.drawImage(timg, dx, dy, tw, th, this);
    		}
    	} finally {
    		g2.dispose();
    	}
    }
    
    @Override
    public void setGridColor(Color color) {
    	grid.setColor(color);
    	repaint();
    }

    private void setMapPosition(int x, int y) {
        if (this.mapPosition.x == x && this.mapPosition.y == y) return;
    	mapPosition.x = x;
        mapPosition.y = y;
    }

    private void redimensionMap(Point mapPosition, int zoom, Dimension frameSize) {
    	Point.Double tempUpperLeftLonLat = positionToLonLat(mapPosition, zoom);     	
    	Point.Double tempLowerRightLonLat = positionToLonLat(new Point(mapPosition.x + frameSize.width, 
    			mapPosition.y + frameSize.height), zoom);

    	if (tempUpperLeftLonLat == null || tempLowerRightLonLat == null) return;

    	upperLeftLonLat = tempUpperLeftLonLat;
    	lowerRightLonLat = tempLowerRightLonLat;
    	
    	grid.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	gpsDot.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	gpsArrow.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	targetRing.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);

    	for (Quad tempQuad : quadList) {
    		tempQuad.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    	
    	for (Dot tempDot : dotList) {
    		tempDot.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    	
    	for (Dot tempIntersect : arcIntersectList) {
    		tempIntersect.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    	
    	for (Ring tempRing : ringList) {
    		tempRing.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    	
    	for (Icon tempIcon : iconList) {
    		tempIcon.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    	
    	for (Line tempLine : lineList) {
    		tempLine.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    	
    	for (HyperbolicProjection tempArc : arcList) {
    		tempArc.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    }

    @Override
    public void displayShapes(boolean displayShapes) {
    	if (this.displayShapes == displayShapes) return;
    	this.displayShapes = displayShapes;
    	grid.setVisible(showGrid && displayShapes);
    	gpsDot.setVisible(displayShapes && showGpsSymbol);
    	gpsArrow.setVisible(displayShapes && showGpsSymbol);
    	targetRing.setVisible(displayShapes && showTargetRing);

    	for (Quad tempQuad : quadList) {
    		tempQuad.setVisible(displayShapes && showQuads);
    	}
    	
    	for (Dot tempDot : dotList) {
    		tempDot.setVisible(displayShapes && showDots);
    	}
    	
    	for (Dot tempIntersect : arcIntersectList) {
    		tempIntersect.setVisible(displayShapes && showArcIntersectPoints);
    	}
    	
    	for (Ring tempRing : ringList) {
    		tempRing.setVisible(displayShapes && showRings);
    	}
    	
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
    		tempArc.showTrail(displayShapes && showArcTrails);
    	}
    }

    @Override
    public void setZoom(int zoom) {
        if (zoom >= 1 && zoom <= tileServer.getMaxZoom()) this.zoom = zoom;
    }

    @Override
    public int getMaxZoom() {
    	return tileServer.getMaxZoom();
    }
    
    private void zoomIn(Point pivot) {
    	if (zooming) return;
    	if (zoom >= tileServer.getMaxZoom()) {
    		zoom = tileServer.getMaxZoom();
    		zooming = false;
    		firePropertyChange(ZOOM_COMPLETE, null, zoom);
    		return;
    	}
    	int dx = pivot.x;
        int dy = pivot.y;
        zoom++;
        displayShapes(false);
        setMapPosition(mapPosition.x * 2 + dx, mapPosition.y * 2 + dy);
        retrieveMapTiles(mapPosition, zoom);
        redimensionMap(mapPosition, zoom, getSize());
    }

    private void zoomOut(Point pivot) {
    	if (zooming) return;
    	if (zoom <= 1) {
    		zoom = 1;
    		zooming = false;
    		firePropertyChange(ZOOM_COMPLETE, null, zoom);
    		return;
    	}
    	int dx = pivot.x;
        int dy = pivot.y;
        zoom--;
        displayShapes(false);
        setMapPosition((mapPosition.x - dx) / 2, (mapPosition.y - dy) / 2);
        retrieveMapTiles(mapPosition, zoom);
        redimensionMap(mapPosition, zoom, getSize());
    }
    
    @Override
    public void zoomIn() {
    	zoomIn(new Point(getSize().width / 2, getSize().height / 2));
    }
    
    @Override
    public void zoomOut() {
    	zoomOut(new Point(getSize().width / 2, getSize().height / 2));
    }

    public Point getCursorPosition() {
        return new Point(mapPosition.x + mouseCoords.x, mapPosition.y + mouseCoords.y);
    }

    public Point getCursorPosition(Point panelPoint) {
        return new Point(mapPosition.x + panelPoint.x, mapPosition.y + panelPoint.y);
    }
    
    public int getCursorMapHorizontal(int x) {
        return mapPosition.x + x;
    }
    
    public int getCursorMapVertical(int y) {
        return mapPosition.y + y;
    }
    
    public static double getN(int y, int z) {
        return Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    }

    @Override
    public Point.Double getCenterLonLat() {
    	return positionToLonLat(getCenterMapPosition(), zoom);
    }

    public Point getCenterMapPosition() {
    	return new Point(mapPosition.x + (getSize().width / 2), mapPosition.y + (getSize().height / 2));
    }
    
    public void setCenterPosition(Point point) {
        setMapPosition(point.x - (getSize().width / 2), point.y - (getSize().height / 2));
    }

    @Override
    public void setCenterLonLat(Point.Double point) {
    	Point position = lonlatToMapPosition(point, zoom, tileSize);
    	setMapPosition(position.x - (getSize().width / 2), position.y - (getSize().height / 2));
    }

    private Point.Double positionToLonLat(Point point, int zoom) {
        double maxX = tileSize.width * (1 << zoom);
        double maxY = tileSize.height * (1 << zoom);
        double lon = point.x / maxX * 360.0 - 180;
        double lat = Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * point.y) / maxY)));
        if (lon >= -180 && lon <= 180 && lat >= -90 && lat <= 90) return new Point.Double(lon,lat);
        return null;
    }

    private static Point centerLonLatToMapPosition(Point.Double point, int zoom, Dimension frameSize, Dimension tileSize) {
    	Point cp = lonlatToMapPosition(point, zoom, tileSize);
    	int ulx = cp.x - (frameSize.width / 2);
    	int uly = cp.y - (frameSize.height / 2);
    	return new Point(ulx, uly);
    }

    private static Point lonlatToMapPosition(Point.Double point, int zoom, Dimension tileSize) {
        double maxX = tileSize.width * (1 << zoom);
        double maxY = tileSize.height * (1 << zoom);
        return new Point((int) Math.floor((point.x + 180) / 360 * maxX),
        		(int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(point.y)) + 
        				1 / Math.cos(Math.toRadians(point.y))) / Math.PI) / 2 * maxY));
    }

    @Override
    public Point.Double getPixelResolutionInDegrees() {
    	if (upperLeftLonLat == null || lowerRightLonLat == null || getWidth() == 0 || getHeight() == 0) return null;
    	double longitudeDegrees = Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
    	double latitudeDegrees = Math.abs(upperLeftLonLat.y - lowerRightLonLat.y);
    	return new Point.Double(longitudeDegrees / getWidth(), latitudeDegrees / getHeight());
    }
    
    @Override
	public Point getMousePosition() {
		return mouseCoords;
	}
    
    @Override
	public Point.Double getMouseCoordinates() {
		return mouseLonLat;
	}

	@Override
	public void setGridSize(Point.Double gridSize) {
		if (upperLeftLonLat == null || lowerRightLonLat == null || gridSize ==  null) return;
		grid.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
		grid.setGridSize(gridSize);
		repaint();
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
		if (upperLeftLonLat == null || lowerRightLonLat == null) return 0;
		return upperLeftLonLat.x;
	}
	
	@Override
	public double getMapRightEdgeLongitude() {
		if (upperLeftLonLat == null || lowerRightLonLat == null) return 0;
		return lowerRightLonLat.x;
	}
	
	@Override
	public double getMapTopEdgeLatitude() {
		if (upperLeftLonLat == null || lowerRightLonLat == null) return 0;
		return upperLeftLonLat.y;
	}
	
	@Override
	public double getMapBottomEdgeLatitude() {
		if (upperLeftLonLat == null || lowerRightLonLat == null) return 0;
		return lowerRightLonLat.y;
	}

	@Override
	public void deleteAllDots() {
		dotList.subList(0, dotList.size()).clear();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("DOT")) compIndex.remove(i);
		}
	}
	
	@Override
	public void deleteAllArcIntersectPoints() {
		arcIntersectList.subList(0, arcIntersectList.size()).clear();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("ARC_INTERSECT")) compIndex.remove(i);
		}
	}
	
	@Override
	public void deleteAllRings() {
		ringList.subList(0, ringList.size()).clear();
		for (int i = 0; i < compIndex.size(); i++) {
			if (compIndex.get(i).contains("RING")) compIndex.remove(i);
		}
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
		this.showGpsSymbol = showGpsSymbol;
		gpsDot.setVisible(showGpsSymbol && displayShapes);
		gpsArrow.setVisible(showGpsSymbol && displayShapes);
	}
	
	@Override
	public void setGpsSymbolColor(Color color) {
		gpsDot.setColor(color);
		gpsArrow.setColor(color);
		repaint();
	}
	
	@Override
	public void setGpsSymbol(Point.Double point, double diameter, Color color, int angle) {
		if (angle == 360) {
			gpsDot.setVisible(true && displayShapes);
			gpsDot.setLocation(point);
			gpsDot.setDiameter(diameter);
			gpsDot.setColor(color);
			gpsArrow.setVisible(false);
		} else {
			gpsArrow.setVisible(true && displayShapes);
			gpsArrow.setLocation(point, angle);
			gpsArrow.setArrowSize(diameter);
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
	public void setGpsDotPosition(Point.Double point) {
		gpsDot.setLocation(point);
		repaint();
	}
	
	@Override
	public void setGpsDotDiameter(double diameter) {
		gpsDot.setDiameter(diameter);
		repaint();
	}
	
	@Override
	public void setGpsDotColor(Color color) {
		gpsDot.setColor(color);
		repaint();
	}
	
	@Override
	public void setGpsArrowPosition(Point.Double point, int angle) {
		gpsArrow.setLocation(point, angle);
		repaint();
	}
	
	@Override
	public void setGpsArrowSize(double size) {
		gpsArrow.setArrowSize(size);
		repaint();
	}
	
	@Override
	public void setGpsArrowColor(Color color) {
		gpsArrow.setColor(color);
		repaint();
	}

	@Override
	public void setTargetRingPosition(Point.Double point) {
		targetRing.setLocation(point);
		repaint();
	}
	
	@Override
	public void setTargetRingDiameter(double diameter) {
		targetRing.setDiameter(diameter);
		repaint();
	}
	
	@Override
	public void setTargetRingColor(Color color) {
		targetRing.setColor(color);
		repaint();
	}

	@Override
	public void setTargetRing(Point.Double point, double diameter) {
		targetRing.setLocation(point);
		targetRing.setDiameter(diameter);
		repaint();
	}
	
	@Override
	public void setTargetRing(Point.Double point, double diameter, Color color) {
		targetRing.setLocation(point);
		targetRing.setDiameter(diameter);
		targetRing.setColor(color);
		repaint();
	}
	
	@Override
	public void addDot(Point.Double p, double size, Color color) {
		if (p == null || upperLeftLonLat == null || lowerRightLonLat == null || size == 0 || color == null) return;
		Dot dot = new Dot(upperLeftLonLat, lowerRightLonLat, p, size, getSize(), color);
		dotList.add(dot);
		compIndex.add("DOT " + Integer.toString(dotList.size() - 1));
		add(dot, compIndex.size() - 1);
		repaint();
	}

	@Override
	public void addArcIntersectPoint(Point.Double p, double size, Color color) {
		if (p == null || upperLeftLonLat == null || lowerRightLonLat == null || size == 0 || color == null) return;
		Dot intersectPoint = new Dot(upperLeftLonLat, lowerRightLonLat, p, size, getSize(), color);
		arcIntersectList.add(intersectPoint);
		compIndex.add("ARC_INTERSECT " + Integer.toString(arcIntersectList.size() - 1));
		add(intersectPoint, compIndex.size() - 1);
		repaint();
	}
	
	@Override
	public void setArcCursorDiameter(int arcCursorDiameter) {
		for (int i = 0; i < arcList.size(); i++) {
    		arcList.get(i).setCursorDiameter(arcCursorDiameter);
    	}
		this.arcCursorDiameter = arcCursorDiameter;
	}
	
	@Override
	public void removeDot(int index) {
		try {
			dotList.remove(index);
			remove(compIndex.indexOf("DOT " + Integer.toString(index)));
			compIndex.remove("DOT " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void removeArcIntersectPoint(int index) {
		try {
			arcIntersectList.remove(index);
			remove(compIndex.indexOf("ARC_INTERSECT " + Integer.toString(index)));
			compIndex.remove("ARC_INTERSECT " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void addLine(Point.Double p1, double angle, Color color) {
		if (p1 == null || upperLeftLonLat == null || lowerRightLonLat == null || Math.abs(angle) >= 360 || color == null) return;
		Line line = new Line(upperLeftLonLat, lowerRightLonLat, p1, angle, getSize(), color);
		lineList.add(line);
		compIndex.add("LINE " + Integer.toString(lineList.size() - 1));
		add(line, compIndex.size() - 1);
	}
	
	@Override
	public void addLine(Point.Double p1, Point.Double p2, Color color) {
		if (p1 == null || p2 == null || upperLeftLonLat == null || lowerRightLonLat == null || color == null) return;
		Line line = new Line(upperLeftLonLat, lowerRightLonLat, p1, p2, getSize(), color);
		lineList.add(line);
		compIndex.add("LINE " + Integer.toString(lineList.size() - 1));
		add(line, compIndex.size() - 1);
	}

	@Override
	public void removeLine(int index) {
		try {
			lineList.remove(index);
			remove(compIndex.indexOf("LINE " + Integer.toString(index)));
			compIndex.remove("LINE " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addArc(StaticMeasurement sma, StaticMeasurement smb, double minCatt, double maxCatt, int flight) {
		if (sma == null || smb == null || upperLeftLonLat == null || lowerRightLonLat == null || maxCatt <= 0 || flight < 0) return;
		try {
			Color tempTrailColor;
			if (trailEqualsFlightColor) {
				tempTrailColor = arcColors[flight];
			} else {
				tempTrailColor = arcTrailColor;
			}
			slp = new HyperbolicProjection(upperLeftLonLat, lowerRightLonLat, sma, smb, getSize(), arcColors[flight],
					showArcs, -1, arcAsymptoteColor, showArcAsymptotes, arcCursorDiameter, arcCursorColor, showArcCursors, 
					tempTrailColor, showArcTrails, flight);
			if (slp.getConicAngleToTarget() <= maxCatt && slp.getConicAngleToTarget() >= minCatt) {
				arcPointList = slp.getArcPointList();
			    hyperbola = slp.getHyperbola();
				arcList.add(slp);
				compIndex.add("ARC " + Integer.toString(arcList.size() - 1));
				add(slp, compIndex.size() - 1);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setArcAsymptoteColor(Color arcAsymptoteColor) {
		for (int i = 0; i < arcList.size(); i++) {
    		arcList.get(i).setAsymptoteColor(arcAsymptoteColor);
    	}
		this.arcAsymptoteColor = arcAsymptoteColor;
	}
    
	@Override
    public void setArcColors(Color[] arcColors) {
    	for (int i = 0; i < arcColors.length; i++) {
    		for (int a = 0; a < arcList.size(); a++) {
    			if (arcList.get(a).getFlight() == i) {
    				arcList.get(a).setArcColor(arcColors[i]);
    			}
    		}
    	}
    	this.arcColors = arcColors;
    }
    
	@Override
    public void setTrailEqualsFlightColor(boolean trailEqualsFlightColor) {
    	for (int i = 0; i < arcColors.length; i++) {
    		for (int a = 0; a < arcList.size(); a++) {
    			if (arcList.get(a).getFlight() == i) {
    				if (trailEqualsFlightColor) {
    					arcList.get(a).setTrailColor(arcColors[i]);
    				} else {
    					arcList.get(a).setTrailColor(arcTrailColor);
    				}
    			}
    		}
    	}
    	this.trailEqualsFlightColor = trailEqualsFlightColor;
    }
	
	@Override
	public void setArcCursorColor(Color arcCursorColor) {
		for (int i = 0; i < arcList.size(); i++) {
    		arcList.get(i).setCursorColor(arcCursorColor);
    	}
		this.arcCursorColor = arcCursorColor;
	}
	
	@Override
	public void setArcTrailColor(Color arcTrailColor) {
		for (int i = 0; i < arcList.size(); i++) {
    		arcList.get(i).setTrailColor(arcTrailColor);
    	}
		this.arcTrailColor = arcTrailColor;
	}
	
    @Override
    public void setArcIntersectPointColor(Color arcIntersectPointColor) {
    	for (Dot tempIntersect : arcIntersectList) {
    		tempIntersect.setColor(arcIntersectPointColor);
    	}
    }
    
	@Override
	public double getConicAngleToTarget() {
		return slp.getConicAngleToTarget();
	}
	
	@Override
	public List<Point.Double> getArcPointList() {
		return arcPointList;
	}
	
	@Override
	public Hyperbola2D getHyperbola() {
		return hyperbola;
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
		if (point == null || upperLeftLonLat == null || lowerRightLonLat == null) return;
		Icon icon = new Icon(upperLeftLonLat, lowerRightLonLat, point, iconPath, identifier, getSize());
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
	public void removeIcon(int index) {
		try {
			iconList.remove(index);
			remove(compIndex.indexOf("ICON " + Integer.toString(index)));
			compIndex.remove("ICON " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void addRing(Point.Double point, double size, Color color) {
		Ring ring = new Ring(upperLeftLonLat, lowerRightLonLat, point, size, getSize(), color);
		ringList.add(ring);
		compIndex.add("RING " + Integer.toString(ringList.size() - 1));
		add(ring, compIndex.size() - 1);
	}

	@Override
	public void removeRing(int index) {
		try {
			ringList.remove(index);
			remove(compIndex.indexOf("RING " + Integer.toString(index)));
			compIndex.remove("RING " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void insertIcon(int index, Point.Double point, String iconPath, String identifier) {
		try {
			Icon icon = new Icon(upperLeftLonLat, lowerRightLonLat, point, iconPath, identifier, getSize());
			iconList.add(index, icon);
			compIndex.add("ICON " + Integer.toString(iconList.size() - 1));
			add(icon, compIndex.size() - 1);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
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
		Quad quad = new Quad(upperLeftLonLat, lowerRightLonLat, point, size, getSize(), color);
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
	public void removeQuad(int index) {
		try {
			quadList.remove(index);
			remove(compIndex.indexOf("QUAD " + Integer.toString(index)));
			compIndex.remove("QUAD " + Integer.toString(index));
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public int numberOfIcons() {
		return iconList.size();
	}

	@Override
	public void showArcs(boolean show) {
		for (int i = 0; i < arcList.size(); i++) {
			arcList.get(i).showArc(show);
		}
		showArcs = show; 
	}
	
	@Override
	public void showArcTrails(boolean show) {
		for (int i = 0; i < arcList.size(); i++) {
			arcList.get(i).showTrail(show);
		}
		showArcTrails = show; 
	}

	@Override
	public void showArcAsymptotes(boolean show) {
		for (int i = 0; i < arcList.size(); i++) {
			arcList.get(i).showAsymptote(show);
		}
		showArcAsymptotes = show; 
	}
	
	@Override
	public void showArcCursors(boolean show) {
		for (int i = 0; i < arcList.size(); i++) {
			arcList.get(i).showCursor(show);
		}
		showArcCursors = show; 
	}
	
	@Override
	public void showQuads(boolean show) {
		for (int i = 0; i < quadList.size(); i++) {
			quadList.get(i).setVisible(show && displayShapes);
		}
		showQuads = show;
	}

	@Override
	public void showDots(boolean show) {
		for (int i = 0; i < dotList.size(); i++) {
			dotList.get(i).setVisible(show && displayShapes);
		}
		showDots = show;
	}

	@Override
	public void showArcIntersectPoints(boolean show) {
		for (int i = 0; i < arcIntersectList.size(); i++) {
			arcIntersectList.get(i).setVisible(show && displayShapes);
		}
		showArcIntersectPoints = show;
	}
	
	@Override
	public void showIconLabels(boolean show) {
		for (int i = 0; i < iconList.size(); i++) {
			iconList.get(i).showIconLabel(show && displayShapes);
		}
		showIconLabels = show;
	}
	
	@Override
	public void showLines(boolean show) {
		for (int i = 0; i < lineList.size(); i++) {
			lineList.get(i).setVisible(show && displayShapes);
		}
		showLines = show;
	}

	@Override
	public void showRings(boolean show) {
		for (int i = 0; i < ringList.size(); i++) {
			ringList.get(i).setVisible(show && displayShapes);
		}
		showRings = show;
	}
	
	@Override
	public void showIcons(boolean show) {
		for (int i = 0; i < iconList.size(); i++) {
			iconList.get(i).setVisible(show && displayShapes);
		}
		showIcons = show;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
	    OpenStreetMapPanel clone = (OpenStreetMapPanel)super.clone();
	    return clone;
	}
	
	@Override
	public BufferedImage getScreenShot() {
		Dimension initialSize = getSize();
		setSize(DEFAULT_PRINTER_PAGE_SIZE);
		BufferedImage image = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		paint(image.createGraphics());
		setSize(initialSize);
		return image;
	}

	@Override
	public void setScale(double scale) {
		if (zoom - 3.0 != scale) {
			zoom = (int) (scale + 3.0);
			redimensionMap(mapPosition, zoom, getSize());
			retrieveMapTiles(mapPosition, zoom);
		}
	}

	@Override
	public double getScale() {
		return zoom - 3;
	}

	@Override
	public int getZoom() {
		return zoom;
	}

    public TileServer.Server getTileServerAvailability() {
        return tileServer.getServerAvailability();
    }

    public TileServer.Interface getNetworkInterfaceAvailability() {
        return tileServer.getNetworkInterfaceAvailability();
    }
    
    public TileServer getTileTileServer() {
        return tileServer;
    }

    public void setTileTileServer(TileServer tileServer) {
    	if(this.tileServer == tileServer) return;
    	this.tileServer = tileServer;
    	while (zoom > tileServer.getMaxZoom()) zoomOut();
    } 
    
    public Point getMapPosition() {
    	return mapPosition;
    }

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mouseCoords = e.getPoint();
		int rotation = e.getWheelRotation();
        if (rotation < 0) {
        	zoomIn();
        } else {
        	zoomOut();
        }
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!fullImageRendered) return;
    	dragging = true;
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		handlePosition(e);
		handleDrag(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		handlePosition(e);
	}

	private void handlePosition(MouseEvent e) {
        mouseCoords = e.getPoint();
        mouseLonLat = positionToLonLat(getCursorPosition(), zoom); 
    }

    private void handleDrag(MouseEvent e) {
        if (downCoords != null) {
            tx = downCoords.x - e.getX();
            ty = downCoords.y - e.getY();
            mapPosition = new Point(downPosition.x + tx, downPosition.y + ty);
            redimensionMap(mapPosition, zoom, getSize());
            repaint();
        }
    }
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
			mouseCoords = e.getPoint();
            zoomOut();
        } 
		else if (e.getButton() == MouseEvent.BUTTON2) {
			mouseCoords = e.getPoint();
            setCenterPosition(getCursorPosition());
            repaint();
        }
		if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() >= 2) {
			mouseCoords = e.getPoint();
        	zoomIn();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && !dragging) {
			downCoords = e.getPoint();
	        downPosition = (Point) mapPosition.clone();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		handleDrag(e);
        downCoords = null;
        downPosition = null;
        retrieveMapTiles(mapPosition, zoom);
        redimensionMap(mapPosition, zoom, getSize());
        dragging = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (fullImageRendered) {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
	
	private Point[] selectTiles(Point mapPosition, Dimension windowSize, Dimension tileSize) {
    	Point[] pt = new Point[2];
    	int x0 = (int) Math.floor(((double) mapPosition.x) / tileSize.width);
    	int y0 = (int) Math.floor(((double) mapPosition.y) / tileSize.height);
        int x1 = (int) Math.ceil(((double) mapPosition.x + windowSize.width) / tileSize.width);
        int y1 = (int) Math.ceil(((double) mapPosition.y + windowSize.height) / tileSize.height);
        pt[0] = new Point(x0,y0);
        pt[1] = new Point(x1,y1);
        return pt;
    }
	
	private Dimension imageSize(Point[] tileArray) {
		return new Dimension((tileArray[1].x - tileArray[0].x) * tileSize.width, 
				(tileArray[1].y - tileArray[0].y) * tileSize.height);
	}

    private void retrieveMapTiles(Point mapPosition, int zoom) {
    	fullImageRendered = false;
        Point[] tileArray = selectTiles(mapPosition, getSize(), tileSize);
        imageSize = imageSize(tileArray);
        image = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_RGB);
    	int xTileCount = 1 << zoom;
        int yTileCount = 1 << zoom;
        int x0 = tileArray[0].x;
        int y0 = tileArray[0].y;
        int x1 = tileArray[1].x;
        int y1 = tileArray[1].y;
        int dy = y0 * tileSize.height - mapPosition.y;
        imageLoc = new Point(x0 * tileSize.width - mapPosition.x, y0 * tileSize.height - mapPosition.y);
        for (int y = y0; y < y1; ++y) { 
        	int dx = x0 * tileSize.width - mapPosition.x;
            for (int x = x0; x < x1; ++x) {        	
            	if (x >= 0 && x < xTileCount && y >= 0 && y < yTileCount) {
            		try {
						String url = TileServer.getTileString(tileServer, x, y, zoom);
						ImageRequest irq = new ImageRequest(new URL(url), tileCache, tileServer, zoom, new Point(x,y), 
							mapPosition, new Point(dx,dy), tileSize);
						imageRequests.add(irq);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
                }
            	dx += tileSize.width;
            }
            dy += tileSize.height;
        }
        issueImageRequests();
    }

    private void issueImageRequests() {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    	Iterator<ImageRequest> itr = imageRequests.iterator();
    	while (itr.hasNext()) {
    		ImageRequest irq = itr.next();
			ImageRetriever worker = new ImageRetriever(irq.url, irq.tileCache, irq.tileServer, irq.zoom, irq.fileIndex, 
				irq.mapPosition, irq.imageLocation, irq.tileSize, false);
			worker.addPropertyChangeListener(new PropertyChangeListener() {
            	@Override
            	public void propertyChange(final PropertyChangeEvent event) {
    	            if (ImageRetriever.TILE_COMPLETE.equals(event.getPropertyName())) {
    	            	addPaintEvent((ImageTile) event.getNewValue());
    	            }
    	            if (ImageRetriever.WAITING.equals(event.getPropertyName())) {
    	            	
    	            }
    	            if (ImageRetriever.BLANK.equals(event.getPropertyName())) {

    	            }
            	}
            });
			executor.execute(worker);
		}
		executor.shutdown();
    }
	
    private void addPaintEvent(ImageTile it) {
    	paintEvents.add(it);
    	if (paintEvents.size() == 1) {
    		paintTimer.start();
    	}
    }

    private void paintJob(ActionEvent e) {
    	ImageTile it;
    	if ((it = paintEvents.poll()) != null) {
    		paintTile(it);
    	} else {
    		paintTimer.stop();
    	}
    }
    
    public class ImageRequest {
    	public URL url;
    	public TileCache tileCache;
    	public TileServer tileServer;
    	public Point fileIndex;
    	public Point mapPosition;
    	public Point imageLocation;
    	public Dimension tileSize;
    	public int zoom;
    	public ImageRequest(URL url, TileCache tileCache, TileServer tileServer, int zoom, 
    			Point fileIndex, Point mapPosition, Point imageLocation, Dimension tileSize) {
    		this.url = url;
    		this.tileCache = tileCache;
    		this.tileServer = tileServer;
    		this.zoom = zoom;
    		this.fileIndex = fileIndex;
    		this.mapPosition = mapPosition;
    		this.imageLocation = imageLocation;
    		this.tileSize = tileSize;
    	}
    }
    
    public static String getTileNumber(final double lat, final double lon, final int zoom) {
    	   int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
    	   int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
    	    if (xtile < 0)
    	     xtile=0;
    	    if (xtile >= (1<<zoom))
    	     xtile=((1<<zoom)-1);
    	    if (ytile < 0)
    	     ytile=0;
    	    if (ytile >= (1<<zoom))
    	     ytile=((1<<zoom)-1);
    	    return("" + zoom + "/" + xtile + "/" + ytile);
    	   }
    	 }
}
