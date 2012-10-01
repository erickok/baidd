package nl.uu.cs.arg.platform.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.aspic.inference.Rule;

import nl.uu.cs.arg.platform.Launcher;
import nl.uu.cs.arg.platform.PlatformException;
import nl.uu.cs.arg.platform.PlatformListener;
import nl.uu.cs.arg.platform.PlatformStateMessage;
import nl.uu.cs.arg.platform.Settings;
import nl.uu.cs.arg.platform.gui.jung.DialogueDecorator;
import nl.uu.cs.arg.platform.gui.jung.DynamicTreeLayout;
import nl.uu.cs.arg.platform.gui.jung.DialogueDecorator.MoveStateEvaluator;
import nl.uu.cs.arg.platform.local.AgentXmlData;
import nl.uu.cs.arg.platform.local.MasXmlData;
import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueState;
import nl.uu.cs.arg.shared.dialogue.DialogueStateChangeMessage;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.Proposal;
import nl.uu.cs.arg.shared.dialogue.locutions.DeliberationLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.OpenDialogueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.ProposeLocution;
import nl.uu.cs.arg.shared.dialogue.protocol.DeliberationRule;
import nl.uu.cs.arg.shared.dialogue.protocol.TerminationRule;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;

public class DialogueViewer extends JFrame implements PlatformListener, MoveStateEvaluator {

	private static final long serialVersionUID = 1L;
	
	private final static String AGENT_TAB_NAME = "AGENT_TAB_";
	
	private Launcher platformLauncher;
	private MasXmlData masXml;
	private Dialogue dialogue;
	private DelegateTree<Move<? extends Locution>, Long> dialogueTree = new DelegateTree<Move<? extends Locution>, Long>();
	private boolean isPlatformRunning = false;
	
	// Models that contain the data for Lists in this JFrame
	private DefaultListModel moves = new DefaultListModel();
	private DefaultListModel messages = new DefaultListModel();
	private DefaultListModel deliberationRules = new DefaultListModel();
	private DefaultListModel terminationRules = new DefaultListModel();
	private File lastFileDialogPath = null;
	private File lastFileOpened = null;

