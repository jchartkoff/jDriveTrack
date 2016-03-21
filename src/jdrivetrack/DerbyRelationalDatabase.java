package jdrivetrack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import types.Measurement;
import types.MeasurementSet;
import types.StaticMeasurement;
import types.TestSettings;
import types.TestTile;

public class DerbyRelationalDatabase {
	public static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String DERBY_URL = "jdbc:derby:";
	
	public static final String DATABASE_OPEN = "DATABASE_OPEN";
	public static final String DATABASE_CLOSED = "DATABASE_CLOSED";
	public static final String DATABASE_CREATION_ERROR = "DATABASE_CREATION_ERROR";
	public static final String DATABASE_RESTORE_PROGRESS = "DATABASE_RESTORE_PROGRESS";
	
	public static final String TILE_TABLE = "TILE_TABLE";
	public static final String MEASUREMENT_TABLE = "MEASUREMENT_TABLE";
	public static final String MEASUREMENT_SET_TABLE = "MEASUREMENT_SET_TABLE";
	public static final String STATIC_TABLE = "STATIC_TABLE";
	public static final String TEST_TABLE = "TEST_TABLE";
	
	public static final String TILE_TABLE_APPENDED = "TILE_TABLE_APPENDED";
	public static final String MEASUREMENT_SET_APPENDED = "MEASUREMENT_SET_APPENDED";
	public static final String MEASUREMENT_APPENDED = "MEASUREMENT_APPENDED";
	public static final String STATIC_MEASUREMENT_APPENDED = "STATIC_MEASUREMENT_APPENDED";
	
	public static final String ALL_COVERAGE_RECORDS_READY = "ALL_COVERAGE_RECORDS_READY";
	public static final String ALL_TILE_RECORDS_READY = "ALL_TILE_RECORDS_READY";
	public static final String ALL_STATIC_RECORDS_READY = "ALL_STATIC_RECORDS_READY";
	
	public static final String SETTINGS_READY = "SETTINGS_READY";
	public static final String TILE_READY = "TILE_READY";
	public static final String TILE_NOT_FOUND = "TILE_NOT_FOUND";
	public static final String MEASUREMENT_SET_READY = "MEASUREMENT_SET_READY";
	public static final String MEASUREMENT_READY = "MEASUREMENT_READY";
	public static final String STATIC_MEASUREMENT_RECORD_READY = "STATIC_MEASUREMENT_RECORD_READY";
	
	public static final String TILE_DELETED = "TILE_DELETED";
	
	public static final String STATIC_MEASUREMENT_COUNT_READY = "STATIC_MEASUREMENT_COUNT_READY";
	public static final String TILE_COMPLETE_COUNT_READY = "TILE_COMPLETE_COUNT_READY";
	public static final String TILE_COUNT_READY = "TILE_COUNT_READY";
	public static final String MEASUREMENT_SET_RECORD_COUNT_READY = "MEASUREMENT_SET_RECORD_COUNT_READY";
	
	public static final String TILE_RETRIEVAL_PROGRESS = "TILE_RETRIEVAL_PROGRESS";
	public static final String STATIC_RETRIEVAL_PROGRESS = "STATIC_RETRIEVAL_PROGRESS";
	
	private static final String DEFAULT_LOG_FILE_DIR = System.getProperty("user.home") + 
	    	File.separator + "drivetrack" + File.separator + "database event log";

	private static final Logger log = Logger.getLogger(Database.class.getName());

	private volatile Connection conn;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private FileHandler fh;
	private volatile boolean databaseCreated = false;
	private List<StaticMeasurement> staticRecordList = new ArrayList<StaticMeasurement>();
	private List<TestTile> tileRecordList = new ArrayList<TestTile>();
	private int measurementSetCount = 0;
	
	private Runnable database;
    
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public DerbyRelationalDatabase() {
		configureEventLog();
	}
    
	public DerbyRelationalDatabase(final File databasePath, final String userName, final String password) {
		configureEventLog();
		Runnable database = new Database(databasePath, userName, password);
		executor.execute(database);
	}
	
