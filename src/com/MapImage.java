package com;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapImage {
	
    public static final String WAITING = "WAITING";
	public static final String BLANK = "BLANK";
	public static final String NORMAL = "NORMAL";
	public static final String TILE = "TILE";
	public static final String COMPOSITE = "COMPOSITE";
	public static final String NO_OPERATION = "NO_OPERATION";
	public static final String PROGRESS = "PROGRESS";
	
	private static final int THREADS = 30;
	
    private boolean cancelled = false;
    private BufferedImage compImage = null;
    private Point compImageLocation = null;
    private Dimension compImageSize = null;
    private ImageTile imageTile = null;
    private Dimension frameSize = null;
    private Dimension drawOffset = null;
    private Dimension downOffset = null;
    private TileCache tileCache = null;
    private TileServer tileServer = null;
    private Point mapPosition = null;
    private Dimension tileSize = null;
    private Dimension drag = null;
	private int zoom;
	private int tileCount;
    private int dx, dy;
    private int x, y;
    private int tilesAdded = 0;
    private Component parent;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private ExecutorService executor;
    private List<ImageRequest> imageRequests = new ArrayList<ImageRequest>();

    public MapImage(Component parent, Dimension frameSize, TileCache tileCache, Dimension tileSize, TileServer tileServer) {
    	this.tileServer = tileServer;
    	this.tileSize = tileSize;
    	this.tileCache = tileCache;
    	this.frameSize = frameSize;
    	this.parent = parent;
    }

    private void reset() {
    	cancelled = false;
    	imageRequests.subList(0, imageRequests.size()).clear();
    	compImageSize = null;
    	tileCount = 0;
    	compImageLocation = null;
    	downOffset = null;
    	drawOffset = null;
    	executor = Executors.newFixedThreadPool(THREADS);
        compImage = null;
        compImageLocation = null;
    }
    
    public void updateMapPanel(Point mapPosition, int zoom) {
    	this.mapPosition = mapPosition;
    	this.zoom = zoom;
    	reset();
    	Point[] compTiles = selectTiles(mapPosition, frameSize, tileSize);
    	compImageSize = imageSize(compTiles, tileSize);
    	compImage = new BufferedImage(compImageSize.width, compImageSize.height, BufferedImage.TYPE_INT_RGB);
		tileCount = tileCount(mapPosition, frameSize, tileSize);
		compImageLocation = new Point(compTiles[0].x * tileSize.width - mapPosition.x,
			compTiles[0].y * tileSize.height - mapPosition.y);
		drawOffset = new Dimension(-compImageLocation.x, -compImageLocation.y);
    	retrieveMapTiles(mapPosition, frameSize, tileSize);
    	issueImageRequests();
    }
    
    public void updateMapPanel(Point mapPosition, int zoom, ImageTile imageTile) {
    	this.mapPosition = mapPosition;
    	this.imageTile = imageTile;
    	this.zoom = zoom;
    	reset();
    	drag = drag(mapPosition, imageTile.getMapPosition());
    	if (!isNorth() && !isSouth() && !isEast() && !isWest()) {
    		pcs.firePropertyChange(NO_OPERATION, null, null);
    		return;
    	}
    	compImage = new BufferedImage(imageTile.getImageSize().width, imageTile.getImageSize().height, 
    		BufferedImage.TYPE_INT_RGB); 
    	compImageSize = imageTile.getImageSize();
    	fillHorizontalBlankImage(mapPosition, imageTile);
    	fillVerticalBlankImage(mapPosition, imageTile);
    	issueImageRequests();
	}

    private boolean isNorth() {
    	return (drag.height + imageTile.getImageLocation().y > 0);
    }
    
    private boolean isSouth() {
    	return (frameSize.height - ((imageTile.getImageLocation().y + drag.height) + imageTile.getImageSize().height) > 0);
    }
    
    private boolean isEast() {
    	return ((-drag.width - imageTile.getImageLocation().x) + frameSize.width > imageTile.getImageSize().width);
    }
    
    private boolean isWest() {
    	return (imageTile.getImageLocation().x + drag.width > 0);
    }

    private int tileCount(final Point mapPosition, final Dimension windowSize, final Dimension tileSize) {
    	Point[] tileArray = selectTiles(mapPosition, windowSize, tileSize);
    	return (tileArray[1].x - tileArray[0].x) * (tileArray[1].y - tileArray[0].y);
    }
    
    private void fillHorizontalBlankImage(Point mapPosition, ImageTile imageTile) {
		Point mp = null;
		Dimension td = null;
		if (isSouth()) {
			int y1 = (int) Math.floor(((double) mapPosition.y) / tileSize.height);
	    	int y2 = (int) Math.floor(((double) imageTile.getMapPosition().y) / tileSize.height);
			mp = new Point(mapPosition.x, 
				(imageTile.getMapPosition().y + imageTile.getImageLocation().y) + imageTile.getImageSize().height);			
			td = new Dimension(frameSize.width + Math.abs(drag.width), 
				((imageTile.getFrameSize().height - imageTile.getImageLocation().y) - drag.height) - 
				imageTile.getImageSize().height);						
			Point[] tileArray = selectTiles(mp, td, tileSize);
			int hdx = tileArray[0].x * tileSize.width - mapPosition.x;	
			drawOffset = new Dimension(-hdx, imageTile.getImageSize().height - ((y1-y2) * tileSize.height));			
			compImageLocation = new Point(hdx, ((y1-y2) * tileSize.height) - (-drag.height - 
				imageTile.getImageLocation().y));	
			downOffset = new Dimension(-hdx + drag.width, drag.height - compImageLocation.y);
		} 
		if (isNorth()) {
			mp = mapPosition;
			td = new Dimension(frameSize.width, drag.height);		
			Point[] tileArray = selectTiles(mp, td, tileSize);		
			int hdy = tileArray[0].y * tileSize.height - mapPosition.y;
			int hdx = tileArray[0].x * tileSize.width - mapPosition.x;
			compImageLocation = new Point(hdx, hdy);		
			drawOffset = new Dimension(-hdx, -hdy);
			downOffset = new Dimension(-hdx + drag.width, -hdy + drag.height);
		} 	
    	if (mp != null) {
	    	tileCount = tileCount(mp, td, tileSize);
	    	retrieveMapTiles(mp, td, tileSize);
    	}
    }	

    private void fillVerticalBlankImage(Point mapPosition, ImageTile imageTile) {
		Point mp = null;
		Dimension td = null;
		if (isEast()) {
			int hdx = 0, hdy = 0;
	    	mp = new Point(((mapPosition.x + drag.width) + imageTile.getImageLocation().x) + imageTile.getImageSize().width, 
					mapPosition.y + drag.height);			
	    	td = new Dimension(((-drag.width - imageTile.getImageLocation().x) + frameSize.width) - 
	    			imageTile.getImageSize().width, imageTile.getFrameSize().height - Math.abs(drag.height));	
	    	if (compImageLocation == null) {
	    		Point[] tileArray = selectTiles(mapPosition, frameSize, tileSize);	
	    		hdx = tileArray[0].x * tileSize.width - mapPosition.x;
	    		hdy = tileArray[0].y * tileSize.height - mapPosition.y;
	    		compImageLocation = new Point(hdx, hdy);
	    		downOffset = new Dimension(-hdx + drag.width, -hdy + drag.height);
	    	} else {
		    	hdy = compImageLocation.y;
	    	}
	    	int ndw = drag.width - compImageLocation.x + imageTile.getImageLocation().x;
			drawOffset = new Dimension(imageTile.getImageSize().width + ndw, -hdy + drag.height);
		} 
		if (isWest()) {
			int hdx = 0, hdy = 0;
			mp = new Point(mapPosition.x, mapPosition.y + drag.height);
			td = new Dimension(drag.width + imageTile.getImageLocation().x, 
					imageTile.getFrameSize().height - Math.abs(drag.height));		
			if (compImageLocation == null) {
				Point[] tileArray = selectTiles(mapPosition, frameSize, tileSize);			
				hdx = tileArray[0].x * tileSize.width - mapPosition.x;
				hdy = tileArray[0].y * tileSize.height - mapPosition.y;
				compImageLocation = new Point(hdx, hdy);
				downOffset = new Dimension(-hdx + drag.width, -hdy + drag.height);
			} else {
				hdy = compImageLocation.y;
			}
			drawOffset = new Dimension(-compImageLocation.x, -hdy + drag.height);
		}
		if (mp != null) {
	    	tileCount += tileCount(mp, td, tileSize);
	    	retrieveMapTiles(mp, td, tileSize);
		}
    }

    private Dimension drag(Point mapPosition, Point downPosition) {
    	return new Dimension(downPosition.x - mapPosition.x, downPosition.y - mapPosition.y);
    }
    
    private Point[] selectTiles(Point mapPosition, Dimension windowSize, Dimension tileSize) {
    	Point[] pt = new Point[2];
    	int x0 = (int) Math.floor(((double) mapPosition.x) / tileSize.width);
    	int y0 = (int) Math.floor(((double) mapPosition.y) / tileSize.height);
        int x1 = (int) Math.ceil(((double) mapPosition.x + windowSize.width) / tileSize.width);
        int y1 = (int) Math.ceil(((double) mapPosition.y + windowSize.height) / tileSize.height);
        pt[0] = new Point(x0,y0);
        pt[1] = new Point(x1,y1);
        return pt;
    }

    private Dimension imageSize(Point[] tileArray, Dimension tileSize) {
    	return new Dimension(tileSize.width * (tileArray[1].x - tileArray[0].x), 
    			tileSize.height * (tileArray[1].y - tileArray[0].y));
    }

    public int getTileCount() {
    	return tileCount;
    }

    public BufferedImage getCompositeImage() {
    	return compImage;
    }
    
    public void setMapPosition(Point mapPosition) {
    	this.mapPosition = mapPosition;
    }
    
    public Point getMapPosition() {
    	return mapPosition;
    }
    
    public void setFrameSize(Dimension frameSize) {
    	this.frameSize = frameSize;
    }
    
    public Dimension getFrameSize() {
    	return frameSize;
    }
    
    public Dimension getCompImageSize() {
    	return compImageSize;
    }

    public void setTileCache(TileCache tileCache) {
    	this.tileCache = tileCache;
    }
    
    public TileCache getTileCache() {
    	return tileCache;
    }
    
    public int getMaxZoom() {
    	return tileServer.getMaxZoom();
    }
    
    public void setTileTileServer(TileServer tileServer) {
    	this.tileServer = tileServer;
    }
    
    public TileServer getTileTileServer() {
    	return tileServer;
    }
    
    public void setTileSize(Dimension tileSize) {
    	this.tileSize = tileSize;
    }
    
    public Dimension getTileSize() {
    	return tileSize;
    }

    public Point getPd() {
    	return new Point(dx,dy);
    }

    public void cancelEvents() {
    	cancelled = true;
    	if (executor != null) {
	    	executor.shutdownNow();
	    	executor = null;
    	}
    }

    private void retrieveMapTiles(Point mapPosition, Dimension windowSize, final Dimension tileSize) {
        Point[] tileArray = selectTiles(mapPosition, windowSize, tileSize);
    	int xTileCount = 1 << zoom;
        int yTileCount = 1 << zoom;
        int x0 = tileArray[0].x;
        int y0 = tileArray[0].y;
        int x1 = tileArray[1].x;
        int y1 = tileArray[1].y;
        int dy = (y0 * tileSize.height) - mapPosition.y;
        for (y = y0; y < y1; ++y) { 
        	int dx = x0 * tileSize.width - mapPosition.x;
            for (x = x0; x < x1; ++x) {        	
            	if (x >= 0 && x < xTileCount && y >= 0 && y < yTileCount) {
            		try {
						String url = TileServer.getTileString(tileServer, x, y, zoom);
						ImageRequest irq = new ImageRequest(new URL(url), tileCache, tileServer, zoom, new Point(x,y), 
							mapPosition, new Point(dx,dy), tileSize, cancelled);
						imageRequests.add(irq);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
                }
            	dx += tileSize.width;
            }
            dy += tileSize.height;
        }
    }

    private void issueImageRequests() {
    	if (cancelled) return;
    	Iterator<ImageRequest> itr = imageRequests.iterator();
    	while (itr.hasNext()) {
    		ImageRequest irq = itr.next();
			ImageRetriever worker = new ImageRetriever(irq.url, irq.tileCache, irq.tileServer, irq.zoom, irq.fileIndex, 
				irq.mapPosition, irq.imageLocation, irq.tileSize, irq.cancelled);
			worker.addPropertyChangeListener(new PropertyChangeListener() {
            	@Override
            	public void propertyChange(PropertyChangeEvent event) {
    	            if (ImageRetriever.TILE_COMPLETE.equals(event.getPropertyName())) {
						updateImage((ImageTile) event.getNewValue());
    	            }
    	            if (ImageRetriever.WAITING.equals(event.getPropertyName())) {
    	            	pcs.firePropertyChange(ImageRetriever.WAITING, null, event.getNewValue());
    	            }
    	            if (ImageRetriever.BLANK.equals(event.getPropertyName())) {
    	            	pcs.firePropertyChange(ImageRetriever.BLANK, null, event.getNewValue());
    	            }
            	}
            });
			executor.execute(worker);
		}
		executor.shutdown();
    }
    
    private synchronized void updateImage(final ImageTile it) {
    	if (cancelled) return;
    	System.out.println(it.getImageLocation());
    	tilesAdded ++;
		pcs.firePropertyChange(PROGRESS, null, 100 * tilesAdded / tileCount);
    	compImage.createGraphics().drawImage(it.getImage(), it.getImageLocation().x + drawOffset.width, 
    			it.getImageLocation().y + drawOffset.height, parent); 
    	if (it.getImageSize().width == tileSize.width && it.getImageSize().height == tileSize.height) {
            pcs.firePropertyChange(TILE, null, it);
    	}
    	if (tilesAdded == imageRequests.size()) {
    		sendCompositeImage();
    	}
    }

    private void sendCompositeImage() {
    	if (cancelled) return;
    	if (imageTile != null && downOffset != null) {
			compImage.createGraphics().drawImage(imageTile.getImage(), imageTile.getImageLocation().x + downOffset.width, 
				imageTile.getImageLocation().y + downOffset.height, parent);	
		}
		ImageTile it = new ImageTile(compImage, mapPosition, compImageLocation, tileSize, compImageSize, frameSize);
		imageRequests.subList(0, imageRequests.size()).clear();
        pcs.firePropertyChange(COMPOSITE, null, it);
    }
    
    public class ImageRequest {
    	public URL url;
    	public TileCache tileCache;
    	public TileServer tileServer;
    	public Point fileIndex;
    	public Point mapPosition;
    	public Point imageLocation;
    	public Dimension tileSize;
    	public int zoom;
    	public boolean cancelled = false;
    	public ImageRequest(URL url, TileCache tileCache, TileServer tileServer, int zoom, 
    			Point fileIndex, Point mapPosition, Point imageLocation, Dimension tileSize, boolean cancelled) {
    		this.url = url;
    		this.tileCache = tileCache;
    		this.tileServer = tileServer;
    		this.zoom = zoom;
    		this.fileIndex = fileIndex;
    		this.mapPosition = mapPosition;
    		this.imageLocation = imageLocation;
    		this.tileSize = tileSize;
    		this.cancelled = cancelled;
    	}
    }
    
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
