package com;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class ImageRetriever implements Runnable, FileStatus {
	private URL url;
	private TileCache tileCache;
	private TileServer tileServer;
	private Point fileIndex;
	private Point imageLocation;
	private Dimension tileSize;
	private int zoom;
	private boolean cancelled = false;
	private BufferedImage bi = null;
	private int waiting = 0;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public ImageRetriever(URL url, TileCache tileCache, TileServer tileServer, int zoom, Point fileIndex, 
	  Point mapPosition, Point imageLocation, Dimension tileSize, boolean cancelled) {
		this.url = url;
		this.tileCache = tileCache;
		this.tileServer = tileServer;
		this.zoom = zoom;
		this.fileIndex = fileIndex;
		this.imageLocation = imageLocation;
		this.tileSize = tileSize;
		this.cancelled = cancelled;
	}
	
	@Override
	public void run() {
		bi = tileCache.get(tileServer, fileIndex.x, fileIndex.y, zoom);
		while (bi == null && !cancelled) {
			pcs.firePropertyChange(BLANK, null, getBlankTile());
			try {
				bi = ImageIO.read(url);
				tileCache.put(tileServer, fileIndex.x, fileIndex.y, zoom, bi);
			} catch(IOException e) {
				try {
					pcs.firePropertyChange(WAITING, null, waiting++);
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		ImageTile it = new ImageTile(bi, imageLocation, tileSize);
		pcs.firePropertyChange(TILE_COMPLETE, null, it);
	}

	private ImageTile getBlankTile() {
		Graphics2D g = null;
		try {
			BufferedImage bi = new BufferedImage(tileSize.width, tileSize.height, BufferedImage.TYPE_INT_RGB);
			g = bi.createGraphics();
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(2.0f));
			g.drawRect(0, 0, tileSize.width, tileSize.height);
			g.setColor(new Color(64,0,0));
			g.setStroke(new BasicStroke(1.0f));
			g.drawLine(0, 0, tileSize.width, tileSize.height);
			g.drawLine(tileSize.width, 0, 0, tileSize.height);
			return new ImageTile(bi, imageLocation, tileSize);
		} finally {
			g.dispose();
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

	public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

}
