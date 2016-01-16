package jdrivetrack;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
 
public class AePlayWave extends Thread { 
	private static final int EXTERNAL_BUFFER_SIZE = 524288;
	
	private String filename;
    private Position curPosition;

    public enum Position {LEFT, RIGHT, NORMAL};
 
    public AePlayWave(String fileName) { 
        this(fileName, Position.NORMAL);
    } 
 
    public AePlayWave(String fileName, Position curPosition) { 
        this.filename = fileName;
        this.curPosition = curPosition;
        start();
    } 
 
    @Override
	public void run() { 

        AudioInputStream audioInputStream;
        
        try { 
            audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource(filename));
        } catch (UnsupportedAudioFileException | IOException e1) { 
            e1.printStackTrace();
            return;
        }
 
        AudioFormat format = audioInputStream.getFormat();
        SourceDataLine auline;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 
        try { 
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) { 
            e.printStackTrace();
            return;
        } catch (Exception e) { 
            e.printStackTrace();
            return;
        } 
 
        if (auline.isControlSupported(FloatControl.Type.PAN)) { 
            FloatControl pan = (FloatControl) auline.getControl(FloatControl.Type.PAN);
            if (curPosition == Position.RIGHT) 
                pan.setValue(1.0f);
            else if (curPosition == Position.LEFT) 
                pan.setValue(-1.0f);
        } 
 
        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
 
        try { 
            while (nBytesRead != -1) { 
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) 
                    auline.write(abData, 0, nBytesRead);
            } 
        } catch (IOException e) { 
            e.printStackTrace();
        } finally { 
            auline.drain();
            auline.close();
            try {
				audioInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        } 
 
    } 
} 
