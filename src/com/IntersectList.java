package com;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class IntersectList extends SwingWorker<List<Point.Double>, Void> {

	private List<ConicSection> coneList = null;
	private int add;
	private Point.Double p1;
	private Point.Double p2;
	private Point.Double p3;
	private Point.Double p4;
	private Line2D.Double l1 = null;
	private Line2D.Double l2 = null;
	
	public IntersectList(List<ConicSection> coneList, int previousListSize) {
    	this.coneList = coneList;
    	add = coneList.size() - previousListSize;
	}
 
    protected List<Point.Double> doInBackground() {
    	List<Point.Double> intersectList = new ArrayList<Point.Double>();
    	if (coneList == null || coneList.size() < 2) return intersectList;
    	if (coneList.size() > 1 && add > 0) {
	    	for (int i = coneList.size() - 1; i >= coneList.size() - add; i--) {
	    		List<Point.Double> sa1 = coneList.get(i).getArcPointList();
	    		if (sa1 == null) return intersectList;
				for (int n = i - 1; n >= 0; n--) {
					List<Point.Double> sa2 = coneList.get(n).getArcPointList();
					if (sa2 == null) return intersectList;
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
							    	if (l1. intersectsLine(l2)) {
							    		Point.Double intersection = intersect(l1, l2);
										intersectList.add(intersection);
									}
							    } else break;	
					    	}
					    } else break;	
					}
				}
			}
    	}
    	return intersectList;
	}

	private Point.Double intersect(Line2D.Double a, Line2D.Double b) {
        double d = (a.x1 - a.x2) * (b.y1 - b.y2) - (a.y1 - a.y2) * (b.x1 - b.x2);
        if (d == 0) {
            return null;
        }

        double xi = ((b.x1 - b.x2) * (a.x1 * a.y2 - a.y1 * a.x2) - (a.x1 - a.x2) * 
        		(b.x1 * b.y2 - b.y1 * b.x2)) / d;
        double yi = ((b.y1 - b.y2) * (a.x1 * a.y2 - a.y1 * a.x2) - (a.y1 - a.y2) * 
        		(b.x1 * b.y2 - b.y1 * b.x2)) / d;

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
