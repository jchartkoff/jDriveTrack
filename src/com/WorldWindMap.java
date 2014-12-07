package com;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceQuad;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLProfile;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class WorldWindMap extends JPanel implements Map, Cloneable {
	private static final long serialVersionUID = -3729676026605579052L;
	
	private static final int DEFAULT_ZOOM = 6;
	private static final int DEFAULT_GPS_SIZE = 200;
	private static final Dimension DEFAULT_MAP_SIZE = new Dimension(800,600);
	private static final double ZOOM_LEVEL_CONVERSION_FACTOR = 4513.988880;
	
	private BasicOrbitView view;
	private Model model;
	private Earth earth;
	private Point.Double gridSize;
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
	private int arcCursorDiameter;
	private Color arcCursorColor = Color.BLUE;
	private Color arcTrailColor;
	private Color arcAsymptoteColor = Color.CYAN;
	private Color gridColor = Color.RED;
	private SurfaceCircle gpsShape;
	private SurfaceCircle targetRingShape;
	private IconLayer iconLayer = new IconLayer();
	private BasicShapeAttributes dotBsa = new BasicShapeAttributes();
	private BasicShapeAttributes ringBsa = new BasicShapeAttributes();
	private BasicShapeAttributes lineBsa = new BasicShapeAttributes();
	private BasicShapeAttributes quadBsa = new BasicShapeAttributes();
	private BasicShapeAttributes targetRingBsa = new BasicShapeAttributes();
	private BasicShapeAttributes arcIntersectBsa = new BasicShapeAttributes();
	private BasicShapeAttributes gpsBsa = new BasicShapeAttributes();
	private BasicShapeAttributes gridBsa = new BasicShapeAttributes();
	private RenderableLayer gpsLayer = new RenderableLayer();
	private RenderableLayer targetRingLayer = new RenderableLayer();
	private RenderableLayer gridLayer = new RenderableLayer();
	private RenderableLayer lineLayer = new RenderableLayer();
	private RenderableLayer ringLayer = new RenderableLayer();
	private RenderableLayer dotLayer = new RenderableLayer();
	private RenderableLayer quadLayer = new RenderableLayer();
	private RenderableLayer arcIntersectLayer = new RenderableLayer();
	private List<UserFacingIcon> iconList;
	private List<SurfacePolyline> lineList;
	private List<SurfaceCircle> ringList;
	private List<SurfaceCircle> dotList;
	private List<SurfaceQuad> quadList;
	private List<SurfaceCircle> arcIntersectList;
	private List<HyperbolicProjection> arcList;
    private Color[] arcColors;
    private double targetRingDiameter;
    private double gpsDotDiameter = DEFAULT_GPS_SIZE;
    private HyperbolicProjection slp = null;
	private Point.Double upperLeftLonLat = null;
	private Point.Double lowerRightLonLat= null;
	private SurfacePolyline gridPolyline = null;
	private Arrow gpsArrow;
	private WorldWindow wwd;
	private boolean wwdRendered = false;
	
	public WorldWindMap() {
		this(new Point.Double(35.0,-86.0), DEFAULT_ZOOM, DEFAULT_MAP_SIZE);
	}
	
	public WorldWindMap(Point.Double lonLat) {
		this(lonLat, DEFAULT_ZOOM, DEFAULT_MAP_SIZE);
	}
	
	public WorldWindMap(Point.Double lonLat, int zoom) {
		this(lonLat, zoom, DEFAULT_MAP_SIZE);
	}
	
	public WorldWindMap(Point.Double lonLat, int zoom, Dimension canvasSize) {		
		setSize(canvasSize);
		setPreferredSize(canvasSize);
		setDoubleBuffered(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		
		if (zoom < 1 || zoom > 18) zoom = 12;
		
		wwd = new WorldWindowGLCanvas();
		view = new BasicOrbitView();
		earth = new Earth();
		model = new BasicModel();
		
		wwd.setModel(model);
		wwd.setView(view);
		
        ((Component) wwd).setPreferredSize(canvasSize);
        
		model.getLayers().add(new LandsatI3WMSLayer());
        model.getLayers().add(new ScalebarLayer());
        model.getLayers().add(new BMNGWMSLayer());
		model.setShowWireframeExterior(false);
		model.setShowWireframeInterior(false);
		model.setShowTessellationBoundingVolumes(false);

		view.setGlobe(earth);
		view.setZoom(zoomToAltitude(zoom));
		
		Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
		view.setCenterPosition(pos);

		wwd.getInputHandler().setForceRedrawOnMousePressed(true);

		wwd.getInputHandler().addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
                processKeyEvent(event);
                event.consume();
			}
			@Override
			public void keyReleased(KeyEvent event) {
				processKeyEvent(event);
				event.consume();
			}
			@Override
			public void keyTyped(KeyEvent event) {
				processKeyEvent(event);
				event.consume();
			}			
		});
		
		wwd.getInputHandler().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
            public void mouseDragged(MouseEvent event) {
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					redimensionMap();
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
		        event.consume();
			}
		});
		
		wwd.getInputHandler().addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() >= 2) {
		            zoomOut();
		        } 
				else if (event.getButton() == MouseEvent.BUTTON2) {
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
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				processMouseEvent(event);
			}
			@Override
			public void mousePressed(MouseEvent event) {
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
					MouseEvent me = new MouseEvent((Component) wwd, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 
							event.getScreenPoint().x, event.getScreenPoint().y, 1, false);
					processMouseMotionEvent(me);
					event.consume();
				} catch (NullPointerException ex) { }
			}
		});

		wwd.addRenderingListener(new RenderingListener() {
			@Override
			public void stageChanged(RenderingEvent event) {
				if (event.getStage() == RenderingEvent.AFTER_BUFFER_SWAP) {
					worldWindRendered(event);
					event.consume();
				}
			}
		});
		
		wwd.addRenderingExceptionListener(new RenderingExceptionListener() {
            public void exceptionThrown(Throwable t) {
                if (t instanceof WWAbsentRequirementException) {
                    String message = "This computer does not meet minimum graphics requirements.\n";
                    message += "Please install an up-to-date graphics driver and try again.\n";
                    message += "Reason: " + t.getMessage() + "\n";

                    JOptionPane.showMessageDialog(getParent(), message, "Unable to Run NASA WorldWind",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        });

		addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent event) {
				boolean connected = setupListenersWhenConnected();
                if (connected) {
                    removeHierarchyListener(this);
                }
			}
        });
		
		add((Component) wwd);
		
		gpsArrow = new Arrow(canvasSize);
		gpsArrow.setVisible(true);
		add(gpsArrow);
		
		gpsBsa.setInteriorMaterial(Material.RED);
		gpsBsa.setOutlineMaterial(Material.RED);
		gpsBsa.setInteriorOpacity(0.5);
		gpsBsa.setOutlineOpacity(0.8);
		gpsBsa.setOutlineWidth(1);
		gpsBsa.setEnableAntialiasing(true);
		
		gpsShape = new SurfaceCircle(LatLon.fromDegrees(lonLat.y,lonLat.x), view.getZoom() / gpsDotDiameter);
		gpsShape.setAttributes(gpsBsa);
		gpsShape.setVisible(true);
		gpsLayer.addRenderable(gpsShape);
		model.getLayers().add(gpsLayer);
		
		targetRingBsa.setInteriorMaterial(Material.RED);
		targetRingBsa.setOutlineMaterial(Material.RED);
		targetRingBsa.setInteriorOpacity(0.0);
		targetRingBsa.setOutlineOpacity(0.8);
		targetRingBsa.setOutlineWidth(1);
		targetRingBsa.setEnableAntialiasing(true);
		
		targetRingShape = new SurfaceCircle(LatLon.fromDegrees(lonLat.y,lonLat.x), 2000);
		targetRingShape.setAttributes(targetRingBsa);
		targetRingLayer.addRenderable(targetRingShape);
		model.getLayers().add(targetRingLayer);
		
		gridBsa.setOutlineMaterial(new Material(gridColor));
		gridBsa.setOutlineWidth(0.5);
		gridBsa.setInteriorOpacity(0.0);
		gridBsa.setOutlineOpacity(0.8);
		gridBsa.setEnableAntialiasing(true);

		iconList = new ArrayList<UserFacingIcon>(1024);
		lineList = new ArrayList<SurfacePolyline>(1024);
		ringList = new ArrayList<SurfaceCircle>(1024);
		dotList = new ArrayList<SurfaceCircle>(1024);
		quadList = new ArrayList<SurfaceQuad>(1024);
		arcIntersectList = new ArrayList<SurfaceCircle>(1024);
		arcList = new ArrayList<HyperbolicProjection>(1024);
		
		firePropertyChange(MAP_IMAGE_COMPLETE, null, null);
	}

	@Override
	public void showSettings(boolean showSettings) {
		BulkDownloadPanel bulkDownloadPanel = new BulkDownloadPanel(wwd);
		bulkDownloadPanel.setVisible(true);
	}
	
	public static double logb( double a, double b ) {
		return Math.log(a) / Math.log(b);
	}
	
	private void worldWindRendered(RenderingEvent event) {
		if (!wwdRendered) {
			wwdRendered = true;
			firePropertyChange(MAP_RENDERED, null, true);
			firePropertyChange(MAP_IMAGE_COMPLETE, null, true);
			redimensionMap();
			displayShapes(true);
		}
	}
	
	@Override
	public boolean isRendered() {
		return wwdRendered;
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
			Position pos = view.computePositionFromScreenPoint(0, 0);
			return pos.getLongitude().getDegrees();
		} catch (NullPointerException ex) { 
			return 0;
		}
	}

	@Override	
	public double getMapRightEdgeLongitude() {
		try {
			Position pos = view.computePositionFromScreenPoint(getSize().width, 0);
			return pos.getLongitude().getDegrees();
		} catch (NullPointerException ex) { 
			return 0;
		}
	}

	@Override	
	public double getMapTopEdgeLatitude() {
		try {
			Position pos = view.computePositionFromScreenPoint(0, 0);
			return pos.getLatitude().getDegrees();
		} catch (NullPointerException ex) { 
			return 0;
		}
	}

	@Override	
	public double getMapBottomEdgeLatitude() {
		try {
			Position pos = view.computePositionFromScreenPoint(0, getSize().height);
			return pos.getLatitude().getDegrees();
		} catch (NullPointerException ex) { 
			return 0;
		}
	}

	@Override	
	public void setCenterLonLat(Point.Double lonLat) {
		try {
			Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
			view.setCenterPosition(pos);
		} catch (NullPointerException ex) { }
	}

	private double zoomToAltitude(double zoom) {
		return ZOOM_LEVEL_CONVERSION_FACTOR * Math.pow(2, 18 - zoom);
	}
	
	private double altitudeToZoom(double altitude) {
		return 18 - logb(altitude / ZOOM_LEVEL_CONVERSION_FACTOR, 2);
	}
	
	private void redimensionMap() {
    	upperLeftLonLat = new Point.Double(getMapLeftEdgeLongitude(), getMapTopEdgeLatitude());   	
    	lowerRightLonLat = new Point.Double(getMapRightEdgeLongitude(), getMapBottomEdgeLatitude()); 
    	for (HyperbolicProjection tempArc : arcList) {
    		tempArc.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    	}
    	gpsArrow.setCornerLonLat(upperLeftLonLat, lowerRightLonLat);
    }
	
	@Override
    public void displayShapes(boolean displayShapes) {
    	for (HyperbolicProjection tempArc : arcList) {
			tempArc.showArc(displayShapes && showArcs);
			tempArc.showAsymptote(displayShapes && showArcAsymptotes);
    		tempArc.showCursor(displayShapes && showArcCursors);
    		tempArc.showTrail(displayShapes && showArcTrails);
    	}

    	for (SurfaceQuad tempQuad : quadList) {
    		tempQuad.setVisible(displayShapes && showQuads);
    	}
    	
    	for (SurfaceCircle tempDot : dotList) {
    		tempDot.setVisible(displayShapes && showDots);
    	}
    	
    	for (SurfaceCircle tempIntersect : arcIntersectList) {
    		tempIntersect.setVisible(displayShapes && showArcIntersectPoints);
    	}
    	
    	for (SurfaceCircle tempRing : ringList) {
    		tempRing.setVisible(displayShapes && showRings);
    	}
    	
    	for (UserFacingIcon tempIcon : iconList) {
    		tempIcon.setVisible(displayShapes && showIcons);
    	}
    	
    	for (SurfacePolyline tempLine : lineList) {
    		tempLine.setVisible(displayShapes && showLines);
    	}
    	
    	targetRingShape.setVisible(showTargetRing && displayShapes);
    	showGpsSymbol(showGpsSymbol && displayShapes);
    }
	
	@Override
	public Point.Double getMouseCoordinates() {
		if (wwd.getCurrentPosition() == null) return new Point.Double(0,0);
		return new Point.Double(wwd.getCurrentPosition().getLongitude().getDegrees(), 
				wwd.getCurrentPosition().getLatitude().getDegrees());
	}

	@Override
	public Point getMousePosition() {
		return getMousePosition();
	}
	
	@Override
	public void showIcons(boolean showIcons) {
		if (this.showIcons != showIcons) {
			this.showIcons = showIcons;
			if (showIcons)
				model.getLayers().add(iconLayer);
			else
				model.getLayers().remove(iconLayer);
		}
	}

	@Override
	public void addIcon(Point.Double lonLat, String iconPath, String identifier) {
		Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
		UserFacingIcon ufi = new UserFacingIcon(iconPath, pos);
		ufi.setShowToolTip(showIconLabels);
		ufi.setToolTipText(identifier);
		iconLayer.addIcon(ufi);
		iconList.add(ufi);
	}

	@Override
	public void removeIcon(int index) throws IndexOutOfBoundsException {
		try {
			UserFacingIcon ufi = iconList.get(index);
			iconLayer.removeIcon(ufi);
			iconList.remove(index);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteAllIcons() {
		iconLayer.removeAllIcons();
		iconList.subList(0, iconList.size()).clear();
	}

	@Override
	public void showIconLabels(boolean showIconLabels) {
		if (this.showIconLabels != showIconLabels) {
			this.showIconLabels = showIconLabels;
			iconLayer.removeAllIcons();
			for (int i = 0; i < iconList.size(); i++) {
				iconList.get(i).setShowToolTip(showIconLabels);
				iconLayer.addIcon(iconList.get(i));
			}
		}
	}

	@Override	
	public void moveIcon(int index, Point.Double lonLat) throws IndexOutOfBoundsException {
		try {
			UserFacingIcon ufi = iconList.get(index);
			String iconPath = ufi.getPath();
			String identifier = ufi.getToolTipText();
			iconLayer.removeIcon(ufi);
			iconList.remove(index);
			Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
			UserFacingIcon newUfi = new UserFacingIcon(iconPath, pos);
			ufi.setShowToolTip(showIconLabels);
			ufi.setToolTipText(identifier);
			iconLayer.addIcon(newUfi);
			iconList.add(index, newUfi);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteAllDots() {
		dotList.subList(0, dotList.size()).clear();
		dotLayer.removeAllRenderables();
	}

	@Override	
	public void deleteAllRings() {
		ringList.subList(0, ringList.size()).clear();
		ringLayer.removeAllRenderables();
	}

	@Override
	public void deleteAllLines() {
		lineList.subList(0, lineList.size()).clear();
		lineLayer.removeAllRenderables();
	}

	@Override
	public void deleteAllQuads() {
		quadList.subList(0, quadList.size()).clear();
		quadLayer.removeAllRenderables();
	}

	@Override
	public boolean isShowGPSDot() {
		return gpsShape.isVisible();
	}

	@Override
	public boolean isShowGPSArrow() {
		return gpsArrow.isVisible();
	}
	
	@Override
	public void setGpsSymbol(Point.Double gpsShapePosition, double gpsDotDiameter, Color color, int angle) {
		if (!wwdRendered) return;
		Material gpsColor = new Material(color);
		this.gpsDotDiameter = gpsDotDiameter * 20;
		if (angle == 360) {
			gpsShape.setVisible(true);
			Position pos = new Position(Angle.fromDegreesLatitude(gpsShapePosition.y),	
					Angle.fromDegreesLongitude(gpsShapePosition.x), 0.0);
			gpsShape.moveTo(pos);
			gpsShape.setRadius(view.getZoom() / this.gpsDotDiameter);
			gpsBsa.setInteriorMaterial(gpsColor);
			gpsBsa.setOutlineMaterial(gpsColor);
			gpsArrow.setVisible(false);
			gpsShape.setAttributes(gpsBsa);
		} else {
			gpsArrow.setVisible(true);
			gpsArrow.setLocation(gpsShapePosition, angle);
			gpsArrow.setArrowSize((int) this.gpsDotDiameter / 20);
			gpsArrow.setColor(color);
			gpsShape.setVisible(false);
		}
	}
	
	@Override
	public void setGpsDotPosition(Point.Double lonLat) {
		Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
		gpsShape.moveTo(pos);
	}

	@Override
	public void setGpsDotDiameter(double gpsDotDiameter) {
		this.gpsDotDiameter = gpsDotDiameter * 20;
		gpsShape.setRadius(view.getZoom() / this.gpsDotDiameter);
	}

	@Override
	public void setGpsDotColor(Color color) {
		Material gpsColor = new Material(color);
		gpsBsa.setInteriorMaterial(gpsColor);
		gpsBsa.setOutlineMaterial(gpsColor);
		gpsShape.setAttributes(gpsBsa);
	}

	@Override
	public void setGpsArrowPosition(Point.Double lonLat, int angle) {
		gpsArrow.setLocation(lonLat, angle);
	}

	@Override
	public void setGpsArrowColor(Color color) {
		gpsArrow.setColor(color);
	}

	@Override
	public void setGpsArrowSize(double size) {
		gpsArrow.setSize((int) size * 20, (int) size * 20);
	}
	
	@Override	
	public void showTargetRing(boolean showTargetRing) {
		targetRingShape.setVisible(showTargetRing);
	}

	@Override
	public void setTargetRing(Point.Double lonLat, double targetRingSize, Color color) {
		Position pos = new Position(Angle.fromDegreesLatitude(lonLat.y), Angle.fromDegreesLongitude(lonLat.x), 0.0);
		targetRingShape.moveTo(pos);
		Material targetRingColor = new Material(color);
		targetRingBsa.setInteriorMaterial(targetRingColor);
		targetRingBsa.setOutlineMaterial(targetRingColor);
		targetRingShape.setRadius(view.getZoom() / targetRingDiameter);
		targetRingShape.setAttributes(targetRingBsa);
	}

	@Override
	public void setTargetRingPosition(Point.Double targetRingPosition) {
		Position pos = new Position(Angle.fromDegreesLatitude(targetRingPosition.y), 
				Angle.fromDegreesLongitude(targetRingPosition.x), 0.0);
		targetRingShape.moveTo(pos);
	}

	@Override
	public void setTargetRingDiameter(double targetRingDiameter) {
		targetRingShape.setRadius(view.getZoom() / targetRingDiameter);
		this.targetRingDiameter = targetRingDiameter;
	}

	@Override
	public void setTargetRingColor(Color color) {
		Material targetRingColor = new Material(color);
		targetRingBsa.setInteriorMaterial(targetRingColor);
		targetRingBsa.setOutlineMaterial(targetRingColor);
		targetRingShape.setAttributes(targetRingBsa);
	}

	@Override
	public void setTargetRing(Point.Double lonLat, double targetRingDiameter) {
		targetRingShape.setRadius(view.getZoom() / targetRingDiameter);
		this.targetRingDiameter = targetRingDiameter;
	}
	
	@Override
	public void deleteAllArcIntersectPoints() {
		arcIntersectList.subList(0, dotList.size()).clear();
		arcIntersectLayer.removeAllRenderables();
	}

	@Override
	public void addArcIntersectPoint(Point.Double p, double arcIntersectPointDiameter, Color color) {
		try {
			SurfaceCircle arcIntersectShape = new SurfaceCircle(LatLon.fromDegrees(p.y, p.x), arcIntersectPointDiameter);
			arcIntersectBsa.setOutlineMaterial(new Material(color));
			arcIntersectBsa.setInteriorMaterial(new Material(color));
			arcIntersectBsa.setOutlineOpacity(0.8);
			arcIntersectBsa.setInteriorOpacity(0.2);
			arcIntersectBsa.setOutlineWidth(1);
			arcIntersectBsa.setEnableAntialiasing(true);
			arcIntersectShape.setAttributes(arcIntersectBsa);
			arcIntersectLayer.addRenderable(arcIntersectShape);
			arcIntersectList.add(arcIntersectShape);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void removeArcIntersectPoint(int index) {
		try {
			arcIntersectList.remove(index);
			arcIntersectLayer.removeAllRenderables();
			for (SurfaceCircle temp : arcIntersectList) {
				arcIntersectLayer.addRenderable(temp);
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void showArcIntersectPoints(boolean showArcIntersectPoints) {
		if (showArcIntersectPoints) model.getLayers().addIfAbsent(arcIntersectLayer);
		else model.getLayers().remove(arcIntersectLayer);
		this.showArcIntersectPoints = showArcIntersectPoints;
	}

	@Override
	public void addDot(Point.Double lonLat, double size, Color color) {
		try {
			SurfaceCircle dotShape = new SurfaceCircle(LatLon.fromDegrees(lonLat.y, lonLat.x), size);
			dotBsa.setOutlineMaterial(new Material(color));
			dotBsa.setInteriorMaterial(new Material(color));
			dotBsa.setOutlineOpacity(0.8);
			dotBsa.setInteriorOpacity(0.2);
			dotBsa.setOutlineWidth(1);
			dotBsa.setEnableAntialiasing(true);
			dotShape.setAttributes(dotBsa);
			dotLayer.addRenderable(dotShape);
			dotList.add(dotShape);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void removeDot(int index) throws IndexOutOfBoundsException {
		try {
			dotList.remove(index);
			dotLayer.removeAllRenderables();
			for (SurfaceCircle temp : dotList) {
				dotLayer.addRenderable(temp);
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addQuad(Point.Double lonLat, Point.Double size, Color color) {
		LatLon latLon = new LatLon(LatLon.fromDegrees(lonLat.y, lonLat.x));
		quadBsa.setInteriorMaterial(new Material(color));
		quadBsa.setInteriorOpacity(0.3);
		quadBsa.setOutlineMaterial(new Material(color));
		quadBsa.setOutlineOpacity(0.4);
		quadBsa.setOutlineWidth(1);
		quadBsa.setEnableAntialiasing(true);
		SurfaceQuad surfaceQuad = new SurfaceQuad(quadBsa, latLon, size.x, size.y);
		quadLayer.addRenderable(surfaceQuad);
		quadList.add(surfaceQuad);
	}

	@Override
	public void changeQuadColor(int index, Color color) throws IndexOutOfBoundsException {		
		try {
			quadBsa.setInteriorMaterial(new Material(color));
			quadBsa.setInteriorOpacity(0.3);
			quadBsa.setOutlineMaterial(new Material(color));
			quadBsa.setOutlineOpacity(0.4);
			quadBsa.setOutlineWidth(1);
			quadBsa.setEnableAntialiasing(true);
			quadList.get(index).setAttributes(quadBsa);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void removeQuad(int index) throws IndexOutOfBoundsException {
		try {
			quadList.remove(index);
			quadLayer.removeAllRenderables();
			for (SurfaceQuad temp : quadList) {
				quadLayer.addRenderable(temp);
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override	
	public void addLine(Point.Double lonLatStart, double angle, Color color) {
		Point.Double lonLatEnd = new Point.Double();
		lonLatEnd.x = (Math.sin(angle * Math.PI / 180) * 0.5) + lonLatStart.x;
		lonLatEnd.y = (Math.cos(angle * Math.PI / 180) * 0.5) + lonLatStart.y;
		List<LatLon> latLonList = new ArrayList<LatLon>();
		latLonList.add(LatLon.fromDegrees(lonLatStart.y, lonLatStart.x));
		latLonList.add(LatLon.fromDegrees(lonLatEnd.y, lonLatEnd.x));
		SurfacePolyline surfacePolyline = new SurfacePolyline(latLonList);
		surfacePolyline.setAttributes(lineBsa);
		lineLayer.addRenderable(surfacePolyline);
		lineList.add(surfacePolyline);
	}

	@Override
	public void addLine(Point.Double lonLatStart, Point.Double lonLatEnd, Color color) {
		List<LatLon> latLonList = new ArrayList<LatLon>();
		latLonList.add(LatLon.fromDegrees(lonLatStart.y, lonLatStart.x));
		latLonList.add(LatLon.fromDegrees(lonLatEnd.y, lonLatEnd.x));
		SurfacePolyline surfacePolyline = new SurfacePolyline(latLonList);
		lineBsa.setOutlineMaterial(new Material(color));
		lineBsa.setOutlineOpacity(0.8);
		lineBsa.setOutlineWidth(1);
		lineBsa.setEnableAntialiasing(true);
		surfacePolyline.setAttributes(lineBsa);
		lineLayer.addRenderable(surfacePolyline);
		lineList.add(surfacePolyline);
	}

	@Override
	public void removeLine(int index) throws IndexOutOfBoundsException {
		try {
			lineList.remove(index);
			lineLayer.removeAllRenderables();
			for (SurfacePolyline temp : lineList) {
				lineLayer.addRenderable(temp);
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override	
	public void addRing(Point.Double lonLat, double size, Color color) {

		SurfaceCircle ringShape = new SurfaceCircle(LatLon.fromDegrees(lonLat.y,lonLat.x), size);

		ringBsa.setOutlineMaterial(new Material(color));
		ringBsa.setInteriorMaterial(new Material(color));
		ringBsa.setOutlineOpacity(0.8);
		ringBsa.setInteriorOpacity(0);
		ringBsa.setOutlineWidth(1);
		ringBsa.setEnableAntialiasing(true);

		ringShape.setAttributes(ringBsa);

		ringLayer.addRenderable(ringShape);
		ringList.add(ringShape);
	}

	@Override
	public void removeRing(int index) throws IndexOutOfBoundsException {
		try {
			ringList.remove(index);
			ringLayer.removeAllRenderables();
			for (SurfaceCircle temp : ringList) {
				ringLayer.addRenderable(temp);
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	public double getAltitude() {
		return view.getZoom();
	}
	
	public void setAltitude(double altitude) {
		view.setZoom(altitude);
	}
	
	@Override
	public void zoomIn() {
		double zoom = altitudeToZoom(view.getZoom());
		if (zoom <= 17.5)
			view.setZoom(zoomToAltitude(zoom + 0.5));
		else
			view.setZoom(zoomToAltitude(18));
		redimensionMap();
		firePropertyChange(ZOOM_COMPLETE, null, altitudeToZoom(view.getZoom()));
		wwd.redraw();
	}

	@Override
	public void zoomOut() {
		double zoom = altitudeToZoom(view.getZoom());
		if (zoom >= 1.5)
			view.setZoom(zoomToAltitude(zoom - 0.5));
		else
			view.setZoom(zoomToAltitude(1));
		redimensionMap();
		firePropertyChange(ZOOM_COMPLETE, null, altitudeToZoom(view.getZoom()));
		wwd.redraw();
	}

	@Override
	public void showGrid(boolean showGrid) {
		if (this.showGrid != showGrid) {
			if (showGrid)
				model.getLayers().add(gridLayer);
			else
				model.getLayers().remove(gridLayer);
			this.showGrid = showGrid;
		}
	}

	@Override
	public boolean isShowGrid() {
		return showGrid;
	}
	
	@Override
	public void setGridSize(Point.Double gridSize) {
		this.gridSize = gridSize;
		gridLayer.removeAllRenderables();
		createGrid();
	}

	private void createGrid() {
		double degreesPerTileLon = gridSize.x / 3600.0;
		double degreesPerTileLat = gridSize.y / 3600.0;
		double verticalPosition = getMapBottomEdgeLatitude() / degreesPerTileLat; 
		double horizontalEdge = (int) verticalPosition * degreesPerTileLat;
		double horizontalPosition = getMapLeftEdgeLongitude() / degreesPerTileLon;
		double verticalEdge = (int) horizontalPosition * degreesPerTileLon;
		
		for (double i = verticalEdge; i <= getMapRightEdgeLongitude(); i = i + degreesPerTileLon) {
			List<LatLon> latLonList = new ArrayList<LatLon>();
			latLonList.add(LatLon.fromDegrees(getMapTopEdgeLatitude(), i));
			latLonList.add(LatLon.fromDegrees(getMapBottomEdgeLatitude(), i));
			gridPolyline = new SurfacePolyline(latLonList);
			gridPolyline.setAttributes(gridBsa);
			gridLayer.addRenderable(gridPolyline);
		}

		for (double i = horizontalEdge; i <= getMapTopEdgeLatitude(); i = i + degreesPerTileLat) {
			List<LatLon> latLonList = new ArrayList<LatLon>();
			latLonList.add(LatLon.fromDegrees(i, getMapRightEdgeLongitude()));
			latLonList.add(LatLon.fromDegrees(i, getMapLeftEdgeLongitude()));
			gridPolyline = new SurfacePolyline(latLonList);
			gridLayer.addRenderable(gridPolyline);
		}
	}

	@Override
	public int numberOfIcons() {
		return iconList.size();
	}

	@Override
	public void showQuads(boolean showQuads) {
		if (showQuads) model.getLayers().addIfAbsent(quadLayer);
		else model.getLayers().remove(quadLayer);
		this.showQuads = showQuads;
	}

	@Override
	public void showDots(boolean showDots) {
		if (showDots) model.getLayers().addIfAbsent(dotLayer);
		else model.getLayers().remove(dotLayer);
		this.showDots = showDots;
	}

	@Override
	public void showLines(boolean showLines) {
		if (showLines) model.getLayers().addIfAbsent(lineLayer);
		else model.getLayers().remove(lineLayer);
		this.showLines = showLines;
	}

	@Override
	public void showRings(boolean showRings) {
		if (showRings) model.getLayers().addIfAbsent(ringLayer);
		else model.getLayers().remove(ringLayer);
		this.showRings = showRings;
	}

	@Override
	public void setGridColor(Color color) {
		gridColor = color;
		repaint();
	}

	@Override
	public BufferedImage getScreenShot() {
		GLProfile glProfile = GLProfile.getDefault();
		AWTGLReadBufferUtil aWTGLReadBufferUtil = new AWTGLReadBufferUtil(glProfile, true);
		wwd.getContext().makeCurrent();
		GL gl = wwd.getContext().getGL();
		BufferedImage image = aWTGLReadBufferUtil.readPixelsToBufferedImage(gl, 0, 0, getWidth(), getHeight(), true);
		wwd.getContext().release();
		return image;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
	    WorldWindMap clone = (WorldWindMap)super.clone();
	    return clone;
	}

	private double scaleToAltitude(double scale) {
		return 65000.0 - (5000.0 * scale);
	}
	
	@Override
	public void setScale(double scale) {
		view.setZoom(scaleToAltitude(scale));
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
	}

	@Override
	public int getZoom() {
		return (int) Math.round(altitudeToZoom(view.getZoom()));
	}

	@Override
	public void deleteAllArcs() {
		arcList.subList(0, arcList.size()).clear();
	}

	@Override
	public void removeArc(int index) {
		arcList.remove(index);
	}

	@Override
	public void showArcs(boolean showArcs) {
		this.showArcs = showArcs;
	}

	@Override
	public void insertIcon(int index, Point.Double lonLat, String iconPath, String identifier) {
		
	}

	@Override
	public boolean isShowMapImage() {
		return isVisible();
	}

	@Override
	public void showMapImage(boolean showMapImage) {
		setVisible(showMapImage);
	}

	@Override
	public void showArcAsymptotes(boolean showArcAsymptotes) {
		this.showArcAsymptotes = showArcAsymptotes;
	}

	@Override
	public void showArcCursors(boolean showArcCursors) {
		this.showArcCursors = showArcCursors;
	}

	@Override
	public void showArcTrails(boolean showArcTrails) {
		this.showArcTrails = showArcTrails;
	}

	@Override
	public void showGpsSymbol(boolean showGpsSymbol) {
		this.showGpsSymbol = showGpsSymbol;
		gpsShape.setVisible(showGpsSymbol);
		gpsArrow.setVisible(showGpsSymbol);
	}

	@Override
	public void addArc(ConicSection cone, int flight) {
		Color tempTrailColor;
		if (trailEqualsFlightColor) {
			tempTrailColor = arcColors[flight];
		} else {
			tempTrailColor = arcTrailColor;
		}
		slp = new HyperbolicProjection(upperLeftLonLat, lowerRightLonLat, cone, getSize(), 
			arcColors[flight], showArcs, -1, arcAsymptoteColor, showArcAsymptotes, arcCursorDiameter, 
			arcCursorColor, showArcCursors, tempTrailColor, showArcTrails, flight);
		arcList.add(slp);
		add(slp);
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
    public void setArcIntersectPointColor(Color color) {
    	for (SurfaceCircle tempIntersect : arcIntersectList) {
    		Material arcIntersectColor = new Material(color);
    		arcIntersectBsa.setOutlineMaterial(arcIntersectColor);
    		arcIntersectBsa.setInteriorMaterial(arcIntersectColor);
    		tempIntersect.setAttributes(arcIntersectBsa);
    	}
    }
	
	@Override
	public boolean isShowTargetRing() {
		return showTargetRing;
	}

	@Override
	public void setGpsSymbolColor(Color color) {
		Material gpsShapeColor = new Material(color);
		gpsBsa.setOutlineMaterial(gpsShapeColor);
		gpsBsa.setInteriorMaterial(gpsShapeColor);
		gpsShape.setAttributes(gpsBsa);
		gpsArrow.setColor(color);
		wwd.redraw();
	}

	@Override
	public void setGpsSymbolAngle(int angle) {
		if (angle == 360) {
			gpsShape.setVisible(true);
			gpsArrow.setVisible(false);
		} else {
			gpsArrow.setVisible(true);
			gpsShape.setVisible(false);
		}
	}
	
	@Override
	public int getMaxZoom() {
		return (int) altitudeToZoom(4513.988880);
	}

	@Override
	public void setArcCursorDiameter(int arcCursorDiameter) {
		for (int i = 0; i < arcList.size(); i++) {
    		arcList.get(i).setCursorDiameter(arcCursorDiameter);
    	}
		this.arcCursorDiameter = arcCursorDiameter;
	}

	private boolean setupListenersWhenConnected() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame == null) {
            return false;
        }
        parentFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	removeAll();
            	wwd.shutdown();
                WorldWind.shutDown();
            }
        });
        return true;
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
	
}
