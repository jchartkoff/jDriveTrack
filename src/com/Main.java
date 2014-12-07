package com;

import gov.nasa.worldwind.Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.GPSInterface.FixQuality;

import jssc.SerialPortException;

public class Main extends JFrame implements MouseListener {
	private static final long serialVersionUID = 7230746210127860L;
	
	public static final Logger log = Logger.getLogger(Main.class.getName());
	
    private static final Dimension PREFERRED_MAP_SIZE = new Dimension(804,604);
    private static final double RDF_BEARING_LENGTH_IN_DEGREES = 0.500;
    private static final Point.Double DEFAULT_STARTUP_COORDINATES = new Point.Double(-86.0,35.0);
    private static final int DEFAULT_STARTUP_ZOOM = 6;
    private static final double DEFAULT_STARTUP_SCALE = 1.0;
    
    private enum LogMode {OPEN, CLOSED, STOP, REPLAY, RECORD, PAUSE, REPLAY_COMPLETE}
    private enum PositionSource {MANUAL, GPS, LOG}
    
    private Triangulate triangulate = null;
    private NetworkTime networkTime = null;
    private NetworkInterfaceCheck nicCheck;
    private boolean isZooming = false;
    private int conicSectionListStartSize = 0; 
    private PropertyChangeListener mapPropertyChangeListener;
    private Preferences userPref;
    private BarMeter barMeter;
    private MapSettings mapSettings;
    private StaticTestSettings staticTestSettings;
    private SerialGPSComponent serialGPSComponent;
    private RadioComponent radioComponent;
    private CoverageTestSettings coverageTestSettings;
    private AprsComponent aprsComponent;
    private JToolBar toolBar;
    private SignalMeterArray signalMeterArray;
    private CompassRose gpsCompassRose;
    private JPanel mapPanel;
    private JLabel recordCountLabel;
    private JLabel recordPointerLabel;
    private JLabel logFileNameLabel;
    private JLabel cursorLatitude;
    private JLabel cursorLongitude;
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
    private JLabel signalQuality0;
    private JLabel signalQuality1;
    private JLabel signalQuality2;
    private JLabel signalQuality3;
    private JLabel signalQuality4;
    private JLabel signalQuality5;
    private JLabel signalQuality6;
    private JLabel signalQuality7;
    private JLabel signalQuality8;
    private JLabel signalQuality9;
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
    private JLabel speedMadeGoodMPH;
    private JLabel cursorUTM;
    private JLabel currentUTM;
    private JLabel cursorGridSquare;
    private JLabel currentGridSquare;
    private JLabel measurementPeriod;
    private JLabel measurementsThisGrid;
    private JLabel radioTxDataWord;
    private JLabel radioRxDataWord;
    private JLabel tileCount;
    private JLabel averageRssiInCurrentTile;
    private JLabel averageBerInCurrentTile;
    private JLabel averageSinadInCurrentTile;
    private JPanel gpsInfoPanel;
    private NumberFormat recordFormat;
    private NumberFormat latFormat;
    private NumberFormat lonFormat;
    private NumberFormat speedFormat;
    private NumberFormat dBmFormat;
    private NumberFormat BERFormat;
    private NumberFormat markerFormat;
    private NumberFormat measurementPeriodFormat;
    private NumberFormat measurementsThisGridFormat;
    private NumberFormat gridSquareFormat;
    private NumberFormat SINADFormat;
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
    private JButton newLogFileButton;
    private JButton openLogFileButton;
    private JButton closeLogFileButton;
    private JButton saveLogFileButton;
    private JButton stopLogFileButton;
    private JButton replayLogFileButton;
    private JButton recordLogFileButton;
    private JButton bofLogFileButton;
    private JButton eofLogFileButton;
    private JButton gpsButton;
    private JButton radioButton;
    private JButton sinadButton;
    private JButton aprsButton;
    private JButton coverageTestButton;
    private JButton staticLocationAnalysisButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JMenuItem aprsComponentMenuItem;
    private JMenuItem gpsComponentMenuItem;
    private JMenuItem receiverComponentMenuItem;
    private JMenuItem coverageTestSettingsMenuItem;
    private JMenuItem mapSettingsMenuItem;
    private JMenuItem mapConfigurationMenuItem;
    private JMenuItem mapClearMenuItem;
    private JMenuItem newLogFileMenuItem;
    private JMenuItem openLogFileMenuItem;
    private JMenuItem closeLogFileMenuItem;
    private JMenuItem saveLogFileMenuItem;
    private JMenuItem saveAsLogFileMenuItem;
    private JMenuItem printPreviewMenuItem;
    private JMenuItem printMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem staticSignalLocationSettingsMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenu fileMenu;
    private JMenu gpsMenu;
    private JMenu receiverMenu;
    private JMenu systemMenu;
    private JMenu mapMenu;
    private JMenu aprsMenu;
    private JMenu staticSignalLocationMenu;
    private JMenu helpMenu;
    private LogMode logMode;
    private int recordPointer;
    private int recordCount;
    private String logFileName;
    private String lastLogFileDirectory;
    private int tilesTraversed;
    private int dotsPerTile;
    private Point.Double currentLonLat = null;
    private Point gridLastMeasured = null;
    private Point currentGrid = null;
    private Point.Double tileDimensionInMeters = null;
    private Point.Double tileSizeArcSeconds = null;
    private Point.Double centerOfGrid = null;
    private int indexPointer = -1;
    private int totalGridsCompleted = 0;
    private Map map;
    private Point.Double startupLonLat = null;
    private double startupScale;
    private int startupZoom;
    private boolean showLines = true;
    private boolean showRings = true;
    private boolean showDots = true;
    private Sinad sinad;
    private int[] sinadArray = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private AccessDatabase accessDatabase;
    private DriveTestData driveTestData;
    private int markerCounter = 0;
    private List<AprsIcon> iconList = new ArrayList<AprsIcon>(1024);
    private List<TileIndex> tileIndex = new ArrayList<TileIndex>(1024);
    private List<Bearing> bearingList = new ArrayList<Bearing>(1024);
    private List<ConicSection> conicSectionList = new ArrayList<ConicSection>();
    private List<StaticMeasurement> staticList = new ArrayList<StaticMeasurement>(1024);
    private List<Integer> staticListStartSize = new ArrayList<Integer>();
    private Point.Double lastDotLocation = new Point.Double();
	private List<Point.Double> intersectList = new ArrayList<Point.Double>();
	private IntersectList intersectListUpdate = null;
    private int logFileRSSI;
    private int logFileSINAD;
    private double logFileBER;
    private boolean analogTest = true;
    private PositionSource lastInputSource;
    private boolean periodTimerTimeOut = false;
    private boolean periodTimerHalt = false;
    private boolean isRecord = false;
    private double averageRssiInTile;
    private double averageBerInTile;
    private double averageSinadInTile;
    private FileHandler fh;
    private double cursorBearing = 0;
    private Point.Double cursorBearingPosition = null;
    private int cursorBearingIndex;
    private boolean cursorBearingSet = false;
    private int startMapArg = -1;
    private boolean startTestMode = false;
    private String averageRssiInCurrentTileText;
    private String averageBerInCurrentTileText;
    private String averageSinadInCurrentTileText;
    private boolean coverageTestModeActive = false;
    private boolean staticLocationAnalysisModeActive = false;
    private boolean gpsModeActive = false;
    private boolean sinadModeActive = false;
    private boolean processStaticMeasurements = false;
    private ProgressMonitor logReplayProgressMonitor;
    private PropertyChangeListener logDatabaseListener;
    private MouseMotionListener mapMouseMotionListener;
    private MouseListener mapMouseListener;
    private KeyListener mapKeyListener;
    private long preGpsTimeInMillis = 0;
    private boolean mapDragged = false;
    private boolean preGpsActive = false;
    private boolean preAprsActive = false;
    private boolean preRadioActive = false;
    
    public Main(String args[]) {

		try {
			Options options = new Options();
	    	options.addOption(new Option("m", true, "map style"));
	    	options.addOption(new Option("d", "demonstration mode"));
	    	CommandLineParser parser = new BasicParser(); 
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("m")) startMapArg = Integer.parseInt(cmd.getOptionValue("m"));
			if (cmd.hasOption("d")) startTestMode = true;
		} catch (ParseException ex) {
			reportException(ex);
		}

