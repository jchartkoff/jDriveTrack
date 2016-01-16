package jdrivetrack;

public class UTMTestTile {
	private String zone;
	private int easting;
	private int northing;
	private String message;
	
	public UTMTestTile() {}
	
	public UTMTestTile(String message) {
		this.message = message;
	}
	
	public UTMTestTile(String zone, int easting, int northing) {
		this.zone = zone;
		this.easting = easting;
		this.northing = northing;
	}
	
	public String getGridZoneDesignator() {
		return zone;
	}
	
	public int getEasting() {
		return easting;
	}
	
	public int getNorthing() {
		return northing;
	}
	
	public void setEasting(int easting) {
		this.easting = easting;
	}
	
	public void setNorthing(int northing) {
		this.northing = northing;
	}
	
	public void setGridZoneDesignator(String zone) {
		this.zone = zone;
	}
	
	@Override
	public String toString() {
		if (message != null) return message;
		return new String(zone + " " + easting + "mE " + northing + "mN");
	}
}
