package main.java;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolyline;
import types.MapDimension;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class TestGridLayer extends RenderableLayer {
	private Point.Double testTileDimension; 
	private MapDimension mapDimension;
	private Color color;
	private BasicShapeAttributes attributes;
	
	public TestGridLayer() {
		attributes = new BasicShapeAttributes();
		color = Color.RED;
	}
	
	public TestGridLayer(Point.Double testTileDimension, MapDimension mapDimension) {
		this(new BasicShapeAttributes(), testTileDimension, mapDimension, Color.RED);
	}
	
	public TestGridLayer(BasicShapeAttributes attributes, Point.Double testTileDimension, MapDimension mapDimension, Color color) {
		this.attributes = attributes;
		this.testTileDimension = testTileDimension;
		this.mapDimension = mapDimension;
		this.color = color;
		createTestGrid();
	}
	
	private void createTestGrid() {
		attributes.setOutlineMaterial(new Material(color));
		attributes.setOutlineWidth(3);
		attributes.setInteriorOpacity(0.0);
		attributes.setOutlineOpacity(0.3);
		attributes.setEnableAntialiasing(true);
		
		double degreesPerTileLon = testTileDimension.x / 3600.0;
		double degreesPerTileLat = testTileDimension.y / 3600.0;
		
		double verticalPosition = mapDimension.getLowerLatitude() / degreesPerTileLat;
		double startingLatitude = (int) verticalPosition * degreesPerTileLat;
		
		double horizontalPosition = mapDimension.getRightLongitude() / degreesPerTileLon;
		double startingLongitude = (int) horizontalPosition * degreesPerTileLon;
		
		for (double d = startingLongitude; d >= mapDimension.getLeftLongitude(); d -= degreesPerTileLon) {
			List<LatLon> latLonList = new ArrayList<LatLon>();
			latLonList.add(LatLon.fromDegrees(mapDimension.getUpperLatitude(), d));
			latLonList.add(LatLon.fromDegrees(mapDimension.getLowerLatitude(), d));
			SurfacePolyline gridPolyline = new SurfacePolyline(latLonList);
			gridPolyline.setAttributes(attributes);
			addRenderable(gridPolyline);
		}

		for (double d = startingLatitude; d <= mapDimension.getUpperLatitude(); d += degreesPerTileLat) {
			List<LatLon> latLonList = new ArrayList<LatLon>();
			latLonList.add(LatLon.fromDegrees(d, mapDimension.getLeftLongitude()));
			latLonList.add(LatLon.fromDegrees(d, mapDimension.getRightLongitude()));
			SurfacePolyline gridPolyline = new SurfacePolyline(latLonList);
			gridPolyline.setAttributes(attributes);
			addRenderable(gridPolyline);
		}
	}
	
	public void setAttributes(BasicShapeAttributes attributes) {
		this.attributes = attributes;
	}
	
	public void setGridColor(Color color) {
		this.color = color;
		attributes.setOutlineMaterial(new Material(color));
	}
	
	public void setTestTileDimension(Point.Double testTileDimension) {
		this.testTileDimension = testTileDimension;
		if (mapDimension == null) {
			removeAllRenderables();
			createTestGrid();
		}
	}
	
	public void setMapDimension(MapDimension mapDimension) {
		this.mapDimension = mapDimension;
		if (testTileDimension != null) {
			removeAllRenderables();
			createTestGrid();
		}
	}
	
	public void setTestGridParameters(Point.Double testTileDimension, MapDimension mapDimension, Color color) {
		this.testTileDimension = testTileDimension;
		this.mapDimension = mapDimension;
		this.color = color;
		removeAllRenderables();
		createTestGrid();
	}
}
