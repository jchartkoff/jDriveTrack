package jdrivetrack;

import java.awt.image.BufferedImage;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import interfaces.TileSource;

import tilesources.BingAerialTileSource;
import tilesources.MapQuestOpenAerialTileSource;
import tilesources.MapQuestOsmTileSource;
import tilesources.OsmTileSource;
import types.Tile;

public class TileCache {
	public enum Progress {UPDATE, RESTORED}
	
	private static Semaphore semaphore = new Semaphore(1);
	private static final int DEFAULT_MAX_CACHE_SIZE = 128;
	private static final int DEFAULT_MAX_FILE_COUNT = 4096;
	private static final long MAX_FILE_DAYS = 35;
    private static final Logger log = Logger.getLogger(TileCache.class.getName());
    private static final File DEFAULT_DIRECTORY_PATH = new File(System.getProperty("user.home") + 
			File.separator + "drivetrack" + File.separator + "cache");
    
    private int cacheSize;
    private long maxFileAge;
    private int maxFileCount;
    private final Map<String, CacheEntry> hash;
    private final CacheLinkedListElement lruTiles;
	private File fileDirectoryPath;
	private boolean canceled = false;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public TileCache() {
		this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_FILE_COUNT, MAX_FILE_DAYS, DEFAULT_DIRECTORY_PATH);
	}

    public TileCache(File fileDirectoryPath) {
    	this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_FILE_COUNT, MAX_FILE_DAYS, fileDirectoryPath);
    }
	
    public TileCache(int maxCacheSize, int maxFileCount, long maxFileAge, File fileDirectoryPath) {
    	this.cacheSize = maxCacheSize;
        this.maxFileCount = maxFileCount;
        this.fileDirectoryPath = fileDirectoryPath;
    	this.maxFileAge = maxFileAge;
        hash = Collections.synchronizedMap(new LinkedHashMap<>(maxCacheSize));
        lruTiles = new CacheLinkedListElement();
    	if (maxFileAge > 0) makeDirectoryPath(fileDirectoryPath);
    	updateMonitor(5);
    }

    private void makeDirectoryPath(File fileDirectoryPath) {
		if (!fileDirectoryPath.exists()) fileDirectoryPath.mkdirs();
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
					File encodedFileName = new File(fileDirectoryPath + File.separator + fileNameEncoder(tileKey));
					try {
						bufferedImage = ImageIO.read(encodedFileName);
					} catch (ArrayIndexOutOfBoundsException ex) {
						System.err.println("ImageIO read fault occured reading file: " + encodedFileName.getPath());
	        		}
					if (bufferedImage != null) {
						Tile tile = new Tile(source, x, y, z, bufferedImage);
						addTile(tile);
						entry = hash.get(tileKey);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} finally {
					semaphore.release();
				}
        	} else {
        		return null;
        	}
			return null;
		} 
    	lruTiles.moveElementToFirstPos(entry);
    	entry.tile.setLoaded(true);
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
    
    private void updateMonitor(int progress) {
        pcs.firePropertyChange(Progress.UPDATE.toString(), null, progress);
    }
    
    public void cancel() {
    	canceled = true;
    }
    
    public void restoreDiskCache() {
    	new Thread(new RestoreDiskCache(fileDirectoryPath, cacheSize, maxFileCount)).start();
    }
    
	private class RestoreDiskCache implements Runnable {
		private File fileDirectoryPath;
		private int maxCacheSize;
		private int maxFileCount;
		private int numberRestored = 0;
		private int numberTilesToBeRestored = 0;
		
		private RestoreDiskCache(File fileDirectoryPath, int maxCacheSize, int maxFileCount) {
			this.fileDirectoryPath = fileDirectoryPath;
			this.maxCacheSize = maxCacheSize;
			this.maxFileCount = maxFileCount;
		}
		
		@Override
		public void run() {
            canceled = false;
			try {
				semaphore.acquire();
	 			List<File> imageFiles = byAccessTime(fileDirectoryPath);
				numberTilesToBeRestored = Math.min(imageFiles.size(), Math.min(maxCacheSize, maxFileCount));
				if (imageFiles != null) {
					for (File file : imageFiles) {
						if (canceled) break;
						if (file.isFile()) {
							String decodedFileName = URLDecoder.decode(file.getName(), "UTF-8");
							ParsedTileKey ptk = new ParsedTileKey(decodedFileName);
							BufferedImage bimg = null;
							try {
								bimg = ImageIO.read(file);
							} catch (ArrayIndexOutOfBoundsException ex) {
								System.err.println("ImageIO read fault occured on file: " + file.getPath());
								boolean deleted = file.delete();
								System.err.println("corrupt file successfully deleted: " + deleted);
							}
							if (bimg != null) {
								Tile tile = new Tile(ptk.getTileSource(), ptk.getX(), ptk.getY(), ptk.getZoom(), bimg);
								addTile(tile);
								numberRestored++;
							}
							int progress = 5 + (int) ((numberRestored / (double) numberTilesToBeRestored) * 95);
							updateMonitor((int) Math.min(progress, 100d));
						}
						if (numberRestored == numberTilesToBeRestored) break;
				    }
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (CancellationException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				canceled = false;
				semaphore.release();
			}
			updateMonitor(100);
	        pcs.firePropertyChange(Progress.RESTORED.toString(), null, numberRestored);
		}
    }
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

	public int getTileCountOnDisk() {
		File[] imageFiles = fileDirectoryPath.listFiles();
		return imageFiles.length;
	}
	
    public void saveToFile(Tile tile) {
    	new Thread(new SaveToFile(tile, fileDirectoryPath, maxFileAge, maxFileCount)).start();
    }
	
	private class SaveToFile implements Runnable {
		private Tile tile;
		private File fileDirectoryPath;
		private long maxFileAge;
		private int maxFileCount;
		private SaveToFile(Tile tile, File fileDirectoryPath, long maxFileAge, int maxFileCount) {
			this.tile = tile;
			this.fileDirectoryPath = fileDirectoryPath;
			this.maxFileAge = maxFileAge;
			this.maxFileCount = maxFileCount;
		}
		@Override
		public void run() {
			File fileName = null;
			try {
	    		fileName = new File (fileDirectoryPath + File.separator + fileNameEncoder(tile.getKey()));
	    		Path filePath = Paths.get(fileName.toURI());
	    		if (Files.exists(filePath)) {
	    			if (getAgeOfFile(fileName).getStandardDays() > maxFileAge) {	
	    				File replacementFile = fileName;
	    				semaphore.acquire();
	    				if (fileName.delete()) {
		    				ImageIO.write(tile.getImage(), "jpg", replacementFile);
		    				addTile(tile);
			    		} else {
			    			int tileCountOnDisk = getTileCountOnDisk();
			    			if (tileCountOnDisk >= maxFileCount) semaphore.acquire();
			    			List<File> imageFiles = byAccessTime(fileDirectoryPath);
			    			while(tileCountOnDisk >= maxFileCount) {
			    				imageFiles.get(imageFiles.size() - 1).delete();
			    				tileCountOnDisk--;
			    			}
		    				ImageIO.write(tile.getImage(), "jpg", fileName);
		    				addTile(tile);
			    		}
	    			}
	    		}
			} catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			} finally {
				semaphore.release();
			}
		}
	}

	public static Duration getAgeOfFile(File file) {
		return new Duration(getFileCreationTime(file), new DateTime(DateTimeZone.UTC));
	}
	
    public static DateTime getFileCreationTime(File file) {
    	FileTime fileCreationTime = null;
    	try {
	    	Path filePath = Paths.get(file.toURI());
			if (Files.exists(filePath)) {
				BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class); 			
				fileCreationTime = attributes.creationTime();
			}
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
    	return new DateTime(fileCreationTime.toMillis());
    }

    private class FileAccessTimeCompare implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            return getLastAccessedTime(o2).compareTo(getLastAccessedTime(o1));
        }
    }
    
    private Long getLastAccessedTime(File file) {
    	Long lastAccessed = null;
    	BasicFileAttributes attributes;
		try {
			attributes = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
			lastAccessed = attributes.lastAccessTime().toMillis();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lastAccessed;
    }
    
    private List<File> byAccessTime(File directory) {
    	List<File> fileList = new ArrayList<File>();
    	fileList.addAll(Arrays.asList(directory.listFiles()));
    	Collections.sort(fileList, new FileAccessTimeCompare());
    	return fileList;
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

	public File getFileDirectoryPath() {
		return fileDirectoryPath;
	}
    
	public void setMaxFileCount(int maxFileCount) {
		this.maxFileCount = maxFileCount;
		Thread worker = new Thread() {
    		@Override
    		public void run() {
    			try {
					int overage = getTileCountOnDisk() - maxFileCount;
					List<File> imageFiles = null;
					if (overage > 0) {
						semaphore.acquire();
						imageFiles = byAccessTime(fileDirectoryPath);
					}
					while (overage > 0) {
						imageFiles.get(imageFiles.size()-1).delete();
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
	
	private boolean isTileOnDisk(String tileKey, File fileDirectoryPath) {
		File encodedFileName = new File(fileDirectoryPath + File.separator + fileNameEncoder(tileKey));
		Path filePath = Paths.get(encodedFileName.toURI());
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
