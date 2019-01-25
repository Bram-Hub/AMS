package abacus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class NodeEditor extends JFrame implements ActionListener
{
	JMenuItem save = new JMenuItem("Save");
	JMenuItem close = new JMenuItem("Close");
	
	JMenuItem modify = new JMenuItem("Modify Initial Register Contents");
	JMenuItem simulate = new JMenuItem("Simulate");
	
	JMenuItem help = new JMenuItem("Help");
	
	JMenuItem[] items = { save, close, modify, simulate , help}; 
	
	// buttons
	static Image imageAdd = makeRedTransparent(new ImageIcon("images/imageAdd.GIF").getImage());
	static Image imageSub = makeRedTransparent(new ImageIcon("images/imageSub.GIF").getImage());
	static Image imageMod = makeRedTransparent(new ImageIcon("images/imageMod.GIF").getImage());
	
	// other images
	static Image pause = makeRedTransparent(new ImageIcon("images/Pause.PNG").getImage());
	static Image resetIm = makeRedTransparent(new ImageIcon("images/Reset.PNG").getImage());
	static Image play = makeRedTransparent(new ImageIcon("images/Play.PNG").getImage());
	static Image fastForward = makeRedTransparent(new ImageIcon("images/FastForward.PNG").getImage());
	
	static Image transAdd = makeHazy(imageAdd);
	static Image transSub = makeHazy(imageSub);
	
	public final static Color babyBlue = new Color(67,203,255);
	
	JToggleButton addState = new JToggleButton(new ImageIcon(imageAdd));
	JToggleButton subState = new JToggleButton(new ImageIcon(imageSub));
	JToggleButton modState = new JToggleButton(new ImageIcon(imageMod));
	
	boolean locked = false;
	JPanel north;
	private final static String TITLE = "PEAS - Primary Editor of Abacus States";
	
	// machine panel
	MachinePanel macPanel = new MachinePanel(this);
	
	// Simluator
	Simulator simulator = new Simulator();
	
	// Register editor
	RegisterEditor re = new RegisterEditor();
	
	public NodeEditor()
	{
		setTitle(TITLE);
		
		// menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.add(save);
		file.addSeparator();
		file.add(close);
		menuBar.add(file);
		JMenu mac = new JMenu("Machine");
		mac.add(modify);
		mac.addSeparator();
		mac.add(simulate);
		menuBar.add(mac);
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(help);
		menuBar.add(helpMenu);
		
		this.setJMenuBar(menuBar);
		
		for (int x = 0; x < items.length; ++x)		
			items[x].addActionListener(this);
		
		// layout
		setLayout(new BorderLayout());
		
		// buttons
		ButtonGroup bg = new ButtonGroup(); 
		bg.add(addState);
		bg.add(subState);
		bg.add(modState);
		
		north = new JPanel();
		FlowLayout f = new FlowLayout();
		f.setAlignment(FlowLayout.CENTER);
		north.setLayout(f);
		
		addState.setSelected(true);
		addState.setToolTipText("Insert Add State");
		north.add(addState);
		subState.setToolTipText("Insert Subtract State");
		north.add(subState);
		modState.setToolTipText("Modify States");
		north.add(modState);
		
		north.setBackground(babyBlue);
		add(north,BorderLayout.NORTH);
		add(macPanel,BorderLayout.CENTER);
		
		pack();
		
		setDefaultCloseOperation(
			    JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() 
				{
			    	public void windowClosing(WindowEvent we) 
			    	{
			    		if (confirmClose())
			    		{			    		
				    		re.setVisible(false);
				    		simulator.setVisible(false);
				    		setVisible(false);
			    		}
			    	}
				});
	}
	
	public boolean confirmClose()
	{
		boolean rv = true;
		
		Object[] options = {"Save Abacus Machine",
        "Close Window", "Cancel"};
		int n = JOptionPane.showOptionDialog(this,
			"Would you like to save your Abacus Machine?",
			"Save Query",
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,     //don't use a custom Icon
			options,  //the titles of buttons
			options[2]); //default button title
		
		if (n == JOptionPane.YES_OPTION)
		{
			if (locked)
			{
				// this should unlock it
				simulate.setVisible(false);
			}
			
			FileData fd = new FileData();
			fd.nodes = macPanel.nodes;
			fd.regs = re.regs;
			fd.comments = macPanel.comments;
			
			fd.save();
		}
		else if (n == JOptionPane.CANCEL_OPTION)
		{
			rv = false;
		}
		
		return rv;
	}
	
	public static final int STATE_ADD = 0;
	public static final int STATE_SUB = 1;
	public static final int STATE_MOD = 2;
	
	// get the state of the togglebuttons
	public int getState()
	{
		int rv = STATE_MOD;
		
		if (addState.isSelected())
			rv = STATE_ADD;
		else if (subState.isSelected())
			rv = STATE_SUB;
		
		return rv;
	}
	
	public void nextButton()
	{
		if (addState.isSelected())
			subState.doClick();
		else if (subState.isSelected())
			modState.doClick();
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

		// System.out.println("width = " + w);

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
		
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == close)
		{
			if (confirmClose())
    		{			    		
	    		re.setVisible(false);
	    		simulator.setVisible(false);
	    		setVisible(false);
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
				"Program by Stanley Bak, April 2006", 
				"RADISH - Radically Advanced, Devious, Ingenious, and Simple Help",
				JOptionPane.INFORMATION_MESSAGE);		
		}
		else if (e.getSource() == modify)
		{
			re.setVisible(true);
		}
		else if (e.getSource() == simulate)
		{
			if (macPanel.nodes.size() > 0)
				simulator.begin(this,re);
			else
				JOptionPane.showMessageDialog(null,
						"You must add at least a single node to simulate a computation.");
		}
		else if (e.getSource() == save)
		{
			if (locked)
			{
				JOptionPane.showMessageDialog(null,"There is currently an active simulation running. "
						+ "First close the simulation window and then attempt saving again.");
			}
			else
			{
				FileData fd = new FileData();
				fd.nodes = macPanel.nodes;
				fd.regs = re.regs;
				fd.comments = macPanel.comments;
				
				fd.save();
			}
		}
	}
}
