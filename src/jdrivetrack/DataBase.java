package jdrivetrack;

import java.awt.Component;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class DataBase {
	protected static final String OPEN = "OPEN";
	protected static final String CLOSED = "CLOSED";
	protected static final String APPENDED = "APPENDED";
	protected static final String RECORD_COUNT_READY = "RECORD_COUNT_READY";
	protected static final String DRIVE_TEST_DATA_READY = "DRIVE_TEST_DATA_READY";
	protected static final String STATIC_MEASUREMENT_DATA_READY = "STATIC_MEASUREMENT_DATA_READY";

	protected static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	protected static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	protected static final String MYSQL_URL = "jdbc:mysql://localhost:3306";
	protected static final String DERBY_URL = "jdbc:derby:";
	
	protected static final String USERNAME = "john";
	protected static final String PASSWORD = "password";
	
	protected enum Row {FIRST, NEXT, LAST}
	
	private static final String DEFAULT_USER_HOME_DIR =  System.getProperty("user.home", ".");
	private static final String DEFAULT_LOG_FILE_DIR = "drivetrack" + File.separator + "database_event_log";
	private static final String DEFAULT_DATA_FILE_PATH = "drivetrack" + File.separator + "data_files";
	private static final String TABLE_NAME = "DATA";
	private static final Logger log = Logger.getLogger(Database.class.getName());
	private static final int NUM_SM_COLS = 9;
	private static final int NUM_DT_COLS = 58;
	
	private Object databaseLock = new Object();
	private Object tableLock = new Object();
	
	private Connection conn;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private FileHandler fh;
	private Component parent;
	private ResultSet dataSet;
	private ResultSet measurementSet;
	private volatile boolean databaseCreated = false;
	private volatile boolean tableCreated = false;
	private volatile boolean sqlExceptionQueued = false;
	private int recordCount;
	
	public DataBase(final Component parent, final String databaseName) {
		this(parent, DEFAULT_USER_HOME_DIR + File.separator + DEFAULT_DATA_FILE_PATH, databaseName);
	}
	
	public DataBase(final Component parent, final String dataFilePath, final String databaseName) {
		this.parent = parent;
		configureEventLog();
		setSystemDir(dataFilePath);
		new Database(dataFilePath, databaseName, USERNAME, PASSWORD);
		new TableStats();
	}
	
	public void appendRecord(final DriveTestData dt) {
		try {
			new AppendRecord(dt, new StaticMeasurement());
		} catch (final IOException ex) {
			reportException(ex);
		}
	}
	
	public void appendRecord(final StaticMeasurement sm) {
		try {
			new AppendRecord(new DriveTestData(), sm);
		} catch (final IOException ex) {
			reportException(ex);
		}
	}

	public void appendRecord(final DriveTestData dt, final StaticMeasurement sm) {
		try {
			new AppendRecord(dt, sm);
		} catch (final IOException ex) {
			reportException(ex);
		}
	}
	
	private class AppendRecord extends SwingWorker<Void, Void> {
		private DriveTestData dt;
		private StaticMeasurement sm;
		
		private AppendRecord(final DriveTestData dt, final StaticMeasurement sm) throws IOException {
			this.dt = dt;
			this.sm = sm;
			recordCount++;
			execute();
		}
		
		@Override
        protected Void doInBackground() throws Exception {
			synchronized(tableLock) {
				while (!tableCreated) {
					tableLock.wait();
				}
				
				String scanString = "";
				String values = "NULL, ";
				
				for (int i = 0; i < 10; i++) {
					scanString += "BER_F" + String.valueOf(i) + "," +
					"RSSI_F" + String.valueOf(i) + "," +
					"SINAD_F" + String.valueOf(i) + "," +
					"RX_F" + String.valueOf(i) + ", ";
				}
				
				for (int i = 1; i <= 64; i++) {
					values += "?";
					if (i <= 63) values += ", ";
				}
				
				String insert = "INSERT INTO " + TABLE_NAME + " " +
				    "(Sentence," +
				    "Millis," + scanString +
				    "TestTileMeasuredEasting," +
					"TestTileMeasuredNorthing," +
					"TestTileMeasuredGZD," +
					"TilesTraversed," +
					"MeasurementTimerDelay," +
					"TileIndexPointer," +
					"TileSizeLongitude," +
					"TileSizeLatitude," +
					"MaximumSamplesPerTile," +
					"MinimumSamplesPerTile," +
					"Longitude," +
					"Latitude," +
					"DopplerDirection," +
					"DopplerQuality," +
					"Marker," +
					"TimeStamp," +
					"FlightLongitude," +
					"FlightLatitude," +
					"CourseMadeGoodTrue," +
					"SpeedMadeGoodKPH," +
					"Altitude," +
					"FrequencyMHz," +
					"Unit) " +
					"VALUES (" + values + ")";
				
				PreparedStatement stmtSaveNewRecord = conn.prepareStatement(insert);
	
				stmtSaveNewRecord.clearParameters();

				stmtSaveNewRecord.setString(1, dt.sentence);
				stmtSaveNewRecord.setString(2, Long.toString(dt.millis));
			    
			    for (int i = 0; i < 10; i++) {
			    	stmtSaveNewRecord.setDouble((i*4)+3, dt.ber[i]);
			    	stmtSaveNewRecord.setDouble((i*4)+4, dt.rssi[i]);
			    	stmtSaveNewRecord.setDouble((i*4)+5, dt.sinad[i]);
			    	stmtSaveNewRecord.setDouble((i*4)+6, dt.freq[i]);
			    }
			    
			    stmtSaveNewRecord.setInt(43, dt.testTileLastMeasured.getEasting());
			    stmtSaveNewRecord.setInt(44, dt.testTileLastMeasured.getNorthing());
			    stmtSaveNewRecord.setString(45, dt.testTileLastMeasured.getGridZoneDesignator());
			    stmtSaveNewRecord.setInt(46, dt.tilesTraversed);
			    stmtSaveNewRecord.setInt(47, dt.measurementDelayTimer);
			    stmtSaveNewRecord.setInt(48, dt.tileIndexPointer);
			    stmtSaveNewRecord.setDouble(49, dt.tileSize.x);
			    stmtSaveNewRecord.setDouble(50, dt.tileSize.y);
			    stmtSaveNewRecord.setInt(51, dt.maximumSamplesPerTile);
			    stmtSaveNewRecord.setInt(52, dt.minimumSamplesPerTile);
			    stmtSaveNewRecord.setDouble(53, dt.position.x);
			    stmtSaveNewRecord.setDouble(54, dt.position.y);
			    stmtSaveNewRecord.setDouble(55, dt.dopplerDirection);
			    stmtSaveNewRecord.setInt(56, dt.dopplerQuality);
			    stmtSaveNewRecord.setInt(57, dt.marker);
			    stmtSaveNewRecord.setString(58, Long.toString(sm.timeStamp));
			    stmtSaveNewRecord.setDouble(59, sm.point.x);
			    stmtSaveNewRecord.setDouble(60, sm.point.y);
			    stmtSaveNewRecord.setDouble(61, sm.courseMadeGoodTrue);
		    	stmtSaveNewRecord.setDouble(62, sm.speedMadeGoodKPH);
		    	stmtSaveNewRecord.setDouble(63, sm.altitude);
		    	stmtSaveNewRecord.setDouble(64, sm.frequencyMHz);
		    	stmtSaveNewRecord.setInt(65, sm.unit);
		    	stmtSaveNewRecord.executeUpdate();
		    	stmtSaveNewRecord.close();
		    	
		        return null;
			}
		}
		@Override
        protected void done() {
			pcs.firePropertyChange(APPENDED, null, recordCount);
        }
	};
	
	private class CreateTable extends SwingWorker<Void, Void> {
		private CreateTable() {
			execute();
		}
		
		@Override
        protected Void doInBackground() throws Exception {
			synchronized(databaseLock) {
				while (!databaseCreated) {
					databaseLock.wait();
				}
		        PreparedStatement preparedStatement = conn.prepareStatement(getTableDef());
		        preparedStatement.executeUpdate();
		        preparedStatement.close();
		        return null;
			}
		}
		
		@Override
		protected void done() {
			synchronized(tableLock) {
				tableCreated = true;
				tableLock.notifyAll();
				pcs.firePropertyChange(OPEN, null, null);
			}
		}
	}

	private String getTableDef() {
		String scanString = "";

		for (int i = 0; i < 10; i++) {
			scanString += "BER_F" + String.valueOf(i) + " FLOAT(38)," +
			"RSSI_F" + String.valueOf(i) + " FLOAT(38)," +
			"SINAD_F" + String.valueOf(i) + " FLOAT(38)," +
			"RX_F" + String.valueOf(i) + " FLOAT(38),";
		}
		
		String tableDef = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER NOT NULL " +
			"PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
		
	   	"Sentence VARCHAR(128)," + 
		"Millis VARCHAR(32)," +
		
		scanString +
		
		"TestTileMeasuredEasting INT," +
		"TestTileMeasuredNorthing INT," +
		"TestTileMeasuredGZD VARCHAR(8)," +
		"TilesTraversed INT," +
		"MeasurementTimerDelay INT," +
		"TileIndexPointer INT," +
		"TileSizeLongitude  FLOAT(38)," +
		"TileSizeLatitude  FLOAT(38)," +
		"MaximumSamplesPerTile INT," +
		"MinimumSamplesPerTile INT," +
		"Longitude  FLOAT(38)," +
		"Latitude  FLOAT(38)," +
		"DopplerDirection INT," +
		"DopplerQuality INT," +
		"Marker INT," +
		"TimeStamp VARCHAR(32)," +
		"FlightLongitude  FLOAT(38)," +
		"FlightLatitude  FLOAT(38)," +
		"CourseMadeGoodTrue  FLOAT(38)," +
		"SpeedMadeGoodKPH  FLOAT(38)," +
		"Altitude  FLOAT(38)," +
		"FrequencyMHz  FLOAT(38)," +
		"Unit INT)";

		return tableDef;
	}

	private class Database extends SwingWorker<Void, Void> {
		private String databaseName;
		private String username;
		private String password;
		private String path;
		
		private Database(String path, String databaseName, String username, String password) {
			this.path = path;
			this.databaseName = databaseName;
			this.username = username;
			this.password = password;
			execute();
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			try {
				String url = DERBY_URL + path + File.separator + databaseName + ";create=true";
			    conn = DriverManager.getConnection(url, username, password);
			    conn.setAutoCommit(true);
			} catch (SQLException ex) {
				reportException(ex);
			}
			return null;
		}
		
		@Override
		protected void done() {
			synchronized(databaseLock) {
				databaseCreated = true;
				databaseLock.notifyAll();
				if (!isTableCreated(TABLE_NAME)) {
					new CreateTable();
				} else {
					pcs.firePropertyChange(OPEN, null, null);
				}
			}
		}
	}
	
	public void setSystemDir(final String dataFilePath) {
		System.setProperty(DERBY_DRIVER, File.separator + dataFilePath);
	}
	
	private boolean isTableCreated(String tableName) {
		boolean result = false;
		try {
			DatabaseMetaData metadata = conn.getMetaData();
			ResultSet tables = metadata.getTables(conn.getCatalog(), null, tableName, null);
			if (tables.next()) result = true;
		} catch (SQLException ex) {
			reportException(ex);
		} catch (NullPointerException ex) {
			reportException(ex);
		}
		return result;
	}
	
	private void configureEventLog() {
		try {
        	String eventLogFileName = System.getProperty("user.dir") + 
        		File.separator + DEFAULT_LOG_FILE_DIR + File.separator + "event.log";
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
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!pcs.hasListeners(null)) {
			pcs.addPropertyChangeListener(listener);
		}
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    private void reportException(final IOException ex) {
    	log.log(Level.WARNING, "IOException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
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
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                	ex.getMessage(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
                sqlExceptionQueued = false;
            }
        });
	}
    
    private void reportException(final NullPointerException ex) {
    	log.log(Level.WARNING, "NullPointerException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                	ex.getMessage(), "Null Pointer Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final InterruptedException ex) {
    	log.log(Level.WARNING, "InterruptedException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                	ex.getMessage(), "Interrupted Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final ExecutionException ex) {
    	log.log(Level.WARNING, "ExecutionException", ex);
    	ex.printStackTrace();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                	ex.getMessage(), "Execution Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
    
    private void reportException(final SecurityException ex) {
    	log.log(Level.WARNING, "SecurityException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                	ex.getMessage(), "Security Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

    private void reportException(final TimeoutException ex) {
    	log.log(Level.WARNING, "TimeoutException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                	ex.getMessage(), "Timeout Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

    private void reportException(final IllegalArgumentException ex) {
    	log.log(Level.WARNING, "IllegalArgumentException", ex);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                	ex.getMessage(), "Illegal Argument Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

	public int getIndexCursor() {
		int ret = 0;
		try {
			if (dataSet != null) return dataSet.getInt("ID");
		} catch (final IllegalArgumentException ex) {
    		reportException(ex);
    	} catch (SQLException ex) {
    		reportException(ex);
		}
		return ret;
	}
	
	public long getTimeStampCursor() {
		int ret = 0;
		try {
			if (measurementSet != null) return measurementSet.getLong("TimeStamp");
		} catch (final SQLException ex) {
			reportException(ex);
    	} catch (final IllegalArgumentException ex) {
    		reportException(ex);
    	}
		return ret;
	}

	public void requestStaticMeasurementData(final int index) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE ID = " + String.valueOf(index);
		new RetrieveStaticMeasurementRecord(sql);
	}
	
	public void requestStaticMeasurement(final long timeStamp) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE TIMESTAMP = " + String.valueOf(timeStamp);
		new RetrieveStaticMeasurementRecord(sql);
	}
	
	public void requestFirstUnitRecord(final int unit) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE UNIT = " + String.valueOf(unit);
		new RetrieveStaticMeasurementRecord(sql, Row.FIRST);
	}
	
	public void requestNextUnitRecord(final int unit) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE UNIT = " + String.valueOf(unit);
		new RetrieveStaticMeasurementRecord(sql, Row.NEXT);
	}
	
	public void requestDriveTestData(final int index) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE ID = " + String.valueOf(index);
		new RetrieveDriveTestRecord(sql);
	}
	
	public void requestDriveTestData(final long timeStamp) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE TIMESTAMP = " + String.valueOf(timeStamp);
		new RetrieveDriveTestRecord(sql);
	}
	
	private Object[] getDriveTestFields(final ResultSet dataSet) {
		Object[] obj = new Object[NUM_DT_COLS];
		try {
			for (int i = 0; i <= 57; i++) {
				obj[i] = dataSet.getObject(i);
			}
		} catch (SQLException ex) {
			reportException(ex);
		}
		return obj;
	}
	
	private Object[] getStaticMeasurementFields(final ResultSet measurementSet) {
		Object[] obj = new Object[NUM_SM_COLS];
		try {
			obj[0] = measurementSet.getObject(0);
			for (int i = 58; i <= 65; i++) {
				obj[i-57] = measurementSet.getObject(i);
			}
		} catch (SQLException ex) {
			reportException(ex);
		}
		return obj;
	}
	
	private class RetrieveStaticMeasurementRecord extends SwingWorker<StaticMeasurement, Void> {
		String sql;
		Row row;
		
		private RetrieveStaticMeasurementRecord(final String sql) {
			this(sql, Row.FIRST);
		}	
		
		private RetrieveStaticMeasurementRecord(final String sql,final Row row) {
			this.sql = sql;
			this.row = row;
			execute();
		}
		
		@Override
        protected StaticMeasurement doInBackground() throws Exception {
			Object[] obj;
			boolean found = false;
			
        	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
        	measurementSet = preparedStatement.executeQuery();
        	preparedStatement.close();
        	
        	switch(row) {
	        	case FIRST : found = measurementSet.first(); break; 
	        	case NEXT : found = measurementSet.next(); break; 
	        	case LAST : found = measurementSet.last(); break; 
        	}
			 
			if (found) {
				obj = getStaticMeasurementFields(measurementSet);
			} else {
				return null;
			}
			
			return StaticMeasurement.objectArrayToStaticMeasurement(obj); 
		}
		
		@Override
        protected void done() {
			try {
				StaticMeasurement sm = get(500, TimeUnit.MILLISECONDS);
				pcs.firePropertyChange(STATIC_MEASUREMENT_DATA_READY, null, sm);
			} catch (final TimeoutException ex) {
				reportException(ex);
			} catch (final InterruptedException ex) {
				reportException(ex);
	    	} catch (final ExecutionException ex) {
	    		reportException(ex);
	    	}
        }
	}
	
	private class RetrieveDriveTestRecord extends SwingWorker<DriveTestData, Void> {
		String sql;
		Row row;
		
		private RetrieveDriveTestRecord(final String sql) {
			this(sql, Row.FIRST);
		}	
		
		private RetrieveDriveTestRecord(final String sql, final Row row) {
			this.sql = sql;
			this.row = row;
			execute();
		}
		
		@Override
        protected DriveTestData doInBackground() throws Exception {
			Object[] obj;
			boolean found = false;
			
        	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
        	dataSet = preparedStatement.executeQuery();
        	preparedStatement.close();
        	
        	switch(row) {
	        	case FIRST : found = dataSet.first(); break; 
	        	case NEXT : found = dataSet.next(); break; 
	        	case LAST : found = dataSet.last(); break; 
        	}
			 
			if (found) {
				obj = getDriveTestFields(dataSet);
			} else {
				return null;
			}
			
			return DriveTestData.objectArrayToDriveTestData(obj); 
		}
		
		@Override
        protected void done() {
			try {
				DriveTestData dt = get(500, TimeUnit.MILLISECONDS);
				pcs.firePropertyChange(DRIVE_TEST_DATA_READY, null, dt);
			} catch (final TimeoutException ex) {
				reportException(ex);
			} catch (final InterruptedException ex) {
				reportException(ex);
	    	} catch (final ExecutionException ex) {
	    		reportException(ex);
	    	}
        }
	}
	
	public void seek(final int index) {
		try {
			if (dataSet != null) dataSet.getInt(index);
		} catch (final SQLException ex) {
			reportException(ex);
		}
	}
	
	public void findLastRow() {
		try {
			if (dataSet != null) dataSet.last();
		} catch (final SQLException ex) {
			reportException(ex);
		}
	}
	
	public void findFirstRow() {
		try {
			if (dataSet != null) dataSet.first();
		} catch (final SQLException ex) {
			reportException(ex);
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
			if (dataSet != null) dataSet.close();
			if (conn != null) conn.close();
			if (measurementSet != null) measurementSet.close();
			if (fh != null) fh.flush();
			if (fh != null) fh.close();
			pcs.firePropertyChange(CLOSED, null, null);
		} catch (final SQLException ex) {
			reportException(ex);
    	}
	}
	
	public void requestUpdatedRecordCount() {
		new TableStats();
	}
	
	public int getRecordCount() {
		return recordCount;
	}
	
	private class TableStats extends SwingWorker<Integer, Void> {
		private TableStats() {
			execute();
		}
		
		@Override
        protected Integer doInBackground() throws Exception {
			synchronized(tableLock) {
				while (!tableCreated) {
					tableLock.wait();
				}
				String sql = "SELECT COUNT(*) AS recordCount FROM " + TABLE_NAME;
				PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
		    	ResultSet resultSet = preparedStatement.executeQuery();
		    	resultSet.next();
		    	int count = resultSet.getInt("recordCount");
		    	preparedStatement.close();
				resultSet.close();
				return count;
			}
		}
		
		@Override
		protected void done() {
			try {
				synchronized(DataBase.this) {
					firePropertyChange(RECORD_COUNT_READY, null, get());
					DataBase.this.notifyAll();
				}
			} catch (InterruptedException ex) {
				reportException(ex);
			} catch (ExecutionException ex) {
				reportException(ex);
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
}
