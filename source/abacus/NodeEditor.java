package abacus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import TuringMachine.OTMSExporter;

public class NodeEditor extends JFrame implements ActionListener
{
	JMenuItem newMachine = new JMenuItem("New Machine");
	JMenuItem saveMachine = new JMenuItem("Save Machine");
	JMenuItem loadMachine = new JMenuItem("Load Machine");
	JMenuItem export_xml = new JMenuItem("Export OwenTMS XML");
	JMenuItem export_tm = new JMenuItem("Export OwenTMS TM");
	JMenuItem close = new JMenuItem("Close All");
	
	JMenuItem help = new JMenuItem("Help");
	
	
	JMenuItem[] items = { newMachine, saveMachine, loadMachine, export_xml, export_tm, close, help}; 
	
	// buttons
	static Image imageAdd = makeRedTransparent(new ImageIcon("images/imageAdd.GIF").getImage());
	static Image imageSub = makeRedTransparent(new ImageIcon("images/imageSub.GIF").getImage());
	static Image imageMod = makeRedTransparent(new ImageIcon("images/imageMod.GIF").getImage());
	static Image imageDel = makeRedTransparent(new ImageIcon("images/imageDel.GIF").getImage());
	
	// other images
	static Image pause = makeRedTransparent(new ImageIcon("images/Pause.PNG").getImage());
	static Image resetAll = makeRedTransparent(new ImageIcon("images/ResetAll.PNG").getImage());
	static Image resetIm = makeRedTransparent(new ImageIcon("images/Reset.PNG").getImage());
	static Image play = makeRedTransparent(new ImageIcon("images/Play.PNG").getImage());
	static Image fastForward = makeRedTransparent(new ImageIcon("images/FastForward.PNG").getImage());
	static Image step = makeRedTransparent(new ImageIcon("images/Step.PNG").getImage());
	
	static Image transAdd = makeHazy(imageAdd);
	static Image transSub = makeHazy(imageSub);
	
	public final static Color babyBlue = new Color(67,203,255);
	
	JToggleButton addState = new JToggleButton(new ImageIcon(imageAdd));
	JToggleButton subState = new JToggleButton(new ImageIcon(imageSub));
	JToggleButton modState = new JToggleButton(new ImageIcon(imageMod));
	JToggleButton delState = new JToggleButton(new ImageIcon(imageDel));
	
	boolean locked = false;
	JPanel north, west, south;
	private final static String TITLE = "Abacus Machine Simulator";
	
	// machine panel
	MachinePanel macPanel = new MachinePanel(this);
	
	// Register editor
	RegisterEditor re = new RegisterEditor(this);
	
	// Simluator
	Simulator simulator = new Simulator(this, re);
	

	
	public NodeEditor()
	{
		setTitle(TITLE);
		
		// menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.add(newMachine);
		file.add(saveMachine);
		file.add(loadMachine);
		file.addSeparator();
		file.add(export_xml);
		file.add(export_tm);
		file.addSeparator();
		file.add(close);
		menuBar.add(file);
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(help);
		menuBar.add(helpMenu);
		
		this.setJMenuBar(menuBar);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		for (int x = 0; x < items.length; ++x)		
			items[x].addActionListener(this);
		
		// layout
		setLayout(new BorderLayout());
		
		// buttons
		ButtonGroup bg = new ButtonGroup(); 
		bg.add(modState);
		bg.add(addState);
		bg.add(subState);
		bg.add(delState);
		
		north = new JPanel();
		BorderLayout nbl = new BorderLayout();
		north.setLayout(nbl);
		north.add(simulator,BorderLayout.CENTER);
		
		west = new JPanel();
		BoxLayout bw = new BoxLayout(west, BoxLayout.PAGE_AXIS);
		west.setLayout(bw);
		
		south = new JPanel();
		BorderLayout sbl = new BorderLayout();
		south.setLayout(sbl);
		south.add(re,BorderLayout.CENTER);
		
		modState.setSelected(true);
		modState.setToolTipText("Modify States");
		west.add(modState);
		addState.setToolTipText("Insert Add State");
		west.add(addState);
		subState.setToolTipText("Insert Subtract State");
		west.add(subState);
		delState.setToolTipText("Delete States");
		west.add(delState);
		
		west.setBackground(babyBlue);
		add(north,BorderLayout.NORTH);
		add(west,BorderLayout.WEST);
		add(macPanel,BorderLayout.CENTER);
		add(south,BorderLayout.SOUTH);
		
		pack();
		
		addWindowListener(new WindowAdapter() 
				{
			    	public void windowClosing(WindowEvent we) 
			    	{
			    		if (confirmClose())
			    		{
			    			boolean allHidden=true;
				    		re.setVisible(false);
				    		simulator.setVisible(false);
				    		setVisible(false);
				    		Frame[] frames = NodeEditor.this.getFrames();
			    			for(int i=0;i<frames.length;i++){
			    				if(frames[i].isVisible()) allHidden=false;
			    			}
			    			if(allHidden) System.exit(0);
			    		}
						else {
							return;
						}
			    	}
				});
	}
	
