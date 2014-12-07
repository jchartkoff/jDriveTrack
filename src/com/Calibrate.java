package com;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Calibrate {

    private String fileName = null;
    private String inputArray[][] = new String[94][2];
    private String manufacturer = null;
    private String model = null;
    private String sn = null;
    private boolean inverted = false;
    private String rssiArray[] = new String[91];
    private String dBmArray[] = new String[91];

    public Calibrate(String newFileName) throws IOException {
        fileName = newFileName;
        RandomAccessFile raf;
        try {
            int i = 0;
            raf = new RandomAccessFile(fileName, "r");
            String inputString;
            while (raf.getFilePointer() < raf.length()) {
                inputString = raf.readLine();
                inputArray[i] = inputString.split("=");
                if (i >= 3) {
                    rssiArray[i - 3] = inputArray[i][0];
                    dBmArray[i - 3] = inputArray[i][1];
                }
                i++;
            }
            if (Integer.parseInt(inputArray[3][0]) < Integer.parseInt(inputArray[93][0])) {
                inverted = false;
            } else {
                inverted = true;
            }
            manufacturer = inputArray[0][1];
            model = inputArray[1][1];
            sn = inputArray[2][1];
            raf.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
            throw new IOException(ex);
        } 
        
    }

    private void setRssiData(String[] array) {
        RandomAccessFile raf;
        String t[] = new String[94];
        int tdBm = -30;
        int len = 0;
        try {
            raf = new RandomAccessFile(fileName, "rw");
            t[0] = "MANUFACTURER=" + manufacturer + System.lineSeparator();
            t[1] = "MODEL=" + model + System.lineSeparator();
            t[2] = "SN=" + sn + System.lineSeparator();
            for (int i = 3; i < 94; i++) {
                t[i] = array[i - 3] + "=" + String.valueOf(tdBm) + System.lineSeparator();
                tdBm--;
            }
            raf.seek(0);
            for (int i = 0; i < 94; i++) {
                raf.writeBytes(t[i]);
                len = len + t[i].length();
            }
            raf.setLength(len);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRSSI(String value, int index) {
        rssiArray[index] = value;
        setRssiData(rssiArray);
    }

    public int getdBm(int rssi) {
        int retVal = -30;
        for (int i = 3; i < inputArray.length; i++) {
            if (!inverted && Integer.parseInt(inputArray[i][0]) <= rssi) {
                retVal = Integer.parseInt(inputArray[i][1]);
            }
            if (inverted && Integer.parseInt(inputArray[i][0]) >= rssi) {
                retVal = Integer.parseInt(inputArray[i][1]);
            }
        }
        return retVal;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return sn;
    }

    public String[] getRssiArray() {
        String[] rssiArray = this.rssiArray; 
        return rssiArray;
    }

    public String[] getdBmArray() {
        String[] dBmArray = this.dBmArray;
        return dBmArray;
    }
}
