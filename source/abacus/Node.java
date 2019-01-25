package abacus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class Node
{
	private int register = 0;
	private boolean plus = false;
	private Node out = null;
	private Node outEmpty = null; // only if this node is empty
	private static final Color nodeColor = new Color(255,255,155);
	private Point location = new Point(50,50);
	
	private static int NODE_SIZE = 50;
	private static int ARROW_SIZE = 10;
	private static int E_DIST = 15;
	private static int TRINAGLE_SIZE = 25;
	private static int LOOP_WIDTH = 20;
	private static int LOOP_HEIGHT = 20;
	private static Stroke medium = new BasicStroke(2);
	private static Stroke thin = new BasicStroke(1);
	private static Font bigFont = new Font("Courier", Font.BOLD, 20);
	private static Font medFont = new Font("Courier", Font.BOLD, 15);
	private static Font smallFont = new Font("Courier", Font.BOLD, 12);
	private static Font smallerFont = new Font("Courier", Font.BOLD, 9);
	private static Font tinyFont = new Font("Courier", Font.BOLD, 6);
	
	// selection
	public static final int SELECTED_NONE = 0;
	public static final int SELECTED_NODE = 1;
	public static final int SELECTED_OUT = 2;
	public static final int SELECTED_OUTEMPTY = 3;
	
	private int selected = SELECTED_NONE;
	private boolean pauseState = false;
	
	public Node()
	{
		
	}
	
	public boolean isPauseState()
	{
		return pauseState;
	}
	
	public void setPauseState(boolean p)
	{
		pauseState = p;
	}
	
	public void simSelect(int which)
	{
		selected = which;
	}
	
	public void setRegister(int r)
	{
		register = r;
	}
	
	public int getRegister()
	{
		return register;
	}
	
	public void setNode(Node other)
	{
		plus = other.plus;
		out = other.out;
		outEmpty = other.outEmpty;
	}

	public Node getOut()
	{
		return out;
	}
	
	public void setLocation(Point location)
	{
		this.location = location;
	}

	public Point getLocation()
	{
		return location;
	}

	public void setOut(Node out)
	{
		this.out = out;
	}

	public Node getOutEmpty()
	{
		return outEmpty;
	}

	public void setOutEmpty(Node outEmpty)
	{
		this.outEmpty = outEmpty;
	}

	public boolean isPlus()
	{
		return plus;
	}

	public void setPlus(boolean plus)
	{
		this.plus = plus;
	}
	
	/**
	 * Drat the initial state triangle on this state
	 * @param g
	 */
	public void drawInitState(Graphics2D g)
	{
		Point one = new Point(location.x - NODE_SIZE/2, location.y);
		Point two = new Point(one.x - TRINAGLE_SIZE/2,one.y-TRINAGLE_SIZE/2);
		Point three = new Point(one.x - TRINAGLE_SIZE/2,one.y+TRINAGLE_SIZE/2);
		
		setupDrawing(g);
		g.setColor(Color.black);
		g.setStroke(medium);
		
		Polygon p = new Polygon();
		p.addPoint(one.x,one.y);
		p.addPoint(two.x,two.y);
		p.addPoint(three.x,three.y);
		
		g.setColor(Color.white);
		g.fill(p);
		
		g.setColor(Color.black);
		g.draw(p);
	}
	
	/**
	 * Is the point in the node
	 * @param p the point
	 * @return true iff the point is in the node
	 */
	public boolean isInNode(Point p)
	{
		boolean rv = false;		
		
		Ellipse2D.Float e = new Ellipse2D.Float(location.x-NODE_SIZE/2,location.y-NODE_SIZE/2,
				NODE_SIZE,NODE_SIZE);
		
		if (e.contains(p))
		{			
			rv = true;
		}
		
		return rv;
	}
	
	/**
	 * Select the node / transition at this point
	 * @param p the point the user clicked on
	 * @return true iff something in this node was selected
	 */
	public boolean select(Point p)
	{
		boolean rv = false;
		selected = SELECTED_NONE;
		
		Ellipse2D.Float e = new Ellipse2D.Float(location.x-NODE_SIZE/2,location.y-NODE_SIZE/2,
				NODE_SIZE,NODE_SIZE);
		
		if (e.contains(p))
		{
			selected = SELECTED_NODE;
			
			rv = true;
		}
		else
		{
			if (out != null)
			{
				if (out == this)
				{
					if (isNearSelfArrow(p))
					{
						selected = SELECTED_OUT;
						rv = true;
					}
				}
				else if (isNearLine(p,location,out.location))
				{
					selected = SELECTED_OUT;
					rv = true;
				}
			}
			else 
			{ // out == null
				Point q = new Point((int)(location.x - 1.5 * NODE_SIZE), 
						(int)(location.y + 1.5 * NODE_SIZE));
				
				if (isNearLine(p,location,q))
				{
					selected = SELECTED_OUT;
					rv = true;
				}
			}
			
			if (!plus)
			{
				if (outEmpty != null && isNearLine(p,location,outEmpty.location))
				{
					selected = SELECTED_OUTEMPTY;
					rv = true;
				}
				else if (outEmpty == null)
				{ // outEmpty == null
					Point q = new Point((int)(location.x + 1.5 * NODE_SIZE),
							(int)(location.y + 1.5 * NODE_SIZE));
					
					if (isNearLine(p,location,q))
					{
						selected = SELECTED_OUTEMPTY;
						rv = true;
					}
				}
			}
		}
		
		return rv;
	}
	
	public void clearSelection()
	{
		selected = Node.SELECTED_NONE;
	}
	
	public int getSelected()
	{
		return selected;
	}
	
	private static boolean isNearLine(Point where, Point end1, Point end2)
	{
		boolean rv = false;
		Point p = end1;
		Point q = end2;
		double dx = q.x-p.x;
		double dy = q.y-p.y;
		double theta = Math.atan2(dy,dx);
		double nodeRadius = NODE_SIZE/2.0;		
		double theta2 = (Math.PI / 2) + theta;
		
		nodeRadius += ARROW_SIZE / 2;
		Point q_line = new Point ((int)(nodeRadius * Math.sin(theta2)) , 
				(int)(nodeRadius * Math.cos(theta2)) );
		
		double thetaArrow = Math.PI / 2 - theta2;
		if (p.distanceSq(q) > 5)
		{						
			double HYP = 10;
			Point start = new Point((int)(HYP * Math.sin(thetaArrow)),
					(int)(HYP * Math.cos(thetaArrow)));
			
			Line2D.Float l = new Line2D.Float(p.x + start.x, p.y + start.y, q.x - q_line.x, 
					q.y + q_line.y);
			
			double dist = l.ptSegDistSq(where);
			
			// within 10 units
			if (dist < 10 * 10)
			{
				rv = true;
			}
		}		
		
		return rv;
	}
	
	public void draw(Graphics2D g)
	{
		Point p = location;
		setupDrawing(g);
		g.setStroke(thin);
		g.setFont(bigFont);
		g.setColor(Color.black);
		
		// draw arrows
		if (selected == SELECTED_OUT)
			g.setColor(Color.red);
		
		if (out != null)
		{
			Point q_real = out.getLocation();
			Point q = new Point(q_real.x,q_real.y);
			boolean self = false;
			
			if (p.equals(q))
			{
				self = true;
				q.y += 1;
			}
			drawArrow(g,q);
			
			if (self)
				drawSelfArrow(g);
		}
		else
			drawNullOutArrow(g);
		
		g.setColor(Color.black);
		
		if (!plus)
		{
			if (selected == SELECTED_OUTEMPTY)
				g.setColor(Color.red);
			
			if (outEmpty == null)
			{
				drawNullOutEmptyArrow(g);
			}
			else
			{
				Point q = outEmpty.getLocation();
				
				drawEArrow(g, q,false);
			}
			
			g.setColor(Color.black);
		}
		
		// draw node
		g.setStroke(medium);
		g.setColor(nodeColor);
		g.fillOval(p.x-NODE_SIZE/2,p.y-NODE_SIZE/2,NODE_SIZE,NODE_SIZE);
		g.setColor((selected == SELECTED_NODE ? Color.red : (pauseState ? Color.orange : Color.black)));
		g.drawOval(p.x-NODE_SIZE/2,p.y-NODE_SIZE/2,NODE_SIZE,NODE_SIZE);
		g.setStroke(thin);		
		
		// draw sign
		String sign = "" + register;
		if (plus)
			sign += "+";
		else
			sign += "-";
		
		drawStringAt(g,sign,p);
		
		g.setColor(Color.black);	
	}
	
	private void drawStringAt(Graphics g,String text, Point p)
	{
		g.setFont(bigFont);
		
		if (text.length() <= 2)
			g.drawString(text,p.x-10,p.y+7);
		else if (text.length() == 3)
			g.drawString(text,p.x-17,p.y+6);
		else if (text.length() < 5)
		{
			g.setFont(medFont);
			g.drawString(text,p.x-19,p.y+6);
		}
		else if (text.length() < 7)
		{
			g.setFont(smallFont);
			g.drawString(text,p.x-19,p.y+5);
		}
		else if (text.length() < 10)
		{
			g.setFont(smallerFont);
			g.drawString(text,p.x-21,p.y+3);
		}
		else
		{
			g.setFont(tinyFont);
			g.drawString(text,p.x-22,p.y+3);
		}
			
		
	}
	
	private void drawSelfArrow(Graphics g)
	{
		Point arrowHead = new Point(location.x,location.y-NODE_SIZE/2);
		
		g.drawOval(arrowHead.x - LOOP_WIDTH, arrowHead.y - LOOP_HEIGHT * 2,
				LOOP_WIDTH, LOOP_HEIGHT * 3);
	}
	
	private boolean isNearSelfArrow(Point p)
	{
		Point arrowHead = new Point(location.x,location.y-NODE_SIZE/2);
		
		Ellipse2D.Float e = new Ellipse2D.Float(arrowHead.x - LOOP_WIDTH - 10, 
				arrowHead.y - LOOP_HEIGHT * 2 - 10, LOOP_WIDTH + 20, LOOP_HEIGHT * 3 + 20);
		
		return e.contains(p.x,p.y);
	}
	
	private void drawEArrow(Graphics g, Point q, boolean drawRaw)
	{
		Point p = location;
		drawArrow(g,q,drawRaw);
		
		// calculate e arrow location
		
		double dx = q.x-p.x;
		double dy = q.y-p.y;
		double theta = Math.atan2(dy,dx);
		double nodeRadius = NODE_SIZE/2.0;
		
		Point p_real = new Point((int)(nodeRadius * Math.cos(theta)), 
				(int)(nodeRadius * Math.sin(theta)));
		
		double theta2 = (Math.PI / 2) + theta;
		
		Point q_real = new Point ((int)(nodeRadius * Math.sin(theta2)) , 
				(int)(nodeRadius * Math.cos(theta2)) );
		
		nodeRadius += E_DIST / 2;
		Point q_line = new Point ((int)(nodeRadius * Math.sin(theta2)) , 
				(int)(nodeRadius * Math.cos(theta2)) );
		
		Line2D.Float l = new Line2D.Float(p.x + p_real.x, p.y + p_real.y, 
				q.x - q_line.x, q.y + q_line.y);
		
		double DX = l.getX1() - l.getX2();
		double DY = l.getY1() - l.getY2();
		double eDist = (3.0 * Math.sqrt(DX * DX + DY * DY)) / 4.0;
		
		theta2 += Math.PI; 		
		
		Point eRef = new Point (q.x - q_real.x + (int)((eDist) * Math.sin(theta2)) , 
				q.y + q_real.y + (int)((-eDist) * Math.cos(theta2)) );
		
		double thetaArrow = theta2 - Math.PI;
		double arrowHyp = E_DIST;
		
		Point ePt = new Point((int)(arrowHyp * Math.cos(thetaArrow)),
				(int)(arrowHyp * Math.sin(thetaArrow)));
		
		g.drawString("e",eRef.x + ePt.x - 5,eRef.y + ePt.y + 5);
	}
	
	private void drawNullOutEmptyArrow(Graphics g)
	{
		Point q = new Point((int)(location.x + 1.5 * NODE_SIZE),(int)(location.y + 1.5 * NODE_SIZE));
		
		drawEArrow(g,q,true);
	}
	
	private void drawNullOutArrow(Graphics g)
	{
		drawArrow(g,new Point((int)(location.x - 1.5 * NODE_SIZE), 
				(int)(location.y + 1.5 * NODE_SIZE)),true);
	}
	
	private void drawArrow(Graphics g,Point q)
	{
		drawArrow(g,q,false);
	}
	
	private void drawArrow(Graphics g,Point q, boolean drawRaw)
	{
		Point p = location;
		double dx = q.x-p.x;
		double dy = q.y-p.y;
		double theta = Math.atan2(dy,dx);
		double nodeRadius = NODE_SIZE/2.0;
		
		double theta2 = (Math.PI / 2) + theta;
		
		Point q_real = new Point ((int)(nodeRadius * Math.sin(theta2)) , 
			(int)(nodeRadius * Math.cos(theta2)) );
		
		nodeRadius += ARROW_SIZE / 2;
		Point q_line = new Point ((int)(nodeRadius * Math.sin(theta2)) , 
				(int)(nodeRadius * Math.cos(theta2)) );
		
		double thetaArrow = Math.PI / 2 - theta2;
				
		if (!drawRaw && p.distanceSq(q) > 5)
		{						
			double HYP = 10;
			Point start = new Point((int)(HYP * Math.sin(thetaArrow)),
					(int)(HYP * Math.cos(thetaArrow)));
			
			g.drawLine(p.x + start.x, p.y + start.y, q.x - q_line.x, q.y + q_line.y);
		}
		else if (drawRaw)
		{
			g.drawLine(p.x, p.y, q.x - q_line.x, q.y + q_line.y);
		}
		
		// arrowhead
		theta2 += Math.PI; 		
		
		Point arrowRef = new Point (q.x - q_real.x + (int)((ARROW_SIZE) * Math.sin(theta2)) , 
				q.y + q_real.y + (int)((-ARROW_SIZE) * Math.cos(theta2)) );
		
		
		double arrowHyp = ARROW_SIZE/2;
		
		Point arrow1 = new Point((int)(arrowHyp * Math.sin(thetaArrow)),
					(int)(arrowHyp * Math.cos(thetaArrow)));		
		
		thetaArrow = Math.PI + thetaArrow;
		
		Point arrow2 = new Point((int)(arrowHyp * Math.sin(thetaArrow)),
				(int)(arrowHyp * Math.cos(thetaArrow)));
		
		Polygon poly = new Polygon();
		poly.addPoint(q.x - q_real.x,q.y + q_real.y);
		poly.addPoint(arrowRef.x + arrow1.x,arrowRef.y + arrow1.y);
		poly.addPoint(arrowRef.x + arrow2.x,arrowRef.y + arrow2.y);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.fill(poly);
	}
	
	private static void setupDrawing(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		// Enable Anti-Aliasing
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
}
