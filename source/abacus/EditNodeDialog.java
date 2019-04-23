package abacus;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class EditNodeDialog extends JDialog implements ActionListener, ItemListener
{
	private JTextField tf = new JTextField(14);
	//private String[] choices = { "Addition State", "Subtraction State" };
	//private JComboBox combo = new JComboBox(choices);
	private JCheckBox pauseCheck = new JCheckBox("Pause State");
	private JCheckBox initial = new JCheckBox("Initial");
	private JButton ok = new JButton("Ok");
	
	private boolean pressedOk, deleted;
	private boolean sInitial; // initial state
	private boolean sAddition; // Addition state?
	private int register;

	public void itemStateChanged(ItemEvent e)
	{
		return;
	}
	
	public EditNodeDialog(Frame parent)
	{
		super(parent,true);
		setTitle("State Editor");
		
		ok.addActionListener(this);
		tf.addActionListener(this);
		
	
		setLayout(new FlowLayout());
		setPreferredSize(new Dimension(405,100));
		
		add(new JLabel("Register:"));
		add(tf);
	//	add(combo);
		add(pauseCheck);
		add(initial);
		add(ok);
		
		pack();
	}
	
	public final static int MOD_OK = 0; // modifications went ok
	public final static int MOD_MAKEINITIAL = 1; // modifications ok, make this the initial state
	public final static int MOD_DELETE = 2; // delete this state leole
	
	/**
	 * Modify this node
	 * @param n the node to modify
	 * @param initial is this the initial state?
	 * @param p the point where the node is
	 * @return one of the MOD_ values
	 */
	public int modifyNode(Node n, boolean initial, Point p)
	{		
		int rv = MOD_OK;
		setLocation(p.x - getWidth() / 2,p.y - getHeight() / 2);
		
		pressedOk = false;
		deleted = false;
		this.initial.setSelected(initial);
		if (initial)
			this.initial.setEnabled(false);
		else
			this.initial.setEnabled(true);
		
		//combo.setSelectedIndex(n.isPlus() ? 0 : 1);
		tf.setText("" + n.getRegister());
		tf.setSelectionStart(0);
		tf.setSelectionEnd(tf.getText().length());
		pauseCheck.setSelected(n.isPauseState());
		
		setVisible(true);
		
		if (pressedOk)
		{
			//n.setPlus(sAddition);
			n.setRegister(register);
			n.setPauseState(pauseCheck.isSelected());
			
			if (sInitial)
				rv = MOD_MAKEINITIAL;
			
		}
		else if (deleted)
		{
			rv = MOD_DELETE;
		}
		
		return rv;
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok || e.getSource() == tf)
		{
			pressedOk = true;
			sInitial = initial.isSelected();
			//sAddition = combo.getSelectedIndex() == 0;
			
			try
			{
				int registerTemp = Integer.parseInt(tf.getText());
				if (registerTemp < 1)
				    throw new NumberFormatException();
				register = registerTemp;
				setVisible(false);
			}
			catch (NumberFormatException er)
			{
				JOptionPane.showMessageDialog(null,"Your register is not a valid positive integer: '" 
						+ tf.getText() + "'");
		        pressedOk = false;
			}
		}
	}

}
