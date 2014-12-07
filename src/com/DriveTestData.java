package com;

import java.awt.Point;
import java.util.Calendar;

public class DriveTestData {
	public String sentence; 
	public Calendar dtg = null;
	public double[] ber = new double[10];
	public int[] rssi = new int[10];
	public int[] sinad = new int[10];
	public double[] freq = new double[10];
	public Point gridLastMeasured = new Point();
	public int tilesTraversed;
	public int measurementDelayTimer;
	public int tileIndexPointer;
	public Point.Double tileSize = new Point.Double();
	public int maximumSamplesPerTile;
	public int minimumSamplesPerTile;
	public Point.Double position = new Point.Double();
	public double dopplerDirection;
	public int dopplerQuality;
	public int marker;
	public DriveTestData() {}
	public DriveTestData(DriveTestData data) {
		this.sentence = data.sentence;
		this.dtg = data.dtg;
		this.ber = data.ber;
		this.rssi = data.rssi;
		this.sinad = data.sinad;
		this.freq = data.freq;
		this.gridLastMeasured = data.gridLastMeasured;
		this.tilesTraversed = data.tilesTraversed;
		this.measurementDelayTimer = data.measurementDelayTimer;
		this.tileIndexPointer = data.tileIndexPointer;
		this.tileSize = data.tileSize;
		this.maximumSamplesPerTile = data.maximumSamplesPerTile;
		this.minimumSamplesPerTile = data.minimumSamplesPerTile;
		this.position = data.position;
		this.dopplerDirection = data.dopplerDirection;
		this.dopplerQuality = data.dopplerQuality;
		this.marker = data.marker;
	}

	public static String[] toStringArray(DriveTestData data) {
		String[] ret = new String[17];
		ret[0] = data.sentence;
		ret[1] = data.dtg.toString();
		ret[2] = data.ber.toString();
		ret[3] = data.rssi.toString();
		ret[4] = data.sinad.toString();
		ret[5] = data.freq.toString();
		ret[6] = data.gridLastMeasured.toString();
		ret[7] = Integer.toString(data.tilesTraversed);
		ret[8] = Integer.toString(data.measurementDelayTimer);
		ret[9] = Integer.toString(data.tileIndexPointer);
		ret[10] = data.tileSize.toString();
		ret[11] = Double.toString(data.maximumSamplesPerTile);
		ret[12] = Double.toString(data.minimumSamplesPerTile);
		ret[13] = data.position.toString();
		ret[14] = Double.toString(data.dopplerDirection);
		ret[15] = Integer.toString(data.dopplerQuality);
		ret[16] = Integer.toString(data.marker);
		return ret;
	}
}

