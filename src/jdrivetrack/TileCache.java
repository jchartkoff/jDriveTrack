package jdrivetrack;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import interfaces.TileSource;
import tilesources.BingAerialTileSource;
import tilesources.MapQuestOpenAerialTileSource;
import tilesources.MapQuestOsmTileSource;
import tilesources.OsmTileSource;

public class TileCache implements PropertyChangeListener {
	public static final String PROGRESS = "PROGRESS";
	public static final String RESTORED = "RESTORE";
	
	private static Semaphore semaphore = new Semaphore(1);
	private static final int DEFAULT_MAX_CACHE_SIZE = 128;
	private static final int DEFAULT_MAX_FILE_COUNT = 4096;
	private static final long FILE_AGE_90_DAYS = 3600000 * 24 * 90 * -1;
    private static final Logger log = Logger.getLogger(TileCache.class.getName());
    private static final String DEFAULT_DIRECTORY_PATH = System.getProperty("user.home") + 
			File.separator + "drivetrack" + File.separator + "cache";
    
    private int cacheSize;
    private long maxFileAge;
    private int maxFileCount;
    private RestoreDiskCache restoreDiskCache;
    private final Map<String, CacheEntry> hash;
    private final CacheLinkedListElement lruTiles;
	private String fileDirectoryPath;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public TileCache() {
		this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_FILE_COUNT, FILE_AGE_90_DAYS, DEFAULT_DIRECTORY_PATH);
	}
	
    public TileCache(String fileDirectoryPath) {
    	this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_FILE_COUNT, FILE_AGE_90_DAYS, fileDirectoryPath);
    }
	
    public TileCache(int maxCacheSize, int maxFileCount, long maxFileAge, String fileDirectoryPath) {
        this.cacheSize = maxCacheSize;
        this.maxFileCount = maxFileCount;
        this.fileDirectoryPath = fileDirectoryPath;
    	this.maxFileAge = maxFileAge;
        hash = Collections.synchronizedMap(new LinkedHashMap<>(maxCacheSize));
        lruTiles = new CacheLinkedListElement();
    	if (maxFileAge > 0) {
			makeDirectoryPath(fileDirectoryPath);
    	}
    }

    private void makeDirectoryPath(String fileDirectoryPath) {
    	File directory = new File(fileDirectoryPath);
		if (!directory.exists()) new File(fileDirectoryPath).mkdirs();
		this.fileDirectoryPath = fileDirectoryPath;
    }
    
    public void addTile(Tile tile) {
    	CacheEntry entry = createCacheEntry(tile);
    	if (hash.put(tile.getKey(), entry) == null) {
            lruTiles.addFirst(entry);
            if (hash.size() > cacheSize || lruTiles.getElementCount() > cacheSize) {
                removeOldEntries();
            }
        }
    }
    
    public Tile getTile(TileSource source, int x, int y, int z) {
    	String tileKey = Tile.getTileKey(source, x, y, z);
        CacheEntry entry = hash.get(tileKey);
        if (entry == null) {
        	if (isTileOnDisk(tileKey, fileDirectoryPath)) {
				BufferedImage bufferedImage = null;
				try {
					semaphore.acquire();
					String encodedFileName = fileDirectoryPath + File.separator + fileNameEncoder(tileKey);
					bufferedImage = ImageIO.read(new File(encodedFileName));
				} catch (IOException | InterruptedException ex) {
					ex.printStackTrace();
				} finally {
					semaphore.release();
				}
				if (bufferedImage != null) {
					Tile tile = new Tile(source, x, y, z, bufferedImage);
					addTile(tile);
					entry = hash.get(tileKey);
				}
        	} else {
        		return null;
        	}
        }
        lruTiles.moveElementToFirstPos(entry);
        return entry.tile;
    }
    
    private void removeOldEntries() {
        try {
            while (lruTiles.getElementCount() > cacheSize) {
                removeEntry(lruTiles.getLastElement());
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    protected void removeEntry(CacheEntry entry) {
        hash.remove(entry.tile.getKey());
        lruTiles.removeEntry(entry);
    }

    protected CacheEntry createCacheEntry(Tile tile) {
        return new CacheEntry(tile);
    }

    public void clear() {
        hash.clear();
        lruTiles.clear();
    }

    public int getTileCountInMemory() {
        return hash.size();
    }

    public void setCacheSize(int maxCacheSize) {
        this.cacheSize = maxCacheSize;
        if (hash.size() > maxCacheSize) removeOldEntries();
    }

    protected static class CacheEntry {
        private Tile tile;
        private CacheEntry next;
        private CacheEntry prev;

        protected CacheEntry(Tile tile) {
            this.tile = tile;
        }
    }

    protected static class CacheLinkedListElement {
        protected CacheEntry firstElement = null;
        protected CacheEntry lastElement;
        protected int elementCount;

        public CacheLinkedListElement() {
            clear();
        }

        public void clear() {
            elementCount = 0;
            firstElement = null;
            lastElement = null;
        }

        public void addFirst(CacheEntry element) {
            if (element == null) return;
            if (elementCount == 0) {
                firstElement = element;
                lastElement = element;
                element.prev = null;
                element.next = null;
            } else {
                element.next = firstElement;
                firstElement.prev = element;
                element.prev = null;
                firstElement = element;
            }
            elementCount++;
        }

        public void removeEntry(CacheEntry element) {
            if (element == null) return;
            if (element.next != null) {
                element.next.prev = element.prev;
            }
            if (element.prev != null) {
                element.prev.next = element.next;
            }
            if (element == firstElement)
                firstElement = element.next;
            if (element == lastElement)
                lastElement = element.prev;
            element.next = null;
            element.prev = null;
            elementCount--;
        }

        public void moveElementToFirstPos(CacheEntry entry) {
            if (firstElement == entry)
                return;
            removeEntry(entry);
            addFirst(entry);
        }

        public int getElementCount() {
            return elementCount;
        }

        public CacheEntry getLastElement() {
            return lastElement;
        }

        public CacheEntry getFirstElement() {
            return firstElement;
        }
    }

    public void cancel(boolean mayInterruptIfRunning) {
    	restoreDiskCache.cancel(mayInterruptIfRunning);
    }
    
    public void restoreDiskCache() {
    	restoreDiskCache = new RestoreDiskCache(fileDirectoryPath, cacheSize, maxFileCount);
    	restoreDiskCache.addPropertyChangeListener(this);
    	restoreDiskCache.execute();
    }
    
	private class RestoreDiskCache extends SwingWorker<Integer, Integer> {
		private String fileDirectoryPath;
		private int maxCacheSize;
		private int maxFileCount;
		private RestoreDiskCache(String fileDirectoryPath, int maxCacheSize, int maxFileCount) {
			this.fileDirectoryPath = fileDirectoryPath;
			this.maxCacheSize = maxCacheSize;
			this.maxFileCount = maxFileCount;
		}
		@Override
		public Integer doInBackground() {
			int numberOfTilesRestored = 0;
			int numberOfTilesToBeRestored = 0;
			int progress = 0;
            setProgress(0);
			try {
				semaphore.acquire();
	 			File[] imageFiles = indexedRecordsByAccessTime(fileDirectoryPath);
				numberOfTilesToBeRestored = Math.min(imageFiles.length, Math.min(maxCacheSize, maxFileCount));
				if (imageFiles != null) {
					for (File file : imageFiles) {
						if (isCancelled()) return numberOfTilesRestored;
						if (file.isFile()) {
							String decodedFileName = URLDecoder.decode(file.getName(), "UTF-8");
							ParsedTileKey ptk = new ParsedTileKey(decodedFileName);
							BufferedImage bufferedImage = null;
							try {
								bufferedImage = ImageIO.read(file);
							} catch (IOException ex) {
								System.err.println("Error reading file from disk cache");
							}
							if (bufferedImage != null) {
								Tile tile = new Tile(ptk.getTileSource(), ptk.getX(), ptk.getY(), ptk.getZoom(), bufferedImage);
								addTile(tile);
								numberOfTilesRestored++;
								progress = (int) ((numberOfTilesRestored / (double) numberOfTilesToBeRestored) * 100);
								setProgress((int) Math.min(progress, 100d));
								if (numberOfTilesRestored >= numberOfTilesToBeRestored) return numberOfTilesRestored;
							}
						}
				    }
				}
				
			} catch (InterruptedException | UnsupportedEncodingException | CancellationException ex) {
				ex.printStackTrace();
			} finally {
				semaphore.release();
			}
			return numberOfTilesRestored;
		}
		@Override
		protected void done() {
			try {
				firePropertyChange(RESTORED, null, get());
			} catch (InterruptedException | ExecutionException | CancellationException e) {
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

	public int getTileCountOnDisk() {
		File[] imageFiles = new File(fileDirectoryPath).listFiles();
		return imageFiles.length;
	}
	
    public void saveToFile(Tile tile) {
    	new Thread(new SaveToFile(tile, fileDirectoryPath, maxFileAge, maxFileCount)).start();
    }
	
	private class SaveToFile implements Runnable {
		private Tile tile;
		private String fileDirectoryPath;
		private long maxFileAge;
		private int maxFileCount;
		private SaveToFile(Tile tile, String fileDirectoryPath, long maxFileAge, int maxFileCount) {
			this.tile = tile;
			this.fileDirectoryPath = fileDirectoryPath;
			this.maxFileAge = maxFileAge;
			this.maxFileCount = maxFileCount;
		}
		@Override
		public void run() {
			String fileName = null;
			try {
	    		fileName = fileDirectoryPath + File.separator + fileNameEncoder(tile.getKey());
	    		Path filePath = Paths.get(fileName);
	    		if (Files.exists(filePath)) {
	    			if (System.currentTimeMillis() - fileCreationTime(fileName) > maxFileAge) {	
	    				semaphore.acquire();
	    				if (new File(fileName).delete()) {
		    				File outputfile = new File(fileName);
		    				ImageIO.write(tile.getImage(), "jpg", outputfile);
		    				addTile(tile);
	    				}
	    			}
	    		} else {
	    			int tileCountOnDisk = getTileCountOnDisk();
	    			if (tileCountOnDisk >= maxFileCount) semaphore.acquire();
	    			File[] imageFiles = indexedRecordsByAccessTime(fileDirectoryPath);
	    			while(tileCountOnDisk >= maxFileCount) {
	    				imageFiles[imageFiles.length - 1].delete();
	    				tileCountOnDisk--;
	    			}
    				ImageIO.write(tile.getImage(), "jpg", new File(fileName));
    				addTile(tile);
	    		}
			} catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			} finally {
				semaphore.release();
			}
		}
	}
    
    private long fileCreationTime(String fileName) {
    	FileTime fileCreationTime = null;
    	try {
	    	Path filePath = Paths.get(fileName);
			if (Files.exists(filePath)) {
				BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class); 			
				fileCreationTime = attributes.creationTime();	
			}
    	} catch (IOException ex) {
    		System.err.println("File " + fileName + " Not Found in Cache");
    	}
		return fileCreationTime.toMillis();
    }
    
    private File[] indexedRecordsByAccessTime(String fileDirectory) {
    	File[] files = new File(fileDirectory).listFiles();
		PairWithLastAccessTime[] pairs = new PairWithLastAccessTime[files.length];
		for (int i = 0; i < files.length; i++) {
			pairs[i] = new PairWithLastAccessTime(files[i]);
		}
		Arrays.sort(pairs);
		for (int i = files.length - 1; i >= 0; i--) {
			files[i] = pairs[i].getFile();
		}
		return files;
    }
    
    private class PairWithLastAccessTime implements Comparable<Object> {
        private long t;
        private File f;

        private PairWithLastAccessTime(File file) {
        	f = file;
            BasicFileAttributes attributes;
			try {
				attributes = Files.readAttributes(Paths.get(f.getPath()), BasicFileAttributes.class);
				t = attributes.lastAccessTime().toMillis();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        private File getFile() {
        	return f;
        }
        
        @Override
        public int compareTo(Object o) {
            long u = ((PairWithLastAccessTime) o).t;
            return t < u ? -1 : t == u ? 0 : 1;
        }
    }
    
    private String fileNameEncoder(String key) {
    	String fileName = null;
    	try {
			fileName = URLEncoder.encode(key + ".jpg", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return fileName;
    }
    
    private static TileSource getSource(String sourceName) {
        switch (sourceName) {
    		case "Mapnik":
    	    	return new OsmTileSource.Mapnik();
    		case "CycleMap":
    			return new OsmTileSource.CycleMap();
    		case "MapQuest-OSM":
    			return new MapQuestOsmTileSource();
    		case "MapQuest Open Aerial":
    			return new MapQuestOpenAerialTileSource();
    		case "Bing":
    			return new BingAerialTileSource();
    		default:
    			return null;
        }
    }

	public int getCacheSize() {
		return cacheSize;
	}

	public void setMaxFileAge(long maxFileAge) {
		this.maxFileAge = maxFileAge;
	}

	public long getMaxFileAge() {
		return maxFileAge;
	}

	public String getFileDirectoryPath() {
		return fileDirectoryPath;
	}
    
	public void setMaxFileCount(int maxFileCount) {
		this.maxFileCount = maxFileCount;
		Thread worker = new Thread() {
    		@Override
    		public void run() {
    			try {
					int overage = getTileCountOnDisk() - maxFileCount;
					File[] imageFiles = null;
					if (overage > 0) {
						semaphore.acquire();
						imageFiles = indexedRecordsByAccessTime(fileDirectoryPath);
					}
					while (overage > 0) {
						imageFiles[imageFiles.length-1].delete();
						overage--;
					}
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			} finally {
    				semaphore.release();
    			}
    		}
		};
		worker.start();
	}

	public int getMaxFileCount() {
		return maxFileCount;
	}
	
	private boolean isTileOnDisk(String tileKey, String fileDirectoryPath) {
		String encodedFileName = fileDirectoryPath + File.separator + fileNameEncoder(tileKey);
		Path filePath = Paths.get(encodedFileName);
		if (Files.exists(filePath)) return true;
		return false;
	}
		
	private class ParsedTileKey {
		private int x, y, zoom;
		private TileSource tileSource;
		
		private ParsedTileKey(String decodedFileName) {
			int fileExtensionIndex = decodedFileName.indexOf(".jpg");
			int sourceIndex = decodedFileName.indexOf("@");
			String source = decodedFileName.substring(sourceIndex + 1, fileExtensionIndex);
			String zxy = decodedFileName.substring(0, sourceIndex);
			String[] split = zxy.split("/");
			if (split.length == 3) {
				zoom = Integer.parseInt(split[0]);
				x = Integer.parseInt(split[1]);
				y = Integer.parseInt(split[2]);
				tileSource = getSource(source);
			}
		}
		
		private int getX() {
			return x;
		}
		
		private int getY() {
			return y;
		}
		
		private int getZoom() {
			return zoom;
		}
		
		private TileSource getTileSource() {
			return tileSource;
		}
		
	}
}
