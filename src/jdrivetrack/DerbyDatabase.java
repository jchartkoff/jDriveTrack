package jdrivetrack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import types.CoverageTestMeasurement;
import types.TestSettings;
import types.StaticMeasurement;
import types.TestTile;

public class DerbyDatabase {
	
	protected static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	protected static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	protected static final String MYSQL_URL = "jdbc:mysql://localhost:3306";
	protected static final String DERBY_URL = "jdbc:derby:";
	
	protected static final String USERNAME = "john";
	protected static final String PASSWORD = "password";
	
	public enum Row {FIRST, NEXT, LAST}

	public static final String DATABASE_OPEN = "DATABASE_OPEN";
	public static final String DATABASE_CLOSED = "DATABASE_CLOSED";
	public static final String DATABASE_RESTORE_PROGRESS = "DATABASE_RESTORE_PROGRESS";
	public static final String COVERAGE_TABLE_APPENDED = "COVERAGE_TABLE_APPENDED";
	public static final String STATIC_TABLE_APPENDED = "STATIC_TABLE_APPENDED";
	
	public static final String SETTINGS_TABLE_APPENDED = "SETTINGS_TABLE_APPENDED";
	public static final String COVERAGE_RECORD_COUNT_READY = "COVERAGE_RECORD_COUNT_READY";
	public static final String STATIC_RECORD_COUNT_READY = "STATIC_RECORD_COUNT_READY";
	public static final String SETTINGS_RECORD_COUNT_READY = "SETTINGS_RECORD_COUNT_READY";
	public static final String COVERAGE_TEST_DATA_READY = "COVERAGE_TEST_DATA_READY";
	public static final String STATIC_TEST_DATA_READY = "STATIC_TEST_DATA_READY";
	
	public static final String TILE_READY = "TILE_READY";
	public static final String TILE_UPDATED = "TILE_UPDATED";
	public static final String TILE_DELETED = "TILE_DELETED";
	public static final String TILE_NOT_FOUND = "TILE_NOT_FOUND";
	public static final String TILE_RETRIEVAL_PROGRESS = "TILE_RETRIEVAL_PROGRESS";
	public static final String TILE_RECORD_COUNT_READY = "TILE_RECORD_COUNT_READY";
	public static final String TILE_TABLE_APPENDED = "TILE_TABLE_APPENDED";
	public static final String TILE_COMPLETE_COUNT_READY = "TILE_COMPLETE_COUNT_READY";
	public static final String TILE_STATUS_CHANGED = "TILE_STATUS_CHANGED";
	
	public static final String SETTINGS_UPDATED = "SETTINGS_UPDATED";
	
	public static final String STATIC_DATA_NOT_FOUND = "STATIC_DATA_NOT_FOUND";
	public static final String COVERAGE_DATA_NOT_FOUND = "COVERAGE_DATA_NOT_FOUND";
	public static final String ALL_COVERAGE_RECORDS_READY = "ALL_COVERAGE_RECORDS_READY";
	public static final String ALL_TILE_RECORDS_READY = "ALL_TILE_RECORDS_READY";
	public static final String ALL_STATIC_RECORDS_READY = "ALL_STATIC_RECORDS_READY";
	
	public static final String STATIC_RETRIEVAL_PROGRESS = "STATIC_RETRIEVAL_PROGRESS";
	public static final String ALL_SIGNAL_MEASUREMENT_RECORDS_READY = "ALL_SIGNAL_MEASUREMENT_RECORDS_READY";
	public static final String TILE_TABLE_NAME = "TILE_TABLE";
	public static final String MEASUREMENT_TABLE = "MEASUREMENT_TABLE";
	public static final String STATIC_TABLE_NAME = "STATIC_TABLE";
	public static final String SETTINGS_READY = "SETTINGS_READY";
	public static final String SETTINGS_TABLE_NAME = "SETTINGS_TABLE";
	
	private static final String DEFAULT_LOG_FILE_DIR = System.getProperty("user.home") + 
	    	File.separator + "drivetrack" + File.separator + "database event log";

	private static final Logger log = Logger.getLogger(Database.class.getName());

	private volatile Connection conn;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private FileHandler fh;
	private volatile ResultSet coverageDataSet;
	private volatile ResultSet staticDataSet;
	private volatile ResultSet tileDataSet;
	private volatile ResultSet settingsDataSet;
	private volatile boolean databaseCreated = false;
	private volatile boolean sqlExceptionQueued = false;
	private boolean staticListReady = false;
	private boolean tileRecordListReady = false;
	private volatile int coverageRecordCount = 0;
	private volatile int staticRecordCount = 0;
	private volatile int settingsRecordCount = 0;
	private volatile TestTile currentTestTile = null;
	private volatile TestSettings testSettings;
    private volatile List<StaticMeasurement> staticList;
    private volatile List<TestTile> tileRecordList;
    private Thread database;
    
    public DerbyDatabase() {
		configureEventLog();
	}
	
	public DerbyDatabase(final File databasePath) {
		configureEventLog();
		openDatabase(databasePath);
	}
	
	public void openDatabase(final File databasePath) {
		openDatabase(databasePath.getPath());
	}
	
	public void openDatabase(final String databasePath) {
		database = new Database(databasePath, USERNAME, PASSWORD);
		new TableStats(MEASUREMENT_TABLE);
		new TableStats(STATIC_TABLE_NAME);
		new TableStats(TILE_TABLE_NAME);
		new TableStats(SETTINGS_TABLE_NAME);
	}
	
	public void appendRecord(final CoverageTestMeasurement ct, TestTile testTile) {
		new AppendCoverageTestRecord(ct, testTile);
	}
	
	public void appendRecord(final StaticMeasurement sm) {
		new AppendStaticMeasurementRecord(sm);
	}
	
