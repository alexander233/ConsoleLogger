/**
 * Copyright (c) 2017 alexander233
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.slf4j.console;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import javax.swing.JSpinner;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.w3c.dom.css.Rect;

import javax.swing.JToggleButton;
import javax.swing.JRadioButton;

/**
 * This class provides a user interface for configuring logger settings using the Java preferences mechanism. 
 *  
 * Used to configure default logger settings using the Java preferences mechanism.
 * 
 * The frame allows setting whether a time stamp should be included and to modify the width of the logger 
 * columns. Logger names can be entered using their fully qualified name. 
 * 
 * For a specification of the keys needed within the Preferences structure, see the constants which are defined in the 
 * {@link ConsoleLogger} class.  
 * 
 *  */

public class ConfigFrame extends JFrame implements ActionListener, ItemListener {

	private static final long serialVersionUID = -868916360726501769L;

	/** This string indicates the null group (i.e. no preferences group is currently activated) 
	 *  This is always the first string in the combo box. */
	private static final String NO_GROUP = "-- ( No loggers active ) --";
	
	/** Lists the available defaults. */
	private JComboBox<String> comboBox;
	private JSpinner spinner; 
	private JTextArea txLoggers;	
	private JCheckBox chkTimestamp;
	private JLabel lblNameColumnWidth; 
	private JButton btnAdd; 
	private JButton btnDelete;
	private JButton btnSave; 
	private JButton btnMenu;
	
	private JPopupMenu popup; 
	
	private transient Preferences prefRoot; 
	
	public ConfigFrame()  {
		
	    prefRoot = Preferences.userNodeForPackage(ConsoleLoggerConfiguration.class);
		
		this.setTitle("Console Logger Configuration");	
		
		this.setMinimumSize(new Dimension(300, 300));		
		
		addWindowListener(new WindowAdapter(){
			@Override
            public void windowClosing(WindowEvent e){
            	saveBounds();
            }
        });		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.5, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);

