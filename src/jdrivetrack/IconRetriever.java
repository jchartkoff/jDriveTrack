package jdrivetrack;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

public class IconRetriever extends SwingWorker<BufferedImage, Void> {
	private String strImageFile;
	private Object object;
    
    public IconRetriever(Object object, String strImageFile) {
        this.strImageFile = strImageFile;
        this.object = object;
        execute();
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
    	try {
    		File imageFile = new File(strImageFile);
    		return ImageIO.read(imageFile);
    	}
    	catch (Exception ex) {
    		System.err.println(strImageFile);
    		return Utility.getDefaultIcon(new Dimension(16,16));
    	}
    }
		
    @Override
    protected void done() {
    	BufferedImage image = null;
        try {
            image = get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (object instanceof JButton) ((JButton) object).setIcon(new ImageIcon(image));
        if (object instanceof JMenuItem) ((JMenuItem) object).setIcon(new ImageIcon(image));
        if (object instanceof JFrame) ((JFrame) object).setIconImage(image);
        if (object instanceof JToggleButton) ((JToggleButton) object).setIcon(new ImageIcon(image));
    }
    
}
