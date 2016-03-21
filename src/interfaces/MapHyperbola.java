package interfaces;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;
import java.util.List;

import jdrivetrack.MapPolylineImpl;

public interface MapHyperbola {
	
	boolean showArc();
	void showArc(boolean showArc);
	Color getArcColor();
	void setArcColor(Color setArcColor);
	MapPolyline getArcMapPolyline();

    boolean showCursors();
    void showCursors(boolean showCursors);
    double getCursorDiameter();
    void setCursorDiameter(double cursorDiameter);
    Color getCursorColor();
	void setCursorColor(Color cursorColor);
	List<? extends ICoordinate> getCursorPoints();

    boolean showAsymptotes();
    void showAsymptotes(boolean showAsymptotes);
    Color getAsymtoteColor();
	void setAsymptoteColor(Color asymptoteColor);
	MapPolyline getAsymptoteMapPolyline();
    
    boolean showTrace();
    void showTrace(boolean showTrace);
    Color getTraceColor();
    void setTraceColor(Color traceColor);
	MapPolyline getArcTraceMapPolyline();

	void paintPolyline(Graphics g, List<Point> points, Color color, Stroke stroke);
	void paintMarker(Graphics g, Point position, int radius, Color foreColor, Color backColor);
	
}
