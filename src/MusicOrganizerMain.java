/**
 * Description: This is the main class that initializes the Swing and AWT components and frame.
 * It then setups up all button action listeners with their specific functionality.
 * 
 * Notes: Bugs that need working:
 * 		Closing JFrame not ending process in Java SE Binary (Might be fixed)
 * 		Some organized songs are being renamed to Jibberish once organized, I forget why, but it is a known bug. Can be fixed, something to do with 
 * 		
 * 
 * 
 * @author Josh Bacon
 * @version 1.0
 */

import java.awt.EventQueue;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JTextPane;

import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JRadioButtonMenuItem;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JProgressBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
//import java.lang.Thread;

import javax.swing.ButtonGroup;
import javax.swing.text.DefaultCaret;

import java.awt.FlowLayout;


public class MusicOrganizerMain{
	public static ExecutorService threadPool;
	public static Path rootMusicPath;
	public static boolean removeEmpty = true;
	public static boolean editSongTags = true;
	public static boolean downloadTags = false;
	public static boolean organizeRecent = false;
	public static boolean ignorePrevOrg = false;
	public static boolean saveHistory = false;

	public static String option1 = "Genre";
	public static String option2 = "Artist";
	public static String option3 = "Null";
	public static int numTasks = 0;
	public static Semaphore sem;
	public static Semaphore semTextAppending;
	public static Date currentDate = new Date();
	public static JTextArea activitiesLog = new JTextArea();
	
