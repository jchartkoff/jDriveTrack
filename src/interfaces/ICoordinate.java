package interfaces;

import java.awt.geom.Point2D;

public interface ICoordinate {

    double getLat();

    void setLat(double lat);

    double getLon();

    void setLon(double lon);
    
    Point2D.Double getLonLat();
}
