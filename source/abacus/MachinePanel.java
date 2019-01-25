package abacus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

public class MachinePanel extends ZoomablePanel
{
	ArrayList comments = new ArrayList();
	ArrayList nodes = new ArrayList();
	public static Color darkBlue = new Color(0,0,128);
	MouseAction ma = new MouseAction();
	NodeEditor parent;
	Image previewImage = null;
	Point mousePoint, lastMousePoint, startPoint;
	EditNodeDialog end;
	private boolean locked = false;
	
	Object selection = null;
	
	public MachinePanel(NodeEditor parent)
	{
		this.parent = parent;
		end = new EditNodeDialog(parent);
		
		setPreferredSize(new Dimension(500,500));
		setBackground(Color.white);
		
		setBorder(BorderFactory.createLineBorder(darkBlue));
		
		addMouseListener(ma);
		addMouseMotionListener(ma);
		addMouseWheelListener(ma);
	}
	
	public void clearSelection()
	{
		for (int x = nodes.size()-1; x >= 0; --x)
		{
			Node n = (Node)nodes.get(x);
			
			n.clearSelection();
		}
		
		for (int x = 0; x < comments.size(); ++x)
		{
			Comment c = (Comment)comments.get(x);
			
			c.clearSelection();
		}
		
		repaint();
	}
	
	public int getRegCount()
	{
		TreeSet t = new TreeSet();
		
		for (int x = nodes.size()-1; x >= 0; --x)
		{
			Node n = (Node)nodes.get(x);
			
			t.add(new Integer(n.getRegister()));
		}
		
		return t.size();
	}
	
	public void lock()
	{
		locked = true;
	}
	
	public void unlock()
	{
		locked = false;
	}
	
	protected void draw(Graphics2D g)
	{
		for (int x = 0; x < comments.size(); ++x)
		{
			Comment c = (Comment)comments.get(x);
			
			c.draw(g);
		}
		
		for (int x = 0; x < nodes.size(); ++x)
		{
			Node n = (Node)nodes.get(x);
			
			n.draw(g);
		}
		
		if (nodes.size() > 0)
		{
			Node n = (Node)nodes.get(0);
			
			n.drawInitState(g);
		}
		
		drawMouseImage(g);
	}
	
	private void drawMouseImage(Graphics2D g)
	{
		if (previewImage != null)
		{
			g.drawImage(previewImage,mousePoint.x - previewImage.getWidth(null) / 2,
					mousePoint.y - previewImage.getHeight(null) / 2,null);
		}
	}
	
	// internal class because ZoomablePanel already implements mousemotionlistener
	class MouseAction implements MouseListener, MouseMotionListener, MouseWheelListener
	{
		public void mouseClicked(MouseEvent e)
		{ }