		SpinnerModel sm = new SpinnerNumberModel(ConsoleLogger.DEFAULT_LOGGER_COLUMN_WIDTH, 0, 99, 1);
		
		
		comboBox = new JComboBox<>();
		comboBox.setFont(new Font(comboBox.getFont().getFontName(),Font.PLAIN, 14));
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"(Empty)"}));
		comboBox.addItemListener(this);
 		
        comboBox.setRenderer(new DefaultListCellRenderer() {
        	@Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value != null) {
            	  String group = prefRoot.get(ConsoleLogger.KEY_PREFERENCES_ATTRIBUTE_SELECTED_GROUP, NO_GROUP);
        		  Font font = getFont();            	  
            	  if (group.equals(value)) {
//            		  setText((String) value+"*");
            		  setFont(font.deriveFont(Font.BOLD,font.getSize()));
            	  } else {
//            		  setText((String) value);
            		  setFont(font.deriveFont(Font.PLAIN,font.getSize()));            		  
            	  }
              }
              return this;
            }
        });
		
        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.gridwidth = 2;
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.anchor = GridBagConstraints.WEST;
        gbc_comboBox.insets = new Insets(3, 3, 5, 5);
        gbc_comboBox.gridx = 0;
        gbc_comboBox.gridy = 2;
        getContentPane().add(comboBox, gbc_comboBox);

        btnAdd = new JButton("Add");
        btnAdd.addActionListener(this);
        GridBagConstraints gbc_btnAdd = new GridBagConstraints();
        gbc_btnAdd.insets = new Insets(3, 3, 5, 5);
        gbc_btnAdd.gridx = 2;
        gbc_btnAdd.gridy = 2;
        getContentPane().add(btnAdd, gbc_btnAdd);


        btnDelete = new JButton("Delete");
        btnDelete.addActionListener(this);
        GridBagConstraints gbc_btnDelete = new GridBagConstraints();
        gbc_btnDelete.insets = new Insets(3, 0, 5, 5);
        gbc_btnDelete.gridx = 3;
        gbc_btnDelete.gridy = 2;
        getContentPane().add(btnDelete, gbc_btnDelete);

        btnMenu = new JButton("\u2630");
        btnMenu.addActionListener(this);
        //btnMenu.setMargin(new Insets(0, 0, 0, 0));
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.insets = new Insets(3, 3, 5, 3);
        gbc_button.gridx = 4;
        gbc_button.gridy = 2;
        //btnMenu.setVisible(false);
        getContentPane().add(btnMenu, gbc_button);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 5;
        gbc_scrollPane.insets = new Insets(3, 3, 5, 3);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 3;
        getContentPane().add(scrollPane, gbc_scrollPane);

        txLoggers = new JTextArea();
        txLoggers.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txLoggers.setMargin( new Insets(0,3,0,3));
        scrollPane.setViewportView(txLoggers);


        lblNameColumnWidth = new JLabel("Name column width");
        GridBagConstraints gbc_lblNameColumnWidth = new GridBagConstraints();
        gbc_lblNameColumnWidth.anchor = GridBagConstraints.EAST;
        gbc_lblNameColumnWidth.insets = new Insets(5, 5, 5, 5);
        gbc_lblNameColumnWidth.gridx = 0;
        gbc_lblNameColumnWidth.gridy = 4;
        getContentPane().add(lblNameColumnWidth, gbc_lblNameColumnWidth);
        spinner = new JSpinner(sm);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(4);
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.anchor = GridBagConstraints.WEST;
        gbc_spinner.insets = new Insets(5, 5, 5, 5);
        gbc_spinner.gridx = 1;
        gbc_spinner.gridy = 4;
        getContentPane().add(spinner, gbc_spinner);

        chkTimestamp = new JCheckBox("Timestamp");
        GridBagConstraints gbc_chkTimestamp = new GridBagConstraints();
        gbc_chkTimestamp.anchor = GridBagConstraints.WEST;
        gbc_chkTimestamp.insets = new Insets(5, 5, 5, 5);
        gbc_chkTimestamp.gridx = 2;
        gbc_chkTimestamp.gridy = 4;
        getContentPane().add(chkTimestamp, gbc_chkTimestamp);

        btnSave = new JButton("Save");
        btnSave.addActionListener(this);
        GridBagConstraints gbc_btnOk = new GridBagConstraints();
        gbc_btnOk.insets = new Insets(5, 5, 5, 5);
        gbc_btnOk.gridx = 3;
        gbc_btnOk.gridy = 4;
        getContentPane().add(btnSave, gbc_btnOk);

	}

	
	private void restoreBounds() {
	      int left = prefRoot.getInt("left", 0);
	      int top = prefRoot.getInt("top", 0);
	      int width = prefRoot.getInt("width", 350);
	      int height = prefRoot.getInt("height", 400);
	      
	      
	      if (isPointOnScreen(new Point(left,top))) {
	    	  setBounds(left, top, width, height);
	      }
	}
	
	private void saveBounds() {
		Rectangle r = this.getBounds();
		prefRoot.putInt("left", r.x);
		prefRoot.putInt("top", r.y);
		prefRoot.putInt("width",r.width);
		prefRoot.putInt("height",  r.height);
	}
	
	private boolean isPointOnScreen(Point point) {
		 for (GraphicsDevice it : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			 if (it.getDefaultConfiguration().getBounds().contains(point)) return true; 
		 }
		 return false; 
	}
	
	@Override
	public void actionPerformed(ActionEvent arg) {
		if (btnSave == arg.getSource()) {
			requestSaveGroup();
		} else if (btnAdd == arg.getSource()) {
			comboBox.setSelectedIndex(-1);
			comboBox.setEditable(true);
			comboBox.getEditor().setItem("");
			txLoggers.setText("");
			txLoggers.setEditable(true);
		} else if (btnDelete == arg.getSource()) {			
			String group = (String) comboBox.getSelectedItem();
			if (group != null && !group.isEmpty() && !NO_GROUP.equals(group)) {
				prefRoot.remove(ConsoleLogger.KEY_PREFERENCES_ATTRIBUTE_SELECTED_GROUP);
				try {
					prefRoot.node(group).removeNode();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
				loadFromPreferences(); 
			}
		} else if (btnMenu == arg.getSource()) {
			createPopupMenu();
			popup.show(btnMenu, 10, 10);
		} else if (arg.getSource() instanceof JMenuItem) {
			switch (((JMenuItem) arg.getSource()).getActionCommand()) {
				case "logdef": Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
		                new StringSelection("static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyClassName.class);"),
		                null
		        );
				break; 
				case "about": JOptionPane.showMessageDialog(this, "ConsoleLogger for SLF4J (2018)\n\nAPI Version: "+ConsoleLoggerServiceProvider.REQUESTED_API_VERSION);
				       break; 
				case "import-all":
				case "export-all":
				default: JOptionPane.showMessageDialog(this, "Not yet implemented", "Error", JOptionPane.ERROR_MESSAGE);
				
				break; 
			}
		}
	}

	/** Loads (or reloads) content of this frame from the User Preferences */	
	private void loadFromPreferences() {
		String group = prefRoot.get(ConsoleLogger.KEY_PREFERENCES_ATTRIBUTE_SELECTED_GROUP, NO_GROUP);
		try {
			String[] children = prefRoot.childrenNames();
			if (children.length > 0) {
				Arrays.sort(children);
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(children);
				model.insertElementAt(NO_GROUP, 0);
				comboBox.setModel(model);			
				
				int idx = 0;
				for (int i=0;i<model.getSize();i++) {
					if (group.equals(model.getElementAt(i))) {
						idx = i; 
						break; 
					}
				}
				comboBox.setSelectedIndex(idx);
				
				comboBox.setEditable(false);				
				txLoggers.setEditable(idx > 0);
			} else {
				comboBox.setModel(new DefaultComboBoxModel<String>(new String[] { NO_GROUP }));
				comboBox.setEditable(true);				
				comboBox.getEditor().setItem("Group1");
				txLoggers.setEditable(true);				
			}
		} catch (BackingStoreException e) {
			e.printStackTrace(); //TODO: handle!
		} 
		
		loadGroup((String) comboBox.getSelectedItem()); 
	}
	
	private void requestSaveGroup() {
		
		String group;
		
		if (comboBox.isEditable()) { //we are adding a group
			group = (String) comboBox.getEditor().getItem();
			if (group == null || group.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please enter a group name");
				return;
			}
			
			List<String> sorted = new ArrayList<>();			
			for (int i=0;i<comboBox.getItemCount();i++) {
				
				if (group.equalsIgnoreCase((String) comboBox.getItemAt(i))) {
					JOptionPane.showMessageDialog(this, "Duplicate group name: "+comboBox.getItemAt(i)+". Please assign a different group name.");					
					return;
				} 
				sorted.add((String) comboBox.getItemAt(i));
			}
			
			sorted.add(group);
			sorted.sort(Collator.getInstance());
			
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(sorted.toArray(new String[0]));
			model.insertElementAt(NO_GROUP, 0);
			comboBox.setModel(model);
			comboBox.setSelectedIndex(sorted.indexOf(group)+1);
			
			comboBox.setEditable(false);
			comboBox.repaint();
			
		} 
		
		saveToPreferences((String) comboBox.getSelectedItem());
		loadGroup((String) comboBox.getSelectedItem());
	}
	
	/** Loads the specified group and rearranges controls based on the selected group. 
	 *  The only control that is not reloaded is the combo box.
	 *  This method may not be called if we are in the 'add' state, because it reload 
	 *  the content which is currently being edited.  
	 *  @param group Name of the currently selected group in the combo box.  */
	private void loadGroup(String group) {
		if (NO_GROUP.equals(group)) {
			chkTimestamp.setVisible(false);
			spinner.setVisible(false);
			lblNameColumnWidth.setVisible(false);
			txLoggers.setEditable(false);  
			txLoggers.setText(HELP_TEXT);
			txLoggers.setCaretPosition(0);
		} else {
			Preferences prefs = prefRoot.node(group);
			
			PrefProps pp = new PrefProps(prefs);
			txLoggers.setText(pp.getFormatted());
		
			spinner.setValue(prefs.getInt(ConsoleLogger.KEY_WIDTH, ConsoleLogger.DEFAULT_LOGGER_COLUMN_WIDTH));
			chkTimestamp.setSelected(prefs.getBoolean(ConsoleLogger.KEY_INCLUDE_TIME,false));
			
			chkTimestamp.setVisible(true);
			spinner.setVisible(true);
			lblNameColumnWidth.setVisible(true);
			txLoggers.setEditable(true);
		}
	}
	
	/** Saves the contents of the current page to the preferences (unless this is NO_GROUP) */
	private void saveToPreferences(String group) {
	
		if (null == group || group.isEmpty()) {
			return; //nothing to do
		}

		prefRoot.put(ConsoleLogger.KEY_PREFERENCES_ATTRIBUTE_SELECTED_GROUP, group);		
		
		if (NO_GROUP.equals(group)) { 
			return; 
		}
		
		Preferences prefs = prefRoot.node(group);		
		PrefProps pp = new PrefProps(txLoggers.getText());				
		pp.save(prefs);				
		

		if (ConsoleLogger.DEFAULT_LOGGER_COLUMN_WIDTH != (Integer) spinner.getValue()) {
			prefs.putInt(ConsoleLogger.KEY_WIDTH, (int) spinner.getValue());
		} else {
			prefs.remove(ConsoleLogger.KEY_WIDTH);
		}
		if (chkTimestamp.isSelected()) {
			prefs.putBoolean(ConsoleLogger.KEY_INCLUDE_TIME, true);
		} else {
			prefs.remove(ConsoleLogger.KEY_INCLUDE_TIME);
		}
	}	
	
	public void initialize() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.pack();
		//this.setLocationRelativeTo(null);
		this.setVisible(true);
		txLoggers.requestFocusInWindow();
		
		restoreBounds(); 

		if (ConsoleLoggerConfiguration.canUsePreferences()) {
			loadFromPreferences();
		} else {
			txLoggers.setText("You are running Java version 8 or lower on Windows. \n\nYour system is affected by a small Java bug which prevents us from providing you with our GUI-based logger configuration feature. Please use the other ways of logger configuration or install the current Java version. More information on the bug is available at: https://bugs.openjdk.java.net/browse/JDK-8139507\n\nPlease click the Cancel button to close this dialog.");
			txLoggers.setForeground(Color.RED);
			txLoggers.setLineWrap(true);
			txLoggers.setWrapStyleWord(true);
			txLoggers.setEditable(false);
			btnSave.setVisible(false);
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			//ignore
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				ConfigFrame frame = new ConfigFrame();
				frame.initialize();
			}
		});
	}

	@Override
	public void itemStateChanged(ItemEvent arg) {
		if (ItemEvent.SELECTED == arg.getStateChange() && !comboBox.isEditable()) {
			loadGroup((String) comboBox.getSelectedItem());
		}
	}	
	
	private static final String HELP_TEXT =
			"#  -- Console Logger Example --\n" + 
			"com.myapp.Maps : trace\n" +
			"   Controller\n" +
			"   MapImage : debug\n" +
			"   QueryGenerator\n" +
			"com.myapp.cache. : debug\n" +
			"   MapCache\n" +
			"   MapInfo\n" +
			"\n" +
			"# Specify loggers as: \n" + 
			"# [<package>[.]][<class>] [:<level>]\n" +
			"\n" +
			"# Classes can be specified without package\n"+ 
			"# name. They inherit the package from the\n"+ 
			"# preceding line (Controller in line 3 refers\n" +
		    "# to com.myapp.Controller). Packages ending\n" + 
			"# with '.' (line 6) are only used for deter-\n" +
		    "# mining package names on the following lines.\n" + 
		    "# They do not become their own logger! \n" +
		    "\n" + 
			"# Levels can be abbreviated (e.g. d=debug, or\n" +
			"# o=off). Lines without level inherit their\n" +
			"# level from the nearest preceding package\n" + 
			"# name: Therefore com.myapp.Controller and \n" + 
			"# com.myApp.QueryGenerator are both set to\n" +
			"# trace level. \n"  +
			"\n" +
			"# '#' refers to a comment line.\n" +
			"\n" +
			"# Only one configuration page is active at\n" +
			"# any time. The active configuration appears\n" +
			"# at startup and is shown in bold in the drop-\n" +
			"# down list. If this page appears at startup \n" + 
			"# then no loggers are enabled for console \n" + 
			"# logging. Click 'Save' on this page, to dis-\n" +
			"# able the other configurations. "
	; 

	private void createPopupMenu() {
		if (popup == null) {
			popup = new JPopupMenu(); 
			
			JMenuItem item = new JMenuItem("Copy logger code snippet");
			item.setActionCommand("logdef");
			item.addActionListener(this);
			popup.add(item);
			
			JMenu menu = new JMenu("Export");
			menu.addActionListener(this);
				JMenuItem child = new JMenuItem("all to file");
				child.setActionCommand("export-all");				
				child.addActionListener(this);
				menu.add(child);
			popup.add(menu);
			
			menu = new JMenu("Import");
			menu.addActionListener(this);
				child = new JMenuItem("all from file");
				child.setActionCommand("import-all");				
				child.addActionListener(this);
				menu.add(child);
			popup.add(menu);	
			
			
			item = new JMenuItem("About");
			item.setActionCommand("about");
			item.addActionListener(this);
			popup.add(item);
		}
	}

}
