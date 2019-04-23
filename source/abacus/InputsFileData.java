package abacus;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
/*import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;*/
import java.math.BigInteger;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JOptionPane;

// a bean used for saving and loading the data
public class InputsFileData
{
	public ArrayList comments = new ArrayList();
	public ArrayList nodes = new ArrayList();
	public TreeMap regs = new TreeMap();
	public int regInputNum = 0;
	public ArrayList<TreeMap> otherRegs = new ArrayList<TreeMap>(); 
	final static FileDialog fileChooser = new FileDialog(new Frame());
	private RegisterEditor target;
	private Simulator runner;
	
	public InputsFileData(RegisterEditor given, Simulator sim) {target = given; runner = sim;}

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
		fileChooser.setMode(FileDialog.LOAD);
		fileChooser.setTitle("Select Puzzle");
		fileChooser.setVisible(true);
		String filename = fileChooser.getFile();
		if (filename != null)
		{
			filename = fileChooser.getDirectory() + filename;
		}
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String input;
			try
			{
				input = br.readLine();
				for (int i = 0; i<10; i++)
				{
					target.setRegisterInput(i);
					for(int j=1;j<10000;j++)
					{
						target.setRegisterContents(j, BigInteger.ZERO);
					}
				}
				while (input != null)
				{
					String[] split = input.split(" ");
					int num = Integer.parseInt(split[0]);
					int inputnum = num - 1;
					int register = Integer.parseInt(split[1]);
					BigInteger value = BigInteger.valueOf(Integer.parseInt(split[2]));
					target.setRegisterInput(inputnum);
					target.setRegisterContents(register, value);

					try
					{
						input = br.readLine();
					}
					catch (IOException e)
					{
					
					}
				} 
				target.setRegisterInput(0);
				runner.setStepNumber(0);
			}
			catch (IOException e)
			{

			}
			
		}
		catch (FileNotFoundException e)
		{
			
		}
		return false;
	}
}