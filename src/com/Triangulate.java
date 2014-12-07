package com;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class Triangulate extends SwingWorker<Point.Double, Void> {
	private static final double RDF_BEARING_LENGTH_IN_DEGREES = 0.500;
	
	private List<Bearing> bearingList;
	private List<Point.Double> intersectList;
	private Point.Double intersectPoint;
	
	public Triangulate(List<Bearing> bearingList) {
		this.bearingList = bearingList;
	}

	@Override
	protected Point.Double doInBackground() throws Exception {
        intersectList = intersections(bearingList);
        double ilx = 0;
        double ily = 0;
        for (int i = 0; i < intersectList.size(); i++) {
            ilx = ilx + intersectList.get(i).getX();
            ily = ily + intersectList.get(i).getY();
        }
        double ilxd = ilx / intersectList.size();
        double ilyd = ily / intersectList.size();

        return new Point.Double(ilxd, ilyd);
    }

	@Override
	protected void done() {
		try {
			intersectPoint = get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public Point.Double getIntersectPoint() {
		return intersectPoint;
	}
	
	public List<Point.Double> getIntersectList() {
		return intersectList;
	}
	
    private List<Point.Double> intersections(List<Bearing> bearingList) {   
        List<Point.Double> intersectList = new ArrayList<>();
        if (bearingList.size() >= 2) {
            for (int n = 0; n < bearingList.size(); n++) {
                for (int i = n + 1; i < bearingList.size(); i++) {
                    intersectList.add(intersect(bearingList.get(i).getBearingPosition(), 
                    		bearingList.get(i).getBearing(), bearingList.get(n).getBearingPosition(), 
                    		bearingList.get(n).getBearing(), RDF_BEARING_LENGTH_IN_DEGREES));
                }
            }
        }
        return intersectList;
    }
    
    private Point.Double intersect(Point.Double a, double t, Point.Double b, double z, double length) {
        double lonA = a.x;
        double latA = a.y;
        double lonB = b.x;
        double latB = b.y;
        
        double tA = t;
        double tB = z;
        
		double lonA2 = (Math.sin(tA * Math.PI / 180.0) * length) + a.x;
        double latA2 = (Math.cos(tA * Math.PI / 180.0) * length) + a.y;
        double lonB2 = (Math.sin(tB * Math.PI / 180.0) * length) + b.x;
        double latB2 = (Math.cos(tB * Math.PI / 180.0) * length) + b.y;
        
        
        double d = (lonA - lonA2) * (latB - latB2) - (latA - latA2) * (lonB - lonB2);
        if (d == 0) {
            return null;
        }

        double xi = ((lonB - lonB2) * (lonA * latA2 - latA * lonA2) - (lonA - lonA2) * 
        		(lonB * latB2 - latB * lonB2)) / d;
        double yi = ((latB - latB2) * (lonA * latA2 - latA * lonA2) - (latA - latA2) * 
        		(lonB * latB2 - latB * lonB2)) / d;

        return new Point.Double(xi, yi);
    }

}
