package abacus;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class Main extends JFrame implements ActionListener
{
	JMenuItem loadMachine = new JMenuItem("Load Machine");
	JMenuItem newWindow = new JMenuItem("New Window");
	JMenuItem closeAll = new JMenuItem("Close All");
	
	public Main()
	{
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		menu.add(file);
		
		setTitle("YAMS - Your Abacus Machine Simluator");
		
		file.add(newWindow);
		file.add(loadMachine);
		file.addSeparator();
		file.add(closeAll);
		
		newWindow.addActionListener(this);
		closeAll.addActionListener(this);
		loadMachine.addActionListener(this);
		
		this.setJMenuBar(menu);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		setLocation(screenSize.width / 2 - 300,50);
		
		JPanel p = new JPanel();
		
		p.setPreferredSize(new Dimension(350,5));
		
		add(p);
		
		pack();
	}
	
	public static void main(String args[])
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
		
		Main m = new Main();
		m.setVisible(true);
	}
	
	public NodeEditor newWindow()
	{
		NodeEditor ne = new NodeEditor();
		
		int randX = (int)(Math.random() * 20) - 10;
		int randY = (int)(Math.random() * 20);
		
		ne.setLocation(randX + getX(),randY + getY()+getHeight());
		
		ne.setVisible(true);
		
		return ne;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == newWindow)
			newWindow();
		else if (e.getSource() == closeAll)
			System.exit(0);
		else if (e.getSource() == loadMachine)
		{
			FileData fd = new FileData();
			
			if (fd.load())
			{
				NodeEditor ne = newWindow();
				
				ne.macPanel.nodes = fd.nodes;
				ne.re.regs = fd.regs;
				ne.macPanel.comments = fd.comments;
				ne.repaint();
				ne.re.repaint();
			}
		}
	}
}
