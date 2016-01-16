package jdrivetrack;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceQuad;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import interfaces.JMapViewerEventListener;
import interfaces.MapInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.media.opengl.GL;
import javax.media.opengl.GLProfile;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class WorldWindMap extends JPanel implements MapInterface, Cloneable {
	private static final long serialVersionUID = -3729676026605579052L;

	private static final double DEFAULT_ZOOM = 6d;
	private static final double DEFAULT_ARC_INTERSECT_POINT_DIAMETER = 3d;
	private static final double DEFAULT_ARC_TRACE_DIAMETER = 3d;
	private static final double DEFAULT_ARC_CURSOR_DIAMETER = 3d;
	private static final double DEFAULT_SIGNAL_MARKER_DIAMETER = 3d;
	private static final Color DEFAULT_ARC_INTERSECT_POINT_COLOR = Color.RED;
	private static final Color DEFAULT_TEST_GRID_COLOR = Color.RED;
	private static final Color DEFAULT_ARC_TRACE_COLOR = Color.ORANGE;
	private static final Color DEFAULT_ARC_CURSOR_COLOR = Color.GREEN;
	private static final Color DEFAULT_ARC_ASYMPTOTE_COLOR = Color.CYAN;
	private static final boolean DEFAULT_TRACE_COLOR_MODE = true;
	private static final Dimension DEFAULT_MAP_SIZE = new Dimension(800, 600);
	private static final double ZOOM_LEVEL_CONVERSION_FACTOR = 4513.988880;
	private static final double ARC_LENGTH = 1d;

	private BasicOrbitView view;
	private Model model;
	private Point.Double gridSize;
	
	private Point.Double downPosition;
	private Point.Double startupLonLat;
	private double zoom;
	private Dimension canvasSize;
	private boolean showStatusBar;
	
	private Object renderingHold = new Object();
	private boolean showPolygons = false;
	private boolean showQuads = false;
	private boolean showSignalMarkers = false;
	private boolean showLines = false;
	private boolean showRings = false;
	private boolean showIcons = false;
	private boolean showGrid = false;
	private boolean showArcs = false;
	private boolean showArcTraces = false;
	private boolean showArcIntersectPoints = false;
	private boolean showArcAsymptotes = false;
	private boolean showArcCursors = false;
	private boolean showIconLabels = false;
	private boolean showGpsSymbol = false;
	private boolean showTargetRing = false;
	
	private boolean traceEqualsFlightColor = DEFAULT_TRACE_COLOR_MODE;
	
	private double arcCursorDiameter = DEFAULT_ARC_CURSOR_DIAMETER;
	private double arcTraceDiameter = DEFAULT_ARC_TRACE_DIAMETER;
	private double arcIntersectPointDiameter = DEFAULT_ARC_INTERSECT_POINT_DIAMETER;
	
	private Color arcTraceColor = DEFAULT_ARC_TRACE_COLOR;
	private Color arcCursorColor = DEFAULT_ARC_CURSOR_COLOR;
	private Color arcAsymptoteColor = DEFAULT_ARC_ASYMPTOTE_COLOR;
	private Color arcIntersectPointColor = DEFAULT_ARC_INTERSECT_POINT_COLOR;
	private Color gridColor = DEFAULT_TEST_GRID_COLOR;
	private Marker gpsArrow;
	private Marker gpsDot;
	private SurfaceCircle targetRingShape;

	private BasicMarkerAttributes gpsDotBma = new BasicMarkerAttributes();
	private BasicMarkerAttributes gpsArrowBma = new BasicMarkerAttributes();
	private BasicMarkerAttributes arcCursorBma = new BasicMarkerAttributes();
	private BasicShapeAttributes arcAsymptoteBsa = new BasicShapeAttributes();
	private BasicMarkerAttributes arcIntersectBma = new BasicMarkerAttributes();
	
	private List<UserFacingIcon> iconList;
	private List<SurfaceQuad> quadList;
	private List<SurfacePolyline> lineList;
	private List<SurfaceCircle> ringList;
	private List<ConicSection> arcList;
	private List<SurfacePolygon> polygonList;
	private List<Marker> intersectMarkers;
	private List<Marker> traceDotMarkers;
	private List<Marker> cursorMarkers;
	private List<Marker> signalMarkers;
	
	private ViewControlsLayer viewControlsLayer;
	private IconLayer iconLayer;
	
	private MarkerLayer gpsArrowLayer;
	private MarkerLayer gpsDotLayer;
	private MarkerLayer intersectLayer;
	private MarkerLayer cursorLayer;
	private MarkerLayer traceDotLayer;
	private MarkerLayer signalMarkerLayer;
	
	private TestGridLayer testGridLayer;
	
	private RenderableLayer targetRingLayer;
	private RenderableLayer lineLayer;
	private RenderableLayer ringLayer;
	private RenderableLayer quadLayer;
	private RenderableLayer arcLayer;
	private RenderableLayer asymptoteLayer;
	private RenderableLayer traceLineLayer;
	private RenderableLayer polygonLayer;

	private ScheduledFuture<?> gridRedrawHandle = null;
    private ScheduledExecutorService gridRedrawScheduler;
	
	private Color[] arcColors;
	private double gpsDotDiameter;
	private WorldWindow wwd;
	private boolean wwdRendered;
	private StatusBar statusBar;
	private BulkDownloadDialog bdd; 
	private Preferences userPref;
	private int gpsAngle;
	private boolean isZooming = false;
	private boolean animationRedrawTimerActive = false;
	
	public WorldWindMap() {
		this(new Point.Double(35.0, -86.0), DEFAULT_ZOOM, DEFAULT_MAP_SIZE, true);
	}

	public WorldWindMap(Point.Double startupLonLat) {
		this(startupLonLat, DEFAULT_ZOOM, DEFAULT_MAP_SIZE, false);
	}

	public WorldWindMap(Point.Double startupLonLat, double zoom) {
		this(startupLonLat, zoom, DEFAULT_MAP_SIZE, false);
	}

	public WorldWindMap(Point.Double startupLonLat, double zoom, Dimension canvasSize, boolean showStatusBar) {
		this.startupLonLat = startupLonLat;
		this.zoom = zoom;
		this.canvasSize = canvasSize;
		this.showStatusBar = showStatusBar;
		
		if (zoom < 1 || zoom > 18) this.zoom = 12;
		
		initializeVariables();
		configurePanel();
		configureListeners();
		createWorldWindMap();
		configureComponents();
		insertMapLayers();
	}
	
	private void initializeVariables() {
		wwd = new WorldWindowGLCanvas();
		userPref = Preferences.userRoot().node(this.getClass().getName());
		view = new BasicOrbitView();
		viewControlsLayer = new ViewControlsLayer();
		statusBar = new StatusBar();
		wwdRendered = false;
		gpsAngle = 360;
		testGridLayer = new TestGridLayer();
		targetRingLayer = new RenderableLayer();
		lineLayer = new RenderableLayer();
		ringLayer = new RenderableLayer();
		quadLayer = new RenderableLayer();
		arcLayer = new RenderableLayer();
		asymptoteLayer = new RenderableLayer();
		traceLineLayer = new RenderableLayer();
		polygonLayer = new RenderableLayer();
		gpsDotBma = new BasicMarkerAttributes();
		gpsArrowBma = new BasicMarkerAttributes();
		arcCursorBma = new BasicMarkerAttributes();
		arcAsymptoteBsa = new BasicShapeAttributes();
		arcIntersectBma = new BasicMarkerAttributes();
		iconList = new ArrayList<UserFacingIcon>(1024);
		quadList = new ArrayList<SurfaceQuad>(1024);
		lineList = new ArrayList<SurfacePolyline>(1024);
		ringList = new ArrayList<SurfaceCircle>(1024);
		arcList = new ArrayList<ConicSection>(1024);
		polygonList = new ArrayList<SurfacePolygon>(1024);
		intersectMarkers = new ArrayList<Marker>(1024);
		traceDotMarkers = new ArrayList<Marker>(1024);
		cursorMarkers = new ArrayList<Marker>(1024);
		signalMarkers = new ArrayList<Marker>(1024);
		iconLayer = new IconLayer();
		gpsArrowLayer = new MarkerLayer();
		gpsDotLayer = new MarkerLayer();
		intersectLayer = new MarkerLayer();
		cursorLayer = new MarkerLayer();
		traceDotLayer = new MarkerLayer();
		signalMarkerLayer = new MarkerLayer();
		targetRingShape = new SurfaceCircle();
	}
	
	private void createWorldWindMap() {
		((Component) wwd).setPreferredSize(canvasSize);
		
		model = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
		
		view.setZoom(zoomToAltitude(zoom));
		
		view.setGlobe(new EllipsoidalGlobe(Vincenty.EQUATORIAL_RADIUS, Vincenty.POLAR_RADIUS, 
				Vincenty.FIRST_ECCENTRICITY_SQUARED, null));

		Position pos = new Position(Angle.fromDegreesLatitude(startupLonLat.y), 
				Angle.fromDegreesLongitude(startupLonLat.x), 0.0);
		
		view.setCenterPosition(pos);

		wwd.addRenderingListener(new RenderingListener() {
			@Override
			public void stageChanged(RenderingEvent event) {
				if (event.getStage() == RenderingEvent.AFTER_BUFFER_SWAP) {
					worldWindRendered(event);
				}
			}
		});

		wwd.setModel(model);
		wwd.setView(view);
		wwd.getInputHandler().setForceRedrawOnMousePressed(true);
		
		add(statusBar, BorderLayout.PAGE_END);
		add((Component) wwd, BorderLayout.CENTER);
	}
	
	private void configurePanel() {
		setLayout(null);
		setOpaque(true);
        setBackground(Color.BLACK);
        setSize(canvasSize);
        setFocusable(true);
		setFocusTraversalKeysEnabled(true);
		setDoubleBuffered(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		statusBar.setEventSource(wwd);
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		statusBar.setVisible(showStatusBar);
	}
	
	private void configureListeners() {
		wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));
		wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
		
		wwd.addSelectListener(new SelectListener() {
            @Override
			public void selected(SelectEvent event) {
                if (event.getTopObject() != null) {
                    if (event.getTopPickedObject().getParentLayer() instanceof MarkerLayer) {
                        PickedObject po = event.getTopPickedObject();
                        System.out.printf("Track position %s, %s, size = %f\n",
                            po.getValue(AVKey.PICKED_OBJECT_ID).toString(),
                            po.getPosition(), po.getValue(AVKey.PICKED_OBJECT_SIZE));
                    }
                }
            }
        });
		
		wwd.getInputHandler().addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
				processKeyEvent(event);
			}

			@Override
			public void keyReleased(KeyEvent event) {
				processKeyEvent(event);
			}

			@Override
			public void keyTyped(KeyEvent event) {
				processKeyEvent(event);
			}
		});

		wwd.getInputHandler().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent event) {
				if (view.isAnimating() && !animationRedrawTimerActive) gridRedrawAnimating(50, 50);
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					if (getMapLowerRightCorner().distance(downPosition) > Math.min(gridSize.x / 3600.0, gridSize.y / 3600.0)) {
						testGridLayer.setMapDimension(getMapDimension());
						wwd.redraw();
					}
					processMouseMotionEvent(event);
				} catch (NullPointerException ex) { }
			}
		});

		wwd.getInputHandler().addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				int rotation = event.getWheelRotation();
				if (rotation < 0) {
					zoomIn();
				} else {
					zoomOut();
				}
				processMouseWheelEvent(event);
			}
		});

		wwd.getInputHandler().addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() >= 2) {
					zoomOut();
				} else if (event.getButton() == MouseEvent.BUTTON2) {
					view.goTo(wwd.getCurrentPosition(), zoomToAltitude(getZoom()));
				}
				if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() >= 2) {
					zoomIn();
				}
				processMouseEvent(event);
			}

			@Override
			public void mouseEntered(MouseEvent event) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				processMouseEvent(event);
			}

			@Override
			public void mouseExited(MouseEvent event) {
				processMouseEvent(event);
			}

			@Override
			public void mousePressed(MouseEvent event) {
				downPosition = getMapLowerRightCorner();
				processMouseEvent(event);
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				processMouseEvent(event);
			}
		});

		wwd.addPositionListener(new PositionListener() {
			@Override
			public void moved(PositionEvent event) {
				try {
					MouseEvent me = new MouseEvent((Component) wwd,
						MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
						0, event.getScreenPoint().x, event.getScreenPoint().y, 1, false);
					processMouseMotionEvent(me);
				} catch (NullPointerException ex) {}
			}
		});

		wwd.addRenderingExceptionListener(new RenderingExceptionListener() {
			@Override
			public void exceptionThrown(Throwable t) {
				if (t instanceof WWAbsentRequirementException) {
					String message = "This computer does not meet minimum graphics requirements.\n";
					message += "Please install an up-to-date graphics driver and try again.\n";
					message += "Reason: " + t.getMessage() + "\n";
					JOptionPane.showMessageDialog(getParent(), message,
							"Unable to Run NASA WorldWind", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});

		addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent event) {
				if (event.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED) {
					boolean connected = setupListenersWhenConnected();
					if (connected) {
						removeHierarchyListener(this);
					}
				}
			}
		});
	}
	
	private void insertMapLayers() {
		insertBeforeCompass(wwd, viewControlsLayer);
		insertBeforeCompass(wwd, new MGRSGraticuleLayer());
		insertBeforePlacenames(wwd, intersectLayer);
		insertBeforePlacenames(wwd, asymptoteLayer);
		insertBeforePlacenames(wwd, targetRingLayer);
		insertBeforePlacenames(wwd, iconLayer);
		insertBeforePlacenames(wwd, lineLayer);
		insertBeforePlacenames(wwd, ringLayer);
		insertBeforePlacenames(wwd, quadLayer);
		insertBeforePlacenames(wwd, polygonLayer);
		insertBeforePlacenames(wwd, arcLayer);
		insertBeforePlacenames(wwd, cursorLayer);
		insertBeforePlacenames(wwd, traceLineLayer);
		insertBeforePlacenames(wwd, traceDotLayer);
        insertBeforePlacenames(wwd, gpsArrowLayer);
        insertBeforePlacenames(wwd, gpsDotLayer);
		insertBeforePlacenames(wwd, signalMarkerLayer);
		insertBeforePlacenames(wwd, testGridLayer);
		
		restoreLayerSelections(wwd);
		
		for (Layer layer : wwd.getModel().getLayers()) {
			if (layer instanceof SelectListener) {
				wwd.addSelectListener((SelectListener) layer);
			}
		}
		
		wwd.redraw();
	}
	
	private void configureComponents() {
		arcIntersectBma.setShapeType(BasicMarkerShape.CYLINDER);
		arcIntersectBma.setMaterial(new Material(DEFAULT_ARC_INTERSECT_POINT_COLOR));
		arcIntersectBma.setMarkerPixels(DEFAULT_ARC_INTERSECT_POINT_DIAMETER);
		arcIntersectBma.setOpacity(0.7d);
		intersectLayer.setEnabled(false);
		intersectLayer.setElevation(0);
		intersectLayer.setEnablePickSizeReturn(true);
		intersectLayer.setOverrideMarkerElevation(true);
		intersectLayer.setKeepSeparated(false);
		intersectLayer.setName(".Arc Intersect Layer");
		intersectLayer.setMarkers(intersectMarkers);
		
		BasicShapeAttributes arcAsymptoteBsa = new BasicShapeAttributes();
		arcAsymptoteBsa.setInteriorOpacity(0.0);
		arcAsymptoteBsa.setOutlineOpacity(1.0);
		arcAsymptoteBsa.setOutlineWidth(1);
		arcAsymptoteBsa.setEnableAntialiasing(false);
		asymptoteLayer.setEnabled(false);
		asymptoteLayer.setName(".Arc Asymptote Layer");
		
		BasicShapeAttributes targetRingBsa = new BasicShapeAttributes();
		targetRingBsa.setInteriorMaterial(Material.RED);
		targetRingBsa.setOutlineMaterial(Material.RED);
		targetRingBsa.setInteriorOpacity(0.2);
		targetRingBsa.setOutlineOpacity(0.8);
		targetRingBsa.setOutlineWidth(2);
		targetRingBsa.setEnableAntialiasing(true);
		targetRingShape = new SurfaceCircle();
		targetRingShape.setAttributes(targetRingBsa);
		targetRingLayer.addRenderable(targetRingShape);
		targetRingLayer.setEnabled(false);
		targetRingLayer.setName(".Target Ring Layer");

		iconLayer.setEnabled(false);
		iconLayer.setName(".APRS Icon Layer");
		
		lineLayer.setEnabled(false);
		lineLayer.setName(".Line Layer");
		
		ringLayer.setEnabled(false);
		ringLayer.setName(".Ring Layer");
		
		quadLayer.setEnabled(false);
		quadLayer.setName(".Quad Layer");

		polygonLayer.setEnabled(false);
		polygonLayer.setName(".Polygon Layer");
		
		testGridLayer.setEnabled(false);
		testGridLayer.setMaxActiveAltitude(60000);
		testGridLayer.setName(".Drive Test Grid");

		arcLayer.setEnabled(false);
		arcLayer.setName(".Arc Layer");

		arcCursorBma.setShapeType(BasicMarkerShape.CYLINDER);
		arcCursorBma.setMaterial(new Material(DEFAULT_ARC_CURSOR_COLOR));
		arcCursorBma.setMarkerPixels(DEFAULT_ARC_CURSOR_DIAMETER);
		arcCursorBma.setOpacity(0.7d);
		cursorLayer.setEnabled(false);
		cursorLayer.setOverrideMarkerElevation(true);
		cursorLayer.setKeepSeparated(false);
        cursorLayer.setElevation(1000d);
        cursorLayer.setName(".Arc Cursor Layer");
		cursorLayer.setMarkers(cursorMarkers);
		
		traceLineLayer.setEnabled(false);
		traceLineLayer.setName(".Arc Trace Line Layer");

		traceDotLayer.setEnabled(false);
		traceDotLayer.setName(".Arc Trace Dot Layer");
		
		gpsArrow = new BasicMarker(Position.fromDegrees(0,0), gpsArrowBma);
		gpsArrowBma.setShapeType(BasicMarkerShape.HEADING_ARROW);
		gpsArrowBma.setMaterial(Material.BLUE);
		gpsArrowBma.setHeadingMaterial(Material.BLUE);
		gpsArrowBma.setOpacity(0.3);
		gpsArrowLayer.setOverrideMarkerElevation(true);
        gpsArrowLayer.setKeepSeparated(false);
        gpsArrowLayer.setElevation(1000d);
        gpsArrowLayer.setEnabled(false);
        gpsArrowLayer.setName(".GPS Arrow");
        gpsArrowLayer.setOpacity(1d);
        List<Marker> gpsArrowMarkers = new ArrayList<Marker>(1);
        gpsArrowMarkers.add(gpsArrow);
        gpsArrowLayer.setMarkers(gpsArrowMarkers);
        
        gpsDot = new BasicMarker(Position.fromDegrees(0,0), gpsDotBma);
		gpsDotBma.setShapeType(BasicMarkerShape.CYLINDER);
		gpsDotBma.setOpacity(0.3);
		gpsDotLayer.setOverrideMarkerElevation(true);
        gpsDotLayer.setKeepSeparated(false);
        gpsDotLayer.setElevation(1000d);
        gpsDotLayer.setEnabled(false);
        gpsDotLayer.setName(".GPS Dot");
        gpsDotLayer.setOpacity(1d);
    	List<Marker> gpsDotMarkers = new ArrayList<Marker>(1);
    	gpsDotMarkers.add(gpsDot);
        gpsDotLayer.setMarkers(gpsDotMarkers);
		
		signalMarkerLayer.setEnabled(false);
		signalMarkerLayer.setOverrideMarkerElevation(true);
		signalMarkerLayer.setKeepSeparated(false);
        signalMarkerLayer.setElevation(1000d);
		signalMarkerLayer.setName(".Signal Marker Layer");
		signalMarkerLayer.setMarkers(signalMarkers);
	}

	@Override
	public void showStatisticsPanel() {
		new StatisticsPanelDialog(wwd);
	}
	
	@Override
	public void showStatusBar(boolean showStatusBar) {
		statusBar.setVisible(showStatusBar);
		wwd.redraw();
	}
	
	@Override
	public void showLayerSelectorPanel() {
		new LayerPanelDialog(wwd, new Dimension(275,600));
	}
	
	@Override
	public void showBulkDownloaderPanel() {
		if (bdd == null) bdd = new BulkDownloadDialog(wwd);
		bdd.setVisible(true);
	}
	
	public static double logb(double a, double b) {
		return Math.log(a) / Math.log(b);
	}

	private void worldWindRendered(RenderingEvent event) {
		if (isZooming) {
			isZooming = false;
			testGridLayer.setMapDimension(getMapDimension());
			wwd.redraw();
			firePropertyChange(ZOOM_COMPLETE, null, altitudeToZoom(view.getZoom()));
		}
		if (!wwdRendered) {
			wwdRendered = true;
			synchronized(renderingHold) {
				renderingHold.notifyAll();
			}
			firePropertyChange(MAP_RENDERED, null, true);
		}
	}

	private void saveLayerSelections(WorldWindow wwd) {
		for (Layer layer : wwd.getModel().getLayers()) {
			if (!layer.getName().contains(".")) userPref.putBoolean(layer.getName(), layer.isEnabled());
		}
	}
	
	private void restoreLayerSelections(WorldWindow wwd) {
		for (Layer layer : wwd.getModel().getLayers()) {
			if (!layer.getName().contains(".")) layer.setEnabled(userPref.getBoolean(layer.getName(), false));
		}
	}

	@Override
	public Point.Double getCenterLonLat() {
		try {
			Position pos = view.computePositionFromScreenPoint(getSize().width / 2.0, getSize().height / 2.0);
			return new Point.Double(pos.getLongitude().getDegrees(), pos.getLatitude().getDegrees());
		} catch (NullPointerException ex) {
			return null;
		}
	}

	@Override
	public double getMapLeftEdgeLongitude() {
		try {
			return view.computePositionFromScreenPoint(0, 0).getLongitude().getDegrees();
		} catch (NullPointerException ex) {
			return 0;
		}
	}

	@Override
	public double getMapRightEdgeLongitude() {
		try {
			return view.computePositionFromScreenPoint(getSize().width, 0).getLongitude().getDegrees();
		} catch (NullPointerException ex) {
			return 0;
		}
	}

	@Override
	public double getMapTopEdgeLatitude() {
		try {
			return view.computePositionFromScreenPoint(0, 0).getLatitude().getDegrees();
		} catch (NullPointerException ex) {
			return 0;
		}
	}

	@Override
	public double getMapBottomEdgeLatitude() {
		try {
			return view.computePositionFromScreenPoint(0, getSize().height).getLatitude().getDegrees();
		} catch (NullPointerException ex) {
			return 0;
		}
	}
	
	@Override
	public Point.Double getMapLowerRightCorner() {
		try {
			return new Point.Double(getMapRightEdgeLongitude(), getMapBottomEdgeLatitude());
		} catch (NullPointerException ex) {
			return null;
		}
	}
	
	@Override
	public Rectangle2D.Double getMapRectangle() {
		try {
			double width = Math.abs(getMapRightEdgeLongitude() - getMapLeftEdgeLongitude());
			double height = Math.abs(getMapTopEdgeLatitude() - getMapBottomEdgeLatitude());
			return new Rectangle2D.Double(getMapLeftEdgeLongitude(), getMapTopEdgeLatitude(), width, height);
		} catch (NullPointerException ex) {
			return null;
		}
	}

	@Override
	public MapDimension getMapDimension() {
		return new MapDimension(getMapRectangle());
	}
	
	@Override
	public void setCenterLonLat(Point.Double lonLat) {
		try {
			Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
			view.setCenterPosition(pos);
		} catch (NullPointerException ex) {}
	}

	private double zoomToAltitude(double zoom) {
		return ZOOM_LEVEL_CONVERSION_FACTOR * Math.pow(2, 18 - zoom);
	}

	private double altitudeToZoom(double altitude) {
		return 18 - logb(altitude / ZOOM_LEVEL_CONVERSION_FACTOR, 2);
	}

	@Override
	public void displayShapes(boolean displayShapes) {
		arcLayer.setEnabled(displayShapes && showArcs);
		asymptoteLayer.setEnabled(displayShapes && showArcAsymptotes);
		cursorLayer.setEnabled(displayShapes && showArcCursors);
		traceLineLayer.setEnabled(displayShapes && showArcTraces);
		traceDotLayer.setEnabled(displayShapes && showArcTraces);
		quadLayer.setEnabled(displayShapes && showQuads);
		polygonLayer.setEnabled(displayShapes && showPolygons);
		signalMarkerLayer.setEnabled(displayShapes && showSignalMarkers);
		intersectLayer.setEnabled(displayShapes && showArcIntersectPoints);
		ringLayer.setEnabled(displayShapes && showRings);
		iconLayer.setEnabled(displayShapes && showIcons);
		lineLayer.setEnabled(displayShapes && showLines);
		targetRingLayer.setEnabled(displayShapes && showTargetRing);
		testGridLayer.setEnabled(displayShapes && showGrid);
		gpsDotLayer.setEnabled(displayShapes && showGpsSymbol);
	}

	@Override
	public Point.Double getMouseCoordinates() {
		if (wwd.getCurrentPosition() == null) return new Point.Double(0, 0);
		return new Point.Double(wwd.getCurrentPosition().getLongitude()
				.getDegrees(), wwd.getCurrentPosition().getLatitude().getDegrees());
	}

	@Override
	public Point getMousePosition() {
		return getMousePosition();
	}

	@Override
	public void showIcons(boolean showIcons) {
		this.showIcons = showIcons;
		iconLayer.setEnabled(showIcons);
		wwd.redraw();
	}

	@Override
	public void addIcon(Point.Double lonLat, String iconPath, String identifier) {
		Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
		UserFacingIcon ufi = new UserFacingIcon(iconPath, pos);
		ufi.setShowToolTip(showIconLabels);
		ufi.setToolTipText(identifier);
		ufi.setValue(identifier, null);
		iconLayer.addIcon(ufi);
		iconList.add(ufi);
		wwd.redraw();
	}

	@Override
	public void hideIcon(int index) throws IndexOutOfBoundsException {
		try {
			iconList.get(index).setVisible(false);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException(ex.getMessage());
		}
	}

	@Override
	public void deleteAllIcons() {
		iconLayer.removeAllIcons();
		iconList.subList(0, iconList.size()).clear();
		wwd.redraw();
	}

	@Override
	public void showIconLabels(boolean showIconLabels) {
		this.showIconLabels = showIconLabels;
		for (WWIcon icon : iconLayer.getIcons()) {
			icon.setShowToolTip(showIconLabels);
		}
		wwd.redraw();
	}

	@Override
	public void moveIcon(int index, Point.Double lonLat) throws IndexOutOfBoundsException {
		try {
			Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
			iconList.get(index).moveTo(pos);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteAllSignalMarkers() {
		Iterator<Marker> itr = signalMarkerLayer.getMarkers().iterator();
		while(itr.hasNext()) {
			itr.next();
         	itr.remove();
		}
		wwd.redraw();
	}

	@Override
	public void deleteAllRings() {
		ringLayer.removeAllRenderables();
		ringList.subList(0, ringList.size()).clear();
		wwd.redraw();
	}

	@Override
	public void deleteAllLines() {
		lineLayer.removeAllRenderables();
		lineList.subList(0, lineList.size()).clear();
		wwd.redraw();
	}

	@Override
	public void deleteAllQuads() {
		quadLayer.removeAllRenderables();
		quadList.subList(0, quadList.size()).clear();
		wwd.redraw();
	}

	@Override
	public void deleteAllPolygons() {
		polygonLayer.removeAllRenderables();
		polygonList.subList(0, polygonList.size()).clear();
		wwd.redraw();
	}
	
	@Override
	public boolean isShowGPSDot() {
		return gpsDotLayer.isEnabled();
	}

	@Override
	public boolean isShowGPSArrow() {
		return gpsArrowLayer.isEnabled();
	}

	@Override
	public void setGpsSymbol(final Point.Double pt, final double diameter, final Color color, final int angle) {
		try {
			gpsDot.setPosition(Position.fromDegrees(pt.y, pt.x, 0));
			gpsArrow.setPosition(Position.fromDegrees(pt.y, pt.x, 0));
			gpsDotBma.setMaterial(new Material(color));
			gpsArrowBma.setMaterial(new Material(color));
			gpsArrow.setHeading(Angle.fromDegrees(angle));
			gpsDotBma.setMarkerPixels(diameter);
			wwd.redraw();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void setGpsDotRadius(double gpsDotDiameter) {
		this.gpsDotDiameter = gpsDotDiameter;
		gpsDotBma.setMarkerPixels(gpsDotDiameter);
		wwd.redraw();
	}

	@Override
	public void setGpsDotColor(Color color) {
		gpsDot.getAttributes().setMaterial(new Material(color));
		wwd.redraw();
	}

	@Override
	public void setGpsSymbolPosition(Point.Double lonLat, int angle) {
		Position position = Position.fromDegrees(lonLat.y, lonLat.x, 0);
		gpsDot.setPosition(position);
		gpsArrow.setPosition(position);
		setGpsSymbolAngle(angle);
	}

	@Override
	public void setGpsArrowColor(Color color) {
		gpsArrow.getAttributes().setHeadingMaterial(new Material(color));
		wwd.redraw();
	}

	@Override
	public void showTargetRing(boolean showTargetRing) {
		targetRingLayer.setEnabled(showTargetRing);
		wwd.redraw();
	}

	@Override
	public void setTargetRing(Point.Double pt, double targetRingDiameter, Color color) {
		targetRingShape.getAttributes().setInteriorMaterial(new Material(color));
		targetRingShape.getAttributes().setOutlineMaterial(new Material(color));
		targetRingShape.setRadius(targetRingDiameter);
		targetRingShape.moveTo(Position.fromDegrees(pt.y, pt.x));
		wwd.redraw();
	}

	@Override
	public void setTargetRingPosition(Point.Double pt) {
		targetRingShape.moveTo(Position.fromDegrees(pt.y, pt.x));
		wwd.redraw();
	}

	@Override
	public void setTargetRingDiameter(double targetRingDiameter) {
		targetRingShape.setRadius(targetRingDiameter);
		wwd.redraw();
	}

	@Override
	public void setTargetRingColor(Color color) {
		targetRingShape.getAttributes().setInteriorMaterial(new Material(color));
		targetRingShape.getAttributes().setOutlineMaterial(new Material(color));
		wwd.redraw();
	}

	@Override
	public void setTargetRing(Point.Double pt, double targetRingDiameter) {
		targetRingShape.moveTo(Position.fromDegrees(pt.y, pt.x));
		targetRingShape.setRadius(targetRingDiameter);
		wwd.redraw();
	}

	@Override
	public void deleteAllArcIntersectPoints() {
		Iterator<Marker> itr = intersectLayer.getMarkers().iterator();
		while(itr.hasNext()) {
			itr.next();
         	itr.remove();
		}
		wwd.redraw();
	}

	@Override
	public void setArcIntersectPointDiameter(double diameter) {
		arcIntersectBma.setMarkerPixels(diameter);
		wwd.redraw();
	}

	@Override
	public void setArcIntersectPoints(List<Point.Double> iplist) {
		deleteAllArcIntersectPoints();
		addArcIntersectPoints(iplist, arcIntersectPointDiameter, arcIntersectPointColor);
	}
	
	@Override
	public void addArcIntersectPoints(List<Point.Double> iplist) {
		addArcIntersectPoints(iplist, arcIntersectPointDiameter, arcIntersectPointColor);
	}
	
	@Override
	public void setArcIntersectPoints(List<Point.Double> iplist, double diameter, Color color) {
		deleteAllArcIntersectPoints();
		addArcIntersectPoints(iplist, diameter, color);
	}
	
	@Override
	public void addArcIntersectPoints(final List<Point.Double> iplist, final double diameter, final Color color) {
		for (Point.Double ip : iplist) {
			BasicMarker arcIntersectMarker = new BasicMarker(Position.fromDegrees(ip.y, ip.x), arcIntersectBma);
			intersectMarkers.add(arcIntersectMarker);
		}
		wwd.redraw();
	}
	
	@Override
	public void addArcIntersectPoint(Point.Double ip, double diameter, Color color) {
		List<Point.Double> iplist = new ArrayList<Point.Double>(1);
		iplist.add(ip);
		addArcIntersectPoints(iplist, diameter, color);
	}

	@Override
	public void showArcIntersectPoints(boolean showArcIntersectPoints) {
		this.showArcIntersectPoints = showArcIntersectPoints;
		intersectLayer.setEnabled(showArcIntersectPoints);
		wwd.redraw();
	}
	
	@Override
	public void addSignalMarker(Point.Double pt, Color color) {
		addSignalMarker(pt, DEFAULT_SIGNAL_MARKER_DIAMETER, color);
	}
	
	@Override
	public void addSignalMarker(Point.Double pt, double diameter, Color color) {
		BasicMarkerAttributes bma = new BasicMarkerAttributes();		
		bma.setShapeType(BasicMarkerShape.CYLINDER);
		bma.setMaterial(new Material(color));
		bma.setMarkerPixels(diameter);
		bma.setOpacity(0.7d);
		BasicMarker signalMarker = new BasicMarker(Position.fromDegrees(pt.y, pt.x), bma);
		signalMarkers.add(signalMarker);
		wwd.redraw();
	}

	@Override
	public void setSignalMarkerDiameter(double diameter) {
		for (Marker signalMarker : signalMarkers) {
			signalMarker.getAttributes().setMarkerPixels(diameter);
		}
		wwd.redraw();
	}
	
	@Override
	public boolean isShowSignalMarkers() {
		return showSignalMarkers;
	}

	@Override
	public boolean isShowArcIntersectPoints() {
		return showArcIntersectPoints;
	}
	
	@Override
	public void deleteSignalMarker(int index) throws IndexOutOfBoundsException {
		try {
			signalMarkers.remove(index);
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
		wwd.redraw();
	}

	@Override
	public boolean isShowPolygons() {
		return showPolygons;
	}
	
	@Override
	public void addPolygon(GeoTile geoTile) {
		SurfacePolygon polygon = geoTile;
		BasicShapeAttributes bsa = new BasicShapeAttributes();
		bsa.setInteriorOpacity(0.3);
		bsa.setOutlineOpacity(0.4);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		bsa.setInteriorMaterial(new Material(Color.YELLOW));
		bsa.setOutlineMaterial(new Material(Color.YELLOW));
		polygon.setAttributes(bsa);
		polygonList.add(polygon);
		polygonLayer.addRenderable(polygon);
		wwd.redraw();
	}
	
	@Override
	public void changePolygonColor(int index, Color color) throws IndexOutOfBoundsException {
		try {
			polygonList.get(index).getAttributes().setInteriorMaterial(new Material(color));
			polygonList.get(index).getAttributes().setOutlineMaterial(new Material(color));
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void setPolygonVisible(int index, boolean isVisible) {
		try {
			polygonList.get(index).setVisible(isVisible);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public void deletePolygon(int index) throws IndexOutOfBoundsException {
		try {
			polygonList.remove(index);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public void addQuad(Point.Double lonLat, Point.Double size, Color color) {
		LatLon latLon = new LatLon(LatLon.fromDegrees(lonLat.y, lonLat.x));
		SurfaceQuad surfaceQuad = new SurfaceQuad(latLon, size.x, size.y);
		BasicShapeAttributes bsa = new BasicShapeAttributes();
		bsa.setInteriorOpacity(0.3);
		bsa.setOutlineOpacity(0.4);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineMaterial(new Material(color));
		surfaceQuad.setAttributes(bsa);
		quadList.add(surfaceQuad);
		quadLayer.addRenderable(surfaceQuad);
		wwd.redraw();
	}

	@Override
	public void changeQuadColor(int index, Color color) throws IndexOutOfBoundsException {
		try {
			quadList.get(index).getAttributes().setInteriorMaterial(new Material(color));
			quadList.get(index).getAttributes().setOutlineMaterial(new Material(color));
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void setQuadVisible(int index, boolean isVisible) {
		try {
			quadList.get(index).setVisible(isVisible);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public void deleteQuad(int index) throws IndexOutOfBoundsException {
		try {
			quadList.remove(index);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void addLine(Point.Double lonLatStart, double angle, Color color) {
		BasicShapeAttributes bsa = new BasicShapeAttributes();
		bsa.setOutlineMaterial(new Material(color));
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineOpacity(0.8);
		bsa.setInteriorOpacity(0);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		Point.Double lonLatEnd = new Point.Double();
		lonLatEnd.x = (Math.sin(angle * Math.PI / 180) * 0.5) + lonLatStart.x;
		lonLatEnd.y = (Math.cos(angle * Math.PI / 180) * 0.5) + lonLatStart.y;
		List<LatLon> latLonList = new ArrayList<LatLon>();
		latLonList.add(LatLon.fromDegrees(lonLatStart.y, lonLatStart.x));
		latLonList.add(LatLon.fromDegrees(lonLatEnd.y, lonLatEnd.x));
		SurfacePolyline surfacePolyline = new SurfacePolyline(latLonList);
		surfacePolyline.setAttributes(bsa);
		lineLayer.addRenderable(surfacePolyline);
		lineList.add(surfacePolyline);
		wwd.redraw();
	}

	@Override
	public void addLine(Point.Double lonLatStart, Point.Double lonLatEnd, Color color) {
		BasicShapeAttributes bsa = new BasicShapeAttributes();
		bsa.setOutlineMaterial(new Material(color));
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineOpacity(0.8);
		bsa.setInteriorOpacity(0);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		List<LatLon> latLonList = new ArrayList<LatLon>();
		latLonList.add(LatLon.fromDegrees(lonLatStart.y, lonLatStart.x));
		latLonList.add(LatLon.fromDegrees(lonLatEnd.y, lonLatEnd.x));
		SurfacePolyline surfacePolyline = new SurfacePolyline(latLonList);
		surfacePolyline.setAttributes(bsa);
		lineLayer.addRenderable(surfacePolyline);
		lineList.add(surfacePolyline);
		wwd.redraw();
	}

	@Override
	public void hideLine(int index) throws IndexOutOfBoundsException {
		try {
			lineList.get(index).setVisible(false);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void addRing(final Point.Double lonLat, final double size, final Color color) {
		SurfaceCircle ringShape = new SurfaceCircle(LatLon.fromDegrees(lonLat.y, lonLat.x), size);
		BasicShapeAttributes bsa = new BasicShapeAttributes();
		bsa.setOutlineMaterial(new Material(color));
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineOpacity(0.8);
		bsa.setInteriorOpacity(0);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		ringShape.setAttributes(bsa);
		ringLayer.addRenderable(ringShape);
		ringList.add(ringShape);
		wwd.redraw();
	}

	@Override
	public void hideRing(int index) throws IndexOutOfBoundsException {
		try {
			ringList.remove(index);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}

	public double getAltitude() {
		return view.getZoom();
	}

	public void setAltitude(double altitude) {
		view.setZoom(altitude);
		wwd.redraw();
	}

	@Override
	public void zoomIn() {
		isZooming = true;
		double zoom = altitudeToZoom(view.getZoom());
		if (zoom <= 17.5) {
			view.setZoom(zoomToAltitude(zoom + 0.5));
		} else {
			view.setZoom(zoomToAltitude(18));
		}
		wwd.redraw();
	}

	@Override
	public void zoomOut() {
		isZooming = true;
		double zoom = altitudeToZoom(view.getZoom());
		if (zoom >= 1.5) {
			view.setZoom(zoomToAltitude(zoom - 0.5));
		} else {
			view.setZoom(zoomToAltitude(1));
		}
		wwd.redraw();
	}

	@Override
	public void showGrid(boolean showGrid) {
		this.showGrid = showGrid;
		testGridLayer.setEnabled(showGrid);
		wwd.redraw();
	}

	@Override
	public boolean isShowGrid() {
		return showGrid;
	}

	@Override
	public void setGridSize(final Point.Double gridSize) {
		this.gridSize = gridSize;
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				synchronized(renderingHold) {
					if(!wwdRendered) renderingHold.wait();
				}
				return null;
			}
			@Override
			protected void done() {
				testGridLayer.setTestGridParameters(gridSize, getMapDimension(), gridColor);
				wwd.redraw();
			}
		};
		worker.execute();
	}

	@Override
	public int numberOfIcons() {
		return iconList.size();
	}

	@Override
	public void showQuads(boolean showQuads) {
		this.showQuads = showQuads;
		quadLayer.setEnabled(showQuads);
		wwd.redraw();
	}

	@Override
	public void showPolygons(boolean showPolygons) {
		this.showPolygons = showPolygons;
		polygonLayer.setEnabled(showPolygons);
		wwd.redraw();
	}
	
	@Override
	public void showSignalMarkers(boolean showSignalMarkers) {
		this.showSignalMarkers = showSignalMarkers;
		signalMarkerLayer.setEnabled(showSignalMarkers);
		wwd.redraw();
	}

	@Override
	public void showLines(boolean showLines) {
		this.showLines = showLines;
		lineLayer.setEnabled(showLines);
		wwd.redraw();
	}

	@Override
	public boolean isShowLines() {
		return showLines;
	}
	
	@Override
	public void showRings(boolean showRings) {
		this.showRings = showRings;
		ringLayer.setEnabled(showRings);
		wwd.redraw();
	}

	@Override
	public boolean isShowRings() {
		return showRings;
	}
	
	@Override
	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
		testGridLayer.setGridColor(gridColor);
		wwd.redraw();
	}

	@Override
	public BufferedImage getScreenShot() {
		GLProfile glProfile = GLProfile.getDefault();
		AWTGLReadBufferUtil aWTGLReadBufferUtil = new AWTGLReadBufferUtil(glProfile, true);
		wwd.getContext().makeCurrent();
		GL gl = wwd.getContext().getGL();
		wwd.getContext().release();
		return aWTGLReadBufferUtil.readPixelsToBufferedImage(gl, 0, 0, getWidth(), getHeight(), true);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private double scaleToAltitude(double scale) {
		return 65000.0 - (5000.0 * scale);
	}

	@Override
	public void setScale(double scale) {
		view.setZoom(scaleToAltitude(scale));
		wwd.redraw();
	}

	private double altitudeToScale(double altitude) {
		return 13.0 - ((altitude / 50000.0) * 10.0);
	}

	@Override
	public double getScale() {
		return altitudeToScale(view.getZoom());
	}

	@Override
	public Point.Double getGridSize() {
		return gridSize;
	}

	@Override
	public void setZoom(int zoom) {
		view.setZoom(zoomToAltitude(zoom));
		wwd.redraw();
	}

	@Override
	public int getZoom() {
		return (int) Math.round(altitudeToZoom(view.getZoom()));
	}

	@Override
	public void deleteAllArcs() {
		arcLayer.removeAllRenderables();
		traceLineLayer.removeAllRenderables();
		asymptoteLayer.removeAllRenderables();
		traceDotMarkers.subList(0, traceDotMarkers.size()).clear();
		cursorMarkers.subList(0, cursorMarkers.size()).clear();
		intersectMarkers.subList(0, intersectMarkers.size()).clear();
		arcList.subList(0, arcList.size()).clear();
		wwd.redraw();
	}

	@Override
	public void removeArc(int index) {
		try {
			arcList.remove(index);
			wwd.redraw();
		} catch (IndexOutOfBoundsException ex) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void showArcs(boolean showArcs) {
		this.showArcs = showArcs;
		arcLayer.setEnabled(showArcs);
		wwd.redraw();
	}

	@Override
	public boolean isShowMapImage() {
		return isVisible();
	}

	@Override
	public void showMapImage(boolean showMapImage) {
		setVisible(showMapImage);
		wwd.redraw();
	}

	@Override
	public void showArcAsymptotes(boolean showArcAsymptotes) {
		this.showArcAsymptotes = showArcAsymptotes;
		asymptoteLayer.setEnabled(showArcAsymptotes);
		wwd.redraw();
	}

	@Override
	public void showArcCursors(boolean showArcCursors) {
		this.showArcCursors = showArcCursors;
		cursorLayer.setEnabled(showArcCursors);
		wwd.redraw();
	}

	@Override
	public void showArcTrace(boolean showArcTrace) {
		this.showArcTraces = showArcTrace;
		traceDotLayer.setEnabled(showArcTrace);
		traceLineLayer.setEnabled(showArcTrace);
		wwd.redraw();
	}

	@Override
	public void showGpsSymbol(boolean showGpsSymbol) {
		this.showGpsSymbol = showGpsSymbol;
		setGpsSymbolAngle(gpsAngle);
	}

	private void createArcSurfacePolyline(ConicSection cone) {
		BasicShapeAttributes bsa = new BasicShapeAttributes();
		List<LatLon> hyperbolicLatLonArray = cone.getHyperbolicLatLonList();
		SurfacePolyline arcSurfacePolyline = new SurfacePolyline(hyperbolicLatLonArray);
		if (arcColors != null) bsa.setOutlineMaterial(new Material(arcColors[cone.getUnit()]));
		bsa.setOutlineOpacity(0.9);
		bsa.setOutlineWidth(1.5);
		bsa.setEnableAntialiasing(true);
		arcSurfacePolyline.setAttributes(bsa);
		arcLayer.addRenderable(arcSurfacePolyline);
	}
	
	public void createArcCursors(ConicSection cone, double diameter, Color color) {
		arcCursorBma.setMaterial(new Material(color));
		arcCursorBma.setOpacity(0.8);
		arcCursorBma.setMarkerPixels(diameter);
		Marker center = new BasicMarker(Position.fromDegrees(cone.getCenter().y, cone.getCenter().x), arcCursorBma);
		Marker vertex = new BasicMarker(Position.fromDegrees(cone.getVertex().y, cone.getVertex().x), arcCursorBma);
		Marker focus = new BasicMarker(Position.fromDegrees(cone.getFocus().y, cone.getFocus().x), arcCursorBma);
		cursorMarkers.add(center);
		cursorMarkers.add(vertex);
		cursorMarkers.add(focus);		
	}
	
	private void createArcTraces(ConicSection cone, double diameter) {
		BasicShapeAttributes bsa = new BasicShapeAttributes();
		BasicMarkerAttributes bma = new BasicMarkerAttributes();
		if (traceEqualsFlightColor && arcColors != null) {
			bsa.setOutlineMaterial(new Material(arcColors[cone.getUnit()]));
			bma.setMaterial(new Material(arcColors[cone.getUnit()]));
		} else {
			bsa.setOutlineMaterial(new Material(arcTraceColor));
			bma.setMaterial(new Material(arcTraceColor));
		}
		bsa.setOutlineOpacity(0.8);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		bma.setShapeType(BasicMarkerShape.CYLINDER);
		bma.setMarkerPixels(diameter);
		bma.setOpacity(0.8d);
		List<LatLon> points = new ArrayList<LatLon>();
		points.add(LatLon.fromDegrees(cone.getSMA().point.y, cone.getSMA().point.x));
		points.add(LatLon.fromDegrees(cone.getSMB().point.y, cone.getSMB().point.x));
		traceLineLayer.addRenderable(new SurfacePolyline(bsa, points));
		traceDotMarkers.add(new BasicMarker(Position.fromDegrees(cone.getSMA().point.y, cone.getSMA().point.x), bma));
		traceDotMarkers.add(new BasicMarker(Position.fromDegrees(cone.getSMB().point.y, cone.getSMB().point.x), bma));
		traceDotLayer.setMarkers(traceDotMarkers);
	}
	
	private void createArcAsymptotes(ConicSection cone, double length, Color color) {
     	SurfacePolyline surfacePolyline = new SurfacePolyline(cone.getAsymptotes());
		surfacePolyline.setAttributes(arcAsymptoteBsa);
		asymptoteLayer.addRenderable(surfacePolyline);
	}

	@Override
	public void addArc(final ConicSection cone) {
		arcList.add(cone);
		createArcSurfacePolyline(cone);
		createArcAsymptotes(cone, ARC_LENGTH, arcAsymptoteColor);
		createArcTraces(cone, arcTraceDiameter);
		createArcCursors(cone, arcCursorDiameter, arcCursorColor);
		wwd.redraw();
	}

	@Override
	public void setArcAsymptoteColor(Color arcAsymptoteColor) {
		this.arcAsymptoteColor = arcAsymptoteColor;
		arcAsymptoteBsa.setOutlineMaterial(new Material(arcAsymptoteColor));
		wwd.redraw();
	}

	@Override
	public void setArcColors(Color[] arcColors) {
		this.arcColors = arcColors;
		wwd.redraw();
	}

	private void updateTraces() {
		traceLineLayer.removeAllRenderables();
		traceDotMarkers.subList(0, traceDotMarkers.size()).clear();
		for (ConicSection cone : arcList) {
			createArcTraces(cone, arcTraceDiameter);
		}
	}
	
	@Override
	public void setTraceEqualsFlightColor(boolean traceEqualsFlightColor) {
		if (this.traceEqualsFlightColor == traceEqualsFlightColor) return;
		this.traceEqualsFlightColor = traceEqualsFlightColor;
		updateTraces();
		wwd.redraw();
	}
	
	@Override
	public void setArcTraceColor(Color arcTraceColor) {
		if (this.arcTraceColor.equals(arcTraceColor)) return;
		this.arcTraceColor = arcTraceColor;
		updateTraces();
		wwd.redraw();
	}

	@Override
	public void setArcCursorColor(Color arcCursorColor) {
		this.arcCursorColor = arcCursorColor;
		arcCursorBma.setMaterial(new Material(arcCursorColor));
		wwd.redraw();
	}

	@Override
	public void setArcIntersectPointColor(Color arcIntersectPointColor) {
		this.arcIntersectPointColor = arcIntersectPointColor;
		arcIntersectBma.setMaterial(new Material(arcIntersectPointColor));
		wwd.redraw();
	}

	@Override
	public boolean isShowTargetRing() {
		return showTargetRing;
	}

	@Override
	public void setGpsSymbolColor(Color color) {
		gpsDot.getAttributes().setMaterial(new Material(color));
		gpsArrow.getAttributes().setHeadingMaterial(new Material(color));
		wwd.redraw();
	}

	@Override
	public void setGpsSymbolAngle(int gpsAngle) {
		this.gpsAngle = gpsAngle;
		if (gpsAngle == 360) {
			gpsDotLayer.setEnabled(showGpsSymbol && gpsDotDiameter > 0);
			gpsArrowLayer.setEnabled(false);
		} else {
			gpsArrowLayer.setEnabled(showGpsSymbol);
			gpsDotLayer.setEnabled(false);
		}
		wwd.redraw();
	}

	@Override
	public int getMaxZoom() {
		return (int) altitudeToZoom(4513.988880);
	}

	@Override
	public void setArcTraceDiameter(double arcTraceDiameter) {
		this.arcTraceDiameter = arcTraceDiameter;
		updateTraces();
		wwd.redraw();
	}
	
	@Override
	public void setArcCursorDiameter(double arcCursorDiameter) {
		this.arcCursorDiameter = arcCursorDiameter;
		arcCursorBma.setMarkerPixels(arcCursorDiameter);
		wwd.redraw();
	}
	
	private boolean setupListenersWhenConnected() {
		Window parentFrame = (Window) getTopLevelAncestor();
		parentFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					shutDown();
				}
			}
		});
		return true;
	}
	
	@Override
	public void shutDown() {
		saveLayerSelections(wwd);
		removeAll();
		wwd.shutdown();
		WorldWind.shutDown();
	}
	
	public static float zoomToResolution(int zoom, float latitude) {
		return (float) (156543.034 * Math.cos(latitude) / (2 ^ zoom));
	}

	public static float resolutionToScale(float resolution) {
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		return (float) (dpi * 39.37 * resolution);
	}

	public static float scaleToOsmZoomLevel(float mapScale, float latitude) {
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		float metersPerInch = 2.54f / 100;
		double realLengthInMeters = 40075016.686 * Math.cos(Math.toRadians(latitude));
		double zoomLevelExp = (realLengthInMeters * dpi) / (256 * metersPerInch * mapScale);
		return (float) Math.pow(zoomLevelExp, 2);
	}

    private static void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
        int position = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer)
                position = layers.indexOf(l);
        }
        layers.add(position, layer);
    }

    private static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof CompassLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }
    
    private void gridRedraw() {
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		        testGridLayer.setMapDimension(getMapDimension());
		        wwd.redraw();
		        if (!view.isAnimating()) {
		        	gridRedrawHandle.cancel(false);
		        	gridRedrawScheduler.shutdown();
		        	animationRedrawTimerActive = false;
		        }
		    }
		});
	}
    
    private void gridRedrawAnimating(int initialDelay, int delay) {
    	animationRedrawTimerActive = true;
    	Runnable gridRedraw = new Runnable() { 
    		@Override
    		public void run() { 
    			gridRedraw(); 
    		}
    	};
        gridRedrawScheduler = Executors.newScheduledThreadPool(2);
        gridRedrawHandle = gridRedrawScheduler.scheduleAtFixedRate(gridRedraw, initialDelay, delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void redraw() {
		wwd.redraw();
	}

	@Override
	public int getMinZoom() {
		return 0;
	}

	@Override
	public void zoomIn(Point pivot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomOut(Point pivot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setZoom(int zoom, Point pivot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveMap(int diffx, int diffy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeJMVListener(JMapViewerEventListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addJMVListener(JMapViewerEventListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AttributionSupport getAttribution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTargetRingRadius(double radius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcCursorRadius(double radius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSignalMarkerRadius(double radius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcTraceRadius(double radius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcIntersectPointRadius(double radius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getMeterPerPixel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeRing(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreCache() {
		// TODO Auto-generated method stub
		
	}
}
