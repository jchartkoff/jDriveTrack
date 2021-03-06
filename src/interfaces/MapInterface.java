package interfaces;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.List;

import jdrivetrack.AttributionSupport;
import jdrivetrack.ConicSection;
import types.GeoTile;
import types.MapDimension;
import types.StaticMeasurement;
import types.TestTile;

public interface MapInterface {
	
	public static final String MOUSE_OFF_GLOBE = "MOUSE_OFF_GLOBE";
	public static final String MAP_READY = "MAP_READY";
	
	void addSignalMarker(Point.Double point, double size, Color color);
	
	void addIcon(Point.Double point, String iconPath, String identifier);

	void addLine(Point.Double point, double angle, double distance, Color color);

	void addLine(Point.Double pointA, Point.Double pointB, Color color);

	void addQuad(Point.Double point, Point.Double size, Color color);

	void changeQuadColor(int index, Color color);

	Object clone() throws CloneNotSupportedException;

	void deleteAllSignalMarkers();

	void deleteAllIcons();

	void deleteAllLines();

	void deleteAllQuads();

	Point.Double getCenterLonLat();

	Point.Double getGridSize();

	double getMapBottomEdgeLatitude();

	double getMapLeftEdgeLongitude();

	double getMapRightEdgeLongitude();

	double getMapTopEdgeLatitude();

	Point.Double getMouseCoordinates();

	Point getMousePosition();

	double getScale();

	BufferedImage getScreenShot();

	Dimension getSize();

	boolean isVisible();

	void moveIcon(int index, Point.Double point);

	int numberOfIcons();

	void deleteSignalMarker(int index);
	
	void deleteQuad(int index);
	
	void repaint();

	void revalidate();
	
	void setCenterLonLat(Point.Double point);
	
	void setGridColor(Color gridColor);

	void setTileSize(Point.Double gridSize);

	void setScale(double scale);

	void setSize(Dimension size);
	
	void setVisible(boolean visible);

	void showSignalMarkers(boolean showDots);
	
	void showGrid(boolean showGrid);

	void showIconLabels(boolean showIconLabels);

	void showIcons(boolean showIcons);

	void showLines(boolean showLines);

	void showQuads(boolean showQuads);

	void showRings(boolean showRings);

	void showTargetRing(boolean showTargetRing);

	void zoomIn();
	
	void zoomIn(Point pivot);

	void zoomOut();
	
	void zoomOut(Point pivot);

	void setZoom(int zoom);
	
	void setZoom(int zoom, Point pivot);

	int getZoom();

	boolean isShowGrid();

	void deleteAllArcs();

	void removeArc(int index);

	void showArcs(boolean show);

	void setGpsDotRadius(double radius);

	void setGpsDotColor(Color color);

	void setGpsSymbolPosition(Double point, int angle);

	void setGpsArrowColor(Color color);

	void setTargetRingPosition(Double point);

	void setTargetRingRadius(double radius);

	void setTargetRingColor(Color color);

	void setTargetRing(Double point, double radius);

	void setTargetRing(Double point, double radius, Color color);

	void addPropertyChangeListener(PropertyChangeListener propertyChangeListener);
	
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

	void removePropertyChangeListener(PropertyChangeListener listener);
	
	void addMouseListener(MouseListener listener);
	
	void removeMouseListener(MouseListener listener);
	
	void addKeyListener(KeyListener listener);
	
	void removeKeyListener(KeyListener listener);
	
	void addMouseMotionListener(MouseMotionListener listener);
	
	void removeMouseMotionListener(MouseMotionListener listener);

	void addMouseWheelListener(MouseWheelListener listener);
	
	void removeMouseWheelListener(MouseWheelListener listener);
	
	boolean isShowGPSDot();

	boolean isShowGPSArrow();

	boolean isShowMapImage();

	void showMapImage(boolean showMapImage);

	void showArcAsymptotes(boolean show);

	void showArcCursors(boolean show);

	void showArcTrace(boolean show);

	void displayShapes(boolean displayShapes);

	void setGpsSymbol(Double point, double radius, Color color, int angle);
	
	void showGpsSymbol(boolean show);

	boolean isShowTargetRing();

	void setArcAsymptoteColor(Color asymptoteColor);

	void setArcColors(Color[] arcColors);

	void setArcTraceColor(Color arcTrailColor);

	void setArcCursorColor(Color arcCursorColor);

	void deleteAllArcIntersectPoints();

	void addArcIntersectPoint(Double ip, double radius, Color color);

	void showArcIntersectPoints(boolean show);

	void setArcIntersectPointColor(Color arcIntersectPointColor);

	void setArcCursorRadius(double radius);

	void setTraceEqualsFlightColor(boolean trailEqualsFlightColor);

	void setGpsSymbolColor(Color color);

	int getMaxZoom();

	void setGpsSymbolAngle(int angle);

	void showStatusBar(boolean showStatusBar);

	void showBulkDownloaderPanel();

	void showStatisticsPanel();

	void showLayerSelectorPanel();

	void addArcIntersectPoints(List<Double> arcIntersectList);

	void setArcIntersectPoints(List<Double> arcIntersectList);
	
	void addArcIntersectPoints(List<Double> iplist, double radius, Color color);
	
	void setArcIntersectPoints(List<Double> iplist, double radius, Color color);

	void setQuadVisible(int index, boolean isVisible);

	void deleteAllTestTiles();

	void showTestTiles(boolean showTestTiles);

	boolean isShowSignalMarkers();

	void setSignalMarkerRadius(double radius);
	
	void setArcTraceRadius(double radius);
	
	void setArcIntersectPointRadius(double radius);

	boolean isShowTestTiles();

	boolean isShowLines();

	boolean isShowRings();

	boolean isShowArcIntersectPoints();

	void addSignalMarker(Point.Double point, Color color);

	void shutDown();

	java.awt.geom.Rectangle2D.Double getMapRectangle();

	MapDimension getMapDimension();

	Double getMapLowerRightCorner();

	void redraw();

	Dimension getPreferredSize();

	int getMinZoom();
	
	void moveMap(int diffx, int diffy);

	void removeJMVListener(JMapViewerEventListener listener);

	void addJMVListener(JMapViewerEventListener listener);

	AttributionSupport getAttribution();

	void setCursor(Cursor cursor);

	double getMeterPerPixel();

	void deleteAllRings();

	void addRing(Double coord, double radius, Color color);

	void removeRing(int index);

	void restoreCache();

	void adviseMouseOffGlobe();

	void handlePosition(MouseEvent e);

	void setGridReference(Double gridReference);

	void setGridSize(Double gridFieldDimension);
	
	void clearCache();

	void setDisplayToFitMapElements(boolean gpsMarker, boolean signalMarkers, boolean testTiles, boolean rings);

	void addArc(StaticMeasurement sma, StaticMeasurement smb, int unit);

	void addArc(ConicSection cone);

	int getCurrentSignalMarkerIndex();

	int getCurrentIconIndex();

	int getCurrentQuadIndex();

	int getCurrentRingIndex();

	int getCurrentLineIndex();

	void deleteLine(int index);

	void deleteCurrentLine();

	void deleteCurrentQuad();

	void deleteRing(int index);

	void deleteCurrentRing();

	void deleteIcon(int index);

	void deleteCurrentIcon();

	void deleteArc(int index);

	void deleteCurrentArc();

	void deleteTestTile(TestTile testTile);

	void addTestTile(GeoTile geoTile, Color color, int id);

	void changeTestTileColor(TestTile testTile, Color color) throws IndexOutOfBoundsException;

}
