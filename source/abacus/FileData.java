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
	public int regInputNum = 0;
	public ArrayList<TreeMap> otherRegs = new ArrayList<TreeMap>(); 
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

	public ArrayList<TreeMap> getOtherRegs()
	{
		return otherRegs;
	}

	public void setOtherRegs(ArrayList<TreeMap> otherRegs)
	{
		this.otherRegs = otherRegs;
	}

	public int getRegInput()
	{
		return this.regInputNum;
	}

	public void setRegInput(int n)
	{
		this.regInputNum = n;
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
        //e.writeObject(nodes);
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
				this.otherRegs = result.otherRegs;
				this.regInputNum = result.regInputNum;
				this.comments = result.comments;
				
				//check if file references register 0 (NODES USING REGISTER 0 ARE CHANGED TO 1, OTHERS ARE UNCHANGED (if so, machine will not work anymore))
				if (this.regs.containsKey(0))
				{
				    TreeMap tmp = new TreeMap();
				    for (Object k : this.regs.keySet())
				    {
				        tmp.put((Integer)k+1, this.regs.get(k));
				    }
				    this.regs = (TreeMap)tmp.clone();
				}
				    
				try
				{
				    for (int index = 1; index < this.nodes.size(); index++)
				    {
				    	Node temp = (Node)this.nodes.get(index);
				    	Node temp2 = (Node)this.nodes.get(0);
				    	boolean tempInit = temp.isInitialState();
				    	boolean temp2Init = temp2.isInitialState();
				    	if (temp2Init)
				    	{
				    	    temp.setInitialState(false);
				    	}
				    	else if (tempInit)
				    	{
				    	    this.nodes.set(index,temp2);
				    	    this.nodes.set(0,temp);
				    	}
				    }				        
				}
				catch (Exception e)
				{
				}
				if (this.nodes.size() > 0)
				{
				    Node init = (Node)this.nodes.get(0);
				    init.setInitialState(false);
				    this.nodes.set(0,init);
				}
			
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