	public void openDatabase(final File databasePath, final String userName, final String password) {
		Runnable database = new Database(databasePath, userName, password);
		executor.execute(database);
	}
	
	public void openDatabase(final File databasePath, final String userName, final String password, 
			final TestSettings testSettings) {
		database = new Database(databasePath, userName, password, testSettings);
		executor.execute(database);
	}

	private class Database implements Runnable {
		private File databaseFile;
		private String username;
		private String password;
		private TestSettings testSettings;

		private Database(final File databaseFile, final String username, final String password) {
			this.databaseFile = databaseFile;
			this.username = username;
			this.password = password;
		}
		
		private Database(final File databaseFile, final String username, final String password, TestSettings testSettings) {
			this.databaseFile = databaseFile;
			this.username = username;
			this.password = password;
			this.testSettings = testSettings;
		}
		
		@Override
		public void run() {
			synchronized(this) {
				try {
					String url = DERBY_URL + databaseFile.getPath() + ";create=true";
					url = url.replace("\\", "/");
				    conn = DriverManager.getConnection(url, username, password);
				    conn.setAutoCommit(true);
				    
				    if (testSettings != null) {
						createTables(conn);
						String testName = databaseFile.getName();
						testSettings.setTestName(testName);
						createTestRecord(testSettings);
					}
				    
				    databaseCreated = true;
				    this.notifyAll();
				    
				    pcs.firePropertyChange(DATABASE_OPEN, null, null);
				    
				    TestSettings testSettings = getTestSettings();
				    requestAllDataRecords(testSettings);
				    
				} catch (SQLException ex) {
					pcs.firePropertyChange(DATABASE_CREATION_ERROR, null, null);
					reportException(ex);
				} catch (NullPointerException ex) {
					pcs.firePropertyChange(DATABASE_CREATION_ERROR, null, null);
					reportException(ex);
				} catch (CloneNotSupportedException ex) {
					pcs.firePropertyChange(DATABASE_CREATION_ERROR, null, null);
					reportException(ex);
				}
			}
		}
	}
	
	public void deleteTestTile(TestTile testTile) {
		executor.execute(new DeleteTestTile(testTile));
	}
	
	private class DeleteTestTile implements Runnable {
		private TestTile testTile;
		
		private DeleteTestTile(TestTile testTile) {
			this.testTile = testTile;
		}

