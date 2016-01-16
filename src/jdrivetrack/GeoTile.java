package jdrivetrack;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.SurfacePolygon;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class GeoTile extends SurfacePolygon {
	private Position position;
	private Point.Double arcSeconds;
	private List<Position> corners;

	public GeoTile(Point.Double latLon, Point.Double arcSeconds) {
		this(new Position(LatLon.fromDegrees(latLon.y, latLon.x), 0), arcSeconds);
	}
	
	public GeoTile(Position position, Point.Double arcSeconds) {
		this.position = position;
		this.arcSeconds = arcSeconds;
		corners = createCornerIterable(position, arcSeconds);
		super.setOuterBoundary(corners);
	}
	
	private List<Position> createCornerIterable(final Position position, final Point.Double arcSeconds) {
		List<Position> corners = new ArrayList<Position>(4);
		
		corners.add(Position.fromDegrees(position.latitude.getDegrees(), 
				position.longitude.getDegrees()));
		
		corners.add(Position.fromDegrees(position.latitude.getDegrees() + (arcSeconds.y / 3600d), 
				position.longitude.getDegrees()));
		
		corners.add(Position.fromDegrees(position.latitude.getDegrees() + (arcSeconds.y / 3600d), 
				position.longitude.getDegrees() - (arcSeconds.x / 3600d)));
		
		corners.add(Position.fromDegrees(position.latitude.getDegrees(), 
				position.longitude.getDegrees() - (arcSeconds.x / 3600d)));
		
		return corners;
	}
	
	public List<Position> getCorners() {
		return corners;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public Point.Double getPoint() {
		return new Point.Double(position.longitude.degrees, position.latitude.degrees);
	}
	
	public Point.Double getArcSeconds() {
		return arcSeconds;
	}

	public double getLowerLatitude() {
		return position.latitude.getDegrees();
	}
	
	public double getUpperLatitude() {
		return position.latitude.getDegrees() + (arcSeconds.y / 3600d);
	}
	
	public double getLeftLongitude() {
		return position.longitude.getDegrees() - (arcSeconds.x / 3600d);
	}
	
	public double getRightLongitude() {
		return position.longitude.getDegrees();
	}
}