	public void appendRecord(final TestTile testTile) {
		new AppendTileDataRecord(testTile);
	}
	
	private class AppendCoverageTestRecord extends Thread {
		private CoverageTestMeasurement ct;
		private TestTile testTile;
		
		private AppendCoverageTestRecord(CoverageTestMeasurement ct, TestTile testTile) {
			this.ct = ct;
			this.testTile = testTile;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					if (testTile.getMeasurementCount() < testSettings.getMaximumSamplesPerTile()) {
						testTile.incrementMeasurementCount();
						currentTestTile = testTile;
					} else {
						return;
					}
					
					String scanString = "";
					
					String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
							"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
					
					for (int i = 0; i < 10; i++) {
						scanString += "BER_F" + String.valueOf(i) + ", " +
						"RSSI_F" + String.valueOf(i) + ", " +
						"SINAD_F" + String.valueOf(i) + ", " +
						"RX_F" + String.valueOf(i) + ", " +
						"SCAN_SELECT_F" + String.valueOf(i) + ", ";
					}

					String insert = "INSERT INTO " + MEASUREMENT_TABLE + " " +
					    "(Sentence, " +
					    "Millis, " + 
					    scanString +
					    "TestTileID, " +
						"Longitude, " +
						"Latitude, " +
						"DopplerDirection, " +
						"DopplerQuality, " +
						"Marker) " +
						"VALUES (" + values + ")";

					PreparedStatement preparedStatement = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);
					
					preparedStatement.clearParameters();

					preparedStatement.setString(1, ct.getSentence());
					preparedStatement.setLong(2, ct.getMillis());
				    
				    for (int i = 0; i < 10; i++) {
				    	preparedStatement.setDouble((i*5)+3, ct.getBer(i));
				    	preparedStatement.setDouble((i*5)+4, ct.getdBm(i));
				    	preparedStatement.setDouble((i*5)+5, ct.getSinad(i));
				    	preparedStatement.setDouble((i*5)+6, ct.getFreq(i));
				    	preparedStatement.setBoolean((i*5)+7, ct.getSelect(i));
				    }
				    
				    preparedStatement.setLong(53, ct.getTestTileID());
				    preparedStatement.setDouble(54, ct.getPosition().x);
				    preparedStatement.setDouble(55, ct.getPosition().y);
				    preparedStatement.setDouble(56, ct.getDopplerDirection());
				    preparedStatement.setInt(57, ct.getDopplerQuality());
				    preparedStatement.setInt(58, ct.getMarker());
				    
			    	preparedStatement.executeUpdate();
					
			    	ResultSet rs = preparedStatement.getGeneratedKeys();
				    
				    while (rs.next()) {
				    	int currentKey = new Integer(rs.getInt(1));
				    	ct.setId(currentKey);
				    }

			    	conn.commit();
			    	preparedStatement.close();
			    	rs.close();
			    	
			    	pcs.firePropertyChange(COVERAGE_TABLE_APPENDED, null, currentTestTile.getMeasurementCount());

				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	public void createTestSettingsRecord(final TestSettings cts) {
		new AppendTestSettings(cts);
	}
	
	private class AppendTestSettings extends Thread {
		private TestSettings cts;
		