		@Override
		public void run() {
			try {
				String sql = "DELETE FROM " + TILE_TABLE + " WHERE " + 
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
		    	
		    	preparedStatement.close();

			} catch (SQLException ex) {
				reportException(ex);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<TestTile> getTileRecordList() {
		return tileRecordList;
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
	
	public TestTile testTileExists(TestTile testTile) {
		if (tileRecordList == null) return null;
		TestTile ret = null;
		for (TestTile tile : tileRecordList) {
			if (testTile.equals(tile)) {
				ret = tile;
				break;
			}
		}
		return ret;
	}
	
	public void requestAllDataRecords(final TestSettings testSettings) {
		executor.execute(new RetrieveAllTestTileRecords(testSettings));
	}
	
	private class RetrieveAllTestTileRecords implements Runnable {
		private TestSettings testSettings = null;
		private int numberTilesComplete = 0;
		
		private RetrieveAllTestTileRecords(TestSettings testSettings) {
			this.testSettings = testSettings;
		}
		
		@Override
		public void run() {
			PreparedStatement preparedStatementData = null;
			PreparedStatement preparedStatementCount = null;
			ResultSet testTileResultSet = null;
			ResultSet tileCountResultSet = null;
			TestTile testTileDataType = null;
			int count = 0;
			int numberTilesRetrieved = 0;
			
			try {
				
				String sqlCount = "SELECT COUNT(*) AS recordCount FROM " + TILE_TABLE;
				preparedStatementCount = conn.prepareStatement(sqlCount);
				tileCountResultSet = preparedStatementCount.executeQuery();
		    	
				if (tileCountResultSet.next()) {
					count = new Integer(tileCountResultSet.getInt("recordCount"));
					pcs.firePropertyChange(TILE_COUNT_READY, null, count);	
				}
				
				String sql = "SELECT * FROM " + TILE_TABLE + " " +
					"INNER JOIN " + TEST_TABLE + " ON " + TEST_TABLE + ".TestName = " + TILE_TABLE + ".TestName " +
					"WHERE " + TILE_TABLE + ".TestName = ?";
				
				preparedStatementData = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				preparedStatementData.setString(1, testSettings.getTestName());
				
				testTileResultSet = preparedStatementData.executeQuery();
				
				while(testTileResultSet.next()) {
					testTileDataType = TestTile.toTestTile(getRecordData(testTileResultSet));
					pcs.firePropertyChange(TILE_READY, null, testTileDataType.clone());
					if (testTileDataType.getMeasurementCount() >= testSettings.getMinimumSamplesPerTile()) numberTilesComplete++;
					requestAllMeasurementSetRecords(testTileDataType);
					numberTilesRetrieved++;
					int progress = (int) ((numberTilesRetrieved / (double) count * 100));
					pcs.firePropertyChange(TILE_RETRIEVAL_PROGRESS, null, progress);
					tileRecordList.add((TestTile) testTileDataType.clone());
				}
				
				pcs.firePropertyChange(ALL_TILE_RECORDS_READY, null, tileRecordList);
				
				pcs.firePropertyChange(TILE_COMPLETE_COUNT_READY, null, numberTilesComplete);
				
		    } catch (SQLException ex) {
				reportException(ex);
			} catch (CloneNotSupportedException ex) {
				reportException(ex);
			} finally {
				try {
					tileCountResultSet.close();
					testTileResultSet.close();
					preparedStatementData.close();
					preparedStatementCount.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			} 
		}		
	}
	
	public void requestAllMeasurementSetRecords(final TestTile testTile) {
		executor.execute(new RetrieveAllMeasurementSetRecords(testTile));
	}
	
	private class RetrieveAllMeasurementSetRecords implements Runnable {
		private TestTile testTile = null;
		
		private RetrieveAllMeasurementSetRecords(TestTile testTile) {
			this.testTile = testTile;
		}
		
		@Override
		public void run() {
			PreparedStatement preparedStatement = null;
			ResultSet measurementSetResultSet = null;
			MeasurementSet measurementSetDataType = null;
			
			try {
				String sql = "SELECT * FROM " + MEASUREMENT_SET_TABLE + " " +
						"INNER JOIN " + TILE_TABLE + " ON " + TILE_TABLE + ".ID = " + MEASUREMENT_SET_TABLE + ".TestTileID " +
						"WHERE " + MEASUREMENT_SET_TABLE + ".TestTileID = ?";
				
				preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				preparedStatement.setInt(1, testTile.getID());
				
				measurementSetResultSet = preparedStatement.executeQuery();
				
				while(measurementSetResultSet.next()) {
					measurementSetDataType = MeasurementSet.toMeasurementSet(getRecordData(measurementSetResultSet));
					pcs.firePropertyChange(MEASUREMENT_SET_READY, null, measurementSetDataType.clone());
					measurementSetCount++;
					requestAllMeasurementRecords(measurementSetDataType);
				}
				
				pcs.firePropertyChange(MEASUREMENT_SET_RECORD_COUNT_READY, null, measurementSetCount);
				
		    } catch (SQLException ex) {
				reportException(ex);
			} catch (CloneNotSupportedException ex) {
				reportException(ex);
			} finally {
				try {
					measurementSetResultSet.close();
					preparedStatement.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			} 
		}		
	}
	
	public int getMeasurementSetCount() {
		return measurementSetCount;
	}
	
	public void requestAllArcRecords() {
		executor.execute(new RetrieveAllArcTileRecords());
	}
	
	private class RetrieveAllArcTileRecords implements Runnable {

		@Override
		public void run() {
			PreparedStatement preparedStatement = null;
			PreparedStatement preparedStatementCount = null;
			ResultSet arcResultSet = null;
			ResultSet arcResultSetCount = null;
			int count = 0;
			int numberRecordsRetrieved = 0;
			
			try {
				String sqlCount = "SELECT COUNT(*) AS recordCount FROM " + STATIC_TABLE;
				preparedStatementCount = conn.prepareStatement(sqlCount);
				arcResultSetCount = preparedStatementCount.executeQuery();
		    	
				if (arcResultSetCount.next()) {
					count = new Integer(arcResultSetCount.getInt("recordCount"));
					pcs.firePropertyChange(STATIC_MEASUREMENT_COUNT_READY, null, count);	
				}

				String sql = "SELECT * FROM " + STATIC_TABLE;
				
				preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				
				arcResultSet = preparedStatement.executeQuery();
				
				while(arcResultSet.next()) {
					StaticMeasurement arcDataType = StaticMeasurement.objectArrayToStaticMeasurement(getRecordData(arcResultSet));
					pcs.firePropertyChange(STATIC_MEASUREMENT_RECORD_READY, null, arcDataType.clone());
					numberRecordsRetrieved++;
					int progress = (int) ((numberRecordsRetrieved / (double) count * 100));
					pcs.firePropertyChange(TILE_RETRIEVAL_PROGRESS, null, progress);
					staticRecordList.add((StaticMeasurement) arcDataType.clone());
				}
				
				pcs.firePropertyChange(ALL_STATIC_RECORDS_READY, null, staticRecordList);
				
		    } catch (SQLException ex) {
				reportException(ex);
			} catch (CloneNotSupportedException ex) {
				reportException(ex);
			} finally {
				try {
					arcResultSet.close();
					arcResultSetCount.close();
					preparedStatement.close();
					preparedStatementCount.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			} 
		}		
	}
	
	public void requestAllMeasurementRecords(final MeasurementSet measurementSet) {
		executor.execute(new RetrieveAllMeasurementRecords(measurementSet));
	}
	
	private class RetrieveAllMeasurementRecords implements Runnable {
		private MeasurementSet measurementSet = null;
		
		private RetrieveAllMeasurementRecords(MeasurementSet measurementSet) {
			this.measurementSet = measurementSet;
		}
		
		@Override
		public void run() {
			PreparedStatement preparedStatement = null;
			ResultSet measurementResultSet = null;
			Measurement measurementDataType = null;
			
			try {
				String sql = "SELECT * FROM " + MEASUREMENT_TABLE + " " +
						"INNER JOIN " + MEASUREMENT_SET_TABLE + " ON " + MEASUREMENT_SET_TABLE + ".ID = " + MEASUREMENT_TABLE + ".MeasurementSetID " +
						"WHERE " + MEASUREMENT_TABLE + ".TestTileID = ?";
				
				preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				preparedStatement.setInt(1, measurementSet.getId());
				
				measurementResultSet = preparedStatement.executeQuery();
				
				while(measurementResultSet.next()) {
					measurementDataType = Measurement.toMeasurement(getRecordData(measurementResultSet));
					pcs.firePropertyChange(MEASUREMENT_READY, null, measurementDataType.clone());
				}
				
		    } catch (SQLException ex) {
				reportException(ex);
			} catch (CloneNotSupportedException ex) {
				reportException(ex);
			} finally {
				try {
					measurementResultSet.close();
					preparedStatement.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			} 
		}		
	}
	
	private TestSettings getTestSettings() throws SQLException, CloneNotSupportedException {
		PreparedStatement preparedStatement = null;
		ResultSet testSettingsSet = null;
		TestSettings testSettings = null;
		
		try {
			String sql = "SELECT * FROM " + TEST_TABLE + " WHERE ID = ?";
			
			preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setInt(1, 1);
			
			testSettingsSet = preparedStatement.executeQuery();

			while(testSettingsSet.next()) {
				testSettings = TestSettings.toTestSettings(getRecordData(testSettingsSet));
			}

	    	pcs.firePropertyChange(SETTINGS_READY, null, testSettings.clone());

	    } catch (SQLException ex) {
			reportException(ex);
		} finally {
			testSettingsSet.close();
			preparedStatement.close();
		}
		
		return testSettings;
	}
	
	public void requestTestTile(final TestTile testTile) {
		executor.execute(new RetrieveTestTile(testTile));
	}

	private class RetrieveTestTile implements Runnable {
		
		private TestTile testTile;
		
		private RetrieveTestTile(final TestTile testTile) {
			this.testTile = testTile;
		}
		
		public void run() {
			PreparedStatement preparedStatement = null;
			ResultSet testTileResultSet = null;
			TestTile testTileDataType = null;
			
			try {
				String sql = "SELECT * FROM " + TILE_TABLE + " " +
					"INNER JOIN " + TEST_TABLE + " ON " + TEST_TABLE + ".TestName = " + TILE_TABLE + ".TestName " +
					"WHERE " + TILE_TABLE + ".Easting = ? " +
					"AND " + TILE_TABLE + ".Northing = ? " +
					"AND " + TILE_TABLE + ".Zone = ? " +
					"AND " + TILE_TABLE + ".LatBand = ? ";
				
				preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				preparedStatement.setLong(1, testTile.getEasting());
				preparedStatement.setLong(2, testTile.getNorthing());
				preparedStatement.setInt(3, testTile.getGridZone());
				preparedStatement.setString(4, testTile.getLatBand());
				
				testTileResultSet = preparedStatement.executeQuery();

				if (testTileResultSet.next()) {
					testTileDataType = TestTile.toTestTile(getRecordData(testTileResultSet));
					pcs.firePropertyChange(TILE_READY, null, testTileDataType.clone());
				} else {
					pcs.firePropertyChange(TILE_NOT_FOUND, null, testTile);
				}

		    } catch (SQLException ex) {
				reportException(ex);
			} catch (CloneNotSupportedException ex) {
				reportException(ex);
			} finally {
				try {
					testTileResultSet.close();
					preparedStatement.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public void requestTileCompleteCount(TestSettings testSettings) {
		executor.execute(new RetrieveTileCompleteCount(testSettings));
	}
	
	private class RetrieveTileCompleteCount implements Runnable {
		private TestSettings testSettings = null;
		private int numberTilesComplete = 0;
		private ResultSet tileDataSet;
		private PreparedStatement preparedStatement;
		
		private RetrieveTileCompleteCount(TestSettings testSettings) {
			this.testSettings = testSettings;
		}

		@Override
		public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String tileSql = "SELECT * FROM " + TILE_TABLE;

					preparedStatement = conn.prepareStatement(tileSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

					tileDataSet = preparedStatement.executeQuery();

		        	while(tileDataSet.next()) {
						TestTile testTile = TestTile.toTestTile(getRecordData(tileDataSet));
		        		if (testTile.getMeasurementCount() >= testSettings.getMinimumSamplesPerTile()) numberTilesComplete++;
		        	}
		        	
		        	pcs.firePropertyChange(TILE_COMPLETE_COUNT_READY, null, numberTilesComplete);

				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (SQLException ex) {
					reportException(ex);
				} finally {
					try {
						tileDataSet.close();
						preparedStatement.close();
					} catch (SQLException ex) {
						reportException(ex);
					}
				}
			}
		}
	}
	
	private boolean createTestRecord(TestSettings testSettings) {
		try {
			String values = "?,?,?,?,?,?,?,?,?,?,?,?";
			
			String insert = "INSERT INTO " + TEST_TABLE + "(" +
					"TestName, " +
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
			
			PreparedStatement preparedStatement = conn.prepareStatement(insert);
			
			preparedStatement.clearParameters();
			
			preparedStatement.setString(1, testSettings.getTestName());
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
	
			preparedStatement.close();
	
		} catch (final SQLException ex) {
			reportException(ex);
			return false;
		}
		
		return true;
	}
	
	public void appendTileRecord(TestTile testTile) {
		executor.execute(new AppendTileRecord(testTile));
	}
	
	private class AppendTileRecord implements Runnable {
		private TestTile testTile;
		
		private AppendTileRecord(TestTile testTile) {
			this.testTile = testTile;
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?";
					
					String insert = "INSERT INTO " + TILE_TABLE + " (" +
						"TestName, " +
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
				    
					preparedStatement.setString(1, testTile.getTestName());
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
				    preparedStatement.setInt(14, testTile.getMeasurementCount());
				    
				    preparedStatement.executeUpdate();
				    
				    ResultSet rs = preparedStatement.getGeneratedKeys();
				    
				    if (testTile.getID() == 0) {
					    while (rs.next()) {
					    	testTile.setID(new Integer(rs.getInt(1)));
					    }
				    }
				    
				    tileRecordList.add((TestTile) testTile);
				    
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
	}
	
	public void appendStaticRecord(StaticMeasurement staticMeasurement) {
		executor.execute(new AppendStaticRecord(staticMeasurement));
	}
	
	private class AppendStaticRecord implements Runnable {
		private StaticMeasurement staticMeasurement;
		
		private AppendStaticRecord(StaticMeasurement staticMeasurement) {
			this.staticMeasurement = staticMeasurement;
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}
					
					String values = "?,?,?,?,?,?,?,?,?,?";
					
					String insert = "INSERT INTO " + STATIC_TABLE + " (" +
						"TestName, " +
					    "TimeStamp, " +
						"FlightLongitude, " +
						"FlightLatitude, " +
						"CourseMadeGoodTrue, " +
						"SpeedMadeGoodKPH, " +
						"Altitude, " +
						"FrequencyMHz, " +
						"dBm, " +
						"Unit, " +
						"VALUES (" + values + ")";
					
					PreparedStatement preparedStatement = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);
					
					preparedStatement.clearParameters();
				    
					preparedStatement.setString(1, staticMeasurement.getTestName());
				    preparedStatement.setLong(2, staticMeasurement.getTimeStamp());
				    preparedStatement.setDouble(3, staticMeasurement.getPoint().x);
				    preparedStatement.setDouble(4, staticMeasurement.getPoint().y);
				    preparedStatement.setDouble(5, staticMeasurement.getCourseMadeGoodTrue());
				    preparedStatement.setDouble(6, staticMeasurement.getSpeedMadeGoodKPH());
					preparedStatement.setDouble(7, staticMeasurement.getAltitude());
				    preparedStatement.setDouble(8, staticMeasurement.getFrequencyMHz());
				    preparedStatement.setDouble(9, staticMeasurement.getdBm());
				    preparedStatement.setInt(10, staticMeasurement.getUnit());
				    
				    preparedStatement.executeUpdate();
				    
				    ResultSet rs = preparedStatement.getGeneratedKeys();
				    
				    if (staticMeasurement.getID() == 0) {
					    while (rs.next()) {
					    	staticMeasurement.setID(new Integer(rs.getInt(1)));
					    }
				    }

				    pcs.firePropertyChange(STATIC_MEASUREMENT_APPENDED, null, staticMeasurement);

					rs.close();
					preparedStatement.close();
					
				} catch (final InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}
	
	public void appendMeasurementSet(TestTile testTile, MeasurementSet measurementSet) {
		executor.execute(new AppendMeasurementSetRecord(testTile, measurementSet));
	}
	
	private class AppendMeasurementSetRecord implements Runnable {
		private TestTile testTile;
		private MeasurementSet measurementSet;
		
		private AppendMeasurementSetRecord(TestTile testTile, MeasurementSet measurementSet) {
			this.testTile = testTile;
			this.measurementSet = measurementSet;
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}

					String values = "?,?,?,?,?,?,?";

					String insert = "INSERT INTO " + MEASUREMENT_SET_TABLE + " (" +
					    "TestTileID, " +
					    "Millis, " + 
						"Longitude, " +
						"Latitude, " +
						"DopplerDirection, " +
						"DopplerQuality, " +
						"Marker) " +
						"VALUES (" + values + ")";

					PreparedStatement preparedStatement = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);
					
					preparedStatement.clearParameters();
					
					preparedStatement.setInt(1, testTile.getID());
					preparedStatement.setLong(2, measurementSet.getMillis());
				    preparedStatement.setDouble(3, measurementSet.getPosition().x);
				    preparedStatement.setDouble(4, measurementSet.getPosition().y);
				    preparedStatement.setDouble(5, measurementSet.getDopplerDirection());
				    preparedStatement.setInt(6, measurementSet.getDopplerQuality());
				    preparedStatement.setInt(7, measurementSet.getMarker());
				    
			    	preparedStatement.executeUpdate();
					
			    	ResultSet rs = preparedStatement.getGeneratedKeys();

				    while (rs.next()) {
				    	measurementSet.setId(new Integer(rs.getInt(1)));
				    }
			    	
			    	preparedStatement.close();
			    	rs.close();
			    	
			    	measurementSetCount++;
			    	
			    	pcs.firePropertyChange(MEASUREMENT_SET_RECORD_COUNT_READY, null, measurementSetCount);
			    	pcs.firePropertyChange(MEASUREMENT_SET_APPENDED, null, measurementSet);

				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}

	public void appendMeasurement(MeasurementSet measurementSet, Measurement measurement) {
		executor.execute(new AppendMeasurementRecord(measurementSet, measurement));
	}
	
	private class AppendMeasurementRecord implements Runnable {
		private MeasurementSet measurementSet;
		private Measurement measurement;
		
		private AppendMeasurementRecord(MeasurementSet measurementSet, Measurement measurement) {
			this.measurementSet = measurementSet;
			this.measurement = measurement;
		}
		
		@Override
        public void run() {
			synchronized(database) {
				try {
					while (!databaseCreated) {
						database.wait();
					}

					String values = "?,?,?,?,?,?,?";

					String insert = "INSERT INTO " + MEASUREMENT_TABLE + " (" +
					    "MeasurementSetID, " +
					    "ChannelNumber, " + 
						"BER, " +
						"DBM, " +
						"SINAD, " +
						"FREQUENCY, " +
						"SELECTED) " +
						"VALUES (" + values + ")";

					PreparedStatement preparedStatement = conn.prepareStatement(insert);
					
					preparedStatement.clearParameters();

					preparedStatement.setInt(1, measurementSet.getId());
				    preparedStatement.setInt(2, measurement.getChannelNumber());
				    preparedStatement.setDouble(3, measurement.getBer());
				    preparedStatement.setDouble(4, measurement.getdBm());
				    preparedStatement.setDouble(5, measurement.getSinad());
				    preparedStatement.setDouble(5, measurement.getFrequency());
				    preparedStatement.setBoolean(7, measurement.getSelected());
				    
			    	preparedStatement.executeUpdate();
					
			    	ResultSet rs = preparedStatement.getGeneratedKeys();
			    	
			    	while (rs.next()) {
				    	measurement.setId(new Integer(rs.getInt(1)));
				    }
			    	
			    	preparedStatement.close();
			    	rs.close();

			    	pcs.firePropertyChange(MEASUREMENT_APPENDED, null, measurement);

				} catch (InterruptedException ex) {
					reportException(ex);
				} catch (final SQLException ex) {
					reportException(ex);
				}
			}
		}
	}

	private void createTables(Connection conn) {
		try {
	        PreparedStatement preparedStatement = conn.prepareStatement(getTestTableDef());
	        preparedStatement.executeUpdate();
	        preparedStatement.close();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 20);

	        preparedStatement = conn.prepareStatement(getStaticTableDef());
	        preparedStatement.executeUpdate();
	        preparedStatement.close();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 40);
	        
	        preparedStatement = conn.prepareStatement(getTileTableDef());
	        preparedStatement.executeUpdate();
	        preparedStatement.close();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 60);

	        preparedStatement = conn.prepareStatement(getMeasurementSetTableDef());
	        preparedStatement.executeUpdate();
	        preparedStatement.close();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 80);

	        preparedStatement = conn.prepareStatement(getMeasurementTableDef());
	        preparedStatement.executeUpdate();
	        preparedStatement.close();
	        pcs.firePropertyChange(DATABASE_RESTORE_PROGRESS, null, 100);
	        
	        pcs.firePropertyChange(DATABASE_OPEN, null, null);
	        preparedStatement.close();
	        
		} catch(SQLException ex) {
			reportException(ex);
		}
	}
	
	public List<StaticMeasurement> getStaticRecordList() {
		return staticRecordList;
	}
	
	public void save() {
		try {
			if (conn != null) conn.commit();
		} catch (SQLException ex) {
			reportException(ex);
		}
	}
	
	public void close() {
		try {
			if (conn != null && !conn.isClosed()) conn.commit();
			if (fh != null) fh.flush();
			executor.shutdown();
			pcs.firePropertyChange(DATABASE_CLOSED, null, null);
			removeAllPropertyChangeListeners();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
    		try {
				if (fh != null) fh.close();
	    		if (conn != null && !conn.isClosed()) conn.close();
		        while (!executor.isTerminated()) {}
	        } catch (SQLException e) {
				e.printStackTrace();
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
	
	public void removeAllPropertyChangeListeners() {
        for (PropertyChangeListener listener : pcs.getPropertyChangeListeners()) {
        	pcs.removePropertyChangeListener(listener);
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
    
    private void reportException(final CloneNotSupportedException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "CloneNotSupportedException", ex);
	}
    
    private void reportException(final SQLException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "SQLException", ex);
	}
    
    private void reportException(final NullPointerException ex) {
    	ex.printStackTrace();
    	log.log(Level.WARNING, "NullPointerException", ex);
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

    private static String getTestTableDef() {
		return "CREATE TABLE " + TEST_TABLE + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"TestName VARCHAR(256) NOT NULL UNIQUE, " +
			"MinimumTimePerTile INTEGER NOT NULL, " +
			"SampleTimingMode INTEGER NOT NULL, " +
			"TileSizeWidth DOUBLE NOT NULL, " +
			"TileSizeHeight DOUBLE NOT NULL, " +
			"MaximumSamplesPerTile INTEGER NOT NULL, " +
			"MinimumSamplesPerTile INTEGER NOT NULL, " +
			"GridReferenceLeft DOUBLE NOT NULL, " +
			"GridReferenceTop DOUBLE NOT NULL, " +
			"GridSizeWidth DOUBLE NOT NULL, " +
			"GridSizeHeight DOUBLE NOT NULL, " +
			"DotsPerTile INTEGER NOT NULL" +
			")";
	}
    
    private static final String getMeasurementTableDef() {
    	return "CREATE TABLE " + MEASUREMENT_TABLE + " (ID INTEGER NOT NULL " +
    		"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
    		"MeasurementSetID INTEGER NOT NULL REFERENCES " + MEASUREMENT_SET_TABLE + "(ID), " +
    		"ChannelNumber SMALLINT NOT NULL," +
    		"BER DOUBLE, " +
    		"RSSI DOUBLE, " +
    		"SINAD DOUBLE, " +
			"FREQUENCY DOUBLE NOT NULL, " +
    		"SELECTED BOOLEAN NOT NULL)";
    }
		
	private String getMeasurementSetTableDef() {	
		return "CREATE TABLE " + MEASUREMENT_SET_TABLE + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"TestTileID INTEGER NOT NULL REFERENCES " + TILE_TABLE + "(ID), " + 
			"Millis BIGINT, " +
			"Longitude DOUBLE, " +
			"Latitude DOUBLE, " +
			"DopplerDirection DOUBLE, " +
			"DopplerQuality SMALLINT, " +
			"Marker SMALLINT)";
	}

    private String getTileTableDef() {
    	return "CREATE TABLE " + TILE_TABLE + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"TestName VARCHAR(256) NOT NULL REFERENCES " + TEST_TABLE + "(TestName), " +
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
			"MeasurementCount INTEGER)";
    }

	private static String getStaticTableDef() {
		return "CREATE TABLE " + STATIC_TABLE + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"TestName VARCHAR(256) NOT NULL REFERENCES " + TEST_TABLE + "(TestName), " +
			"TimeStamp BIGINT, " +
			"FlightLongitude DOUBLE, " +
			"FlightLatitude DOUBLE, " +
			"CourseMadeGoodTrue DOUBLE, " +
			"SpeedMadeGoodKPH DOUBLE, " +
			"Altitude DOUBLE, " +
			"FrequencyMHz DOUBLE, " +
			"dBm DOUBLE, " +
			"Unit SMALLINT)";
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
