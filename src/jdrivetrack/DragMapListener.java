package jdrivetrack;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DragMapListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
    private Point mouseCoords = null;
    private Point downCoords = null;
    private Point dragToCoords = null;
    private boolean dragging = false;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public DragMapListener() {
        mouseCoords = new Point();
        downCoords = new Point();
        new Point();
        dragToCoords = new Point();
    }

    @Override
	public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
            pcs.firePropertyChange("ZOOM_IN", mouseCoords, e.getPoint());
        } else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() >= 2) {
        	pcs.firePropertyChange("ZOOM_IN", mouseCoords, e.getPoint());
        } else if (e.getButton() == MouseEvent.BUTTON2) {
        	pcs.firePropertyChange("CENTER_BUTTON_CLICK", mouseCoords, e.getPoint());
        } else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
        	pcs.firePropertyChange("RIGHT_BUTTON_SINGLE_CLICK", mouseCoords, e.getPoint());
        } else if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1 ) {
        	pcs.firePropertyChange("LEFT_BUTTON_SINGLE_CLICK", mouseCoords, e.getPoint());
        }
        mouseCoords = e.getPoint();
    }

    @Override
	public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
            downCoords = new Point(e.getPoint());
            pcs.firePropertyChange("MOUSE_PRESSED", mouseCoords, e.getPoint());
            mouseCoords = e.getPoint();
        } 
    }

    @Override
	public void mouseReleased(MouseEvent e) {
        downCoords = null;
        dragging = false;
        pcs.firePropertyChange("MOUSE_RELEASED", mouseCoords, null);
        mouseCoords = e.getPoint();
    }

    @Override
	public void mouseMoved(MouseEvent e) {
        handlePosition(e);
    }

    @Override
	public void mouseDragged(MouseEvent e) {
    	dragging = true;
    	handlePosition(e);
        handleDrag(e);
    }

    @Override
	public void mouseExited(MouseEvent e) {
    	dragging = false;
    	pcs.firePropertyChange("MOUSE_EXITED", mouseCoords, e.getPoint());
    	mouseCoords = e.getPoint();
    }

    @Override
	public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        pcs.firePropertyChange("MOUSE_ENTERED", mouseCoords, e.getPoint());
        mouseCoords = e.getPoint();
    }

    public boolean isDragging() {
    	return dragging;
    }
    
    public Point getDragToCoords() {
    	return dragToCoords;
    }

    public Point getMouseCoords() {
    	return mouseCoords;
    }
    
    private void handlePosition(MouseEvent e) {
        pcs.firePropertyChange("MOUSE_POSITION_CHANGED", mouseCoords, e.getPoint());
        mouseCoords = e.getPoint();
    }
    
    private synchronized void handleDrag(MouseEvent e) {
        if (downCoords != null) {
            int tx = e.getX() - downCoords.x;
            int ty = e.getY() - downCoords.y;
            pcs.firePropertyChange("MOUSE_DRAGGING", dragToCoords, new Point(tx, ty));
            dragToCoords = new Point(tx, ty);
        } 
    }

    @Override
	public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
        if (rotation < 0) {
        	pcs.firePropertyChange("ZOOM_IN", mouseCoords, e.getPoint());
        } else {
        	pcs.firePropertyChange("ZOOM_OUT", mouseCoords, e.getPoint());
        }
        mouseCoords = e.getPoint();
    }
    
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}