	public boolean confirmClose()
	{
		boolean rv = false;
// FAULTY CODE FOR "CANCEL" OPTION IS REMOVED VIA COMMENT. SEE README.TXT FOR EXPLANATION		
//		Object[] options = {"Save Abacus Machine",
//        "Close Window", "Cancel"};
		Object[] options = {"Save Abacus Machine",
        "Close Window"};
		int n = JOptionPane.showOptionDialog(this,
			"Would you like to save your Abacus Machine?",
			"Save Query",
//			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,     //don't use a custom Icon
			options,  //the titles of buttons
			options[0]); //default button title
		
		if (n == JOptionPane.YES_OPTION)
		{
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			if (locked)
			{
				// this should unlock it
				saveMachine.setVisible(false);
			}
			
			FileData fd = new FileData();
			fd.setNodes(macPanel.nodes);
			fd.setRegs(re.regs);
			fd.setComments(macPanel.comments);
			
			fd.save();
			rv = true;
		}
		else if (n == JOptionPane.NO_OPTION)
		{
			rv = true;
		}
/*		else if (n == JOptionPane.CANCEL_OPTION)
		{
			rv=false;
		}*/
		return rv;
	}
	
	public static final int STATE_ADD = 0;
	public static final int STATE_SUB = 1;
	public static final int STATE_MOD = 2;
	public static final int STATE_DEL = 3;
	
	// get the state of the togglebuttons
	public int getState()
	{
		int rv = STATE_MOD;
		
		if (addState.isSelected())
			rv = STATE_ADD;
		else if (subState.isSelected())
			rv = STATE_SUB;
		else if (delState.isSelected())
			rv = STATE_DEL;
		
		return rv;
	}
	
	public void nextButton()
	{
		if (addState.isSelected())
			subState.doClick();
		else if (subState.isSelected())
			modState.doClick();
		else if (modState.isSelected())
			delState.doClick();
		else
			addState.doClick();
	}
	
	public static Image makeRedTransparent(Image source)
	{
		return makeTransparent(source,Color.red);
	}
	
	public void clearSelection()
	{
		macPanel.clearSelection();
	}
	
	public static Image makeTransparent(Image source, Color c)
	{
		int w = source.getWidth(null);
		int h = source.getHeight(null);

		int[] cols = getAllColors(source);

		for (int x = 0; x < w; ++x)
			for (int y = 0; y < h; ++y)
			{
				int color = cols[x + y * w];

				int[] rgb = getRGB(color);
				
				if (Math.abs(rgb[0] - c.getRed()) < 5 && 
					Math.abs(rgb[1] - c.getGreen()) < 5 && 
					Math.abs(rgb[2] - c.getBlue()) < 5)
				{ // red					
						
					cols[x + y * w] = 0;
				}
				else
					cols[x + y * w] = (255 << 24) | (rgb[0] << 16)
							| (rgb[1] << 8) | rgb[2];
			}

		return (new JPanel()).createImage(new MemoryImageSource(w, h, cols, 0,
				w));

	}
	
	public static Image makeHazy(Image source)
	{
		int w = source.getWidth(null);
		int h = source.getHeight(null);

		int[] cols = getAllColors(source);

		for (int x = 0; x < w; ++x)
			for (int y = 0; y < h; ++y)
			{
				int color = cols[x + y * w];

				int[] rgb = getRGB(color);
				if ((color & 0xFF000000) != 0) // make this guy hazy
				{
					cols[x + y * w] = (96 << 24) | (rgb[0] << 16)
					| (rgb[1] << 8) | rgb[2];
				}
					
			}

		return (new JPanel()).createImage(new MemoryImageSource(w, h, cols, 0,
				w));

	}
	
