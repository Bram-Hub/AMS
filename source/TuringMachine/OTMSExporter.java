package TuringMachine;

import abacus.Node;

import java.awt.FileDialog;
import java.awt.Frame;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;

public class OTMSExporter
{
	public static final int SPACING = 100;

	public static final char NUL = 0;

	private int stateCounter = 0;
	private int row = 1;
	private int col = 1;

	Map<Node, INode> nodeMap;
	INode startNode;

	Vector<State> states = new Vector<State>(100, 100);
	Vector<Transition> transitions = new Vector<Transition>(100, 100);

	// Setup the exporter and construct the intermediary representation
	public OTMSExporter(Node s)
	{
		nodeMap = new HashMap<Node, INode>();
		startNode = new INode(s);
	}

	// Get the output file name
	public String getOutFile(String ext)
	{
		// Get the output file
		FileDialog fileChooser = new FileDialog(new Frame());
		fileChooser.setMode(FileDialog.SAVE);
		fileChooser.setTitle("Export As...");
		fileChooser.setVisible(true);
		String filename = fileChooser.getFile();
		if(filename == null)
		{
			return null;
		}

		if(!filename.toLowerCase().endsWith(ext))
		{
			filename += "." + ext;
		}

		return fileChooser.getDirectory() + filename;
	}

	// Export as OTM TM file
	public void exportTM()
	{
		// Get name
		String filename = getOutFile("tm");
		if(filename == null)
		{
			return;
		}

		// Construct the OTM representation
		startNode.construct();
		for(State state : states)
		{
			state.x = (state.x * SPACING);
			state.y = (state.y * SPACING);
		}

		// Serialize the OTM representation
		//
		// ALERT!!! If the OwenTMSimulator represnetation is changed,
		// this also needs to be updated (unless you don't care about exporting TM files)
		//
		try
		{
			FileOutputStream outfile = new FileOutputStream(filename);
			ObjectOutputStream saver = new ObjectOutputStream(outfile);

			saver.writeObject(states);
			saver.writeInt(transitions.size());
			for(Transition transition : transitions)
			{
				saver.writeChar(transition.oldChar);
				saver.writeChar(transition.newChar);
				saver.writeInt(transition.direction);
				saver.writeDouble(0);
				saver.writeDouble(transition.fromState.x);
				saver.writeDouble(transition.fromState.y);
				saver.writeObject(transition.fromState.stateName);
				saver.writeDouble(transition.toState.x);
				saver.writeDouble(transition.toState.y);
				saver.writeObject(transition.toState.stateName);
			}
			saver.flush();
			saver.close();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error exporting: " + e);
			e.printStackTrace();
		}
	}