		public void mousePressed(MouseEvent e)
		{ 
			Point point = e.getPoint();
			if (!isZoomAction(point) && !locked && e.getButton() == MouseEvent.BUTTON1)
			{
				boolean doNotSelect = false;
				int state = parent.getState();
				lastMousePoint = e.getPoint();
				mousePoint = toRealCoords(point);
				
				if (state == NodeEditor.STATE_ADD)
				{
					Node n = new Node();
					n.setPlus(true);
					n.setLocation(new Point(mousePoint.x + 8,mousePoint.y - 14));
					nodes.add(n);
				}
				else if (state == NodeEditor.STATE_SUB)
				{
					Node n = new Node();
					n.setLocation(new Point(mousePoint.x,mousePoint.y-13));
					nodes.add(n);
				}
				else
				{
					if (selection != null)
					{ // apply an action to the current selection
						Node applyTo = null;
						int index = -1;
						
						for (int x = nodes.size()-1; x >= 0; --x)
						{
							Node n = (Node)nodes.get(x);
							
							if (n.isInNode(mousePoint) == true)
							{
								index = x;
								applyTo = n;
								break;
							}
						}
						
						if (applyTo != null && selection instanceof Node)
						{ // apply it
							Node selected = (Node)selection;
							int sel = selected.getSelected();
							
							if (sel == Node.SELECTED_OUT)
							{
								selected.setOut(applyTo);
							}
							else if (sel == Node.SELECTED_OUTEMPTY)
							{
								if (selected == applyTo)
									selected.setOutEmpty(null);
								else
									selected.setOutEmpty(applyTo);
							}
							else if (sel == Node.SELECTED_NODE && selected == applyTo &&
									e.getClickCount() == 2)
							{								
								Point p = parent.getLocation();
								p.x += getX() + e.getPoint().x;
								p.y += getY() + e.getPoint().y;
								
								int rv = end.modifyNode(selected,index == 0,p);
								
								if (index != 0 && rv == EditNodeDialog.MOD_MAKEINITIAL)
								{
									Object temp = nodes.get(index);
									nodes.set(index,nodes.get(0));
									nodes.set(0,temp);
								}
								else if (rv == EditNodeDialog.MOD_DELETE)
								{
									for (int x = nodes.size()-1; x >= 0; --x)
									{
										Node n = (Node)nodes.get(x);
										
										if (n.getOut() == selected)
											n.setOut(null);
										else if (n.getOutEmpty() == selected)
											n.setOutEmpty(null);
									}
									
									nodes.remove(index);
								}
								
								doNotSelect = true;
							}
						}
						else if (selection instanceof Comment)
						{
							if (e.getClickCount() == 2)
							{
								String s = (String)JOptionPane.showInputDialog(
					                    null,
					                    "Comment Text, or leave blank to remove:",
					                    "Set Comment Text",
					                    JOptionPane.PLAIN_MESSAGE,
					                    null,
					                    null,
					                    ((Comment)selection).getS());

								//If a string was returned
								if (s != null) 
								{
									if (s.length() > 0)
										((Comment)selection).setS(s);
									else
									{
										doNotSelect = true;
										comments.remove(selection);
									}
								}
							}
						}
						
						if (selection instanceof Node)
						{
							((Node)selection).clearSelection();
						}
						else if (selection instanceof Comment)
							((Comment)selection).clearSelection();
						
						selection = null;
						
					}
					
					if (!doNotSelect && selection == null) // select something
					{
						for (int x = 0; x < nodes.size(); ++x)
						{
							Node n = (Node)nodes.get(x);
							
							if (selection == null && n.select(mousePoint) == true)
							{
								selection = n;
								
								if (n.getSelected() == Node.SELECTED_NODE)
								{
									startPoint = new Point(mousePoint.x, mousePoint.y);
								}
								
								break;
							}
							else if (selection != null)
							{
								n.clearSelection();
							}
						}
						
						if (selection == null)
						{ // try comments
							for (int x = 0; x < comments.size(); ++x)
							{
								Comment c = (Comment)comments.get(x);
								
								if (selection == null && c.select(mousePoint))
								{
									selection = c;
									startPoint = new Point(mousePoint.x, mousePoint.y);
								}
								else if (selection != null)
									c.clearSelection();
							}
							
							if (selection == null)
								startPoint = null;
							
							if (selection == null && (e.getClickCount() == 2))
							{ // create a comment
								String s = (String)JOptionPane.showInputDialog(
					                    null,
					                    "Comment Text, or leave blank to remove:",
					                    "Set Comment Text",
					                    JOptionPane.PLAIN_MESSAGE,
					                    null,
					                    null,
					                    "");

								//If a string was returned
								if (s != null) 
								{
									if (s.length() > 0)
									{
										Comment c = new Comment();
										c.setS(s);
										c.setP(mousePoint);
										
										comments.add(c);
										selection = c;
									}
								}
							}
									
						}
						else
						{
							for (int x = 0; x < comments.size(); ++x)
							{
								Comment c = (Comment)comments.get(x);
								
								c.clearSelection();
							}
						}
					}
					
				}
				
				repaint();
			}
			else if (e.getButton() == MouseEvent.BUTTON3)
			{
				parent.nextButton();
				mouseMoved(e);
			}
		}

		public void mouseReleased(MouseEvent e)
		{ 
			startPoint = null;
		}

		public void mouseEntered(MouseEvent e)
		{ }

		public void mouseExited(MouseEvent e)
		{ 
			previewImage = null;
			repaint();
		}

		public void mouseDragged(MouseEvent e)
		{ 
			if (!locked && startPoint != null && selection != null)
			{
				Point p = toRealCoords(e.getPoint());
				
				int dx = p.x - startPoint.x;
				int dy = p.y - startPoint.y;
				
				Point oldLoc = (selection instanceof Node) ? ((Node)selection).getLocation() 
						: ((Comment)selection).getP();
				
				if (selection instanceof Node)
				{
					((Node)selection).setLocation(new Point(oldLoc.x + dx,oldLoc.y + dy));
				}
				else if (selection instanceof Comment)
				{
					((Comment)selection).setP(new Point(oldLoc.x + dx,oldLoc.y + dy));
				}
				
				repaint();
				startPoint = p;
			}
		}

		public void mouseWheelMoved(MouseWheelEvent e)
		{
			mousePoint = toRealCoords(lastMousePoint);
			repaint();
		}
		
		public void mouseMoved(MouseEvent e)
		{ 
			if (!locked)
			{
				int state = parent.getState();
				lastMousePoint = e.getPoint();
				mousePoint = toRealCoords(e.getPoint());
				
				if (state == NodeEditor.STATE_ADD)
				{
					previewImage = NodeEditor.transAdd;
				}
				else if (state == NodeEditor.STATE_SUB)
				{
					previewImage = NodeEditor.transSub;
				}
				else
					previewImage = null;
				
				repaint();
			}
		}
		
	}
	
}
