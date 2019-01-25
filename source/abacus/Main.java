package abacus;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.UIManager;

public class Main
{
	
	public static void main(String args[])
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
		
		NodeEditor ne = new NodeEditor();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		ne.setLocation(screenSize.width / 2 - 300,50);
		
		ne.setVisible(true);
	}
}
