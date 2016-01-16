package interfaces;

import java.awt.Graphics;
import java.awt.Point;

import jdrivetrack.Coordinate;

public interface MapMarker extends MapObject, ICoordinate{

    enum STYLE {
        FIXED,
        VARIABLE
    }

    Coordinate getCoordinate();

    @Override
    double getLat();

    @Override
    double getLon();

    double getRadius();
    
    STYLE getMarkerStyle();

    void paint(Graphics g, Point position, int radius);
}
