package types;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.SurfacePolygon;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class GeoTile extends SurfacePolygon {
	private Position position;
	private Point.Double arcSeconds;
	private List<Position> positions;
	private List<Coordinate> coordinates;
	private Coordinate coordinate;

	public GeoTile(final Point.Double lonLat, final Point.Double arcSeconds) {
		this(new Position(LatLon.fromDegrees(lonLat.y, lonLat.x), 0), arcSeconds);
		coordinates = createCoordinatesIterable(new Coordinate(lonLat.y, lonLat.x), arcSeconds);
	}

	public GeoTile(final Position position, final Point.Double arcSeconds) {
		this.position = position;
		this.arcSeconds = arcSeconds;
		positions = createPositionsIterable(position, arcSeconds);
		super.setOuterBoundary(positions);
	}
	
	private List<Position> createPositionsIterable(final Position position, final Point.Double arcSeconds) {
		List<Position> positions = new ArrayList<Position>(4);
		
		positions.add(Position.fromDegrees(position.latitude.getDegrees(), 
				position.longitude.getDegrees()));
		
		positions.add(Position.fromDegrees(position.latitude.getDegrees() + (arcSeconds.y / 3600d), 
				position.longitude.getDegrees()));
		
		positions.add(Position.fromDegrees(position.latitude.getDegrees() + (arcSeconds.y / 3600d), 
				position.longitude.getDegrees() + (arcSeconds.x / 3600d)));
		
		positions.add(Position.fromDegrees(position.latitude.getDegrees(), 
				position.longitude.getDegrees() + (arcSeconds.x / 3600d)));
		
		return positions;
	}
	
	private List<Coordinate> createCoordinatesIterable(final Coordinate coordinate, final Point.Double arcSeconds) {
		List<Coordinate> coordinates = new ArrayList<Coordinate>(4);
		
		coordinates.add(new Coordinate(coordinate.getLat(), coordinate.getLon()));
		
		coordinates.add(new Coordinate(coordinate.getLat() + (arcSeconds.y / 3600d), coordinate.getLon()));
		
		coordinates.add(new Coordinate(coordinate.getLat() + (arcSeconds.y / 3600d), 
				coordinate.getLon() + (arcSeconds.x / 3600d)));
		
		coordinates.add(new Coordinate(coordinate.getLat(), coordinate.getLon() + (arcSeconds.x / 3600d)));
		
		return coordinates;
	}
	
	public List<Position> getPositions() {
		return positions;
	}
	
	public List<Coordinate> getCoordinates() {
		return coordinates;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
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
		return position.longitude.getDegrees();
	}
	
	public double getRightLongitude() {
		return position.longitude.getDegrees() + (arcSeconds.x / 3600d);
	}
}
