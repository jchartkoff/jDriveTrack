package jdrivetrack;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Arrow2D implements Shape {
	private GeneralPath path = new GeneralPath();
	private List<Point> list;
	
	public Arrow2D(double angle, double length) {
		list = buildArrow(angle, length);
		path = buildShape(list);
	}

	private GeneralPath buildShape(List<Point> list) {
    	GeneralPath path = new GeneralPath();
    	path.moveTo(list.get(0).x, list.get(0).y);
    	for (int i = 1; i < list.size(); i++) {
	        Point n = list.get(i); 
	    	path.lineTo(n.x, n.y);
	    }
		return path;
    }
	
	public List<Point> getPoints() {
		return list;
	}
	
	public void setPoints(List<Point> list) {
		this.list = list;
	}
	
	public void setAngle(double angle, double length) {
		list = buildArrow(angle, length);
		path = buildShape(list);
	}
	
	@Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	@Override
	public boolean contains(double x, double y) {
		return path.contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return path.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return path.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return path.contains(r);
	}

	@Override
	public PathIterator getPathIterator(final AffineTransform at) {
		return path.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return path.getPathIterator(at, flatness);
	}
	
	private List<Point> buildArrow(double angle, double length) {
		List<Point> arrow = new ArrayList<Point>(6);

		arrow.add(new Point((int) (-Math.sin(Math.toRadians(angle)) * length), (int) (Math.cos(Math.toRadians(-angle)) * length)));
		arrow.add(new Point(0,0));
		arrow.add(new Point((int) (-Math.sin(Math.toRadians(angle + 40)) * length), (int) (Math.cos(Math.toRadians(angle + 40)) * length)));
		arrow.add(new Point(0,0));
		arrow.add(new Point((int) (-Math.sin(Math.toRadians(angle - 40)) * length), (int) (Math.cos(Math.toRadians(angle - 40)) * length)));
		arrow.add(new Point(0,0));
		
		return arrow;
	}
}

