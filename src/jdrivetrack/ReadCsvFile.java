package jdrivetrack;

import java.io.RandomAccessFile;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

public class ReadCsvFile {
	private String inputString = "";
	private String[][] inputArray;
	private int inputArrayLength;
	private int columns;
	private String fileName;
	private int[] commaArray;
	private int inputInt;
	
	public ReadCsvFile(String newFileName) throws IOException {
		fileName = FilenameUtils.separatorsToSystem(newFileName);
		columns = getColumnCount();
		setInputArray();
	}
	
	public String[] getRow(int newRow) throws IndexOutOfBoundsException {
		String[] row = new String[columns];
		try {
			row = inputArray[newRow];
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
		return row;
	}

	public String[] getColumn(int newColumn) throws IndexOutOfBoundsException {
		String[] column = new String[inputArrayLength];
		try {
			for (int i = 0; i < inputArrayLength; i++) {
				column[i] = inputArray[i][newColumn];
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
		return column;
	}

	public int getRowCount() {
		return inputArrayLength;
	}
	
	public void setValue(int newRow, int newColumn, String newData) throws IOException {
		String after = "";
		String before = "";
		String data = "";
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(fileName, "rw"); 
			
			setInputArray();
			setCommaArray();
			
			raf.seek(commaArray[(newRow * columns) + newColumn + 1] - 1);

			while (raf.getFilePointer() < raf.length()) {
				after = after + raf.readLine() + System.lineSeparator(); 
			}	

			for (int i = 0; i < newRow; i++) {
				for (int k = 0; k < columns; k++) {
					before = before + inputArray[i][k];
					if (k < columns - 1) before = before + ",";
				}
				before = before + System.lineSeparator();
			}
			
			for (int i = 0; i < newColumn; i++) {
				before = before + inputArray[newRow][i] + ",";
			}
			
			data = before + newData + after;

			raf.seek(0);
			raf.writeBytes(data);
			raf.setLength(data.length());
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			raf.close();
		}
	}
	
	public int getColumnCount() throws IOException {
		RandomAccessFile cc = null;
		int columnCount = 0;
		try {
			cc = new RandomAccessFile(fileName, "r");
			cc.seek(0);
			String testString = cc.readLine();
			String testArray[] = testString.split(",");
			columnCount = testArray.length;
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IOException(ex);
		} finally {
			cc.close();
		}
		return columnCount;
	}
	
	private void setCommaArray() throws IOException {
		RandomAccessFile ca = null;
		int commaArrayCounter = 0;
		try {
			ca = new RandomAccessFile(fileName, "r"); 
			commaArray = new int[(int) ca.length()];
			ca.seek(0);
			commaArray[0] = 0;
			commaArrayCounter++;
			while (ca.getFilePointer() < ca.length()) {
				inputInt = ca.read(); 
				if (inputInt == 44 || inputInt == 13) {
					commaArray[commaArrayCounter] = (int) ca.getFilePointer(); 
					commaArrayCounter++;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			ca.close();
		}
	}
	
	private void setInputArray() throws IOException {
		RandomAccessFile raf = null;
		try {
			int i = 0;
			raf = new RandomAccessFile(fileName, "r"); 
			inputArray = new String[1024][columns];
			while (inputString != null && raf.getFilePointer() < raf.length())
			{
				inputString = raf.readLine();
				inputArray[i] = inputString.split(",");
				i++;
			}
			inputArrayLength = i;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			raf.close();
		}
	}
}