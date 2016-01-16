package jdrivetrack;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class ImageTile implements Cloneable {
	public BufferedImage image;
	public Point mapPosition;
	public Point imageLocation;
	public Dimension imageSize;
	public Dimension tileSize;
	public Dimension frameSize;
	public Dimension imageLocationOffset;
	public ImageObserver imageObserver;

	public ImageTile(BufferedImage image, Point imageLocation, Dimension tileSize) {
		this(image, null, imageLocation, tileSize, null, null, null, null);
	}
	
	public ImageTile(BufferedImage image, Point mapPosition, Point imageLocation, Dimension tileSize) {
		this(image, mapPosition, imageLocation, tileSize, null, null, null, null);
	}
	
	public ImageTile(BufferedImage image, Point mapPosition, Point imageLocation, Dimension tileSize, 
			Dimension imageSize) {
		this(image, mapPosition, imageLocation, tileSize, imageSize, null, null, null);
	}

	public ImageTile(BufferedImage image, Point mapPosition, Point imageLocation, 
			Dimension tileSize, Dimension imageSize, Dimension frameSize) {
		this(image, mapPosition, imageLocation, tileSize, imageSize, frameSize, null, null);
	}
	
	public ImageTile(BufferedImage image, Point mapPosition, Point imageLocation, 
			Dimension tileSize, Dimension imageSize, Dimension frameSize, Dimension imageLocationOffset) {
		this(image, mapPosition, imageLocation, tileSize, imageSize, frameSize, imageLocationOffset, null);
	}
	
	public ImageTile(BufferedImage image, Point mapPosition, Point imageLocation, Dimension tileSize, Dimension imageSize, 
			Dimension frameSize, ImageObserver imageObserver) {
		this(image, mapPosition, imageLocation, tileSize, imageSize, frameSize, null, imageObserver);
	}
	
	public ImageTile(BufferedImage image, Point mapPosition, Point imageLocation, Dimension tileSize, Dimension imageSize, 
			Dimension frameSize, Dimension imageLocationOffset, ImageObserver imageObserver) {
		this.image = image;
		this.mapPosition = mapPosition;
		this.imageLocation = imageLocation;
		this.tileSize = tileSize;
		this.imageLocationOffset = imageLocationOffset;
		this.frameSize = frameSize;
		this.imageObserver = imageObserver;
		if (imageSize == null) {
			this.imageSize = imageSize();
		} else {
			this.imageSize = imageSize;
		}
	}

	private Dimension imageSize() {
		return new Dimension(image.getWidth(), image.getHeight());
	}

	public BufferedImage getImage() {
		return image;
	}

	public Graphics getGraphics() {
		return image.getGraphics();
	}
	
	public boolean addToImage(BufferedImage bi) {
		return image.getGraphics().drawImage(image, imageLocation.x, imageLocation.y, imageSize.width, imageSize.height,
				imageObserver);
	}
	
	public Point getMapPosition() {
		return mapPosition;
	}

	public Point getImageLocation() {
		return imageLocation;
	}

	public Dimension getImageLocationOffset() {
		return imageLocationOffset;
	}
	
	public void setImageLocation(Point imageLocation) {
		this.imageLocation = imageLocation;
	}
	
	public Dimension getTileSize() {
		return tileSize;
	}

	public Dimension getImageSize() {
		return imageSize;
	}
	
	public Dimension getFrameSize() {
		return frameSize;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
