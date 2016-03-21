package jdrivetrack;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class DatabaseConfiguration extends JDialog {
	private static final long serialVersionUID = 7632585731422001608L;
	
	public static final String PROPERTY_CHANGE = "PROPERTY_CHANGE";
	
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	
	public static final String MYSQL_URL = "jdbc:mysql://localhost:3306";
	public static final String DERBY_URL = "jdbc:derby:";
	
	private JTabbedPane tabbedPane;
	private JPanel configPanel;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
    private JTextField cryptoKey;
    private JPanel databaseConnectionPanel;
    private JFormattedTextField passWord;
    private JPanel optionsPanel;
    private JLabel passWordLabel;
    private JPanel securityPanel;
    private JLabel sqlDatabaselUrlLabel;
    private JFormattedTextField sqlDatabaseDriver;
    private JLabel sqlDatabaseDriverLabel;
    private JFormattedTextField sqlDatabaseURL;
    private JCheckBox useAESEncryptionCheckBox;
    private JCheckBox useEmbeddedDatabase;
    private JFormattedTextField userName;
    private JLabel userNameLabel;

    private Preferences systemPrefs = Preferences.userRoot().node("jdrivetrack/prefs/DatabaseSettings");

	public DatabaseConfiguration(boolean clearAllPrefs) {
		if (clearAllPrefs) {
			try {
				systemPrefs.clear();
			} catch (BackingStoreException ex) {
				ex.printStackTrace();
			}
		}
		
		initComponents();
		configureComponents();
		getSettingsFromRegistry();
		createGUI();
	}	
                        
    private void initComponents() {
    	tabbedPane = new JTabbedPane();
    	configPanel = new JPanel();
    	okButton = new JButton("OK");
    	cancelButton = new JButton("Cancel");
    	applyButton = new JButton("Apply");
    	securityPanel = new JPanel();
	    userName = new JFormattedTextField();
	    passWord = new JFormattedTextField();
	    userNameLabel = new JLabel();
	    passWordLabel = new JLabel();
	    cryptoKey = new JTextField();
	    useAESEncryptionCheckBox = new JCheckBox();
	    databaseConnectionPanel = new JPanel();
	    sqlDatabaseURL = new JFormattedTextField();
	    sqlDatabaselUrlLabel = new JLabel();
	    sqlDatabaseDriver = new JFormattedTextField();
	    sqlDatabaseDriverLabel = new JLabel();
	    optionsPanel = new JPanel();
	    useEmbeddedDatabase = new JCheckBox();
    }	

    private void configureComponents() {
    	setTitle("Database Settings");
		
    	okButton.setMultiClickThreshhold(50L);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.doClick();
				setVisible(false);
			}
		});
		
		cancelButton.setMultiClickThreshhold(50L);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});
		
		applyButton.setMultiClickThreshhold(50L);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButtonActionListenerEvent(event);
			}
		});
		
		tabbedPane.addTab(" Database Configuration ", null, configPanel, null);
    	
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        useEmbeddedDatabase.setText("Use Internal Embedded Database");
        useEmbeddedDatabase.setToolTipText("");
	    
        databaseConnectionPanel.setBorder(BorderFactory.createTitledBorder("External Database Connection"));

        sqlDatabaselUrlLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        sqlDatabaselUrlLabel.setText("SQL Database URL");
        
        sqlDatabaseURL.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
        		sqlDatabaseUrlActionPerformed(evt);
            }
        });
        
        sqlDatabaseDriverLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        sqlDatabaseDriverLabel.setText("SQL Database Driver");

        sqlDatabaseDriver.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
        		sqlDatabaseDriverActionPerformed(evt);
            }
        });
        
        securityPanel.setBorder(BorderFactory.createTitledBorder("Security"));

        userName.setMinimumSize(new Dimension(150, 20));
        userName.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                userNameChangedEvent(evt);
            }
        });
        
        passWord.setMinimumSize(new Dimension(150, 20));
        passWord.addActionListener(new ActionListener() {
            @Override
        	public void actionPerformed(ActionEvent evt) {
                passWordChangedEvent(evt);
            }
        });

        userNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userNameLabel.setText("Username");

        passWordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passWordLabel.setText("Password");

        cryptoKey.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                cryptoKeyActionPerformed(evt);
            }
        });

        useAESEncryptionCheckBox.setText("Use AES Encryption");
        useAESEncryptionCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
    }
	
    private void userNameChangedEvent(ActionEvent evt) {                                                     
        
    }
    
    private void passWordChangedEvent(ActionEvent evt) {                                                     
        
    }                                                    

    private void cryptoKeyActionPerformed(ActionEvent evt) {                                          
        
    } 
    
    private void sqlDatabaseDriverActionPerformed(ActionEvent evt) {                                                  

    }                                                 

    private void sqlDatabaseUrlActionPerformed(ActionEvent evt) {                                               

    }  
    
    private void createGUI() {    
    	GroupLayout layout = new GroupLayout(getContentPane());

		getContentPane().setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(okButton, 90,90,90)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(applyButton, 90,90,90)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(cancelButton, 90,90,90)
				.addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(applyButton)
					.addComponent(cancelButton)
					.addComponent(okButton))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    	
        GroupLayout securityPanelLayout = new GroupLayout(securityPanel);
        
        securityPanel.setLayout(securityPanelLayout);
        
        securityPanelLayout.setHorizontalGroup(
            securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(securityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(passWordLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(useAESEncryptionCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userNameLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(passWord, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userName, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cryptoKey, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE))
                .addContainerGap()));
        
        securityPanelLayout.setVerticalGroup(
            securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(securityPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(securityPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(userName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(userNameLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(securityPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(passWord, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(passWordLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(securityPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(cryptoKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(useAESEncryptionCheckBox))
                .addContainerGap(18, Short.MAX_VALUE)));

        GroupLayout databaseConnectionPanelLayout = new GroupLayout(databaseConnectionPanel);
        
        databaseConnectionPanel.setLayout(databaseConnectionPanelLayout);
        
        databaseConnectionPanelLayout.setHorizontalGroup(
            databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(databaseConnectionPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(sqlDatabaselUrlLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sqlDatabaseDriverLabel, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(sqlDatabaseDriver, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                        .addComponent(sqlDatabaseURL, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
                    .addContainerGap())));
        
        databaseConnectionPanelLayout.setVerticalGroup(
            databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
            .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(databaseConnectionPanelLayout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(sqlDatabaseURL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(sqlDatabaselUrlLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(sqlDatabaseDriver, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(sqlDatabaseDriverLabel))
                    .addContainerGap(27, Short.MAX_VALUE))));

        GroupLayout optionsPanelLayout = new GroupLayout(optionsPanel);
        
        optionsPanel.setLayout(optionsPanelLayout);
        
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useEmbeddedDatabase, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useEmbeddedDatabase)
                .addContainerGap(46, Short.MAX_VALUE)));

        GroupLayout configPanelLayout = new GroupLayout(configPanel);
        
        configPanelLayout.setHorizontalGroup(
        	configPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(configPanelLayout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(configPanelLayout.createParallelGroup(Alignment.LEADING, false)
        				.addComponent(databaseConnectionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        				.addComponent(optionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        				.addComponent(securityPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap(43, Short.MAX_VALUE)));
        
        configPanelLayout.setVerticalGroup(
        	configPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(configPanelLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(databaseConnectionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        			.addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(securityPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap()));
        
        Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		
		pack();
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
    } 
    
	public void showSettingsDialog(boolean showSettingsDialog) {
		setVisible(showSettingsDialog);
	}
	
	private void getSettingsFromRegistry() {
		userName.setText(systemPrefs.get("UserName", ""));
		passWord.setText(systemPrefs.get("PassWord", ""));
		sqlDatabaseDriver.setText(systemPrefs.get("SqlDatabaseDriver", ""));
		sqlDatabaseURL.setText(systemPrefs.get("SqlDatabaseURL", ""));
		cryptoKey.setText(systemPrefs.get("AESKeyCypher", ""));
		useAESEncryptionCheckBox.setSelected(systemPrefs.getBoolean("UseAESKey", false));
		useEmbeddedDatabase.setSelected(systemPrefs.getBoolean("UseEmbeddedDatabase", false));
	}
	
	private void applyButtonActionListenerEvent(ActionEvent event) {
		systemPrefs.put("UserName", userName.getText());
		systemPrefs.put("PassWord", passWord.getText());
		systemPrefs.put("SqlDatabaseDriver", sqlDatabaseDriver.getText());
		systemPrefs.put("SqlDatabaseURL", sqlDatabaseURL.getText());
		systemPrefs.put("AESKeyCypher", cryptoKey.getText());
		systemPrefs.putBoolean("UseAESKey", useAESEncryptionCheckBox.isSelected());
		systemPrefs.putBoolean("UseEmbeddedDatabase", useEmbeddedDatabase.isSelected());
		firePropertyChange(PROPERTY_CHANGE, null, null);
	}
	
	public String getUserName() {
		return userName.getText();
	}
	
	public String getPassWord() {
		return passWord.getText();
	}
	
	public String getDriver() {
		return sqlDatabaseDriver.getText();
	}
	
	public String getURL() {
		return sqlDatabaseURL.getText();
	}
	
	public String getKey() {
		return cryptoKey.getText();
	}
	
	public boolean isUseAESKey() {
		return useAESEncryptionCheckBox.isSelected();
	}
	
	public boolean isUseEmbeddedDatabase() {
		return useEmbeddedDatabase.isSelected();
	}
	
}
