package jdrivetrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

	public Zip (String inputFolder, String targetZippedFolder) {

		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		
        try {
        	
			fos = new FileOutputStream(new File(targetZippedFolder + ".zip"));
	        
			zos = new ZipOutputStream(fos);
	
	        File inputFile = new File(inputFolder);
	
	        if (inputFile.isFile()) {
	            zipFile(inputFile, "", zos);
	        } else if (inputFile.isDirectory()) {
	            zipFolder(zos, inputFile, "");
	        }

        } catch (IOException ex) {
        	ex.printStackTrace();
        } finally {
        	try {
				fos.close();
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	
	private void zipFolder(ZipOutputStream zos, File inputFolder, String parentName)  throws IOException {

        String name = parentName + inputFolder.getName() + "\\";

        ZipEntry ze = new ZipEntry(name);
        
        ze.setMethod(ZipEntry.DEFLATED) ;
        
        zos.putNextEntry(ze);

        File[] files = inputFolder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                zipFile(file, name, zos);
            } else if(file.isDirectory()) {
                zipFolder(zos, file, name);
            }
        }
        
        zos.closeEntry();
    }

    private void zipFile(File inputFile, String parentName, ZipOutputStream zos) throws IOException {

        ZipEntry ze = new ZipEntry(parentName + inputFile.getName());
        
        ze.setMethod(ZipEntry.DEFLATED) ;
        
        zos.putNextEntry(ze);

        FileInputStream fis = new FileInputStream(inputFile);
        
        byte[] buffer = new byte[1024];
        
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, bytesRead);
        }

        zos.closeEntry();
        fis.close();
    }
}    