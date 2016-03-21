package jdrivetrack;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import interfaces.MapInterface;
import types.GeoTile;
import types.MapDimension;
import types.StaticMeasurement;

public class ImageMap extends JPanel implements MapInterface, Cloneable {
	private static final long serialVersionUID = 1L;
	
	private Color gpsDotColor;
	private double gpsDotSize;
	private Dot gpsDot;
	private Ring targetRing;
	private Arrow2D gpsArrow;
	private boolean showGpsSymbol = false;
	private boolean showTargetRing = false;
	private Point.Double gpsDotPosition = new Point.Double();
	private Point.Double targetRingPosition = new Point.Double();
	private double targetRingSize;
	private Color targetRingColor;
	private int gpsArrowAngle;
	private BufferedImage bimage;
	private int translateX = 0;
	private int translateY = 0;
	private double scale = 1.0;
	private Point.Double upperLeftPosition = new Point.Double();
	private Point.Double lowerRightPosition = new Point.Double();
	private Point.Double mapCenterPosition = new Point.Double();
	private Point.Double mapSizeDegrees = new Point.Double();
	private String mapFileName;
	private Point.Double gridSize;
	private Color gridColor;
	private boolean showTestGrid;
	private boolean showIcons;
	private boolean showQuads = false;
	private boolean showLines = false;
	private boolean showRings = false;
	private boolean showDots = false;
	private Dimension mapSize = new Dimension(800,600);
	private Point mapPosition = new Point();
    private Point.Double mouseCoordinates = new Point.Double();
	private DragListener mouseListener = new DragListener();
	private KeyPressedListener keyPressedListener = new KeyPressedListener();
	private ImageMapSelector imageMapSelector = new ImageMapSelector();
	private List<Line> lineList;
	private List<Dot> dotList;
	private List<Ring> ringList;
	private List<Icon> iconList;
	private List<PolygonArrayLayer> quadList;
	private List<ChangeListener> mouseMovedChangeListeners;
	private List<ChangeListener> mouseLeftButtonClickedChangeListeners;
	private List<ChangeListener> mouseRightButtonClickedChangeListeners;
	private List<ChangeListener> spaceKeyPressedChangeListeners;
	private List<ChangeListener> leftArrowKeyPressedChangeListeners;
	private List<ChangeListener> rightArrowKeyPressedChangeListeners;
	private List<ChangeListener> enterKeyPressedChangeListeners;
	private ReadCsvFile iniFile;
	private int selectedMapIndex;
	private Preferences userPref;
	private Point.Double position = new Point.Double();
	private Point.Double edge = new Point.Double();
	private Point.Double reference = new Point.Double();
	
	public ImageMap() {
		this(System.getProperty("user.dir") + "\\maps\\" + "DefaultImageMap.jpg", new Point.Double(-86.6987, 30.5685), new Point.Double(-86.3995, 30.3760));
	}
	
