package jdrivetrack;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import interfaces.MapMarker;
import types.Coordinate;
import types.Layer;
import types.Style;

public class MapMarkerCircle extends MapObjectImpl implements MapMarker {

    private Coordinate coord;
    private double radius;
    private STYLE markerStyle;

    public MapMarkerCircle(Coordinate coord, double radius) {
        this(null, null, coord, radius);
    }

    public MapMarkerCircle(String name, Coordinate coord, double radius) {
        this(null, name, coord, radius);
    }

    public MapMarkerCircle(Layer layer, Coordinate coord, double radius) {
        this(layer, null, coord, radius);
    }

    public MapMarkerCircle(double lat, double lon, double radius) {
        this(null, null, new Coordinate(lat, lon), radius);
    }

    public MapMarkerCircle(Layer layer, double lat, double lon, double radius) {
        this(layer, null, new Coordinate(lat, lon), radius);
    }

    public MapMarkerCircle(Layer layer, String name, Coordinate coord, double radius) {
        this(layer, name, coord, radius, STYLE.VARIABLE, getDefaultStyle());
    }

    public MapMarkerCircle(Layer layer, String name, Coordinate coord, double radius, STYLE markerStyle, Style style) {
        super(layer, name, style);
        this.markerStyle = markerStyle;
        this.coord = coord;
        this.radius = radius;
    }

    @Override
    public Coordinate getCoordinate() {
        return coord;
    }

    @Override
    public double getLat() {
        return coord.getLat();
    }

    @Override
    public double getLon() {
        return coord.getLon();
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public STYLE getMarkerStyle() {
        return markerStyle;
    }
    
    public void setRadius(double radius) {
    	this.radius = radius;
    }
    
    @Override
    public void paint(Graphics g, Point position, int radius) {
        int size_h = radius;
        int size = size_h * 2;

        if (g instanceof Graphics2D && getBackColor() != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g.fillOval(position.x - size_h, position.y - size_h, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(getColor());
        g.drawOval(position.x - size_h, position.y - size_h, size, size);

        if (getLayer() == null || getLayer().isVisibleTexts()) paintText(g, position);
    }

    public static Style getDefaultStyle() {
        return new Style(new Color(256,0,0,128), new Color(256,0,0,64), null, getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapMarker at " + getLat() + " " + getLon();
    }

    @Override
    public void setLat(double lat) {
        if (coord == null) coord = new Coordinate(lat, 0);
        else coord.setLat(lat);
    }

    @Override
    public void setLon(double lon) {
        if (coord == null) coord = new Coordinate(0, lon);
        else coord.setLon(lon);
    }

	@Override
	public Point2D.Double getLonLat() {
		return new Point2D.Double(coord.getLon(), coord.getLat());
	}

}
