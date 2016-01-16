package jdrivetrack;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class IntersectList extends SwingWorker<List<Point.Double>, Void> {

	private List<ConicSection> coneList = null;
	private int add;
	
	public IntersectList(List<ConicSection> coneList, int previousListSize) {
    	this.coneList = coneList;
    	add = coneList.size() - previousListSize;
	}

    @Override
	protected List<Point.Double> doInBackground() {
    	List<Point.Double> intersectList = new ArrayList<Point.Double>();
    	Point.Double p1, p2, p3, p4;
    	Line2D.Double l1, l2;
    	if (coneList == null || coneList.size() < 2) return intersectList;
    	if (coneList.size() > 1 && add > 0) {
	    	for (int i = coneList.size() - 1; i >= coneList.size() - add; i--) {
	    		List<Point.Double> sa1 = coneList.get(i).getHyperbolicPointArrayList();
	    		if (sa1 == null) return intersectList;
				for (int n = i - 1; n >= 0; n--) {
					List<Point.Double> sa2 = coneList.get(n).getHyperbolicPointArrayList();
					if (sa2 == null) return intersectList;
					ArrayList<Point.Double> intersectPoints = new ArrayList<Point.Double>(3);
					for (int q = 0; q < sa1.size(); q += 10) {
					    p1 = sa1.get(q);
					    if (q < sa1.size() - 10) {
					    	p2 = sa1.get(q + 10);
					    	l1 = new Line2D.Double(p1, p2);
					    	for (int r = 0; r < sa2.size(); r += 10) {
							    p3 = sa2.get(r);
							    if (r < sa2.size() - 10) {
							    	p4 = sa2.get(r + 10);
							    	l2 = new Line2D.Double(p3, p4);
							    	if (l1.intersectsLine(l2)) {
							    		Point.Double intersection = intersect(l1, l2);
										intersectPoints.add(intersection);
									}
							    } else break;	
					    	}
					    } else break;	
					}
					if (isConvergent(coneList.get(i), coneList.get(n))) {
						Point.Double nearestPoint = selectNearestPoint(coneList.get(i), coneList.get(n), intersectPoints);
						intersectList.add(nearestPoint);
					} else {
						if (intersectPoints.size() > 0) intersectList.addAll(intersectPoints);
					}
				}
			}
    	}
    	return intersectList;
	}
    
    
    
    private boolean isConvergent(ConicSection c1, ConicSection  c2) {
    	double db = c1.getSMB().point.distance(c2.getSMB().point);
    	double da = c1.getSMA().point.distance(c2.getSMA().point);
    	return da < db;
    }

    private Point.Double selectNearestPoint(ConicSection c1, ConicSection  c2, ArrayList<Point.Double> points) {
    	double angleBetweenPoints = Vincenty.finalBearingTo(c1.getSMA().point, c2.getSMA().point);
    	double distanceBetweenPoints = Vincenty.distanceToOnSurface(c1.getSMA().point, c2.getSMA().point);
    	double distanceToNearestPoint = Double.MAX_VALUE;
    	Point.Double centerPoint = Vincenty.getVincentyDirect(c1.getSMA().point, angleBetweenPoints, distanceBetweenPoints / 2d).point;
    	Point.Double nearestPoint = null;
    	for (int i = 0; i < points.size(); i++) {
    		double distanceToThisPoint = centerPoint.distance(points.get(i));
    		if (distanceToThisPoint <= distanceToNearestPoint) {
    			distanceToNearestPoint = distanceToThisPoint;
    			nearestPoint = points.get(i);
    		}
    	}
    	return nearestPoint;
    }
    
	private Point.Double intersect(Line2D.Double a, Line2D.Double b) {
        double d = (a.x1 - a.x2) * (b.y1 - b.y2) - (a.y1 - a.y2) * (b.x1 - b.x2);
        if (d == 0) {
            return null;
        }

        double xi = ((b.x1 - b.x2) * (a.x1 * a.y2 - a.y1 * a.x2) - (a.x1 - a.x2) * (b.x1 * b.y2 - b.y1 * b.x2)) / d;
        double yi = ((b.y1 - b.y2) * (a.x1 * a.y2 - a.y1 * a.x2) - (a.y1 - a.y2) * (b.x1 * b.y2 - b.y1 * b.x2)) / d;

        return new Point.Double(xi, yi);
    }
	
	@Override
	protected void done() {
		try {
			List<Point.Double> il = get();
			if (il != null && !il.isEmpty()) firePropertyChange("INTERSECT_LIST_COMPLETE", null, il);
		} catch (InterruptedException | ExecutionException | NullPointerException e) {
			e.printStackTrace();
		}
	}
}
