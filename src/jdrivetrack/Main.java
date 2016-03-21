package jdrivetrack;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.geom.coords.MGRSCoord;

import interfaces.APRSInterface;
import interfaces.GPSInterface;
import interfaces.GPSInterface.FixQuality;
import interfaces.MapInterface;
import interfaces.RadioInterface;
import interfaces.SerialInterface;
import jdrivetrack.CoordinateUtils.Precision;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;

import jssc.SerialPortException;
import types.AprsIcon;
import types.Bearing;
import types.TestSettings;
import types.GeoTile;
import types.Measurement;
import types.MeasurementSet;
import types.StaticMeasurement;
import types.TestTile;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import interfaces.JMapViewerEventListener;
import events.JMVCommandEvent;

public class Main extends JFrame {
	private static final long serialVersionUID = 7230746210127860L;
	
	public static final Logger log = Logger.getLogger(Main.class.getName());
	public static final Point.Double TEST_POINT = new Point.Double(-83.075707,40.025348);
	
	private static final Dimension PREFERRED_MAP_SIZE = new Dimension(795,585);
    private static final double RDF_BEARING_LENGTH_IN_DEGREES = 0.500;
    private static final Point.Double DEFAULT_STARTUP_COORDINATES = new Point.Double(-86.0,35.0);
    private static final int DEFAULT_STARTUP_ZOOM = 6;
    private static final double DEFAULT_STARTUP_SCALE = 1.0;
    private static final Dimension BUTTON_DIM = new Dimension(60,28);
    
    private static final String DEFAULT_TILE_COMPLETE_SOUND = System.getProperty("user.home") +  
    	File.separator + "drivetrack" + File.separator + "sounds" + File.separator + "Ding.wav";  

    private static final String DEFAULT_DATA_FILE_DIRECTORY = System.getProperty("user.home") +  
        File.separator + "drivetrack" + File.separator + "data"; 

    private static final String USERNAME = "john";
    private static final String PASSWORD = "password";
    
    private static final boolean MONITOR_RADIO_HANDSHAKING = false;
    private static final boolean MONITOR_GPS_HANDSHAKING = false;
    
    private static final long WINDOW_CLOSING_COMPLETE = 1;
    private static final long STARTUP_COMPLETE = 2;
    private static final long NETWORK_CLOCK_STOPPED = 4;
    private static final long TIME_UPDATES_STOPPED = 8;
    private static final long NETWORK_INTERFACE_CHECK_COMPLETE = 16;
    private static final long APRS_INTERFACE_CLOSED = 32;
    private static final long RADIO_INTERFACE_CLOSED = 64;
    private static final long GPS_INTERFACE_CLOSED = 128;
    private static final long DATABASE_CLOSED = 256;
    private static final long ALL_THREADS_RELEASED = 511;
    
    private static final Color tileSelectedColor = new Color(64, 64, 64, 32);
	private static final Color tileInProgressColor = new Color(64, 64, 0, 32);
	private static final Color tileCompleteColor = new Color(0, 64, 0, 32);
	
    private enum DataMode {OPEN, CLOSED, STOP, RECORD, RESTORE_COMPLETE}
    private enum PositionSource {MANUAL, GPS, ARCHIVE, TIMER, LEARN}
    private enum TestMode {RSSI, RSSI_SINAD, SINAD, BER, RSSI_BER, MODE_NOT_SELECTED, DOPPLER, STATIC, OFF}
    
    private boolean monitorRadioHandshaking = MONITOR_RADIO_HANDSHAKING;
    private boolean monitorGPSHandshaking = MONITOR_GPS_HANDSHAKING;
    private volatile boolean serialErrorQueued = false;
    private long shutdownMask;
    private volatile boolean readyToExit = false;
    private Triangulate triangulate = null;
    private NetworkTime networkTime = null;
    private NetworkInterfaceCheck nicCheck;
    private int conicSectionListStartSize = 0; 
    private PropertyChangeListener mapPropertyChangeListener;
    private Preferences systemPref;
    private MapSettings mapSettings;
    private StaticTestSettings staticTestSettings;
    private SerialGPSComponent serialGPSComponent;
    private RadioComponent radioComponent;
    private CoverageTestSettings coverageTestSettings;
    private DatabaseConfiguration databaseConfig;
    private AprsComponent aprsComponent;
    private SignalAnalysis signalAnalysis;
    private JToolBar toolBar;
    private SignalMeterArray signalMeterArray;
    private CompassRose gpsCompassRose;
    private JPanel mapPanel;
    private JLabel recordCountLabel;
    private JLabel logFileNameLabel;
    private JLabel cursorLatitude;
    private JLabel cursorLongitude;
    private JLabel cursorTerrainAMSL;
    private JLabel viewingAltitude;
    private JLabel nmeaSentenceStringLabel;
    private JLabel gpsStatus;
    private JLabel gpsTxData;
    private JLabel gpsRxData;
    private JLabel gpsCTS;
    private JLabel gpsDSR;
    private JLabel gpsCD;
    private JLabel radioStatus;
    private JLabel radioTxData;
    private JLabel radioRxData;
    private JLabel radioCTS;
    private JLabel radioDSR;
    private JLabel radioCD;
    private JLabel aprsGPWPLSentence;
    private JLabel[] signalQuality;
    private JLabel aprsLatitude;
    private JLabel aprsLongitude;
    private JLabel aprsCallSign;
    private JLabel aprsSSID;
    private JLabel aprsStatus;
    private JLabel aprsTxData;
    private JLabel aprsRxData;
    private JLabel aprsCTS;
    private JLabel aprsDSR;
    private JLabel aprsCD;
    private JLabel markerID;
    private JLabel latitude;
    private JLabel longitude;
    private JLabel utcLabel;
    private JLabel speedMadeGood;
    private JLabel cursorMGRS;
    private JLabel currentMGRS;
    private JLabel cursorTestTileReference;
    private JLabel currentGridSquare;
    private JLabel measurementPeriod;
    private JLabel measurementsThisTile;
    private JLabel maxMeasurementsPerTile;
    private JLabel minMeasurementsPerTile;
    private JLabel tilesCompleted;
    private JLabel totalTiles;
    private JLabel averageRssiInCurrentTile;
    private JLabel averageBerInCurrentTile;
    private JLabel averageSinadInCurrentTile;
    private JPanel gpsInfoPanel;
    private Timer periodTimer;
    private Timer gpsRxDataTimer;
    private Timer gpsTxDataTimer;
    private Timer radioRxDataTimer;
    private Timer radioTxDataTimer;
    private Timer aprsRxDataTimer;
    private Timer aprsTxDataTimer;
    private Timer gpsValidHeadingTimer;
    private Timer measurementTimer;
    private Timer zoomOutMouseDownTimer;
    private Timer zoomInMouseDownTimer;
    private JToggleButton newDataFileButton;
    private JToggleButton openDataFileButton;
    private JToggleButton closeDataFileButton;
    private JButton saveDataFileButton;
    private JToggleButton stopDataFileButton;
    private JToggleButton recordDataFileButton;
    private JToggleButton gpsButton;
    private JButton centerOnGpsButton;
    private JToggleButton radioButton;
    private JToggleButton sinadButton;
    private JToggleButton aprsButton;
    private JToggleButton learnModeButton;
    private JToggleButton coverageTestButton;
    private JToggleButton staticLocationAnalysisButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JMenuItem aprsComponentMenuItem;
    private JMenuItem gpsComponentMenuItem;
    private JMenuItem receiverComponentMenuItem;
    private JMenuItem coverageTestSettingsMenuItem;
    private JMenuItem mapSettingsMenuItem;
    private JMenuItem mapBulkDownloaderMenuItem;
    private JMenuItem mapStatisticsMenuItem;
    private JMenuItem mapLayerSelectorMenuItem;
    private JMenuItem mapClearMenuItem;
    private JMenuItem newDataFileMenuItem;
    private JMenuItem openDataFileMenuItem;
    private JMenuItem closeDataFileMenuItem;
    private JMenuItem saveDataFileMenuItem;
    private JMenuItem saveAsDataFileMenuItem;
    private JMenuItem printPreviewMenuItem;
    private JMenuItem printMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem staticSignalLocationSettingsMenuItem;
    private JMenuItem signalAnalysisMenuItem;
    private JMenuItem databaseConfigMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenu fileMenu;
    private JMenu gpsMenu;
    private JMenu receiverMenu;
    private JMenu systemMenu;
    private JMenu mapMenu;
    private JMenu aprsMenu;
    private JMenu staticSignalLocationMenu;
    private JMenu signalAnalysisMenu;
    private JMenu databaseConfigMenu;
    private JMenu helpMenu;
    private DataMode dataMode;
    private int recordCount;
    private File dataDirectory;
    private MapInterface map;
    private DerbyRelationalDatabase database;
    private Point.Double startupLonLat = null;
    private double startupScale;
    private int startupZoom;
    private boolean coverageTestActive = false;
    private boolean staticTestActive = false;
    private Sinad sinad;
    private Integer[] sinadArray = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int markerCounter = 0;
    private List<AprsIcon> iconList = new ArrayList<AprsIcon>(1024);
    private List<Bearing> bearingList = new ArrayList<Bearing>(1024);
    private List<ConicSection> conicSectionList = new ArrayList<ConicSection>(1024);
    private List<Integer> staticListStartSize = new ArrayList<Integer>();
    private Point.Double lastDotLocation = new Point.Double();
	private List<Point.Double> intersectList = new ArrayList<Point.Double>();
	private IntersectList intersectListUpdate = null;
    private PositionSource lastInputSource;
    private boolean periodTimerTimeOut = false;
    private boolean periodTimerHalt = false;
    private FileHandler fh;
    private double cursorBearing = 0;
    private Point.Double cursorBearingPosition = null;
    private int cursorBearingIndex;
    private boolean cursorBearingSet = false;
    private int startMapArg = -1;
    private int startRadioArg = -1;
    private int startGpsArg = -1;
    private boolean startInDemoMode = false;
    private boolean clearAllPrefs = false;
    private boolean processStaticMeasurements = false;
    private PropertyChangeListener databaseListener;
    private PropertyChangeListener radioComponentListener;
    private PropertyChangeListener radioSerialInterfaceListener;
    private PropertyChangeListener radioDeviceInterfaceListener;
    private PropertyChangeListener gpsSerialInterfaceListener;
    private PropertyChangeListener gpsDeviceInterfaceListener;
    private PropertyChangeListener serialGPSComponentListener;
    private PropertyChangeListener aprsSerialInterfaceListener;
    private PropertyChangeListener aprsDeviceInterfaceListener;
    private MouseMotionListener mapMouseMotionListener;
    private MouseListener mapMouseListener;
    private KeyListener mapKeyListener;
    private JMapViewerEventListener jmvEventListener;
    private long previousGpsTimeInMillis = 0;
    private boolean mouseOffGlobe = true;
    private boolean mapDragged = false;
    private TestMode testMode;
    private int measurementDelay;
    private ProgressMonitor fileOpenMonitor;
    private ProgressMonitor shutdownMonitor;
    private ProgressMonitor coverageTestRestoreMonitor;
    private ProgressMonitor staticTestRestoreMonitor;
    private ProgressMonitor newFileMonitor;
    private boolean learnMode = false;
    private TestSettings testSettings;
    private TestTile currentTestTile;
    private boolean learnModeHold = false;
    private String averageRssiInCurrentTileText;
    private String averageBerInCurrentTileText;
    private String averageSinadInCurrentTileText;
    private String logFileName;
    
    public Main(String args[]) {
    	registerShutdownHook();
    	
		try {
			Options options = new Options();
	    	options.addOption(new Option("m", true, "map style")); 
	    	options.addOption(new Option("r", true, "radio")); 
	    	options.addOption(new Option("g", true, "gps")); 
	    	options.addOption(new Option("d", "demonstration mode")); 
	    	options.addOption(new Option("c", "clear all preferences")); 
	    	CommandLineParser parser = new DefaultParser(); 
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("r")) startRadioArg = Integer.parseInt(cmd.getOptionValue("r")); 
			if (cmd.hasOption("m")) startMapArg = Integer.parseInt(cmd.getOptionValue("m")); 
			if (cmd.hasOption("g")) startGpsArg = Integer.parseInt(cmd.getOptionValue("g")); 
			if (cmd.hasOption("d")) startInDemoMode = true; 
			if (cmd.hasOption("c")) clearAllPrefs = true; 
		} catch (ParseException ex) {
			reportException(ex);
		}

        try {
        	String eventLogFileName = System.getProperty("user.home") + File.separator +"drivetrack"  
        		+ File.separator + "eventLog" + File.separator + "event.log"; 
        	Path path = Paths.get(eventLogFileName);
    		File directory = new File(path.getParent().toString());
    		if (!directory.exists()) new File(path.getParent().toString()).mkdirs();
        	fh = new FileHandler(eventLogFileName, 4096, 64, true);
            log.addHandler(fh);
            log.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException ex) {
        	reportException(ex);
        } catch (IOException ex) {
        	reportException(ex);
        }
        
        setResizable(false);
        setExtendedState(Frame.NORMAL);
        setAlwaysOnTop(false);

        getSettingsFromRegistry();
        createDirectories();
        initializeLookAndFeel();
        initializeComponents();
        if (clearAllPrefs) clearAllPreferences();
        configureComponents();
        initializeComponentListeners();
        initializeListeners();
        createGraphicalUserInterface();
        displayGraphicalUserInterface();
        monitorHandshaking();
        
		if (startInDemoMode) startDemo();

		signalReadyToExit(STARTUP_COMPLETE);
		
