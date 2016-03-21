package jdrivetrack;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

public class IconRetriever extends SwingWorker<BufferedImage, Void> {
	private Object object;
	private File file;
    
    public IconRetriever(Object object, URL url) throws URISyntaxException {
        this.file = new File(url.toURI());
        this.object = object;
        execute();
    }
    
    public IconRetriever(Object object, String str) {
    	this.file = new File(str);
        this.object = object;
        execute();
    }
    
    @Override
    protected BufferedImage doInBackground() throws Exception {
    	try {
    		return ImageIO.read(file);
    	}
    	catch (Exception ex) {
    		System.err.println(file.getPath() + " not found");
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
