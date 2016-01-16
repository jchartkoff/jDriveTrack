package jdrivetrack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class PreviewPrintPanel extends JDialog {
	private static final long serialVersionUID = -5504376931350424669L;

	public PreviewPrintPanel(BufferedImage image) {
		JPanel panel = new ImagePrint(image);
		paintImage(panel);
	}
	
	public PreviewPrintPanel(JPanel panel) {
		paintImage(panel);
	}
		
	private void paintImage(JPanel panel) {
		JPanel m_panel = panel;
		
		m_panel.setLayout(new FlowLayout());
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Print Preview");

        m_panel.setBackground(new Color(255, 255, 255));
        m_panel.setOpaque(true);
        m_panel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 255)));
 
        add(m_panel);

        pack();

		setLocation((screenSize.width / 2) - (getWidth() / 2),
				(screenSize.height / 2) - (getHeight() / 2));
		
		setVisible(true);

	}
	
	@Override
	public Dimension getPreferredSize() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		return new Dimension((int) (screenSize.width * 0.9), (int) (screenSize.height * 0.9));
	}
	
    private class ImagePrint extends JPanel {
        private static final long serialVersionUID = 1L;
        private BufferedImage image;
        private int width;
        private int height;

        private ImagePrint(BufferedImage image) {
            this.image = image;
            width = image.getWidth();
            height =  image.getHeight();
            setSize(width, height);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(image, 0, 0, width, height, null);
        }
    }
	
}