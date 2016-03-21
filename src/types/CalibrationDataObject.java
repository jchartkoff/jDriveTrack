package types;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CalibrationDataObject {
	private static final int MAX_DBM = -30;
	private static final int MIN_DBM = -120;
	private String fileName;
    private String manufacturer = null;
    private String model = null;
    private String sn = null;
    private boolean inverted = false;
    private int[][] array = new int[2][91];
    private File file;
    private boolean calFileOpen = false;
    
    public CalibrationDataObject(String calFileDir, String fileName) {
    	fileName = calFileDir + File.separator + fileName; 
    	file = new File(fileName);
    	this.fileName = fileName;
		parseCalFile();
    }
    
    public CalibrationDataObject(String fileName, String manufacturer, String model, String sn) {
    	try {
	    	Path path = Paths.get(fileName);
			File directory = new File(path.getParent().toString());
			if (!directory.exists()) new File(path.getParent().toString()).mkdirs();
			if (Files.notExists(path)) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.close();
				calFileOpen = true;
			} else {
				file = new File(fileName);
				parseCalFile();
			}
			this.fileName = fileName;
			this.manufacturer = manufacturer;
			this.sn = sn;
    	} catch (IOException ex) {
    		calFileOpen = false;
    		ex.printStackTrace();
    	}
    }
    
    public void setCalibrationFileDirectory(String calFileDir) {
    	
    }
    
    public boolean isCalFileOpen() {
    	return calFileOpen;
    }
    
    public void saveFile() {
    	saveRssiArrayToFile();
    }
    
    public String getManufacturer() {
    	return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
    	this.manufacturer = manufacturer;
    }
    
    public String getModelString() {
    	return model;
    }
    
    public void setModelString(String model) {
    	this.model = model;
    }
    
    public String getSerialString() {
    	return sn;
    }
    
    public void setSerialString(String sn) {
    	this.sn = sn;
    }
    
    public boolean isRssiInverted() {
    	return inverted;
    }
    
    public int[][] getArray() {
    	return array;
    }
    
    public int[] getRssiArray() {
    	return array[0];
    }
    
    public String[] getRssiStringArray() {
    	String[] rssiStringArray = new String[array.length];
    	for (int i = 0; i < array.length; i++) {
    		rssiStringArray[i] = Integer.toString(array[0][i]);
    	}
    	return rssiStringArray;
    }
    
    public String[] getdBmStringArray() {
    	String[] dBmStringArray = new String[array[1].length];
    	for (int i = 0; i < array[1].length; i++) {
    		dBmStringArray[i] = Integer.toString(array[1][i]);
    	}
    	return dBmStringArray;
    }
    
    public int getArraySize() {
    	return array[0].length;
    }
    
    public int[] getdBmArray() {
    	return array[1];
    }
    
    public String getFileName() {
    	return fileName;
    }

    public int getRssi(int dBm) {
    	if (dBm > MAX_DBM || dBm < MIN_DBM) return -1;
    	return array[0][Math.abs(dBm) + MAX_DBM];
    }
    
	public void updateRssiElement(int dBm, int rssi) {
		if (dBm > MAX_DBM || dBm < MIN_DBM) return;
		array[0][Math.abs(dBm) + MAX_DBM] = rssi;
	}
	
	private void parseCalFile() {
        RandomAccessFile raf = null;
        String[][] stringArray = new String[94][2];
        try {
            int i = 0;
            raf = new RandomAccessFile(file, "r");
            String inputString = null;
            while (raf.getFilePointer() < raf.length()) {
                inputString = raf.readLine();
                stringArray[i] = inputString.split("=");
                if (i >= 3) {
                    array[0][i - 3] = Integer.parseInt(stringArray[i][0]);
                    array[1][i - 3] = Integer.parseInt(stringArray[i][1]);
                }
                i++;
            }
            if (array[0][0] < array[0][90]) {
                inverted = false;
            } else {
                inverted = true;
            }
            manufacturer = stringArray[0][1];
            model = stringArray[1][1];
            sn = stringArray[2][1];
            calFileOpen = true;
        } catch (final IOException ex) {
        	calFileOpen = false;
            ex.printStackTrace();
        } finally {
			try {
				raf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
        }
    }
	
    private void saveRssiArrayToFile() {
        RandomAccessFile raf = null;
        String[] t = new String[94];
        int tdBm = MAX_DBM;
        int len = 0;
        try {
            raf = new RandomAccessFile(file, "rw");
            t[0] = "MANUFACTURER=" + manufacturer + System.lineSeparator();
            t[1] = "MODEL=" + model + System.lineSeparator();
            t[2] = "SN=" + sn + System.lineSeparator();
            for (int i = 3; i < 94; i++) {
                t[i] = array[0][i - 3] + "=" + String.valueOf(tdBm) + System.lineSeparator();
                tdBm--;
            }
            raf.seek(0);
            for (int i = 0; i < t.length; i++) {
                raf.writeBytes(t[i]);
                len = len + t[i].length();
            }
            raf.setLength(len);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
        	try {raf.close();} catch (IOException ex) {ex.printStackTrace();}
        }
    }
    
    public double getdBmElement(int rssi) {
        double dBm = -1;
        for (int i = 0; i < array[0].length; i++) {
            if (!inverted && array[0][i] <= rssi) {
                dBm = array[1][i];
            }
            if (inverted && array[0][i] >= rssi) {
                dBm = array[1][i];
            }
        }
        return dBm;
    }
}