	private Layout<Move<? extends Locution>, Long> dialogueLayout = null;
	private VisualizationViewer<Move<? extends Locution>, Long> dialogueVisViewer;
	private JPanel paneContent;
	private JScrollPane paneDialogue;
	private JTabbedPane paneTabs;
	private JMenuItem mntmReload;
	private JMenuItem mntmStartPause;
	private JMenuItem mntmStep;
	private JLabel lblDialogueStatus;
	private JLabel lblMasStatus;
	private JTextField txtLoadedMasFile;
	private JTextField txtDialogueTopic;
	private JTextField txtDialogueGoal;
	private JTextField txtOutcomeSelRule;
	private JButton btnStartPause;
	private JButton btnStep;
	private JTextField txtName;
	private JTextField txtID;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		// Set the look-n-feel of the app
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// See if we got a .baidd MAS file from the command line
		final File masFile = (args.length > 0? new File(args[0]): null);;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DialogueViewer frame = new DialogueViewer();
					frame.setVisible(true);
					if (masFile != null) {
						if (masFile.exists()) {
							frame.openMas(masFile);
						} else {
							frame.onMessagesReceived(Arrays.asList(new DialogueMessage("Requested to start '" + masFile.toString() + "' but it does not exist (relative to " + (new File(".")).getCanonicalPath().toString() + ").")));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public DialogueViewer() {
		setTitle(Settings.APPLICATION_NAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 953, 934);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnPlatform = new JMenu("Platform");
		menuBar.add(mnPlatform);
		
		JMenuItem mntmOpen = new JMenuItem("Open MAS...");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				/*FileDialog fd = new FileDialog(DialogueViewer.this, "Select the MAS to open", FileDialog.LOAD);
				fd.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".baidd");
					}
				});
				fd.setVisible(true);
				File file = new File(fd.getDirectory() + File.separator + fd.getFile());*/
				
				// Create a file chooser dialog (that defaults to show only .baidd files)
				JFileChooser fc = new JFileChooser(lastFileDialogPath);
				FileFilter baiddFilter = new FileFilter() {
					@Override
					public String getDescription() {
						return ".baidd MAS files";
					}
					
					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().endsWith(".baidd");
					}
				};
				fc.addChoosableFileFilter(baiddFilter);
				fc.setFileFilter(baiddFilter);
				if (fc.showOpenDialog(DialogueViewer.this) == JFileChooser.APPROVE_OPTION) {
					// Save the chosen directory to use the next time we start the file chooser dialog
					lastFileDialogPath = new File(fc.getSelectedFile().getParent());
					// Open and initialize the MAS
					openMas(fc.getSelectedFile());
				}
			}
		});
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		mnPlatform.add(mntmOpen);

		mntmReload = new JMenuItem("Reload MAS");
		mntmReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openMas(lastFileOpened);
			}
		});
		mntmReload.setEnabled(false);
		mntmReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		mnPlatform.add(mntmReload);
		
		JSeparator sep1 = new JSeparator();
		mnPlatform.add(sep1);
		
		mntmStartPause = new JMenuItem("Start");
		mntmStartPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!isPlatformRunning) {
					startPlatform();
				} else {
					pausePlatform();
				}
			}
		});
		mntmStartPause.setEnabled(false);
		mntmStartPause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		mnPlatform.add(mntmStartPause);
		
		mntmStep = new JMenuItem("Step");
		mntmStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stepPlatform();
			}
		});
		mntmStep.setEnabled(false);
		// Note that it is not mapped to F6 because that key is reserved by Swing for its UI components
		mntmStep.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		mnPlatform.add(mntmStep);
		
		JSeparator sep2 = new JSeparator();
		mnPlatform.add(sep2);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnPlatform.add(mntmExit);
		
		JMenu mnInfo = new JMenu("Info");
		menuBar.add(mnInfo);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AboutDialog about = new AboutDialog();
				about.setVisible(true);
			}
		});
		mnInfo.add(mntmAbout);
		paneContent = new JPanel();
		paneContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(paneContent);
		
		JSplitPane paneVerticalSplit = new JSplitPane();
		paneVerticalSplit.setResizeWeight(1.0);
		paneVerticalSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		JSplitPane paneHorizontalSplit = new JSplitPane();
		paneHorizontalSplit.setResizeWeight(1.0);
		paneVerticalSplit.setLeftComponent(paneHorizontalSplit);
		
		paneDialogue = new JScrollPane();
		paneDialogue.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		paneDialogue.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		paneHorizontalSplit.setLeftComponent(paneDialogue);
		
		
		JScrollPane paneMoves = new JScrollPane();
		paneHorizontalSplit.setRightComponent(paneMoves);
		
		JList lstMoves = new JList(moves);
		
		paneMoves.setViewportView(lstMoves);
		
		paneTabs = new JTabbedPane(JTabbedPane.TOP);
		paneTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		paneVerticalSplit.setRightComponent(paneTabs);
		
		JPanel paneTabMas = new JPanel();
		paneTabs.addTab("MAS", null, paneTabMas, null);
		
		JLabel lblLoadedMasFile = new JLabel("Loaded MAS file");
		
		JScrollPane paneDeliberationRules = new JScrollPane();
		
		JList lstDeliberationRules = new JList(deliberationRules);
		lstDeliberationRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneDeliberationRules.setViewportView(lstDeliberationRules);
		
		JLabel lblDeliberationRules = new JLabel("Deliberation rules");
		
		JScrollPane paneTerminationRules = new JScrollPane();
		
		JList lstTerminationRules = new JList(terminationRules);
		lstTerminationRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneTerminationRules.setViewportView(lstTerminationRules);
		
		JLabel lblTerminationRules = new JLabel("Termination rules");
		
		JLabel lblDialogueTopic = new JLabel("Dialogue topic");
		
		txtLoadedMasFile = new JTextField();
		txtLoadedMasFile.setText("No MAS loaded");
		txtLoadedMasFile.setEditable(false);
		txtLoadedMasFile.setColumns(10);
		
		txtDialogueTopic = new JTextField();
		txtDialogueTopic.setEditable(false);
		txtDialogueTopic.setColumns(10);

		JLabel lblDialogueGoal = new JLabel("Dialogue goal");
		
		txtDialogueGoal = new JTextField();
		txtDialogueGoal.setEditable(false);
		txtDialogueGoal.setColumns(10);

		JLabel lblOutcomeSelRule = new JLabel("Outcome selection rule");
		
		txtOutcomeSelRule = new JTextField();
		txtOutcomeSelRule.setEditable(false);
		txtOutcomeSelRule.setColumns(10);
		
		GroupLayout groupLayout_2 = new GroupLayout(paneTabMas);
		groupLayout_2.setHorizontalGroup(
			groupLayout_2.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout_2.createParallelGroup(Alignment.LEADING)
						.addComponent(lblLoadedMasFile)
						.addComponent(lblDialogueTopic)
						.addComponent(lblDialogueGoal)
						.addComponent(lblOutcomeSelRule)
						.addComponent(txtLoadedMasFile, GroupLayout.PREFERRED_SIZE, 392, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout_2.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(txtDialogueGoal, Alignment.LEADING)
							.addComponent(txtDialogueTopic, Alignment.LEADING)
							.addComponent(txtOutcomeSelRule, Alignment.LEADING)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout_2.createParallelGroup(Alignment.LEADING)
						.addComponent(paneDeliberationRules, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblDeliberationRules))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout_2.createParallelGroup(Alignment.LEADING)
						.addComponent(paneTerminationRules, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblTerminationRules))
					.addGap(24))
		);
		groupLayout_2.setVerticalGroup(
			groupLayout_2.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblLoadedMasFile)
						.addComponent(lblTerminationRules)
						.addComponent(lblDeliberationRules))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout_2.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout_2.createParallelGroup(Alignment.BASELINE)
							.addComponent(paneTerminationRules, 0, 0, Short.MAX_VALUE)
							.addComponent(paneDeliberationRules, 0, 0, Short.MAX_VALUE))
						.addGroup(groupLayout_2.createSequentialGroup()
							.addComponent(txtLoadedMasFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblDialogueTopic)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtDialogueTopic, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblDialogueGoal)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtDialogueGoal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblOutcomeSelRule)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtOutcomeSelRule, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(132, Short.MAX_VALUE))
		);
		paneTabMas.setLayout(groupLayout_2);
		
		JScrollPane paneTabMessages = new JScrollPane();
		paneTabs.addTab("Messages", null, paneTabMessages, null);
		
		JList lstMessages = new JList(messages);
		lstMessages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneTabMessages.setViewportView(lstMessages);
		
		JPanel panel = new JPanel();
		
		lblDialogueStatus = new JLabel("No ongoing dialogue");
		
		btnStartPause = new JButton("Start");
		btnStartPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!isPlatformRunning) {
					startPlatform();
				} else {
					pausePlatform();
				}
			}
		});
		btnStartPause.setEnabled(false);
		
		btnStep = new JButton("Step");
		btnStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stepPlatform();
			}
		});
		btnStep.setEnabled(false);
		
		lblMasStatus = new JLabel("No MAS is loaded");
		GroupLayout groupLayout_1 = new GroupLayout(panel);
		groupLayout_1.setHorizontalGroup(
			groupLayout_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblMasStatus)
					.addGap(12)
					.addComponent(lblDialogueStatus, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
					.addGap(67)
					.addComponent(btnStartPause)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnStep))
		);
		groupLayout_1.setVerticalGroup(
			groupLayout_1.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout_1.createSequentialGroup()
					.addGroup(groupLayout_1.createParallelGroup(Alignment.LEADING)
						.addComponent(btnStep, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnStartPause, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(groupLayout_1.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblDialogueStatus, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
							.addComponent(lblMasStatus, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
					.addGap(0))
		);
		panel.setLayout(groupLayout_1);
		GroupLayout groupLayout = new GroupLayout(paneContent);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(paneVerticalSplit, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(26)
					.addComponent(paneVerticalSplit, GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(440, Short.MAX_VALUE))
		);
		paneContent.setLayout(groupLayout);
		
		paneTabs.setSelectedIndex(1);
	}
	
	private JPanel buildAgentTab(AgentXmlData agentXml) {

		DefaultListModel options = new DefaultListModel();
		for (Rule option :agentXml.getOptions()) {
			options.addElement(option.toString());
		}
		DefaultListModel goalsHidden = new DefaultListModel();
		for (Goal goal :agentXml.getHiddenGoals()) {
			goalsHidden.addElement(goal.toString());
		}
		DefaultListModel goalsPublic = new DefaultListModel();
		for (Goal goal :agentXml.getPublicGoals()) {
			goalsPublic.addElement(goal.toString());
		}
		DefaultListModel beliefs = new DefaultListModel();
		for (Object rule : agentXml.getBeliefBase().getRules()) {
			beliefs.addElement(rule.toString());
		}
		
		JPanel paneTabAgent = new JPanel();
		paneTabAgent.setName(AGENT_TAB_NAME + agentXml.getName());
		
		JLabel lblName = new JLabel("Participant name");
		
		JScrollPane paneBeliefs = new JScrollPane();
		
		JList lstBeliefs = new JList(beliefs);
		//lstBeliefs.setSelectionBackground(lstBeliefs.getBackground()); // Hide the visible selection by setting the selection color to the list's background color
		paneBeliefs.setViewportView(lstBeliefs);

		JLabel lblOptions = new JLabel("Options");
		
		JScrollPane paneOptions = new JScrollPane();
		
		JList lstOptions = new JList(options);
		paneOptions.setViewportView(lstOptions);

		JLabel lblGoalsHidden = new JLabel("Hidden goals");
		
		JScrollPane paneGoalsHidden = new JScrollPane();
		
		JList lstGoalsHidden = new JList(goalsHidden);
		paneGoalsHidden.setViewportView(lstGoalsHidden);
		
		JLabel lblGoalsPublic = new JLabel("Public goals");
		
		JScrollPane paneGoalsPublic = new JScrollPane();
		
		JList lstGoalsPublic = new JList(goalsPublic);
		paneGoalsPublic.setViewportView(lstGoalsPublic);
		
		txtName = new JTextField(agentXml.getName());
		txtName.setColumns(10);
		
		txtID = new JTextField();
		txtID.setColumns(10);
		
		JLabel lblId = new JLabel("ID");
		
		JLabel lblBeliefs = new JLabel("Beliefs");
		GroupLayout groupLayout_3 = new GroupLayout(paneTabAgent);
		groupLayout_3.setHorizontalGroup(
			groupLayout_3.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout_3.createSequentialGroup()
					.addGroup(groupLayout_3.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout_3.createSequentialGroup()
							.addContainerGap()
							.addGroup(groupLayout_3.createParallelGroup(Alignment.LEADING)
								.addComponent(txtName, GroupLayout.PREFERRED_SIZE, 396, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblName))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout_3.createParallelGroup(Alignment.LEADING)
								.addComponent(lblId)
								.addComponent(txtID, 0, 0, Short.MAX_VALUE)))
						.addGroup(groupLayout_3.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblBeliefs))
						.addGroup(groupLayout_3.createSequentialGroup()
							.addGap(12)
							.addComponent(paneBeliefs, GroupLayout.PREFERRED_SIZE, 459, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout_3.createParallelGroup(Alignment.LEADING, false)
						.addComponent(lblGoalsHidden)
						.addComponent(lblGoalsPublic)
						.addComponent(lblOptions)
						.addComponent(paneGoalsPublic, 0, 0, Short.MAX_VALUE)
						.addComponent(paneGoalsHidden, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE)
						.addComponent(paneOptions, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		groupLayout_3.setVerticalGroup(
			groupLayout_3.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout_3.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout_3.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout_3.createSequentialGroup()
							.addGroup(groupLayout_3.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblOptions)
								.addComponent(lblId))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(paneOptions, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblGoalsHidden)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(paneGoalsHidden, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblGoalsPublic)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(paneGoalsPublic, GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
						.addGroup(groupLayout_3.createSequentialGroup()
							.addComponent(lblName)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout_3.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtID, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblBeliefs)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(paneBeliefs, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		paneTabAgent.setLayout(groupLayout_3);
		
		return paneTabAgent;
		
	}

	private void openMas(File selectedFile) {

		lastFileOpened = selectedFile;
		mntmReload.setEnabled(true);
		
		if (masXml != null) {
			
			// Destroy old mas and dialogue info
			masXml = null;
			dialogue = null;
			dialogueTree = new DelegateTree<Move<? extends Locution>, Long>();
			platformLauncher = null;
			isPlatformRunning = false;
			
			// Empty widgets
			paneDialogue.setViewportView(null);
			lblMasStatus.setText("No MAS is loaded");
			lblDialogueStatus.setText("No ongoing dialogue");
			moves.removeAllElements();
			messages.removeAllElements();
			deliberationRules.removeAllElements();
			terminationRules.removeAllElements();
			txtLoadedMasFile.setText("No MAS loaded");
	
	        // Destroy old agent tabs
			List<Component> toRemove = new ArrayList<Component>();
			if (paneTabs.getTabCount() > 2) {
		        for (int i = 0; i < paneTabs.getTabCount(); i++) {
		        	Component tab = paneTabs.getComponentAt(i);
		        	if (tab.getName() != null && tab.getName().startsWith(AGENT_TAB_NAME)) {
		        		toRemove.add(tab); // Do not yet remove them, because then the iteration over tab indexes get messed up      		
		        	}
				}
			}
			for (Component tab : toRemove) {
				paneTabs.remove(paneTabs.indexOfComponent(tab));
			}

			updateDialogueView(null);
			updatePlatformView();
			
		}
        
        // Read the MAS XML file (and agent XML files that are specified there)
        setTitle(selectedFile.getName() + " - " + Settings.APPLICATION_NAME);
		try {
			masXml = MasXmlData.loadAgentDataFromXml(selectedFile);
		} catch (Error e) {
			messages.addElement(e.getMessage());
			return;
		} catch (Exception e) {
			messages.addElement(e.getMessage());
			return;
		}

		// Copy the read settings to the platform settings object
        Settings settings = new Settings(masXml.getDeliberationRules(), masXml.getTerminationRules(), masXml.getOutcomeSelectionRule());
        
        // Update this JFrame fields to show the info of the newly loaded mas
        lblDialogueStatus.setText("Dialogue: " + DialogueState.Unopened.name());
        txtLoadedMasFile.setText(selectedFile.toString());
        txtDialogueTopic.setText(masXml.getTopic().inspect());
        txtDialogueGoal.setText(masXml.getTopicGoal().inspect());
        txtOutcomeSelRule.setText(masXml.getOutcomeSelectionRule().name());
        for (DeliberationRule rule : masXml.getDeliberationRules()) {
        	deliberationRules.addElement(rule.name());
        }
        for (TerminationRule rule : masXml.getTerminationRules()) {
        	terminationRules.addElement(rule.name());
        }
                
        // Create new tabs for the agents to show their info
        for (AgentXmlData agentXml : masXml.getAgentXmlDatas()) {
        	paneTabs.addTab(agentXml.getName(), null, buildAgentTab(agentXml), null);
        }
        
        // Start the agent platform
        List<PlatformListener> listeners = new ArrayList<PlatformListener>();
        listeners.add(this);
        platformLauncher = new Launcher(settings, masXml, listeners);
        platformLauncher.initPlatform();

	}

	@Override
	public void onExceptionThrown(final PlatformException e) {
		// We are still in the platform thread, so schedule this to execute in the gui's Swing thread instead
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				// Add the exception message to the messages list
				messages.addElement(e.getMessage() + (e.isCritical()? " (platform halted)": ""));
            }
		});
	}

	@Override
	public void onMessagesReceived(final List<DialogueMessage> rmessages) {
		// We are still in the platform thread, so schedule this to execute in the gui's Swing thread instead
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				// Add received messages to the messages list
				for (DialogueMessage message : rmessages) {
					messages.addElement(message.getMessage());
		
					// Update our platform state for platform state messages
					if (message instanceof PlatformStateMessage) {
						isPlatformRunning = ((PlatformStateMessage)message).isPlatformNowRunning();
						updatePlatformView();
					}
					
					// Update our dialogue state for state change messages
					if (dialogue != null) {
						if (message instanceof DialogueStateChangeMessage) {
							dialogue.setState(((DialogueStateChangeMessage)message).getNewState());
							updateDialogueView(null);
						}
					}
				}
            }
    	});
	}

	@Override
	public void onMoves(final List<Move<? extends Locution>> rmoves) {
		// We are still in the platform thread, so schedule this to execute in the gui's Swing thread instead
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
		
				for (Move<? extends Locution> move : rmoves) {
		
					// Show the move that was made
					moves.addElement(move.toString());
					
					// Dialogue opening?
					if (move.getLocution() instanceof OpenDialogueLocution) {
						OpenDialogueLocution open = (OpenDialogueLocution) move.getLocution();
						dialogue = new Dialogue(open.getTopic(), open.getTopicGoal());
					}
					
				}
				
				// Update our internal dialogue
				if (dialogue != null) {
					try {
						dialogue.update(rmoves);
						updateDialogueView(rmoves);
					} catch (DialogueException e) {
						messages.addElement(e.getMessage());
					}
				}
            }
		});
	}

	private void startPlatform() {
		platformLauncher.setPlatformSingleStepping(false);
		platformLauncher.startPlatform();
	}
	
	private void pausePlatform() {
		platformLauncher.pausePlatform();
	}

	private void stepPlatform() {
		platformLauncher.setPlatformSingleStepping(true);
		platformLauncher.startPlatform();
	}
	
	/**
	 * Update the widgets of this JFrame to show the platform state and (dis)allow starting/stopping/stepping
	 */
	private void updatePlatformView() {
		lblMasStatus.setText("Platform: " + (isPlatformRunning? "Running": "Paused"));
		btnStartPause.setText((isPlatformRunning? "Pause": "Start"));
		mntmStartPause.setText((isPlatformRunning? "Pause": "Start"));
		btnStartPause.setEnabled(dialogue == null || dialogue.getState() != DialogueState.Terminated);
		mntmStartPause.setEnabled(dialogue == null || dialogue.getState() != DialogueState.Terminated);
		btnStep.setEnabled(!isPlatformRunning);
		mntmStep.setEnabled(!isPlatformRunning);
	}
	
	/**
	 * Update the widgets of this JFrame to show the updated dialogue
	 * @param rmoves The new moves that were made in the dialogue
	 */
	private void updateDialogueView(List<Move<? extends Locution>> moves) {
		if (dialogue != null) {
			lblDialogueStatus.setText("Dialogue: " + dialogue.getState().name());
			btnStartPause.setEnabled(dialogue.getState() != DialogueState.Terminated);
			mntmStartPause.setEnabled(dialogue.getState() != DialogueState.Terminated);
			
			// Only add the new moves to the graphical tree
			if (moves != null) {
				for (Move<? extends Locution> move : moves) {
					if (move.getLocution() instanceof OpenDialogueLocution) {
						
						// Set the root of the graphical tree to the open-dialogue locution
						dialogueTree.setRoot(move);
						
						// Set up the new JUNG graph panel for this dialogue tree
						dialogueLayout = new DynamicTreeLayout<Move<? extends Locution>, Long>(dialogueTree, 200, 80);
						dialogueVisViewer = new VisualizationViewer<Move<? extends Locution>, Long>(dialogueLayout);
						VisualizationViewer<Move<? extends Locution>, Long> vv = dialogueVisViewer;
						
						// Graphical properties
						vv.setBackground(Color.white);
						DefaultModalGraphMouse<Number, Number> gm = new DefaultModalGraphMouse<Number,Number>();
						gm.setMode(DefaultModalGraphMouse.Mode.TRANSFORMING);
				        vv.setGraphMouse(gm);
				        VertexLabelAsShapeRenderer<Move<? extends Locution>, Long> vlasr = new VertexLabelAsShapeRenderer<Move<? extends Locution>, Long>(vv.getRenderContext());
				        vv.getRenderContext().setVertexLabelTransformer(new DialogueDecorator.VertexLabeller());
				        vv.getRenderContext().setVertexShapeTransformer(vlasr);
				        vv.getRenderer().setVertexLabelRenderer(vlasr);
						vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Move<? extends Locution>, Long>());
						//vv.getRenderContext().setEdgeArrowTransformer(new EdgeShape.());
				        vv.getRenderContext().setVertexStrokeTransformer(new DialogueDecorator.VertexStroker(this));
				        vv.getRenderContext().setVertexDrawPaintTransformer(new DialogueDecorator.VertexPainter(this));
				        vv.getRenderContext().setVertexFillPaintTransformer(new DialogueDecorator.VertexFillPainter());
				        vv.setVertexToolTipTransformer(new DialogueDecorator.ToolTipLabeller());
				        paneDialogue.setViewportView(vv);
						
					} else if (move.getLocution() instanceof DeliberationLocution) {
						if (move.getLocution() instanceof ProposeLocution) {
							
							// Add the new proposal to the graphical tree
							dialogueTree.addChild(move.getIndex(), dialogueTree.getRoot(), move);
							dialogueLayout.reset();
							
						} else {
							
							// Attach the new move to its appropriate target move
							dialogueTree.addChild(move.getIndex(), move.getTarget(), move);
							dialogueLayout.reset();
							
						}
						dialogueVisViewer.repaint();
					}
				}
			}
			
		}
	}

	@Override
	public boolean evaluateMove(Move<? extends Locution> move) {
		if (dialogue != null) {
			for (Proposal proposal : dialogue.getProposals()) {
				try {
					return proposal.isIn(move);
				} catch (DialogueException e) {
					// Just continue into the next proposal
				}
			}
		}
		return false;
	}
	
}
