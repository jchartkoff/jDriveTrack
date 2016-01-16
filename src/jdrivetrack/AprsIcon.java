package jdrivetrack;

import java.awt.Point;

public class AprsIcon {
	private int index;
	private Point.Double point;
	private String identifier;

	public AprsIcon(int index, Point.Double point, String identifier) {
		this.index = index;
		this.point = point;
		this.identifier = identifier;
	}

	public int getIndex() {
		return index;
	}

	public Point.Double getAprsPosition() {
		return point;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public String getCallSign() {
		String cs;
		int dash = identifier.indexOf("-");
		if (dash >= 0 && dash <= 15)
			cs = identifier.substring(0, dash);
		else
			cs = identifier;
		return cs;
	}

	public String getSsid() {
		String id;
		int dash = identifier.indexOf("-");
		if (dash >= 0 && dash <= 15)
			id = identifier.substring(dash + 1);
		else
			id = "0";
		return id;
	}
}

