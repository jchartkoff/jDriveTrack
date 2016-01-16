package jdrivetrack;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

public class ImagePrint {
	
    public ImagePrint(Component comp) {
    	PrintService service = PrintServiceLookup.lookupDefaultPrintService();
	    DocPrintJob job = service.createPrintJob();
	    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
	    SimpleDoc doc = new SimpleDoc(new MyPrintable(comp), flavor, null);
	    try {
			job.print(doc, null);
		} catch (PrintException e) {
			e.printStackTrace();
		}
	}

	private class MyPrintable implements Printable {
		private Component comp = null;
		
		private MyPrintable(Component comp) {
			this.comp = comp;
		}
		
		@Override
		public int print(Graphics g, PageFormat pf, int pageIndex) {
			Graphics2D g2d = (Graphics2D) g;
			g.translate((int) (pf.getImageableX()), (int) (pf.getImageableY()));
			if (pageIndex == 0) {
				double pageWidth = pf.getImageableWidth();
				double pageHeight = pf.getImageableHeight();
				double imageWidth = comp.getWidth();
				double imageHeight = comp.getHeight();
				double scaleX = pageWidth / imageWidth;
				double scaleY = pageHeight / imageHeight;
				double scaleFactor = Math.min(scaleX, scaleY);
				g2d.scale(scaleFactor, scaleFactor);
				comp.paint(g2d);  
				return Printable.PAGE_EXISTS;
			}
			return Printable.NO_SUCH_PAGE;
		}
	}
}
