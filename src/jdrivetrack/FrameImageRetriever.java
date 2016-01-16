package jdrivetrack;

import java.awt.Frame;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

public class FrameImageRetriever extends SwingWorker<Image, Void> {
	private String strImageFile;
	private Frame frame;
    
    public FrameImageRetriever(Frame frame, String strImageFile) {
        this.strImageFile = strImageFile;
        this.frame = frame;
        execute();
    }
    
    @Override
    protected Image doInBackground() throws Exception {
        return ImageIO.read(getClass().getResource(strImageFile));
    }

    @Override
    protected void done() {
        Image image = null;
        try {
            image = get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frame.setIconImage(image);
    }
}
