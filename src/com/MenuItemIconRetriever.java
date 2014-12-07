package com;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

public class MenuItemIconRetriever extends SwingWorker<Icon, Void> {
	private String strImageFile;
	private JMenuItem mnu;
    
    public MenuItemIconRetriever(JMenuItem mnu, String strImageFile) {
        this.strImageFile = strImageFile;
        this.mnu = mnu;
        execute();
    }
    
    @Override
    protected Icon doInBackground() throws Exception {
    	return new ImageIcon(getClass().getResource(strImageFile));
    }

    @Override
    protected void done() {
        Icon icon = null;
        try {
            icon = get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mnu.setIcon(icon);
    }
}