		private AppendTestSettings(final TestSettings cts) {
			if (settingsRecordCount > 0) return;
			this.cts = cts;
			settingsRecordCount++;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String values = "?,?,?,?,?,?,?,?,?,?,?";
					
					String insert = "INSERT INTO " + SETTINGS_TABLE_NAME + "(" +
						    "MinimumTimePerTile, " +
							"SampleTimingMode, " +
							"TileSizeWidth, " +
							"TileSizeHeight, " +
							"MaximumSamplesPerTile, " +
							"MinimumSamplesPerTile, " +
							"GridReferenceLeft, " +
							"GridReferenceTop, " +
							"GridSizeWidth, " +
							"GridSizeHeight, " +
							"DotsPerTile) " +
							"VALUES (" + values + ")";
					
					PreparedStatement preparedStatement = conn.prepareStatement(insert, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					
					preparedStatement.clearParameters();
					
				    preparedStatement.setInt(1, cts.getMinimumTimePerTile());
				    preparedStatement.setInt(2, cts.getSampleTimingMode());
				    preparedStatement.setDouble(3, cts.getTileSize().x);
				    preparedStatement.setDouble(4, cts.getTileSize().y);
				    preparedStatement.setInt(5, cts.getMaximumSamplesPerTile());
					preparedStatement.setInt(6, cts.getMinimumSamplesPerTile());
					preparedStatement.setDouble(7, cts.getGridReference().x);
				    preparedStatement.setDouble(8, cts.getGridReference().y);
				    preparedStatement.setDouble(9, cts.getGridSize().x);
				    preparedStatement.setDouble(10, cts.getGridSize().y);
				    preparedStatement.setInt(11, cts.getDotsPerTile());
				    
					preparedStatement.executeUpdate();
					
					conn.commit();
					preparedStatement.close();
					
					pcs.firePropertyChange(SETTINGS_TABLE_APPENDED, null, cts);

				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	private class AppendStaticMeasurementRecord extends Thread {
		private StaticMeasurement sm;
		
		private AppendStaticMeasurementRecord(final StaticMeasurement sm) {
			this.sm = sm;
			staticRecordCount++;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}

					String values = "?,?,?,?,?,?,?,?";
					
					String append = "INSERT INTO " + STATIC_TABLE_NAME + " " +
						"(TimeStamp," +
						"FlightLongitude," +
						"FlightLatitude," +
						"CourseMadeGoodTrue," +
						"SpeedMadeGoodKPH," +
						"Altitude," +
						"FrequencyMHz," +
						"Unit) " +
						"VALUES (" + values + ")";
					
					PreparedStatement preparedStatement = conn.prepareStatement(append);
					
					preparedStatement.clearParameters();

				    preparedStatement.setLong(1, sm.getTimeStamp());
				    preparedStatement.setDouble(2, sm.getPoint().x);
				    preparedStatement.setDouble(3, sm.getPoint().y);
				    preparedStatement.setDouble(4, sm.getCourseMadeGoodTrue());
			    	preparedStatement.setDouble(5, sm.getSpeedMadeGoodKPH());
			    	preparedStatement.setDouble(6, sm.getAltitude());
			    	preparedStatement.setDouble(7, sm.getFrequencyMHz());
			    	preparedStatement.setInt(8, sm.getUnit());
			    	
			    	preparedStatement.executeUpdate();
					
			    	conn.commit();
			    	preparedStatement.close();
			    	
			    	pcs.firePropertyChange(STATIC_TABLE_APPENDED, null, staticRecordCount);

				} catch (final InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	};
	
	private class AppendTileDataRecord extends Thread {
		private TestTile testTile;
		
		private AppendTileDataRecord(TestTile testTile) {
			this.testTile = testTile;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String values = "?,?,?,?,?,?,?,?,?,?,?,?,?";
					
					String insert = "INSERT INTO " + TILE_TABLE_NAME + " (" +
					    "Easting, " +
						"Northing, " +
						"Zone, " +
						"Longitude, " +
						"Latitude, " +
						"Precision, " +
						"LatBand, " +
						"AvgSinad, " +
						"AvgBer, " +
						"AvgRssi, " +
						"TileSizeWidth, " +
						"TileSizeHeight, " +
						"MeasurementCount) " +
						"VALUES (" + values + ")";
					
					PreparedStatement preparedStatement = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);
					
					preparedStatement.clearParameters();
				    
				    preparedStatement.setLong(1, testTile.getEasting());
				    preparedStatement.setLong(2, testTile.getNorthing());
				    preparedStatement.setInt(3, testTile.getGridZone());
				    preparedStatement.setDouble(4, testTile.getLonLat().x);
				    preparedStatement.setDouble(5, testTile.getLonLat().y);
					preparedStatement.setInt(6, testTile.getPrecision().ordinal());
				    preparedStatement.setString(7, testTile.getLatBand());
				    preparedStatement.setDouble(8, testTile.getAvgSinad());
				    preparedStatement.setDouble(9, testTile.getAvgBer());
				    preparedStatement.setDouble(10, testTile.getAvgdBm());
				    preparedStatement.setDouble(11, testTile.getTileSize().x);
				    preparedStatement.setDouble(12, testTile.getTileSize().y);
				    preparedStatement.setInt(13, testTile.getMeasurementCount());
				    
				    preparedStatement.executeUpdate();
				    
				    ResultSet rs = preparedStatement.getGeneratedKeys();
				    
				    if (testTile.getID() == 0) {
					    while (rs.next()) {
					    	int currentTileKey = new Integer(rs.getInt(1));
					    	testTile.setID(currentTileKey);
					    }
				    }
				    
				    currentTestTile = testTile;
				    
				    tileRecordList.add(testTile);
				    
				    conn.commit();
				    
				    pcs.firePropertyChange(TILE_TABLE_APPENDED, null, testTile);

					rs.close();
					preparedStatement.close();
					
				} catch (final InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	};
	
	private void createTables(Connection conn) {
		try {
	        PreparedStatement preparedStatement = conn.prepareStatement(getCoverageTableDef());
	        preparedStatement.executeUpdate();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 20);
	        preparedStatement.close();
	        preparedStatement = conn.prepareStatement(getTileTableDef());
	        preparedStatement.executeUpdate();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 40);
	        preparedStatement.close();
	        preparedStatement = conn.prepareStatement(getTileTableIndexDef());
	        preparedStatement.executeUpdate();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 60);
	        preparedStatement.close();
	        preparedStatement = conn.prepareStatement(getStaticTableDef());
	        preparedStatement.executeUpdate();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 80);
	        preparedStatement.close();
	        preparedStatement = conn.prepareStatement(getStaticTableIndexDef());
	        preparedStatement.executeUpdate();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 100);
	        preparedStatement.close();
	        preparedStatement = conn.prepareStatement(getSettingsTableDef());
	        preparedStatement.executeUpdate();
	        pcs.firePropertyChange(DATABASE_OPEN, null, null);
	        preparedStatement.close();
	        
		} catch(SQLException ex) {
			reportException(ex);
		}
	}

	private class Database extends Thread {
		private String databasePath;
		private String username;
		private String password;
		
		private Database(final File file, final String username, final String password) {
			this(file.getPath(), username, password);
		}
		
		private Database(final String databasePath, final String username, final String password) {
			this.databasePath = databasePath;
			this.username = username;
			this.password = password;
			start();
		}
		
		@Override
		public void run() {
			synchronized(this) {
				try {
					String url = DERBY_URL + databasePath + ";create=true";
					url = url.replace("\\", "/");
				    conn = DriverManager.getConnection(url, username, password);
				    conn.setAutoCommit(false);
				    if (!isTableCreated(TILE_TABLE_NAME)) {
						createTables(conn);
					}
				    conn.commit();
				    databaseCreated = true;
				    this.notifyAll();
				    pcs.firePropertyChange(DATABASE_OPEN, null, conn);
				} catch (SQLException ex) {
					pcs.firePropertyChange(DATABASE_CLOSED, null, null);
					reportException(ex);
				} catch (NullPointerException ex) {
					pcs.firePropertyChange(DATABASE_CLOSED, null, null);
					reportException(ex);
				}
			}
		}
	}

	private boolean isTableCreated(String tableName) {
		boolean result = false;
		ResultSet table = null;
		try {
			DatabaseMetaData metadata = conn.getMetaData();
			table = metadata.getTables(conn.getCatalog(), null, tableName, null);
			if (table.next()) result = true;
		} catch (SQLException ex) {
			reportException(ex);
		} catch (NullPointerException ex) {
			reportException(ex);
		} finally {
			try {
				table.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private void configureEventLog() {
		try {
        	String eventLogFileName = DEFAULT_LOG_FILE_DIR + File.separator + "event.log";
        	Path path = Paths.get(eventLogFileName);
    		File directory = new File(path.getParent().toString());
    		if (!directory.exists()) new File(path.getParent().toString()).mkdirs();
        	fh = new FileHandler(eventLogFileName, 4096, 64, true);
            log.addHandler(fh);
            log.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (final SecurityException ex) {
        	reportException(ex);
        } catch (final IOException ex) {
        	reportException(ex);
        }
	}

	public int getCursor(String tableName) {
		int ret = 0;
		try {
			if(tableName.contains(SETTINGS_TABLE_NAME)) {
				if (settingsDataSet != null && !settingsDataSet.isClosed() && settingsDataSet.getRow() > 0) {
					return settingsDataSet.getInt(1);
				} else {
					return 0;
				}
			}
			if(tableName.contains(STATIC_TABLE_NAME)) {
				if (staticDataSet != null && !staticDataSet.isClosed() && staticDataSet.getRow() > 0) {
					return staticDataSet.getInt(1);
				} else {
					return 0;
				}
			}
			if(tableName.contains(TILE_TABLE_NAME)) {
				if (tileDataSet != null && !tileDataSet.isClosed() && tileDataSet.getRow() > 0) {
					return tileDataSet.getInt(1);
				} else {
					return 0;
				}
			}
		} catch (final IllegalArgumentException ex) {
    		reportException(ex);
    	} catch (final SQLException ex) {
    		reportException(ex);
		}
		return ret;
	}
	
	public void requestCoverageTestRecord(final int index) {
		String sql = "SELECT * FROM " + MEASUREMENT_TABLE + " WHERE ID = " + String.valueOf(index);
		new RetrieveCoverageTestRecord(sql);
	}
	
	public void requestStaticTestRecord(final int index) {
		String sql = "SELECT * FROM " + STATIC_TABLE_NAME + " WHERE ID = " + String.valueOf(index);
		new RetrieveStaticTestRecord(sql);
	}
	
	public void requestTileRecord(final int index) {
		String sql = "SELECT * FROM " + TILE_TABLE_NAME + " WHERE ID = " + String.valueOf(index);
		new RetrieveTileRecord(sql);
	}
	
	public void requestTileRecord(final TestTile testTile) {
		String sql = "SELECT * FROM " + TILE_TABLE_NAME + " WHERE " + 
				"Easting = " + String.valueOf(testTile.getEasting()) + " AND " +
				"Northing = " + String.valueOf(testTile.getNorthing()) + " AND " +
				"Zone = " + String.valueOf(testTile.getGridZone()) + " AND " +
				"LatBand = " + "\'" + testTile.getLatBand() + "\'";
		if (currentTestTile.equals(testTile)) {
			pcs.firePropertyChange(TILE_READY, null, currentTestTile);
		} else {
			currentTestTile = testTile;
			new RetrieveTileRecord(sql);
		}
	}
	
	public void requestTilesCompleteCount() {
		new RetrieveTilesCompleteCount();
	}
	
	private class RetrieveTilesCompleteCount extends Thread {
		
		private int numberTilesComplete = 0;
		
		private RetrieveTilesCompleteCount() {
			this.start();
		}

		@Override
		public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String tileSql = "SELECT * FROM " + TILE_TABLE_NAME;

					PreparedStatement preparedStatement = conn.prepareStatement(tileSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

					ResultSet tileDataSet = preparedStatement.executeQuery();

		        	while(tileDataSet.next()) {
						TestTile testTile = TestTile.toTestTile(getRecordData(tileDataSet).clone());
		        		if (testTile.getMeasurementCount() >= testSettings.getMinimumSamplesPerTile()) numberTilesComplete++;
		        	}
		        	
		        	pcs.firePropertyChange(TILE_COMPLETE_COUNT_READY, null, numberTilesComplete);
		        	
		        	conn.commit();
		        	tileDataSet.close();
		        	preparedStatement.close();
		        	
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (SQLException ex) {
					reportException(ex);
				}
			}
			
		}
	}
	
	public TestTile testTileExists(TestTile testTile) {
		if (tileRecordList == null) return null;
		TestTile ret = null;
		for (TestTile tile : tileRecordList) {
			if (testTile.equals(tile)) {
				currentTestTile = testTile;
				ret = tile;
				break;
			}
		}
		return ret;
	}	
	
	public boolean isTileCreated(TestTile testTile) {
		if (tileRecordList == null) return false;
		boolean exists = false;
		for (TestTile tile : tileRecordList) {
			if (testTile.equals(tile)) {
				exists = true;
				break;
			}
		}
		return exists;
	}	
	
	public void deleteTestTile(TestTile testTile) {
		new DeleteTestTile(testTile);
	}
	
	private class DeleteTestTile extends Thread {
		private TestTile testTile;
		
		private DeleteTestTile(TestTile testTile) {
			this.testTile = testTile;
			start();
		}

		@Override
		public void run() {
			try {
				String sql = "DELETE FROM " + TILE_TABLE_NAME + " WHERE " + 
					"Easting = ? AND " + 
					"Northing = ? AND " +
					"Zone = ? AND " +
					"LatBand = ? ";
		    	
				PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		    	
				preparedStatement.setLong(1, testTile.getEasting());
				preparedStatement.setLong(2, testTile.getNorthing());
				preparedStatement.setInt(3, testTile.getGridZone());
				preparedStatement.setString(4, testTile.getLatBand());
				
		    	preparedStatement.executeUpdate();
		    	
		    	Iterator<TestTile> iter = tileRecordList.iterator();
		    	
		    	while(iter.hasNext()) {
		    		TestTile testTile = iter.next();
		    		if (testTile.getID() == this.testTile.getID()) iter.remove();
		    	}

	    		pcs.firePropertyChange(TILE_DELETED, null, testTile.clone());
		    	
		    	conn.commit();
		    	preparedStatement.close();

			} catch (SQLException ex) {
				reportException(ex);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Object[] getRecordData(final ResultSet resultSet) {
		try {
			int columnCount = resultSet.getMetaData().getColumnCount();
			Object[] obj = new Object[columnCount];
			for (int i = 0; i < obj.length; i++) {
				obj[i] = resultSet.getObject(i + 1);
			}
			return obj;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void requestAllTileRecords() {
		new RetrieveAllTileRecords();
	}
	
	public void requestAllStaticRecords() {
		new RetrieveAllStaticRecords();
	}
	
	public List<StaticMeasurement> getStaticRecordList() {
		if (!staticListReady) return null;
		return staticList;
	}
	
	public StaticMeasurement getStaticRecord(int index) {
		if (!staticListReady) return null;
		return staticList.get(index);
	}
	
	private class RetrieveAllStaticRecords extends Thread {
		
		private int numberTilesRetrieved = 0;
		
		private RetrieveAllStaticRecords() {
			this.start();
		}

		@Override
		public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String sql = "SELECT * FROM " + STATIC_TABLE_NAME;
					
					staticList = new ArrayList<StaticMeasurement>();

					PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
					
					ResultSet staticDataSet = preparedStatement.executeQuery();

		        	while(staticDataSet.next()) {
		        		numberTilesRetrieved++;
						int progress = 30 + (int) ((numberTilesRetrieved / (double) tileRecordList.size()) * 70);
						
						pcs.firePropertyChange(STATIC_RETRIEVAL_PROGRESS, null, progress);

		        		staticList.add(StaticMeasurement.objectArrayToStaticMeasurement(getRecordData(staticDataSet).clone()));
		        	}

		        	staticListReady = true;
		        	
		        	pcs.firePropertyChange(ALL_STATIC_RECORDS_READY, null, staticList);

		        	conn.commit();
		        	preparedStatement.close();
		        	staticDataSet.close();
		        	
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	public List<TestTile> getTileRecordList() {
		if (!tileRecordListReady) return null;
		return tileRecordList;
	}
	
	public TestTile getTileRecord(TestTile testTile) { 
		if (tileRecordList == null) return null;
		TestTile tile = null;
		for (TestTile t : tileRecordList) {
			if (t.equals(testTile)) {
				tile = t;
			}
		}
		return tile;
	}
	
	public TestTile getTileRecord(int index) {
		if (!tileRecordListReady) return null;
		return tileRecordList.get(index);
	}
	
	private class RetrieveAllTileRecords extends Thread {
		
		private int numberTilesRetrieved = 0;
		private int tilesCompleteCount = 0;
		
		private RetrieveAllTileRecords() {
			this.start();
		}

		@Override
		public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String tileSql = "SELECT * FROM " + TILE_TABLE_NAME;
					
					tileRecordList = new ArrayList<TestTile>();

					PreparedStatement psTile = conn.prepareStatement(tileSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
					
					ResultSet tileDataSet = psTile.executeQuery();

		        	while(tileDataSet.next()) {
		        		numberTilesRetrieved++;
						int progress = (int) ((numberTilesRetrieved / (double) tileRecordList.size()) * 100);
						
						pcs.firePropertyChange(TILE_RETRIEVAL_PROGRESS, null, progress);
		        		
						TestTile testTile = TestTile.toTestTile(getRecordData(tileDataSet).clone());

		        		if (testTile.getMeasurementCount() >= testSettings.getMinimumSamplesPerTile()) tilesCompleteCount++;

		        		String dataSql = "SELECT * FROM " + MEASUREMENT_TABLE + " WHERE TestTileID = ?";

		        		PreparedStatement psMeasurement = conn.prepareStatement(dataSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		        		
		        		psMeasurement.setInt(1, testTile.getID());
		        		
		        		ResultSet coverageDataSet = psMeasurement.executeQuery();
		        		
		        		while(coverageDataSet.next()) {
		        			CoverageTestMeasurement ctm = CoverageTestMeasurement.fromObjectArray(getRecordData(coverageDataSet));
		        			pcs.firePropertyChange(COVERAGE_TEST_DATA_READY, null, ctm.clone());
		        		}

		        		coverageDataSet.close();
		        		psMeasurement.close();
		        		
		        		tileRecordList.add((TestTile) testTile.clone());
		        	}
		        	
		        	tileRecordListReady = true;

		        	pcs.firePropertyChange(TILE_COMPLETE_COUNT_READY, null, tilesCompleteCount);
					pcs.firePropertyChange(ALL_TILE_RECORDS_READY, null, tileRecordList);

		        	conn.commit();
		        	tileDataSet.close();
		        	psTile.close();
		        	
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (SQLException ex) {
					
					reportException(ex);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private class RetrieveTileRecord extends Thread {
		private String sql;
		private Row row;

		private RetrieveTileRecord(final String sql) {
			this(sql, Row.FIRST);
		}	
		
		private RetrieveTileRecord(final String sql, final Row row) {
			this.sql = sql;
			this.row = row;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}

					boolean found = false;
		        	
			    	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			    	
			    	tileDataSet = preparedStatement.executeQuery();
					
			    	switch(row) {
			        	case FIRST : found = tileDataSet.first(); break; 
			        	case NEXT : found = tileDataSet.next(); break; 
			        	case LAST : found = tileDataSet.last(); break; 
		        	}

					if (found) {
						Object[] obj = getRecordData(tileDataSet);
						
						TestTile testTile = TestTile.toTestTile(obj);
						
						pcs.firePropertyChange(TILE_READY, null, testTile);
					} else {
						pcs.firePropertyChange(TILE_NOT_FOUND, null, null);
					}
					
					conn.commit();
					preparedStatement.close();
					
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	private class RetrieveCoverageTestRecord extends Thread {
		private String sql;
		private Row row;
		private CoverageTestMeasurement coverageTestData;
		
		private RetrieveCoverageTestRecord(final String sql, CoverageTestMeasurement coverageTestData) {
			this.coverageTestData = coverageTestData;
			this.sql = sql;
			row = Row.FIRST;
			start();
		}
		
		private RetrieveCoverageTestRecord(final String sql) {
			this(sql, Row.FIRST);
		}	
		
		private RetrieveCoverageTestRecord(final String sql, final Row row) {
			this.sql = sql;
			this.row = row;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					Object[] obj;
					boolean found = false;
		        	
			    	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			    	coverageDataSet = preparedStatement.executeQuery();
					
			    	switch(row) {
			        	case FIRST : found = coverageDataSet.first(); break; 
			        	case NEXT : found = coverageDataSet.next(); break; 
			        	case LAST : found = coverageDataSet.last(); break; 
		        	}

					if (found) {
						obj = getRecordData(coverageDataSet);
						pcs.firePropertyChange(COVERAGE_TEST_DATA_READY, null, obj);
					} else {
						pcs.firePropertyChange(COVERAGE_DATA_NOT_FOUND, null, coverageTestData);
					}
					
					conn.commit();
					preparedStatement.close();
					
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	private class RetrieveStaticTestRecord extends Thread {
		private String sql;
		private Row row;
		private StaticMeasurement staticMeasurementData;
		
		private RetrieveStaticTestRecord(final String sql, StaticMeasurement staticMeasurementData) {
			this.staticMeasurementData = staticMeasurementData;
			this.sql = sql;
			row = Row.FIRST;
			start();
		}
		
		private RetrieveStaticTestRecord(final String sql) {
			this(sql, Row.FIRST);
		}	
		
		private RetrieveStaticTestRecord(final String sql, final Row row) {
			this.sql = sql;
			this.row = row;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					Object[] obj;
					boolean found = false;
		        	
			    	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			    	staticDataSet = preparedStatement.executeQuery();
					
			    	switch(row) {
			        	case FIRST : found = staticDataSet.first(); break; 
			        	case NEXT : found = staticDataSet.next(); break; 
			        	case LAST : found = staticDataSet.last(); break; 
		        	}

					if (found) {
						obj = getRecordData(staticDataSet);
						pcs.firePropertyChange(STATIC_TEST_DATA_READY, null, obj);
					} else {
						pcs.firePropertyChange(STATIC_DATA_NOT_FOUND, null, staticMeasurementData);
					}
					
					conn.commit();
					preparedStatement.close();
					
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	public void requestTestSettings() {
		new RetrieveTestSettings(); 
	}
	
	private class RetrieveTestSettings extends Thread {
		
		private RetrieveTestSettings() {
			start();
		}

		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}

					String sql = "SELECT * FROM " + SETTINGS_TABLE_NAME;
					
			    	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			    	settingsDataSet = preparedStatement.executeQuery();
			    	
					if (settingsDataSet.next()) {

						Object[] obj = getRecordData(settingsDataSet);
						
						testSettings = (TestSettings) TestSettings.toTestSettings(obj).clone();

					}
					
					pcs.firePropertyChange(SETTINGS_READY, null, testSettings);
					
					conn.commit();
					preparedStatement.close();
					
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void UpdateTileRecord(TestTile testTile) {
		new UpdateTileRecord(testTile);
	}
	
	private class UpdateTileRecord extends Thread {
		private TestTile testTile;
		
		private UpdateTileRecord(TestTile testTile) {
			this.testTile = testTile;
			start();
		}	

		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String update = "UPDATE " + TILE_TABLE_NAME + " SET " +
						    "ID = ?, " +
							"Easting = ?, " +
							"Northing = ?, " +
							"Zone = ?, " +
							"Longitude = ?, " +
							"Latitude = ?, " +
							"Precision = ?, " +
							"LatBand = ?, " +
							"AvgSinad = ?, " +
							"AvgBer ?, " +
							"AvgRssi ?, " +
							"TileSizeWidth ?, " +
							"TileSizeHeight ?, " +
							"WHERE Easting = ? " + 
							"AND Northing = ? " + 
							"AND Zone = ? " +
							"AND Precison = ? " +
							"AND LatBand = ?" +
							"AND TileSizeHeight BETWEEN ? AND ?";

					PreparedStatement preparedStatement = conn.prepareStatement(update, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					
					preparedStatement.clearParameters();
					
				    preparedStatement.setLong(1, testTile.getID());
				    preparedStatement.setLong(2, testTile.getEasting());
				    preparedStatement.setLong(3, testTile.getNorthing());
				    preparedStatement.setInt(4, testTile.getGridZone());
				    preparedStatement.setDouble(5, testTile.getLonLat().x);
				    preparedStatement.setDouble(6, testTile.getLonLat().y);
					preparedStatement.setInt(7, testTile.getPrecision().ordinal());
				    preparedStatement.setString(8, testTile.getLatBand());
				    preparedStatement.setDouble(9, testTile.getAvgSinad());
				    preparedStatement.setDouble(10, testTile.getAvgBer());
				    preparedStatement.setDouble(11, testTile.getAvgdBm());
				    preparedStatement.setDouble(12, testTile.getTileSize().x);
				    preparedStatement.setDouble(13, testTile.getTileSize().y);
				    preparedStatement.setLong(14, testTile.getEasting());
				    preparedStatement.setLong(15, testTile.getNorthing());
				    preparedStatement.setInt(16, testTile.getGridZone());
				    preparedStatement.setInt(17, testTile.getPrecision().ordinal());
				    preparedStatement.setString(18, testTile.getLatBand());
				    preparedStatement.setDouble(19, testTile.getTileSize().y - 2);
				    preparedStatement.setDouble(20, testTile.getTileSize().y + 2);
				    
					preparedStatement.executeUpdate();
					
					conn.commit();
					preparedStatement.close();
					
					currentTestTile = testTile;
					pcs.firePropertyChange(TILE_UPDATED, null, testTile);
					
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	public void UpdateTestSettings(TestSettings testSettings) {
		new UpdateTestSettings(testSettings);
	}
	
	private class UpdateTestSettings extends Thread {
		private TestSettings testSettings;
		
		private UpdateTestSettings(TestSettings testSettings) {
			this.testSettings = testSettings;
			start();
		}	

		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String update = "UPDATE " + SETTINGS_TABLE_NAME + " SET " +
							"ID = ?, " +
						    "MinimumTimePerTile = ?, " +
							"SampleTimingMode = ?, " +
							"TileSizeWidth = ?, " +
							"TileSizeHeight = ?, " +
							"MaximumSamplesPerTile = ?, " +
							"MinimmumSamplesPerTile = ?, " +
							"GridReferenceLeft = ?, " +
							"GridReferenceTop = ?, " +
							"GridWidth = ?, " +
							"GridHeight = ?, " +
							"DotsPerTile = ? " +
							"WHERE ID = 1";
					
					PreparedStatement preparedStatement = conn.prepareStatement(update);
					
					preparedStatement.clearParameters();
					
				    preparedStatement.setLong(1, testSettings.getId());
				    preparedStatement.setInt(2, testSettings.getMinimumTimePerTile());
				    preparedStatement.setInt(3, testSettings.getSampleTimingMode());
				    preparedStatement.setDouble(4, testSettings.getTileSize().x);
				    preparedStatement.setDouble(5, testSettings.getTileSize().y);
				    preparedStatement.setInt(6, testSettings.getMaximumSamplesPerTile());
					preparedStatement.setInt(7, testSettings.getMinimumSamplesPerTile());
					preparedStatement.setDouble(8, testSettings.getGridReference().x);
				    preparedStatement.setDouble(9, testSettings.getGridReference().y);
				    preparedStatement.setDouble(10, testSettings.getGridSize().x);
				    preparedStatement.setDouble(11, testSettings.getGridSize().y);
				    preparedStatement.setInt(12, testSettings.getDotsPerTile());
				    
					preparedStatement.executeUpdate();
					
					conn.commit();
					preparedStatement.close();
					
					pcs.firePropertyChange(SETTINGS_UPDATED, null, testSettings);
					
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	public boolean isClosed() {
		boolean closed = false;
		try {
			if (conn == null || conn.isClosed()) closed = true;
		} catch (SQLException ex) {
			reportException(ex);
		}
		return closed;
	}
	
	public void close() {
		try {
			if(coverageDataSet != null) coverageDataSet.close();
			if(staticDataSet != null) staticDataSet.close();
			if(settingsDataSet != null) settingsDataSet.close();
			if(tileDataSet != null) tileDataSet.close();
			if (conn != null) {
				conn.close();
			}
			if (fh != null) fh.flush();
			if (fh != null) fh.close();
			
			pcs.firePropertyChange(DATABASE_CLOSED, null, null);

		} catch (final SQLException ex) {
			reportException(ex);
    	}
	}

	public int getRecordCount(final String tableName) {
		if(tableName.contains(MEASUREMENT_TABLE)) {
			return coverageRecordCount;
		}
		if(tableName.contains(STATIC_TABLE_NAME)) {
			if (staticList == null) return 0;
			else return staticRecordCount;
		}
		if(tableName.contains(TILE_TABLE_NAME)) {
			if (tileRecordList == null) return 0;
			else return tileRecordList.size();
		}
		if(tableName.contains(SETTINGS_TABLE_NAME)) {
			return settingsRecordCount;
		}
		return 0;
	}

	public void requestUpdatedRecordCount(final String tableName) {
		new TableStats(tableName);
	}

	private class TableStats extends Thread {
		private String tableName;
		
		private TableStats(final String tableName) {
			this.tableName = tableName;
			start();
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String sql = "SELECT COUNT(*) AS recordCount FROM " + tableName;
					PreparedStatement preparedStatement = conn.prepareStatement(sql);
			    	preparedStatement.setFetchSize(1);
					ResultSet resultSet = preparedStatement.executeQuery();
			    	resultSet.next();
			    	int count = new Integer(resultSet.getInt("recordCount"));
					
			    	if (tableName.contains(MEASUREMENT_TABLE)) {
						coverageRecordCount = count;
						pcs.firePropertyChange(COVERAGE_RECORD_COUNT_READY, null, count);
					}
					
					if (tableName.contains(STATIC_TABLE_NAME)) {
						staticRecordCount = count;
						pcs.firePropertyChange(STATIC_RECORD_COUNT_READY, null, count);
					}
					
					if (tableName.contains(TILE_TABLE_NAME)) {
						pcs.firePropertyChange(TILE_RECORD_COUNT_READY, null, count);
					}
					
					if (tableName.contains(SETTINGS_TABLE_NAME)) {
						settingsRecordCount = count;
						pcs.firePropertyChange(SETTINGS_RECORD_COUNT_READY, null, count);
					}
					
					conn.commit();
					preparedStatement.close();
					resultSet.close();
					
				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	public void save() {
		try {
			if (conn != null) conn.commit();
		} catch (SQLException ex) {
			reportException(ex);
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!pcs.hasListeners(null)) {
			pcs.addPropertyChangeListener(listener);
		}
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    private void reportException(final InterruptedException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "InterruptedException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                	ex.getMessage(), "Thread Interrupted Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final IOException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "IOException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                	ex.getMessage(), "Input / Output Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

    private void reportException(final SQLException ex) {
    	ex.printStackTrace();
    	if (sqlExceptionQueued) return;
    	sqlExceptionQueued = true;
    	log.log(Level.WARNING, "SQLException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                	ex.getMessage(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
                sqlExceptionQueued = false;
            }
        });
	}
    
    private void reportException(final NullPointerException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "NullPointerException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                	ex.getLocalizedMessage(), "Null Pointer Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

    private void reportException(final SecurityException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "SecurityException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                	ex.getMessage(), "Security Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

    private void reportException(final IllegalArgumentException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "IllegalArgumentException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                	ex.getMessage(), "Illegal Argument Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private static String getSettingsTableDef() {
		
		String tableDef = "CREATE TABLE " + SETTINGS_TABLE_NAME + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"MinimumTimePerTile INTEGER, " +
			"SampleTimingMode SMALLINT, " +
			"TileSizeWidth DOUBLE, " +
			"TileSizeHeight DOUBLE, " +
			"MaximumSamplesPerTile INTEGER, " +
			"MinimumSamplesPerTile INTEGER, " +
			"GridReferenceLeft DOUBLE, " +
			"GridReferenceTop DOUBLE, " +
			"GridSizeWidth DOUBLE, " +
			"GridSizeHeight DOUBLE, " +
			"DotsPerTile SMALLINT" +
			")";

		return tableDef;
	}
    
    private static String getCoverageTableDef() {
		String scanString = "";

		for (int i = 0; i < 10; i++) {
			scanString += "BER_F" + String.valueOf(i) + " DOUBLE, " +
			"RSSI_F" + String.valueOf(i) + " DOUBLE, " +
			"SINAD_F" + String.valueOf(i) + " DOUBLE, " +
			"RX_F" + String.valueOf(i) + " DOUBLE, " +
			"SCAN_SELECT_F" + String.valueOf(i) + " BOOLEAN, ";
		}
		
		String tableDef = "CREATE TABLE " + MEASUREMENT_TABLE + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
		
		   	"Sentence VARCHAR(128), " + 
			"Millis BIGINT, " +
			
			scanString +
			
			"TestTileID INTEGER, " +
			"Longitude DOUBLE, " +
			"Latitude DOUBLE, " +
			"DopplerDirection DOUBLE, " +
			"DopplerQuality SMALLINT, " +
			"Marker SMALLINT)";

		return tableDef;
	}
 
    private static String getTileTableDef() {
    	String def = "CREATE TABLE " + TILE_TABLE_NAME + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"Easting BIGINT, " +
			"Northing BIGINT, " +
			"Zone SMALLINT, " +
			"Longitude DOUBLE, " +
			"Latitude DOUBLE, " +
			"Precision SMALLINT, " +
			"LatBand VARCHAR(1), " +
			"AvgSinad DOUBLE, " +
			"AvgBer DOUBLE, " +
			"AvgRssi DOUBLE, " +
			"TileSizeWidth DOUBLE, " +
			"TileSizeHeight DOUBLE, " +
			"MeasurementCount INTEGER" +
			")";
    	return def;
    }
    
    private static String getTileTableIndexDef() {
    	String def = "CREATE INDEX TileIndex ON " + TILE_TABLE_NAME + " " +
			"(Easting ASC, " +
			"Northing ASC, " +
			"Zone ASC, " +
			"LatBand ASC)";
    	return def;
	}
    
	private static String getStaticTableDef() {
		String  def = "CREATE TABLE " + STATIC_TABLE_NAME + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
			"TimeStamp BIGINT," +
			"FlightLongitude DOUBLE," +
			"FlightLatitude DOUBLE," +
			"CourseMadeGoodTrue DOUBLE," +
			"SpeedMadeGoodKPH DOUBLE," +
			"Altitude DOUBLE," +
			"FrequencyMHz DOUBLE," +
			"Unit SMALLINT)";
		return def;
	}
	
	private static String getStaticTableIndexDef() {
		String def = "CREATE INDEX StaticMeasurementTestDataIndex ON " + STATIC_TABLE_NAME + " " +
			"(TimeStamp DESC, " +
			"Unit ASC)"; 
		return def;
	}
	
	public static void printResultSetToConsole(ResultSet rs) {
		System.out.println("---------------------------------------------------------");
		System.out.println("Start ResultSet");
		int columnCount;
		try {
			columnCount = rs.getMetaData().getColumnCount();
			Object[] obj = new Object[columnCount];
			for (int i = 0; i < obj.length; i++) {
				obj[i] = rs.getObject(i + 1);
				System.out.println(i + " = " + obj[i]);
			}
			System.out.println("End ResultSet");
			System.out.println("---------------------------------------------------------");
		} catch (SQLException e) {
		}
	}
	
	public static void printObjectArrayToConsole(Object[] obj) {
		System.out.println("---------------------------------------------------------");
		System.out.println("Start Object Array");
		for (int i = 0; i < obj.length; i++) {
			System.out.println(i + " = " + obj[i]);
		}
		System.out.println("End Object Array");
		System.out.println("---------------------------------------------------------");
	}
}
