package abacus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Simulator extends JPanel implements ActionListener
{	
	private static ImageIcon resetIcon = new ImageIcon(NodeEditor.resetIm);// NodeEditor.reset;
	private static ImageIcon pauseIcon = new ImageIcon(NodeEditor.pause);
	private static ImageIcon playIcon = new ImageIcon(NodeEditor.play);
	private static ImageIcon fastForwardIcon = new ImageIcon(NodeEditor.fastForward);
	
	private int realNumSteps = 0;
	private JLabel haltLabel = new JLabel("              ");
	private JLabel numSteps = new JLabel("Number of Steps: 0");
	private JLabel numRegisters = new JLabel("Number of Registers: 0");
	private JButton resetButton = new JButton("",resetIcon);
	private JButton playButton  = new JButton("",playIcon);
	private JButton pauseButton = new JButton("",pauseIcon);
	private JButton fastForwardButton = new JButton("",fastForwardIcon);
	
	private Node curNode = null;
	private int speed = 0;
	private boolean firstClick = true;
	
	NodeEditor ne = null;
	RegisterEditor re = null;
	boolean resetMachine = false;
	
	public Simulator(NodeEditor ne, RegisterEditor re)
	{	
		this.ne = ne;
		this.re = re;
		
		resetButton.addActionListener(this);
		playButton.addActionListener(this);
		pauseButton.addActionListener(this);
		fastForwardButton.addActionListener(this);
			
		setSize(480,120);
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		JPanel west = new JPanel();
		west.add(resetButton);
		west.add(playButton);
		west.add(pauseButton);
		west.add(fastForwardButton);
		
		JPanel east = new JPanel();
		east.add(numRegisters);
		east.add(numSteps);
		
		add(west, BorderLayout.WEST);
		add(haltLabel, BorderLayout.CENTER);
		haltLabel.setForeground(Color.red);
		add(east, BorderLayout.EAST);
		
		Player p = new Player();
		p.start();
	}
	
	// begin a simulation
	public void begin()
	{		
		if (firstClick) {
			ne.clearSelection();
			re.clearSelection();
			re.initial();
			haltLabel.setText("");
			curNode = (Node)ne.macPanel.nodes.get(0);
			firstClick = false;
			numRegisters.setText("Number of Registers: " + ne.macPanel.getRegCount());
		}
		ne.lock();
		re.lock();
		speed = 100;
	}
	
	public void pause()
	{
		speed = 0;
		ne.unlock();
		re.unlock();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == resetButton)
		{
			resetMachine = true;
		}
		else if (e.getSource() == playButton)
		{
			if (ne.macPanel.nodes.size() > 0)
				begin();
			else
				JOptionPane.showMessageDialog(null,
						"You must add at least a single node to simulate a computation.");
		}
		else if (e.getSource() == pauseButton)
		{
			pause();
		}
		
		else if (e.getSource() == fastForwardButton)
		{
			if (ne.macPanel.nodes.size() > 0) {
				begin();
				speed = 750;
			}
			else
				JOptionPane.showMessageDialog(null,
						"You must add at least a single node to simulate a computation.");
		}
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
				speed = 0;
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
					re.restore();
					ne.clearSelection();
					re.clearSelection();					
					
					resetMachine = false;
					firstClick = true;
				}
				else
				{				
					int val = speed;
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
