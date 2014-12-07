package com;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TileServer {
	private static final String DEFAULT_TILE_SERVER_URL = new String("http://tile.openstreetmap.org");
	private static final int DEFAULT_MAX_ZOOM = 18;
	private static final Dimension DEFAULT_TILE_SIZE = new Dimension(256,256);
	
	private ScheduledFuture<?> interfaceHandle = null;
	private ScheduledFuture<?> serverHandle = null;
	private String urlString;
	private int maxZoom;
	private Dimension tileSize;
	private Interface networkInterfaceAvailability = Interface.UNKNOWN;
	private Server serverAvailability = Server.UNKNOWN;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public static final String ONLINE = "ONLINE";
	public static final String OFFLINE = "OFFLINE";
	public static final String WAITING = "WAITING";
	public static final String UNKNOWN = "UNKNOWN";
	public static final String AVAILABLE = "AVAILABLE";
	public static final String UNAVAILABLE = "UNAVAILABLE";
	
	public enum Interface {ONLINE, OFFLINE, WAITING, UNKNOWN}
	public enum Server {AVAILABLE, UNAVAILABLE, WAITING, UNKNOWN}
	
	public TileServer() {
		this(DEFAULT_TILE_SERVER_URL, DEFAULT_MAX_ZOOM, DEFAULT_TILE_SIZE);
	}
	
	public TileServer(String urlString) {
		this(urlString, DEFAULT_MAX_ZOOM, DEFAULT_TILE_SIZE);
	}
	
	public TileServer(int maxZoom) {
		this(DEFAULT_TILE_SERVER_URL, maxZoom, DEFAULT_TILE_SIZE);
	}
	
	public TileServer(String urlString, int maxZoom, Dimension tileSize) {
        this.urlString = urlString;
		this.maxZoom = maxZoom;
		this.tileSize = tileSize;
	}	

	public int getMaxZoom() {
        return maxZoom;
    }
    
	public Dimension getTileSize() {
		return tileSize;
	}
	
	public Interface getNetworkInterfaceAvailability() {
		return networkInterfaceAvailability;
	}
	
	public Server getServerAvailability() {
		return serverAvailability;
	}
	
	public String getURL() {
        return new String(urlString);
    }
    
	public void adviseOnNetworkInterfaceAvailability() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		final Runnable checkNetworkInterface = new Runnable() {public void run() {isNetworkInterfaceAvailable();}};
		interfaceHandle = scheduler.scheduleAtFixedRate(checkNetworkInterface, 10, 10, TimeUnit.SECONDS);
		scheduler.execute(checkNetworkInterface);
	}

    public static String getTileString(String url, int xtile, int ytile, int zoom) {
        return url + "/" + zoom + "/" + xtile + "/" + ytile + ".png";
    }
	
    public static String getTileString(TileServer tileServer, int xtile, int ytile, int zoom) {
    	return getTileString(tileServer.getURL(), xtile, ytile, zoom);
    }
    
    public void adviseOnTileServerAvailability() {
    	adviseOnTileServerAvailability(getTileString(urlString, 1, 1, 1));
    }
    
	public void adviseOnTileServerAvailability(final String urlString) {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		final Runnable checkServer = new Runnable() {public void run() {isTileServerAvailable(urlString);}};
		serverHandle = scheduler.scheduleAtFixedRate(checkServer, 10, 10, TimeUnit.SECONDS);
		scheduler.execute(checkServer);
	}
	
	private void isTileServerAvailable(String urlString) {
        try {
            URL url = new URL(urlString);
            url.getContent();
            serverHandle.cancel(true);
            serverAvailability = Server.AVAILABLE;
            pcs.firePropertyChange(AVAILABLE, null, urlString);
        } catch (Exception e) {
        	if (!serverHandle.isCancelled()) {
	        	serverAvailability = Server.UNAVAILABLE;
	        	pcs.firePropertyChange(WAITING, null, urlString);
        	}
        }
	}

	private void isNetworkInterfaceAvailable() {
		Enumeration<NetworkInterface> interfaces = null;
		NetworkInterface interf = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
			    interf = interfaces.nextElement();
			    if (interf.isUp() && !interf.isLoopback()) {
				    List<InterfaceAddress> adrs = interf.getInterfaceAddresses();
				    for (Iterator<InterfaceAddress> iter = adrs.iterator(); iter.hasNext();) {
				        InterfaceAddress adr = iter.next();
				        InetAddress inadr = adr.getAddress();
				        if (inadr instanceof Inet4Address) {
				        	interfaceHandle.cancel(false);
				        	networkInterfaceAvailability = Interface.ONLINE;
				        	pcs.firePropertyChange(ONLINE, null, interf.getDisplayName());
				        }
		            }
			    }
			}
		} catch (SocketException e) {
			if (!interfaceHandle.isCancelled()) {
				networkInterfaceAvailability = Interface.OFFLINE;
				pcs.firePropertyChange(WAITING, null, interf.getDisplayName());
			}
		}
	}	
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}

