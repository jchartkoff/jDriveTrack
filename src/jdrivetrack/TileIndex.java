package jdrivetrack;

import java.awt.Color;

import interfaces.MapInterface;;

public class TileIndex {     
    private UTMTestTile testTileCoords;
    private double sinad;
    private double ber;
    private double rssi;
    private int measurementCount;
    private MapInterface map;

    public TileIndex(MapInterface map, UTMTestTile testTileCoords, GeoTile geoTile, int measurementCount) {
    	this.map = map;
    	this.measurementCount = measurementCount;
    	this.testTileCoords = testTileCoords;
    	map.addPolygon(geoTile);
    }

    public UTMTestTile getUTMTestTile() {
        return testTileCoords;
    }

    public int getMeasurementCount() {
        return measurementCount;
    }

    public void incrementMeasurementCount() {
        measurementCount++;
    }

    public void setColor(int index, Color color) {
    	map.changePolygonColor(index, color);
    }
    
    public void addSinad(double sinad) {
        this.sinad = this.sinad + sinad;
    }

    public double getAvgSinad() {
        return sinad / measurementCount;
    }

    public void addBer(double ber) {
        this.ber = this.ber + ber;
    }

    public double getAvgBer() {
        return ber / measurementCount;
    }

    public void addRssi(double rssi) {
        this.rssi = this.rssi + rssi;
    }

    public double getAvgRssi() {
        return rssi / measurementCount;
    }
}
