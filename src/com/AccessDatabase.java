package com;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;
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

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.IndexBuilder;
import com.healthmarketscience.jackcess.IndexCursor;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

public class AccessDatabase {
	private static final Logger log = Logger.getLogger(AccessDatabase.class.getName());
	private final static String TABLE_NAME = "log";
	private final static int NUM_SM_COLS = 9;
	private final static int NUM_DT_COLS = 57;

	private DataBase dataBaseObj = new DataBase();
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private FileHandler fh;
	private Component parent;
	
	public static final String OPEN = "OPEN";
	public static final String CLOSED = "CLOSED";
	public static final String APPENDED = "APPENDED";
	public static final String DRIVE_TEST_DATA_READY = "DRIVE_TEST_DATA_READY";
	public static final String STATIC_MEASUREMENT_DATA_READY = "STATIC_MEASUREMENT_DATA_READY";

	public AccessDatabase(Component parent, String fileName) {
		this.parent = parent;
		configureEventLog();
		new CreateDatabase(dataBaseObj, fileName);
	}

	private void configureEventLog() {
		try {
        	String eventLogFileName = System.getProperty("user.dir") + File.separator + "database event log" + File.separator + "event.log";
        	Path path = Paths.get(eventLogFileName);
    		File directory = new File(path.getParent().toString());
    		if (!directory.exists()) new File(path.getParent().toString()).mkdirs();
        	fh = new FileHandler(eventLogFileName, 4096, 64, true);
            log.addHandler(fh);
            log.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (final SecurityException e) {
        	log.log(Level.WARNING, "SecurityException", e);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(null),
                    		e.getCause().toString(), "Security Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
        } catch (final IOException e) {
        	log.log(Level.WARNING, "IOException", e);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(null),
                    		e.getCause().toString(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
	}
	
	private int mmmToInt(String mmm) {
		switch (mmm) {
		    case "Jan": return 1;
	        case "Feb": return 2;
	        case "Mar": return 3;
	        case "Apr": return 4;
	        case "May": return 5;
	        case "Jun": return 6;
	        case "Jul": return 7;
	        case "Aug": return 8;
	        case "Sep": return 9;
	        case "Oct": return 10;
	        case "Nov": return 11;
	        case "Dec": return 12;
	        default: return 0;
		}
	}

	private class CreateDatabase {
		private String fileName;
		private Table table = null;
		private IndexCursor indexCursor = null;
		private IndexCursor timeStampCursor = null;
		private IndexCursor unitCursor = null;
		private Database database = null;
		
		private CreateDatabase(final DataBase dataBase, final String fileName) {
			this.fileName = fileName;
			createDatabase();
		}
		
		private DataBase createDatabase() {
			try {
				Path path = Paths.get(fileName);
				File directory = new File(path.getParent().toString());
				if (!directory.exists())
					new File(path.getParent().toString()).mkdirs();
				if (Files.notExists(path)) {
					database = buildDatabase(fileName);
					if (database != null) {
						table = buildTable(database);
						indexCursor = CursorBuilder.createCursor(table.getIndex(IndexBuilder.PRIMARY_KEY_NAME));
						timeStampCursor = CursorBuilder.createCursor(table.getIndex("TimeStampIndex"));
						unitCursor = CursorBuilder.createCursor(table.getIndex("UnitIndex"));
					}
				} else {
					database = DatabaseBuilder.open(new File(fileName));
					table = database.getTable(TABLE_NAME);
					indexCursor = CursorBuilder.createCursor(table.getIndex(IndexBuilder.PRIMARY_KEY_NAME));
					timeStampCursor = CursorBuilder.createCursor(table.getIndex("TimeStampIndex"));
					unitCursor = CursorBuilder.createCursor(table.getIndex("UnitIndex"));
				}
				pcs.firePropertyChange(OPEN, null, null);
				return new DataBase(database, table, indexCursor, timeStampCursor, unitCursor);
			} catch (final IOException e) {
				log.log(Level.WARNING, "IOException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
			}
			return null;
		}
	}	

	public void appendRecord(final DriveTestData dt) {
		try {
			new AppendRecord(dataBaseObj, dt, new StaticMeasurement());
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}
	
	public void appendRecord(final StaticMeasurement sm) {
		try {
			new AppendRecord(dataBaseObj, new DriveTestData(), sm);
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}

	public void appendRecord(final DriveTestData dt, final StaticMeasurement sm) {
		try {
			new AppendRecord(dataBaseObj, dt, sm);
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}

	private class AppendRecord extends SwingWorker<DataBase, Void> {
		private DriveTestData dt = null;
		private DataBase dataBase = null;
		private StaticMeasurement sm = null;
		
		private AppendRecord(final DataBase dataBase, final DriveTestData dt, final StaticMeasurement sm) 
				throws IOException {
			this.dt = dt;
			this.sm = sm;
			this.dataBase = dataBase;
			execute();
		}
		
		@Override
        protected DataBase doInBackground() throws Exception {
			Object[] obj = new Object[dataBase.table.getColumnCount()];
			obj[0] = Column.AUTO_NUMBER;
			obj[1] = dt.sentence;
			java.util.Calendar cal = dt.dtg;
		    java.util.Date utilDate = new java.util.Date();
		    cal.setTime(utilDate);
		    cal.set(Calendar.HOUR_OF_DAY, 0);
		    cal.set(Calendar.MINUTE, 0);
		    cal.set(Calendar.SECOND, 0);
		    cal.set(Calendar.MILLISECOND, 0);    
		    java.sql.Date sqlDate = new java.sql.Date(cal.getTime().getTime());
		    obj[2] = sqlDate;
		    for (int i = 0; i < 10; i++) {
		    	obj[(i*4)+3] = dt.ber[i];
		    	obj[(i*4)+4] = dt.rssi[i];
		    	obj[(i*4)+5] = dt.sinad[i];
		    	obj[(i*4)+6] = dt.freq[i];
		    }
		    obj[43] = dt.gridLastMeasured.x;
		    obj[44] = dt.gridLastMeasured.y;
		    obj[45] = dt.tilesTraversed;
		    obj[46] = dt.measurementDelayTimer;
		    obj[47] = dt.tileIndexPointer;
		    obj[48] = dt.tileSize.x;
		    obj[49] = dt.tileSize.y;
		    obj[50] = dt.maximumSamplesPerTile;
		    obj[51] = dt.minimumSamplesPerTile;
		    obj[52] = dt.position.x;
		    obj[53] = dt.position.y;
		    obj[54] = dt.dopplerDirection;
		    obj[55] = dt.dopplerQuality;
		    obj[56] = dt.marker;
		    obj[57] = sm.timeStamp;
		    obj[58] = sm.point.x;
		    obj[59] = sm.point.y;
		    obj[60] = sm.courseMadeGoodTrue;
	    	obj[61] = sm.speedMadeGoodKPH;
	    	obj[62] = sm.altitude;
	    	obj[63] = sm.frequencyMHz;
	    	obj[64] = sm.unit;
			dataBase.table.addRow(obj);
			dataBase.indexCursor.afterLast();
			dataBase.indexCursor.getPreviousRow();
			return dataBase;
		}

		@Override
        protected void done() {
			try {
				DataBase db = get(1000, TimeUnit.MILLISECONDS);
				this.dataBase = db;
				pcs.firePropertyChange(APPENDED, null, null);
			} catch (final TimeoutException e) {
				log.log(Level.WARNING, "TimeoutException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Timeout Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
			} catch (final InterruptedException e) {
	    		log.log(Level.WARNING, "InterruptedException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Interrupted Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	    	} catch (final ExecutionException e) {
	    		log.log(Level.WARNING, "ExecutionException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Execution Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	    	}
        }
	}

	private Table buildTable(Database database) {
		try {
			Table table = new TableBuilder(TABLE_NAME)
				.addColumn(new ColumnBuilder("ID", DataType.LONG).setAutoNumber(true))
				.addColumn(new ColumnBuilder("Sentence").setSQLType(Types.VARCHAR).toColumn())
				.addColumn(new ColumnBuilder("DateTime").setSQLType(Types.TIMESTAMP).toColumn())
				.addColumn(new ColumnBuilder("F0BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F0RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F0SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F0").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F1BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F1RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F1SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F1").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F2BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F2RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F2SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F2").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F3BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F3RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F3SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F3").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F4BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F4RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F4SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F4").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F5BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F5RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F5SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F5").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F6BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F6RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F6SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F6").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F7BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F7RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F7SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F7").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F8BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F8RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F8SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F8").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F9BER").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("F9RSSI").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F9SINAD").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("F9").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("GridLastMeasuredX").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("GridLastMeasuredY").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("TilesTraversed").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("MeasurementTimerDelay").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("TileIndexPointer").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("TileSizeLongitude").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("TileSizeLatitude").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("MaximumSamplesPerTile").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("MinimumSamplesPerTile").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("Longitude").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("Latitude").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("DopplerDirection").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("DopplerQuality").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("Marker").setSQLType(Types.INTEGER).toColumn())
				.addColumn(new ColumnBuilder("TimeStamp").setSQLType(Types.LONGVARCHAR).toColumn())
				.addColumn(new ColumnBuilder("FlightLongitude").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("FlightLatitude").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("CourseMadeGoodTrue").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("SpeedMadeGoodKPH").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("Altitude").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("FrequencyMHz").setSQLType(Types.DOUBLE).toColumn())
				.addColumn(new ColumnBuilder("Unit").setSQLType(Types.INTEGER).toColumn())
				.addIndex(new IndexBuilder(IndexBuilder.PRIMARY_KEY_NAME).addColumns("ID").setPrimaryKey())
				.addIndex(new IndexBuilder("UnitIndex").addColumns("Unit"))
				.addIndex(new IndexBuilder("TimeStampIndex").addColumns("TimeStamp"))
				.toTable(database);
			return table;
		} catch (final IOException e) {
    		log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	} catch (final SQLException e) {
    		log.log(Level.WARNING, "SQLException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	}
		return null;
	}
	
	public boolean isClosed() {
		boolean cl = false;
		if (dataBaseObj.database == null) cl = true;
		return cl;
	}

	public void close() {
		try {
			dataBaseObj.database.flush();
			dataBaseObj.database.close();
			dataBaseObj.table = null;
			dataBaseObj.table = null;
			dataBaseObj.indexCursor = null;
			dataBaseObj.timeStampCursor = null;
			dataBaseObj.unitCursor = null;
			dataBaseObj.indexCursor = null;
			dataBaseObj = null;
			pcs.firePropertyChange(CLOSED, null, null);
		} catch (final IOException e) {
    		log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	}
	}

	public int getNumberOfRecords() { 
		return dataBaseObj.table.getRowCount();	
	}
	
	public void requestDriveTestData(long index) {
		try {
			new RetrieveDriveTestData(index);
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		} catch (final ParseException e) {
    		log.log(Level.WARNING, "ParseException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "Parse Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	}
	}
	
	private class RetrieveDriveTestData extends SwingWorker<DriveTestData, Void> {
		private long index;
		
		private RetrieveDriveTestData (long index) throws IOException, ParseException {
			this.index = index;
			execute();
		}
		
        @Override
        protected DriveTestData doInBackground() throws Exception {
			long y = Math.min(index, dataBaseObj.table.getRowCount());
			String[] str = new String[NUM_DT_COLS];
			DriveTestData dt = new DriveTestData();
			boolean found = dataBaseObj.indexCursor.findFirstRow(Collections.singletonMap("ID", y));
			if (found) {
				for (int i = 0; i <= 56; i++) {
					str[i] = dataBaseObj.indexCursor.getCurrentRowValue(dataBaseObj.table.getColumns().get(i)).toString();
				}
			} else {
				return dt;
			}
			dt.sentence = str[1];
		    int yyyy =  Integer.parseInt(str[2].substring(24, 28));
		    int mM = mmmToInt(str[2].substring(4, 7));
		    int dd = Integer.parseInt(str[2].substring(8, 10));
		    int hH = Integer.parseInt(str[2].substring(11, 13));
		    int mm = Integer.parseInt(str[2].substring(14, 16));
		    int ss = Integer.parseInt(str[2].substring(17, 19));
		    String zzz = str[2].toString().substring(20, 23);
		    Calendar cal = Calendar.getInstance();
		    cal.setTimeZone(TimeZone.getTimeZone(zzz));
		    cal.set(yyyy, mM, dd, hH, mm, ss); 
		    dt.dtg = cal;
		    for (int i = 0; i < 10; i++) { 	
		    	dt.ber[i] = Double.parseDouble(str[(i*4)+3]);
		    	dt.rssi[i] = Integer.parseInt(str[(i*4)+4]);
		    	dt.sinad[i] = Integer.parseInt(str[(i*4)+5]);
		    	dt.freq[i] = Double.parseDouble(str[(i*4)+6]);
		    }
			dt.gridLastMeasured.x = Integer.parseInt(str[43]);
			dt.gridLastMeasured.y = Integer.parseInt(str[44]);
			dt.tilesTraversed = Integer.parseInt(str[45]);
			dt.measurementDelayTimer = Integer.parseInt(str[46]);  
			dt.tileIndexPointer = Integer.parseInt(str[47]);
			dt.tileSize.x = Double.parseDouble(str[48]);
			dt.tileSize.y = Double.parseDouble(str[49]);
			dt.maximumSamplesPerTile = Integer.parseInt(str[50]);
			dt.minimumSamplesPerTile = Integer.parseInt(str[51]);
			dt.position.x = Double.parseDouble(str[52]);
			dt.position.y = Double.parseDouble(str[53]);
			dt.dopplerDirection = Double.parseDouble(str[54]);
			dt.dopplerQuality = Integer.parseInt(str[55]);
			dt.marker = Integer.parseInt(str[56]);
			return 	dt;
        }
		
		@Override
        protected void done() {
			try {
				DriveTestData dt = get(1000, TimeUnit.MILLISECONDS);
				pcs.firePropertyChange(DRIVE_TEST_DATA_READY, null, dt);
			} catch (final TimeoutException e) {
				log.log(Level.WARNING, "TimeoutException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Timeout Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
			} catch (final InterruptedException e) {
	    		log.log(Level.WARNING, "InterruptedException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Interrupted Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	    	} catch (final ExecutionException e) {
	    		log.log(Level.WARNING, "ExecutionException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Execution Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	    	}
        }
	}

	public int getIndexCursor() {
		try {
			return (int) dataBaseObj.indexCursor.getCurrentRowValue(dataBaseObj.table.getColumn("ID"));
		} catch (final IOException e) {
    		log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	} catch (final IllegalArgumentException e) {
    		log.log(Level.WARNING, "IllegalArgumentException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "Illegal Argument Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	}
		return 0;
	}
	
	public int getTimeStampCursor() {
		try {
			return (int) dataBaseObj.timeStampCursor.getCurrentRowValue(dataBaseObj.table.getColumn("TimeStamp"));
		} catch (final IOException e) {
    		log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	} catch (final IllegalArgumentException e) {
    		log.log(Level.WARNING, "IllegalArgumentException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "Illegal Argument Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	}
		return 0;
	}

	public synchronized StaticMeasurement getTimeStampRecord(long timeStamp) throws IOException, ParseException {
		try {
			long y = Math.min(timeStamp, dataBaseObj.table.getRowCount());
			String[] str = new String[NUM_SM_COLS];
			StaticMeasurement sm = new StaticMeasurement();
			boolean found = dataBaseObj.timeStampCursor.findFirstRow(Collections.singletonMap("TimeStamp", String.valueOf(y)));
			if (found) {
				str[0] = dataBaseObj.timeStampCursor.getCurrentRowValue(dataBaseObj.table.getColumns().get(0)).toString();
				for (int i = 57; i <= 64; i++) {
					str[i-56] = dataBaseObj.timeStampCursor.getCurrentRowValue(dataBaseObj.table.getColumns().get(i)).toString();
				}
			} else {
				return sm;
			}
			return StaticMeasurement.stringArrayToStaticMeasurement(str); 
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
		return null;
	}

	public void requestStaticMeasurement(long index) {
		try {
			new RetrieveStaticMeasurementData(index);
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		} catch (final ParseException e) {
			log.log(Level.WARNING, "ParseException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "Parse Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}
	
	private String[] staticMeasurementFields() throws IOException {
		String[] str = new String[NUM_SM_COLS];
		str[0] = dataBaseObj.unitCursor.getCurrentRowValue(dataBaseObj.table.getColumns().get(0)).toString();
		for (int i = 57; i <= 64; i++) {
			str[i-56] = dataBaseObj.unitCursor.getCurrentRowValue(dataBaseObj.table.getColumns().get(i)).toString();
			
		}
		return str;
	}
	
	private class RetrieveStaticMeasurementData extends SwingWorker<StaticMeasurement, Void> {
		private long index;
		
		private RetrieveStaticMeasurementData (long index) throws IOException, ParseException {
			this.index = index;
			execute();
		}
		
        @Override
        protected StaticMeasurement doInBackground() throws Exception {
			long y = Math.min(index, dataBaseObj.table.getRowCount());
			String[] str = new String[NUM_SM_COLS];
			StaticMeasurement sm = new StaticMeasurement();
			boolean found = dataBaseObj.indexCursor.findFirstRow(Collections.singletonMap("ID", y));
			if (found) {
				str = staticMeasurementFields();
			} else {
				return sm;
			}
			return StaticMeasurement.stringArrayToStaticMeasurement(str);
        }

        @Override
        protected void done() {
			try {
				StaticMeasurement sm = get(1000, TimeUnit.MILLISECONDS);
				pcs.firePropertyChange(STATIC_MEASUREMENT_DATA_READY, null, sm);
			} catch (final TimeoutException e) {
				log.log(Level.WARNING, "TimeoutException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Timeout Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
			} catch (final InterruptedException e) {
	    		log.log(Level.WARNING, "InterruptedException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Interrupted Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	    	} catch (final ExecutionException e) {
	    		log.log(Level.WARNING, "ExecutionException", e);
	    		SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                    		e.getLocalizedMessage(), "Execution Exception", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	    	}
        }
	}
	
	public synchronized StaticMeasurement getFirstUnitRecord(int unit) {
		try {
			int y = Math.min(unit, dataBaseObj.table.getRowCount());
			String[] str = new String[NUM_SM_COLS];
			StaticMeasurement sm = new StaticMeasurement();
			boolean found = dataBaseObj.unitCursor.findFirstRow(Collections.singletonMap("Unit", y));
			if (found) {
				str = staticMeasurementFields();
			} else {
				return sm;
			}
			return StaticMeasurement.stringArrayToStaticMeasurement(str);
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
		return null;
	}
	
	public synchronized StaticMeasurement getNextUnitRecord(int unit) {
		try {
			int y = Math.min(unit, dataBaseObj.table.getRowCount());
			String[] str = new String[NUM_SM_COLS];
			StaticMeasurement sm = new StaticMeasurement();	
			boolean found = dataBaseObj.unitCursor.findNextRow(Collections.singletonMap("Unit", y));
			if (found) {
				str = staticMeasurementFields();
			} else {
				return sm;
			}
			return StaticMeasurement.stringArrayToStaticMeasurement(str);
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
		return null;
	}
	
	public synchronized void seek(int index) {
		try {
			int i = Math.min(index, dataBaseObj.table.getRowCount());
			dataBaseObj.indexCursor.findFirstRow(Collections.singletonMap("ID", i));
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
	                		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
	            }
	        });
		}
	}
	
	public synchronized void findLastRow() throws IOException {
		try {
			dataBaseObj.indexCursor.afterLast();
			dataBaseObj.indexCursor.getPreviousRow();
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}
	
	public synchronized void findFirstRow() {
		try {
			dataBaseObj.indexCursor.beforeFirst();
			dataBaseObj.indexCursor.getNextRow();
		} catch (final IOException e) {
			log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}

	private static class DataBase {
		public Database database = null;
		public Table table = null;
		public IndexCursor indexCursor = null;
		public IndexCursor timeStampCursor = null;
		public IndexCursor unitCursor = null;
		private DataBase() {}
		private DataBase(Database database, Table table, IndexCursor indexCursor, IndexCursor timeStampCursor, 
				IndexCursor unitCursor) {
			this.database = database;
			this.table = table;
			this.indexCursor = indexCursor;
			this.timeStampCursor = timeStampCursor;
			this.unitCursor = unitCursor;
		}
	}

	private Database buildDatabase(String fileName) {
		try {
			Path path = Paths.get(fileName);
			if (Files.notExists(path)) {
				Database database = DatabaseBuilder.create(Database.FileFormat.V2007, new File(fileName));
				return database;
			}
		} catch (final IOException e) {
    		log.log(Level.WARNING, "IOException", e);
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                    		e.getLocalizedMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
                }
            });
    	}
		return null;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
	
}