	public static int[] getAllColors(Image theImage)
	{
		int w = theImage.getWidth(null);
		int h = theImage.getHeight(null);

		int[] storeHere = new int[w * h];
		PixelGrabber pg = new PixelGrabber(theImage, 0, 0, w, h, storeHere, 0,
				w);
		try
		{
			pg.grabPixels();
		}
		catch (InterruptedException e)
		{
			JOptionPane.showMessageDialog(null,
					"interrupted waiting for pixels!");
			storeHere = null;
			return null;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0)
		{
			JOptionPane.showMessageDialog(null,
					"image fetch aborted or errored");
			storeHere = null;
			return null;
		}

		return storeHere;
	}

	public static int[] getRGB(int pixel)
	{
		int[] rgb = new int[3];

		rgb[0] = (pixel >> 16) & 0xff;
		rgb[1] = (pixel >> 8) & 0xff;
		rgb[2] = (pixel) & 0xff;

		return rgb;
	}
	
	public void lock()
	{
		locked = true;
		macPanel.lock();
		setTitle(TITLE + " (locked)");
	}
	
	public void unlock()
	{
		locked = false;
		macPanel.unlock();
		north.setVisible(true);
		setTitle(TITLE);
	}
		
	public NodeEditor newWindow()
	{
		NodeEditor ne = new NodeEditor();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		ne.setLocation(screenSize.width / 2 - 300,50);
		
		ne.setVisible(true);
		return ne;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == newMachine) 
		{
			this.setVisible(true);
			newWindow();
			// i was going to delete the current window but I guess Java does that automagically
		}
		else if (e.getSource() == close)
		{
			System.out.print("getsource is close\n");
			if (confirmClose())
    		{
				System.out.print("confirmClose is TRUE\n");
				try {
					Thread.sleep(1000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
	    		System.exit(0);
				this.setVisible(false);
    		}
		}
		else if (e.getSource() == loadMachine)
		{
			FileData fd = new FileData();
			
			if (fd.load())
			{
				NodeEditor ne = newWindow();
				
				ne.macPanel.nodes = fd.getNodes();
				ne.re.regs = fd.getRegs();
				ne.macPanel.comments = fd.getComments();
				ne.repaint();
				ne.re.repaint();
			}
		}
		else if (e.getSource() == help)
		{
			JOptionPane.showMessageDialog(null,
				"To set an initial state: Double click a node when in modification mode.\n" +
				"To change a state's register: Double click a node when in modification mode.\n" +
				"To clear an empty transition's destination: Make the empty transition go to the " +
					"originating node.\n" +
				"To scroll: Hold the middle mouse button and drag.\n" +
				"To zoom: Scroll with the mouse wheel.\n" +
				"To reset zoom and scroll location: Double middle click\n" +
				"To switch between tools quickly: Press the right mouse button.\n\n" +
				"To create comments: Double click on an empty space when in modification mode.\n\n" +
				"To modify/delete comments: Double click on a comment when in modification mode.\n\n" +
				"Program by Stanley Bak, April 2006 \n" + 
				"Modified by Martin Papesh, May 2012",
				"Help", 
				JOptionPane.INFORMATION_MESSAGE);		
		}
		else if (e.getSource() == saveMachine)
		{
			if (locked)
			{
				JOptionPane.showMessageDialog(null,"There is currently an active simulation running. "
						+ "First close the simulation window and then attempt saving again.");
			}
			else
			{
				FileData fd = new FileData();
				//fd.nodes = macPanel.nodes;
				//fd.regs = re.regs;
				//fd.comments = macPanel.comments;
        fd.setNodes(macPanel.nodes);
        fd.setRegs(re.regs);
        fd.setComments(macPanel.comments);
				
				fd.save();
			}
		}
		else if (e.getSource() == export_xml)
		{
			if (locked)
			{
				JOptionPane.showMessageDialog(null,"There is currently an active simulation running. "
						+ "First close the simulation window and then attempt exporting again.");
			}
			else
			{
				OTMSExporter otms = new OTMSExporter((Node)macPanel.nodes.get(0));
				otms.exportXML();
			}
		}
		else if (e.getSource() == export_tm)
		{
			if (locked)
			{
				JOptionPane.showMessageDialog(null,"There is currently an active simulation running. "
						+ "First close the simulation window and then attempt exporting again.");
			}
			else
			{
				OTMSExporter otms = new OTMSExporter((Node)macPanel.nodes.get(0));
				otms.exportTM();
			}
		}
	}
}