        try {
        	String eventLogFileName = System.getProperty("user.dir") + File.separator +"drivetrack" + File.separator + "eventLog" + File.separator + "event.log";
        	Path path = Paths.get(eventLogFileName);
    		File directory = new File(path.getParent().toString());
    		if (!directory.exists()) new File(path.getParent().toString()).mkdirs();
        	fh = new FileHandler(eventLogFileName, 4096, 64, true);
            log.addHandler(fh);
            log.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch(SecurityException ex) {
        	reportException(ex);
        } catch(IOException ex) {
        	reportException(ex);
        } finally {
        	fh.flush();
        	fh.close();
        }
        
        getSettingsFromRegistry();
        initializeLookAndFeel();
        initializeComponents();                
        createGraphicalUserInterface();
        initializeListeners();
        configureComponents();
        displayGraphicalUserInterface();

        if (startTestMode) startTest();
    }

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		        new Main(args);
		    }
		});
	}

    private void getSettingsFromRegistry() {
    	userPref = Preferences.userRoot();
        lastLogFileDirectory = userPref.get("LastLogFileDirectory", null);
        startupLonLat = new Point.Double(userPref.getDouble("MapLongitude", DEFAULT_STARTUP_COORDINATES.x),
        		userPref.getDouble("MapLatitude", DEFAULT_STARTUP_COORDINATES.y));
        if (startupLonLat.y < -90 || startupLonLat.y > 90 || startupLonLat.x < -180 || startupLonLat.x > 180) 
        	startupLonLat = DEFAULT_STARTUP_COORDINATES;
        startupScale = userPref.getDouble("MapScale", DEFAULT_STARTUP_SCALE); 
        startupZoom = userPref.getInt("MapZoom", DEFAULT_STARTUP_ZOOM); 
    }
    
    private void initializeComponents() {
        recordCount = 0;
        recordPointer = 0;
        
        networkTime = new NetworkTime();
        
        mapPanel = new JPanel();
        toolBar = new JToolBar();
        newLogFileButton = new JButton();
        openLogFileButton = new JButton();
        closeLogFileButton = new JButton();
        saveLogFileButton = new JButton();
        stopLogFileButton = new JButton();
        replayLogFileButton = new JButton();
        recordLogFileButton = new JButton();
        bofLogFileButton = new JButton();
        eofLogFileButton = new JButton();
        gpsButton = new JButton();
        radioButton = new JButton();
        sinadButton = new JButton();
        aprsButton = new JButton();
        coverageTestButton = new JButton();
        staticLocationAnalysisButton = new JButton();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();

        toolBar.add(newLogFileButton);
        toolBar.add(openLogFileButton);
        toolBar.add(saveLogFileButton);
        toolBar.add(closeLogFileButton);
        toolBar.addSeparator();
        toolBar.add(bofLogFileButton);
        toolBar.add(replayLogFileButton);
        toolBar.add(stopLogFileButton);
        toolBar.add(recordLogFileButton);
        toolBar.add(eofLogFileButton);
        toolBar.addSeparator();
        toolBar.add(gpsButton);
        toolBar.add(radioButton);
        toolBar.add(sinadButton);
        toolBar.add(aprsButton);
        toolBar.addSeparator();
        toolBar.add(coverageTestButton);
        toolBar.addSeparator();
        toolBar.add(staticLocationAnalysisButton);
        toolBar.addSeparator();
        toolBar.add(zoomInButton);
        toolBar.add(zoomOutButton);

        JMenuBar menuBar = new JMenuBar();

        setJMenuBar(menuBar);

        fileMenu = new JMenu(" File ");
        gpsMenu = new JMenu(" GPS ");
        receiverMenu = new JMenu(" Receiver ");
        systemMenu = new JMenu(" Coverage Test ");
        mapMenu = new JMenu(" Map ");
        aprsMenu = new JMenu(" APRS ");
        staticSignalLocationMenu = new JMenu(" Static Signal Location ");
        helpMenu = new JMenu(" Help ");

        menuBar.add(fileMenu);
        menuBar.add(gpsMenu);
        menuBar.add(receiverMenu);
        menuBar.add(systemMenu);
        menuBar.add(mapMenu);
        menuBar.add(aprsMenu);
        menuBar.add(staticSignalLocationMenu);
        menuBar.add(helpMenu);

        aprsComponentMenuItem = new JMenuItem(" APRS Settings ");
        gpsComponentMenuItem = new JMenuItem(" GPS Settings ");
        receiverComponentMenuItem = new JMenuItem(" Receiver Settings ");
        coverageTestSettingsMenuItem = new JMenuItem(" Coverage Test Settings ");
        mapSettingsMenuItem = new JMenuItem(" Map Settings ");
        mapConfigurationMenuItem = new JMenuItem(" Configure Map ");
        mapClearMenuItem = new JMenuItem(" Clear Map ");
        newLogFileMenuItem = new JMenuItem(" New Database File ");
        openLogFileMenuItem = new JMenuItem(" Open Database File ");
        closeLogFileMenuItem = new JMenuItem(" Close Database File ");
        saveLogFileMenuItem = new JMenuItem(" Save Database File ");
        saveAsLogFileMenuItem = new JMenuItem(" Save Database File As ");
        printPreviewMenuItem = new JMenuItem(" Print Preview ");
        printMenuItem = new JMenuItem(" Print... ");
        exitMenuItem = new JMenuItem(" Exit ");
        staticSignalLocationSettingsMenuItem = new JMenuItem(" Static Signal Location Settings ");
        aboutMenuItem = new JMenuItem(" About DriveTrack ");

        fileMenu.add(newLogFileMenuItem);
        fileMenu.add(openLogFileMenuItem);
        fileMenu.add(closeLogFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveLogFileMenuItem);
        fileMenu.add(saveAsLogFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(printPreviewMenuItem);
        fileMenu.add(printMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        gpsMenu.add(gpsComponentMenuItem);

        receiverMenu.add(receiverComponentMenuItem);

        systemMenu.add(coverageTestSettingsMenuItem);

        mapMenu.add(mapSettingsMenuItem);
        mapMenu.add(mapConfigurationMenuItem);
        mapMenu.addSeparator();
        mapMenu.add(mapClearMenuItem);

        aprsMenu.add(aprsComponentMenuItem);

        staticSignalLocationMenu.add(staticSignalLocationSettingsMenuItem);

        helpMenu.add(aboutMenuItem);

        gpsInfoPanel = new JPanel();
        longitude = new JLabel();
        latitude = new JLabel();
        utcLabel = new JLabel();
        speedMadeGoodMPH = new JLabel();
        cursorLatitude = new JLabel();
        cursorLongitude = new JLabel();
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
        signalQuality0 = new JLabel();
        signalQuality1 = new JLabel();
        signalQuality2 = new JLabel();
        signalQuality3 = new JLabel();
        signalQuality4 = new JLabel();
        signalQuality5 = new JLabel();
        signalQuality6 = new JLabel();
        signalQuality7 = new JLabel();
        signalQuality8 = new JLabel();
        signalQuality9 = new JLabel();
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
        recordPointerLabel = new JLabel();
        logFileNameLabel = new JLabel();
        currentUTM = new JLabel();
        cursorUTM = new JLabel();
        currentGridSquare = new JLabel();
        cursorGridSquare = new JLabel();
        measurementPeriod = new JLabel();
        measurementsThisGrid = new JLabel();
        radioTxDataWord = new JLabel();
        radioRxDataWord = new JLabel();
        tileCount = new JLabel();
        averageRssiInCurrentTile = new JLabel();
        averageBerInCurrentTile = new JLabel();
        averageSinadInCurrentTile = new JLabel();
        recordFormat = new DecimalFormat("00000000");
        latFormat = new DecimalFormat("#0.000000000");
        lonFormat = new DecimalFormat("##0.000000000");
        speedFormat = new DecimalFormat("###0.0");
        dBmFormat = new DecimalFormat("##00");
        BERFormat = new DecimalFormat("#0.00");
        markerFormat = new DecimalFormat("00000");
        measurementPeriodFormat = new DecimalFormat("##00");
        measurementsThisGridFormat = new DecimalFormat("##0");
        gridSquareFormat = new DecimalFormat("0000");
        SINADFormat = new DecimalFormat("00");
        serialGPSComponent = new SerialGPSComponent();
        aprsComponent = new AprsComponent();
        radioComponent = new RadioComponent();
        coverageTestSettings = new CoverageTestSettings();
        mapSettings = new MapSettings();
        staticTestSettings = new StaticTestSettings();
        signalMeterArray = new SignalMeterArray();
        gpsCompassRose = new CompassRose();
        barMeter = new BarMeter();
        mapPanel.setBorder(BorderFactory.createEtchedBorder());
        mapPanel.setLayout(new BorderLayout());
        mapSettings.getMapType();
        
        if (startMapArg != -1) mapSettings.setMapType(startMapArg);
        
        switch (mapSettings.getMapType()) {
            case 0:
                map = new ImageMap(userPref.getInt("SelectedMapIndex", -1));
                map.setScale(startupScale);
                break;
            case 1:
                map = new WorldWindMap(startupLonLat, startupZoom, PREFERRED_MAP_SIZE);
                break;
            case 2:
                map = new OpenStreetMapPanel(startupLonLat, startupZoom, PREFERRED_MAP_SIZE);
                break;
        }
        
        mapPanel.add((Component) map, BorderLayout.CENTER);
        
        nicCheck = new NetworkInterfaceCheck();
    }

    private void initializeListeners() {
    	
        mapPropertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (Map.MAP_IMAGE_COMPLETE.equals(event.getPropertyName())) {
					mapImageCompletePropertyChangeListener(event);
				}
				if (Map.NO_OP.equals(event.getPropertyName())) {
					mapImageNoOperationPropertyChangeListener(event);
				}
				if (Map.MAP_RENDERED.equals(event.getPropertyName())) {
					mapRenderedPropertyChangeListener(event);
				}
				if (Map.ZOOM_COMPLETE.equals(event.getPropertyName())) {
					mapZoomCompletePropertyChangeListener(event);
				}
			}
        };
        
        mapMouseMotionListener = new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent event) {
				mapPanelDragged(event);	
			}
			@Override
			public void mouseMoved(MouseEvent event) {
				mapPanelMouseMoved(event);	
			}
        };
        
        mapMouseListener = new MouseListener() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
        			mapPanelLeftMouseButtonClicked(event);
        		}
        		if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
        			mapPanelRightMouseButtonClicked(event);
        		}
        	}
			@Override
			public void mousePressed(MouseEvent event) {			
			
			}
			@Override
			public void mouseReleased(MouseEvent event) {			
			
			}
			@Override
			public void mouseEntered(MouseEvent event) {			
				
			}
			@Override
			public void mouseExited(MouseEvent event) {
				
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
    	
    	networkTime.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
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
        	}
        });
    	
    	networkTime.startAutomaticNetworkTimeUpdates();
    	networkTime.startClock();
    	
        nicCheck.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if (NetworkInterfaceCheck.ONLINE.equals(event.getPropertyName())) {
	            	networkInterfaceIsAvailable(event);
	            }
        	}
        });
        
        nicCheck.adviseOnNetworkInterfaceAvailability();

    	logDatabaseListener = new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
        		if (AccessDatabase.OPEN.equals(event.getPropertyName())) {
        			databaseOpenChangeListenerEvent(event);
        		}
        		if (AccessDatabase.CLOSED.equals(event.getPropertyName())) {
        			databaseClosedChangeListenerEvent(event);
        		}
        		if (AccessDatabase.APPENDED.equals(event.getPropertyName())) {
        			databaseAppendedChangeListenerEvent(event);
        		}
        		if (AccessDatabase.DRIVE_TEST_DATA_READY.equals(event.getPropertyName())) {
        			databaseDriveTestDataReadyChangeListenerEvent(event);
        		}
        		if (AccessDatabase.STATIC_MEASUREMENT_DATA_READY.equals(event.getPropertyName())) {
        			databaseStaticMeasurementDataReadyChangeListenerEvent(event);
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

        zoomInButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                zoomInButtonMousePressed(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                zoomInButtonMouseReleased(event);
            }
        });
        
        aprsComponent.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
        		if (SerialInterface.CTS.equals(event.getPropertyName())) {
        			aprsCTSHoldingChangeListenerEvent(event);
        		}
        		if (SerialInterface.DSR.equals(event.getPropertyName())) {
        			aprsDSRHoldingChangeListenerEvent(event);
        		}
        		if (SerialInterface.RLSD.equals(event.getPropertyName())) {
        			aprsCDHoldingChangeListenerEvent(event);
        		}
        		if (SerialInterface.RX_DATA.equals(event.getPropertyName())) {
        			aprsRxDataChangeListenerEvent(event);
        		}
        		if (SerialInterface.TX_DATA.equals(event.getPropertyName())) {
        			aprsTxDataChangeListenerEvent(event);
        		}
        		if (SerialInterface.ERROR.equals(event.getPropertyName())) {
        			serialErrorChangeListenerEvent(event, "APRS Modem");
        		}
        		if (APRSInterface.WAYPOINT.equals(event.getPropertyName())) {
        			aprsWaypointListenerEvent(event);
        		}
        		if (SerialInterface.INVALID_COM_PORT.equals(event.getPropertyName())) {
        			invalidComPortChangeListenerEvent(event, "APRS modem");
        		}
        	}
        });
        
        radioComponent.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
        		if (SerialInterface.CTS.equals(event.getPropertyName())) {
        			radioCTSHoldingChangeListenerEvent(event);
        		}
        		if (SerialInterface.DSR.equals(event.getPropertyName())) {
        			radioDSRHoldingChangeListenerEvent(event);
        		}
        		if (SerialInterface.RLSD.equals(event.getPropertyName())) {
        			radioCDHoldingChangeListenerEvent(event);
        		}
        		if (RadioInterface.RSSI.equals(event.getPropertyName())) {
        			radioRSSIChangeListenerEvent(event);
        		}
        		if (RadioInterface.BUSY.equals(event.getPropertyName())) {
        			radioBusyChangeListenerEvent(event);
        		}
        		if (SerialInterface.CTS.equals(event.getPropertyName())) {
        			radioBERChangeListenerEvent(event);
        		}
        		if (SerialInterface.RX_DATA.equals(event.getPropertyName())) {
        			radioRxDataChangeListenerEvent(event);
        		}
        		if (SerialInterface.TX_DATA.equals(event.getPropertyName())) {
        			radioTxDataChangeListenerEvent(event);
        		}
        		if (SerialInterface.ERROR.equals(event.getPropertyName())) {
        			serialErrorChangeListenerEvent(event, "Radio");
        		}
        		if (SerialInterface.ONLINE.equals(event.getPropertyName())) {
        			radioOnlineChangeListenerEvent(event);
        		}
        		if (SerialInterface.INVALID_COM_PORT.equals(event.getPropertyName())) {
        			invalidComPortChangeListenerEvent(event, "radio");
        		}
        	}
        });	
        
        serialGPSComponent.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
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
        		if (SerialInterface.RX_DATA.equals(event.getPropertyName())) {
        			gpsRxDataPropertyChangeListenerEvent(event);
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
        		if (SerialInterface.SERIAL_PORT_CONFIGURATION_ERROR.equals(event.getPropertyName())) {
        			serialErrorChangeListenerEvent(event, "Serial Port Configuration");
        		}
        		if (SerialInterface.ERROR.equals(event.getPropertyName())) {
        			serialErrorChangeListenerEvent(event, "GPS");
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
        		if (GPSInterface.COURSE_MADE_GOOD_TRUE.equals(event.getPropertyName())) {
        			gpsCourseMadeGoodTruePropertyChangeListenerEvent(event);
        		}
        		if (GPSInterface.COURSE_MADE_GOOD_MAGNETIC.equals(event.getPropertyName())) {
        			
        		}
        		if (GPSInterface.NMEA_DATA.equals(event.getPropertyName())) {
        			gpsReceivedDataPropertyChangeListenerEvent(event);
        		}
        		if (SerialInterface.INVALID_COM_PORT.equals(event.getPropertyName())) {
        			invalidComPortChangeListenerEvent(event, "GPS receiver");
        		}
        		if (GPSInterface.CRC_ERROR.equals(event.getPropertyName())) {
        			gpsCRCErrorEvent(event);
        		}
        	}
        });
        
        map.addMouseMotionListener(mapMouseMotionListener);
        
        map.addMouseListener(mapMouseListener);
        
        map.addPropertyChangeListener(mapPropertyChangeListener);
        
        map.addKeyListener(mapKeyListener);

        zoomOutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                zoomOutButtonMousePressed(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                zoomOutButtonMouseReleased(event);
            }
        });

        gpsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                gpsButtonMouseClicked(event);
            }
        });

        radioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                radioButtonMouseClicked(event);
            }
        });

        sinadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                sinadButtonMouseClicked(event);
            }
        });

        aprsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                aprsButtonMouseClicked(event);
            }
        });

        coverageTestButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                coverageTestButtonMouseClicked(event);
            }
        });

        staticLocationAnalysisButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                staticLocationAnalysisButtonMouseClicked(event);
            }
        });
        
        newLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                newLogFileButtonMouseClicked(event);
            }
        });

        openLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                openLogFileButtonMouseClicked(event);
            }
        });

        saveLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                saveLogFileButtonMouseClicked(event);
            }
        });

        closeLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                closeLogFileButtonMouseClicked(event);
            }
        });

        stopLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                stopLogFileButtonMouseClicked(event);
            }
        });

        replayLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                replayLogFileButtonMouseClicked(event);
            }
        });

        recordLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                recordLogFileButtonMouseClicked(event);
            }
        });

        bofLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                bofLogFileButtonMouseClicked(event);
            }
        });

        eofLogFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                eofLogFileButtonMouseClicked(event);
            }
        });

        newLogFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                newLogFileMenuItemActionListenerEvent(event);
            }
        });

        openLogFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openLogFileMenuItemActionListenerEvent(event);
            }
        });

        closeLogFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                closeLogFileMenuItemActionListenerEvent(event);
            }
        });

        saveLogFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveLogFileMenuItemActionListenerEvent(event);
            }
        });

        saveAsLogFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveAsLogFileMenuItemActionListenerEvent(event);
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

        mapConfigurationMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mapConfigurationMenuActionListenerEvent(event);
            }
        });
        
        staticSignalLocationSettingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                staticSignalLocationSettingsMenuActionListenerEvent(event);
            }
        });
        
        mapClearMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mapClearMenuActionListenerEvent(event);
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

        staticTestSettings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				staticTestSettingsPropertyChangeEvent(event);
			}
        });
        
        coverageTestSettings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				coverageTestSettingsPropertyChangeEvent(event);
			}
        });

        aprsComponent.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				aprsComponentChangeListenerEvent(event);
			}
        });

        mapSettings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if ("NEW_MAP_TYPE".equals(event.getPropertyName())) {
					mapTypeChangedChangeListenerEvent(event);
				}
				if ("PROPERTY_CHANGED".equals(event.getPropertyName())) {
					mapPropertyChangedChangeListenerEvent(event);
				}
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
    
    private void timeStrataChangeEvent(PropertyChangeEvent event) {
    	setUTCLabelColors((int) event.getNewValue());
    }
    
    private void networkClockUpdate(PropertyChangeEvent event) {
    	utcLabel.setText("    " + event.getNewValue() + " Z");
    }

    private void networkClockFailure(PropertyChangeEvent event) {
    	
    }
    
    private void networkTimeIsAvailable(PropertyChangeEvent event) {

    }
    
    private void setUTCLabelColors(int timeStrata) {
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
    
    private void networkInterfaceIsAvailable(PropertyChangeEvent event) {
    	networkTime.requestNetworkTime();
    }

	private void rdfHeadingTruePropertyChangeListenerEvent(PropertyChangeEvent event) {
        cursorBearingPosition = map.getMouseCoordinates();
        cursorBearing = (double) event.getNewValue();
        cursorBearingIndex = addRdfBearing(cursorBearingPosition, cursorBearing,
        	RDF_BEARING_LENGTH_IN_DEGREES, serialGPSComponent.gpsInterface.getRdfQuality(), Color.RED);
        map.addRing(cursorBearingPosition, 6, Color.RED);
        processBearingInformation();
    }

    private void rdfHeadingRelativePropertyChangeListenerEvent(PropertyChangeEvent event) {
        
    }

    private void processBearingInformation() {
    	triangulate = new Triangulate(bearingList);
		triangulate.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (StateValue.DONE.toString().equals(event.getNewValue())) {
		        	triangulationComplete(event);
		        }
			}
		});
		triangulate.execute();
    }

    private void triangulationComplete(PropertyChangeEvent event) {
    	double ringSize = Math.max(pointStandardDeviation(triangulate.getIntersectList()) * 3000, 20);
		map.setTargetRing(triangulate.getIntersectPoint(), (int) ringSize, Color.GREEN);
		map.showTargetRing(true);
    }
    
    private int addRdfBearing(Point.Double p1, double bearing, double length, GPSInterface.RdfQuality quality, Color color) {
        int index = bearingList.size();
        bearingList.add(index, new Bearing(index, p1, bearing, length, getRdfQuality(quality), color));
        
        Point.Double p2 = new Point.Double((Math.sin(bearing * Math.PI / 180) * length) + p1.x, 
        		(Math.cos(bearing * Math.PI / 180) * length) + p1.y);

        map.addLine(p1, p2, color);
        
        return index;
    }

    private void moveRdfBearing(int index, Point.Double p1, double bearing, double length, int quality, Color color) {
    	map.removeLine(index);
        bearingList.remove(index);
        
        bearingList.add(index, new Bearing(index, p1, bearing, length, quality, color));
        
        Point.Double p2 = new Point.Double((Math.sin(bearing * Math.PI / 180) * length) + p1.x, 
        		(Math.cos(bearing * Math.PI / 180) * length) + p1.y);

        map.addLine(p1, p2, color);
    }

    private void aboutMenuActionListenerEvent(ActionEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                        "DriveTrack version 7.3.1024" + System.lineSeparator() + "(c) Copyright John R. Chartkoff, 2014.  All rights reserved.",
                        "About DriveTrack", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void serialErrorChangeListenerEvent(final PropertyChangeEvent event, final String device) {
        if (event.getNewValue() == null) return;
        long eventValue = Long.parseLong((String) event.getNewValue());
        final String eventMessage = serialPortErrorMessage((int) eventValue);
        log.log(Level.WARNING, device + " SerialPortError", eventMessage);
        if (eventValue != 0L) { 
	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
	                    eventMessage, device + " Serial Port Error", JOptionPane.ERROR_MESSAGE);
	            }
	        });
        }
    }

    private void gpsCRCErrorEvent(final PropertyChangeEvent event) {
    	if (event.getNewValue() == null) return;
        log.log(Level.INFO, "GPS_CRC_Error", event.getNewValue());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                    event.getNewValue(), "GPS CRC Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private String serialPortErrorMessage(int eventMask) {
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
    
    private void mapClearMenuActionListenerEvent(ActionEvent event) {
        map.deleteAllDots();
        map.deleteAllIcons();
        map.deleteAllLines();
        map.deleteAllQuads();
        map.deleteAllRings();
        map.deleteAllArcs();
        map.deleteAllArcIntersectPoints();
    }

    private void aprsWaypointListenerEvent(PropertyChangeEvent event) {
        boolean found = false;
        for (AprsIcon ai : iconList) {
            if (aprsComponent.aprsInterface.getAprsIdentifier().contains(ai.getIdentifier())) {
                map.moveIcon(ai.getIndex(), aprsComponent.aprsInterface.getAprsPosition());
                found = true;
            }
        }
        if (!found) {
            int nextNewIcon = map.numberOfIcons();
            String newSsidPath = System.getProperty("user.dir") + File.separator + "maps" + File.separator + Utility.getIconPathNameFromSSID(Utility
                    .parseSSID(aprsComponent.aprsInterface.getAprsIdentifier()));
            map.insertIcon(nextNewIcon, aprsComponent.aprsInterface.getAprsPosition(), 
            		newSsidPath, aprsComponent.aprsInterface.getAprsIdentifier());
            iconList.add(new AprsIcon(iconList.size(), aprsComponent.aprsInterface.getAprsPosition(), 
            		aprsComponent.aprsInterface.getAprsIdentifier()));
        }
        aprsGPWPLSentence.setText(" " + aprsComponent.aprsInterface.getGPWPLMessageString());
        aprsLatitude.setText(latFormat.format(aprsComponent.aprsInterface.getAprsPosition().y));
        aprsLongitude.setText(lonFormat.format(aprsComponent.aprsInterface.getAprsPosition().x));
        aprsCallSign.setText(Utility.parseCallSign(aprsComponent.aprsInterface.getAprsIdentifier())
                + " - " + Utility.parseSSID(aprsComponent.aprsInterface.getAprsIdentifier()));
        aprsSSID.setText(Utility.parseSSID(aprsComponent.aprsInterface.getAprsIdentifier()));
    }

    private void initializeLookAndFeel() {
    	System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "NASA World Wind");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (Configuration.isWindowsOS()) {
            System.setProperty("sun.awt.noerasebackground", "true");
	        try {
	            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
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

    private void configureComponents() {
        toolBar.setRollover(false);
		setResizable(false);
		
		new IconRetriever(this, System.getProperty("user.dir") + File.separator + "images" + File.separator + "route_icon.jpg");
		
		barMeter.setColor(Color.BLUE);
		barMeter.setBorder(BorderFactory.createEtchedBorder());
		barMeter.setToolTipText("Relative Signal Strength");

		signalMeterArray.setColorMeter0(Color.BLUE);
		signalMeterArray.setColorMeter1(Color.BLUE);
		signalMeterArray.setColorMeter2(Color.BLUE);
		signalMeterArray.setColorMeter3(Color.BLUE);
		signalMeterArray.setColorMeter4(Color.BLUE);
		signalMeterArray.setColorMeter5(Color.BLUE);
		signalMeterArray.setColorMeter6(Color.BLUE);
		signalMeterArray.setColorMeter7(Color.BLUE);
		signalMeterArray.setColorMeter8(Color.BLUE);
		signalMeterArray.setColorMeter9(Color.BLUE);

		new IconRetriever(newLogFileMenuItem, System.getProperty("user.dir") + File.separator + "images" + File.separator + "NewDocumentHS.png");
		
		new IconRetriever(zoomOutButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "112_Minus_Green_16x16_72.png");
		zoomOutButton.setFocusable(false);
		zoomOutButton.setMultiClickThreshhold(50L);
		zoomOutButton.setBorder(BorderFactory.createEtchedBorder());
		zoomOutButton.setToolTipText("Zoom Out");
		zoomOutButton.setEnabled(true);
		
		new IconRetriever(zoomInButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "112_Plus_Green_16x16_72.png");
		zoomInButton.setFocusable(false);
		zoomInButton.setMultiClickThreshhold(50L);
		zoomInButton.setBorder(BorderFactory.createEtchedBorder());
		zoomInButton.setToolTipText("Zoom In");
		zoomInButton.setEnabled(true);
		
		new IconRetriever(gpsButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "Web.png");
		gpsButton.setFocusable(false);
		gpsButton.setMultiClickThreshhold(50L);
		gpsButton.setBorder(BorderFactory.createEtchedBorder());
		gpsButton.setToolTipText("Start GPS");

		new IconRetriever(radioButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "Filter2HS.png");
		radioButton.setFocusable(false);
		radioButton.setMultiClickThreshhold(50L);
		radioButton.setBorder(BorderFactory.createEtchedBorder());
		radioButton.setToolTipText("Start Radio");

		new IconRetriever(sinadButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "greenStateIcon.png");
		sinadButton.setFocusable(false);
		sinadButton.setMultiClickThreshhold(50L);
		sinadButton.setBorder(BorderFactory.createEtchedBorder());
		sinadButton.setToolTipText("Start SINAD Meter");

		new IconRetriever(aprsButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "SSID-00.png");
		aprsButton.setFocusable(false);
		aprsButton.setMultiClickThreshhold(50L);
		aprsButton.setBorder(BorderFactory.createEtchedBorder());
		aprsButton.setToolTipText("Start APRS");

		new IconRetriever(coverageTestButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "signalstrength.gif");
		coverageTestButton.setFocusable(false);
		coverageTestButton.setMultiClickThreshhold(50L);
		coverageTestButton.setBorder(BorderFactory.createEtchedBorder());
		coverageTestButton.setToolTipText("Start Coverage Test");

		new IconRetriever(staticLocationAnalysisButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "radio.png");
		staticLocationAnalysisButton.setFocusable(false);
		staticLocationAnalysisButton.setMultiClickThreshhold(50L);
		staticLocationAnalysisButton.setBorder(BorderFactory.createEtchedBorder());
		staticLocationAnalysisButton.setToolTipText("Static Location Analysis");
		
		new IconRetriever(newLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "NewDocumentHS.png");
		newLogFileButton.setFocusable(false);
		newLogFileButton.setMultiClickThreshhold(50L);
		newLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		newLogFileButton.setToolTipText("Create New Log File");

		new IconRetriever(openLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "OpenHS.png");
		openLogFileButton.setFocusable(false);
		openLogFileButton.setMultiClickThreshhold(50L);
		openLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		openLogFileButton.setToolTipText("Open Log File");

		new IconRetriever(saveLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "SaveHS.png");
		saveLogFileButton.setFocusable(false);
		saveLogFileButton.setMultiClickThreshhold(50L);
		saveLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		saveLogFileButton.setToolTipText("Save Log File");

		new IconRetriever(closeLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "closefile.gif");
		closeLogFileButton.setFocusable(false);
		closeLogFileButton.setMultiClickThreshhold(50L);
		closeLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		closeLogFileButton.setToolTipText("Close Log File");

		new IconRetriever(bofLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "MoveFirstHS.png");
		bofLogFileButton.setFocusable(false);
		bofLogFileButton.setMultiClickThreshhold(50L);
		bofLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		bofLogFileButton.setToolTipText("Jump to Start of Log File");

		new IconRetriever(eofLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "MoveLastHS.png");
		eofLogFileButton.setFocusable(false);
		eofLogFileButton.setMultiClickThreshhold(50L);
		eofLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		eofLogFileButton.setToolTipText("Jump to End of Log File");

		new IconRetriever(stopLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "StopHS.png");
		stopLogFileButton.setFocusable(false);
		stopLogFileButton.setMultiClickThreshhold(50L);
		stopLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		stopLogFileButton.setToolTipText("Stop Log File");

		new IconRetriever(replayLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "PlayHS.png");
		replayLogFileButton.setFocusable(false);
		replayLogFileButton.setMultiClickThreshhold(50L);
		replayLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		replayLogFileButton.setToolTipText("Replay Log File");

		new IconRetriever(recordLogFileButton, System.getProperty("user.dir") + File.separator + "images" + File.separator + "RecordHS.png");
		recordLogFileButton.setFocusable(false);
		recordLogFileButton.setMultiClickThreshhold(50L);
		recordLogFileButton.setBorder(BorderFactory.createEtchedBorder());
		recordLogFileButton.setToolTipText("Record Log File");

		gpsStatus.setBorder(BorderFactory.createEtchedBorder());
		gpsStatus.setHorizontalAlignment(SwingConstants.CENTER);
		gpsStatus.setFont(new Font("Tahoma", 1, 11));
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
		radioStatus.setFont(new Font("Tahoma", 1, 11));
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
		aprsStatus.setFont(new Font("Tahoma", 1, 11));
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

		signalQuality0.setBorder(BorderFactory.createEtchedBorder());
		signalQuality0.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality0.setFont(new Font("Tahoma", 0, 11));
		signalQuality0.setText("         ");

		signalQuality1.setBorder(BorderFactory.createEtchedBorder());
		signalQuality1.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality1.setFont(new Font("Tahoma", 0, 11));
		signalQuality1.setText("         ");

		signalQuality2.setBorder(BorderFactory.createEtchedBorder());
		signalQuality2.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality2.setFont(new Font("Tahoma", 0, 11));
		signalQuality2.setText("         ");

		signalQuality3.setBorder(BorderFactory.createEtchedBorder());
		signalQuality3.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality3.setFont(new Font("Tahoma", 0, 11));
		signalQuality3.setText("         ");

		signalQuality4.setBorder(BorderFactory.createEtchedBorder());
		signalQuality4.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality4.setFont(new Font("Tahoma", 0, 11));
		signalQuality4.setText("         ");

		signalQuality5.setBorder(BorderFactory.createEtchedBorder());
		signalQuality5.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality5.setFont(new Font("Tahoma", 0, 11));
		signalQuality5.setText("         ");

		signalQuality6.setBorder(BorderFactory.createEtchedBorder());
		signalQuality6.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality6.setFont(new Font("Tahoma", 0, 11));
		signalQuality6.setText("         ");

		signalQuality7.setBorder(BorderFactory.createEtchedBorder());
		signalQuality7.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality7.setFont(new Font("Tahoma", 0, 11));
		signalQuality7.setText("         ");

		signalQuality8.setBorder(BorderFactory.createEtchedBorder());
		signalQuality8.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality8.setFont(new Font("Tahoma", 0, 11));
		signalQuality8.setText("         ");

		signalQuality9.setBorder(BorderFactory.createEtchedBorder());
		signalQuality9.setHorizontalAlignment(SwingConstants.CENTER);
		signalQuality9.setFont(new Font("Tahoma", 0, 11));
		signalQuality9.setText("         ");

		aprsLatitude.setBorder(BorderFactory.createEtchedBorder());
		aprsLatitude.setHorizontalAlignment(SwingConstants.CENTER);
		aprsLatitude.setFont(new Font("Tahoma", 0, 11));
		aprsLatitude.setToolTipText("APRS Latitude");

		aprsLongitude.setBorder(BorderFactory.createEtchedBorder());
		aprsLongitude.setHorizontalAlignment(SwingConstants.CENTER);
		aprsLongitude.setFont(new Font("Tahoma", 0, 11));
		aprsLongitude.setToolTipText("APRS Longitude");

		aprsCallSign.setBorder(BorderFactory.createEtchedBorder());
		aprsCallSign.setHorizontalAlignment(SwingConstants.CENTER);
		aprsCallSign.setFont(new Font("Tahoma", 0, 11));
		aprsCallSign.setToolTipText("APRS Call Sign");

		aprsSSID.setBorder(BorderFactory.createEtchedBorder());
		aprsSSID.setHorizontalAlignment(SwingConstants.CENTER);
		aprsSSID.setFont(new Font("Tahoma", 0, 11));
		aprsSSID.setToolTipText("APRS Service Set Identifier");

		markerID.setBorder(BorderFactory.createEtchedBorder());
		markerID.setHorizontalAlignment(SwingConstants.CENTER);
		markerID.setFont(new Font("Tahoma", 0, 11));
		markerID.setToolTipText("Record Marker");
		markerID.setText(markerFormat.format(markerCounter));

		recordPointerLabel.setBorder(BorderFactory.createEtchedBorder());
		recordPointerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		recordPointerLabel.setFont(new Font("Tahoma", 0, 11));
		recordPointerLabel.setToolTipText("Record Pointer");

		recordCountLabel.setBorder(BorderFactory.createEtchedBorder());
		recordCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
		recordCountLabel.setFont(new Font("Tahoma", 0, 11));
		recordCountLabel.setToolTipText("Record Count");

		cursorUTM.setBorder(BorderFactory.createEtchedBorder());
		cursorUTM.setHorizontalAlignment(SwingConstants.CENTER);
		cursorUTM.setFont(new Font("Tahoma", 0, 11));
		cursorUTM.setToolTipText("UTM Location of Cursor");

		measurementPeriod.setBorder(BorderFactory.createEtchedBorder());
		measurementPeriod.setHorizontalAlignment(SwingConstants.CENTER);
		measurementPeriod.setFont(new Font("Tahoma", 0, 11));
		measurementPeriod.setToolTipText("Measurement Timer Period in Milliseconds");

		measurementsThisGrid.setBorder(BorderFactory.createEtchedBorder());
		measurementsThisGrid.setHorizontalAlignment(SwingConstants.CENTER);
		measurementsThisGrid.setFont(new Font("Tahoma", 0, 11));
		measurementsThisGrid.setToolTipText("Measurements Taken in This Grid");

		tileCount.setBorder(BorderFactory.createEtchedBorder());
		tileCount.setHorizontalAlignment(SwingConstants.CENTER);
		tileCount.setFont(new Font("Tahoma", 0, 11));
		tileCount.setToolTipText("Total Number of Fully Measured Tiles");

		averageRssiInCurrentTile.setBorder(BorderFactory.createEtchedBorder());
		averageRssiInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
		averageRssiInCurrentTile.setFont(new Font("Tahoma", 0, 11));
		averageRssiInCurrentTile.setToolTipText("Average RSSI in Current Tile");

		averageBerInCurrentTile.setBorder(BorderFactory.createEtchedBorder());
		averageBerInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
		averageBerInCurrentTile.setFont(new Font("Tahoma", 0, 11));
		averageBerInCurrentTile.setToolTipText("Average BER in Current Tile");

		averageSinadInCurrentTile.setBorder(BorderFactory.createEtchedBorder());
		averageSinadInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
		averageSinadInCurrentTile.setFont(new Font("Tahoma", 0, 11));
		averageSinadInCurrentTile.setToolTipText("Average SINAD in Current Tile");

		radioRxDataWord.setBorder(BorderFactory.createEtchedBorder());
		radioRxDataWord.setHorizontalAlignment(SwingConstants.LEFT);
		radioRxDataWord.setFont(new Font("Tahoma", 0, 11));
		radioRxDataWord.setToolTipText("Data Received From Radio");
		radioRxDataWord.setHorizontalTextPosition(SwingConstants.LEFT);

		radioTxDataWord.setBorder(BorderFactory.createEtchedBorder());
		radioTxDataWord.setHorizontalAlignment(SwingConstants.LEFT);
		radioTxDataWord.setFont(new Font("Tahoma", 0, 11));
		radioTxDataWord.setToolTipText("Data Sent To Radio");
		radioTxDataWord.setHorizontalTextPosition(SwingConstants.LEFT);

		cursorGridSquare.setBorder(BorderFactory.createEtchedBorder());
		cursorGridSquare.setHorizontalAlignment(SwingConstants.CENTER);
		cursorGridSquare.setFont(new Font("Tahoma", 0, 11));
		cursorGridSquare.setToolTipText("Grid Square of Cursor");

		cursorLatitude.setBorder(BorderFactory.createEtchedBorder());
		cursorLatitude.setHorizontalAlignment(SwingConstants.CENTER);
		cursorLatitude.setFont(new Font("Tahoma", 0, 11));
		cursorLatitude.setToolTipText("Latitude of Mouse Cursor");

		cursorLongitude.setBorder(BorderFactory.createEtchedBorder());
		cursorLongitude.setHorizontalAlignment(SwingConstants.CENTER);
		cursorLongitude.setFont(new Font("Tahoma", 0, 11));
		cursorLongitude.setToolTipText("Longitude of Mouse Cursor");

		logFileNameLabel.setBorder(BorderFactory.createEtchedBorder());
		logFileNameLabel.setFont(new Font("Courier New", 0, 11));
		logFileNameLabel.setToolTipText("Log File Name");

		nmeaSentenceStringLabel.setBorder(BorderFactory.createEtchedBorder());
		nmeaSentenceStringLabel.setFont(new Font("Courier New", 0, 11));
		nmeaSentenceStringLabel.setToolTipText("Received NMEA Sentence String");
		nmeaSentenceStringLabel.setHorizontalTextPosition(SwingConstants.LEFT);

		utcLabel.setBorder(BorderFactory.createEtchedBorder());
		utcLabel.setFont(new Font("Tahoma", 0, 11));
		utcLabel.setToolTipText("Universal Coordinated Time");
		utcLabel.setOpaque(true);
		utcLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		gpsInfoPanel.setBorder(BorderFactory.createTitledBorder("GPS Information"));

		gpsCompassRose.setBorder(BorderFactory.createTitledBorder("GPS Heading"));
		gpsCompassRose.setSelectColor(Color.GRAY);

		signalMeterArray.setBorder(BorderFactory.createTitledBorder("Signal Strength"));

		longitude.setFont(new Font("Tahoma", 1, 12));
		longitude.setHorizontalAlignment(SwingConstants.CENTER);
		longitude.setBorder(BorderFactory.createTitledBorder(null, "Longitude",
		        TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 9)));
		longitude.setHorizontalTextPosition(SwingConstants.CENTER);

		latitude.setFont(new Font("Tahoma", 1, 12));
		latitude.setHorizontalAlignment(SwingConstants.CENTER);
		latitude.setBorder(BorderFactory.createTitledBorder(null, "Latitude",
		        TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 9)));
		latitude.setHorizontalTextPosition(SwingConstants.CENTER);

		currentUTM.setFont(new Font("Tahoma", 1, 10));
		currentUTM.setHorizontalAlignment(SwingConstants.CENTER);
		currentUTM.setBorder(BorderFactory.createTitledBorder(null, "UTM",
		        TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 9)));
		currentUTM.setToolTipText("GPS UTM Coordinates");
		currentUTM.setHorizontalTextPosition(SwingConstants.CENTER);

		currentGridSquare.setFont(new Font("Tahoma", 1, 12));
		currentGridSquare.setHorizontalAlignment(SwingConstants.CENTER);
		currentGridSquare.setBorder(BorderFactory.createTitledBorder(null,
		        "Reference Grid Square", TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 9)));
		currentGridSquare.setToolTipText("Reference Grid Square at GPS Location");
		currentGridSquare.setHorizontalTextPosition(SwingConstants.CENTER);

		speedMadeGoodMPH.setFont(new Font("Tahoma", 1, 12));
		speedMadeGoodMPH.setHorizontalAlignment(SwingConstants.CENTER);
		speedMadeGoodMPH.setBorder(BorderFactory.createTitledBorder(null,
		        "Speed Made Good MPH", TitledBorder.DEFAULT_JUSTIFICATION,
		        TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 9)));
		speedMadeGoodMPH.setHorizontalTextPosition(SwingConstants.CENTER);

		setToolTipText();
		tileSizeArcSeconds = coverageTestSettings.getTileSizeArcSeconds(startupLonLat.y);
		currentLonLat = startupLonLat;
		
		staticTestSettingsPropertyChangeEvent(null);
		updateMapSettings();
		map.setVisible(true);
		
		analogTest = !coverageTestSettings.isBERSamplingInEffect();

		setLogMode(LogMode.CLOSED);
    }

    private void setToolTipText() {
        if (analogTest) {
            signalQuality0.setToolTipText("Receive Channel 1 dBm / SINAD");
        } else {
            signalQuality0.setToolTipText("Receive Channel 1 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality1.setToolTipText("Receive Channel 2 dBm / SINAD");
        } else {
            signalQuality1.setToolTipText("Receive Channel 2 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality2.setToolTipText("Receive Channel 3 dBm / SINAD");
        } else {
            signalQuality2.setToolTipText("Receive Channel 3 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality3.setToolTipText("Receive Channel 4 dBm / SINAD");
        } else {
            signalQuality3.setToolTipText("Receive Channel 4 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality4.setToolTipText("Receive Channel 5 dBm / SINAD");
        } else {
            signalQuality4.setToolTipText("Receive Channel 5 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality5.setToolTipText("Receive Channel 6 dBm / SINAD");
        } else {
            signalQuality5.setToolTipText("Receive Channel 6 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality6.setToolTipText("Receive Channel 7 dBm / SINAD");
        } else {
            signalQuality6.setToolTipText("Receive Channel 7 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality7.setToolTipText("Receive Channel 8 dBm / SINAD");
        } else {
            signalQuality7.setToolTipText("Receive Channel 8 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality8.setToolTipText("Receive Channel 9 dBm / SINAD");
        } else {
            signalQuality8.setToolTipText("Receive Channel 9 dBm / Bit Error Rate");
        }
        if (analogTest) {
            signalQuality9.setToolTipText("Receive Channel 10 dBm / SINAD");
        } else {
            signalQuality9.setToolTipText("Receive Channel 10 dBm / Bit Error Rate");
        }
    }

    private void createGraphicalUserInterface() {
        GroupLayout gpsInfoPanelLayout = new GroupLayout(gpsInfoPanel);

        gpsInfoPanel.setLayout(gpsInfoPanelLayout);

        gpsInfoPanelLayout.setHorizontalGroup(gpsInfoPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(longitude, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addComponent(latitude, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addComponent(currentUTM, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addComponent(currentGridSquare, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addComponent(speedMadeGoodMPH, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE));

        gpsInfoPanelLayout.setVerticalGroup(gpsInfoPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                gpsInfoPanelLayout
                .createSequentialGroup()
                .addComponent(longitude,
                GroupLayout.DEFAULT_SIZE, 40,
                Short.MAX_VALUE)
                .addComponent(latitude,
                GroupLayout.DEFAULT_SIZE, 40,
                Short.MAX_VALUE)
                .addComponent(currentUTM,
                GroupLayout.DEFAULT_SIZE, 40,
                Short.MAX_VALUE)
                .addComponent(currentGridSquare,
                GroupLayout.DEFAULT_SIZE, 40,
                Short.MAX_VALUE)
                .addComponent(speedMadeGoodMPH,
                GroupLayout.DEFAULT_SIZE, 40,
                Short.MAX_VALUE).addContainerGap()));

        GroupLayout layout = new GroupLayout(getContentPane());

        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(toolBar, GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(
                layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mapPanel,
                GroupLayout.PREFERRED_SIZE, PREFERRED_MAP_SIZE.width,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addGroup(
                layout.createParallelGroup(
                GroupLayout.Alignment.LEADING)
                .addComponent(
                gpsCompassRose,
                GroupLayout.PREFERRED_SIZE,
                170,
                GroupLayout.PREFERRED_SIZE)
                .addGroup(
                layout.createSequentialGroup()
                .addGap(5)
                .addComponent(
                barMeter,
                GroupLayout.PREFERRED_SIZE,
                14,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                gpsInfoPanel,
                GroupLayout.PREFERRED_SIZE,
                150,
                GroupLayout.PREFERRED_SIZE))
                .addComponent(
                signalMeterArray,
                GroupLayout.PREFERRED_SIZE,
                170,
                GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
                .addGroup(
                layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(
                layout.createParallelGroup(
                GroupLayout.Alignment.TRAILING)
                .addGroup(
                layout.createSequentialGroup()
                .addComponent(
                cursorLatitude,
                GroupLayout.PREFERRED_SIZE,
                90,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                cursorLongitude,
                GroupLayout.PREFERRED_SIZE,
                90,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                cursorUTM,
                GroupLayout.PREFERRED_SIZE,
                145,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                cursorGridSquare,
                GroupLayout.PREFERRED_SIZE,
                75,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                nmeaSentenceStringLabel,
                GroupLayout.PREFERRED_SIZE,
                279,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                utcLabel,
                GroupLayout.PREFERRED_SIZE,
                100,
                GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(
                gpsStatus,
                GroupLayout.PREFERRED_SIZE,
                55,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                gpsTxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                gpsRxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                gpsCTS,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                gpsDSR,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                gpsCD,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE))
                .addGroup(
                layout.createSequentialGroup()
                .addComponent(
                logFileNameLabel,
                GroupLayout.PREFERRED_SIZE,
                149,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                measurementPeriod,
                GroupLayout.PREFERRED_SIZE,
                55,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                measurementsThisGrid,
                GroupLayout.PREFERRED_SIZE,
                45,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                tileCount,
                GroupLayout.PREFERRED_SIZE,
                45,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                averageRssiInCurrentTile,
                GroupLayout.PREFERRED_SIZE,
                50,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                averageBerInCurrentTile,
                GroupLayout.PREFERRED_SIZE,
                50,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                averageSinadInCurrentTile,
                GroupLayout.PREFERRED_SIZE,
                50,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                radioRxDataWord,
                GroupLayout.PREFERRED_SIZE,
                80,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                radioTxDataWord,
                GroupLayout.PREFERRED_SIZE,
                80,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                recordPointerLabel,
                GroupLayout.PREFERRED_SIZE,
                75,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                recordCountLabel,
                GroupLayout.PREFERRED_SIZE,
                75,
                GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(
                radioStatus,
                GroupLayout.PREFERRED_SIZE,
                55,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                radioTxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                radioRxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                radioCTS,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                radioDSR,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                radioCD,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE))
                .addGroup(
                layout.createSequentialGroup()
                .addComponent(
                signalQuality0,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality1,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality2,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality3,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality4,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality5,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality6,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality7,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality8,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalQuality9,
                GroupLayout.PREFERRED_SIZE,
                66,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                markerID,
                GroupLayout.PREFERRED_SIZE,
                94,
                GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(
                aprsStatus,
                GroupLayout.PREFERRED_SIZE,
                55,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                aprsTxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                aprsRxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                aprsCTS,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                aprsDSR,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                aprsCD,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(10, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                layout.createSequentialGroup()
                .addComponent(toolBar,
                GroupLayout.DEFAULT_SIZE, 25,
                Short.MAX_VALUE)
                .addGap(5)
                .addGroup(
                layout.createParallelGroup(
                GroupLayout.Alignment.LEADING)
                .addComponent(
                mapPanel,
                GroupLayout.PREFERRED_SIZE,
                PREFERRED_MAP_SIZE.height,
                GroupLayout.PREFERRED_SIZE)
                .addGroup(
                layout.createSequentialGroup()
                .addGroup(
                layout.createParallelGroup(
                GroupLayout.Alignment.LEADING)
                .addComponent(
                barMeter,
                GroupLayout.PREFERRED_SIZE,
                250,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                gpsInfoPanel,
                GroupLayout.PREFERRED_SIZE,
                250,
                GroupLayout.PREFERRED_SIZE))
                .addGap(5)
                .addComponent(
                gpsCompassRose,
                GroupLayout.PREFERRED_SIZE,
                170,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                signalMeterArray,
                GroupLayout.PREFERRED_SIZE,
                170,
                GroupLayout.PREFERRED_SIZE)))
                .addGap(5)
                .addGroup(
                layout.createParallelGroup(
                GroupLayout.Alignment.TRAILING)
                .addComponent(
                signalQuality0,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality1,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality2,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality3,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality4,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality5,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality6,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality7,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality8,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                signalQuality9,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                markerID,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                aprsStatus,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                aprsTxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                aprsRxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                aprsCTS,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                aprsDSR,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                aprsCD,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE))
                .addGap(5)
                .addGroup(
                layout.createParallelGroup(
                GroupLayout.Alignment.TRAILING)
                .addComponent(
                logFileNameLabel,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                measurementPeriod,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                measurementsThisGrid,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                tileCount,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                averageRssiInCurrentTile,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                averageBerInCurrentTile,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                averageSinadInCurrentTile,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioRxDataWord,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioTxDataWord,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                recordCountLabel,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                recordPointerLabel,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioStatus,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioTxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioRxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioCTS,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioDSR,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                radioCD,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE))
                .addGap(5)
                .addGroup(
                layout.createParallelGroup(
                GroupLayout.Alignment.LEADING)
                .addComponent(
                cursorLongitude,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                cursorLatitude,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                cursorUTM,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                cursorGridSquare,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                nmeaSentenceStringLabel,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                utcLabel,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                gpsStatus,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                gpsTxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                gpsRxData,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                gpsCTS,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                gpsDSR,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                gpsCD,
                GroupLayout.PREFERRED_SIZE,
                18,
                GroupLayout.PREFERRED_SIZE))
                .addGap(5)));
    }

   private void displayGraphicalUserInterface() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();

        pack();
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DriveTrack Radio System Performance Measurement Utility");
        
        if (radioComponent.isStartRadioWithSystem()) makeRadioActive(true);

        setVisible(true);
    }

   	private void staticTestSettingsPropertyChangeEvent(PropertyChangeEvent event) {
   		map.setTargetRingColor(staticTestSettings.getTargetRingColor());
   		map.showTargetRing(staticTestSettings.isShowTargetRing());
   		map.setArcAsymptoteColor(staticTestSettings.getAsymptoteColor());
   		map.showArcAsymptotes(staticTestSettings.isShowAsymptotes());
   		map.setArcColors(staticTestSettings.getArcColors());
   		map.showArcs(staticTestSettings.isShowArcs());
   		map.setArcTrailColor(staticTestSettings.getTrailColor());
   		map.showArcTrails(staticTestSettings.isShowTrails());
   		map.setArcCursorColor(staticTestSettings.getCursorColor());
   		map.setArcCursorDiameter(staticTestSettings.getCursorDiameter());
   		map.showArcCursors(staticTestSettings.isShowCursors());
   		map.setArcIntersectPointColor(staticTestSettings.getIntersectPointColor());
	   	map.showArcIntersectPoints(staticTestSettings.isShowIntersectPoints());
	   	map.setTrailEqualsFlightColor(staticTestSettings.isTrailEqualsFlightColor());
   	}
   
   	private void coverageTestSettingsPropertyChangeEvent(PropertyChangeEvent event) {
    	tileSizeArcSeconds = coverageTestSettings.getTileSizeArcSeconds(currentLonLat.y);
        if (Math.abs(map.getGridSize().y - tileSizeArcSeconds.y) > 0.0000001 ||
        		Math.abs(map.getGridSize().x - tileSizeArcSeconds.x) > 0.0000001) {
            map.deleteAllQuads();
        	map.setGridSize(tileSizeArcSeconds);
            indexPointer = 0;
            tileIndex.subList(0, tileIndex.size()).clear();
        }
        map.showQuads(coverageTestSettings.isShowGridSquareShading());
        radioComponent.radioInterface.sampleBERValues(coverageTestSettings.isBERSamplingInEffect());
        radioComponent.radioInterface.sampleRSSIValues(coverageTestSettings.isRSSISamplingInEffect());
        if (coverageTestSettings.getMinSamplesPerTile() == -1) {
            periodTimer.setDelay(coverageTestSettings.getMinTimePerTile() * 1000);
            periodTimer.setInitialDelay(coverageTestSettings.getMinTimePerTile() * 1000);
            periodTimer.start();
        } else {
            periodTimer.stop();
        }
        analogTest = !coverageTestSettings.isBERSamplingInEffect();
        setToolTipText();
    }

    private void aprsComponentChangeListenerEvent(PropertyChangeEvent event) {
        map.showIcons(aprsComponent.isEnableAprsTracking());
        map.showIconLabels(aprsComponent.isEnableAprsShowIconLabels());
    }
    
    private void mapPropertyChangedChangeListenerEvent(PropertyChangeEvent event) {
        staticTestSettingsPropertyChangeEvent(null);
        updateMapSettings();
    }
    
    private void mapTypeChangedChangeListenerEvent(PropertyChangeEvent event) {
        Point.Double point = map.getCenterLonLat();
        Dimension mapSize = map.getSize();
        int zoom = map.getZoom();
        map.setVisible(false);  
        mapPanel.removeAll();
        map.removePropertyChangeListener(mapPropertyChangeListener);
        map.removeMouseListener(mapMouseListener);
        map.removeMouseMotionListener(mapMouseMotionListener);
        map.removeKeyListener(mapKeyListener);
        
        switch ((int) event.getNewValue()) {
            case 0:
                map = new ImageMap(userPref.getInt("SelectedMapIndex", -1));
                map.setScale(1.0);
                break;
            case 1:
                map = new WorldWindMap(point, zoom, mapSize);
                break;
            case 2:
                map = new OpenStreetMapPanel(point, zoom, mapSize);
                break;
        }
 
        map.addPropertyChangeListener(mapPropertyChangeListener);
        map.addMouseListener(mapMouseListener);
        map.addMouseMotionListener(mapMouseMotionListener);
        map.addKeyListener(mapKeyListener);

        mapPanel.add((Component) map, BorderLayout.CENTER);
        
        mapPanel.revalidate();
        mapPanel.repaint();
        
        staticTestSettingsPropertyChangeEvent(null);
        updateMapSettings();
    }

    private void updateMapSettings() {
    	map.displayShapes(mapSettings.isDisplayShapes());
    	map.setGridColor(mapSettings.getGridColor());
		map.setGridSize(tileSizeArcSeconds);
		map.showGrid(mapSettings.isShowGrid());
		map.showIcons(aprsComponent.isEnableAprsTracking());
		map.showIconLabels(aprsComponent.isEnableAprsShowIconLabels());
		map.showLines(showLines);
		map.showRings(showRings);
		map.showDots(showDots);
		map.showQuads(coverageTestSettings.isShowGridSquareShading());
    }
    
    private void frameClosingWindowEvent(WindowEvent event) {
        try {
            Point.Double point = map.getCenterLonLat();
            if (point != null && point.x >= -180 && point.x <= 180) userPref.putDouble("MapLongitude", point.x);
            if (point != null && point.y >= -90 && point.y <= 90) userPref.putDouble("MapLatitude", point.y);
            userPref.putDouble("MapScale", map.getScale());
            userPref.putInt("MapZoom", map.getZoom());
            serialGPSComponent.dispose();
            radioComponent.dispose();
            aprsComponent.dispose();
            mapSettings.dispose();
            coverageTestSettings.dispose();
            staticTestSettings.dispose();
            getContentPane().removeAll();
            fh.flush();
            fh.close();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Exception", ex);
        }
        Runtime.getRuntime().exit(0);
    }

    private void doZoomIn() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		zoomInButton.setEnabled(false);
		zoomOutButton.setEnabled(false);
    	intersectList.subList(0, intersectList.size()).clear();
    	isZooming = true;
    	map.zoomIn();
    }
    
    private void doZoomOut() {
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	zoomInButton.setEnabled(false);
		zoomOutButton.setEnabled(false);
    	intersectList.subList(0, intersectList.size()).clear();
    	isZooming = true;
    	map.zoomOut();
    }
    
    private void zoomInButtonMousePressed(MouseEvent event) {
    	if (map.getZoom() >= map.getMaxZoom()) return;
        zoomInMouseDownTimer.start();
        doZoomIn();
    }
    
    private void zoomInButtonMouseReleased(MouseEvent event) {
        zoomInMouseDownTimer.stop();
    }

    private void zoomOutButtonMousePressed(MouseEvent event) {
    	if (map.getZoom() <= 1) return;
    	zoomOutMouseDownTimer.start();
        doZoomOut();
    }

    private void zoomOutButtonMouseReleased(MouseEvent event) {
        zoomOutMouseDownTimer.stop();
    }

    private void mapImageCompletePropertyChangeListener(PropertyChangeEvent event) {
    	repaint();
        if (processStaticMeasurements) compileStaticMeasurements();
    }
    
    private void mapImageNoOperationPropertyChangeListener(PropertyChangeEvent event) {
    	
    }
    
    private void mapRenderedPropertyChangeListener(PropertyChangeEvent event) {
    	if (serialGPSComponent.startGpsWithSystem()) makeGpsActive(true);
        if (aprsComponent.isStartAprsWithSystem())  makeAprsActive(true);
    }
    
    private void mapZoomCompletePropertyChangeListener(PropertyChangeEvent event) {
    	configureZoomButtons();
    	isZooming = false;
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void configureZoomButtons() {
    	if (map.getZoom() <= 1  ) {
    		zoomOutButton.setEnabled(false);
    	} else {
    		zoomOutButton.setEnabled(true);
    	}
    	if (map.getZoom() >= map.getMaxZoom()) {
    		zoomInButton.setEnabled(false);
    	} else {
    		zoomInButton.setEnabled(true);
    	}
        zoomInButton.setSelected(false);
        zoomOutButton.setSelected(false);
    }
    
    private void takeDownGps() {
    	gpsValidHeadingTimer.stop();
		gpsRxDataTimer.stop();
		map.showGpsSymbol(false);
		gpsStatus.setBackground(new Color(127, 0, 0));
		gpsCTS.setBackground(new Color(127, 0, 0));
		gpsDSR.setBackground(new Color(127, 0, 0));
		gpsCD.setBackground(new Color(127, 0, 0));
		gpsTxData.setBackground(new Color(127, 0, 0));
		gpsRxData.setBackground(new Color(127, 0, 0));
		nmeaSentenceStringLabel.setText("");
		latitude.setText("");
		longitude.setText("");
		currentUTM.setText("");
		currentGridSquare.setText("");
		speedMadeGoodMPH.setText("");
		gpsStatus.setText("OFF LINE");
		setUTCLabelColors(networkTime.getTimeStratum());
		gpsButton.setSelected(false);
		preGpsActive = false;
    }
    
    private void makeGpsActive(boolean isActive) {
		gpsButton.setSelected(isActive);
    	if (isActive && !preGpsActive) {
        	try {
	    		serialGPSComponent.serialInterface.setOnline(serialGPSComponent.getPortName());
	        	mapDragged = false;
	            if (serialGPSComponent.gpsInterface.getPosition() != null) {
	            	map.setGpsSymbol(serialGPSComponent.gpsInterface.getPosition(), 10, Color.RED, 360);
	            	map.showGpsSymbol(true);
	            	gpsValidHeadingTimer.start();
	            }
        	} catch (SerialPortException ex) {
        		takeDownGps();
        		reportException(ex);
        	}
    	}
    	if (!isActive && preGpsActive){
    		try {
	    		takeDownGps();
	            serialGPSComponent.serialInterface.closeSerialPort();
    		} catch (SerialPortException ex) {
    			reportException(ex);
    		}
        }
	    preGpsActive = isActive;
	}

	private void reportException(final ClassNotFoundException ex) {
    	log.log(Level.WARNING, "ClassNotFoundException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Class Not Found Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	private void reportException(final InstantiationException ex) {
    	log.log(Level.WARNING, "InstantiationException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Instantiation Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	private void reportException(final ParseException ex) {
    	log.log(Level.WARNING, "ParseException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Parse Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	private void reportException(final IOException ex) {
    	log.log(Level.WARNING, "IOException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Input / Output Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	private void reportException(final IllegalAccessException ex) {
    	log.log(Level.WARNING, "IllegalAccessException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Illegal Access Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
    private void reportException(final UnsupportedLookAndFeelException ex) {
    	log.log(Level.WARNING, "UnsupportedLookAndFeelException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Unsupported Look And Feel Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final SerialPortException ex) {
    	log.log(Level.WARNING, "SerialPortException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Serial Port Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final InterruptedException ex) {
    	log.log(Level.WARNING, "InterruptedException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Interrupted Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final ExecutionException ex) {
    	log.log(Level.WARNING, "ExecutionException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "Execution Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final SecurityException ex) {
    	log.log(Level.WARNING, "SecurityException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                	ex.getMessage(), "SecurityException", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void gpsButtonMouseClicked(MouseEvent event) {
    	gpsButton.setSelected(!gpsButton.isSelected());
        makeGpsActive(gpsButton.isSelected());
    }

    private void aprsButtonMouseClicked(MouseEvent event) {
    	aprsButton.setSelected(!aprsButton.isSelected());
        makeAprsActive(aprsButton.isSelected());
    }
    
    private void takeDownAprs() {
    	aprsRxDataTimer.stop();
        aprsTxDataTimer.stop();
        aprsCTS.setBackground(new Color(127, 0, 0));
        aprsDSR.setBackground(new Color(127, 0, 0));
        aprsCD.setBackground(new Color(127, 0, 0));
        aprsTxData.setBackground(new Color(127, 0, 0));
        aprsRxData.setBackground(new Color(127, 0, 0));
        aprsStatus.setBackground(new Color(127, 0, 0));
        aprsStatus.setText("OFF LINE");
        aprsButton.setSelected(false);
        preAprsActive = false;
    }
    
    private void makeAprsActive(boolean isActive) {
        if (isActive && !preAprsActive) {
        	aprsButton.setSelected(isActive);
            try {
                aprsComponent.serialInterface.setOnline(aprsComponent.getPortName());
                aprsStatus.setText("ON LINE");
                aprsStatus.setBackground(new Color(0, 255, 0));
            } catch(SerialPortException ex) {
                takeDownAprs();
                reportException(ex);
            }
        }
        if (!isActive && preAprsActive){
            try {
            	takeDownAprs();
                aprsComponent.serialInterface.closeSerialPort();
            } catch(SerialPortException ex) {
            	reportException(ex);
            }
        }
        preAprsActive = isActive;
    }

    private void coverageTestButtonMouseClicked(MouseEvent event) {
        coverageTestButton.setSelected(!coverageTestButton.isSelected());
        coverageTestModeActive = coverageTestButton.isSelected();
        if (coverageTestModeActive) {
            currentLonLat = serialGPSComponent.gpsInterface.getPosition();
            gridLastMeasured = Coordinates.convertLonLatToGridSquare(currentLonLat, tileSizeArcSeconds);
            coverageTestSettings.tileSizeArcSecondsComboBoxEnabled(false);
            periodTimerTimeOut = false;
            periodTimerHalt = false;
            periodTimer.start();
        } else {
            coverageTestSettings.tileSizeArcSecondsComboBoxEnabled(true);
            coverageTestButton.setSelected(false);
            measurementTimer.stop();
        }
    }

    private void staticLocationAnalysisButtonMouseClicked(MouseEvent event) {
        staticLocationAnalysisButton.setSelected(!staticLocationAnalysisButton.isSelected());
        staticLocationAnalysisModeActive = staticLocationAnalysisButton.isSelected();
    }
    
    private void radioButtonMouseClicked(MouseEvent event) {
    	radioButton.setSelected(!radioButton.isSelected());
        makeRadioActive(radioButton.isSelected());
    }
    
    private void takeDownRadio() {
    	radioRxDataTimer.stop();
        radioTxDataTimer.stop();
        radioStatus.setBackground(new Color(127, 0, 0));
        radioCTS.setBackground(new Color(127, 0, 0));
        radioDSR.setBackground(new Color(127, 0, 0));
        radioCD.setBackground(new Color(127, 0, 0));
        radioTxData.setBackground(new Color(127, 0, 0));
        radioRxData.setBackground(new Color(127, 0, 0));
        radioStatus.setText("OFF LINE");
        radioTxDataWord.setText("");
		radioRxDataWord.setText("");
        barMeter.setLevel(0);
        signalMeterArray.setLevelMeter0(0);
        signalMeterArray.setLevelMeter1(0);
        signalMeterArray.setLevelMeter2(0);
        signalMeterArray.setLevelMeter3(0);
        signalMeterArray.setLevelMeter4(0);
        signalMeterArray.setLevelMeter5(0);
        signalMeterArray.setLevelMeter6(0);
        signalMeterArray.setLevelMeter7(0);
        signalMeterArray.setLevelMeter8(0);
        signalMeterArray.setLevelMeter9(0);
        radioButton.setSelected(false);
        preRadioActive = false;
    }
    
    private void makeRadioActive(boolean isActive) {
        radioButton.setSelected(isActive);
        if (isActive && !preRadioActive) {
            try {
                radioComponent.radioInterface.sampleBERValues(coverageTestSettings.isBERSamplingInEffect());
                radioComponent.radioInterface.sampleRSSIValues(coverageTestSettings.isRSSISamplingInEffect());
                radioComponent.serialInterface.setOnline(radioComponent.getPortName());
            } catch(SerialPortException ex) {
            	takeDownRadio();
                reportException(ex);
            }
        }
        if (!isActive && preRadioActive){
            try {
            	takeDownRadio();
                radioComponent.serialInterface.setOnline(radioComponent.getPortName());
            } catch(SerialPortException ex) {
            	reportException(ex);
            }
        }
        preRadioActive = isActive;
    }

    private void sinadButtonMouseClicked(MouseEvent event) {
        sinadButton.setSelected(!sinadButton.isSelected());
        sinadModeActive = sinadButton.isSelected();
        if (sinadModeActive) {
            sinad = new Sinad();
            sinad.startSinad();
            sinad.addSinadChangedChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent event) {
                    sinadChangedChangeListenerEvent(event);
                }
            });
        } else {
            sinad.stopSinad();
        }
    }

    private void eofLogFileButtonMouseClicked(MouseEvent event) {
        if (logMode != LogMode.CLOSED) {
            accessDatabase.seek(accessDatabase.getNumberOfRecords());
			recordPointer = accessDatabase.getIndexCursor();
			recordPointerLabel.setText(recordFormat.format(recordPointer));
        }
    }

    private void bofLogFileButtonMouseClicked(MouseEvent event) {
        if (logMode != LogMode.CLOSED) {
            accessDatabase.seek(0);
			recordPointer = accessDatabase.getIndexCursor() + 1;
			recordPointerLabel.setText(recordFormat.format(recordPointer));
        }
    }

    private void replayLogFileButtonMouseClicked(MouseEvent event) {
        replayLogFileButton.setSelected(!replayLogFileButton.isSelected());
        if (replayLogFileButton.isSelected()) {
            replayLogFile();
        }
    }

    private void recordLogFileButtonMouseClicked(MouseEvent event) {
        recordLogFileButton.setSelected(!recordLogFileButton.isSelected());
        if (recordLogFileButton.isSelected()) {
            if (logMode == LogMode.CLOSED) {
                newLogFileMenuItem.doClick();
            }
            setLogMode(LogMode.RECORD);
        } else {
            setLogMode(LogMode.STOP);
        }
    }

    private void stopLogFileButtonMouseClicked(MouseEvent event) {
        setLogMode(LogMode.STOP);
    }

    private void saveLogFileButtonMouseClicked(MouseEvent event) {
        saveLogFileMenuItem.doClick();
    }

    private void closeLogFileButtonMouseClicked(MouseEvent event) {
        closeLogFileMenuItem.doClick();
    }

    private void openLogFileButtonMouseClicked(MouseEvent event) {
        openLogFileMenuItem.doClick();
    }

    private void newLogFileButtonMouseClicked(MouseEvent event) {
        newLogFileMenuItem.doClick();
    }

    private void exitMenuItemActionListenerEvent(ActionEvent event) {
        System.exit(0);
    }

    private void printPreviewMenuItemActionListenerEvent(ActionEvent event) {
    	new PreviewPrintPanel(map.getScreenShot());
    }

    private void printMenuItemActionListenerEvent(ActionEvent event) {
    	SwingUtilities.invokeLater(new PrintUtilities(map.getScreenShot()));
    }

    private void mapPanelSpaceKeyPressed(KeyEvent event) {
        markerCounter++;
		markerID.setText(markerFormat.format(markerCounter));
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

    private void mapPanelRightMouseButtonClicked(MouseEvent event) {
        cursorBearingSet = false;
        cursorBearing = 0;
        cursorBearingPosition = map.getMouseCoordinates();
        cursorBearingIndex = addRdfBearing(cursorBearingPosition, 0, RDF_BEARING_LENGTH_IN_DEGREES, 
        	GPSInterface.RdfQuality.RDF_QUAL_8, Color.RED);
        map.addRing(cursorBearingPosition, 6, Color.RED);
    }

    private void mapPanelDragged(MouseEvent event) {
    	mapDragged = true;
    }
    
    private void mapPanelMouseMoved(MouseEvent event) {
    	Point.Double mousePosition = new Point.Double();
        mousePosition = map.getMouseCoordinates();
        if (mousePosition == null) return;
        updateGridSquare(mousePosition, tileSizeArcSeconds);
        cursorLatitude.setText(latFormat.format(mousePosition.y));
        cursorLongitude.setText(lonFormat.format(mousePosition.x));
        cursorUTM.setText(Coordinates.convertLonLatToUTM(mousePosition));
    }

    private void updateGridSquare(final Point.Double mousePosition, final Point.Double tileSizeArcSeconds) {
    	SwingWorker<Point, Void> upgradeGridSquareWorker = new SwingWorker<Point, Void>() {
            @Override
            protected Point doInBackground() throws Exception {
            	return Coordinates.convertLonLatToGridSquare(mousePosition, tileSizeArcSeconds);
            }
            @Override
            protected void done() {
				try {
					Point gridSquare = get();
					cursorGridSquare.setText(gridSquareFormat.format(gridSquare.x) + "  " + 
							gridSquareFormat.format(gridSquare.y));
				} catch(InterruptedException ex) {
					reportException(ex);
				} catch(ExecutionException ex) {
					reportException(ex);
				}
            }
    	};
    	upgradeGridSquareWorker.execute();
    }
    
    private double milliSecondsBetweenMeasurements(double sizeOfTileInMeters,
            double speedMadeGoodKPH, double measurementsPerTile) {
        double timeAcrossTileAtGivenSpeed = ((sizeOfTileInMeters / 1000.0)
                / (Math.max(speedMadeGoodKPH, 1))) * 3600.0 * 1000.0;
        return timeAcrossTileAtGivenSpeed / measurementsPerTile;
    }

    private void gpsValidFixPropertyChangeListenerEvent(PropertyChangeEvent event) {
    	currentLonLat = serialGPSComponent.gpsInterface.getPosition();
        processPositionInformation(PositionSource.GPS, currentLonLat);
    }
    
    private void gpsValidPositionPropertyChangeListenerEvent(PropertyChangeEvent event) {
    	currentLonLat = serialGPSComponent.gpsInterface.getPosition();
    	processPositionInformation(PositionSource.GPS, currentLonLat);
    }

    private void gpsValidTimePropertyChangeListenerEvent(PropertyChangeEvent event) {
    	long newGpsTimeInMillis = (long) event.getNewValue();
    	if (preGpsTimeInMillis != newGpsTimeInMillis) {
    		networkTime.setGpsTimeInMillis(newGpsTimeInMillis);
    	}
    	preGpsTimeInMillis = newGpsTimeInMillis;
    }
    
    private void processPositionInformation(PositionSource dataSourceMode, Point.Double currentLonLat) {
    	int m_delay = 0;
    	this.currentLonLat = currentLonLat;
        lastInputSource = dataSourceMode;

        currentGrid = Coordinates.convertLonLatToGridSquare(currentLonLat, tileSizeArcSeconds);
        
        centerOfGrid = Coordinates.convertLonLatToCenterOfGrid(currentLonLat, tileSizeArcSeconds); 
        
        tileDimensionInMeters = new Point.Double(
        		Vincenty.distanceTo(centerOfGrid, new Point.Double(centerOfGrid.x + (tileSizeArcSeconds.x / 3600.0),
        				centerOfGrid.y)), 
                Vincenty.distanceTo(centerOfGrid, new Point.Double(centerOfGrid.x, centerOfGrid.y + 
                		(tileSizeArcSeconds.y / 3600.0))));
        
        gridLastMeasured = currentGrid;
        		
        if (serialGPSComponent.enableGpsTracking() && dataSourceMode != PositionSource.MANUAL) {
            double angle;
            if (serialGPSComponent.gpsInterface.getSpeedMadeGoodMPH() >= 2) {
                angle = serialGPSComponent.gpsInterface.getCourseMadeGoodTrue();
            } else {
                angle = 360;
            }
            map.setGpsSymbol(currentLonLat, 10, getGpsColor(serialGPSComponent.gpsInterface.getFixQuality()), (int) angle);
        } else {
            map.showGpsSymbol(false);
        }

        if (serialGPSComponent.centerMapOnGPSPosition() && dataSourceMode != PositionSource.MANUAL) {
        	boolean outOfCenter = checkMapRecenter(currentLonLat);
            if (!mapDragged && outOfCenter) map.setCenterLonLat(currentLonLat);
            if (mapDragged && !outOfCenter) mapDragged = false;
        }

        if (coverageTestModeActive && dataSourceMode != PositionSource.LOG) {

            double tileDimensionMinimumInMeters = Math.min(tileDimensionInMeters.x, tileDimensionInMeters.y);

            switch (coverageTestSettings.getSampleTimingMode()) {
                case 0:
                    m_delay = (int) Math.round(milliSecondsBetweenMeasurements(
                            tileDimensionMinimumInMeters, serialGPSComponent.gpsInterface.getSpeedMadeGoodKPH(),
                            coverageTestSettings.getDotsPerTile()));
                    break;
                case 1:
                    m_delay = (int) Math.round(milliSecondsBetweenMeasurements(
                            tileDimensionMinimumInMeters, serialGPSComponent.gpsInterface.getSpeedMadeGoodKPH(),
                            coverageTestSettings.getMinSamplesPerTile() + 5));
                    break;
                case 2:
                    m_delay = 200;
                    break;
            }
        }

        if (coverageTestSettings.getMinSamplesPerTile() == -1) {
            periodTimer.setDelay(coverageTestSettings.getMinTimePerTile() * 1000);
            periodTimer.setInitialDelay(coverageTestSettings.getMinTimePerTile() * 1000);
            periodTimer.start();
        } else {
            periodTimer.stop();
        }

        longitude.setText(lonFormat.format(currentLonLat.x));
        latitude.setText(latFormat.format(currentLonLat.y));
        speedMadeGoodMPH.setText(speedFormat.format(serialGPSComponent.gpsInterface.getSpeedMadeGoodMPH()));
        currentUTM.setText(serialGPSComponent.gpsInterface.getUTMCoordinates()); 
        currentGridSquare.setText(gridSquareFormat.format(currentGrid.x) + ", " + gridSquareFormat.format(currentGrid.y));
        tileCount.setText(measurementsThisGridFormat.format(totalGridsCompleted));

        averageRssiInCurrentTile.setText(averageRssiInCurrentTileText);
        averageBerInCurrentTile.setText(averageBerInCurrentTileText);
        averageSinadInCurrentTile.setText(averageSinadInCurrentTileText);

        if (dataSourceMode == PositionSource.GPS && coverageTestModeActive) {
            measurementTimer.setDelay(m_delay);
            measurementTimer.setInitialDelay(0);
            measurementPeriod.setText(measurementPeriodFormat.format(m_delay));
            isRecord = true;
            measurementTimer.start();
        } else if (dataSourceMode == PositionSource.MANUAL) {
            measurementTimer.stop();
            measurementPeriod.setText("MAN");
            isRecord = true;
            processDriveTestMeasurement();
        } else if (dataSourceMode == PositionSource.LOG) {
            measurementTimer.stop();
            measurementPeriod.setText("LOG");
            isRecord = true;
            processDriveTestMeasurement();
        }
    }
    
    private boolean checkMapRecenter(Point.Double point) {
    	if (!map.isRendered()) return false;
    	double longitudeThreshold = Math.abs((map.getMapLeftEdgeLongitude() - map.getMapRightEdgeLongitude()) / 4);
        double latitudeThreshold = Math.abs((map.getMapTopEdgeLatitude() - map.getMapBottomEdgeLatitude()) / 4);
        if (map.getMapRightEdgeLongitude() - point.x < longitudeThreshold
                || point.x - map.getMapLeftEdgeLongitude() < longitudeThreshold
                || map.getMapTopEdgeLatitude() - point.y < latitudeThreshold
                || point.y - map.getMapBottomEdgeLatitude() < latitudeThreshold) {
        	return true;
        } else {
        	return false;
        }
    }
    
    private void periodTimerActionListenerEvent(ActionEvent event) {
        periodTimerTimeOut = true;
    }

    private void measurementTimerActionListenerEvent(ActionEvent event) {
        processDriveTestMeasurement();
    }

    private void processStaticMeasurement(StaticMeasurement sm) {
    	staticList.add(sm);
    	if (logMode == LogMode.RECORD) accessDatabase.appendRecord(sm);
    	if (processStaticMeasurements) compileStaticMeasurements();
    }

    private void processDriveTestMeasurement() {
        boolean foundExistingTile = false;

        for (int i = 0; i < tileIndex.size(); i++) {
            if (currentGrid.x == tileIndex.get(i).getXY().x && currentGrid.y == tileIndex.get(i).getXY().y) {
                indexPointer = i;
                foundExistingTile = true;
                break;
            }
        }

        if (foundExistingTile) {
            tileIndex.get(indexPointer).incrCount();
        } else {
            tileIndex.add(new TileIndex(map, currentGrid, 1, centerOfGrid, tileSizeArcSeconds, Color.YELLOW));
            indexPointer = tileIndex.size() - 1;
            dotsPerTile = 0;
            tilesTraversed++;
        }

        if (dotsPerTile < coverageTestSettings.getDotsPerTile()) {
            if (Math.abs(lastDotLocation.x - currentLonLat.x) > 0.00001
                    || Math.abs(lastDotLocation.y - currentLonLat.y) > 0.00001) {
                Color dotColor;
                switch (coverageTestSettings.getSignalQualityDisplayMode()) {
                    case 0:
                        if (logMode == LogMode.REPLAY) {
                            dotColor = sinadToColor(logFileSINAD);
                        } else {
                            dotColor = sinadToColor(sinad.getSINAD());
                        }
                        map.addDot(currentLonLat, 3, dotColor);
                        break;
                    case 1:
                        if (logMode == LogMode.REPLAY) {
                            dotColor = dBmToColor(logFileRSSI);
                        } else {
                            dotColor = dBmToColor(radioComponent.radioInterface.getdBm());
                        }
                        map.addDot(currentLonLat, 3, dotColor);
                        break;
                    case 2:
                        if (logMode == LogMode.REPLAY) {
                            dotColor = berToColor(logFileBER);
                        } else {
                            dotColor = berToColor(radioComponent.radioInterface.getBER());
                        }
                        map.addDot(currentLonLat, 3, dotColor);
                        break;
                }
                dotsPerTile++;
                lastDotLocation = currentLonLat;
            }
        }
        
        double si = 0.0;
        
        if (sinad != null) si = sinad.getSINAD();
        
        if (logMode == LogMode.REPLAY) {
            tileIndex.get(indexPointer).addSinad(logFileSINAD);
            tileIndex.get(indexPointer).addBer(logFileBER);
            tileIndex.get(indexPointer).addRssi(logFileRSSI);
        } else {
            tileIndex.get(indexPointer).addSinad(si);
            tileIndex.get(indexPointer).addBer(radioComponent.radioInterface.getBER());
            tileIndex.get(indexPointer).addRssi(radioComponent.radioInterface.getRSSI());
        }

        if ((tileIndex.get(indexPointer).getCount() >= coverageTestSettings.getMinSamplesPerTile()
                && coverageTestSettings.getMinSamplesPerTile() != -1)
                || (periodTimerTimeOut && coverageTestSettings.getMinSamplesPerTile() == -1)) {
            for (int i = 0; i < tileIndex.get(indexPointer).getCount(); i++) {
                averageRssiInTile = tileIndex.get(indexPointer).getAvgRssi();
                averageBerInTile = tileIndex.get(indexPointer).getAvgBer();
                averageSinadInTile = tileIndex.get(indexPointer).getAvgSinad();
                averageRssiInCurrentTileText = dBmFormat.format(averageRssiInTile) + " dBm";
                averageBerInCurrentTileText = BERFormat.format(averageBerInTile) + " %";
                averageSinadInCurrentTileText = SINADFormat.format(averageSinadInTile) + " dB";
            }
        }

        if ((tileIndex.get(indexPointer).getCount() == coverageTestSettings.getMinSamplesPerTile()
                && coverageTestSettings.getMinSamplesPerTile() != -1)
                || (periodTimerTimeOut && coverageTestSettings.getMinSamplesPerTile() == -1)) {
            tileIndex.get(indexPointer).setColor(indexPointer, Color.GREEN);
            periodTimer.stop();
            totalGridsCompleted++;
            periodTimerTimeOut = false;
            periodTimerHalt = true;
            if (coverageTestSettings.isAlertOnMinimumSamplesPerTileAcquired() && logMode != LogMode.REPLAY
                    && logMode != LogMode.STOP) {
                AePlayWave aePlayWaveDing = new AePlayWave("/sounds/Ding.wav");
                aePlayWaveDing.start();
            }
        }

        if (logMode == LogMode.RECORD && lastInputSource != PositionSource.LOG
                && ((tileIndex.get(indexPointer).getCount() <= coverageTestSettings.getMaxSamplesPerTile()
                && coverageTestSettings.getMinSamplesPerTile() != -1)
                || ((!periodTimerHalt && coverageTestSettings.getMinSamplesPerTile() == -1)))) {

            DriveTestData data = new DriveTestData();
            
            data.ber = radioComponent.radioInterface.getBERList();
            data.freq = radioComponent.radioInterface.getScanList();
            data.rssi = radioComponent.radioInterface.getdBmList();

            if (serialGPSComponent.gpsInterface.isValidFix()) {
            	data.dtg = serialGPSComponent.gpsInterface.getDate();
            } else {
            	data.dtg = Calendar.getInstance();
            }

            if (serialGPSComponent.gpsInterface.isValidFix()) {            	
            	data.position = serialGPSComponent.gpsInterface.getPosition();
            } else {
            	data.position = map.getMouseCoordinates();
            }

            if (serialGPSComponent.gpsInterface.isValidTrueRdfHeading()) {
                data.dopplerDirection = serialGPSComponent.gpsInterface.getRdfHeadingTrue();
                data.dopplerQuality = getRdfQuality(serialGPSComponent.gpsInterface.getRdfQuality());
            }

            data.sentence = "#DBMTD";
            data.gridLastMeasured = gridLastMeasured;
            data.tilesTraversed = tilesTraversed;
            data.measurementDelayTimer= measurementTimer.getDelay();
            data.tileIndexPointer = tileIndex.get(indexPointer).getCount();
            data.tileSize = coverageTestSettings.getTileSizeArcSeconds(data.position.y);
            data.maximumSamplesPerTile = coverageTestSettings.getMaxSamplesPerTile();
            data.minimumSamplesPerTile = coverageTestSettings.getMinSamplesPerTile();
            data.marker = markerCounter;
            
            measurementsThisGrid.setText(measurementsThisGridFormat.format(tileIndex.get(indexPointer).getCount()));
            
            writeLogFileLine(data);
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
        gpsRxData.setBackground(new Color(255, 0, 0));
        gpsRxDataTimer.stop();
    }

    private void gpsTxDataTimerActionListenerEvent(ActionEvent event) {
        gpsTxData.setBackground(new Color(255, 0, 0));
        gpsTxDataTimer.stop();
    }

    private void radioRxDataTimerActionListenerEvent(ActionEvent event) {
        radioRxData.setBackground(new Color(255, 0, 0));
        radioRxDataTimer.stop();
    }

    private void radioTxDataTimerActionListenerEvent(ActionEvent event) {
        radioTxData.setBackground(new Color(255, 0, 0));
        radioTxDataTimer.stop();
    }

    private void aprsRxDataTimerActionListenerEvent(ActionEvent event) {
        aprsRxData.setBackground(new Color(255, 0, 0));
        aprsRxDataTimer.stop();
    }

    private void aprsTxDataTimerActionListenerEvent(ActionEvent event) {
        aprsTxData.setBackground(new Color(255, 0, 0));
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

    private void mapConfigurationMenuActionListenerEvent(ActionEvent event) {
        map.showSettings(true);
    }
    
    private void staticSignalLocationSettingsMenuActionListenerEvent(ActionEvent event) {
        staticTestSettings.showSettingsDialog(true);
    }
    
    private void coverageTestSettingsMenuActionListenerEvent(ActionEvent event) {
        coverageTestSettings.showSettingsDialog(true);
    }

    private void sinadChangedChangeListenerEvent(ChangeEvent event) {
        sinadArray[radioComponent.radioInterface.getCurrentChannel()] = (int) sinad.getSINAD();
        setSignalQualityDisplay();
        if (lastInputSource == PositionSource.MANUAL && coverageTestSettings.getManualDataCollectionMode() == 0
                && coverageTestModeActive && isRecord) {
            processDriveTestMeasurement();
        }
    }

    private void newLogFileMenuItemActionListenerEvent(ActionEvent event) {
        setLogMode(LogMode.OPEN);
		Calendar cal = Calendar.getInstance();
		recordPointer = 0;
		recordCount = 0;
		recordPointerLabel.setText(recordFormat.format(recordPointer));
		recordCountLabel.setText(recordFormat.format(recordCount));
		markerCounter = 0;
		markerID.setText(markerFormat.format(markerCounter));
		map.deleteAllDots();
		map.deleteAllIcons();
		map.deleteAllLines();
		map.deleteAllQuads();
		map.deleteAllRings();
		map.deleteAllArcs();
		tileIndex.subList(0, tileIndex.size()).clear();
		indexPointer = -1;
		dotsPerTile = 0;
		tilesTraversed = 0;
		lastDotLocation = new Point.Double(0,0);
		isRecord = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String fln = sdf.format(cal.getTime()) + ".mdb";
		logFileName = System.getProperty("user.home") + File.separator + "drivetrack" + File.separator + "log" +                
			File.separator + fln;
		accessDatabase = new AccessDatabase(this, logFileName);
		accessDatabase.addPropertyChangeListener(logDatabaseListener);
		logFileNameLabel.setText(fln);
		setLogMode(LogMode.OPEN);
    }

    private void openLogFileMenuItemActionListenerEvent(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
		if (lastLogFileDirectory != null) {
		    fileChooser.setCurrentDirectory(new File(lastLogFileDirectory));
		}
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Coverage Test Database Files", "mdb");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle("Open Database File");
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    remove(fileChooser);
		    repaint();
		    lastLogFileDirectory = fileChooser.getCurrentDirectory().getPath();
		    userPref.put("LastLogFileDirectory", lastLogFileDirectory);
		    logFileName = fileChooser.getCurrentDirectory().getPath() + File.separator + fileChooser.getSelectedFile().getName();
		    accessDatabase = new AccessDatabase(this, logFileName);
	        accessDatabase.addPropertyChangeListener(logDatabaseListener);
		    logFileNameLabel.setText(fileChooser.getSelectedFile().getName());
		} else if (returnVal == JFileChooser.CANCEL_OPTION) {
		    remove(fileChooser);
		} 
    }

    private void closeLogFileMenuItemActionListenerEvent(ActionEvent event) {
        if (logMode != LogMode.CLOSED && !accessDatabase.isClosed()) {
			accessDatabase.close();
		}
    }

    private void saveLogFileMenuItemActionListenerEvent(ActionEvent event) {
        if (logMode != LogMode.CLOSED && !accessDatabase.isClosed()) {
		    accessDatabase.close();
		    accessDatabase = new AccessDatabase(this, logFileName);
		}
    }

    private void saveAsLogFileMenuItemActionListenerEvent(ActionEvent event) {
        if (logMode != LogMode.CLOSED) {
            JFileChooser fileChooser = new JFileChooser();
            if (lastLogFileDirectory != null) {
                fileChooser.setCurrentDirectory(new File(lastLogFileDirectory));
            }
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Coverage Test Database Files", "mdb");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Save Database File As");
            fileChooser.showSaveDialog(this);
            File from = null;
            File to = null;
            if (event.getID() == JFileChooser.APPROVE_OPTION) {
                int currentRecord = accessDatabase.getIndexCursor();
				accessDatabase.close();
				from = new File(logFileName);
				logFileName = fileChooser.getCurrentDirectory().getPath() + fileChooser.getSelectedFile().getName() + 
						".mdb";
				to = new File(logFileName);
				if (!from.renameTo(to)) {
					log.log(Level.WARNING, "IOException", "File rename failed");
				    SwingUtilities.invokeLater(new Runnable() {
				        @Override
				        public void run() {
				            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
				            	"File rename failed", "I/O Exception", JOptionPane.ERROR_MESSAGE);
				        }
				    });
					lastLogFileDirectory = fileChooser.getCurrentDirectory().getPath();
					userPref.put("LastLogFileDirectory", lastLogFileDirectory);	
					logFileName = from.getPath() + from.getName();
				}
				accessDatabase = new AccessDatabase(this, logFileName);
				logFileNameLabel.setText(fileChooser.getSelectedFile().getName());
				remove(fileChooser);
				setLogMode(LogMode.OPEN);
				if (accessDatabase.getNumberOfRecords() > 0) accessDatabase.seek(currentRecord); 
                remove(fileChooser);
            } 
        }
    }

    private void writeLogFileLine(DriveTestData data) {
        if (logMode == LogMode.RECORD) {
            accessDatabase.appendRecord(data);
        }
    }

    private void gpsReceivedDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        nmeaSentenceStringLabel.setText((String) event.getNewValue());
    }

    private void gpsCourseMadeGoodTruePropertyChangeListenerEvent(PropertyChangeEvent event) {
    	gpsCompassRose.setSelectColor(getGpsColor(serialGPSComponent.gpsInterface.getFixQuality()));
        gpsValidHeadingTimer.restart();
        gpsCompassRose.setHeading((int) serialGPSComponent.gpsInterface.getCourseMadeGoodTrue());
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
	        case GPS_DOWN: return Color.BLACK;
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
	        case INVALID: return new Color(127, 0, 0);
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
	        case GPS_DOWN: return "DOWN";
	        default: return "DOWN";
        }
	}
    
    private void gpsFixQualityPropertyChangeListenerEvent(PropertyChangeEvent event) {
    	FixQuality fixQuality = (FixQuality) event.getNewValue();
    	gpsStatus.setBackground(getGpsStatusBackgroundColor(fixQuality));
    	gpsStatus.setForeground(getGpsStatusForegroundColor(fixQuality));
    	gpsStatus.setText(getGpsStatusText(fixQuality));
    	map.setGpsSymbolColor(getGpsColor(fixQuality));
    }

   	private void gpsRxDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        gpsRxData.setBackground(Color.GREEN);
        gpsRxDataTimer.start();
    }

    private void gpsCTSHoldingPropertyChangeListenerEvent(PropertyChangeEvent event) {
        if (serialGPSComponent.serialInterface.isCTS()) {
            gpsCTS.setBackground(Color.GREEN);
        } else {
            gpsCTS.setBackground(Color.RED);
        }
    }

    private void gpsDSRHoldingPropertyChangeListenerEvent(PropertyChangeEvent event) {
        if (serialGPSComponent.serialInterface.isDSR()) {
            gpsDSR.setBackground(Color.GREEN);
        } else {
            gpsDSR.setBackground(Color.RED);
        }
    }

    private void gpsCDHoldingPropertyChangeListenerEvent(PropertyChangeEvent event) {
        if (serialGPSComponent.serialInterface.isRLSD()) {
            gpsCD.setBackground(Color.GREEN);
        } else {
            gpsCD.setBackground(Color.RED);
        }
    }
    
    private void radioBusyChangeListenerEvent(PropertyChangeEvent event) {
        if (radioComponent.radioInterface.isBusy()) {
            radioStatus.setText("BUSY");
            radioStatus.setBackground(Color.YELLOW);
        } else {
            radioStatus.setText("MON");
            radioStatus.setBackground(Color.RED);
        }
    }

    private void radioRSSIChangeListenerEvent(PropertyChangeEvent event) {
        barMeter.setLevel((int) Math.round(radioComponent.radioInterface.getPercent()));
        signalMeterArray.setLevelMeter0(radioComponent.radioInterface.getPercentList()[0]);
        signalMeterArray.setLevelMeter1(radioComponent.radioInterface.getPercentList()[1]);
        signalMeterArray.setLevelMeter2(radioComponent.radioInterface.getPercentList()[2]);
        signalMeterArray.setLevelMeter3(radioComponent.radioInterface.getPercentList()[3]);
        signalMeterArray.setLevelMeter4(radioComponent.radioInterface.getPercentList()[4]);
        signalMeterArray.setLevelMeter5(radioComponent.radioInterface.getPercentList()[5]);
        signalMeterArray.setLevelMeter6(radioComponent.radioInterface.getPercentList()[6]);
        signalMeterArray.setLevelMeter7(radioComponent.radioInterface.getPercentList()[7]);
        signalMeterArray.setLevelMeter8(radioComponent.radioInterface.getPercentList()[8]);
        signalMeterArray.setLevelMeter9(radioComponent.radioInterface.getPercentList()[9]);
        setSignalQualityDisplay();
        radioComponent.setCurrentRSSILevel((int) event.getNewValue());
        if (lastInputSource == PositionSource.MANUAL && coverageTestSettings.getManualDataCollectionMode() == 0
                && coverageTestModeActive && isRecord) {
            processDriveTestMeasurement();
        }
    }

    private void radioBERChangeListenerEvent(PropertyChangeEvent event) {
        setSignalQualityDisplay();
        if (lastInputSource == PositionSource.MANUAL && coverageTestSettings.getManualDataCollectionMode() == 0
                && coverageTestModeActive && isRecord) {
            processDriveTestMeasurement();
        }
    }

    private void setSignalQualityDisplay() {
        if (analogTest) {
            signalQuality0.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[0]) + " / "
                    + SINADFormat.format(sinadArray[0]));
            signalQuality1.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[1]) + " / "
                    + SINADFormat.format(sinadArray[1]));
            signalQuality2.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[2]) + " / "
                    + SINADFormat.format(sinadArray[2]));
            signalQuality3.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[3]) + " / "
                    + SINADFormat.format(sinadArray[3]));
            signalQuality4.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[4]) + " / "
                    + SINADFormat.format(sinadArray[4]));
            signalQuality5.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[5]) + " / "
                    + SINADFormat.format(sinadArray[5]));
            signalQuality6.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[6]) + " / "
                    + SINADFormat.format(sinadArray[6]));
            signalQuality7.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[7]) + " / "
                    + SINADFormat.format(sinadArray[7]));
            signalQuality8.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[8]) + " / "
                    + SINADFormat.format(sinadArray[8]));
            signalQuality9.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[9]) + " / "
                    + SINADFormat.format(sinadArray[9]));
        } else {
            signalQuality0.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[0]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[0]));
            signalQuality1.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[1]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[1]));
            signalQuality2.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[2]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[2]));
            signalQuality3.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[3]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[3]));
            signalQuality4.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[4]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[4]));
            signalQuality5.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[5]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[5]));
            signalQuality6.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[6]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[6]));
            signalQuality7.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[7]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[7]));
            signalQuality8.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[8]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[8]));
            signalQuality9.setText(dBmFormat.format(radioComponent.radioInterface.getdBmList()[9]) + " / "
                    + BERFormat.format(radioComponent.radioInterface.getBERList()[9]));
        }
    }

    private void aprsTxDataChangeListenerEvent(PropertyChangeEvent event) {
        aprsTxData.setBackground(Color.GREEN);
        aprsTxDataTimer.start();
    }

    private void aprsRxDataChangeListenerEvent(PropertyChangeEvent event) {
        aprsRxData.setBackground(Color.GREEN);
        aprsRxDataTimer.start();
    }

    private void aprsCTSHoldingChangeListenerEvent(PropertyChangeEvent event) {
        if (aprsComponent.serialInterface.isCTS()) {
            aprsCTS.setBackground(Color.GREEN);
        } else {
            aprsCTS.setBackground(Color.RED);
        }
    }

    private void aprsDSRHoldingChangeListenerEvent(PropertyChangeEvent event) {
        if ((boolean) event.getNewValue()) {
            aprsDSR.setBackground(Color.GREEN);
        } else {
            aprsDSR.setBackground(Color.RED);
        }
    }

    private void aprsCDHoldingChangeListenerEvent(PropertyChangeEvent event) {
        if ((boolean) event.getNewValue()) {
            aprsCD.setBackground(Color.GREEN);
        } else {
            aprsCD.setBackground(Color.RED);
        }
    }

    private void radioTxDataChangeListenerEvent(PropertyChangeEvent event) {
        radioTxData.setBackground(Color.GREEN);
        radioTxDataWord.setText("   " + event.getNewValue());
        radioTxDataTimer.start();
    }

    private void radioRxDataChangeListenerEvent(PropertyChangeEvent event) {
        radioRxData.setBackground(Color.GREEN);
        radioRxDataWord.setText("   " + radioComponent.radioInterface.getReceivedData());
        radioRxDataTimer.start();
    }

    private void radioOnlineChangeListenerEvent(PropertyChangeEvent event) {

    }
    
    private void invalidComPortChangeListenerEvent(final PropertyChangeEvent event, final String device) {
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Main.this),
                    "The " + device + " is configured to use comm port " + event.getNewValue() + "\n" +
                    "Please select a valid comm port from the radio settings menu.\n",
                    "Comm Port Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void radioCTSHoldingChangeListenerEvent(PropertyChangeEvent event) {
        if (radioComponent.serialInterface.isCTS()) {
            radioCTS.setBackground(Color.GREEN);
        } else {
            radioCTS.setBackground(Color.RED);
        }
    }

    private void radioDSRHoldingChangeListenerEvent(PropertyChangeEvent event) {
        if (radioComponent.serialInterface.isDSR()) {
            radioDSR.setBackground(Color.GREEN);
        } else {
            radioDSR.setBackground(Color.RED);
        }
    }

    private void radioCDHoldingChangeListenerEvent(PropertyChangeEvent event) {
        if (radioComponent.serialInterface.isRLSD()) {
            radioCD.setBackground(Color.GREEN);
        } else {
            radioCD.setBackground(Color.RED);
        }
    }

	private void replayLogFile() {
		logReplayProgressMonitor = ProgressUtil.createModelessProgressMonitor("Replaying Drive Test Log...", 
        		SwingConstants.HORIZONTAL, 0, 100, false, 50, 150);
        
        logReplayProgressMonitor.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if ("CANCEL".equals(event.getPropertyName())) {
	            	cancelProgressMonitor(event);
	            }
        	}
        });
        
        logReplayProgressMonitor.start(String.format("Completed %d%% of log file replay\n", 0));
        logReplayProgressMonitor.setNote("Please Stand By");
		
		driveTestData = new DriveTestData();
		
		indexPointer = 0;
		accessDatabase.findFirstRow();
		
		map.deleteAllDots();
		map.deleteAllIcons();
		map.deleteAllLines();
		map.deleteAllQuads();
		map.deleteAllRings();
		map.deleteAllArcs();
		map.deleteAllArcIntersectPoints();
		
		tileIndex.subList(0, tileIndex.size()).clear();

		setLogMode(LogMode.REPLAY);

		for (int i = 1; i <= recordCount; i++) {
			int progress = i / recordCount * 100;
			if (staticLocationAnalysisModeActive) accessDatabase.requestStaticMeasurement(i);
			if (coverageTestModeActive) accessDatabase.requestDriveTestData(i);
			logReplayProgressMonitor.setProgress(String.format("Completed %d%% of log file replay\n", progress), progress);
		    if (logMode == LogMode.STOP) break;
		}
		processStaticMeasurements = true;
		setLogMode(LogMode.REPLAY_COMPLETE);
    }

    private void cancelProgressMonitor(PropertyChangeEvent event) {
    	processStaticMeasurements = true;
    	setLogMode(LogMode.REPLAY_COMPLETE);
    }

    protected void databaseStaticMeasurementDataReadyChangeListenerEvent(PropertyChangeEvent event) {
    	recordPointer = accessDatabase.getIndexCursor();
		recordPointerLabel.setText(recordFormat.format(recordPointer));
		StaticMeasurement sm = (StaticMeasurement) event.getNewValue();
		if (sm.timeStamp > 0) {
			boolean outOfCenter = checkMapRecenter(sm.point);
            if (!mapDragged && outOfCenter) map.setCenterLonLat(currentLonLat);
            if (mapDragged && !outOfCenter) mapDragged = false;
			processStaticMeasurement(sm);
		} 
	}

	protected void databaseDriveTestDataReadyChangeListenerEvent(PropertyChangeEvent event) {
		recordPointer = accessDatabase.getIndexCursor();
		recordPointerLabel.setText(recordFormat.format(recordPointer));
        if (driveTestData.sentence.substring(0, 1).toString().equals("#")) {
            coverageTestSettings.setTileSize(driveTestData.tileSize);
            coverageTestSettings.setMinSamplesPerTile(driveTestData.minimumSamplesPerTile);
            coverageTestSettings.setMaxSamplesPerTile(driveTestData.maximumSamplesPerTile);
            logFileRSSI = driveTestData.rssi[0];
            logFileSINAD = driveTestData.sinad[0];
            logFileBER = driveTestData.ber[0];
            Point.Double currentLonLat = driveTestData.position;
            boolean outOfCenter = checkMapRecenter(currentLonLat);
            if (!mapDragged && outOfCenter) map.setCenterLonLat(currentLonLat);
            if (mapDragged && !outOfCenter) mapDragged = false;
            processPositionInformation(PositionSource.LOG, currentLonLat);
        }
	}

	protected void databaseAppendedChangeListenerEvent(PropertyChangeEvent event) {
    	recordCount = accessDatabase.getNumberOfRecords();
        recordPointer = recordCount;
        recordPointerLabel.setText(recordFormat.format(recordPointer));
        recordCountLabel.setText(recordFormat.format(recordCount));
	}

	protected void databaseClosedChangeListenerEvent(PropertyChangeEvent event) {
		accessDatabase.removePropertyChangeListener(logDatabaseListener);
    	setLogMode(LogMode.CLOSED);
	}

	protected void databaseOpenChangeListenerEvent(PropertyChangeEvent event) {
    	recordPointer = 0;
    	synchronized(accessDatabase) {
    		try {
				accessDatabase.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	recordCount = accessDatabase.getNumberOfRecords();
		recordPointerLabel.setText(recordFormat.format(recordPointer));
		recordCountLabel.setText(recordFormat.format(recordCount));
		setLogMode(LogMode.OPEN);
	}
	
    private void setLogMode(LogMode mode) {
        switch (mode) {
            case OPEN:
                newLogFileMenuItem.setEnabled(false);
                openLogFileMenuItem.setEnabled(false);
                closeLogFileMenuItem.setEnabled(true);
                saveLogFileMenuItem.setEnabled(true);
                saveAsLogFileMenuItem.setEnabled(true);
                newLogFileButton.setEnabled(false);
                openLogFileButton.setEnabled(false);
                saveLogFileButton.setEnabled(true);
                closeLogFileButton.setEnabled(true);
                stopLogFileButton.setEnabled(true);
                replayLogFileButton.setEnabled(true);
                recordLogFileButton.setEnabled(true);
                bofLogFileButton.setEnabled(true);
                eofLogFileButton.setEnabled(true);
                break;
            case CLOSED:
                newLogFileMenuItem.setEnabled(true);
                openLogFileMenuItem.setEnabled(true);
                closeLogFileMenuItem.setEnabled(false);
                saveLogFileMenuItem.setEnabled(false);
                saveAsLogFileMenuItem.setEnabled(false);
                newLogFileButton.setEnabled(true);
                openLogFileButton.setEnabled(true);
                saveLogFileButton.setEnabled(false);
                closeLogFileButton.setEnabled(false);
                stopLogFileButton.setEnabled(false);
                replayLogFileButton.setEnabled(false);
                recordLogFileButton.setEnabled(false);
                bofLogFileButton.setEnabled(false);
                eofLogFileButton.setEnabled(false);
                recordCountLabel.setText("");
                recordPointerLabel.setText("");
                logFileNameLabel.setText("");
                break;
            case STOP:
                newLogFileMenuItem.setEnabled(false);
                openLogFileMenuItem.setEnabled(true);
                closeLogFileMenuItem.setEnabled(true);
                saveLogFileMenuItem.setEnabled(true);
                saveAsLogFileMenuItem.setEnabled(true);
                newLogFileButton.setEnabled(false);
                openLogFileButton.setEnabled(false);
                saveLogFileButton.setEnabled(true);
                closeLogFileButton.setEnabled(true);
                stopLogFileButton.setEnabled(true);
                replayLogFileButton.setEnabled(true);
                recordLogFileButton.setEnabled(true);
                recordLogFileButton.setSelected(false);
                bofLogFileButton.setEnabled(true);
                eofLogFileButton.setEnabled(true);
                break;
            case REPLAY:
                newLogFileMenuItem.setEnabled(false);
                openLogFileMenuItem.setEnabled(false);
                closeLogFileMenuItem.setEnabled(true);
                saveLogFileMenuItem.setEnabled(false);
                saveAsLogFileMenuItem.setEnabled(false);
                newLogFileButton.setEnabled(false);
                openLogFileButton.setEnabled(false);
                saveLogFileButton.setEnabled(false);
                closeLogFileButton.setEnabled(true);
                stopLogFileButton.setEnabled(true);
                replayLogFileButton.setEnabled(true);
                recordLogFileButton.setEnabled(false);
                bofLogFileButton.setEnabled(true);
                eofLogFileButton.setEnabled(true);
                break;
            case RECORD:
                newLogFileMenuItem.setEnabled(false);
                openLogFileMenuItem.setEnabled(false);
                closeLogFileMenuItem.setEnabled(true);
                saveLogFileMenuItem.setEnabled(true);
                saveAsLogFileMenuItem.setEnabled(true);
                newLogFileButton.setEnabled(false);
                openLogFileButton.setEnabled(false);
                saveLogFileButton.setEnabled(true);
                closeLogFileButton.setEnabled(true);
                stopLogFileButton.setEnabled(true);
                replayLogFileButton.setEnabled(false);
                recordLogFileButton.setEnabled(true);
                bofLogFileButton.setEnabled(false);
                eofLogFileButton.setEnabled(false);
                break;
            case PAUSE:
                newLogFileMenuItem.setEnabled(false);
                openLogFileMenuItem.setEnabled(false);
                closeLogFileMenuItem.setEnabled(true);
                saveLogFileMenuItem.setEnabled(true);
                saveAsLogFileMenuItem.setEnabled(true);
                newLogFileButton.setEnabled(false);
                openLogFileButton.setEnabled(false);
                saveLogFileButton.setEnabled(true);
                closeLogFileButton.setEnabled(true);
                stopLogFileButton.setEnabled(true);
                replayLogFileButton.setEnabled(true);
                recordLogFileButton.setEnabled(true);
                bofLogFileButton.setEnabled(true);
                eofLogFileButton.setEnabled(true);
                break;
            case REPLAY_COMPLETE:
                newLogFileMenuItem.setEnabled(false);
                openLogFileMenuItem.setEnabled(false);
                closeLogFileMenuItem.setEnabled(true);
                saveLogFileMenuItem.setEnabled(true);
                saveAsLogFileMenuItem.setEnabled(true);
                newLogFileButton.setEnabled(false);
                openLogFileButton.setEnabled(false);
                saveLogFileButton.setEnabled(true);
                closeLogFileButton.setEnabled(true);
                stopLogFileButton.setEnabled(true);
                replayLogFileButton.setEnabled(false);
                recordLogFileButton.setEnabled(true);
                bofLogFileButton.setEnabled(true);
                eofLogFileButton.setEnabled(true);
                break;
        }
        logMode = mode;
    }

    private static class TileIndex {     
        private Point point;
        private double sinad;
        private double ber;
        private double rssi;
        private int count;
        private Map map;

        private TileIndex(Map map, Point point, int count, Point.Double centerOfGrid, Point.Double tileSizeArcSeconds, 
        		Color color) {
        	this.map = map;
            this.point = point;
            this.count = count;
            map.addQuad(centerOfGrid, tileSizeArcSeconds, color);
        }

        private Point getXY() {
            return point;
        }

        private int getCount() {
            return count;
        }

        private void incrCount() {
            count++;
        }

        private void setColor(int index, Color color) {
        	map.changeQuadColor(index, color);
        }
        
        private void addSinad(double sinad) {
            this.sinad = this.sinad + sinad;
        }

        private double getAvgSinad() {
            return sinad / count;
        }

        private void addBer(double ber) {
            this.ber = this.ber + ber;
        }

        private double getAvgBer() {
            return ber / count;
        }

        private void addRssi(double rssi) {
            this.rssi = this.rssi + rssi;
        }

        private double getAvgRssi() {
            return rssi / count;
        }
    }

    private void startTest() {
	    final double TEST_SPEED_KPH = 300;
	    final double TEST_HEADING = 0;	
	    
	    Point.Double distance = Vincenty.metersToDegrees(3000, currentLonLat.y);
	    Point.Double source = new Point.Double(-83.07724, 40.026563);
	    
	    Point.Double p1 = new Point.Double(-83.23791 - (2*distance.x), 40.05074 + (4*distance.y));
	    Point.Double p2 = new Point.Double(-83.23791 - (4*distance.x), 40.05074 + (8*distance.y));
	    
	    Point.Double p3 = new Point.Double(-82.84790 + (2*distance.x), 40.02551 + (4*distance.y));
	    Point.Double p4 = new Point.Double(-82.84790 + (4*distance.x), 40.02551 + (8*distance.y));
	    
	    double alt = 10000;
	    
	    double d1 = Vincenty.getVincentyInverse(source, p1).distance;
	    double d2 = Vincenty.getVincentyInverse(source, p2).distance;
	    double d3 = Vincenty.getVincentyInverse(source, p3).distance;
	    double d4 = Vincenty.getVincentyInverse(source, p4).distance;
	    
	    double da = Math.sqrt((d1*d1)+(alt*alt));
	    double db = Math.sqrt((d2*d2)+(alt*alt));
	    double dc = Math.sqrt((d3*d3)+(alt*alt));
	    double dd = Math.sqrt((d4*d4)+(alt*alt));
	    
	    double k1 = ConicSection.getFreeSpacePathLoss(da, 100);
	    double k2 = ConicSection.getFreeSpacePathLoss(db, 100);
        double k3 = ConicSection.getFreeSpacePathLoss(dc, 100);
        double k4 = ConicSection.getFreeSpacePathLoss(dd, 100);

        processStaticMeasurement(new StaticMeasurement(p1, networkTime.getBestTimeInMillis(), k1, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 1));
        processStaticMeasurement(new StaticMeasurement(p2, networkTime.getBestTimeInMillis(), k2, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 1));
        processStaticMeasurement(new StaticMeasurement(p3, networkTime.getBestTimeInMillis(), k3, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 2));       
        processStaticMeasurement(new StaticMeasurement(p4, networkTime.getBestTimeInMillis(), k4, TEST_HEADING, TEST_SPEED_KPH, alt, 100, 2)); 

        map.setCenterLonLat(source);
        processStaticMeasurements = true;
	}

    private int getNumberOfFlights(List<StaticMeasurement> sml) {
    	int lastUnit = 0;
    	for (StaticMeasurement sm : staticList) {
    		lastUnit = Math.max(sm.unit, lastUnit);
		}
    	return lastUnit;
    }

    private void compileStaticMeasurements() {
    	if (staticList.isEmpty()) return;
    	
    	int numberOfFlights = getNumberOfFlights(staticList);
    	double maxCatt = staticTestSettings.getMaxCatt();
    	double minCatt = 15;
    	
    	conicSectionListStartSize = conicSectionList.size();
    	
    	for (int z = 0; z < numberOfFlights; z++) {

    		List<StaticMeasurement> sl = new ArrayList<>();
    		
    		for (StaticMeasurement sm : staticList) {
    			if (sm.unit == z+1) sl.add(sm);
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
		    			if (sl.get(n).dBm < sl.get(i).dBm) {
		    				ConicSection cone = new ConicSection(sl.get(n), sl.get(i));
		    				if (cone.getConicAngleToTarget() >= minCatt && cone.getConicAngleToTarget() <= maxCatt) {
		    					conicSectionList.add(cone);
		    					map.addArc(cone, z);
		    				}
		    			} else {
		    				ConicSection cone = new ConicSection(sl.get(i), sl.get(n));
		    				if (cone.getConicAngleToTarget() >= minCatt && cone.getConicAngleToTarget() <= maxCatt) {
		    					conicSectionList.add(cone);
		    					map.addArc(cone, z);
		    				}
		    			}
		    		}
		    	}
    		}
    	}
        doInterceptListUpdate(conicSectionList, conicSectionListStartSize);
        processStaticMeasurements = false;
    }

    private void doInterceptListUpdate(List<ConicSection> list, int startSize) {
    	map.showTargetRing(false);
    	
    	intersectListUpdate = new IntersectList(list, startSize);

    	intersectListUpdate.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if ("INTERSECT_LIST_COMPLETE".equals(event.getPropertyName())) {
					intersectListUpdatePropertyChangeListenerEvent(event);
				}
			}
        });
    	
    	intersectListUpdate.execute();
    }

    private Color sinadToColor(double sinad) {
        return coverageTestSettings.getSinadColor(sinad);
    }

    private Color berToColor(double ber) {
        return coverageTestSettings.getBerColor(ber);
    }

    private Color dBmToColor(double dBm) {
        return coverageTestSettings.getdBmColor(dBm);
    }

    private void mapPanelLeftArrowKeyPressed(KeyEvent event) {
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

    private void mapPanelRightArrowKeyPressed(KeyEvent event) {
        if (!cursorBearingSet) {
            if (cursorBearing < 359) {
                cursorBearing++;
            } else {
                cursorBearing = 0;
            }
            moveRdfBearing(cursorBearingIndex, cursorBearingPosition, cursorBearing, RDF_BEARING_LENGTH_IN_DEGREES, 8, Color.RED);
        }
    }
    
    private void mapPanelLeftMouseButtonClicked(MouseEvent event) {
        if (!gpsModeActive && map.isShowGrid() && coverageTestModeActive) {
            Point.Double currentLonLat = map.getMouseCoordinates();
            periodTimerTimeOut = false;
            periodTimerHalt = false;
            periodTimer.start();
            isRecord = true;
            processPositionInformation(PositionSource.MANUAL, currentLonLat);
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
    	double size = 0;
    	double stdDev = pointStandardDeviation(intersectList);
		double s = (stdDev * 200.0) + (20.0 / (intersectList.size() / 10.0));
		if (intersectList != null) size = Vincenty.degreesToMeters(s, intersectList.get(0).y).x;
		return size;
    }
    
    private void intersectListUpdatePropertyChangeListenerEvent(PropertyChangeEvent event) {
    	try {
			intersectList.addAll(intersectListUpdate.get());
			Point.Double meanIntersect = pointMean(intersectList);
			map.setTargetRing(meanIntersect, getTargetRingSize(intersectList), staticTestSettings.getTargetRingColor());
			map.showTargetRing(staticTestSettings.isShowTargetRing());
			for (Point.Double p : intersectList) {
				map.addArcIntersectPoint(p, 5, staticTestSettings.getIntersectPointColor());
			}
			map.showArcIntersectPoints(staticTestSettings.isShowIntersectPoints());
		} catch(InterruptedException ex) {
			reportException(ex);
		}
    	catch(ExecutionException ex) {
    		reportException(ex);
		}
    }

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (isZooming) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

}

