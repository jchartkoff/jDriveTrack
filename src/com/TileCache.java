package com;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

public class TileCache implements PropertyChangeListener {
	
	private static final String DEFAULT_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + 
			"drivetrack" + File.separator + "cache";
	
	private static final int DEFAULT_MAX_CACHE_SIZE = 4096;
	private static final long FILE_AGE_90_DAYS = 3600000 * 24 * 90 * -1;
	public static final String RESTORED = "RESTORED";
	public static final String PROGRESS = "PROGRESS";
	
	private int maxCacheSize;
	private String fileDirectoryPath;
	private long maxFileAge;
	private RestoreCache restoreCache;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private LinkedHashMap<Tile, BufferedImage> map = new LinkedHashMap<Tile, BufferedImage>(maxCacheSize, 0.75f, true) {
		private static final long serialVersionUID = -3553554052865897244L;

		@Override 
		protected boolean removeEldestEntry(Map.Entry<Tile,BufferedImage> eldest) {
            boolean remove = size() > maxCacheSize;
            return remove;
        }
    };

	public TileCache() {
		this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_DIRECTORY_PATH, FILE_AGE_90_DAYS);
	}
    
    public TileCache(String fileDirectoryPath) {
		this(DEFAULT_MAX_CACHE_SIZE, fileDirectoryPath, FILE_AGE_90_DAYS);
	}
	
    public TileCache(int maxCacheSize) {
		this(maxCacheSize, DEFAULT_DIRECTORY_PATH, FILE_AGE_90_DAYS);
	}
    
    public TileCache(int maxCacheSize, long maxFileAge) {
		this(maxCacheSize, DEFAULT_DIRECTORY_PATH, maxFileAge);
	}
    
    public TileCache(long maxFileAge) {
		this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_DIRECTORY_PATH, maxFileAge);
	}
    
    public TileCache(int maxCacheSize, String fileDirectoryPath, long maxFileAge) {
    	this.maxCacheSize = maxCacheSize;
    	this.fileDirectoryPath = fileDirectoryPath;
    	this.maxFileAge = maxFileAge;
    	if (maxFileAge > 0) {
			File directory = new File(fileDirectoryPath);
			if (!directory.exists()) new File(fileDirectoryPath).mkdirs();
    	}
    }
	
    public void setMaxFileAge(long maxFileAge) {
    	this.maxFileAge = maxFileAge;
    }
    
    public long getMaxFileAge() {
    	return maxFileAge;
    }
    
    public int getCacheSize() {
    	return map.size();
    }
    
    public int getMaxCacheSize() {
    	return maxCacheSize;
    }
    
    public String getFileDirectoryPath() {
    	return fileDirectoryPath;
    }
    
    public void put(TileServer tileServer, int x, int y, int z, BufferedImage bufferedImage) {
    	map.put(new Tile(tileServer.getURL(), x, y, z), bufferedImage);
    	if (maxFileAge > 0) saveToFile(tileServer.getURL(), x, y, z, bufferedImage);
    }
    
    public BufferedImage get(TileServer tileServer, int x, int y, int z) {
        return map.get(new Tile(tileServer.getURL(), x, y, z)); 
    }

    private void saveToFile(final String tileTileServer, final int x, final int y, final int z, final BufferedImage bufferedImage) {
    	SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
    		@Override
    		protected Void doInBackground() {
	    		try {
		    		String url = URLEncoder.encode(tileTileServer + "," + Integer.toString(x) +
		    			"," + Integer.toString(y) + "," + Integer.toString(z) + ".jpg", "UTF-8");
		    		String fileName = fileDirectoryPath + File.separator + url;
		    		Path filePath = Paths.get(fileName);
		    		if (Files.exists(filePath)) {
		    			BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class); 			
		    			FileTime fileCreationTime = attributes.creationTime();			
		    			if (System.currentTimeMillis() - fileCreationTime.toMillis() > maxFileAge) {		
		    				File currentFile = new File(fileName);
		    				if (currentFile.delete()) {
			    				File outputfile = new File(fileName);
			    				ImageIO.write(bufferedImage, "jpg", outputfile);
		    				}
		    			}
		    		} else {
		    			File outputfile = new File(fileName);
	    				ImageIO.write(bufferedImage, "jpg", outputfile);
		    		}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
    		}
    	};
    	worker.execute();
    }
    
    public void cancel(boolean mayInterruptIfRunning) {
    	restoreCache.cancel(mayInterruptIfRunning);
    }
    
    public void restoreCache() {
    	restoreCache = new RestoreCache(fileDirectoryPath);
    	restoreCache.addPropertyChangeListener(this);
    	restoreCache.execute();
    }
    
	private class RestoreCache extends SwingWorker<Integer, Void> {
		private String fileDirectory; 
		private RestoreCache(String fileDirectory) {
			this.fileDirectory = fileDirectory;
		}
		@Override
		protected Integer doInBackground() {
			File[] imageFiles = new File(fileDirectory).listFiles();
			int numberOfTilesRestored = 0;
			int numberOfTilesInDirectory = imageFiles.length;
			if (imageFiles != null) {
				for (File file : imageFiles) {
					if (isCancelled()) return numberOfTilesRestored;
					if (file.isFile()) {
						try {
							String encodedFileName = file.getName();
							String decodedFileName = URLDecoder.decode(encodedFileName, "UTF-8");
							String[] s = decodedFileName.split(",");
							if (s.length == 4) {
								String url = s[0];
								int x = Integer.parseInt(s[1]);
								int y = Integer.parseInt(s[2]);
								int z = Integer.parseInt(s[3].substring(0, s[3].indexOf(".")));
								BufferedImage bufferedImage = null;
								try {
									bufferedImage = ImageIO.read(file);
								} catch (ArrayIndexOutOfBoundsException ex) {
									
								}
								if (bufferedImage != null) {
									map.put(new Tile(url, x, y, z), bufferedImage);
									numberOfTilesRestored++;
									double progress = (numberOfTilesRestored / (double) numberOfTilesInDirectory) * 100;
									setProgress((int) progress);
								}
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
			    }
			}
			return numberOfTilesRestored;
		}
		
		@Override
		protected void done() {
			try {
				if (!isCancelled()) firePropertyChange(RESTORED, null, get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
    }

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if ("progress".equals(event.getPropertyName())) {
			pcs.firePropertyChange(PROGRESS, null, event.getNewValue());
        } 
		if (RESTORED.equals(event.getPropertyName())) {
			pcs.firePropertyChange(RESTORED, null, event.getNewValue());
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
	
}

