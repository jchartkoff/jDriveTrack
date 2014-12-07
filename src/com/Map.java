package com;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;

public interface Map {
    
    public static final String MAP_IMAGE_COMPLETE = "MAP_IMAGE_COMPLETE";
    public static final String ZOOM_COMPLETE = "ZOOM_COMPLETE";
    public static final String SIGNAL_LOCATION_PROJECTION_COMPLETE = "SIGNAL_LOCATION_PROJECTION_COMPLETE";
    public static final String TILE = "TILE";
    public static final String NO_OP = "NO_OP";
    public static final String MAP_RENDERED = "MAP_RENDERED";
    
    boolean isRendered();
    
	void addDot(Point.Double point, double size, Color color);
	
	void addIcon(Point.Double point, String iconPath, String identifier);

	void addLine(Point.Double point, double angle, Color color);

	void addLine(Point.Double pointA, Point.Double pointB, Color color);

	void addQuad(Point.Double point, Point.Double size, Color color);

	void addRing(Point.Double point, double size, Color color);

	void changeQuadColor(int index, Color color) throws IndexOutOfBoundsException;

	Object clone() throws CloneNotSupportedException;

	void deleteAllDots();

	void deleteAllIcons();

	void deleteAllLines();

	void deleteAllQuads();

	void deleteAllRings();

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

	void insertIcon(int index, Point.Double point, String iconPath, String identifier);
	
	boolean isVisible();

	void moveIcon(int index, Point.Double point) throws IndexOutOfBoundsException;

	int numberOfIcons();

	void removeDot(int index) throws IndexOutOfBoundsException;
	
	void removeIcon(int index) throws IndexOutOfBoundsException;

	void removeLine(int index) throws IndexOutOfBoundsException;
	
	void removeQuad(int index) throws IndexOutOfBoundsException;

	void removeRing(int index) throws IndexOutOfBoundsException;
	
	void repaint();

	void revalidate();
	
	void setCenterLonLat(Point.Double point);
	
	void setGridColor(Color gridColor);

	void setGridSize(Point.Double gridSize);

	void setScale(double scale);

	void setSize(Dimension size);
	
	void setVisible(boolean visible);

	void showDots(boolean showDots);
	
	void showGrid(boolean showGrid);

	void showIconLabels(boolean showIconLabels);

	void showIcons(boolean showIcons);

	void showLines(boolean showLines);

	void showQuads(boolean showQuads);

	void showRings(boolean showRings);

	void showTargetRing(boolean showTargetRing);

	void zoomIn();

	void zoomOut();

	void setZoom(int zoom);

	int getZoom();

	boolean isShowGrid();

	void deleteAllArcs();

	void removeArc(int index);

	void addArc(ConicSection cone, int flight);

	void showArcs(boolean show);

	void setGpsDotPosition(Double point);

	void setGpsDotDiameter(double diameter);

	void setGpsDotColor(Color color);

	void setGpsArrowPosition(Double point, int angle);

	void setGpsArrowColor(Color color);

	void setGpsArrowSize(double size);

	void setTargetRingPosition(Double point);

	void setTargetRingDiameter(double diameter);

	void setTargetRingColor(Color color);

	void setTargetRing(Double point, double diameter);

	void setTargetRing(Double point, double diameter, Color color);

	void addPropertyChangeListener(PropertyChangeListener propertyChangeListener);
	
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

	void removePropertyChangeListener(PropertyChangeListener listener);
	
	void addMouseListener(MouseListener listener);
	
	void removeMouseListener(MouseListener listener);
	
	void addKeyListener(KeyListener listener);
	
	void removeKeyListener(KeyListener listener);
	
	void addMouseMotionListener(MouseMotionListener listener);
	
	void removeMouseMotionListener(MouseMotionListener listener);

	boolean isShowGPSDot();

	boolean isShowGPSArrow();

	boolean isShowMapImage();

	void showMapImage(boolean showMapImage);

	void showArcAsymptotes(boolean show);

	void showArcCursors(boolean show);

	void showArcTrails(boolean show);

	void displayShapes(boolean displayShapes);

	void setGpsSymbol(Double point, double diameter, Color color, int angle);
	
	void showGpsSymbol(boolean show);

	boolean isShowTargetRing();

	void setArcAsymptoteColor(Color asymptoteColor);

	void setArcColors(Color[] arcColors);

	void setArcTrailColor(Color arcTrailColor);

	void setArcCursorColor(Color arcCursorColor);

	void deleteAllArcIntersectPoints();

	void addArcIntersectPoint(Double p, double size, Color color);

	void removeArcIntersectPoint(int index);

	void showArcIntersectPoints(boolean show);

	void setArcIntersectPointColor(Color arcIntersectPointColor);

	void setArcCursorDiameter(int arcCursorDiameter);

	void setTrailEqualsFlightColor(boolean trailEqualsFlightColor);

	void setGpsSymbolColor(Color color);

	int getMaxZoom();

	void setGpsSymbolAngle(int angle);

	void showSettings(boolean showSettings);

}