		revalidate();
		repaint();
    }

	public static void main(final String[] args) {
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
		        new Main(args);
		    }
		});
	}
	
	private void createDirectories() {
    	if (!dataDirectory.exists()) dataDirectory.mkdirs();
	}
	
    private void getSettingsFromRegistry() {
    	systemPref = Preferences.userRoot().node("jdrivetrack/prefs/Main"); 
    	dataDirectory = new File(systemPref.get("LastDataFileDirectory", DEFAULT_DATA_FILE_DIRECTORY)); 
        startupLonLat = new Point.Double(systemPref.getDouble("MapLongitude", DEFAULT_STARTUP_COORDINATES.x), 
        	systemPref.getDouble("MapLatitude", DEFAULT_STARTUP_COORDINATES.y)); 
        if (startupLonLat.y < -90 || startupLonLat.y > 90 || startupLonLat.x < -180 || startupLonLat.x > 180) 
        	startupLonLat = DEFAULT_STARTUP_COORDINATES;
        startupScale = systemPref.getDouble("MapScale", DEFAULT_STARTUP_SCALE);  
        startupZoom = systemPref.getInt("MapZoom", DEFAULT_STARTUP_ZOOM);  
    }
    
    private void monitorHandshaking() {
        try {
			if (monitorRadioHandshaking) radioComponent.getSerialConfig().getSerialInterface().monitorHandshaking(radioComponent.getSerialConfig().getPortName());
		} catch (SerialPortException ex) {
			reportException(ex);
		}
        try {
			if (monitorGPSHandshaking) serialGPSComponent.getSerialConfig().getSerialInterface().monitorHandshaking(serialGPSComponent.getSerialConfig().getPortName());
		} catch (SerialPortException ex) {
			reportException(ex);
		}
    }

    private void initializeComponents() {
		coverageTestRestoreMonitor = new ProgressMonitor(this, "Restoring Coverage Test", "", 0, 100); 
		staticTestRestoreMonitor = new ProgressMonitor(this, "Restoring Static Signal Location Test", "", 0, 100); 
		shutdownMonitor = new ProgressMonitor(this, "", "Closing Application", 0, 19); 
		fileOpenMonitor = new ProgressMonitor(this, "Downloading Data from Disk", "", 0, 100); 
		newFileMonitor = new ProgressMonitor(this, "Building New SQL Database", "", 0, 100); 
		
    	shutdownMask += DATABASE_CLOSED;
    	recordCount = 0;
        networkTime = new NetworkTime();
        nicCheck = new NetworkInterfaceCheck();
        mapPanel = new JPanel();
        toolBar = new JToolBar();
        testSettings = new TestSettings();
        
        openDataFileButton = new JToggleButton();
        closeDataFileButton = new JToggleButton();
        stopDataFileButton = new JToggleButton();
        recordDataFileButton = new JToggleButton(); 
        gpsButton = new JToggleButton();
        radioButton = new JToggleButton();
        sinadButton = new JToggleButton();
        aprsButton = new JToggleButton();
        learnModeButton = new JToggleButton();
        coverageTestButton = new JToggleButton();
        staticLocationAnalysisButton = new JToggleButton();
        newDataFileButton = new JToggleButton();
        
        centerOnGpsButton = new JButton();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        saveDataFileButton = new JButton();
        
        JMenuBar menuBar = new JMenuBar();

        setJMenuBar(menuBar);

        fileMenu = new JMenu(" File "); 
        gpsMenu = new JMenu(" GPS "); 
        receiverMenu = new JMenu(" Receiver "); 
        systemMenu = new JMenu(" Coverage Test "); 
        mapMenu = new JMenu(" Map "); 
        aprsMenu = new JMenu(" APRS "); 
        staticSignalLocationMenu = new JMenu(" Static Signal Location "); 
        signalAnalysisMenu = new JMenu(" Signal Analysis "); 
        databaseConfigMenu = new JMenu(" Database ");
        helpMenu = new JMenu(" Help "); 

        menuBar.add(fileMenu);
        menuBar.add(gpsMenu);
        menuBar.add(receiverMenu);
        menuBar.add(systemMenu);
        menuBar.add(mapMenu);
        menuBar.add(aprsMenu);
        menuBar.add(staticSignalLocationMenu);
        menuBar.add(signalAnalysisMenu);
        menuBar.add(databaseConfigMenu);
        menuBar.add(helpMenu);

        aprsComponentMenuItem = new JMenuItem(" APRS Settings "); 
        gpsComponentMenuItem = new JMenuItem(" GPS Settings "); 
        receiverComponentMenuItem = new JMenuItem(" Receiver Settings "); 
        coverageTestSettingsMenuItem = new JMenuItem(" Coverage Test Settings "); 
        mapSettingsMenuItem = new JMenuItem(" Map Settings "); 
        mapBulkDownloaderMenuItem = new JMenuItem(" Bulk Downloader "); 
        mapLayerSelectorMenuItem = new JMenuItem(" Layer Selector "); 
        mapStatisticsMenuItem = new JMenuItem(" Statistics Panel "); 
        mapClearMenuItem = new JMenuItem(" Clear Map "); 
        newDataFileMenuItem = new JMenuItem(" New Database File "); 
        openDataFileMenuItem = new JMenuItem(" Open Database File "); 
        closeDataFileMenuItem = new JMenuItem(" Close Database File "); 
        saveDataFileMenuItem = new JMenuItem(" Save Database File "); 
        saveAsDataFileMenuItem = new JMenuItem(" Save Database File As "); 
        printPreviewMenuItem = new JMenuItem(" Print Preview "); 
        printMenuItem = new JMenuItem(" Print... "); 
        exitMenuItem = new JMenuItem(" Exit "); 
        staticSignalLocationSettingsMenuItem = new JMenuItem(" Static Signal Location Settings "); 
        signalAnalysisMenuItem = new JMenuItem(" Signal Analysis Monitor "); 
        databaseConfigMenuItem = new JMenuItem(" Database Configuration "); 
        aboutMenuItem = new JMenuItem(" About SignalTrack "); 

        fileMenu.add(newDataFileMenuItem);
        fileMenu.add(openDataFileMenuItem);
        fileMenu.add(closeDataFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveDataFileMenuItem);
        fileMenu.add(saveAsDataFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(printPreviewMenuItem);
        fileMenu.add(printMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        gpsMenu.add(gpsComponentMenuItem);

        receiverMenu.add(receiverComponentMenuItem);

        systemMenu.add(coverageTestSettingsMenuItem);

        mapMenu.add(mapSettingsMenuItem);
        mapMenu.add(mapBulkDownloaderMenuItem);
        mapMenu.add(mapStatisticsMenuItem);
        mapMenu.add(mapLayerSelectorMenuItem);
        mapMenu.addSeparator();
        mapMenu.add(mapClearMenuItem);
        
        signalMeterArray = new SignalMeterArray();

        gpsInfoPanel = new JPanel();
        
        gpsCompassRose = new CompassRose(32,10);
        
        aprsMenu.add(aprsComponentMenuItem);

        staticSignalLocationMenu.add(staticSignalLocationSettingsMenuItem);
        
        signalAnalysisMenu.add(signalAnalysisMenuItem);
        
        databaseConfigMenu.add(databaseConfigMenuItem);

        helpMenu.add(aboutMenuItem);

        longitude = new JLabel();
        latitude = new JLabel();
        utcLabel = new JLabel();
        speedMadeGood = new JLabel();
        cursorLatitude = new JLabel();
        cursorLongitude = new JLabel();
        viewingAltitude = new JLabel();
        cursorTerrainAMSL = new JLabel();
        nmeaSentenceStringLabel = new JLabel();
        gpsStatus = new JLabel();
        gpsTxData = new JLabel();
        gpsRxData = new JLabel();
        gpsCTS = new JLabel();
        gpsDSR = new JLabel();
        gpsCD = new JLabel();
        radioStatus = new JLabel();
        radioTxData = new JLabel();
        radioRxData = new JLabel();
        radioCTS = new JLabel();
        radioDSR = new JLabel();
        radioCD = new JLabel();
        aprsGPWPLSentence = new JLabel();
        signalQuality = new JLabel[10];
        aprsLatitude = new JLabel();
        aprsLongitude = new JLabel();
        aprsCallSign = new JLabel();
        aprsSSID = new JLabel();
        aprsStatus = new JLabel();
        aprsTxData = new JLabel();
        aprsRxData = new JLabel();
        aprsCTS = new JLabel();
        aprsDSR = new JLabel();
        aprsCD = new JLabel();
        markerID = new JLabel();
        recordCountLabel = new JLabel();
        logFileNameLabel = new JLabel();
        currentMGRS = new JLabel();
        cursorMGRS = new JLabel();
        currentGridSquare = new JLabel();
        cursorTestTileReference = new JLabel();
        measurementPeriod = new JLabel();
        measurementsThisTile = new JLabel();
        maxMeasurementsPerTile = new JLabel();
        minMeasurementsPerTile = new JLabel();
        tilesCompleted = new JLabel();
        totalTiles = new JLabel();
        averageRssiInCurrentTile = new JLabel();
        averageBerInCurrentTile = new JLabel();
        averageSinadInCurrentTile = new JLabel();
        aprsComponent = new AprsComponent();
        coverageTestSettings = new CoverageTestSettings(clearAllPrefs);
        databaseConfig = new DatabaseConfiguration(clearAllPrefs);
        mapSettings = new MapSettings();
        staticTestSettings = new StaticTestSettings();
        signalAnalysis = new SignalAnalysis();
        radioComponent = new RadioComponent(startRadioArg, clearAllPrefs);
        serialGPSComponent = new SerialGPSComponent(startGpsArg, clearAllPrefs);
        
        if (startMapArg != -1) {
        	mapSettings.setMapType(startMapArg);
        }
        
        switch (mapSettings.getMapType()) {
            case 0:
                map = new ImageMap(systemPref.getInt("SelectedMapIndex", -1)); 
                map.setScale(startupScale);
                break;
            case 1:
                map = new WorldWindMap(startupLonLat, startupZoom, PREFERRED_MAP_SIZE, false);
                break;
            case 2:
                map = new OpenStreetMapPanel(startupLonLat, startupZoom, PREFERRED_MAP_SIZE);
                break;
            default:
            	break;
        }
    }

    private void initializeListeners() {
    	mapMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
        			mapPanelLeftMouseButtonClicked(e);
        		}
        		if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
        			mapPanelRightMouseButtonClicked(e);
        		}
            }
            @Override
			public void mousePressed(final MouseEvent event) {			
			
			}
			@Override
			public void mouseReleased(final MouseEvent event) {			
			
			}
			@Override
			public void mouseEntered(final MouseEvent event) {			
				mouseOffGlobe = false;
			}
			@Override
			public void mouseExited(final MouseEvent event) {
				setMouseOffGlobe();
			}
            
        };

        mapMouseMotionListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mapPanelMouseMoved(map.getMouseCoordinates(), 
                		coverageTestSettings.getTileSizeArcSeconds(map.getMouseCoordinates().getY()),
                		coverageTestSettings.getGridReference(), coverageTestSettings.getGridSize());
                boolean cursorHand = map.getAttribution().handleAttributionCursor(e.getPoint());
                if (cursorHand) {
                    map.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            	mapPanelDragged(e);
            }
        };
        
        jmvEventListener = new JMapViewerEventListener() {
			@Override
			public void processCommand(JMVCommandEvent command) {
				if (command.getCommand().equals(JMVCommandEvent.Command.ZOOM)) {
		        	updateZoomParameters((int) command.getNewValue());
		        }
		        if (command.getCommand().equals(JMVCommandEvent.Command.MOVE)) {
		            
		        }
		        if (command.getCommand().equals(JMVCommandEvent.Command.ZOOM_IN_DISABLED)) {
		            disableZoomIn((boolean) command.getNewValue());
		        }
		        if (command.getCommand().equals(JMVCommandEvent.Command.ZOOM_OUT_DISABLED)) {
		            disableZoomOut((boolean) command.getNewValue());
		        }
			}
        };
        
        mapKeyListener = new KeyListener() {
    		@Override
    		public void keyPressed(KeyEvent event) {
    			if (event.getKeyCode() == KeyEvent.VK_SPACE) {
    				mapPanelSpaceKeyPressed(event);
    			}
    			if (event.getKeyCode() == KeyEvent.VK_LEFT) {
    				mapPanelLeftArrowKeyPressed(event);
    			}
    			if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
    				mapPanelRightArrowKeyPressed(event);
    			}
    			if (event.getKeyCode() == KeyEvent.VK_ENTER) {
    				mapPanelEnterKeyPressed(event);
    			}
    		}
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyReleased(KeyEvent event) {
				
			}
    	};
    	
    	mapPropertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (MapInterface.MOUSE_OFF_GLOBE.equals(event.getPropertyName())) {
	            	setMouseOffGlobe();
	            }
				if (MapInterface.MAP_READY.equals(event.getPropertyName())) {
			        updateStaticTestSettings(null);
			        updateCoverageTestSettings(null);
			        updateMapSettings(null);
			        updateAprsSettings(null);
	            }
			}
    	};
    	
    	networkTime.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(final PropertyChangeEvent event) {
	            if (NetworkTime.OFFSET.equals(event.getPropertyName())) {
	            	networkTimeIsAvailable(event);
	            }
	            if (NetworkTime.CLOCK.equals(event.getPropertyName())) {
	            	networkClockUpdate(event);
	            }
	            if (NetworkTime.FAIL.equals(event.getPropertyName())) {
	            	networkClockFailure(event);
	            }
	            if (NetworkTime.STRATA_CHANGE.equals(event.getPropertyName())) {
	            	timeStrataChangeEvent(event);
	            }
	            if (NetworkTime.CLOCK_STOPPED.equals(event.getPropertyName())) {
	            	signalReadyToExit(NETWORK_CLOCK_STOPPED);
	            }
	            if (NetworkTime.UPDATES_STOPPED.equals(event.getPropertyName())) {
	            	signalReadyToExit(TIME_UPDATES_STOPPED);
	            }
        	}
        });
    	
    	networkTime.startAutomaticNetworkTimeUpdates();
    	networkTime.startClock();
    	
        nicCheck.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(final PropertyChangeEvent event) {
	            if (NetworkInterfaceCheck.ONLINE.equals(event.getPropertyName())) {
	            	networkInterfaceIsAvailable(event);
	            }
	            if (NetworkInterfaceCheck.READY_TO_EXIT.equals(event.getPropertyName())) {
	            	signalReadyToExit(NETWORK_INTERFACE_CHECK_COMPLETE);
	            }
        	}
        });
        
        nicCheck.adviseOnNetworkInterfaceAvailability();
        
    	databaseListener = new PropertyChangeListener() {
        	@Override
        	public void propertyChange(final PropertyChangeEvent event) {
        		if (DerbyRelationalDatabase.DATABASE_OPEN.equals(event.getPropertyName())) {
        			databaseOpenEvent(event);
        		}
        		if (DerbyRelationalDatabase.DATABASE_CLOSED.equals(event.getPropertyName())) {
        			databaseClosedEvent(event);
        		}
        		if (DerbyRelationalDatabase.DATABASE_RESTORE_PROGRESS.equals(event.getPropertyName())) {
        			updateOpenDatabaseMonitor((int) event.getNewValue());
        		}
        		if (DerbyRelationalDatabase.MEASUREMENT_SET_APPENDED.equals(event.getPropertyName())) {
        			measurementSetTableAppendedEvent(event);
        		}
        		if (DerbyRelationalDatabase.STATIC_MEASUREMENT_APPENDED.equals(event.getPropertyName())) {
        			staticTableAppendedEvent(event);
        		}
        		if (DerbyRelationalDatabase.TILE_TABLE_APPENDED.equals(event.getPropertyName())) {
        			tileTableAppendedEvent(event);
        		}
        		if (DerbyRelationalDatabase.TILE_DELETED.equals(event.getPropertyName())) {
        			tileDeletedEvent(event);
        		}
        		if (DerbyRelationalDatabase.MEASUREMENT_SET_RECORD_COUNT_READY.equals(event.getPropertyName())) {
        			measurementSetRecordCountReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.STATIC_MEASUREMENT_COUNT_READY.equals(event.getPropertyName())) {
        			staticRecordCountReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.TILE_COMPLETE_COUNT_READY.equals(event.getPropertyName())) {
        			tileCompleteCountReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.TILE_COUNT_READY.equals(event.getPropertyName())) {
        			tileRecordCountReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.MEASUREMENT_SET_READY.equals(event.getPropertyName())) {
        			measurementSetDataReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.STATIC_MEASUREMENT_RECORD_READY.equals(event.getPropertyName())) {
        			staticMeasurementDataReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.SETTINGS_READY.equals(event.getPropertyName())) {
        			testSettingsLoadedEvent(event);
        		}
        		if (DerbyRelationalDatabase.TILE_READY.equals(event.getPropertyName())) {
        			tileRecordReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.TILE_RETRIEVAL_PROGRESS.equals(event.getPropertyName())) {
        			updateCoverageTestRestoreMonitorProgress((int) event.getNewValue());
        		}
        		if (DerbyRelationalDatabase.STATIC_RETRIEVAL_PROGRESS.equals(event.getPropertyName())) {
        			updateStaticTestRestoreMonitorProgress((int) event.getNewValue());
        		}
        		if (DerbyRelationalDatabase.ALL_TILE_RECORDS_READY.equals(event.getPropertyName())) {
        			allTileRecordsReadyEvent(event);
        		}
        		if (DerbyRelationalDatabase.ALL_STATIC_RECORDS_READY.equals(event.getPropertyName())) {
        			allStaticRecordsReadyEvent(event);
        		}
        	}
        };
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
            	if (WindowEvent.WINDOW_CLOSING == event.getID()) {
            		frameClosingWindowEvent(event);
            	}   
            }
        });
        
        zoomOutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == Event.ACTION_EVENT) {
					zoomOutButtonMousePressed(event);
				}
			}
        });
        
        zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == Event.ACTION_EVENT) {
					zoomInButtonMousePressed(event);
				}
			}
        });
        
        centerOnGpsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					centerMapOnGpsPosition();
				}
			}
        });
        
        zoomOutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent event) {
                zoomOutButtonMouseReleased(event);
            }
        });
        
        zoomInButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent event) {
                zoomInButtonMouseReleased(event);
            }
        });
        
        map.addPropertyChangeListener(mapPropertyChangeListener);
        map.addMouseListener(mapMouseListener);
        map.addMouseMotionListener(mapMouseMotionListener);
        map.addKeyListener(mapKeyListener);
        map.addJMVListener(jmvEventListener);
        
        aprsComponent.getSerialConfig().getSerialInterface().addPropertyChangeListener(aprsSerialInterfaceListener);
        aprsComponent.getAPRSInterface().addPropertyChangeListener(aprsDeviceInterfaceListener); 
        
        radioComponent.addPropertyChangeListener(radioComponentListener);		
        radioComponent.getSerialConfig().getSerialInterface().addPropertyChangeListener(radioSerialInterfaceListener);
		radioComponent.getRadioInterface().addPropertyChangeListener(radioDeviceInterfaceListener);	
        
		serialGPSComponent.addPropertyChangeListener(serialGPSComponentListener);
        serialGPSComponent.getSerialConfig().getSerialInterface().addPropertyChangeListener(gpsSerialInterfaceListener);
        serialGPSComponent.getGPSInterface().addPropertyChangeListener(gpsDeviceInterfaceListener);

        gpsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if(gpsButton.isSelected()){
				        startGps();
					} else {
				        stopGps();
				    }
				}
			}
        });
        
        radioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if(radioButton.isSelected()){
				        startRadio();
					} else {
				        stopRadio();
				    }
				}
			}
        });
        
        staticLocationAnalysisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if(staticLocationAnalysisButton.isSelected()){
						startStaticTest();
					} else {
						stopStaticTest();
				    }
				}
			}
        });
        
        sinadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if(sinadButton.isSelected()){
						startSINAD();
					} else {
						stopSINAD();
				    }
				}
			}
        });
        
        aprsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if(aprsButton.isSelected()){
						startAPRS();
					} else {
						stopAPRS();
				    }
				}
			}
        });

        learnModeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if(learnModeButton.isSelected()){
						startLearnMode();
					} else {
						stopLearnMode();
				    }
				}
			}
        });
        
        coverageTestButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if(coverageTestButton.isSelected()){
						startCoverageTest();
					} else {
						stopCoverageTest();
				    }
				}
			}
        });

        newDataFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					newDataFileMenuItem.doClick();
				}
			}
        });
        
        openDataFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					openDataFileMenuItem.doClick();
				}
			}
        });
        
        saveDataFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					saveDataFileMenuItem.doClick();
				}
			}
        });
        
        closeDataFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if (closeDataFileButton.isSelected()) {
						closeDataFileMenuItem.doClick();
					}
				}
			}
        });
        
        stopDataFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if (stopDataFileButton.isSelected()) {
						setDataMode(DataMode.STOP);
					}
				}
			}
        });

        recordDataFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getID() == ActionEvent.ACTION_PERFORMED) {
					if (recordDataFileButton.isSelected()) {
						setDataMode(DataMode.RECORD);
					}
				}
			}
        });

        newDataFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewDataFile(null);
            }
        });

        openDataFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openDataFileMenuItemActionListenerEvent(event);
            }
        });

        closeDataFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                closeDataFileMenuItemActionListenerEvent(event);
            }
        });

        saveDataFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveDataFileMenuItemActionListenerEvent(event);
            }
        });

        saveAsDataFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveAsDataFileMenuItemActionListenerEvent(event);
            }
        });
        
        printPreviewMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                printPreviewMenuItemActionListenerEvent(event);
            }
        });

        printMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                printMenuItemActionListenerEvent(event);
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                exitMenuItemActionListenerEvent(event);
            }
        });

        aprsComponentMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                aprsComponentMenuActionListenerEvent(event);
            }
        });

        gpsComponentMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                gpsComponentMenuActionListenerEvent(event);
            }
        });

        receiverComponentMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                receiverComponentMenuActionListenerEvent(event);
            }
        });

        coverageTestSettingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                coverageTestSettingsMenuActionListenerEvent(event);
            }
        });

        mapSettingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mapSettingsMenuActionListenerEvent(event);
            }
        });

        mapBulkDownloaderMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mapBulkDownloaderMenuActionListenerEvent(event);
            }
        });
        
        mapStatisticsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mapStatisticsMenuActionListenerEvent(event);
            }
        });
        
        mapLayerSelectorMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mapLayerSelectorMenuActionListenerEvent(event);
            }
        });
        
        staticSignalLocationSettingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                staticSignalLocationSettingsMenuActionListenerEvent(event);
            }
        });
        
        signalAnalysisMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                signalAnalysisMenuActionListenerEvent(event);
            }
        });
        
        mapClearMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mapClearMenuActionListenerEvent(event);
            }
        });
        
        databaseConfigMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                databseConfigMenuActionListenerEvent(event);
            }
        });
        
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                aboutMenuActionListenerEvent(event);
            }
        });

        ActionListener gpsRxDataTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                gpsRxDataTimerActionListenerEvent(event);
            }
        };

        ActionListener zoomOutMouseDownTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                zoomOutMouseDownTimerActionListenerEvent(event);
            }
        };

        ActionListener zoomInMouseDownTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                zoomInMouseDownTimerActionListenerEvent(event);
            }
        };

        ActionListener gpsTxDataTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                gpsTxDataTimerActionListenerEvent(event);
            }
        };

        ActionListener aprsRxDataTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                aprsRxDataTimerActionListenerEvent(event);
            }
        };

        ActionListener aprsTxDataTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                aprsTxDataTimerActionListenerEvent(event);
            }
        };

        ActionListener radioRxDataTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                radioRxDataTimerActionListenerEvent(event);
            }
        };

        ActionListener radioTxDataTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                radioTxDataTimerActionListenerEvent(event);
            }
        };

        ActionListener gpsValidHeadingTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                gpsValidHeadingTimerActionListenerEvent(event);
            }
        };

        ActionListener measurementTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                measurementTimerActionListenerEvent(event);
            }
        };

        ActionListener periodTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                periodTimerActionListenerEvent(event);
            }
        };

        serialGPSComponent.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				gpsSettingsPropertyChangeEvent(event);
			}
        });
        
        staticTestSettings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (event.getPropertyName().equals(StaticTestSettings.APPLY_SETTINGS)) {
					updateStaticTestSettings(event);
				}
			}
        });
        
        coverageTestSettings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (CoverageTestSettings.PROPERTY_CHANGE.equals(event.getPropertyName())) {
					updateCoverageTestSettings(event);
				}
			}
        });

        aprsComponent.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				updateAprsSettings(event);
			}
        });

        mapSettings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if ("NEW_MAP_TYPE".equals(event.getPropertyName())) { 
					mapTypeChangedChangeListenerEvent(event);
				}
				if ("PROPERTY_CHANGED".equals(event.getPropertyName())) { 
					mapPropertyChangedChangeListenerEvent(event);
				}
			}
        });
        
        String cancelName = "cancel"; 
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        
        actionMap.put(cancelName, new AbstractAction() {
			private static final long serialVersionUID = 8202337975538704917L;
			@Override
			public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }); 
        
        gpsRxDataTimer = new Timer(250, gpsRxDataTimerActionListener);
        gpsTxDataTimer = new Timer(250, gpsTxDataTimerActionListener);
        radioRxDataTimer = new Timer(250, radioRxDataTimerActionListener);
        radioTxDataTimer = new Timer(250, radioTxDataTimerActionListener);
        aprsRxDataTimer = new Timer(250, aprsRxDataTimerActionListener);
        aprsTxDataTimer = new Timer(250, aprsTxDataTimerActionListener);
        gpsValidHeadingTimer = new Timer(2000, gpsValidHeadingTimerActionListener);
        gpsValidHeadingTimer.setRepeats(true);
        measurementTimer = new Timer(1000, measurementTimerActionListener);
        measurementTimer.setRepeats(true);
        zoomOutMouseDownTimer = new Timer(300, zoomOutMouseDownTimerActionListener);
        zoomInMouseDownTimer = new Timer(300, zoomInMouseDownTimerActionListener);
        periodTimer = new Timer(60000, periodTimerActionListener);
        periodTimer.setRepeats(true);

        if (coverageTestSettings.getMinSamplesPerTile() == -1) {
            periodTimer.setDelay(coverageTestSettings.getMinTimePerTile() * 1000);
            periodTimer.setInitialDelay(coverageTestSettings.getMinTimePerTile() * 1000);
        }
    }
    
    private void initializeComponentListeners() { 
	    aprsSerialInterfaceListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	    		if (SerialInterface.CTS.equals(event.getPropertyName())) {
	    			aprsCTSHoldingChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.DSR.equals(event.getPropertyName())) {
	    			aprsDSRHoldingChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.RLSD.equals(event.getPropertyName())) {
	    			aprsCDHoldingChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.RX_CHAR.equals(event.getPropertyName())) {
	    			aprsRxCharChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.TX_DATA.equals(event.getPropertyName())) {
	    			aprsTxDataChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.INVALID_COM_PORT.equals(event.getPropertyName())) {
	    			invalidComPortChangeListenerEvent(event, "APRS"); 
	    		}
	    		if (SerialInterface.PORT_CLOSED.equals(event.getPropertyName())) {
	    			signalReadyToExit(APRS_INTERFACE_CLOSED);
	    		}
	    		if (SerialInterface.SERIAL_PORT_CONFIGURATION_ERROR.equals(event.getPropertyName())) {
	    			if (event.getNewValue().getClass().equals(Integer.class)) {
	    				serialErrorChangeListenerEvent((int) event.getNewValue(), "APRS Port Configuration Error"); 
	    		    }
	    		    else if (event.getNewValue().getClass().equals(String.class)) {
	    		    	serialErrorChangeListenerEvent((String) event.getNewValue(), "APRS Port Configuration Error"); 
	    		    }
	    		}
	    		if (SerialInterface.ERROR.equals(event.getPropertyName())) {
	    			if (event.getNewValue().getClass().equals(Integer.class)) {
	    				serialErrorChangeListenerEvent((int) event.getNewValue(), "APRS Error"); 
	    		    }
	    		    else if (event.getNewValue().getClass().equals(String.class)) {
	    		    	serialErrorChangeListenerEvent((String) event.getNewValue(), "APRS Error"); 
	    		    }
	    		}
	    	}
	    };
	    
	    aprsDeviceInterfaceListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	            if (APRSInterface.WAYPOINT.equals(event.getPropertyName())) {
	            	aprsWaypointListenerEvent(event);
	            }
	    	}
	    }; 
    	
	    radioComponentListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	    		if (RadioComponent.SERIAL_PORT_ERROR.equals(event.getPropertyName())) {
	    			resetRadioIndicators();
	    			 stopCoverageTest();
	    			deviceNotOnlineException((String[]) event.getNewValue());
	    		}
	    		if (RadioComponent.SCAN_MODE_CHANGE.equals(event.getPropertyName())) {
	    			if (testMode != TestMode.OFF) signalMeterArray.setMeterLevels(0);
	    		}
	    	}
	    };
	    
	    radioSerialInterfaceListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	    		if (SerialInterface.CTS.equals(event.getPropertyName())) {
	    			radioCTSHoldingChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.DSR.equals(event.getPropertyName())) {
	    			radioDSRHoldingChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.RLSD.equals(event.getPropertyName())) {
	    			radioCDHoldingChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.RX_CHAR.equals(event.getPropertyName())) {
	    			radioRxCharChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.TX_DATA.equals(event.getPropertyName())) {
	    			radioTxDataChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.ONLINE.equals(event.getPropertyName())) {
	    			radioOnlineChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.INVALID_COM_PORT.equals(event.getPropertyName())) {
	    			invalidComPortChangeListenerEvent(event, "Radio"); 
	    		}
	    		if (SerialInterface.PORT_CLOSED.equals(event.getPropertyName())) {
	    			signalReadyToExit(RADIO_INTERFACE_CLOSED);
	    		}
	    		if (SerialInterface.SERIAL_PORT_CONFIGURATION_ERROR.equals(event.getPropertyName())) {
	    			if (event.getNewValue().getClass().equals(Integer.class)) {
	    				serialErrorChangeListenerEvent((int) event.getNewValue(), "Radio Port Configuration Error"); 
	    		    }
	    		    else if (event.getNewValue().getClass().equals(String.class)) {
	    		    	serialErrorChangeListenerEvent(event.getSource().getClass().getName(), "Radio Port Configuration Error"); 
	    		    }
	    		}
	    		if (SerialInterface.ERROR.equals(event.getPropertyName())) {
	    			if (event.getNewValue().getClass().equals(Integer.class)) {
	    				serialErrorChangeListenerEvent((int) event.getNewValue(), "Radio Error"); 
	    		    }
	    		    else if (event.getNewValue().getClass().equals(String.class)) {
	    		    	serialErrorChangeListenerEvent(event.getSource().getClass().getName(), "Radio Error"); 
	    		    }
	    		}
	    	}
	    };
	   
	    radioDeviceInterfaceListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	    		if (RadioInterface.RSSI.equals(event.getPropertyName())) {
	    			radioRSSIChangeListenerEvent(event);
	    		}
	    		if (RadioInterface.BUSY.equals(event.getPropertyName())) {
	    			radioBusyChangeListenerEvent(event);
	    		}
	    		if (RadioInterface.RX_DATA.equals(event.getPropertyName())) {
	    			radioRxDataChangeListenerEvent(event);
	    		}
	    		if (RadioInterface.POWER.equals(event.getPropertyName())) {
	    			radioPowerChangeListenerEvent(event);
	    		}
	    		if (RadioInterface.BER.equals(event.getPropertyName())) {
	    			radioBERChangeListenerEvent(event);
	    		}
	    		if (RadioInterface.READY.equals(event.getPropertyName())) {
	    			radioReadyChangeListenerEvent(event);
	    		}
	    		if (RadioInterface.SCAN_CHANNEL_READY.equals(event.getPropertyName())) {
	    			radioScanChannelReadyChangeListenerEvent(event);
	    		}
	    	}
	    };	
	    
	    gpsSerialInterfaceListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	    		if (SerialInterface.RX_CHAR.equals(event.getPropertyName())) {
	    			gpsRxDataPropertyChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.PORT_CLOSED.equals(event.getPropertyName())) {
	    			signalReadyToExit(GPS_INTERFACE_CLOSED);
	    		}
	    		if (SerialInterface.SERIAL_PORT_CONFIGURATION_ERROR.equals(event.getPropertyName())) {
	    			if (event.getNewValue().getClass().equals(Integer.class)) {
	    				serialErrorChangeListenerEvent((int) event.getNewValue(), "GPS Port Configuration Error"); 
	    		    }
	    		    else if (event.getNewValue().getClass().equals(String.class)) {
	    		    	serialErrorChangeListenerEvent((String) event.getNewValue(), "GPS Port Configuration Error"); 
	    		    }
	    		}
	    		if (SerialInterface.ERROR.equals(event.getPropertyName())) {
	    			if (event.getNewValue().getClass().equals(Integer.class)) {
	    				serialErrorChangeListenerEvent((int) event.getNewValue(), "GPS Error"); 
	    		    }
	    		    else if (event.getNewValue().getClass().equals(String.class)) {
	    		    	serialErrorChangeListenerEvent((String) event.getNewValue(), "GPS Error"); 
	    		    }
	    		}
	    		if (SerialInterface.DSR.equals(event.getPropertyName())) {
	    			gpsDSRHoldingPropertyChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.CTS.equals(event.getPropertyName())) {
	    			gpsCTSHoldingPropertyChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.RLSD.equals(event.getPropertyName())) {
	    			gpsCDHoldingPropertyChangeListenerEvent(event);
	    		}
	    		if (SerialInterface.INVALID_COM_PORT.equals(event.getPropertyName())) {
	    			invalidComPortChangeListenerEvent(event, "GPS"); 
	    		}
	    		if (SerialInterface.ONLINE.equals(event.getPropertyName())) {
	    			gpsOnlineChangeListenerEvent(event);
	    		}
	    	}
	    };
	
	    gpsDeviceInterfaceListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	    		if (GPSInterface.VALID_WAYPOINT.equals(event.getPropertyName())) {
	    			aprsWaypointListenerEvent(event);
	    		}
	    		if (GPSInterface.VALID_FIX.equals(event.getPropertyName())) {
	    			gpsValidFixPropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.VALID_POSITION.equals(event.getPropertyName())) {
	    			gpsValidPositionPropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.VALID_TIME.equals(event.getPropertyName())) {
	    			gpsValidTimePropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.RDF_HEADING_RELATIVE.equals(event.getPropertyName())) {
	    			rdfHeadingRelativePropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.RDF_HEADING_TRUE.equals(event.getPropertyName())) {
	    			rdfHeadingTruePropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.FIX_QUALITY.equals(event.getPropertyName())) {
	    			gpsFixQualityPropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.FAA_MODE.equals(event.getPropertyName())) {
	    			
	    		}
	    		if (GPSInterface.COURSE_MADE_GOOD_TRUE.equals(event.getPropertyName())) {
	    			gpsCourseMadeGoodTruePropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.COURSE_MADE_GOOD_MAGNETIC.equals(event.getPropertyName())) {
	    			
	    		}
	    		if (GPSInterface.NMEA_DATA.equals(event.getPropertyName())) {
	    			gpsReceivedDataPropertyChangeListenerEvent(event);
	    		}
	    		if (GPSInterface.CRC_ERROR.equals(event.getPropertyName())) {
	    			gpsCRCErrorEvent(event);
	    		}
	    	}
	    };
	    
	    serialGPSComponentListener = new PropertyChangeListener() {
	    	@Override
	    	public void propertyChange(final PropertyChangeEvent event) {
	    		if (SerialGPSComponent.SERIAL_PORT_ERROR.equals(event.getPropertyName())) {
	    			resetGPSIndicators();
	    			deviceNotOnlineException((String[]) event.getNewValue());
	    		}
	    	}
	    };
    }
    
    void setMouseOffGlobe() {
    	mouseOffGlobe = true;
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				cursorTestTileReference.setText("Off Globe"); 
				cursorMGRS.setText(""); 
				cursorLongitude.setText(""); 
				cursorLatitude.setText(""); 
            }
        });
    }

    private void timeStrataChangeEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	setUTCLabelColors((int) event.getNewValue());
            }
        });
    }
    
    private void networkClockUpdate(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	utcLabel.setText(event.getNewValue() + " Z");
            	setStrataTextTag(networkTime.getTimeStratum(), utcLabel);
            }
        });
    }

    private void networkClockFailure(final PropertyChangeEvent event) {
    	
    }
    
    private void networkTimeIsAvailable(final PropertyChangeEvent event) {

    }
    
    private void setUTCLabelColors(final int timeStrata) {
    	if (timeStrata == NetworkTime.STRATUM_GPS || timeStrata == NetworkTime.STRATUM_NTP0) {
			utcLabel.setBackground(Color.BLUE);
	    	utcLabel.setForeground(Color.WHITE);
    	}
    	if (timeStrata >= NetworkTime.STRATUM_NTP1 && timeStrata <= NetworkTime.STRATUM_NTP3) {
			utcLabel.setBackground(Color.GREEN);
	    	utcLabel.setForeground(Color.BLACK);
    	}
    	if (timeStrata >= NetworkTime.STRATUM_NTP4 && timeStrata <= NetworkTime.STRATUM_NTP15) {
			utcLabel.setBackground(Color.YELLOW);
	    	utcLabel.setForeground(Color.BLACK);
    	}
    	if (timeStrata == NetworkTime.STRATUM_UNSYNC) {
			utcLabel.setBackground(Color.RED);
	    	utcLabel.setForeground(Color.BLACK);
    	}
    }
    
    private void setStrataTextTag(final int timeStrata, JLabel utcLabel) {
    	if (timeStrata == NetworkTime.STRATUM_GPS) {
    		utcLabel.setText(utcLabel.getText() + "  STR GPS");
    	}
    	else if (timeStrata >= NetworkTime.STRATUM_NTP0 && timeStrata <= NetworkTime.STRATUM_NTP15) {
    		utcLabel.setText(utcLabel.getText() + "  STR NTP" + String.valueOf(timeStrata));
    	}
    	else if (timeStrata == NetworkTime.STRATUM_UNSYNC) {
    		utcLabel.setText(utcLabel.getText() + "  UNSYNC");
    	}
    }
    
    private void networkInterfaceIsAvailable(final PropertyChangeEvent event) {
    	networkTime.requestNetworkTime();
    }

	private void rdfHeadingTruePropertyChangeListenerEvent(final PropertyChangeEvent event) {
        cursorBearingPosition = map.getMouseCoordinates();
        cursorBearing = (double) event.getNewValue();
        cursorBearingIndex = addRdfBearing(cursorBearingPosition, cursorBearing,
        	RDF_BEARING_LENGTH_IN_DEGREES, serialGPSComponent.getGPSInterface().getRdfQuality(), Color.RED);
        map.addRing(cursorBearingPosition, 6, Color.RED);
        processBearingInformation();
    }

    private void rdfHeadingRelativePropertyChangeListenerEvent(final PropertyChangeEvent event) {
        
    }

    private void processBearingInformation() {
    	triangulate = new Triangulate(bearingList);
		triangulate.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (StateValue.DONE.toString().equals(event.getNewValue())) {
		        	triangulationComplete(event);
		        }
			}
		});
		triangulate.execute();
    }

    private void triangulationComplete(final PropertyChangeEvent event) {
    	double ringSize = Math.max(pointStandardDeviation(triangulate.getIntersectList()) * 3000, 20);
		map.setTargetRing(triangulate.getIntersectPoint(), (int) ringSize, Color.GREEN);
		map.showTargetRing(true);
    }
    
    private int addRdfBearing(final Point.Double p1, final double bearing, final double length, 
    		final GPSInterface.RdfQuality quality, final Color color) {
        int index = bearingList.size();
        bearingList.add(index, new Bearing(index, p1, bearing, length, getRdfQuality(quality), color));
        
        Point.Double p2 = new Point.Double((Math.sin(bearing * Math.PI / 180) * length) + p1.x, 
        		(Math.cos(bearing * Math.PI / 180) * length) + p1.y);

        map.addLine(p1, p2, color);
        
        return index;
    }

    private void moveRdfBearing(final int index, final Point.Double p1, final double bearing, 
    		final double length, final int quality, final Color color) {
    	map.deleteLine(index);
        bearingList.remove(index);
        
        bearingList.add(index, new Bearing(index, p1, bearing, length, quality, color));
        
        Point.Double p2 = new Point.Double((Math.sin(bearing * Math.PI / 180) * length) + p1.x, 
        		(Math.cos(bearing * Math.PI / 180) * length) + p1.y);

        map.addLine(p1, p2, color);
    }

    private void aboutMenuActionListenerEvent(ActionEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                    "SignalTrack version 7.3.1036" + System.lineSeparator() + "(c) Copyright John R. Chartkoff, 2015.  All rights reserved.", 
                    "About SignalTrack", JOptionPane.INFORMATION_MESSAGE); 
            }
        });
    }

    private void serialErrorChangeListenerEvent(final int event, final String errorText) {
    	if (serialErrorQueued) return;
    	serialErrorQueued = true;
        try {
	        final String eventMessage = serialPortErrorMessage(event);
	        log.log(Level.WARNING, errorText, eventMessage);
	        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	            @Override
	            public void run() {
	                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
	                	"Serial Port " + eventMessage, errorText, JOptionPane.ERROR_MESSAGE); 
	                serialErrorQueued = false;
	            }
	        });
        } catch (ClassCastException | NumberFormatException ex) {
        	ex.printStackTrace();
        }
    }
    
    private void serialErrorChangeListenerEvent(final String event, final String errorText) {
    	if (serialErrorQueued) return;
    	serialErrorQueued = true;
    	try {
	        log.log(Level.WARNING, errorText, event);
	        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	            @Override
	            public void run() {
	            	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
	                    event, errorText, JOptionPane.ERROR_MESSAGE);
	            	serialErrorQueued = false;
	            }
	        });
        } catch (ClassCastException | NumberFormatException ex) {
        	ex.printStackTrace();
        }
    }

    private void gpsCRCErrorEvent(final PropertyChangeEvent event) {
    	if (event.getNewValue() == null) return;
        log.log(Level.INFO, "GPS_CRC_Error", event.getNewValue()); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	String message = (String) event.getNewValue();
	            String formattedMsg = message.substring(0,Math.min(50,message.length()));
            	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
	                formattedMsg, "GPS CRC Error", JOptionPane.ERROR_MESSAGE); 
            }
        });
    }
    
    private String serialPortErrorMessage(final int eventMask) {
        switch (eventMask) {
        	case 1: return "Parameter Configuration Error"; 
	        case 2: return "Buffer Overrun Error"; 
	        case 4: return "Parity Mismatch Error"; 
	        case 8: return "Framing Error"; 
	        case 16: return "Data Terminal Ready (DTR) Line Not Set As Requested"; 
	        case 32: return "Ready To Send (RTS) Line Not Set As Requested"; 
	        case 64: return "Event Mask Not Set As Requested"; 
	        case 128: return "Flow Control Not Set As Requested"; 
	        case 256: return "Port Not Purged"; 
	        case 512: return "Break Interrupt"; 
	        case 1024: return "Transmit Error"; 
	        case 2048: return "Framing Error"; 
	        case 4096: return "Buffer Overrun Error"; 
	        case 8192: return "Parity Mismatch Error"; 
	        default: return "Unspecified Serial Communications Error"; 
        }
    }
    
    private void mapClearMenuActionListenerEvent(final ActionEvent event) {
    	map.deleteAllSignalMarkers();
        map.deleteAllIcons();
        map.deleteAllLines();
        map.deleteAllQuads();
        map.deleteAllRings();
        map.deleteAllArcs();
        map.deleteAllTestTiles();
        map.deleteAllArcIntersectPoints();
    }

    private void aprsWaypointListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	boolean found = false;
		        for (AprsIcon ai : iconList) {
		            if (aprsComponent.getAPRSInterface().getAprsIdentifier().contains(ai.getIdentifier())) {
		                map.moveIcon(ai.getIndex(), aprsComponent.getAPRSInterface().getAprsPosition());
		                found = true;
		            }
		        }
		        if (!found) {
		            String newSsidPath = System.getProperty("user.home") + File.separator + "maps" + File.separator +  
		            	Utility.getIconPathNameFromSSID(Utility.parseSSID(aprsComponent.getAPRSInterface().getAprsIdentifier()));
		            map.addIcon(aprsComponent.getAPRSInterface().getAprsPosition(), 
		            	newSsidPath, aprsComponent.getAPRSInterface().getAprsIdentifier());
		            iconList.add(new AprsIcon(iconList.size(), aprsComponent.getAPRSInterface().getAprsPosition(), 
		            	aprsComponent.getAPRSInterface().getAprsIdentifier()));
		        }
		        aprsGPWPLSentence.setText(" " + aprsComponent.getAPRSInterface().getGPWPLMessageString()); 
		        aprsLatitude.setText(String.format("%8f", aprsComponent.getAPRSInterface().getAprsPosition().y)); 
		        aprsLongitude.setText(String.format("%9f", aprsComponent.getAPRSInterface().getAprsPosition().x)); 
		        aprsCallSign.setText(Utility.parseCallSign(aprsComponent.getAPRSInterface().getAprsIdentifier())
		            + " - " + Utility.parseSSID(aprsComponent.getAPRSInterface().getAprsIdentifier())); 
		        aprsSSID.setText(Utility.parseSSID(aprsComponent.getAPRSInterface().getAprsIdentifier()));
            }
        });
    }

    private void initializeLookAndFeel() {
    	System.setProperty("java.net.useSystemProxies", "true"); 
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true"); 
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "NASA World Wind"); 
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false"); 
            System.setProperty("apple.awt.brushMetalLook", "true"); 
        } else if (Configuration.isWindowsOS()) {
	        try {
	        	System.setProperty("sun.awt.noerasebackground", "true"); 
	            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); 
	            setDefaultLookAndFeelDecorated(true);
	            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	        } catch (UnsupportedLookAndFeelException ex) {
	            reportException(ex);
	        } catch (IllegalAccessException ex) {
	        	reportException(ex);
	        } catch (InstantiationException ex) {
	        	reportException(ex);
	        } catch (ClassNotFoundException ex) {
	        	reportException(ex);
	        }
        } else if (Configuration.isLinuxOS()) {
    		try {
    			System.setProperty("sun.awt.noerasebackground", "true"); 
			    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); 
				setDefaultLookAndFeelDecorated(true);
	            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} catch (UnsupportedLookAndFeelException ex) {
	            reportException(ex);
	        } catch (IllegalAccessException ex) {
	        	reportException(ex);
	        } catch (InstantiationException ex) {
	        	reportException(ex);
	        } catch (ClassNotFoundException ex) {
	        	reportException(ex);
	        }
        }
    }
    
    private void createToolBar() { 
    	try {
			new IconRetriever(zoomOutButton, getClass().getResource("/bin/images/112_Minus_Green_16x16_72.png")); 
			zoomOutButton.setFocusable(false);
			zoomOutButton.setRolloverEnabled(true);
			zoomOutButton.setMultiClickThreshhold(50L);
			zoomOutButton.setToolTipText("Zoom Out"); 
			
			new IconRetriever(zoomInButton, getClass().getResource("/bin/images/112_Plus_Green_16x16_72.png")); 
			zoomInButton.setFocusable(false);
			zoomInButton.setRolloverEnabled(true);
			zoomInButton.setMultiClickThreshhold(50L);
			zoomInButton.setToolTipText("Zoom In"); 
			
			new IconRetriever(gpsButton, getClass().getResource("/bin/images/Web.png")); 
			gpsButton.setFocusable(false);
			gpsButton.setRolloverEnabled(true);
			gpsButton.setMultiClickThreshhold(50L);
			gpsButton.setToolTipText("Enable GPS Receiver"); 
			
			new IconRetriever(centerOnGpsButton, getClass().getResource("/bin/images/recenterOnLocationButtonImage.jpg")); 
			centerOnGpsButton.setEnabled(false);
			centerOnGpsButton.setFocusable(false);
			centerOnGpsButton.setRolloverEnabled(true);
			centerOnGpsButton.setMultiClickThreshhold(50L);
			centerOnGpsButton.setToolTipText("Center Map on GPS Location"); 
	
			radioButton.setText("RADIO"); 
			radioButton.setPreferredSize(BUTTON_DIM);
			radioButton.setFont(new Font("Calibri", Font.BOLD, 12)); 
			radioButton.setForeground(Color.BLUE);
			radioButton.setRolloverEnabled(true);
			radioButton.setFocusable(false);
			radioButton.setMultiClickThreshhold(50L);
			radioButton.setToolTipText("Enable Radio System"); 
	
			sinadButton.setText("SINAD"); 
			sinadButton.setPreferredSize(BUTTON_DIM);
			sinadButton.setFont(new Font("Calibri", Font.BOLD, 12)); 
			sinadButton.setForeground(Color.BLUE);
			sinadButton.setRolloverEnabled(true);
			sinadButton.setFocusable(false);
			sinadButton.setMultiClickThreshhold(50L);
			sinadButton.setToolTipText("Enable SINAD Meter"); 
	
			aprsButton.setText("APRS"); 
			aprsButton.setPreferredSize(BUTTON_DIM);
			aprsButton.setFont(new Font("Calibri", Font.BOLD, 12)); 
			aprsButton.setForeground(Color.BLUE);
			aprsButton.setRolloverEnabled(true);
			aprsButton.setFocusable(false);
			aprsButton.setMultiClickThreshhold(50L);
			aprsButton.setToolTipText("Enable APRS Position Reporting"); 
			
			learnModeButton.setText("LEARN"); 
			learnModeButton.setPreferredSize(BUTTON_DIM);
			learnModeButton.setFont(new Font("Calibri", Font.BOLD, 12)); 
			learnModeButton.setForeground(Color.RED);
			learnModeButton.setRolloverEnabled(true);
			learnModeButton.setFocusable(false);
			learnModeButton.setMultiClickThreshhold(50L);
			learnModeButton.setToolTipText("Learn Test Tiles to Search"); 
	
			coverageTestButton.setText("DRIVE"); 
			coverageTestButton.setPreferredSize(BUTTON_DIM);
			coverageTestButton.setFont(new Font("Calibri", Font.BOLD, 12)); 
			coverageTestButton.setForeground(Color.RED);
			coverageTestButton.setRolloverEnabled(true);
			coverageTestButton.setFocusable(false);
			coverageTestButton.setMultiClickThreshhold(50L);
			coverageTestButton.setToolTipText("Initialize Coverage Test"); 
	
			staticLocationAnalysisButton.setText("RDF-S"); 
			staticLocationAnalysisButton.setPreferredSize(BUTTON_DIM);
			staticLocationAnalysisButton.setFont(new Font("Calibri", Font.BOLD, 12)); 
			staticLocationAnalysisButton.setForeground(Color.RED);
			staticLocationAnalysisButton.setRolloverEnabled(true);
			staticLocationAnalysisButton.setFocusable(false);
			staticLocationAnalysisButton.setMultiClickThreshhold(50L);
			staticLocationAnalysisButton.setToolTipText("Initialize Static Signal Location Search"); 

			new IconRetriever(newDataFileButton, getClass().getResource("/bin/images/NewDocumentHS.png")); 
			newDataFileButton.setFocusable(false);
			newDataFileButton.setRolloverEnabled(true);
			newDataFileButton.setMultiClickThreshhold(50L);
			newDataFileButton.setToolTipText("Create New Data File"); 
	
			new IconRetriever(openDataFileButton, getClass().getResource("/bin/images/openHS.png")); 
			openDataFileButton.setFocusable(false);
			openDataFileButton.setRolloverEnabled(true);
			openDataFileButton.setMultiClickThreshhold(50L);
			openDataFileButton.setToolTipText("Open Data File"); 
	
			new IconRetriever(saveDataFileButton, getClass().getResource("/bin/images/saveHS.png")); 
			saveDataFileButton.setFocusable(false);
			saveDataFileButton.setRolloverEnabled(true);
			saveDataFileButton.setMultiClickThreshhold(50L);
			saveDataFileButton.setToolTipText("Save Data File"); 
	
			new IconRetriever(closeDataFileButton, getClass().getResource("/bin/images/closefile.gif")); 
			closeDataFileButton.setFocusable(false);
			closeDataFileButton.setRolloverEnabled(true);
			closeDataFileButton.setMultiClickThreshhold(50L);
			closeDataFileButton.setToolTipText("Close Data File"); 
	
			new IconRetriever(stopDataFileButton, getClass().getResource("/bin/images/StopHS.png")); 
			stopDataFileButton.setFocusable(false);
			stopDataFileButton.setRolloverEnabled(true);
			stopDataFileButton.setMultiClickThreshhold(50L);
			stopDataFileButton.setToolTipText("Stop Data File"); 
	
			new IconRetriever(recordDataFileButton, getClass().getResource("/bin/images/RecordHS.png")); 
			recordDataFileButton.setFocusable(false);
			recordDataFileButton.setRolloverEnabled(true);
			recordDataFileButton.setMultiClickThreshhold(50L);
			recordDataFileButton.setToolTipText("Record Data File"); 
			
	        
			new IconRetriever(this, getClass().getResource("/bin/images/route_icon.jpg")); 
		   	
			new IconRetriever(newDataFileMenuItem, getClass().getResource("/bin/images/NewDocumentHS.png")); 
		
    	} catch (URISyntaxException ex) {
			ex.printStackTrace();
		}
    	
		toolBar.add(newDataFileButton);
        toolBar.add(openDataFileButton);
        toolBar.add(saveDataFileButton);
        toolBar.add(closeDataFileButton);
        toolBar.addSeparator();
        toolBar.add(stopDataFileButton);
        toolBar.add(recordDataFileButton);
        toolBar.addSeparator();
        toolBar.add(gpsButton);
        toolBar.add(centerOnGpsButton);
        addToToolbar(radioButton);
        addToToolbar(sinadButton);
        addToToolbar(aprsButton);
        toolBar.addSeparator();
        addToToolbar(coverageTestButton);
        addToToolbar(staticLocationAnalysisButton);
        addToToolbar(learnModeButton);
        toolBar.addSeparator();
        toolBar.add(zoomInButton);
        toolBar.add(zoomOutButton);
    }
    
    private void addToToolbar(Component component) {
        Dimension d = component.getPreferredSize();
        component.setMaximumSize(d);
        component.setMinimumSize(d);
        component.setPreferredSize(d);
        toolBar.add(component);
    }
    
    private void configureComponents() {
    	createToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEtchedBorder());
		
        mapPanel.setLayout(new BorderLayout());
        mapPanel.setDoubleBuffered(true);
        mapPanel.setOpaque(true);
        mapPanel.setBackground(Color.BLACK);
        mapPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY));

		signalMeterArray.setMeterColor(Color.BLUE);

		gpsStatus.setBorder(BorderFactory.createEtchedBorder());
		gpsStatus.setHorizontalAlignment(SwingConstants.CENTER);
		gpsStatus.setFont(new Font("Calabri", Font.BOLD, 10)); 
		gpsStatus.setOpaque(true);
		gpsStatus.setToolTipText("GPS Receive Status"); 
		gpsStatus.setBackground(new Color(127, 0, 0));
		gpsStatus.setText("OFF LINE"); 

		gpsTxData.setBackground(new Color(127, 0, 0));
		gpsTxData.setBorder(BorderFactory.createEtchedBorder());
		gpsTxData.setOpaque(true);
		gpsTxData.setToolTipText("GPS Port - Transmitted Data"); 

		gpsRxData.setBackground(new Color(127, 0, 0));
		gpsRxData.setBorder(BorderFactory.createEtchedBorder());
		gpsRxData.setOpaque(true);
		gpsRxData.setToolTipText("GPS Port - Received Data"); 

		gpsCTS.setBackground(new Color(127, 0, 0));
		gpsCTS.setBorder(BorderFactory.createEtchedBorder());
		gpsCTS.setOpaque(true);
		gpsCTS.setToolTipText("GPS Port - Clear To Send Line Active"); 

		gpsDSR.setBackground(new Color(127, 0, 0));
		gpsDSR.setBorder(BorderFactory.createEtchedBorder());
		gpsDSR.setOpaque(true);
		gpsDSR.setToolTipText("GPS Port - Data Set Ready Line Active"); 

		gpsCD.setBackground(new Color(127, 0, 0));
		gpsCD.setBorder(BorderFactory.createEtchedBorder());
		gpsCD.setOpaque(true);
		gpsCD.setToolTipText("GPS Port - CD Line Active"); 

		radioStatus.setBackground(new Color(127, 0, 0));
		radioStatus.setBorder(BorderFactory.createEtchedBorder());
		radioStatus.setHorizontalAlignment(SwingConstants.CENTER);
		radioStatus.setFont(new Font("Calabri", Font.BOLD, 10)); 
		radioStatus.setText("OFF LINE"); 
		radioStatus.setOpaque(true);
		radioStatus.setToolTipText("Radio Receive Status"); 

		radioTxData.setBackground(new Color(127, 0, 0));
		radioTxData.setBorder(BorderFactory.createEtchedBorder());
		radioTxData.setOpaque(true);
		radioTxData.setToolTipText("Radio Port - Transmitted Data"); 

		radioRxData.setBackground(new Color(127, 0, 0));
		radioRxData.setBorder(BorderFactory.createEtchedBorder());
		radioRxData.setOpaque(true);
		radioRxData.setToolTipText("Radio Port - Received Data"); 

		radioCTS.setBackground(new Color(127, 0, 0));
		radioCTS.setBorder(BorderFactory.createEtchedBorder());
		radioCTS.setOpaque(true);
		radioCTS.setToolTipText("Radio Port - Clear To Send Line Active"); 

		radioDSR.setBackground(new Color(127, 0, 0));
		radioDSR.setBorder(BorderFactory.createEtchedBorder());
		radioDSR.setOpaque(true);
		radioDSR.setToolTipText("Radio Port - Data Set Ready Line Active"); 

		radioCD.setBackground(new Color(127, 0, 0));
		radioCD.setBorder(BorderFactory.createEtchedBorder());
		radioCD.setOpaque(true);
		radioCD.setToolTipText("Radio Port - CD Line Active"); 

		aprsStatus.setBackground(new Color(127, 0, 0));
		aprsStatus.setBorder(BorderFactory.createEtchedBorder());
		aprsStatus.setHorizontalAlignment(SwingConstants.CENTER);
		aprsStatus.setFont(new Font("Calabri", Font.BOLD, 10)); 
		aprsStatus.setText("OFF LINE"); 
		aprsStatus.setOpaque(true);
		aprsStatus.setToolTipText("APRS Receive Status"); 

		aprsTxData.setBackground(new Color(127, 0, 0));
		aprsTxData.setBorder(BorderFactory.createEtchedBorder());
		aprsTxData.setOpaque(true);
		aprsTxData.setToolTipText("APRS Port - Transmitted Data"); 

		aprsRxData.setBackground(new Color(127, 0, 0));
		aprsRxData.setBorder(BorderFactory.createEtchedBorder());
		aprsRxData.setOpaque(true);
		aprsRxData.setToolTipText("APRS Port - Received Data"); 

		aprsCTS.setBackground(new Color(127, 0, 0));
		aprsCTS.setBorder(BorderFactory.createEtchedBorder());
		aprsCTS.setOpaque(true);
		aprsCTS.setToolTipText("APRS Port - Clear To Send Line Active"); 

		aprsDSR.setBackground(new Color(127, 0, 0));
		aprsDSR.setBorder(BorderFactory.createEtchedBorder());
		aprsDSR.setOpaque(true);
		aprsDSR.setToolTipText("APRS Port - Data Set Ready Line Active"); 

		aprsCD.setBackground(new Color(127, 0, 0));
		aprsCD.setBorder(BorderFactory.createEtchedBorder());
		aprsCD.setOpaque(true);
		aprsCD.setToolTipText("APRS Port - CD Line Active"); 

		aprsGPWPLSentence.setBorder(BorderFactory.createEtchedBorder());
		aprsGPWPLSentence.setHorizontalAlignment(SwingConstants.LEFT);
		aprsGPWPLSentence.setFont(new Font("Courier New", 0, 11)); 
		aprsGPWPLSentence.setToolTipText("APRS $GPWPL Waypoint Sentence"); 

		for (int i = 0; i < signalQuality.length; i++) {
			signalQuality[i] = new JLabel();
			signalQuality[i].setBorder(BorderFactory.createEtchedBorder());
			signalQuality[i].setHorizontalAlignment(SwingConstants.CENTER);
			signalQuality[i].setFont(new Font("Calabri", Font.PLAIN, 11)); 
			signalQuality[i].setToolTipText("RSSI (dBm) of Channel " + i); 
			signalQuality[i].setText(""); 
		}

		aprsLatitude.setBorder(BorderFactory.createEtchedBorder());
		aprsLatitude.setHorizontalAlignment(SwingConstants.CENTER);
		aprsLatitude.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		aprsLatitude.setToolTipText("APRS Latitude"); 

		aprsLongitude.setBorder(BorderFactory.createEtchedBorder());
		aprsLongitude.setHorizontalAlignment(SwingConstants.CENTER);
		aprsLongitude.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		aprsLongitude.setToolTipText("APRS Longitude"); 

		aprsCallSign.setBorder(BorderFactory.createEtchedBorder());
		aprsCallSign.setHorizontalAlignment(SwingConstants.CENTER);
		aprsCallSign.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		aprsCallSign.setToolTipText("APRS Call Sign"); 

		aprsSSID.setBorder(BorderFactory.createEtchedBorder());
		aprsSSID.setHorizontalAlignment(SwingConstants.CENTER);
		aprsSSID.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		aprsSSID.setToolTipText("APRS Service Set Identifier"); 

		markerID.setBorder(BorderFactory.createEtchedBorder());
		markerID.setHorizontalAlignment(SwingConstants.CENTER);
		markerID.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		markerID.setToolTipText("Record Marker"); 

		recordCountLabel.setBorder(BorderFactory.createEtchedBorder());
		recordCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
		recordCountLabel.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		recordCountLabel.setToolTipText("Record Count"); 

		cursorMGRS.setBorder(BorderFactory.createEtchedBorder());
		cursorMGRS.setHorizontalAlignment(SwingConstants.CENTER);
		cursorMGRS.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		cursorMGRS.setToolTipText("MGRS Location of Cursor"); 

		measurementPeriod.setBorder(BorderFactory.createEtchedBorder());
		measurementPeriod.setHorizontalAlignment(SwingConstants.CENTER);
		measurementPeriod.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		measurementPeriod.setToolTipText("Measurement Timer Period in Milliseconds"); 

		measurementsThisTile.setBorder(BorderFactory.createEtchedBorder());
		measurementsThisTile.setHorizontalAlignment(SwingConstants.CENTER);
		measurementsThisTile.setVerticalAlignment(SwingConstants.CENTER);
		measurementsThisTile.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		measurementsThisTile.setToolTipText("Measurements Taken in This Tile"); 

		tilesCompleted.setBorder(BorderFactory.createEtchedBorder());
		tilesCompleted.setHorizontalAlignment(SwingConstants.CENTER);
		tilesCompleted.setVerticalAlignment(SwingConstants.CENTER);
		tilesCompleted.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		tilesCompleted.setToolTipText("Total Number of Fully Measured Tiles"); 
		
		totalTiles.setBorder(BorderFactory.createEtchedBorder());
		totalTiles.setHorizontalAlignment(SwingConstants.CENTER);
		totalTiles.setVerticalAlignment(SwingConstants.CENTER);
		totalTiles.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		totalTiles.setToolTipText("Total Number of Tiles Designated for Testing"); 
		
		averageRssiInCurrentTile.setBorder(BorderFactory.createEtchedBorder());
		averageRssiInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
		averageRssiInCurrentTile.setVerticalAlignment(SwingConstants.CENTER);
		averageRssiInCurrentTile.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		averageRssiInCurrentTile.setToolTipText("Average RSSI in Current Tile"); 

		averageBerInCurrentTile.setBorder(BorderFactory.createEtchedBorder());
		averageBerInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
		averageBerInCurrentTile.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		averageBerInCurrentTile.setToolTipText("Average BER in Current Tile"); 

		averageSinadInCurrentTile.setBorder(BorderFactory.createEtchedBorder());
		averageSinadInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
		averageSinadInCurrentTile.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		averageSinadInCurrentTile.setToolTipText("Average SINAD in Current Tile"); 

		minMeasurementsPerTile.setBorder(BorderFactory.createEtchedBorder());
		minMeasurementsPerTile.setHorizontalAlignment(SwingConstants.LEFT);
		minMeasurementsPerTile.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		minMeasurementsPerTile.setToolTipText("Minimum Measurements Required to Complete Tile"); 

		maxMeasurementsPerTile.setBorder(BorderFactory.createEtchedBorder());
		maxMeasurementsPerTile.setHorizontalAlignment(SwingConstants.LEFT);
		maxMeasurementsPerTile.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		maxMeasurementsPerTile.setToolTipText("Maximum Measurements Allowed Per Tile"); 

		cursorTestTileReference.setBorder(BorderFactory.createEtchedBorder());
		cursorTestTileReference.setHorizontalAlignment(SwingConstants.CENTER);
		cursorTestTileReference.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		cursorTestTileReference.setToolTipText("Test Tile Reference at Cursor"); 

		cursorLatitude.setBorder(BorderFactory.createEtchedBorder());
		cursorLatitude.setHorizontalAlignment(SwingConstants.CENTER);
		cursorLatitude.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		cursorLatitude.setToolTipText("Latitude at Mouse Cursor"); 

		cursorLongitude.setBorder(BorderFactory.createEtchedBorder());
		cursorLongitude.setHorizontalAlignment(SwingConstants.CENTER);
		cursorLongitude.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		cursorLongitude.setToolTipText("Longitude at Mouse Cursor"); 

		logFileNameLabel.setBorder(BorderFactory.createEtchedBorder());
		logFileNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		logFileNameLabel.setFont(new Font("Courier New", Font.PLAIN, 11)); 
		logFileNameLabel.setToolTipText("Data File Name"); 

		cursorTerrainAMSL.setBorder(BorderFactory.createEtchedBorder());
		cursorTerrainAMSL.setHorizontalAlignment(SwingConstants.CENTER);
		cursorTerrainAMSL.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		cursorTerrainAMSL.setToolTipText("Height of Terrain Above Mean Sea Level at Cursor"); 
		
		viewingAltitude.setBorder(BorderFactory.createEtchedBorder());
		viewingAltitude.setHorizontalAlignment(SwingConstants.CENTER);
		viewingAltitude.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		viewingAltitude.setToolTipText("Viewing Altitude"); 
		
		nmeaSentenceStringLabel.setBorder(BorderFactory.createEtchedBorder());
		nmeaSentenceStringLabel.setFont(new Font("Courier New", java.awt.Font.PLAIN, 11)); 
		nmeaSentenceStringLabel.setToolTipText("Received NMEA Sentence String"); 
		nmeaSentenceStringLabel.setHorizontalAlignment(SwingConstants.LEFT);

		utcLabel.setBorder(BorderFactory.createEtchedBorder());
		utcLabel.setFont(new Font("Calabri", Font.PLAIN, 11)); 
		utcLabel.setToolTipText("Universal Coordinated Time"); 
		utcLabel.setOpaque(true);
		utcLabel.setHorizontalAlignment(SwingConstants.CENTER);

		gpsInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				"GPS Information", TitledBorder.DEFAULT_JUSTIFICATION, 
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.BOLD, 9))); 
		gpsInfoPanel.setOpaque(false);
		gpsInfoPanel.setDoubleBuffered(true);
		gpsInfoPanel.setPreferredSize(new Dimension(170,230));
		
		gpsCompassRose.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				"True Course Made Good", TitledBorder.DEFAULT_JUSTIFICATION, 
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.BOLD, 9))); 
		gpsCompassRose.setSelectColor(Color.GRAY);
		gpsCompassRose.setOpaque(false);
		gpsCompassRose.setDoubleBuffered(true);
		gpsCompassRose.setPreferredSize(new Dimension(170,170));
		
		signalMeterArray.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				"Signal Strength", TitledBorder.DEFAULT_JUSTIFICATION, 
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.BOLD, 9))); 
		signalMeterArray.setOpaque(false);
		signalMeterArray.setDoubleBuffered(true);
		signalMeterArray.setPreferredSize(new Dimension(170,170));
		
		longitude.setFont(new Font("Calabri", Font.BOLD, 16)); 
		longitude.setHorizontalAlignment(SwingConstants.CENTER);
		longitude.setVerticalAlignment(SwingConstants.CENTER);
		longitude.setBorder(BorderFactory.createTitledBorder(null, "Longitude", 
		        TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9))); 
		longitude.setToolTipText("GPS Longitude (WGS84)"); 

		latitude.setFont(new Font("Calabri", Font.BOLD, 16)); 
		latitude.setHorizontalAlignment(SwingConstants.CENTER);
		latitude.setVerticalAlignment(SwingConstants.CENTER);
		latitude.setBorder(BorderFactory.createTitledBorder(null, "Latitude", 
		        TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9))); 
		latitude.setToolTipText("GPS Latitude (WGS84)"); 

		currentMGRS.setFont(new Font("Calabri", Font.BOLD, 14)); 
		currentMGRS.setHorizontalAlignment(SwingConstants.CENTER);
		currentMGRS.setVerticalAlignment(SwingConstants.CENTER);
		currentMGRS.setBorder(BorderFactory.createTitledBorder(null, "MGRS", 
		        TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9))); 
		currentMGRS.setToolTipText("GPS MGRS Location"); 

		currentGridSquare.setFont(new Font("Calabri", Font.BOLD, 14)); 
		currentGridSquare.setHorizontalAlignment(SwingConstants.CENTER);
		currentGridSquare.setVerticalAlignment(SwingConstants.CENTER);
		currentGridSquare.setBorder(BorderFactory.createTitledBorder(null,
		        "Reference Grid Square", TitledBorder.DEFAULT_JUSTIFICATION, 
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9))); 
		currentGridSquare.setToolTipText("Reference Grid Square at GPS Location"); 

		speedMadeGood.setFont(new Font("Calabri", Font.BOLD, 12)); 
		speedMadeGood.setHorizontalAlignment(SwingConstants.CENTER);
		speedMadeGood.setVerticalAlignment(SwingConstants.CENTER);
		speedMadeGood.setBorder(BorderFactory.createTitledBorder(null,
		        "Speed Made Good", TitledBorder.DEFAULT_JUSTIFICATION, 
		        TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9))); 
		
		testMode = validateTestMode();
		setToolTipText(testMode);

		setDataMode(DataMode.CLOSED);
		populateCurrentTestSettingsFromCoverageTestSettingsDialog();
    }

    private void setToolTipText(final TestMode testMode) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	for (int i = 0; i < signalQuality.length; i++) {
		    		if (testMode == TestMode.RSSI) {
			            signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " dBm"); 
			        }
		    		if (testMode == TestMode.BER) {
			            signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " Bit Error Rate"); 
			        } 
		    		if (testMode == TestMode.SINAD) {
			            signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " SINAD"); 
			        }
		    		if (testMode == TestMode.RSSI_SINAD) {
			            signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " dBm / SINAD"); 
			        }
		    		if (testMode == TestMode.RSSI_BER) {
			            signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " dBm / Bit Error Rate"); 
			        }
		    		if (testMode == TestMode.MODE_NOT_SELECTED) {
			            signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " Test Mode Is Not Configured"); 
			        }
		    	}
            }
        });
    }

    private void createGraphicalUserInterface() {

    	GroupLayout gpsInfoPanelLayout = new GroupLayout(gpsInfoPanel);

        gpsInfoPanel.setLayout(gpsInfoPanelLayout);

        gpsInfoPanelLayout.setHorizontalGroup(gpsInfoPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(longitude, 157, 157, 157)
            .addComponent(latitude, 157, 157, 157)
            .addComponent(currentMGRS, 157, 157, 157)
            .addComponent(currentGridSquare, 157, 157, 157)
            .addComponent(speedMadeGood, 157, 157, 157));

        gpsInfoPanelLayout.setVerticalGroup(gpsInfoPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
    		.addGroup(gpsInfoPanelLayout.createSequentialGroup()
	            .addComponent(longitude, 42, 42, 42)
	            .addComponent(latitude, 42, 42, 42)
	            .addComponent(currentMGRS, 42, 42, 42)
	            .addComponent(currentGridSquare, 42, 42, 42)
	            .addComponent(speedMadeGood, 42, 42, 42)
	            .addContainerGap()));

        GroupLayout layout = new GroupLayout(getContentPane());

        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	.addComponent(toolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        	.addGroup(layout.createSequentialGroup()
        		.addGap(5)
            	.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            		.addComponent(mapPanel, PREFERRED_MAP_SIZE.width, PREFERRED_MAP_SIZE.width, PREFERRED_MAP_SIZE.width)
					.addGroup(layout.createSequentialGroup()	
						.addComponent(cursorLatitude, 90,90,90)
	                	.addGap(4)
	                	.addComponent(cursorLongitude, 90,90,90)
		                .addGap(4)
		                .addComponent(cursorMGRS, 145,145,145)
		                .addGap(4)
		                .addComponent(cursorTestTileReference, 130,130,130)
		                .addGap(4)
		                .addComponent(viewingAltitude, 85,85,85)
		                .addGap(4)
		                .addComponent(cursorTerrainAMSL, 85,85,85)
		                .addGap(4)
		                .addComponent(utcLabel, 144,144,144))
		            .addGroup(layout.createSequentialGroup()
		                .addComponent(logFileNameLabel, 147,147,147)
		                .addGap(4)
		                .addComponent(measurementPeriod, 46,46,46)
		                .addGap(4)
		                .addComponent(measurementsThisTile, 53,53,53)
		                .addGap(4)
		                .addComponent(minMeasurementsPerTile, 53,53,53)
		                .addGap(4)
		                .addComponent(maxMeasurementsPerTile, 53,53,53)
		                .addGap(4)
		                .addComponent(tilesCompleted, 65,65,65)
		                .addGap(4)
		                .addComponent(totalTiles, 65,65,65)
		                .addGap(4)
		                .addComponent(averageRssiInCurrentTile, 64,64,64)
		                .addGap(4)
		                .addComponent(averageBerInCurrentTile, 64,64,64)
		                .addGap(4)
		                .addComponent(averageSinadInCurrentTile, 64,64,64)
		                .addGap(4)
		                .addComponent(recordCountLabel, 80,80,80)
		                )
		            .addGroup(layout.createSequentialGroup()
	                	.addComponent(signalQuality[0], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[1], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[2], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[3], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[4], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[5], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[6], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[7], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[8], 70,70,70)
	                	.addGap(4)
	                	.addComponent(signalQuality[9], 70,70,70)
	                	.addGap(4)
	                	.addComponent(markerID, 54,54,54)))     
	            .addGap(5)
	            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            	.addComponent(gpsInfoPanel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	            	.addComponent(gpsCompassRose, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addComponent(signalMeterArray, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGroup(layout.createSequentialGroup()
        				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
        					.addComponent(aprsStatus, 55,55,55)
        					.addComponent(radioStatus, 55,55,55)
        					.addComponent(gpsStatus, 55,55,55))
        				.addGap(4)
        				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
    						.addComponent(aprsTxData, 18,18,18)
        					.addComponent(radioTxData, 18,18,18)
        					.addComponent(gpsTxData, 18,18,18))
        				.addGap(4)
        				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
    						.addComponent(aprsRxData, 18,18,18)
        					.addComponent(radioRxData, 18,18,18)
        					.addComponent(gpsRxData, 18,18,18))
        				.addGap(4)
        				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
    						.addComponent(aprsCTS, 18,18,18)
        					.addComponent(radioCTS, 18,18,18)
        					.addComponent(gpsCTS, 18,18,18))
        				.addGap(4)
        				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
    						.addComponent(aprsDSR, 18,18,18)
        					.addComponent(radioDSR, 18,18,18)
        					.addComponent(gpsDSR, 18,18,18))
        				.addGap(4)
        				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
    						.addComponent(aprsCD, 18,18,18)
        					.addComponent(radioCD, 18,18,18)
        					.addComponent(gpsCD, 18,18,18))))
        		.addGap(5)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            	.addComponent(toolBar, 25,25,25)
            	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            	.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            		.addComponent(mapPanel, PREFERRED_MAP_SIZE.height, PREFERRED_MAP_SIZE.height, PREFERRED_MAP_SIZE.height)
            		.addGroup(layout.createSequentialGroup()
	        			.addComponent(gpsInfoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	        			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                	.addComponent(gpsCompassRose, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
		                .addComponent(signalMeterArray, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
		        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            	.addComponent(signalQuality[0], 18,18,18)
	            	.addComponent(signalQuality[1], 18,18,18)
	            	.addComponent(signalQuality[2], 18,18,18)
	            	.addComponent(signalQuality[3], 18,18,18)
	            	.addComponent(signalQuality[4], 18,18,18)
	            	.addComponent(signalQuality[5], 18,18,18)
	            	.addComponent(signalQuality[6], 18,18,18)
	            	.addComponent(signalQuality[7], 18,18,18)
	            	.addComponent(signalQuality[8], 18,18,18)
	            	.addComponent(signalQuality[9], 18,18,18)
	                .addComponent(markerID, 18,18,18)
	                .addComponent(aprsStatus, 18,18,18)
	                .addComponent(aprsTxData, 18,18,18)
	                .addComponent(aprsRxData, 18,18,18)
	                .addComponent(aprsCTS, 18,18,18)
	                .addComponent(aprsDSR, 18,18,18)
	                .addComponent(aprsCD, 18,18,18))
	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
	                .addComponent(logFileNameLabel, 18,18,18)
	                .addComponent(measurementPeriod, 18,18,18)
	                .addComponent(measurementsThisTile, 18,18,18)
	                .addComponent(minMeasurementsPerTile, 18,18,18)
	                .addComponent(maxMeasurementsPerTile, 18,18,18)
	                .addComponent(tilesCompleted, 18,18,18)
	                .addComponent(totalTiles, 18,18,18)
	                .addComponent(averageRssiInCurrentTile, 18,18,18)
	                .addComponent(averageBerInCurrentTile, 18,18,18)
	                .addComponent(averageSinadInCurrentTile, 18,18,18)
	                .addComponent(recordCountLabel, 18,18,18)
	                .addComponent(radioStatus, 18,18,18)
	                .addComponent(radioTxData, 18,18,18)
	                .addComponent(radioRxData, 18,18,18)
	                .addComponent(radioCTS, 18,18,18)
	                .addComponent(radioDSR, 18,18,18)
	                .addComponent(radioCD, 18,18,18))
	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                .addComponent(cursorLongitude, 18,18,18)
	                .addComponent(cursorLatitude, 18,18,18)
	                .addComponent(cursorMGRS, 18,18,18)
	                .addComponent(cursorTestTileReference, 18,18,18)
	                .addComponent(viewingAltitude, 18,18,18)
	                .addComponent(cursorTerrainAMSL, 18,18,18)
	                .addComponent(utcLabel, 18,18,18)
	                .addComponent(gpsStatus, 18,18,18)
	                .addComponent(gpsTxData, 18,18,18)
	                .addComponent(gpsRxData, 18,18,18)
	                .addComponent(gpsCTS, 18,18,18)
	                .addComponent(gpsDSR, 18,18,18)
	                .addComponent(gpsCD, 18,18,18))
	            .addGap(5)));
    }

   private void displayGraphicalUserInterface() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
        setTitle("SignalTrack Radio Network Coverage Analysis System"); 
        if (radioComponent.isStartRadioWithSystem()) startRadio();
        setVisible(true);
        mapPanel.add((Component) map, BorderLayout.CENTER);
        map.restoreCache();
    }

   	private void gpsSettingsPropertyChangeEvent(final PropertyChangeEvent event) {
   		if (event.getPropertyName().equals(SerialGPSComponent.GPS_RADIUS_UPDATED)) {
   			map.setGpsDotRadius((double) event.getNewValue());
   		}
   	}
   
   	private void updateStaticTestSettings(final PropertyChangeEvent event) {
   		map.setTargetRingColor(staticTestSettings.getTargetRingColor());
   		map.showTargetRing(staticTestSettings.isShowTargetRing());
   		map.setArcAsymptoteColor(staticTestSettings.getAsymptoteColor());
   		map.showArcAsymptotes(staticTestSettings.isShowAsymptotes());
   		map.setTraceEqualsFlightColor(staticTestSettings.isTraceEqualsFlightColor());
   		map.setArcColors(staticTestSettings.getArcColors());
   		map.showArcs(staticTestSettings.isShowArcs());
   		map.setArcTraceColor(staticTestSettings.getTraceColor());
   		map.setArcTraceRadius(staticTestSettings.getTraceRadius());
   		map.showArcTrace(staticTestSettings.isShowTrace());
   		map.setArcCursorColor(staticTestSettings.getCursorColor());
   		map.setArcCursorRadius(staticTestSettings.getCursorRadius());
   		map.showArcCursors(staticTestSettings.isShowCursors());
   		map.setArcIntersectPointColor(staticTestSettings.getIntersectPointColor());
   		map.setArcIntersectPointRadius(staticTestSettings.getIntersectPointRadius());
	   	map.showArcIntersectPoints(staticTestSettings.isShowIntersectPoints());
   	}
   
   	private void updateCoverageTestSettings(final PropertyChangeEvent event) {
   		try {
   			map.deleteAllTestTiles();
	    	Point.Double tileSizeArcSeconds = coverageTestSettings.
	    		getTileSizeArcSeconds(serialGPSComponent.getGPSInterface().getPosition().y);
        	map.setTileSize(tileSizeArcSeconds);
        	map.setGridSize(coverageTestSettings.getGridSize());
        	map.setGridReference(coverageTestSettings.getGridReference()); 
	        map.setSignalMarkerRadius(coverageTestSettings.getSignalMarkerRadius());
	        map.showTestTiles(coverageTestSettings.isShowGridSquareShading());
	   		map.showLines(coverageTestSettings.isShowLines());
	   		map.showRings(coverageTestSettings.isShowRings());
	   		map.showQuads(coverageTestSettings.isShowQuads());
	   		map.showSignalMarkers(coverageTestSettings.isShowSignalMarkers());
	   		map.setSignalMarkerRadius(coverageTestSettings.getSignalMarkerRadius());
	        radioComponent.getRadioInterface().sampleBerValues(coverageTestSettings.isBERSamplingInEffect());
	        radioComponent.getRadioInterface().sampleRssiValues(coverageTestSettings.isRSSIMode());
	        radioComponent.getRadioInterface().setSinadEnabled(coverageTestSettings.isSinadSamplingInEffect());
	        if (coverageTestSettings.getMinSamplesPerTile() == -1) {
	            periodTimer.setDelay(coverageTestSettings.getMinTimePerTile() * 1000);
	            periodTimer.setInitialDelay(coverageTestSettings.getMinTimePerTile() * 1000);
	            periodTimer.start();
	        } else {
	            periodTimer.stop();
	        }
	        testMode = validateTestMode();
	        setToolTipText(testMode);
	        populateCurrentTestSettingsFromCoverageTestSettingsDialog();
   		} catch (NullPointerException ex) {
   			ex.printStackTrace();
   		}
    }
   	
   	private TestMode validateTestMode() {
   		TestMode testMode = TestMode.MODE_NOT_SELECTED;
   		if (coverageTestSettings.isRSSIMode()) {
   			if (coverageTestSettings.isBERSamplingInEffect()) testMode = TestMode.RSSI_BER;
   			else if (coverageTestSettings.isSinadSamplingInEffect()) testMode = TestMode.RSSI_SINAD;
   			else testMode = TestMode.RSSI;
   		} else if (coverageTestSettings.isBERSamplingInEffect()) {
   			testMode = TestMode.BER;
   		}
   		else if (coverageTestSettings.isSinadSamplingInEffect()) {
   			testMode = TestMode.SINAD;		
   		}
        return testMode;
   	}
   	
    private void updateAprsSettings(final PropertyChangeEvent event) {
        map.showIcons(aprsComponent.isEnableAprsTracking());
        map.showIconLabels(aprsComponent.isEnableAprsShowIconLabels());
    }
    
    private void mapPropertyChangedChangeListenerEvent(final PropertyChangeEvent event) {
        updateMapSettings(event);
    }
    
    private void mapTypeChangedChangeListenerEvent(final PropertyChangeEvent event) {
        Point.Double point = map.getCenterLonLat();
        Dimension mapPreferredSize = map.getPreferredSize();
        double zoom = map.getZoom();
        map.setVisible(false);
        map.clearCache();
        mapPanel.removeAll();
        map.removePropertyChangeListener(mapPropertyChangeListener);
        map.removeMouseListener(mapMouseListener);
        map.removeMouseMotionListener(mapMouseMotionListener);
        map.removeKeyListener(mapKeyListener);
        map.removeJMVListener(jmvEventListener);
        
        switch ((int) event.getNewValue()) {
            case 0:
                map = new ImageMap(systemPref.getInt("SelectedMapIndex", -1)); 
                map.setScale(1.0);
                break;
            case 1:
                map = new WorldWindMap(point, zoom, mapPreferredSize, false);
                break;
            case 2:
                map = new OpenStreetMapPanel(point, (int) zoom, mapPreferredSize);
                break;
        }
 
        map.addPropertyChangeListener(mapPropertyChangeListener);
        map.addMouseListener(mapMouseListener);
        map.addMouseMotionListener(mapMouseMotionListener);
        map.addKeyListener(mapKeyListener);
        map.addJMVListener(jmvEventListener);
        
        mapPanel.add((Component) map, BorderLayout.CENTER);
        
        mapPanel.revalidate();
        mapPanel.repaint();

        map.restoreCache();
    }
    
    private Precision getRequiredPrecision(Point.Double tileSizeArcSeconds) {
    	long width = (long) Vincenty.arcSecondsToMeters(tileSizeArcSeconds.x, 90, map.getMapBottomEdgeLatitude());
    	long height = (long) Vincenty.arcSecondsToMeters(tileSizeArcSeconds.y, 0, map.getMapBottomEdgeLatitude());
    	long size = Math.max(width, height);
    	if (size <= 9) return Precision.PRECISION_1_M;
    	if (size <= 99) return Precision.PRECISION_10_M;
    	if (size <= 999) return Precision.PRECISION_100_M;
    	if (size <= 9999) return Precision.PRECISION_1_KM;
    	if (size <= 99999) return Precision.PRECISION_10_KM;
    	if (size <= 999999) return Precision.PRECISION_100_KM;
    	return Precision.PRECISION_1000_KM;
    }
    
    private void updateMapSettings(PropertyChangeEvent event) {
    	map.setGridColor(mapSettings.getGridColor());
    	map.showGrid(mapSettings.isShowGrid());
    	map.displayShapes(mapSettings.isDisplayShapes());
    }
    
    private void setShutdownMonitorProgress(String note, int progress) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	shutdownMonitor.setNote(note);
            	shutdownMonitor.setProgress(progress);
            }
        });
    }
    
    private void frameClosingWindowEvent(WindowEvent event) {
    	try {
        	setShutdownMonitorProgress("Saving Map Location", 1); 
            Point.Double point = map.getCenterLonLat();
            if (point != null && point.x >= -180 && point.x <= 180) systemPref.putDouble("MapLongitude", point.x); 
            if (point != null && point.y >= -90 && point.y <= 90) systemPref.putDouble("MapLatitude", point.y); 
            systemPref.putDouble("MapScale", map.getScale()); 
            systemPref.putInt("MapZoom", map.getZoom()); 
            setShutdownMonitorProgress("Closing GPS Receiver", 2); 
            serialGPSComponent.getSerialConfig().getSerialInterface().closeSerialPort();
            setShutdownMonitorProgress("Closing Radio Port", 3); 
            radioComponent.getSerialConfig().getSerialInterface().closeSerialPort();
            setShutdownMonitorProgress("Closing APRS Modem", 4); 
            aprsComponent.getSerialConfig().getSerialInterface().closeSerialPort();
            setShutdownMonitorProgress("Cancelling Static Measurements", 5); 
            if (intersectListUpdate != null) intersectListUpdate.cancel(true);
            setShutdownMonitorProgress("Cancelling Doppler Triangulations", 6); 
            if (triangulate != null) triangulate.cancel(true);
            setShutdownMonitorProgress("Stopping Network Interface", 7); 
            nicCheck.cancel();
            setShutdownMonitorProgress("Releasing Radio Interface Resources", 8); 
            radioComponent.getRadioInterface().dispose();
            setShutdownMonitorProgress("Releasing GPS Interface Settings", 9); 
            serialGPSComponent.dispose();
            setShutdownMonitorProgress("Releasing Radio Resources", 10); 
            radioComponent.dispose();
            setShutdownMonitorProgress("Releasing APRS Resources", 11); 
            aprsComponent.dispose();
            setShutdownMonitorProgress("Releasing Map Resources", 12); 
            mapSettings.dispose();
            setShutdownMonitorProgress("Releasing Coverage Test Resources", 13); 
            coverageTestSettings.dispose();
            setShutdownMonitorProgress("Releasing Static Test Resources", 14); 
            staticTestSettings.dispose();
            setShutdownMonitorProgress("Suspending Clock", 15); 
            networkTime.stopClock();
            setShutdownMonitorProgress("Suspending Network Time Updates", 16); 
            networkTime.stopAutomaticNetworkTimeUpdates();
            setShutdownMonitorProgress("Saving Survey Records", 17); 
            if (database != null) database.close();
            setShutdownMonitorProgress("Closing Database", 18); 
            fh.flush();
            fh.close();
            setShutdownMonitorProgress("Shutdown", 19); 
        } catch (Exception ex) {
        	ex.printStackTrace();
            log.log(Level.SEVERE, "Exception", ex); 
        } finally {
        	shutdownMonitor.close();
        	signalReadyToExit(WINDOW_CLOSING_COMPLETE);
        }
    }

    private void doZoomIn() {
    	map.zoomIn();
    }
    
    private void doZoomOut() {
    	map.zoomOut();
    }
    
    private void zoomInButtonMousePressed(final ActionEvent event) {
        zoomInMouseDownTimer.start();
        doZoomIn();
    }
    
    private void zoomInButtonMouseReleased(final MouseEvent event) {
        zoomInMouseDownTimer.stop();
    }

    private void zoomOutButtonMousePressed(final ActionEvent event) {
    	zoomOutMouseDownTimer.start();
        doZoomOut();
    }

    private void zoomOutButtonMouseReleased(final MouseEvent event) {
        zoomOutMouseDownTimer.stop();
    }

    private void stopGps() {
    	try {
	    	serialGPSComponent.enableGPS(false);
	    	gpsValidHeadingTimer.stop();
			gpsRxDataTimer.stop();
            serialGPSComponent.getSerialConfig().getSerialInterface().closeSerialPort();
            if (monitorGPSHandshaking) serialGPSComponent.getSerialConfig().getSerialInterface().monitorHandshaking(serialGPSComponent.getSerialConfig().getPortName());
    	} catch (SerialPortException ex) {
    		ex.printStackTrace();
    	} finally {
    		resetGPSIndicators();
    	}
    }
    
    private void resetGPSIndicators() {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	nmeaSentenceStringLabel.setText(""); 
				latitude.setText(""); 
				longitude.setText(""); 
				currentMGRS.setText(""); 
				currentGridSquare.setText(""); 
				speedMadeGood.setText(""); 
				gpsStatus.setBackground(getGpsStatusBackgroundColor(FixQuality.OFF_LINE));
				gpsStatus.setText("OFF LINE"); 
		        gpsCTS.setBackground(new Color(127, 0, 0));
		        gpsDSR.setBackground(new Color(127, 0, 0));
		        gpsCD.setBackground(new Color(127, 0, 0));
		        gpsTxData.setBackground(new Color(127, 0, 0));
		        gpsRxData.setBackground(new Color(127, 0, 0));
				setUTCLabelColors(networkTime.getTimeStratum());
				gpsButton.setSelected(false);
				map.showGpsSymbol(false);
            }
        });
    }
    
    private void startGps() {
    	try {
    		serialGPSComponent.enableGPS(true);
    		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
                @Override
                public void run() {
                	latitude.setForeground(Color.LIGHT_GRAY);
		        	longitude.setForeground(Color.LIGHT_GRAY);
		        	currentMGRS.setForeground(Color.LIGHT_GRAY);
		        	currentGridSquare.setForeground(Color.LIGHT_GRAY);
		        	speedMadeGood.setForeground(Color.LIGHT_GRAY);
		        	map.showGpsSymbol(false);
		        	gpsButton.setSelected(true);
                }
            });
    	} catch (SerialPortException ex) {
    		resetGPSIndicators();
    		reportException(ex);
    	}
	}

    private void centerMapOnGpsPosition() {
    	if (serialGPSComponent.getGPSInterface().isValidFix()) {
    		mapDragged = false;
    		map.setCenterLonLat(serialGPSComponent.getGPSInterface().getPosition());
    		map.setDisplayToFitMapElements(true, true, true, true);
    	}
    }
    
	private void reportException(final ClassNotFoundException ex) {
    	log.log(Level.WARNING, "ClassNotFoundException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Class Not Found Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
	
	private void reportException(final InstantiationException ex) {
    	log.log(Level.WARNING, "InstantiationException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Instantiation Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
	
	private void reportException(final ParseException ex) {
    	log.log(Level.WARNING, "ParseException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Parse Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
	
	private void reportException(final IOException ex) {
    	log.log(Level.WARNING, "IOException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Input / Output Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
	
	private void reportException(final IllegalAccessException ex) {
    	log.log(Level.WARNING, "IllegalAccessException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Illegal Access Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
	
    private void reportException(final UnsupportedLookAndFeelException ex) {
    	log.log(Level.WARNING, "UnsupportedLookAndFeelException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Unsupported Look And Feel Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
    
    private void reportException(final SerialPortException ex) {
    	log.log(Level.WARNING, "SerialPortException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Serial Port Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
    
    private void reportException(final InterruptedException ex) {
    	log.log(Level.WARNING, "InterruptedException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Interrupted Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
    
    private void reportException(final ExecutionException ex) {
    	log.log(Level.WARNING, "ExecutionException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Execution Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
    
    private void reportException(final SecurityException ex) {
    	log.log(Level.WARNING, "SecurityException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Security Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}

    private void reportException(final BackingStoreException ex) {
    	log.log(Level.WARNING, "BackingStoreException", ex); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Backing Store Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
	}
    
    private void deviceNotOnlineException(final String[] str) {
    	log.log(Level.WARNING, "DeviceNotOnlineException", str[0] + " " + str[1]); 
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	str[0], str[1], JOptionPane.ERROR_MESSAGE);
            }
        });
	}

    private void resetAPRSIndicators() {
    	aprsRxDataTimer.stop();
        aprsTxDataTimer.stop();
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
				aprsCTS.setBackground(new Color(127, 0, 0));
		        aprsDSR.setBackground(new Color(127, 0, 0));
		        aprsCD.setBackground(new Color(127, 0, 0));
		        aprsTxData.setBackground(new Color(127, 0, 0));
		        aprsRxData.setBackground(new Color(127, 0, 0));
		        aprsStatus.setBackground(new Color(127, 0, 0));
		        aprsStatus.setText("OFF LINE"); 
		        aprsButton.setSelected(false);
            }
        });
    }
    
    private void startAPRS() {
        try {
        	aprsComponent.getSerialConfig().getSerialInterface().setOnline(aprsComponent.getSerialConfig().getPortName());
        	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	            @Override
	            public void run() {
					aprsStatus.setText("ON LINE"); 
					aprsStatus.setBackground(Color.GREEN);
					aprsButton.setSelected(true);
	            }
	        });
        } catch(SerialPortException ex) {
        	resetAPRSIndicators();
            reportException(ex);
        }
    }
    
    private void stopAPRS() {
        try {
        	resetAPRSIndicators();
        	aprsComponent.getSerialConfig().getSerialInterface().closeSerialPort();
        } catch(SerialPortException ex) {
        	reportException(ex);
        }
    }

    private void startCoverageTest() {
    	if (dataMode != DataMode.RECORD && dataMode != DataMode.CLOSED) {
    		setDataMode(DataMode.RECORD);
    	}
    	if (dataMode == DataMode.CLOSED) {
    		 invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	            @Override
	            public void run() {
	            	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
		                "Please select a coverage test", "File Not Open Error", JOptionPane.ERROR_MESSAGE); 
	            }
	        });
    		int returnVal = openDataFile();
    		if (returnVal == JFileChooser.CANCEL_OPTION) {
    			stopCoverageTest();
    			return;
    		}
    	}
    	startRadio();
    	startGps();
    	serialGPSComponent.getGPSInterface().enableContinuousUpdate(true);
    	coverageTestActive = true;
	}

    private void stopCoverageTest() {
    	coverageTestActive = false;
        coverageTestSettings.setControlsEnabled(true);
        serialGPSComponent.getGPSInterface().enableContinuousUpdate(false);
        measurementTimer.stop();
		setDataMode(DataMode.STOP);
    }
    
    private void startStaticTest() {
    	if (dataMode == DataMode.CLOSED) createNewDataFile(null);
    	if (!serialGPSComponent.isGpsOnLine()) startGps();
        staticTestActive = true;
    }
    
    private void stopStaticTest() {
        staticTestActive = false;
		setDataMode(DataMode.CLOSED);
    }

    private void startLearnMode() {
    	if (dataMode != DataMode.RECORD && dataMode != DataMode.CLOSED) {
    		setDataMode(DataMode.RECORD);
    	}
    	if (dataMode == DataMode.CLOSED) {
    		 invokeLaterInDispatchThreadIfNeeded(new Runnable() {
    	            @Override
    	            public void run() {
    	            	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
    		                "Please select an existing coverage test, or create a new test", "File Not Open Error", JOptionPane.ERROR_MESSAGE); 
    	            }
    	        });
    		if (openDataFile() == JFileChooser.CANCEL_OPTION) {
    			stopLearnMode();
    			return;
    		}
    	}
    	setDataMode(DataMode.RECORD);
    	learnMode = true;
    }
    
    private void stopLearnMode() {
    	learnMode = false;
    	learnModeButton.setSelected(false);
    }
    
    private void startRadio() {
        try {  	
        	radioButton.setSelected(true);
			radioComponent.startRadio();
			radioComponent.getRadioInterface().sampleBerValues(coverageTestSettings.isBERSamplingInEffect());
	        radioComponent.getRadioInterface().sampleRssiValues(coverageTestSettings.isRSSIMode());
	        testMode = validateTestMode();
	        setToolTipText(testMode);
        } catch (SerialPortException ex) {
			resetRadioIndicators();
			reportException(ex);
		}
    }  
    
    private void resetRadioIndicators() {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
				radioStatus.setBackground(new Color(127,0,0));
		        radioStatus.setText("OFF LINE"); 
		        radioCTS.setBackground(new Color(127, 0, 0));
		        radioDSR.setBackground(new Color(127, 0, 0));
		        radioCD.setBackground(new Color(127, 0, 0));
		        radioTxData.setBackground(new Color(127, 0, 0));
		        radioRxData.setBackground(new Color(127, 0, 0));
				radioButton.setSelected(false);
				testMode = TestMode.OFF;
				signalMeterArray.setMeterLevels(0);
				setSignalQualityDisplay(testMode, false);
				averageBerInCurrentTile.setText(""); 
				averageSinadInCurrentTile.setText(""); 
				averageRssiInCurrentTile.setText(""); 
            }
        });
    }
    
    private void stopRadio() {
    	try {
	    	radioComponent.getRadioInterface().initiateRadioStop();
	    	radioRxDataTimer.stop();
	        radioTxDataTimer.stop();
		    if (monitorRadioHandshaking) radioComponent.getSerialConfig().getSerialInterface().monitorHandshaking(radioComponent.getSerialConfig().getPortName());
		    resetRadioIndicators();
    	} catch (SerialPortException ex) {
			reportException(ex);
		}  
    }
    
    private void startSINAD() {
    	sinad = new Sinad();
        sinad.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (Sinad.SINAD_CHANGED.equals(event.getPropertyName())) {
	            	sinadChangedChangeListenerEvent(event);
	    		}
			}
        });
        sinad.startSinad();
    }
    
    private void stopSINAD() {	
    	sinad.stopSinad();
    }

    private void exitMenuItemActionListenerEvent(ActionEvent event) {
    	dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void printPreviewMenuItemActionListenerEvent(ActionEvent event) {
    	new PreviewPrintPanel(map.getScreenShot());
    }

    private void printMenuItemActionListenerEvent(ActionEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new PrintUtilities(map.getScreenShot()));
    }

    private void mapPanelSpaceKeyPressed(KeyEvent event) {
        markerCounter++;
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	markerID.setText(String.format("%5d", markerCounter)); 
            }
        });
    }

    private void mapPanelEnterKeyPressed(KeyEvent event) {
        cursorBearingSet = true;
        processBearingInformation();
    }

    private Point.Double pointMean(final List<Point.Double> list) {
        double xs = 0.0;
        double ys = 0.0;
        double xm;
        double ym;
        if (list.size() < 2) return list.get(0);
        Iterator<Point.Double> iterator = list.iterator();
        while(iterator.hasNext()) {
        	Point.Double a = iterator.next();
            xs += a.getX();
            ys += a.getY();
        }
        xm = xs / list.size();
        ym = ys / list.size();
        return new Point.Double(xm, ym);
    }

    private double pointVariance(final List<Point.Double> list) {
    	if (list.size() < 2) return 0;
        Point.Double mean = pointMean(list);
        double xv = 0.0;
        double yv = 0.0;
        Iterator<Point.Double> iterator = list.iterator();
        while(iterator.hasNext()) {
        	Point.Double a = iterator.next();
            xv += (mean.getX() - a.getX()) * (mean.getX() - a.getX());
            yv += (mean.getY() - a.getY()) * (mean.getY() - a.getY());
        }
        return Math.max(xv / list.size(), yv / list.size());
    }

    private double pointStandardDeviation(List<Point.Double> list) {
        double d = 0;
    	d = Math.sqrt(pointVariance(list));
		return d;
    }

    void mapPanelRightMouseButtonClicked(final MouseEvent event) {
        if (testMode == TestMode.DOPPLER) {
	    	cursorBearingSet = false;
	        cursorBearing = 0;
	        cursorBearingPosition = map.getMouseCoordinates();
	        cursorBearingIndex = addRdfBearing(cursorBearingPosition, 0, RDF_BEARING_LENGTH_IN_DEGREES, 
	        	GPSInterface.RdfQuality.RDF_QUAL_8, Color.RED);
	        map.addRing(cursorBearingPosition, 6, Color.RED);
        }
    }

    void mapPanelDragged(final MouseEvent event) {
    	mapDragged = true;
    }
    
    void mapPanelMouseMoved(final Point.Double cursorLonLat, final Point.Double tileSizeArcSeconds,
    		final Point.Double gridReference, final Point.Double gridSize) {
    	if (!mouseOffGlobe) {
	        updateTestTileLabel(cursorLonLat, tileSizeArcSeconds, gridReference, gridSize);
	        updateMGRSLabel(cursorLonLat);
	        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	            @Override
	            public void run() {
					cursorLatitude.setText(String.format("%8f", cursorLonLat.y)); 
					cursorLongitude.setText(String.format("%9f", cursorLonLat.x)); 
	            }
	        });
    	} else {
    		setMouseOffGlobe();
    	}   	
    }

    private void updateTestTileLabel(final Point.Double position, final Point.Double tileSizeArcSeconds, 
    	final Point.Double gridReference, final Point.Double gridSize) {
    	TestTile testTile = CoordinateUtils.lonLatToTestTile(position, tileSizeArcSeconds, gridReference, gridSize);
    	if (testTile != null) {
    		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	            @Override
	            public void run() {
					cursorTestTileReference.setText(testTile.toFormattedTestTileDesignator());
	            }
	        });
    	}
    }
    
    private void updateMGRSLabel(final Point.Double mousePosition) {
    	SwingWorker<MGRSCoord, Void> worker = new SwingWorker<MGRSCoord, Void>() {
            @Override
            protected MGRSCoord doInBackground() throws Exception {
            	return CoordinateUtils.lonLatToMGRS(mousePosition, 5);
            }
            @Override
            protected void done() {
				try {
					if (!mouseOffGlobe) cursorMGRS.setText(get().toString());
				} catch(InterruptedException ex) {
					reportException(ex);
				} catch(ExecutionException ex) {
					reportException(ex);
				}
            }
    	};
    	worker.execute();
    }
    
    private double milliSecondsBetweenMeasurements(double sizeOfTileInMeters,
            double speedMadeGoodKPH, double measurementsPerTile) {
        double timeAcrossTileAtGivenSpeed = ((sizeOfTileInMeters / 1000.0) /
            (Math.max(speedMadeGoodKPH, 1))) * 3600.0 * 1000.0;
        return timeAcrossTileAtGivenSpeed / measurementsPerTile;
    }

    private void gpsValidFixPropertyChangeListenerEvent(final PropertyChangeEvent event) {

    }
    
    private void gpsValidPositionPropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	processPositionInformation(PositionSource.GPS, (Point.Double) event.getNewValue());
    }

    private void gpsValidTimePropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	long newGpsTimeInMillis = (long) event.getNewValue();
    	if (previousGpsTimeInMillis != newGpsTimeInMillis) {
    		networkTime.setGpsTimeInMillis(newGpsTimeInMillis);
    	}
    	previousGpsTimeInMillis = newGpsTimeInMillis;
    }
    
    private void processPositionInformation(PositionSource source, Point.Double currentLonLat) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	ppi(source, currentLonLat);
            }
    	});
    }
    
    private void ppi(final PositionSource source, final Point.Double currentLonLat) {
    	lastInputSource = source;
    	
        if (serialGPSComponent.enableGpsTracking() && source != PositionSource.MANUAL) {
            double angle;
            if (serialGPSComponent.getGPSInterface().getSpeedMadeGoodMPH() >= 2) {
                angle = serialGPSComponent.getGPSInterface().getCourseMadeGoodTrue();
            } else {
                angle = 360;
            }
            
            map.showGpsSymbol(true);
            map.setGpsSymbol(currentLonLat, serialGPSComponent.getGpsSymbolRadius() * 2, 
            	getGpsColor(serialGPSComponent.getGPSInterface().getFixQuality()), (int) angle);
        }
        
        if (serialGPSComponent.centerMapOnGPSPosition() && source != PositionSource.MANUAL) {
        	if (checkMapRecenter(currentLonLat)) {
        		map.setCenterLonLat(currentLonLat);
        	}
        }

        if (coverageTestSettings.getMinSamplesPerTile() == -1) {
            periodTimer.setDelay(coverageTestSettings.getMinTimePerTile() * 1000);
            periodTimer.setInitialDelay(coverageTestSettings.getMinTimePerTile() * 1000);
            periodTimer.start();
        } else {
            periodTimer.stop();
        }
        
        String mph = String.format("%3.1f", serialGPSComponent.getGPSInterface().getSpeedMadeGoodMPH()) + " MPH"; 
        String kph = String.format("%3.1f", serialGPSComponent.getGPSInterface().getSpeedMadeGoodKPH()) + " Knots"; 

		longitude.setText(String.format("%9f", currentLonLat.x)); 
		latitude.setText(String.format("%8f", currentLonLat.y)); 
		speedMadeGood.setText(mph + " / " + kph); 
		currentMGRS.setText(serialGPSComponent.getGPSInterface().getMGRSLocation());
		
		setCurrentGridSquareLabel(currentLonLat);
        
        if (currentTestTile != null) {
	        if (coverageTestSettings.isRSSIMode()) {
	        	averageRssiInCurrentTile.setText(String.format("%4.1f", currentTestTile.getAvgdBm())); 
	        } else {
	        	averageRssiInCurrentTile.setText(""); 
	        }
	        
	        if (coverageTestSettings.isBERSamplingInEffect()) {
	        	averageBerInCurrentTile.setText(String.format("%1.2f", currentTestTile.getAvgBer())); 
	        } else {
	        	averageBerInCurrentTile.setText(""); 
	        }
	        
	        if (coverageTestSettings.isSinadSamplingInEffect()) {
	        	averageSinadInCurrentTile.setText(String.format("%2.1f", currentTestTile.getAvgSinad())); 
	        } else {
	        	averageSinadInCurrentTile.setText(""); 
	        }
	        
	        measurementsThisTile.setText(String.format("%5d", currentTestTile.getMeasurementCount())); 
        }
        
        if(dataMode == DataMode.RECORD) processCoverageTestMeasurement(source, serialGPSComponent.getGPSInterface().getPosition());
    }
    
    private void setCurrentGridSquareLabel(Point.Double currentLonLat) {
    	if (coverageTestActive) {
        	if (isOffCourse(currentLonLat)) {
	        	currentGridSquare.setText("Off Course"); 
	        	currentGridSquare.setForeground(Color.RED);
        	} else {
        		currentGridSquare.setText(CoordinateUtils.lonLatToTestTile(currentLonLat, 
                	coverageTestSettings.getTileSizeArcSeconds(currentLonLat.getY()), 
                	coverageTestSettings.getGridReference(), coverageTestSettings.getGridSize())
        			.toFormattedTestTileDesignator());
        		currentGridSquare.setForeground(Color.GREEN);
        	}
        } else {	
        	currentGridSquare.setText(CoordinateUtils.lonLatToTestTile(currentLonLat, 
        		coverageTestSettings.getTileSizeArcSeconds(currentLonLat.getY()), 
        		coverageTestSettings.getGridReference(), coverageTestSettings.getGridSize())
        		.toFormattedTestTileDesignator());
        	currentGridSquare.setForeground(Color.BLACK);
        }
        
    }
    
    private boolean checkMapRecenter(final Point.Double point) {
    	boolean ret = false;
    	double longitudeThreshold = Math.abs((map.getMapLeftEdgeLongitude() - map.getMapRightEdgeLongitude()) / 4);
        double latitudeThreshold = Math.abs((map.getMapTopEdgeLatitude() - map.getMapBottomEdgeLatitude()) / 4);
        if ((map.getMapRightEdgeLongitude() - point.x < longitudeThreshold
                || point.x - map.getMapLeftEdgeLongitude() < longitudeThreshold
                || map.getMapTopEdgeLatitude() - point.y < latitudeThreshold
                || point.y - map.getMapBottomEdgeLatitude() < latitudeThreshold) && !mapDragged) {
        	ret = true;
        } else {
        	ret = false;
        }
    	return ret;
    }
    
    private void periodTimerActionListenerEvent(ActionEvent event) {
        periodTimerTimeOut = true;
    }

    private void measurementTimerActionListenerEvent(ActionEvent event) {
    	if(dataMode == DataMode.RECORD) processCoverageTestMeasurement(PositionSource.TIMER, serialGPSComponent.getGPSInterface().getPosition());
    }

    private void processStaticMeasurement(StaticMeasurement sm) {
    	if (dataMode == DataMode.RECORD) database.appendStaticRecord(sm);
    	if (processStaticMeasurements) compileStaticMeasurements();
    }
    
    private TestTile getCurrentTestTileAt(Point.Double currentLonLat) {
    	coverageTestSettings.setDegreesLatitude(currentLonLat.y);
    	
    	Point.Double tileSizeArcSeconds = coverageTestSettings.getTileSizeArcSeconds(currentLonLat.getY());
    	Point.Double gridReference = coverageTestSettings.getGridReference();
    	Point.Double testFieldDimensions = coverageTestSettings.getGridSize();
    	Precision precision = getRequiredPrecision(tileSizeArcSeconds);
    	Point.Double testTileLowerLeftLonLat = CoordinateUtils.lonLatToLowerLeftCornerOfTile(currentLonLat, 
    		tileSizeArcSeconds, gridReference, testFieldDimensions);
    	
        return new TestTile(logFileName, testTileLowerLeftLonLat, tileSizeArcSeconds, precision);
    }
    
    private boolean isOffCourse(Point.Double currentLonLat) {
    	coverageTestSettings.setDegreesLatitude(currentLonLat.y);
    	
    	Point.Double tileSizeArcSeconds = coverageTestSettings.getTileSizeArcSeconds(currentLonLat.getY());
    	Point.Double gridReference = coverageTestSettings.getGridReference();
    	Point.Double testFieldDimensions = coverageTestSettings.getGridSize();
    	Precision precision = getRequiredPrecision(tileSizeArcSeconds);
    	Point.Double testTileLowerLeftLonLat = CoordinateUtils.lonLatToLowerLeftCornerOfTile(currentLonLat, 
    		tileSizeArcSeconds, gridReference, testFieldDimensions);
    	
    	return !database.isTileCreated(new TestTile(logFileName, testTileLowerLeftLonLat, tileSizeArcSeconds, precision));
    }
    
    private void processCoverageTestMeasurement(PositionSource source, Point.Double currentLonLat) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
		    	if (database == null) return;
		    	
		    	TestTile workingTile = getCurrentTestTileAt(currentLonLat);
		
		        if (workingTile != null) {
		        	
			        switch (coverageTestSettings.getSampleTimingMode()) { //TODO: come up with improved algorithm
			            case 0:
			                measurementDelay = (int) Math.round(milliSecondsBetweenMeasurements(
			                    workingTile.getTileSize().x, serialGPSComponent.getGPSInterface().getSpeedMadeGoodKPH(),
			                    coverageTestSettings.getDotsPerTile()));
			                break;
			            case 1:
			            	measurementDelay = (int) Math.round(milliSecondsBetweenMeasurements(
			            		workingTile.getTileSize().x, serialGPSComponent.getGPSInterface().getSpeedMadeGoodKPH(),
			                    coverageTestSettings.getMinSamplesPerTile() + 5));
			                break;
			            case 2:
			            	measurementDelay = 200;
			                break;
			        }
			        
			        averageRssiInCurrentTile.setText(averageRssiInCurrentTileText);
			        averageBerInCurrentTile.setText(averageBerInCurrentTileText);
			        averageSinadInCurrentTile.setText(averageSinadInCurrentTileText);
			        
			        if (source == PositionSource.GPS && coverageTestActive) {
			            measurementTimer.setDelay(measurementDelay);
			            measurementTimer.setInitialDelay(0);
			            setMeasurementPeriodText(String.format("%4d", measurementDelay)); 
			            if (!measurementTimer.isRunning()) measurementTimer.start();
			        } else if (source == PositionSource.MANUAL) {
			            measurementTimer.stop();
			            setMeasurementPeriodText("MAN"); 
			            addSignalMarker(workingTile);
			        } else if (source == PositionSource.TIMER) {
			        	addSignalMarker(workingTile);
			        }
		        } else {
		        	currentGridSquare.setText("Off Course"); 
		        	currentGridSquare.setForeground(Color.RED);
		        }
            }
    	});
    }

    private void setMeasurementPeriodText(String text) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	measurementPeriod.setText(text);
            }
		});
    }
    
    private void setMinMeasurementsPerTileText(String text) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	minMeasurementsPerTile.setText(text);
            }
		});
    }
    
    private void setMaxMeasurementsPerTileText(String text) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	maxMeasurementsPerTile.setText(text);
            }
		});
    }

	private void tileRecordReadyEvent(final PropertyChangeEvent event) {	
		currentTestTile = (TestTile) event.getNewValue();
	}
	
	private void updateTileDisplay(TestTile testTile) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
				if ((testTile.getMeasurementCount() > 0 && testTile.getMeasurementCount() < coverageTestSettings.getMinSamplesPerTile() 
						&& coverageTestSettings.getMinSamplesPerTile() != -1)
						|| (periodTimerTimeOut && coverageTestSettings.getMinSamplesPerTile() == -1)) {
		            averageRssiInCurrentTileText = String.format("%3.1f", testTile.getAvgdBm()) + " dBm"; 
		            averageBerInCurrentTileText = String.format("%1.2f", testTile.getAvgBer()) + " %"; 
		            averageSinadInCurrentTileText = String.format("%2.1f", testTile.getAvgSinad()) + " dB"; 
				}
				if ((testTile.getMeasurementCount() == coverageTestSettings.getMinSamplesPerTile() &&
		                coverageTestSettings.getMinSamplesPerTile() != -1)
		                || (periodTimerTimeOut && coverageTestSettings.getMinSamplesPerTile() == -1)) {
		            periodTimer.stop();
		            periodTimerTimeOut = false;
		            periodTimerHalt = true;
		            if (coverageTestSettings.isAlertOnMinimumSamplesPerTileAcquired() && dataMode != DataMode.STOP) {
		                playTileCompleteSound();
		            }
		        }
            }
    	});    
	}

	private void showSignalMarker(int mode, int channel) {
        if (Math.abs(lastDotLocation.x - serialGPSComponent.getGPSInterface().getPosition().x) > 0.0005
                || Math.abs(lastDotLocation.y - serialGPSComponent.getGPSInterface().getPosition().y) > 0.0005) {
            Color dotColor;
            switch (mode) {
                case 0:
                    dotColor = sinadToColor(radioComponent.getRadioInterface().getSinad(channel));
                    map.addSignalMarker(serialGPSComponent.getGPSInterface().getPosition(), coverageTestSettings.getSignalMarkerRadius(), dotColor);
                    break;
                case 1:
                    dotColor = dBmToColor(radioComponent.getRadioInterface().getdBm(channel));
                    map.addSignalMarker(serialGPSComponent.getGPSInterface().getPosition(), coverageTestSettings.getSignalMarkerRadius(), dotColor);
                    break;
                case 2:
                    dotColor = berToColor(radioComponent.getRadioInterface().getBer(channel));
                    map.addSignalMarker(serialGPSComponent.getGPSInterface().getPosition(), coverageTestSettings.getSignalMarkerRadius(), dotColor);
                    break;
            }
            lastDotLocation = serialGPSComponent.getGPSInterface().getPosition();
        }
	}
	
	private void addSignalMarker(TestTile testTile) {
        if (dataMode == DataMode.RECORD && lastInputSource != PositionSource.ARCHIVE 
                && ((testTile.getMeasurementCount() <= coverageTestSettings.getMaxSamplesPerTile()
                && coverageTestSettings.getMinSamplesPerTile() != -1)
                || (!periodTimerHalt && coverageTestSettings.getMinSamplesPerTile() == -1))) {

        	MeasurementSet measurementSet = new MeasurementSet();
	        
	        measurementSet.setMarker(markerCounter);
	        measurementSet.setMillis(networkTime.currentTimeInMillis());
	        measurementSet.setPosition(serialGPSComponent.getGPSInterface().getPosition());
	        measurementSet.setTestTileID(testTile.getID());
	        
	        database.appendMeasurementSet(testTile, measurementSet);
	        map.changeTestTileColor(testTile, getTestTileColor(testTile));
        }
	}
    
    private void createBlankTestTile(Point.Double lonLat) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	map.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
    	});
    	learnModeHold = true;
    	if (learnMode && mapSettings.isShowGrid()) {
    		if (database == null) {
    			invokeLaterInDispatchThreadIfNeeded(new Runnable() {
    	            @Override
    	            public void run() {
    	            	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
    	                    "Please create or open the data file for this coverage test", "SQL Database Error", JOptionPane.ERROR_MESSAGE); 
    	            }
    			});
    		} else {
    			Point.Double tileSizeArcSeconds = coverageTestSettings.getTileSizeArcSeconds(lonLat.getY());
    			Precision precision = getRequiredPrecision(tileSizeArcSeconds);
    			Point.Double gridReference = coverageTestSettings.getGridReference();
    			Point.Double gridSize = coverageTestSettings.getGridSize();
    			Point.Double testTileLowerLeftLonLat = CoordinateUtils.lonLatToLowerLeftCornerOfTile(lonLat, 
    				tileSizeArcSeconds, gridReference, gridSize);
    	        TestTile newTile = new TestTile(logFileName, testTileLowerLeftLonLat, tileSizeArcSeconds, precision);
    			TestTile existingTile = database.testTileExists(newTile);
    	        if (existingTile != null) {
			    	database.deleteTestTile(existingTile);
		    	} else {
		    		database.appendTileRecord(newTile);
		    	}
    		}
    	}
    }

    private Color getGpsColor(GPSInterface.FixQuality fixQuality) {
        switch (fixQuality) {
		    case FIX_3D: return Color.GREEN;
	        case FIX_2D: return Color.YELLOW;
	        case DGPS_FIX: return Color.BLUE;
	        case PPS_FIX: return Color.YELLOW;
	        case RTK: return Color.YELLOW;
	        case FLOAT_RTK: return Color.YELLOW;
	        case ESTIMATED: return Color.YELLOW;
	        case MANUAL: return Color.YELLOW;
	        case SIMULATION: return Color.YELLOW;
	        case OFF_LINE: return Color.RED;
	        case ACQUIRING: return Color.RED;
	        case INVALID: return Color.RED;
	        default: return Color.RED;
        }
	}

    private void playTileCompleteSound() {
    	new AePlayWave(DEFAULT_TILE_COMPLETE_SOUND);
    }
    	
    private void zoomOutMouseDownTimerActionListenerEvent(ActionEvent event) {
        doZoomOut();
    }

    private void zoomInMouseDownTimerActionListenerEvent(ActionEvent event) {
        doZoomIn();
    }

    private void gpsValidHeadingTimerActionListenerEvent(ActionEvent event) {
        gpsCompassRose.setSelectColor(Color.GRAY);
        map.setGpsSymbolAngle(360);
    }

    private void gpsRxDataTimerActionListenerEvent(ActionEvent event) {
    	if (gpsButton.isSelected()) gpsRxData.setBackground(new Color(255, 0, 0));
        gpsRxDataTimer.stop();
    }

    private void gpsTxDataTimerActionListenerEvent(ActionEvent event) {
    	if (gpsButton.isSelected()) gpsTxData.setBackground(new Color(255, 0, 0));
        gpsTxDataTimer.stop();
    }

    private void radioRxDataTimerActionListenerEvent(ActionEvent event) {
    	if (radioButton.isSelected()) radioRxData.setBackground(new Color(255, 0, 0));
        radioRxDataTimer.stop();
    }

    private void radioTxDataTimerActionListenerEvent(ActionEvent event) {
    	if (radioButton.isSelected()) radioTxData.setBackground(new Color(255, 0, 0));
        radioTxDataTimer.stop();
    }

    private void aprsRxDataTimerActionListenerEvent(ActionEvent event) {
    	if (aprsButton.isSelected()) aprsRxData.setBackground(new Color(255, 0, 0));
        aprsRxDataTimer.stop();
    }

    private void aprsTxDataTimerActionListenerEvent(ActionEvent event) {
    	if (aprsButton.isSelected()) aprsTxData.setBackground(new Color(255, 0, 0));
        aprsTxDataTimer.stop();
    }

    private void aprsComponentMenuActionListenerEvent(ActionEvent event) {
        aprsComponent.showSettingsDialog(true);
    }

    private void gpsComponentMenuActionListenerEvent(ActionEvent event) {
        serialGPSComponent.showSettingsDialog(true);
    }

    private void receiverComponentMenuActionListenerEvent(ActionEvent event) {
        radioComponent.showSettingsDialog(true);
    }

    private void mapSettingsMenuActionListenerEvent(ActionEvent event) {
        mapSettings.showSettingsDialog(true);
    }

    private void mapBulkDownloaderMenuActionListenerEvent(ActionEvent event) {
        map.showBulkDownloaderPanel();
    }
    
    private void mapStatisticsMenuActionListenerEvent(ActionEvent event) {
        map.showStatisticsPanel();
    }
    
    private void mapLayerSelectorMenuActionListenerEvent(ActionEvent event) {
        map.showLayerSelectorPanel();
    }
    
    private void signalAnalysisMenuActionListenerEvent(ActionEvent event) {
        signalAnalysis.showSettingsDialog(true);
    }
    
    private void databseConfigMenuActionListenerEvent(ActionEvent event) {
        databaseConfig.showSettingsDialog(true);
    }
    
    private void staticSignalLocationSettingsMenuActionListenerEvent(ActionEvent event) {
        staticTestSettings.showSettingsDialog(true);
    }
    
    private void coverageTestSettingsMenuActionListenerEvent(ActionEvent event) {
        coverageTestSettings.showSettingsDialog(true);
    }
    
    private void sinadChangedChangeListenerEvent(PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	sinadArray[radioComponent.getRadioInterface().getCurrentChannel()] = (int) sinad.getSINAD();
            	setSignalQualityDisplay(testMode, !radioComponent.isVfoMode());
            	if (lastInputSource == PositionSource.MANUAL && coverageTestSettings.getManualDataCollectionMode() == 0
                && coverageTestActive && dataMode == DataMode.RECORD) {
            		processCoverageTestMeasurement(PositionSource.MANUAL, serialGPSComponent.getGPSInterface().getPosition());
		        }
            }
        }); 
    }
    
    private void updateOpenDatabaseMonitor(int progress) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	        @Override
	        public void run() {
	            fileOpenMonitor.setNote(String.format("Completed %d%% of Database Recovery", progress)); 
	            fileOpenMonitor.setProgress(progress);
	        }
	    });
    }
    
    private void updateNewFileMonitor(int progress) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
	        @Override
	        public void run() {
	            newFileMonitor.setNote(String.format("Completed %d%% of new Database Build", progress)); 
	            newFileMonitor.setProgress(progress);
	        }
	    });
    }
    
    private void createNewDataFile(final File file) {
    	updateNewFileMonitor(33);
    	database = new DerbyRelationalDatabase();
    	database.addPropertyChangeListener(databaseListener);
		recordCount = 0;
		markerCounter = 0;
		lastDotLocation = new Point.Double(0,0);
		map.deleteAllQuads();
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	tilesCompleted.setText(String.format("%07d", 0)); 
				recordCountLabel.setText(String.format("%08d", recordCount)); 
				markerID.setText(String.format("%5d", markerCounter)); 
				if (file == null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
					String fln = sdf.format(Calendar.getInstance().getTime()) + ".sql"; 
					updateNewFileMonitor(66);
					database.openDatabase(new File(dataDirectory.getPath() + File.separator + fln), USERNAME, PASSWORD,
							testSettings);
					logFileName = fln;
					logFileNameLabel.setText(logFileName);
				} else {
					database.openDatabase(file, USERNAME, PASSWORD, testSettings);
					logFileName = file.getName();
					logFileNameLabel.setText(logFileName);
				}
            }
		});
    }
    
    private void openDataFileMenuItemActionListenerEvent(ActionEvent event) {
    	openDataFile();
    }
    
    private int openDataFile() {
    	JFileChooser fileChooser = new JFileChooser(dataDirectory);
    	
    	fileChooser.setFileView(new FileView() {
            @Override
            public Boolean isTraversable(File f) {
                return (f.isDirectory()); 
            }
        });
    	
    	FileNameExtensionFilter filter = new FileNameExtensionFilter("Database Files", "sql"); 
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Open Database File"); 
		
		int returnVal = fileChooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			systemPref.put("LastDataFileDirectory", fileChooser.getCurrentDirectory().getPath()); 
			dataDirectory = fileChooser.getCurrentDirectory();
		    if (!fileChooser.getCurrentDirectory().exists()) {
		    	createNewDataFile(fileChooser.getCurrentDirectory());
		    	returnVal = 3;
		    } else {
			    database = new DerbyRelationalDatabase();
		        database.addPropertyChangeListener(databaseListener);
		        database.openDatabase(fileChooser.getSelectedFile(), USERNAME, PASSWORD, testSettings);
		        logFileName = fileChooser.getSelectedFile().getName();
			    logFileNameLabel.setText(logFileName);
		    }
		    dataMode = DataMode.RECORD;
		}
		remove(fileChooser);
		repaint();
		return returnVal;
    }

    private void closeDataFileMenuItemActionListenerEvent(ActionEvent event) {
        if (dataMode != DataMode.CLOSED && !database.isClosed()) {
			database.close();
		}
    }

    private void saveDataFileMenuItemActionListenerEvent(ActionEvent event) {
        if (dataMode != DataMode.CLOSED && !database.isClosed()) {
		    database.save();
		}
    }

    private void saveAsDataFileMenuItemActionListenerEvent(ActionEvent event) {
        if (dataMode != DataMode.CLOSED) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Test Database Files", "sql"); 
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Save Database File As"); 
            fileChooser.showSaveDialog(null);
            File from = null;
            File to = null;
            if (event.getID() == JFileChooser.APPROVE_OPTION) {
            	from = dataDirectory;
				to = fileChooser.getSelectedFile();
				database.close();
				if (!from.renameTo(to)) {
					log.log(Level.WARNING, "IOException", "File rename failed"); 
				    invokeLaterInDispatchThreadIfNeeded(new Runnable() {
				        @Override
				        public void run() {
				            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
				            	"File rename failed", "I/O Exception", JOptionPane.ERROR_MESSAGE); 
				        }
				    });
				    database.openDatabase(to, USERNAME, PASSWORD);
				} else {
					dataDirectory = to;
					systemPref.put("LastDataFileDirectory", dataDirectory.getParent()); 
					logFileNameLabel.setText(fileChooser.getSelectedFile().getName());
				}
            } 
            remove(fileChooser);
        }
    }

    private void gpsReceivedDataPropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
		        nmeaSentenceStringLabel.setText((String) event.getNewValue());
		    }
		});
    }

    private void gpsCourseMadeGoodTruePropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
		    	gpsCompassRose.setSelectColor(getGpsColor(serialGPSComponent.getGPSInterface().getFixQuality()));
		        gpsValidHeadingTimer.restart();
		        gpsCompassRose.setHeading((int) serialGPSComponent.getGPSInterface().getCourseMadeGoodTrue());
		    }
		});
    }

    private Color getGpsStatusForegroundColor(GPSInterface.FixQuality fixQuality) {
    	switch (fixQuality) {
		    case FIX_3D: return Color.BLACK;
	        case FIX_2D: return Color.BLACK;
	        case DGPS_FIX: return Color.WHITE;
	        case PPS_FIX: return Color.BLACK;
	        case RTK: return Color.BLACK;
	        case FLOAT_RTK: return Color.BLACK;
	        case ESTIMATED: return Color.BLACK;
	        case MANUAL: return Color.BLACK;
	        case SIMULATION: return Color.BLACK;
	        case OFF_LINE: return Color.BLACK;
	        case ACQUIRING: return Color.BLACK;
	        case INVALID: return Color.BLACK;
	        case ERROR: return Color.BLACK;
	        default: return Color.GRAY;
    	}
	}
    
    private Color getGpsStatusBackgroundColor(GPSInterface.FixQuality fixQuality) {
    	switch (fixQuality) {
		    case FIX_3D: return Color.GREEN;
	        case FIX_2D: return Color.YELLOW;
	        case DGPS_FIX: return Color.BLUE;
	        case PPS_FIX: return new Color(127, 255, 127);
	        case RTK: return Color.ORANGE;
	        case FLOAT_RTK: return Color.MAGENTA;
	        case ESTIMATED: return Color.YELLOW;
	        case MANUAL: return Color.WHITE;
	        case SIMULATION: return Color.CYAN;
	        case OFF_LINE: return new Color(127, 0, 0);
	        case ACQUIRING: return Color.RED;
	        case INVALID: return Color.RED;
	        case ERROR: return Color.GRAY;
	        default: return Color.GRAY;
    	}
	}
    
    private String getGpsStatusText(GPSInterface.FixQuality fixQuality) {
        switch (fixQuality) {
		    case FIX_3D: return "3D Fix"; 
	        case FIX_2D: return "2D Fix"; 
	        case DGPS_FIX: return "DGPS Fix"; 
	        case PPS_FIX: return "PPS Fix"; 
	        case RTK: return "RTK"; 
	        case FLOAT_RTK: return "FLT RTK"; 
	        case ESTIMATED: return "EST"; 
	        case MANUAL: return "MANUAL"; 
	        case SIMULATION: return "SIM"; 
	        case OFF_LINE: return "OFF LINE"; 
	        case ACQUIRING: return "ACQUIRE"; 
	        case INVALID: return "INVALID"; 
	        case ERROR: return "DOWN"; 
	        default: return "ERROR"; 
        }
	}
    
    private void gpsFixQualityPropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	FixQuality fixQuality = (FixQuality) event.getNewValue();
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	centerOnGpsButton.setEnabled(serialGPSComponent.getGPSInterface().isValidFix());
		    	gpsStatus.setBackground(getGpsStatusBackgroundColor(fixQuality));
		    	gpsStatus.setForeground(getGpsStatusForegroundColor(fixQuality));
		    	gpsStatus.setText(getGpsStatusText(fixQuality));
		    	map.setGpsSymbolColor(getGpsColor(fixQuality));
		    	latitude.setForeground(Color.BLACK);
		    	longitude.setForeground(Color.BLACK);
		    	currentMGRS.setForeground(Color.BLACK);
		    	if (!coverageTestActive) currentGridSquare.setForeground(Color.BLACK);
		    	speedMadeGood.setForeground(Color.BLACK);
            }
        });
    }

   	private void gpsRxDataPropertyChangeListenerEvent(final PropertyChangeEvent event) {
   		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	gpsRxData.setBackground(Color.GREEN);
            }
        });
        gpsRxDataTimer.start();
    }

    private void gpsCTSHoldingPropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            gpsCTS.setBackground(Color.GREEN);
		        } else {
		            gpsCTS.setBackground(Color.RED);
		        }
            }
        });	
    }

    private void gpsDSRHoldingPropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            gpsDSR.setBackground(Color.GREEN);
		        } else {
		            gpsDSR.setBackground(Color.RED);
		        }
            }
        });
    }

    private void gpsCDHoldingPropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            gpsCD.setBackground(Color.GREEN);
		        } else {
		            gpsCD.setBackground(Color.RED);
		        }           	
            }
        });
    }
    
    private void radioBusyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if (!radioButton.isSelected()) return;
		    	if ((boolean) event.getNewValue()) {
		            radioStatus.setText("BUSY"); 
		            radioStatus.setBackground(Color.YELLOW);
		        } else {
		            radioStatus.setText("MON"); 
		            radioStatus.setBackground(Color.RED);
		        }
            }
        });
    }
    
    private void radioPowerChangeListenerEvent(final PropertyChangeEvent event) {
    	if ((boolean) event.getNewValue()) {
    		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
                @Override
                public void run() {
                	radioStatus.setText("POWER"); 
                	radioStatus.setBackground(new Color(127,0,0));
                }
            });
        }
    }
    
    private void radioReadyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		    		radioStatus.setText("ON LINE"); 
		            radioStatus.setBackground(Color.GREEN);
		        } else {
		        	radioStatus.setText("OFF LINE"); 
		            radioStatus.setBackground(Color.RED);
		        }
            }
        });
    }
    
    private void radioRSSIChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
		        setSignalQualityDisplay(testMode, !radioComponent.isVfoMode());
		        if (lastInputSource == PositionSource.MANUAL && coverageTestSettings.getManualDataCollectionMode() == 0
		        		&& coverageTestActive && dataMode == DataMode.RECORD) {
		        	processCoverageTestMeasurement(PositionSource.MANUAL, serialGPSComponent.getGPSInterface().getPosition());
                }
            }
        });
    }
    
    private void radioScanChannelReadyChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
		        if (testMode != TestMode.OFF) signalMeterArray.setMeterLevels(radioComponent.getRadioInterface().getPercentArray());
		        setSignalQualityDisplay(testMode, !radioComponent.isVfoMode());
		        if (lastInputSource == PositionSource.MANUAL && coverageTestSettings.getManualDataCollectionMode() == 0
		        		&& coverageTestActive && dataMode == DataMode.RECORD) {
		        	processCoverageTestMeasurement(PositionSource.MANUAL, serialGPSComponent.getGPSInterface().getPosition());
                }
            }
        });
    }
    
    private void radioBERChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if (testMode != TestMode.OFF) setSignalQualityDisplay(testMode, !radioComponent.isVfoMode());
		        if (lastInputSource == PositionSource.MANUAL && coverageTestSettings.getManualDataCollectionMode() == 0
		        		&& coverageTestActive && dataMode == DataMode.RECORD) {
		        	processCoverageTestMeasurement(PositionSource.MANUAL, serialGPSComponent.getGPSInterface().getPosition());
                }
            }  
        });
    }

    private void setSignalQualityDisplay(TestMode testMode, boolean isScanning) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	int scanLength;
            	if (isScanning) {
            		scanLength = signalQuality.length;
            		if (testMode != TestMode.OFF) signalMeterArray.setMeterLevels(radioComponent.getRadioInterface().getPercentArray());
            	} else {
            		scanLength = 1;
            		if (testMode != TestMode.OFF) signalMeterArray.setMeterLevel(0, radioComponent.getRadioInterface().getPercentList().get(0));
            	}
            	for (int i = 0; i < scanLength; i++) {
            		if (testMode == TestMode.OFF) {
            			signalQuality[i].setText(""); 
            		} else if (testMode == TestMode.RSSI) {
		        		signalQuality[i].setText(String.format("%3.1f", radioComponent.getRadioInterface().getdBm(i))); 
		        	} else if (testMode == TestMode.SINAD) {
		        		signalQuality[i].setText(String.format("%3.1f", radioComponent.getRadioInterface().getdBm(i)) + " / " + 
		        			String.format("%2d", sinadArray[i])); 
		        	} else if (testMode == TestMode.BER) {
		        		signalQuality[i].setText(String.format("%3.1f", radioComponent.getRadioInterface().getdBm(i)) + " / " + 
		        			String.format("%1.2f", radioComponent.getRadioInterface().getBer(i)));	 
		        	}
	            }
            }
	    });
    }

    private void aprsTxDataChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	aprsTxData.setBackground(Color.GREEN);
            }
        });
        aprsTxDataTimer.start();
    }

    private void aprsRxCharChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	aprsRxData.setBackground(Color.GREEN);
            }
        });
        aprsRxDataTimer.start();
    }

    private void aprsCTSHoldingChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            aprsCTS.setBackground(Color.GREEN);
		        } else {
		            aprsCTS.setBackground(Color.RED);
		        }
            }
        });
    }

    private void aprsDSRHoldingChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            aprsDSR.setBackground(Color.GREEN);
		        } else {
		            aprsDSR.setBackground(Color.RED);
		        }
            }
        });
    }

    private void aprsCDHoldingChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            aprsCD.setBackground(Color.GREEN);
		        } else {
		            aprsCD.setBackground(Color.RED);
		        }
            }
        });
    }
    
    private void radioTxDataChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	radioTxData.setBackground(Color.GREEN);
            }
        });
        radioTxDataTimer.start();
    }
    
    private void radioRxCharChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	radioRxData.setBackground(Color.GREEN);
            }
        });
    	radioRxDataTimer.start();
    }
    
    private void radioRxDataChangeListenerEvent(final PropertyChangeEvent event) {

    }

    private void radioOnlineChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		    		radioCTS.setBackground(Color.RED);
		    		radioCD.setBackground(Color.RED);
		    		radioDSR.setBackground(Color.RED);
		    		radioRxData.setBackground(Color.RED);
		    		radioTxData.setBackground(Color.RED);
		        } else {
		        	radioCTS.setBackground(new Color(127,0,0));
		    		radioCD.setBackground(new Color(127,0,0));
		    		radioDSR.setBackground(new Color(127,0,0));
		    		radioRxData.setBackground(new Color(127,0,0));
		    		radioTxData.setBackground(new Color(127,0,0));
		        }
            }
        });
    }
    
    private void gpsOnlineChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		    		gpsCTS.setBackground(Color.RED);
		    		gpsCD.setBackground(Color.RED);
		    		gpsDSR.setBackground(Color.RED);
		    		gpsRxData.setBackground(Color.RED);
		    		gpsTxData.setBackground(Color.RED);
		        } else {
		        	gpsCTS.setBackground(new Color(127,0,0));
		    		gpsCD.setBackground(new Color(127,0,0));
		    		gpsDSR.setBackground(new Color(127,0,0));
		    		gpsRxData.setBackground(new Color(127,0,0));
		    		gpsTxData.setBackground(new Color(127,0,0));
		        }
            }
        });
    }
    
    private void invalidComPortChangeListenerEvent(final PropertyChangeEvent event, final String device) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                    "The " + device + " is configured to use comm port " + event.getNewValue() + "\n" +  
                    "Please select a valid comm port from the " + device + " settings menu.\n", 
                    "Comm Port Error", JOptionPane.ERROR_MESSAGE); 
            }
        });
    }
    
    private void radioCTSHoldingChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            radioCTS.setBackground(Color.GREEN);
		        } else {
		            radioCTS.setBackground(Color.RED);
		        }
            }
        });
    }

    private void radioDSRHoldingChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
            		radioDSR.setBackground(Color.GREEN);
		        } else {
		            radioDSR.setBackground(Color.RED);
		        }
            }
        });
    }

    private void radioCDHoldingChangeListenerEvent(final PropertyChangeEvent event) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	if ((boolean) event.getNewValue()) {
		            radioCD.setBackground(Color.GREEN);
		        } else {
		            radioCD.setBackground(Color.RED);
		        }
            }
        });
    }

	private void updateCoverageTestRestoreMonitorProgress(int progress) {
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	coverageTestRestoreMonitor.setProgress(progress);
        		coverageTestRestoreMonitor.setNote(String.format("Completed %d%% of coverage test restore\n", progress)); 
            }
        });
	}
	
	private void updateStaticTestRestoreMonitorProgress(int progress) {
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	staticTestRestoreMonitor.setProgress(progress);
        		staticTestRestoreMonitor.setNote(String.format("Completed %d%% of static test restore\n", progress)); 
            }
        });
	}
	
	private void testSettingsLoadedEvent(final PropertyChangeEvent event) {
		testSettings = ((TestSettings) event.getNewValue()); 
		populateCoverageTestSettingDialogFromCurrentTestSettings(testSettings);
		setMeasurementPeriodText(String.format("%4d", measurementDelay)); 
		setMinMeasurementsPerTileText(String.format("%6d", testSettings.getMinimumSamplesPerTile())); 
		setMaxMeasurementsPerTileText(String.format("%6d", testSettings.getMaximumSamplesPerTile())); 
	}
	
	private void populateCurrentTestSettingsFromCoverageTestSettingsDialog() {
		testSettings.setDotsPerTile(coverageTestSettings.getDotsPerTile());
		testSettings.setGridReference(coverageTestSettings.getGridReference());
		testSettings.setGridSize(coverageTestSettings.getGridSize());
		testSettings.setMaximumSamplesPerTile(coverageTestSettings.getMaxSamplesPerTile());
		testSettings.setMinimumSamplesPerTile(coverageTestSettings.getMinSamplesPerTile());
		testSettings.setSampleTimingMode(coverageTestSettings.getSampleTimingMode());
		testSettings.setTileSize(coverageTestSettings.getTileSizeArcSeconds(coverageTestSettings.getGridReference().y));
		testSettings.setMinimumTimePerTile(coverageTestSettings.getMinTimePerTile());
	}
	
	private void populateCoverageTestSettingDialogFromCurrentTestSettings(TestSettings testSettings) {
		coverageTestSettings.setMinSamplesPerTile(testSettings.getMinimumSamplesPerTile());
		coverageTestSettings.setMaxSamplesPerTile(testSettings.getMaximumSamplesPerTile());
		coverageTestSettings.setTileSize(testSettings.getTileSize());
		coverageTestSettings.setGridReference(testSettings.getGridReference());
		coverageTestSettings.setGridSize(testSettings.getGridSize());
		coverageTestSettings.setDotsPerTile(testSettings.getDotsPerTile());
		coverageTestSettings.setMinTimePerTile(testSettings.getMinimumTimePerTile());
		coverageTestSettings.setSampleTimingMode(testSettings.getSampleTimingMode());
		map.setTileSize(coverageTestSettings.getTileSizeArcSeconds(coverageTestSettings.getGridReference().y));
		map.setGridReference(coverageTestSettings.getGridReference());
		map.setGridSize(coverageTestSettings.getGridSize());
		map.showGrid(true);
		map.showTestTiles(true);
	}
	
    private void staticMeasurementDataReadyEvent(final PropertyChangeEvent event) {

	}

	private void measurementSetDataReadyEvent(final PropertyChangeEvent event) {
		
	}
	
	private void allStaticRecordsReadyEvent(final PropertyChangeEvent event) {
		compileStaticMeasurements();
	}
	
	private Color getTestTileColor(TestTile testTile) {
		if (testTile.getMeasurementCount() == 0) return tileSelectedColor;
		else if (testTile.getMeasurementCount() < coverageTestSettings.getMinSamplesPerTile()) return tileInProgressColor;
		else if (testTile.getMeasurementCount() >= coverageTestSettings.getMaxSamplesPerTile()) return tileCompleteColor;
		else return tileSelectedColor;
	}
	
	private void allTileRecordsReadyEvent(final PropertyChangeEvent event) {
		List<TestTile> tileList = database.getTileRecordList();
		if (tileList != null) { 
			for (TestTile testTile : tileList) {
				GeoTile geoTile = new GeoTile(testTile.getLonLat(), testTile.getTileSize());
	            map.addTestTile(geoTile, getTestTileColor(testTile), testTile.getID());
	            updateTileDisplay(testTile);
			}
		}
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
				totalTiles.setText(String.format("%07d", database.getTileRecordList().size())); 
		    }
		});
    	updateOpenDatabaseMonitor(100);
    	updateNewFileMonitor(100);
		if (coverageTestActive || staticTestActive) {
	    	setDataMode(DataMode.RECORD);
		} else {
			setDataMode(DataMode.OPEN);
		}
	}
	
	private void measurementSetTableAppendedEvent(final PropertyChangeEvent event) {
		for (int i = 0; i < 10; i++) {
        	if (radioComponent.getRadioInterface().getScanSelectList(i)) {
        		Measurement measurement = new Measurement();
        		measurement.setFrequency(radioComponent.getRadioInterface().getFrequency(i));
	        	measurement.setSelected(radioComponent.getRadioInterface().getScanSelectList(i));
	        	measurement.setBer(radioComponent.getRadioInterface().getBer(i));        
	        	measurement.setSinad(radioComponent.getRadioInterface().getSinad(i));
	        	measurement.setdBm(radioComponent.getRadioInterface().getdBm(i));
	        	MeasurementSet measurementSet = (MeasurementSet) event.getNewValue();
	        	database.appendMeasurement(measurementSet, measurement);
	        	showSignalMarker(coverageTestSettings.getSignalQualityDisplayMode(), radioComponent.getRadioInterface().getCurrentChannel());
        	}
        }
	}
	
	private void staticTableAppendedEvent(final PropertyChangeEvent event) {
		
	}
	
	private void tileTableAppendedEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
		        int total = database.getTileRecordList().size();
				totalTiles.setText(String.format("%07d", total)); 
				learnModeHold = false;
				TestTile newTile = (TestTile) event.getNewValue();
				map.addTestTile(new GeoTile(newTile.getLonLat(), newTile.getTileSize()), getTestTileColor(newTile), newTile.getID());
				updateTileDisplay(newTile);
				map.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		    }
		});
	}
	
	private void tileDeletedEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
		        int total = database.getTileRecordList().size();
				totalTiles.setText(String.format("%07d", total)); 
				learnModeHold = false;
				TestTile testTile = (TestTile) event.getNewValue();
		    	map.deleteTestTile(testTile);
				map.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		    }
		});
	}
	
	private void measurementSetRecordCountReadyEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
		        recordCountLabel.setText(String.format("%08d", (Integer) event.getNewValue())); 
		    }
		});
	}

	private void tileCompleteCountReadyEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
				tilesCompleted.setText(String.format("%07d", (int) event.getNewValue())); 
            }
		});
	}
	
	private void staticRecordCountReadyEvent(final PropertyChangeEvent event) {
		
	}
	
	private void tileRecordCountReadyEvent(final PropertyChangeEvent event) {
		if (coverageTestActive) {
			if ((Integer) event.getNewValue() == 0) {
				invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		            @Override
		            public void run() {
		            	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
			                "Tiles must be selected before coverage test can begin", "Coverage Test Error", JOptionPane.ERROR_MESSAGE); 
		            }
		        });
				stopCoverageTest();
				return;
			}
	    	if (!radioComponent.isRadioOnLine()) startRadio();
	    	if (!serialGPSComponent.isGpsOnLine()) startGps();
	        coverageTestSettings.setControlsEnabled(false);
	        periodTimerTimeOut = false;
	        periodTimerHalt = false;
	        periodTimer.start();
		}
		invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
				totalTiles.setText(String.format("%07d", event.getNewValue())); 
		    }
		});;
	}

	private void databaseClosedEvent(final PropertyChangeEvent event) {
		database.removePropertyChangeListener(databaseListener);
    	setDataMode(DataMode.CLOSED);
    	signalReadyToExit(DATABASE_CLOSED);
	}

	private void databaseOpenEvent(final PropertyChangeEvent event) {
		setDataMode(DataMode.OPEN);
	}	

    private void setDataMode(DataMode mode) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
		    @Override
		    public void run() {
		        switch (mode) {
		            case OPEN:
		                newDataFileMenuItem.setEnabled(false);
		                openDataFileMenuItem.setEnabled(false);
		                closeDataFileMenuItem.setEnabled(true);
		                saveDataFileMenuItem.setEnabled(true);
		                saveAsDataFileMenuItem.setEnabled(true);
		                
		                newDataFileButton.setEnabled(false);
		                openDataFileButton.setEnabled(false);
		                saveDataFileButton.setEnabled(true);
		                
		                closeDataFileButton.setEnabled(true);
		                closeDataFileButton.setSelected(false);
		                stopDataFileButton.setEnabled(true);
		                stopDataFileButton.setSelected(true);
		                recordDataFileButton.setEnabled(true);
		                recordDataFileButton.setSelected(false);
		                break;
		            case CLOSED:
		            	map.deleteAllTestTiles();
		            	learnMode = false;
		            	
		                newDataFileMenuItem.setEnabled(true);
		                openDataFileMenuItem.setEnabled(true);
		                closeDataFileMenuItem.setEnabled(false);
		                saveDataFileMenuItem.setEnabled(false);
		                saveAsDataFileMenuItem.setEnabled(false);
		                
		                newDataFileButton.setEnabled(true);
		                openDataFileButton.setEnabled(true);
		                saveDataFileButton.setEnabled(false);
		                
		                learnModeButton.setSelected(false);
		                openDataFileButton.setSelected(false);
		                newDataFileButton.setSelected(false);
		                closeDataFileButton.setEnabled(false);
		                closeDataFileButton.setSelected(false);
		                stopDataFileButton.setEnabled(false);
		                stopDataFileButton.setSelected(false);
		                recordDataFileButton.setEnabled(false);
		                recordDataFileButton.setSelected(false);
		                
		                minMeasurementsPerTile.setText(""); 
		                maxMeasurementsPerTile.setText(""); 
		                measurementsThisTile.setText(""); 
		                measurementPeriod.setText(""); 
		                tilesCompleted.setText(""); 
		                totalTiles.setText(""); 
		                recordCountLabel.setText(""); 
		                logFileNameLabel.setText(""); 
		                break;
		            case STOP:
		                newDataFileMenuItem.setEnabled(false);
		                openDataFileMenuItem.setEnabled(true);
		                closeDataFileMenuItem.setEnabled(true);
		                saveDataFileMenuItem.setEnabled(true);
		                saveAsDataFileMenuItem.setEnabled(true);
		                
		                newDataFileButton.setEnabled(false);
		                openDataFileButton.setEnabled(false);
		                saveDataFileButton.setEnabled(true);
		                
		                closeDataFileButton.setEnabled(true);
		                closeDataFileButton.setSelected(false);
		                stopDataFileButton.setEnabled(true);
		                stopDataFileButton.setSelected(true);
		                recordDataFileButton.setEnabled(true);
		                recordDataFileButton.setSelected(false);
		                break;
		            case RECORD:
		                newDataFileMenuItem.setEnabled(false);
		                openDataFileMenuItem.setEnabled(false);
		                closeDataFileMenuItem.setEnabled(true);
		                saveDataFileMenuItem.setEnabled(true);
		                saveAsDataFileMenuItem.setEnabled(true);
		                
		                newDataFileButton.setEnabled(false);
		                openDataFileButton.setEnabled(false);
		                saveDataFileButton.setEnabled(true);
		
		                closeDataFileButton.setEnabled(true);
		                closeDataFileButton.setSelected(false);
		                stopDataFileButton.setEnabled(true);
		                stopDataFileButton.setSelected(false);
		                recordDataFileButton.setEnabled(true);
		                recordDataFileButton.setSelected(true);  
		                break;
		            case RESTORE_COMPLETE:
		                newDataFileMenuItem.setEnabled(false);
		                openDataFileMenuItem.setEnabled(false);
		                closeDataFileMenuItem.setEnabled(true);
		                saveDataFileMenuItem.setEnabled(true);
		                saveAsDataFileMenuItem.setEnabled(true);
		                
		                newDataFileButton.setEnabled(false);
		                openDataFileButton.setEnabled(false);
		                saveDataFileButton.setEnabled(true);
		                
		                closeDataFileButton.setEnabled(true);
		                stopDataFileButton.setEnabled(true);
		                recordDataFileButton.setEnabled(true);
		                
		                closeDataFileButton.setEnabled(true);
		                closeDataFileButton.setSelected(false);
		                stopDataFileButton.setEnabled(true);
		                stopDataFileButton.setSelected(false);
		                recordDataFileButton.setEnabled(false);
		                recordDataFileButton.setSelected(false);
		                break;
		        }
		        dataMode = mode;
		    }
		});
    }

    private void startDemo() {
    	startStaticTest();
	    final double TEST_SPEED_KPH = 300;
	    final double TEST_HEADING = 0;	
	    
	    double distance = Vincenty.metersToDegrees(3000, TEST_HEADING, serialGPSComponent.getGPSInterface().getPosition().y);
	    
	    Point.Double p1 = new Point.Double(-83.23791 - (2*distance), 40.05074 + (4*distance));
	    Point.Double p2 = new Point.Double(-83.23791 - (4*distance), 40.05074 + (8*distance));
	    
	    Point.Double p3 = new Point.Double(-82.84790 + (2*distance), 40.02551 + (4*distance));
	    Point.Double p4 = new Point.Double(-82.84790 + (4*distance), 40.02551 + (8*distance));
	    
	    double alt = 5000;
	    
	    double d1 = Vincenty.getVincentyInverse(TEST_POINT, p1).distance;
	    double d2 = Vincenty.getVincentyInverse(TEST_POINT, p2).distance;
	    double d3 = Vincenty.getVincentyInverse(TEST_POINT, p3).distance;
	    double d4 = Vincenty.getVincentyInverse(TEST_POINT, p4).distance;

	    double da = Math.sqrt((d1*d1)+(alt*alt));
	    double db = Math.sqrt((d2*d2)+(alt*alt));
	    double dc = Math.sqrt((d3*d3)+(alt*alt));
	    double dd = Math.sqrt((d4*d4)+(alt*alt));
	    
	    double k1 = RFPath.getFreeSpacePathLoss(da, 100);
	    double k2 = RFPath.getFreeSpacePathLoss(db, 100);
        double k3 = RFPath.getFreeSpacePathLoss(dc, 100);
        double k4 = RFPath.getFreeSpacePathLoss(dd, 100);
        
        long td = networkTime.getBestTimeInMillis();
        processStaticMeasurement(new StaticMeasurement(logFileName, p1, td, k1, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 1));
        processStaticMeasurement(new StaticMeasurement(logFileName, p2, td, k2, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 1));
        
        td = networkTime.getBestTimeInMillis();
        processStaticMeasurement(new StaticMeasurement(logFileName, p3, td, k3, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 2));       
        processStaticMeasurement(new StaticMeasurement(logFileName, p4, td, k4, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 2)); 
        
        map.setCenterLonLat(TEST_POINT);
        processStaticMeasurements = true;
	}

    private int getNumberOfFlights(List<StaticMeasurement> sml) {
    	int lastUnit = 0;
    	for (StaticMeasurement sm : sml) {
    		lastUnit = Math.max(sm.getUnit(), lastUnit);
		}
    	return lastUnit;
    }

    private void compileStaticMeasurements() {
    	if (database.getStaticRecordList().isEmpty()) return;
    	
    	int numberOfFlights = getNumberOfFlights(database.getStaticRecordList());
    	double maxCatt = staticTestSettings.getMaxCatt();
    	double minCatt = 0;
    	
    	conicSectionListStartSize = conicSectionList.size();
    	
    	for (int z = 0; z < numberOfFlights; z++) {

    		List<StaticMeasurement> sl = new ArrayList<>();
    		
    		for (StaticMeasurement sm : database.getStaticRecordList()) {
    			if (sm.getUnit() == z+1) sl.add(sm);
    		}
    		
    		int slss = 0;
    		
    		if (staticListStartSize.size() > z) {
    			slss = staticListStartSize.get(z);
    		} else {
    			staticListStartSize.add(0);
    		}
    		
    		staticListStartSize.set(z, sl.size());
    		
    		int add = sl.size() - slss;
    		
    		if (!sl.isEmpty() && sl.size() > 1) {
				for (int i = sl.size() - 1; i > sl.size() - add; i--) {
		    		for (int n = i - 1; n >= 0; n--) {
		    			if (sl.get(n).getdBm() < sl.get(i).getdBm()) {
		    				ConicSection cone = new ConicSection(sl.get(n), sl.get(i), z);
		    				if (cone.getConicAngleToTarget() >= minCatt && cone.getConicAngleToTarget() <= maxCatt) {
		    					conicSectionList.add(cone);
		    					map.addArc(cone);
		    				}
		    			} else {
		    				ConicSection cone = new ConicSection(sl.get(i), sl.get(n), z);
		    				if (cone.getConicAngleToTarget() >= minCatt && cone.getConicAngleToTarget() <= maxCatt) {
		    					conicSectionList.add(cone);
		    					map.addArc(cone);
		    				}
		    			}
		    		}
		    	}
    		}
    	}
        doInterceptListUpdate(conicSectionList, conicSectionListStartSize);
        processStaticMeasurements = false;
    }

    private void doInterceptListUpdate(List<ConicSection> list, final int startSize) {
    	map.showTargetRing(false);
    	
    	intersectListUpdate = new IntersectList(list, startSize);

    	intersectListUpdate.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if ("INTERSECT_LIST_COMPLETE".equals(event.getPropertyName())) { 
					intersectListUpdatePropertyChangeListenerEvent(event);
				}
			}
        });
    	
    	intersectListUpdate.execute();
    }

    private Color sinadToColor(final double sinad) {
        return coverageTestSettings.getSinadColor(sinad);
    }

    private Color berToColor(final double ber) {
        return coverageTestSettings.getBerColor(ber);
    }

    private Color dBmToColor(final double dBm) {
        return coverageTestSettings.getdBmColor(dBm);
    }

    private void mapPanelLeftArrowKeyPressed(final KeyEvent event) {
        if (!cursorBearingSet) {
            if (cursorBearing > 0) {
                cursorBearing--;
            } else {
                cursorBearing = 359;
            }
            moveRdfBearing(cursorBearingIndex, cursorBearingPosition, cursorBearing,
                RDF_BEARING_LENGTH_IN_DEGREES, 8, Color.RED);
        }
    }

    private void mapPanelRightArrowKeyPressed(final KeyEvent event) {
        if (!cursorBearingSet) {
            if (cursorBearing < 359) {
                cursorBearing++;
            } else {
                cursorBearing = 0;
            }
            moveRdfBearing(cursorBearingIndex, cursorBearingPosition, cursorBearing, 
            	RDF_BEARING_LENGTH_IN_DEGREES, 8, Color.RED);
        }
    }
    
    void mapPanelLeftMouseButtonClicked(final MouseEvent event) {
        if (!serialGPSComponent.isGpsOnLine() && map.isShowGrid() && coverageTestActive && !learnMode) {
            periodTimerTimeOut = false;
            periodTimerHalt = false;
            periodTimer.start();
            processPositionInformation(PositionSource.MANUAL, map.getMouseCoordinates());
        }
        if (!coverageTestActive && learnMode && !learnModeHold) {
        	createBlankTestTile(map.getMouseCoordinates());
        }
    }

    private int getRdfQuality(final GPSInterface.RdfQuality rdfqual) {
    	switch (rdfqual) {
		    case RDF_QUAL_1: return 1;
	        case RDF_QUAL_2: return 2;
	        case RDF_QUAL_3: return 3;
	        case RDF_QUAL_4: return 4;
	        case RDF_QUAL_5: return 5;
	        case RDF_QUAL_6: return 6;
	        case RDF_QUAL_7: return 7;
	        case RDF_QUAL_8: return 8;
	        default: return 0;
    	}
	}

    private double getTargetRingSize(List<Point.Double> intersectList) {
    	if (intersectList == null) return 0;
    	double stdDev = pointStandardDeviation(intersectList);
		return (stdDev * 4e4) + (2e4 / intersectList.size());
    }
    
    private void intersectListUpdatePropertyChangeListenerEvent(final PropertyChangeEvent event) {
    	try {
			intersectList.addAll(intersectListUpdate.get());
			Point.Double meanIntersect = pointMean(intersectList);
			map.setTargetRing(meanIntersect, getTargetRingSize(intersectList), staticTestSettings.getTargetRingColor());
			map.showTargetRing(staticTestSettings.isShowTargetRing());
			for (Point.Double p : intersectList) {
				map.addArcIntersectPoint(p, staticTestSettings.getIntersectPointRadius(), 
						staticTestSettings.getIntersectPointColor());
			}
			map.showArcIntersectPoints(staticTestSettings.isShowIntersectPoints());
		} catch(InterruptedException ex) {
			reportException(ex);
		}
    	catch(ExecutionException ex) {
    		reportException(ex);
		}
    }

    private void registerShutdownHook() {
        Thread shutdownThread = new Thread() {
            @Override
        	public void run() {
                synchronized (this) {
                    if (!readyToExit) {
                        try {
                            wait(5000);
                        } catch (InterruptedException ex) {
                        	ex.printStackTrace();
                        } 
                        if (!readyToExit) {
                            System.err.println("Ungraceful Shutdown : " + shutdownMask); 
                            log.log(Level.SEVERE, "Ungraceful Shutdown = " + shutdownMask, new Exception()); 
                        } 
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private synchronized void signalReadyToExit(final long mask) {
    	shutdownMask = shutdownMask | mask;
        if (shutdownMask == ALL_THREADS_RELEASED) {
	    	readyToExit = true;
	        notify();
        }
    }
    
    private void clearAllPreferences() {
    	try {
			systemPref.clear();
		} catch (BackingStoreException ex) {
			reportException(ex);
		}
    }
    
    void updateZoomParameters(int zoom) {
    	
    }
    
    void disableZoomIn(boolean disabled) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	zoomInButton.setEnabled(!disabled);
            }
    	});
    }
    
    void disableZoomOut(boolean disabled) {
    	invokeLaterInDispatchThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
            	zoomOutButton.setEnabled(!disabled);
            }
    	});
    }

    private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
    
    public static void invokeAndWaitInDispatchThreadIfNeeded(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
}
