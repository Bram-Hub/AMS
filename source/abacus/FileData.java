package abacus;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JOptionPane;

// a bean used for saving and loading the data
public class FileData
{
	public ArrayList comments = new ArrayList();
	public ArrayList nodes = new ArrayList();
	public TreeMap regs = new TreeMap();
	final static FileDialog fileChooser = new FileDialog(new Frame());
	
	public FileData() {}

	public ArrayList getNodes()
	{
		return nodes;
	}

	public void setNodes(ArrayList nodes)
	{
		this.nodes = nodes;
	}
	
	public ArrayList getComments()
	{
		return comments;
	}

	public void setComments(ArrayList comments)
	{
		this.comments = comments;
	}

	public TreeMap getRegs()
	{
		return regs;
	}

	public void setRegs(TreeMap regs)
	{
		this.regs = regs;
	}
	
	public void save()
	{
		fileChooser.setMode(FileDialog.SAVE);
		fileChooser.setTitle("Save As...");
		fileChooser.setVisible(true);
		String filename = fileChooser.getFile();
		
		if (filename != null) // user didn't pressed cancel
		{
			filename = fileChooser.getDirectory() + filename;
			
	    	if (!filename.toLowerCase().endsWith(".yam"))
	    		filename = filename + ".yam";
	    	
	    	try
	    	{
		    	 XMLEncoder e = new XMLEncoder(
		                 new BufferedOutputStream(
		                     new FileOutputStream(filename)));
				e.writeObject(this);
				e.close();
	    	}
	    	catch (Exception e)
	    	{
	    		JOptionPane.showMessageDialog(null,"Error saving: " + e);
	    	}
		}
	}
	
	public boolean load()
	{
		boolean rv = false;
		fileChooser.setMode(FileDialog.LOAD);
		fileChooser.setTitle("Select Puzzle");
		fileChooser.setVisible(true);
		String filename = fileChooser.getFile();
		//String dir = fileChooser.getDirectory();

		if (filename != null) // user didn't pressed cancel
		{
			filename = fileChooser.getDirectory() + filename;
			
			try
	    	{
		    	XMLDecoder d = new XMLDecoder(
		                new BufferedInputStream(
		                    new FileInputStream(filename)));
		    	FileData result = (FileData)d.readObject();
				d.close();
				
				this.nodes = result.nodes;
				this.regs = result.regs;
				this.comments = result.comments;
			
				rv = true;
	    	}
	    	catch (Exception e)
	    	{
	    		JOptionPane.showMessageDialog(null,"Error loading: " + e);
	    		rv = false;
	    	}
		}
		
		return rv;
	}
}
