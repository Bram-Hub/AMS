package abacus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

public class Simulator extends JFrame implements ActionListener
{	
	private static ImageIcon resetIcon = new ImageIcon(NodeEditor.resetIm);// NodeEditor.reset;
	private static Image pause = NodeEditor.pause;
	private static Image play = NodeEditor.play;
	private static Image fastForward = NodeEditor.fastForward;
	
	private JSlider slider = new JSlider(JSlider.HORIZONTAL,0,1000,0);
	private int realNumSteps = 0;
	private JLabel haltLabel = new JLabel("");
	private JLabel numSteps = new JLabel("Number of Steps: 0");
	private JLabel numRegisters = new JLabel("Number of Registers: 0");
	private JButton done = new JButton("Done");
	private JButton resetButton = new JButton("",resetIcon);
	
	private Node curNode = null;
	
	NodeEditor ne = null;
	RegisterEditor re = null;
	boolean resetMachine = false;
	
	public Simulator()
	{
		setTitle("PLUMS - Pleasant Looking and Useful Machine Simulator");
		
		JLabel pauseLabel = new JLabel(new ImageIcon(pause));
		JLabel playLabel = new JLabel(new ImageIcon(play));
		JLabel fastForwardLabel = new JLabel(new ImageIcon(fastForward));
		
		done.addActionListener(this);
		resetButton.addActionListener(this);
		
		setDefaultCloseOperation(
			    JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() 
				{
			    	public void windowClosing(WindowEvent we) 
			    	{
			    		ne.unlock();
						re.unlock();
						setVisible(false);
			    	}
				});
		
		setSize(480,160);
		this.setResizable(false);
		setLayout(null);
		
		add(slider);
		slider.setBounds(15,5,getWidth() - 30,20);
		int y = slider.getY() + slider.getHeight() - 3;
		
		add(resetButton);
		resetButton.setBounds(15,y + 48,48,48);
		
		add(pauseLabel);
		pauseLabel.setBounds(slider.getX(),y,40,40);
		
		add(playLabel);
		playLabel.setBounds(slider.getX() + slider.getWidth() / 2 - 20,y,40,40);
		
		add(fastForwardLabel);
		fastForwardLabel.setBounds(slider.getX() + slider.getWidth() - 40 ,y,40,40);
		
		add(haltLabel);
		haltLabel.setForeground(Color.red);
		haltLabel.setBounds(resetButton.getX() + resetButton.getWidth() + 30,y + 30,150,20);
		
		add(numRegisters);
		numRegisters.setBounds(resetButton.getX() + resetButton.getWidth() + 30,y + 50,150,20);
		
		add(numSteps);
		numSteps.setBounds(resetButton.getX() + resetButton.getWidth() + 30,y + 70,150,20);
		
		add(done);
		done.setBounds(getWidth() - 125, y + 70,100,25);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
		
		Player p = new Player();
		p.start();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == resetButton)
		{
			resetMachine = true;
		}
		else if (e.getSource() == done)
		{			
			ne.unlock();
			re.unlock();
			
			setVisible(false);
		}
	}
	
	// begin a simulation
	public void begin(NodeEditor ne, RegisterEditor re)
	{		
		this.ne = ne;
		this.re = re;
		
		slider.setValue(0);
		ne.lock();
		re.lock();
		ne.clearSelection();
		re.clearSelection();
		haltLabel.setText("");
		numRegisters.setText("Number of Registers: " + ne.macPanel.getRegCount());
		realNumSteps = 0;
		
		curNode = (Node)ne.macPanel.nodes.get(0);
		
		re.setVisible(true);
		setVisible(true);
	}
	
	class Player extends Thread
	{
		private void doStep(int halfSleep)
		{
			numSteps.setText("Number of Steps: " + ++realNumSteps);
			
			curNode.simSelect(Node.SELECTED_NODE);
			ne.repaint();
			
			if (halfSleep > 0)
			{
				try
				{
					Thread.sleep(halfSleep);
				}
				catch (Exception e) { }
			}
			
			curNode.simSelect(Node.SELECTED_NONE);
			// do the transition	
			Node nextNode = null;
			
			int reg = curNode.getRegister();
			
			if (curNode.isPlus())
			{
				re.addOne(reg);
				curNode.simSelect(Node.SELECTED_OUT);
				nextNode = curNode.getOut();
			}
			else
			{
				if (!re.subOne(reg))
				{ // empty
					curNode.simSelect(Node.SELECTED_OUTEMPTY);
					nextNode = curNode.getOutEmpty();
				}
				else
				{
					curNode.simSelect(Node.SELECTED_OUT);
					nextNode = curNode.getOut();
				}
			}
			
			ne.repaint();
			if (halfSleep > 0)
			{
				try
				{
					Thread.sleep(halfSleep);
				}
				catch (Exception e) { }
			}			
			
			if (nextNode == null)
			{
				haltLabel.setText("Machine Halted");
			}
			else
			{
				curNode.simSelect(Node.SELECTED_NONE);
			}
			
			curNode = nextNode;
			
			if (curNode != null && curNode.isPauseState())
			{ // pause!
				curNode.simSelect(Node.SELECTED_NODE);
				slider.setValue(0);
			}
		}
		
		public void run()
		{
			while (true)
			{
				if (resetMachine)
				{
					numSteps.setText("Number of Steps: 0");
					haltLabel.setText("");
					realNumSteps = 0;
					curNode = (Node)ne.macPanel.nodes.get(0);
					re.unlock();
					re.lock();
					ne.clearSelection();
					re.clearSelection();					
					
					resetMachine = false;
				}
				else
				{				
					int val = slider.getValue();
					if (isVisible() && val > 0 && curNode != null)
					{ // slider is between 1 and 1000
						
						// scale it a little bit
						double squareMe = 1.0 - (val / 1000.0);
						int halfSleep = (int)(1000 * (squareMe * squareMe));
					
						doStep(halfSleep); // will sleep if necessary
						
					}
					else
					{
						try
						{
							Thread.sleep(10);
						}
						catch (Exception e) { }
					}
				}
			}
		}
	}
}