	private JFrame frame = new JFrame();
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JFileChooser fileChooser;
	private JScrollPane scrollPane = new JScrollPane(activitiesLog, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private GridBagConstraints gbc_scrollPane = new GridBagConstraints();
	private JTextPane progDescription = new JTextPane();
	private GridBagConstraints gbc_progDescription = new GridBagConstraints();
	private JButton folderSelectButton = new JButton("Select Music Folder");
	private GridBagConstraints gbc_folderSelectButton = new GridBagConstraints();
	private JPanel settingsPanel = new JPanel();
	private GridBagConstraints gbc_settingsPanel = new GridBagConstraints();
	private JTextArea musicDirText = new JTextArea();
	private final JButton option1Button = new JButton("Genre");
	private JTextArea dividerText = new JTextArea();
	private JTextArea dividerText2 = new JTextArea();
	private JTextArea dividerText3 = new JTextArea();
	private final JButton option2Button = new JButton("Artist");
	private final JButton option3Button = new JButton("Null");
	private JCheckBox fldrRmCheckBox = new JCheckBox("Remove Empty Folders?");
	private GridBagConstraints gbc_fldrRmCheckBox = new GridBagConstraints();
	private JCheckBox editTagsCheckBox = new JCheckBox("Convert Tags to ID3v24?");
	private GridBagConstraints gbc_editTagsCheckBox = new GridBagConstraints();
	private JCheckBox downloadCheckBox = new JCheckBox("Download Missing Tags?");
	private GridBagConstraints gbc_downloadCheckBox = new GridBagConstraints();
	private JCheckBox organizeRecentCheckBox = new JCheckBox("Only Organize Recently Modified Files?");
	private GridBagConstraints gbc_organizeRecentCheckBox = new GridBagConstraints();
	private JCheckBox ignorePrevOrgCheckBox = new JCheckBox("Only Organize Unorganized Mp3s?");
	private GridBagConstraints gbc_ignorePrevOrgCheckBox = new GridBagConstraints();
	private JCheckBox saveHistoryCheckBox = new JCheckBox("Save record of organized files?");
	private GridBagConstraints gbc_saveHistoryCheckBox = new GridBagConstraints();
	private JButton organizerButton = new JButton("Start Organizing!");
	private GridBagConstraints gbc_organizerButton = new GridBagConstraints();
	private JProgressBar progBar = new JProgressBar();
	private GridBagConstraints gbc_progBar = new GridBagConstraints();
	private ButtonGroup option1Group = new ButtonGroup();
	private ButtonGroup option2Group = new ButtonGroup();
	private ButtonGroup option3Group = new ButtonGroup();
	private JRadioButtonMenuItem rdbtnmnrmOption1_Genre = new JRadioButtonMenuItem("Genre");
	private JRadioButtonMenuItem rdbtnmnrmOption1_Artist = new JRadioButtonMenuItem("Artist");
	private JRadioButtonMenuItem rdbtnmnrmOption1_Album = new JRadioButtonMenuItem("Album");
	private JRadioButtonMenuItem rdbtnmnrmOption1_Null = new JRadioButtonMenuItem("Null");
	private JRadioButtonMenuItem rdbtnmnrmOption2_Genre = new JRadioButtonMenuItem("Genre");
	private JRadioButtonMenuItem rdbtnmnrmOption2_Artist = new JRadioButtonMenuItem("Artist");
	private JRadioButtonMenuItem rdbtnmnrmOption2_Album = new JRadioButtonMenuItem("Album");
	private JRadioButtonMenuItem rdbtnmnrmOption2_Null = new JRadioButtonMenuItem("Null");
	private JRadioButtonMenuItem rdbtnmnrmOption3_Genre = new JRadioButtonMenuItem("Genre");
	private JRadioButtonMenuItem rdbtnmnrmOption3_Artist = new JRadioButtonMenuItem("Artist");
	private JRadioButtonMenuItem rdbtnmnrmOption3_Album = new JRadioButtonMenuItem("Album");
	private JRadioButtonMenuItem rdbtnmnrmOption3_Null = new JRadioButtonMenuItem("Null");
	private final JPopupMenu option1Menu = new JPopupMenu();
	private final JPopupMenu option2Menu = new JPopupMenu();
	private final JPopupMenu option3Menu = new JPopupMenu();
	
	
	/**
	 * Main of class, which creates a new MusicOrganizerMain object and then
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MusicOrganizerMain window = new MusicOrganizerMain();
					window.frame.pack();
					window.frame.setVisible(true);
					window.frame.setMinimumSize(window.frame.getSize());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This is the class constructor, which calls initialize() and setupAllButtonListeners() which essentially builds the UI 
	*/
	public MusicOrganizerMain() {
		initializeUI();
		setupAllButtonListeners();
	}
	
	/**
	 * Sets up all the button listeners and their actions performed, each button has its own listener. 
	 * The start Organization button does most of the 
	 */
	private void setupAllButtonListeners() {
		folderSelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Select Music folder to be organized");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnedVal = fileChooser.showOpenDialog(null);
				if(returnedVal == JFileChooser.APPROVE_OPTION) {
					rootMusicPath = Paths.get(fileChooser.getSelectedFile().getAbsolutePath());
					//rootMusicPath = new File(fileChooser.getSelectedFile().getAbsolutePath());
					activitiesLog.setText("Selected Music Folder: "+fileChooser.getSelectedFile().getAbsolutePath()+"\n");
					//musicDir = fileChooser.getSelectedFile().getAbsolutePath();
				}
				else {
					//No Selection activities log
				}
			}
		});
		
		option1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				option1Menu.show((Component)e.getSource(), 0, 0);
			}
		});
		option2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				option2Menu.show((Component)e.getSource(), 0, 0);
			}
		});
		option3Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				option3Menu.show((Component)e.getSource(), 0, 0);
			}
		});
		
		rdbtnmnrmOption1_Genre.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option1 = "Genre";
					option1Button.setText("Genre");
				}
			}
		});
		rdbtnmnrmOption1_Artist.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option1 = "Artist";
					option1Button.setText("Artist");
				}
			}
		});
		rdbtnmnrmOption1_Album.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option1 = "Album";
					option1Button.setText("Album");
				}
			}
		});
		rdbtnmnrmOption1_Null.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option1 = "Null";
					option1Button.setText("Null");
				}
			}
		});
		rdbtnmnrmOption2_Genre.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option2 = "Genre";
					option2Button.setText("Genre");
				}
			}
		});
		rdbtnmnrmOption2_Artist.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option2 = "Artist";
					option2Button.setText("Artist");
				}
			}
		});
		rdbtnmnrmOption2_Album.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option2 = "Album";
					option2Button.setText("Album");
				}
			}
		});
		rdbtnmnrmOption2_Null.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option2 = "Null";
					option2Button.setText("Null");
				}
			}
		});
		rdbtnmnrmOption3_Genre.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option3 = "Genre";
					option3Button.setText("Genre");
				}
			}
		});
		rdbtnmnrmOption3_Artist.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();   
				if(state == ItemEvent.SELECTED) {
					option3 = "Artist";
					option3Button.setText("Artist");
				}
			}
		});
		rdbtnmnrmOption3_Album.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option3 = "Album";
					option3Button.setText("Album");
				}
			}
		});
		rdbtnmnrmOption3_Null.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					option3 = "Null";
					option3Button.setText("Null");
				}
			}
		});
		fldrRmCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					removeEmpty = true;
				}
				else
					removeEmpty = false;
			}
		});
		
		editTagsCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) 74itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					editSongTags = true;
				}
				else
					editSongTags = false;
			}
		});
		downloadCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					downloadTags = true;
				}
				else 
					downloadTags = false;
			}
		});
		organizeRecentCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					organizeRecent = true;
				}
				else
					organizeRecent = false;
			}
		});
		ignorePrevOrgCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					ignorePrevOrg = true;
				}
				else
					ignorePrevOrg = false;
			}
		});
		saveHistoryCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				//AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				if(state == ItemEvent.SELECTED) {
					saveHistory = true;
				}
				else
					saveHistory = false;
			}
		});
		
		//First: Counts number of processers to determine max number of threads to use
		//Second: Outputs this information to the UI activity log
		//Third: Creates threadpool and initializes semaphore
		//Fourth: Runs an Runnable implementation of MusicOrganizer with the specific library path
		//Fifth: Awaits for threadpool to fininish
		//Sixth: Begins removal of empty folders and outputs result
		organizerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if(rootMusicPath != null) {
						if(saveHistory == true) {
							//if()
						}
						if(ignorePrevOrg == true) {
							
						}
						activitiesLog.append("Starting Organizer..."+"\n");
						int cpuCores = Runtime.getRuntime().availableProcessors();
						activitiesLog.append("Number of processors = "+cpuCores+"\n");
						threadPool = Executors.newFixedThreadPool(cpuCores -1);
						sem = new Semaphore(1);
						semTextAppending = new Semaphore(1);
						activitiesLog.append("In Main Thread ID: "+Thread.currentThread().getId()+"\n");
						Runnable worker = new MusicOrganizer(rootMusicPath);
						MusicOrganizerMain.sem.acquire();
						MusicOrganizerMain.numTasks++;
						MusicOrganizerMain.threadPool.execute(worker);
						MusicOrganizerMain.sem.release();
						while(numTasks > 0) {
							Thread.sleep(2000);
						}
						threadPool.shutdown();
						threadPool.awaitTermination(60, TimeUnit.SECONDS);
						/*
						while(!threadPool.isTerminated()) {
							Thread.sleep(500);
						}
						*/
						System.out.println(removeEmpty);
						if(removeEmpty == true) {
							MusicOrganizer organizer = new MusicOrganizer(rootMusicPath);
							System.out.println("Starting Removal of Empty Folders...");
							activitiesLog.append("Starting Removal of Empty Folders...\n");
							organizer.removeEmptyFolders(rootMusicPath);
							System.out.println("Finished Removal of Empty Folders.");
							activitiesLog.append("Finished Removal of Empty Folders\n");
						}
						
						activitiesLog.append("Finished Organizing\n");
						//Add progress text
						//Add Text: Done
					}
					else {
						activitiesLog.append("Please select a music folder path and try starting organizer again\n");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	/**
	 * Description: Initializes the UI of the program. For every component, gridBagLayout and gridBagConstraints are used, 
	 * with weighted x and y values in proportion to the rows and columns
	 */
	//Initialize the frame/panels/buttons/textAreas
	private void initializeUI() {
		frame.getContentPane().setLayout(gridBagLayout);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		activitiesLog.setWrapStyleWord(true);
		activitiesLog.setLineWrap(true);
		activitiesLog.setText("Activities Log: ");
		activitiesLog.setEditable(false);
		scrollPane.setWheelScrollingEnabled(true);
		DefaultCaret caret = (DefaultCaret)activitiesLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		gbc_scrollPane.insets = new Insets(0, 0, 8, 0);
		gbc_scrollPane.gridheight = GridBagConstraints.REMAINDER;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.weightx = 1;
		gbc_scrollPane.weighty = 1;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 0;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);
		
		//progDescription.setMinimumSize(new Dimension(50,25));
		//progDescription.setWrapStyleWord(true);
		//progDescription.setLineWrap(true);
		//progDescription.setFont(new Font("Dialog", Font.BOLD, 12));
		//EditorKit kit = new EditorKit();
		//progDescription.setEditorKit(kit);
		progDescription.setBackground(frame.getBackground());
		progDescription.setText("Program Description: Renames and organizes Mp3 files from a Directory into appropirate folders.");
		//progDescription.append("Program Description: ");
		//progDescription.setFont(new Font("Dialog", Font.PLAIN, 12));
		//progDescription.append("Renames and organizes Mp3 files from a Directory into appropirate folders");
		progDescription.setEditable(false);
		progDescription.setPreferredSize(new Dimension(100,50));
		gbc_progDescription.insets = new Insets(0, 0, 8, 8);
		//gbc_progDescription.insets = new Insets(0, 0, 0, 0);
		gbc_progDescription.fill = GridBagConstraints.BOTH;
		gbc_progDescription.weightx = 0;
		gbc_progDescription.weighty = .1;
		gbc_progDescription.gridx = 0;
		gbc_progDescription.gridy = 0;
		frame.getContentPane().add(progDescription, gbc_progDescription);
		
		//folderSelectButton.setMinimumSize(new Dimension(50,25));
		folderSelectButton.setFont(new Font("Dialog", Font.BOLD, 14));
		gbc_folderSelectButton.insets = new Insets(8, 8, 8, 8);
		gbc_folderSelectButton.fill = GridBagConstraints.BOTH;
		gbc_folderSelectButton.weightx = 0;
		gbc_folderSelectButton.weighty = .01;
		gbc_folderSelectButton.gridx = 0;
		gbc_folderSelectButton.gridy = 1;
		frame.getContentPane().add(folderSelectButton, gbc_folderSelectButton);
		/*
		JButton settingsButton = new JButton("Organizer Path Settings");
		settingsButton.setFont(new Font("Dialog", Font.BOLD, 16));
		//settingsButton.setMinimumSize(new Dimension(50,25));
		GridBagConstraints gbc_settingsButton = new GridBagConstraints();
		gbc_settingsButton.insets = new Insets(8, 8, 8, 8);
		gbc_settingsButton.fill = GridBagConstraints.BOTH;
		gbc_settingsButton.weightx = 0;
		gbc_settingsButton.weighty = .1;
		gbc_settingsButton.gridx = 0;
		gbc_settingsButton.gridy = 2;
		frame.getContentPane().add(settingsButton, gbc_settingsButton);
		*/
		
		//Settings panel indicates how/where to organize mp3 files under the directory specified.
			settingsPanel.setPreferredSize(new Dimension(100,25));
			gbc_settingsPanel.fill = GridBagConstraints.BOTH;
			gbc_settingsPanel.insets = new Insets(8, 8, 8, 8);
			gbc_settingsPanel.weightx = 0;
			gbc_settingsPanel.weighty = .01;
			gbc_settingsPanel.gridx = 0;
			gbc_settingsPanel.gridy = 3;
			frame.getContentPane().add(settingsPanel, gbc_settingsPanel);
			
			settingsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			
			musicDirText.setFont(new Font("Dialog", Font.BOLD, 12));
			musicDirText.setBackground(frame.getBackground());
			musicDirText.setText("Directory/.../");
			
			option1Button.setMargin(new Insets(0,0,0,0));
			option1Button.setFont(new Font("Dialog", Font.PLAIN, 12));
			
			dividerText.setFont(new Font("Dialog", Font.BOLD, 12));
			dividerText.setBackground(frame.getBackground());
			dividerText.setText("/");
			
			dividerText2.setFont(new Font("Dialog", Font.BOLD, 12));
			dividerText2.setBackground(frame.getBackground());
			dividerText2.setText("/");
			
			dividerText3.setFont(new Font("Dialog", Font.BOLD, 12));
			dividerText3.setBackground(frame.getBackground());
			dividerText3.setText("/");
			
			option2Button.setMargin(new Insets(0,0,0,0));
			option2Button.setFont(new Font("Dialog", Font.PLAIN, 12));
			
			option3Button.setMargin(new Insets(0,0,0,0));
			option3Button.setFont(new Font("Dialog", Font.PLAIN, 12));
			
			settingsPanel.add(musicDirText);
			settingsPanel.add(option1Button);
			settingsPanel.add(dividerText);
			settingsPanel.add(option2Button);
			settingsPanel.add(dividerText2);
			settingsPanel.add(option3Button);
			settingsPanel.add(dividerText3);
			frame.getContentPane().add(settingsPanel, gbc_settingsPanel);
		
		//fldrRmCheckBox.setMinimumSize(new Dimension(50,25));
		fldrRmCheckBox.setToolTipText("<HTML>Activate if you want to<br>remove empty folders<br>after finishing organization </HTML>");
		fldrRmCheckBox.setFont(new Font("Dialog", Font.BOLD, 12));
		fldrRmCheckBox.setSelected(true);
		gbc_fldrRmCheckBox.insets = new Insets(8, 8, 8, 8);
		gbc_fldrRmCheckBox.fill = GridBagConstraints.BOTH;
		gbc_fldrRmCheckBox.weightx = 0;
		gbc_fldrRmCheckBox.weighty = .01;
		gbc_fldrRmCheckBox.gridx = 0;
		gbc_fldrRmCheckBox.gridy = 4;
		frame.getContentPane().add(fldrRmCheckBox, gbc_fldrRmCheckBox);
		
		//editTagsCheckBox.setMinimumSize(new Dimension(50,25));
		editTagsCheckBox.setToolTipText("<HTML>Activate if you want to<br>edit mp3 tags to be<br>uniformed to IDv3.24</HTML>");
		editTagsCheckBox.setFont(new Font("Dialog", Font.BOLD, 12));
		editTagsCheckBox.setSelected(true);
		gbc_editTagsCheckBox.insets = new Insets(8, 8, 8, 8);
		gbc_editTagsCheckBox.fill = GridBagConstraints.BOTH;
		gbc_editTagsCheckBox.weightx = 0;
		gbc_editTagsCheckBox.weighty = .01;
		gbc_editTagsCheckBox.gridx = 0;
		gbc_editTagsCheckBox.gridy = 5;
		frame.getContentPane().add(editTagsCheckBox, gbc_editTagsCheckBox);
		
		//downloadCheckBox.setMinimumSize(new Dimension(50,25));
		downloadCheckBox.setToolTipText("<HTML>Check if you want to..<br>download missing mp3 tags<br>from the internet<HTML>");
		downloadCheckBox.setFont(new Font("Dialog", Font.BOLD, 12));
		downloadCheckBox.setSelected(false);
		gbc_downloadCheckBox.insets = new Insets(8, 8, 8, 8);
		gbc_downloadCheckBox.fill = GridBagConstraints.BOTH;
		gbc_downloadCheckBox.weightx = 0;
		gbc_downloadCheckBox.weighty = .01;
		gbc_downloadCheckBox.gridx = 0;
		gbc_downloadCheckBox.gridy = 6;
		frame.getContentPane().add(downloadCheckBox, gbc_downloadCheckBox);
		
		//downloadCheckBox.setMinimumSize(new Dimension(50,25));
		organizeRecentCheckBox.setToolTipText("<HTML>Check if you want to..<br>only organize recently modified/created<br>mp3 files withn last 3 days (Quicker)<HTML>");
		organizeRecentCheckBox.setFont(new Font("Dialog", Font.BOLD, 12));
		organizeRecentCheckBox.setSelected(false);
		gbc_organizeRecentCheckBox.insets = new Insets(8, 8, 8, 8);
		gbc_organizeRecentCheckBox.fill = GridBagConstraints.BOTH;
		gbc_organizeRecentCheckBox.weightx = 0;
		gbc_organizeRecentCheckBox.weighty = .01;
		gbc_organizeRecentCheckBox.gridx = 0;
		gbc_organizeRecentCheckBox.gridy = 7;
		frame.getContentPane().add(organizeRecentCheckBox, gbc_organizeRecentCheckBox);
		
		//downloadCheckBox.setMinimumSize(new Dimension(50,25));
		ignorePrevOrgCheckBox.setToolTipText("<HTML>Check if you only want to.. <br>only organize items that are not<br>on record as already being organized (Quicker)<HTML>");
		ignorePrevOrgCheckBox.setFont(new Font("Dialog", Font.BOLD, 12));
		ignorePrevOrgCheckBox.setSelected(false);
		gbc_ignorePrevOrgCheckBox.insets = new Insets(8, 8, 8, 8);
		gbc_ignorePrevOrgCheckBox.fill = GridBagConstraints.BOTH;
		gbc_ignorePrevOrgCheckBox.weightx = 0;
		gbc_ignorePrevOrgCheckBox.weighty = .01;
		gbc_ignorePrevOrgCheckBox.gridx = 0;
		gbc_ignorePrevOrgCheckBox.gridy = 8;
		frame.getContentPane().add(ignorePrevOrgCheckBox, gbc_ignorePrevOrgCheckBox);
		
		//downloadCheckBox.setMinimumSize(new Dimension(50,25));
		saveHistoryCheckBox.setToolTipText("<HTML>Check if you only want to.. <br>save an organization record<br>so future organizations can exclude mp3s<br>that have already been organized (Quicker)<HTML>");
		saveHistoryCheckBox.setFont(new Font("Dialog", Font.BOLD, 12));
		saveHistoryCheckBox.setSelected(false);
		gbc_saveHistoryCheckBox.insets = new Insets(8, 8, 8, 8);
		gbc_saveHistoryCheckBox.fill = GridBagConstraints.BOTH;
		gbc_saveHistoryCheckBox.weightx = 0;
		gbc_saveHistoryCheckBox.weighty = .01;
		gbc_saveHistoryCheckBox.gridx = 0;
		gbc_saveHistoryCheckBox.gridy = 9;
		frame.getContentPane().add(saveHistoryCheckBox, gbc_saveHistoryCheckBox);
		
		organizerButton.setFont(new Font("Dialog", Font.BOLD, 14));
		//organizerButton.setMinimumSize(new Dimension(50,25));
		gbc_organizerButton.insets = new Insets(8, 8, 8, 8);
		gbc_organizerButton.fill = GridBagConstraints.BOTH;
		gbc_organizerButton.weightx = 0;
		gbc_organizerButton.weighty = .01;
		gbc_organizerButton.gridx = 0;
		gbc_organizerButton.gridy = 10;
		frame.getContentPane().add(organizerButton, gbc_organizerButton);
		
		gbc_progBar.insets = new Insets(8, 8, 8, 8);
		gbc_progBar.fill = GridBagConstraints.BOTH;
		gbc_progBar.weightx = 0;
		gbc_progBar.weighty = .01;
		gbc_progBar.gridx = 0;
		gbc_progBar.gridy = 11;
		frame.getContentPane().add(progBar, gbc_progBar);
		
		rdbtnmnrmOption1_Genre.setSelected(true);
		rdbtnmnrmOption2_Artist.setSelected(true);
		rdbtnmnrmOption3_Null.setSelected(true);
		
		option1Group.add(rdbtnmnrmOption1_Genre);
		option1Group.add(rdbtnmnrmOption1_Artist);
		option1Group.add(rdbtnmnrmOption1_Album);
		option1Group.add(rdbtnmnrmOption1_Null);
		option2Group.add(rdbtnmnrmOption2_Genre);
		option2Group.add(rdbtnmnrmOption2_Artist);
		option2Group.add(rdbtnmnrmOption2_Album);
		option2Group.add(rdbtnmnrmOption2_Null);
		option3Group.add(rdbtnmnrmOption3_Genre);
		option3Group.add(rdbtnmnrmOption3_Artist);
		option3Group.add(rdbtnmnrmOption3_Album);
		option3Group.add(rdbtnmnrmOption3_Null);
		
		option1Menu.add(rdbtnmnrmOption1_Genre);
		option1Menu.add(rdbtnmnrmOption1_Artist);
		option1Menu.add(rdbtnmnrmOption1_Album);
		option1Menu.add(rdbtnmnrmOption1_Null);
		
		option2Menu.add(rdbtnmnrmOption2_Genre);
		option2Menu.add(rdbtnmnrmOption2_Artist);
		option2Menu.add(rdbtnmnrmOption2_Album);
		option2Menu.add(rdbtnmnrmOption2_Null);
		
		option3Menu.add(rdbtnmnrmOption3_Genre);
		option3Menu.add(rdbtnmnrmOption3_Artist);
		option3Menu.add(rdbtnmnrmOption3_Album);
		option3Menu.add(rdbtnmnrmOption3_Null);
	}
}