	// Export as an OTM XML file
	public void exportXML()
	{
		// Get name
		String filename = getOutFile("xml");
		if(filename == null)
		{
			return;
		}

		// Construct the OTM representation
		startNode.construct();

		// Export to XML file
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			Document xmldoc = impl.createDocument(null, null, null);

			Element root = xmldoc.createElement("TuringMachine");
			xmldoc.appendChild(root);

			Element state_elements = xmldoc.createElement("States");
			root.appendChild(state_elements);

			for(State state : states)
			{
				Element e = xmldoc.createElement("State_" + state.stateName);
				state_elements.appendChild(e);

				Element f = xmldoc.createElement("x");
				f.appendChild(xmldoc.createTextNode(Double.toString(state.x * SPACING)));
				e.appendChild(f);

				f = xmldoc.createElement("y");
				f.appendChild(xmldoc.createTextNode(Double.toString(state.y * SPACING)));
				e.appendChild(f);

				f = xmldoc.createElement("finalstate");
				f.appendChild(xmldoc.createTextNode(Boolean.toString(state.finalState)));
				e.appendChild(f);

				f = xmldoc.createElement("startstate");
				f.appendChild(xmldoc.createTextNode(Boolean.toString(state.startState)));
				e.appendChild(f);
			}

			Element transition_elements = xmldoc.createElement("Transitions");
			root.appendChild(transition_elements);

			int i = 0;
			for(Transition transition : transitions)
			{
				Element e = xmldoc.createElement("Transition_" + Integer.toString(i));
				transition_elements.appendChild(e);
				i++;

				Element f = xmldoc.createElement("fromstate");
				f.appendChild(xmldoc.createTextNode(transition.fromState.stateName));
				e.appendChild(f);

				f = xmldoc.createElement("tostate");
				f.appendChild(xmldoc.createTextNode(transition.toState.stateName));
				e.appendChild(f);

				f = xmldoc.createElement("oldchar");
				if(transition.oldChar != NUL)
				{
					f.appendChild(xmldoc.createTextNode(Character.toString(transition.oldChar)));
				}
				else
				{
					f.appendChild(xmldoc.createTextNode("null"));
				}
				e.appendChild(f);

				f = xmldoc.createElement("newchar");
				if(transition.newChar != NUL)
				{
					f.appendChild(xmldoc.createTextNode(Character.toString(transition.newChar)));
				}
				else
				{
					f.appendChild(xmldoc.createTextNode("null"));
				}
				e.appendChild(f);


				f = xmldoc.createElement("direction");
				f.appendChild(xmldoc.createTextNode(Integer.toString(transition.direction)));
				e.appendChild(f);
			}

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(xmldoc);
			Result result = new StreamResult(filename);
			transformer.transform(source, result);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error exporting: " + e);
			e.printStackTrace();
		}
	}

	//
	// Intermediary State representation
	//
	class INode
	{
		public final static int ADD = 0;
		public final static int SUB = 1;

		// Abacus node IDs
		public INode next = null;
		public INode nextEmpty = null;

		// Abacus action
		public int register = 0;
		public int action = ADD;

		// TM state IDs
		public State firstState = null;
		public State endState = null;
		public State endStateEmpty = null;

		boolean constructed = false;

		// Recursively construct the intermediary representation
		public INode(Node n)
		{
			nodeMap.put(n, this);

			register = n.getRegister();
			action = ((n.isPlus()) ? ADD : SUB);

			Node o = n.getOut();
			if(o != null)
			{
				INode on = nodeMap.get(o);
				if(on == null)
				{
					on = new INode(o);
				}
				next = on;
			}

			if(action == SUB)
			{
				Node oe = n.getOutEmpty();
				if(oe != null)
				{
					INode oen = nodeMap.get(oe);
					if(oen == null)
					{
						oen = new INode(oe);
					}
					nextEmpty = oen;
				}
			}
		}

		// Recursively construct the OTM representation
		public void construct()
		{
			// Make sure we don't try to reconstruct this
			if(constructed)
			{
				return;
			}
			constructed = true;

			movePastRegister();

			if(action == ADD)
			{
				add();
			}
			else
			{
				sub();

				moveToStart(true);
			}

			moveToStart(false);

			if(next != null)
			{
				next.construct();
				newTransition(endState, next.firstState, '1', '1', Transition.NULL);
			}
			else
			{
				endState.finalState = true;
			}
			
			if(action == SUB)
			{
				if(nextEmpty != null)
				{
					nextEmpty.construct();
					newTransition(endStateEmpty, nextEmpty.firstState, '1', '1', Transition.NULL);
				}
				else
				{
					endStateEmpty.finalState = true;
				}
			}
		}

		//  Move past the "0" following the block we're about to modify
		private void movePastRegister()
		{
			col = 1;

			State prevState = null;
			for(int i = 0; i <= register; i++)
			{
				State state1 = newState();
				State state2 = newState();

				newTransition(state1, state1, '0', '1', Transition.NULL);
				newTransition(state1, state2, '1', NUL, Transition.RGHT);
				newTransition(state2, state2, '1', NUL, Transition.RGHT);
				if(prevState == null)
				{
					firstState = state1;
				}
				else
				{
					newTransition(prevState, state1, '0', NUL, Transition.RGHT);
				}
				prevState = state2;
			}

			row++;
		}

		// Add one to the block and shift the subsiquent blocks one to the right
		private void add()
		{
			State starts = states.get(stateCounter - 1);

			col = 2;
			row++;
			State state0 = newState();
			State state1 = newState();
			State state2 = newState();

			row--;
			col--;
			State state3 = newState();
			col -= 2;
			State state4 = newState();
			row++;
			col++;


			newTransition(starts, state0, '0', '0', Transition.NULL);
			newTransition(state0, state1, '0', '1', Transition.NULL);

			newTransition(state1, state2, '1', NUL, Transition.RGHT);
			newTransition(state2, state3, '1', '0', Transition.NULL);
			newTransition(state3, state4, '0', NUL, Transition.RGHT);
			newTransition(state4, state4, '1', NUL, Transition.RGHT);
			newTransition(state4, state1, '0', '1', Transition.NULL);

			endState = state2;

			row++;
		}

		// Remove one from the current block (if possible) and shift the
		// subsiquent blocks one to the left
		private void sub()
		{
			State starts = states.get(stateCounter - 1);

			col = 2;
			State state0 = newState();
			State state1 = newState();
			State state2 = newState();
			State state3 = newState();

			row++;
			State state4 = newState();
			State state5 = newState();
			State state6 = newState();
			row--;
			col--;
			State state7 = newState();
			col -= 2;
			State state8 = newState();

			row++;
			col = ((int)state2.x);
			State state9 = newState();
			State state10 = newState();

			col = ((int)state6.x) + 1;
			State state11 = newState();
			State state12 = newState();

			newTransition(starts, state0, '0', '0', Transition.NULL);
			newTransition(state0, state1, '0', NUL, Transition.LEFT);
			newTransition(state1, state2, '1', NUL, Transition.LEFT);
			newTransition(state2, state3, '1', NUL, Transition.RGHT);

			newTransition(state3, state4, '1', '0', Transition.NULL);
			newTransition(state4, state5, '0', NUL, Transition.RGHT);
			newTransition(state5, state6, '0', NUL, Transition.RGHT);
			newTransition(state6, state7, '1', NUL, Transition.LEFT);
			newTransition(state7, state8, '0', '1', Transition.NULL);
			newTransition(state8, state8, '1', NUL, Transition.RGHT);
			newTransition(state8, state3, '0', NUL, Transition.LEFT);

			newTransition(state2, state9, '0', NUL, Transition.RGHT);
			newTransition(state9, state10, '1', NUL, Transition.RGHT);

			newTransition(state6, state11, '0', NUL, Transition.LEFT);
			newTransition(state11, state12, '0', NUL, Transition.LEFT);

			endState = state12;
			endStateEmpty = state10;

			row++;
		}

		// Move to the beginning of the tape. This can conditionally act on the
		// normal output or the 'empty' output (for subtraction nodes)
		private void moveToStart(boolean emptyPath)
		{
			col = 3;
			State state0 = newState();
			State state1 = newState();
			State state2 = newState();
			State state3 = newState();
			State state4 = newState();

			State starts;
			if(!emptyPath)
			{
				starts = endState;
			}
			else
			{
				starts = endStateEmpty;
			}

			newTransition(starts, state0, '0', '0', Transition.NULL);
			newTransition(state0, state1, '0', NUL, Transition.LEFT);

			newTransition(state1, state1, '1', NUL, Transition.LEFT);
			newTransition(state1, state2, '0', NUL, Transition.LEFT);
			newTransition(state2, state1, '1', NUL, Transition.LEFT);
			newTransition(state2, state3, '0', NUL, Transition.RGHT);
			newTransition(state3, state4, '0', NUL, Transition.RGHT);

			if(!emptyPath)
			{
				endState = state4;
			}
			else
			{
				endStateEmpty = state4;
			}

			row++;
		}
	}

	// Add a new state to the OTM representation
	private State newState()
	{
		State s = new State(col, row, String.valueOf(stateCounter), (stateCounter == 0));
		stateCounter++;
		col++;
		states.add(s);
		return s;
	}

	// Add a new transition to the OTM representation
	private Transition newTransition(State from, State to, char o, char n, int d)
	{
		Transition t = new Transition(from, to, o, n, d);
		transitions.add(t);
		return t;
	}
}

// State representation
//
// ALERT!!! If the OwenTMSimulator represnetation is changed,
// this also needs to be updated (unless you don't care about exporting TM files)
//
class State implements Serializable
{
	double x;
	double y;
	String stateName;
	boolean finalState = false;
	boolean currentState = false;
	boolean startState = false;
	boolean highlight = false;

	public State(double col, double row, String name, boolean s)
	{
		x = col;
		y = row;
		stateName = name;
		startState = s;
	}
}

// Transition representation
class Transition
{
	public static final int NULL = 0;
	public static final int LEFT = 1;
	public static final int RGHT = 2;

	public State fromState;
	public State toState;

	public char oldChar = 0;
	public char newChar = 0;
	public int direction = 0;

	public Transition(State from, State to, char o, char n, int d)
	{
		fromState = from;
		toState = to;
		oldChar = o;
		newChar = n;
		direction = d;

		// If the states are directly virtical to each other, jitter the x position
		// to fix label problems.
		if(fromState != toState && fromState.x == toState.x)
		{
			if(fromState.y < toState.y)
			{
				toState.x += 0.01;
			}
			else
			{
				fromState.x += 0.01;
			}
		}
	}
}