	public ImageMap(int startUpMapIndex) {
		try {
			iniFile = new ReadCsvFile(System.getProperty("user.dir") + "\\maps\\" + "map.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
		retrieveMapProperties(startUpMapIndex);
		initComponents();
	}
	
	public ImageMap(String mapFileName, Point.Double upperLeftPosition, Point.Double lowerRightPosition) {
		this.upperLeftPosition = upperLeftPosition;
		this.lowerRightPosition = lowerRightPosition;
		this.mapFileName = mapFileName.trim();
		initComponents();
	}
	
	private void initComponents() {
		setMapImage(mapFileName, upperLeftPosition, lowerRightPosition);
		setMapDimensions();
		setOpaque(true);
		setDoubleBuffered(true);
		setVisible(true);
		setFocusable(true);
		setFocusTraversalKeysEnabled(true);
		
		dotList = new ArrayList<>();
		lineList = new ArrayList<>();
		ringList = new ArrayList<>();
		iconList = new ArrayList<>();
		quadList = new ArrayList<>();
		gpsDot = new Dot(mapSize);
		gpsArrow = new Arrow2D(mapSize);
		targetRing = new Ring(mapSize);
		
		userPref = Preferences.userRoot().node(this.getClass().getName());
		
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);
		addKeyListener(keyPressedListener);
				
		imageMapSelector.addPropertiesChangedChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				imageMapSelectorPropertiesChangedListenerEvent(event);
			}
		});
	}
	
	protected void imageMapSelectorPropertiesChangedListenerEvent(ChangeEvent event) {
		try {
			iniFile = new ReadCsvFile(System.getProperty("user.dir") + "\\maps\\" + "map.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
		selectedMapIndex = userPref.getInt("SelectedMapIndex", -1);
		retrieveMapProperties(selectedMapIndex);
		setMapImage(mapFileName, upperLeftPosition, lowerRightPosition);
	}

	private void setMapDimensions() {
		mapCenterPosition = new Point.Double((Math.abs(upperLeftPosition.x - lowerRightPosition.x) / 2.0) + upperLeftPosition.x,
				(Math.abs(upperLeftPosition.y - lowerRightPosition.y) / 2.0) + lowerRightPosition.y);
		mapSizeDegrees = new Point.Double(Math.abs(upperLeftPosition.x - lowerRightPosition.x), Math.abs(upperLeftPosition.y - lowerRightPosition.y));

	}
	
	private void retrieveMapProperties(int mapIndex) {
		try {
			mapFileName = System.getProperty("user.dir") + "\\maps\\" + iniFile.getRow(mapIndex)[4].trim();
			upperLeftPosition.y = Double.parseDouble(iniFile.getRow(mapIndex)[6]);
			upperLeftPosition.x = Double.parseDouble(iniFile.getRow(mapIndex)[5]);
			lowerRightPosition.y = Double.parseDouble(iniFile.getRow(mapIndex)[8]);
			lowerRightPosition.x = Double.parseDouble(iniFile.getRow(mapIndex)[7]);
		} catch (IndexOutOfBoundsException | NullPointerException ex) {
			upperLeftPosition.y = 30.5685;
			upperLeftPosition.x = -86.6987;
			lowerRightPosition.y = 30.3760;
			lowerRightPosition.x = -86.3995;
			mapFileName = System.getProperty("user.dir") + "\\maps\\" + "DefaultImageMap.jpg";
		} 
		setMapDimensions();
	}
	
	private void setMapImage(String newMapFileName, Point.Double newUpperLeftPosition,
			Point.Double newLowerRightPosition) {
		try {
			mapFileName = newMapFileName;
			Path path = Paths.get(mapFileName);
			if (Files.exists(path))
				bimage = ImageIO.read(new File(mapFileName));
			else
				// TODO: insert default image here
			repaint();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void setTileSize(Point.Double gridSize) {
		this.gridSize = gridSize;
		repaint();
	}
	
	@Override
	public void showGrid(boolean newShowGrid) {
		showTestGrid = newShowGrid;
		repaint();
	}
	
	@Override
	public boolean isShowGrid() {
		return showTestGrid;
	}
	
	@Override
	public double getMapLeftEdgeLongitude() {
		return upperLeftPosition.x;
	}
	
	@Override
	public double getMapRightEdgeLongitude() {
		return lowerRightPosition.x;
	}
	
	@Override
	public double getMapTopEdgeLatitude() {
		return upperLeftPosition.y;
	}
	
	@Override
	public double getMapBottomEdgeLatitude() {
		return lowerRightPosition.y;
	}

	@Override
	public void showIconLabels(boolean newShowIconLabels) {
		repaint();
	}

	@Override
	public void deleteAllSignalMarkers() {
		dotList.subList(0, dotList.size()).clear();
		repaint();
	}

	@Override
	public void deleteAllQuads() {
		quadList.subList(0, quadList.size()).clear();
		repaint();
	}

	@Override
	public void deleteAllRings() {
		ringList.subList(0, ringList.size()).clear();
		repaint();
	}
	
	@Override
	public void deleteAllIcons() {
		iconList.subList(0, iconList.size()).clear();
		repaint();
	}
	
	@Override
	public void deleteAllLines() {
		lineList.subList(0, lineList.size()).clear();
		repaint();
	}

	@Override
	public void setGpsSymbol(Point.Double gpsDotPosition, double size, Color color, int angle) {
		this.gpsDotPosition = gpsDotPosition;
		this.gpsDotSize = size;
		this.gpsDotColor = color;
		this.gpsArrowAngle = angle;
		repaint();
	}
	
	@Override
	public void showTargetRing(boolean newShowTargetRing) {
		showTargetRing = newShowTargetRing;
		repaint();
	}
	
	@Override
	public void setTargetRing(Point.Double newPoint, double size, Color color) {
		targetRingPosition = newPoint;
		targetRingSize = size;
		targetRingColor = color;
		repaint();
	}
	
	@Override
	public void addSignalMarker(Point.Double point, double diameter, Color color) {
		Dot newDot = new Dot(upperLeftPosition, lowerRightPosition, point, diameter, mapSize, color);
		dotList.add(newDot);
		repaint();
	}

	@Override
	public void deleteSignalMarker(int index) {
		try {
			dotList.remove(index);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addQuad(Point.Double point, Point.Double size, Color color) {
		PolygonArrayLayer quad = new PolygonArrayLayer(upperLeftPosition, lowerRightPosition, point, size, mapSize, color);
		quadList.add(quad);
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
	public void deleteQuad(int index) {
		try {
			quadList.remove(index);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addLine(Point.Double point, double angle, Color color) {
		Line line = new Line(upperLeftPosition, lowerRightPosition, point, angle, mapSize, color);
		lineList.add(line);
		repaint();
	}
	
	@Override
	public void addLine(Point.Double pointStart, Point.Double pointEnd, Color color) {
		Line line = new Line(upperLeftPosition, lowerRightPosition, pointStart, pointEnd, mapSize, color);
		lineList.add(line);
		repaint();
	}

	@Override
	public void hideLine(int index) {
		try {
			lineList.remove(index);
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void addIcon(Point.Double point, String iconPath, String identifier) {
		Icon icon = new Icon(upperLeftPosition, lowerRightPosition, point, iconPath, identifier, mapSize);
		iconList.add(icon);
	}

	@Override
	public void showIcons(boolean showIcons) {
		this.showIcons = showIcons;
		repaint();
	}
	
	@Override
	public void moveIcon(int index, Point.Double point) {
		try {
			String iconPath = iconList.get(index).getIconPathName();
			String iconLabel = iconList.get(index).getIconLabel();
			iconList.remove(index);
			Icon icon = new Icon(upperLeftPosition, lowerRightPosition, point, iconPath, iconLabel, mapSize);
			iconList.add(index, icon);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void hideIcon(int index) {
		try {
			iconList.remove(index);
			repaint();
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void addRing(Point.Double point, double size, Color color) {
		Ring ring = new Ring(upperLeftPosition, lowerRightPosition, point, size, mapSize, color);
		ringList.add(ring);
	}

	@Override
	public void hideRing(int index) {
		try {
			ringList.remove(index);
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void zoomIn() {
		scale += 0.1;
		scale = Math.max(0.00001, scale);
		redimensionMap();
	}
	
	@Override
	public void zoomOut() {
		scale -= 0.1;
		scale = Math.max(1, scale);
		redimensionMap();
	}

	private void redimensionMap() {
		upperLeftPosition.y = mapCenterPosition.y + ((mapSizeDegrees.y / 2.0) / scale);
		lowerRightPosition.y = mapCenterPosition.y - ((mapSizeDegrees.y / 2.0) / scale);
		upperLeftPosition.x = mapCenterPosition.x - ((mapSizeDegrees.x / 2.0) / scale);
		lowerRightPosition.x = mapCenterPosition.x + ((mapSizeDegrees.x / 2.0) / scale);
	}
	
	//TODO
	private int scaleToZoom(double scale) {
		return 6;
	}
	//TODO
	private double zoomToScale(int zoom) {
		return 1.0;
	}

	private int longitudeToX(double newLongitude) {
		double leftToRightDegrees = Math.abs(lowerRightPosition.x - upperLeftPosition.x);
		return (int) Math.round(mapSize.width - ((lowerRightPosition.x - newLongitude) * (mapSize.width / leftToRightDegrees)));
	}

	private int latitudeToY(double newLatitude) {
		double topToBottomDegrees = Math.abs(upperLeftPosition.y - lowerRightPosition.y);
		return (int) Math.round((upperLeftPosition.y - newLatitude) * (mapSize.height / topToBottomDegrees));
	}
	
	private double xToLongitude(int x) {
		double leftToRightDegrees = Math.abs(upperLeftPosition.x - lowerRightPosition.x);
		return ((x / (double) mapSize.width * leftToRightDegrees) + upperLeftPosition.x);
	}

	private double yToLatitude(int y) {
		double topToBottomDegrees = Math.abs(upperLeftPosition.y - lowerRightPosition.y);
		return (((y / (double) mapSize.height) * topToBottomDegrees) - upperLeftPosition.y) * -1.0;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		try {
			AffineTransform tx = new AffineTransform();
			tx.scale(scale, scale);
			if (scale != 1.0) tx.translate(translateX, translateY);

			position.y = upperLeftPosition.y / (gridSize.y / 3600.0); 
			position.x = upperLeftPosition.x / (gridSize.x / 3600.0);
			edge.y = (int) position.y * (gridSize.y / 3600.0);
			edge.x = (int) position.x * (gridSize.x / 3600.0);
	        reference.x = longitudeToX(edge.x);
	        reference.y = latitudeToY(edge.y);
        
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.transform(tx);
			g2.translate(((mapSize.width / 2.0) / scale) - (mapSize.width / 2.0), 
					((mapSize.height / 2.0) / scale) - (mapSize.height / 2.0));
			if (bimage != null) {
				g2.drawImage(bimage, 0, 0, null);
			}
	
			if (showTestGrid) {
				double pixelsPerDegreeLon = mapSize.width / Math.abs(upperLeftPosition.x - lowerRightPosition.x);
				double pixelsPerTileLon = Math.max(pixelsPerDegreeLon * gridSize.x / 3600.0, 5.0);
				for (double i = reference.x; i <= mapSize.width; i = i + pixelsPerTileLon) {
					Line2D.Double line = new Line2D.Double(i, 0, i, mapSize.height);
					g2.setColor(gridColor);
					g2.draw(line);
				}
				double pixelsPerDegreeLat = mapSize.height / Math.abs(upperLeftPosition.y - lowerRightPosition.y);
				double pixelsPerTileLat = Math.max(pixelsPerDegreeLat * gridSize.y / 3600.0, 5.0);
				for (double i = reference.y; i <= mapSize.height; i = i + pixelsPerTileLat) {
					Line2D.Double line = new Line2D.Double(0, i, mapSize.width, i);
					g2.setColor(gridColor);
					g2.draw(line);
				}
			}
	
			if (gpsDot != null && showGpsSymbol) {
				if (gpsArrowAngle == 360) {
					gpsDot.setLocation(gpsDotPosition);
					gpsDot.setDiameter(gpsDotSize);
					gpsDot.setColor(gpsDotColor);
					add(gpsDot);
				} else {
					gpsArrow.setLocation(gpsDotPosition, gpsArrowAngle);
					gpsArrow.setArrowSize(gpsDotSize);
					gpsArrow.setColor(gpsDotColor);
					add(gpsArrow);
				}
			}
	
			if (targetRing != null && showTargetRing) {
				targetRing.setLocation(targetRingPosition);
				targetRing.setDiameter(targetRingSize);
				targetRing.setColor(targetRingColor);
				add(targetRing);
			}
			
			if (!iconList.isEmpty() && showIcons) {
				for (Icon tempIcons : iconList) {
					add(tempIcons);
				}
			}
			
			if (!quadList.isEmpty() && showQuads) {
				for (PolygonArrayLayer tempQuads : quadList) {
					add(tempQuads);
				}
			}
			
			if (!dotList.isEmpty() && showDots) {
				for (Dot tempDots : dotList) {
					add(tempDots);
				}
			}
	
			if (!lineList.isEmpty() && showLines) {
				for (Line tempLines : lineList) {
					add(tempLines);
				}
			}
	
			if (!ringList.isEmpty() && showRings) {
				for (Ring tempRings : ringList) {
					add(tempRings);
				}
			}
		} finally {
			g2.dispose();
		}
	}
	
	private void mousePositionChanged() {
		if (mouseMovedChangeListeners != null) {
			for (ChangeListener cl : mouseMovedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}

	private void mouseLeftButtonClicked() {
		if (mouseLeftButtonClickedChangeListeners != null) {
			for (ChangeListener cl : mouseLeftButtonClickedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}
	
	private void mouseRightButtonClicked() {
		if (mouseRightButtonClickedChangeListeners != null) {
			for (ChangeListener cl : mouseRightButtonClickedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}
	
	private void enterKeyPressedEvent() {
		if (enterKeyPressedChangeListeners != null) {
			for (ChangeListener cl : enterKeyPressedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}
	
	private void spaceKeyPressedEvent() {
		if (spaceKeyPressedChangeListeners != null) {
			for (ChangeListener cl : spaceKeyPressedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}
	
	private void leftArrowKeyPressedEvent() {
		if (leftArrowKeyPressedChangeListeners != null) {
			for (ChangeListener cl : leftArrowKeyPressedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}
	
	private void rightArrowKeyPressedEvent() {
		if (rightArrowKeyPressedChangeListeners != null) {
			for (ChangeListener cl : rightArrowKeyPressedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}
	
	@Override
	public Point.Double getMouseLonLat() {
		return mouseCoordinates;
	}

	@Override
	public int numberOfIcons() {
		return iconList.size();
	}

	@Override
    public double getScale() {
        return scale;
    }

	@Override
    public void setScale(double scale) {
    	this.scale = scale;
    	scale = 1.0;
    	redimensionMap();
    }
  
	@Override
    public int getZoom() {
        return scaleToZoom(scale);
    }

	@Override
    public void setZoom(int zoom) {
    	scale = zoomToScale(zoom);
    	redimensionMap();
    }
	
	@Override
	public Point.Double getCenterLonLat() {
		Point.Double point = new Point.Double(0.0, 0.0);
		point.y = upperLeftPosition.y - (Math.abs(upperLeftPosition.y - lowerRightPosition.y) / 2);
		point.x = upperLeftPosition.x + (Math.abs(upperLeftPosition.x - lowerRightPosition.x) / 2);
		return point;
	}

	@Override
	public void setCenterLonLat(Point.Double point) {
		translateX = (int) point.x;
		translateY = (int) point.y;
		repaint();
	}

	@Override
	public void setSize(Dimension newMapSize) {
		mapSize = newMapSize;
		repaint();
	}

	@Override
	public Dimension getSize() {
		return mapSize;
	}

	@Override
	public void showQuads(boolean show) {
		showQuads = show;
		repaint();
	}

	@Override
	public void showSignalMarkers(boolean show) {
		showDots = show;
		repaint();
	}

	@Override
	public void showLines(boolean show) {
		showLines = show;
		repaint();
	}

	@Override
	public void showRings(boolean show) {
		showRings = show;
		repaint();
	}

	@Override
	public void setGridColor(Color color) {
		gridColor = color;
		repaint();
	}
	
	private class DragListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
		private Point downCoords;
        private Point downPosition;

        public DragListener() {
            new Point();
        }
        
        @Override
		public void mouseClicked(MouseEvent e) {
        	if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 2) {
        		imageMapSelector.showSettingsDialog(true);
        	} else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
        		mouseLeftButtonClicked();
        	} else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
        		mouseRightButtonClicked();
        	}
        }

        @Override
		public void mousePressed(MouseEvent e) {
        	if (e.getButton() == MouseEvent.BUTTON1) {
                downCoords = e.getPoint();
                downPosition = mapPosition;
        	}
        }

        @Override
		public void mouseReleased(MouseEvent e) {
        	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            handleDrag(e);
            downCoords = null;
            downPosition = null;
        }

        @Override
		public void mouseMoved(MouseEvent e) {
            handlePosition(e);
        }

        @Override
		public void mouseDragged(MouseEvent e) {
        	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            handlePosition(e);
            handleDrag(e);
        }
 
        @Override
		public void mouseExited(MouseEvent e) {
        	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
		public void mouseEntered(MouseEvent me) {
            super.mouseEntered(me);
        }
	
		private void handlePosition(MouseEvent e) {
			e.getPoint();
            mouseCoordinates.x = xToLongitude(e.getX()); 
            mouseCoordinates.y = yToLatitude(e.getY());
            mousePositionChanged();
        }
		
		private void handleDrag(MouseEvent e) {
            if (downCoords != null) {
                int tx = downCoords.x - e.getX();
                int ty = downCoords.y - e.getY();
                translateX = downPosition.x - tx;
                translateY = downPosition.y - ty;
                repaint();
            }
		}

	}

    private class KeyPressedListener extends KeyAdapter implements KeyListener {
		public KeyPressedListener() {}
		@Override
		public void keyPressed(KeyEvent ke) {
			if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
				spaceKeyPressedEvent();
			}
			if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
				leftArrowKeyPressedEvent();
			}
			if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightArrowKeyPressedEvent();
			}
			if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
				enterKeyPressedEvent();
			}
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
	    ImageMap clone = (ImageMap)super.clone();
	    return clone;
	}

	@Override
	public BufferedImage getScreenShot() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		paint(image.createGraphics());
		return image;
	}

	@Override
	public Point.Double getGridSize() {
		return gridSize;
	}

	@Override
	public void deleteAllArcs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeArc(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addArc(StaticMeasurement sma, StaticMeasurement smb, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Point.Double> getArcPointList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.awt.geom.Point2D.Double getPixelResolutionInDegrees() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showArcs(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertIcon(int index, java.awt.geom.Point2D.Double point,
			String iconPath, String identifier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGpsShapePosition(java.awt.geom.Point2D.Double point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGpsDotRadius(double diameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGpsDotColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGpsSymbolPosition(java.awt.geom.Point2D.Double point,
			int angle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGpsArrowColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGpsArrowSize(double size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTargetRingPosition(java.awt.geom.Point2D.Double point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTargetRingDiameter(double diameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTargetRingColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTargetRing(java.awt.geom.Point2D.Double point,
			double diameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Hyperbola2D getHyperbola() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isShowGPSDot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowGPSArrow() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowMapImage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void showMapImage(boolean showMapImage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showArcAsymptotes(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showArcCursors(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showArcTrails(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayShapes(boolean displayShapes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showGpsSymbol(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.awt.geom.Point2D.Double getMouseCoordinates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addArc(ConicSection cone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showArcTrace(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isShowTargetRing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setArcAsymptoteColor(Color asymptoteColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcColors(Color[] arcColors) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcTraceColor(Color arcTrailColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcCursorColor(Color arcCursorColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllArcIntersectPoints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addArcIntersectPoint(java.awt.geom.Point2D.Double ip, double diameter, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showArcIntersectPoints(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcIntersectPointColor(Color arcIntersectPointColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcCursorDiameter(double diameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTraceEqualsFlightColor(boolean trailEqualsFlightColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGpsSymbolColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxZoom() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setGpsSymbolAngle(int angle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showStatusBar(boolean showStatusBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showBulkDownloaderPanel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showStatisticsPanel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showLayerSelectorPanel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addArcIntersectPoints(List<java.awt.geom.Point2D.Double> arcIntersectList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcIntersectPoints(List<java.awt.geom.Point2D.Double> arcIntersectList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addArcIntersectPoints(List<java.awt.geom.Point2D.Double> iplist, double diameter, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcIntersectPoints(List<java.awt.geom.Point2D.Double> iplist, double diameter, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setQuadVisible(int index, boolean isVisible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPolygon(GeoTile geotile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteTestTile(int index) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTestTileVisible(int index, boolean isVisible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeTestTileColor(int index, Color color) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllTestTiles() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showPolygons(boolean showPolygons) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isShowSignalMarkers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSignalMarkerDiameter(double diameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcTraceDiameter(double diameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArcIntersectPointDiameter(double diameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isShowPolygons() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowLines() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowRings() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowArcIntersectPoints() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addSignalMarker(java.awt.geom.Point2D.Double pt, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.awt.geom.Rectangle2D.Double getMapRectangle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapDimension getMapDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.awt.geom.Point2D.Double getMapLowerRightCorner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void redraw() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMinZoom() {
		// TODO Auto-generated method stub
		return 0;
	}

}