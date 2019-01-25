package abacus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.Date;

import javax.swing.JPanel;

public abstract class ZoomablePanel extends JPanel implements MouseWheelListener, 
MouseListener, MouseMotionListener
{
//	 negative = zoom in, positive = zoom out
	private double zoomFactor = 0;
	private double curZoomFactor = 0;
	private double moveX = 0, moveY = 0;
	private int lastX = -1, lastY = -1;
	
	private static RoundRectangle2D.Double up = new RoundRectangle2D.Double(30,5,20,20,15,15);
	private static RoundRectangle2D.Double down = new RoundRectangle2D.Double(30,55,20,20,15,15);
	private static RoundRectangle2D.Double left = new RoundRectangle2D.Double(5,30,20,20,15,15);
	private static RoundRectangle2D.Double right = new RoundRectangle2D.Double(55,30,20,20,15,15);
	private static RoundRectangle2D.Double in = new RoundRectangle2D.Double(30,30,10,20,15,15);
	private static RoundRectangle2D.Double out = new RoundRectangle2D.Double(40,30,10,20,15,15);
	
	private static RoundRectangle2D.Double rects[] = 
	{
		up, down, left, right, in, out
	};
	
	private static int[] xUp = {40, 35, 45 };
	private static int[] yUp = {10, 20, 20 };
	
	private static int[] xDown = xUp;
	private static int[] yDown = {70, 60, 60 };
	
	private static int[] xLeft = yUp;
	private static int[] yLeft = xUp;
	
	private static int[] xRight = yDown;
	private static int[] yRight = xUp;
	
	
	
	private static Polygon shapes[] = 
	{
		new Polygon( xUp , yUp, 3),
		new Polygon( xDown , yDown, 3),
		new Polygon( xLeft , yLeft, 3),
		new Polygon( xRight , yRight, 3),
		null,null
	};
	
	private static RoundRectangle2D.Double toggle = new RoundRectangle2D.Double(5,5,20,20,15,15);
	
	private boolean moveOn = false;
	private boolean mouseDown = false;
	private Point mousePoint = null;
	private static Stroke med = new BasicStroke(2);
	private static Stroke thin = new BasicStroke(1);
	public static final Color babyBlue = new Color(67,203,255);
	
	public ZoomablePanel()
	{		
		addMouseWheelListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		PaintThread pt = new PaintThread();
		pt.start();
		
		MoveThread mt = new MoveThread();
		mt.start();
	}
	
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		AffineTransform a = g2d.getTransform();
		preDraw(g2d);
		draw(g2d);
		g2d.setTransform(a);
		
		setupDrawing(g);
		g2d.setStroke(med);
		
		if (moveOn)
		{
			for (int x = 0; x < rects.length; ++x)
			{
				g.setColor(Color.white);
				g2d.fill(rects[x]);
				
				g.setColor(Color.black);				
				
				g2d.draw(rects[x]);
				
				if (x > 3)
				{	
					Point middle = new Point(40,40);
					final int o = 2;
					g2d.setStroke(thin);
					if (x == 4)
					{
						middle = new Point(middle.x-5,middle.y);
						Point top = new Point(middle.x,middle.y-o);
						Point bottom = new Point(middle.x,middle.y+o);
						Point left = new Point(middle.x-o,middle.y);
						Point right = new Point(middle.x+o,middle.y);
						
						g2d.drawLine(top.x,top.y,bottom.x,bottom.y);
						g2d.drawLine(left.x,left.y,right.x,right.y);
					}
					else if (x == 5)
					{
						middle = new Point(middle.x+5,middle.y);
						
						Point left = new Point(middle.x-o,middle.y);
						Point right = new Point(middle.x+o,middle.y);
						
						g2d.drawLine(left.x,left.y,right.x,right.y);
					}
					
					g2d.setStroke(med);
				}
				else if (shapes[x] != null)
				{
						g2d.fill(shapes[x]);
				}
			}
		}
		
		drawToggle(g2d);
	}

	/**
	 * @param g the grahpics object to use
	 */
	private void drawToggle(Graphics2D g)
	{
		final int o = 5;
		g.setColor(Color.white);
		g.fill(toggle);
		g.setColor(moveOn ? Color.red : Color.black);
		g.draw(toggle);
		
		g.drawLine((int)toggle.x + o,(int)toggle.y + (int)toggle.height / 2,
				(int)toggle.x + (int)toggle.width - o, (int)toggle.y + (int)toggle.height / 2);
		
		if (!moveOn)
		{ // plus
			g.drawLine((int)toggle.x + (int)toggle.width / 2,(int)toggle.y + o,
					(int)toggle.x + (int)toggle.width / 2, (int)toggle.y + (int)toggle.height - o);
		}
	}
	
	private void setupDrawing(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		// Enable Anti-Aliasing
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	protected abstract void draw(Graphics2D g);
	
	private double getScale()
	{
		double scale = 1.0;
		
		if (curZoomFactor < 0)
		{ // scale > 1
			scale = 1 + -curZoomFactor * 0.1;
		}
		else if (curZoomFactor > 0)
		{
			scale = 1/(0.1 * curZoomFactor + 1);
		}
		
		return scale;
	}
	
	private void preDraw(Graphics2D g)
	{
		double scale = getScale();
		
		g.scale(scale,scale);
		
		// move
		g.translate(moveX,moveY);
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		int num = e.getWheelRotation();
		
		zoomFactor += num;
	}

	public void mouseClicked(MouseEvent arg0){	}
	public void mousePressed(MouseEvent e) 
	{  
		
//		if (e.getButton() == MouseEvent.BUTTON1)

//	This section of code handles the left-mouse drag scrolling
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			/*if (MachinePanel.state == NodeEditor.STATE_MOD)
			{
				System.out.print("Correct\n");
			}*/
			try
			{
				Thread.sleep(100);
			}
			catch (Exception f) { }
			if (!MachinePanel.isClicked)
			{
				System.out.print("isClicked is false!\n");
				if (e.getClickCount() == 2)
				{
					moveX = moveY = zoomFactor = curZoomFactor = 0;
					repaint();
				}
			
				Point p = e.getPoint();
			
				lastX = p.x;
				lastY = p.y;
			}
		}
//		else if (e.getButton() == MouseEvent.BUTTON1)

//	This section of code handles all other left-clicks.
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			Point p = e.getPoint();
			
			if (toggle.contains(p))
			{
				moveOn = !moveOn;
				repaint();
			}
			else if (moveOn)
			{				
				if (up.contains(p) || down.contains(p) || left.contains(p) || right.contains(p) ||
						in.contains(p) || out.contains(p))
				{
					// set mouseDown
					mouseDown = true;
					mousePoint = p;
				}
			}
		}
	}
	
	/**
	 * Is pressing the mouse button here a zoom action? 
	 * @param p the point where the mouse was pressed 
	 * @return true iff it was a zoom/scroll action
	 */
	public boolean isZoomAction(Point p)
	{
		boolean rv = false;
		
		if (toggle.contains(p))
		{
			rv = true;
		}
		else if (moveOn)
		{
			if (up.contains(p)) rv = true;
			else if (down.contains(p)) rv = true;
			else if (left.contains(p)) rv = true;
			else if (right.contains(p)) rv = true;
			else if (in.contains(p)) rv = true;
			else if (out.contains(p)) rv = true;
		}
		
		return rv;
	}
	
	public Point toRealCoords(Point p)
	{
		double scale = getScale();
		
		return new Point((int)(p.x/scale - moveX) ,(int)(p.y/scale - moveY) );
	}
	
	public void mouseReleased(MouseEvent e) 
	{  
//		if (e.getButton() == MouseEvent.BUTTON2)
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			if (!MachinePanel.isClicked)
			{
				Point p = e.getPoint();
				
				double dx = lastX - p.x;
				double dy = p.y - lastY;
				double scale = getScale();
				
				moveX += dx/scale;
				moveY += dy/scale;
				
				lastX = lastY = -1;
				
				repaint();
			}
			MachinePanel.isClicked=false;
		}
		if (e.getButton() == MouseEvent.BUTTON1)
			mouseDown = false;
	}
	public void mouseEntered(MouseEvent arg0) {  }
	public void mouseExited(MouseEvent arg0) { mouseDown = false; }
	public void mouseDragged(MouseEvent e) 
	{  		 
		if (lastX != -1 && lastY != -1)
		{
			Point p = e.getPoint();
			
			double dx = p.x - lastX;
			double dy = p.y - lastY;			
			double scale = getScale();
			
			moveX += dx/scale;
			moveY += dy/scale;
			
			lastX = p.x;
			lastY = p.y;
			
			repaint();
		}
		
	}
	
	public void mouseMoved(MouseEvent arg0) {  }
	
	class MoveThread extends Thread
	{
		public void run()
		{
			long lastTime = -1;
			
			while (true)
			{	
				if (mouseDown)
				{
					long time  = new Date().getTime();
					double scale = getScale();
					double w = getWidth() / scale;
					double h = getHeight() / scale;
					long dif = time - lastTime;
					
					if (lastTime == -1)
					{
						if (out.contains(mousePoint))
							curZoomFactor += 0.1;
						else if (in.contains(mousePoint))
							curZoomFactor -= 0.1;
						else if (up.contains(mousePoint))
							moveY += 5;
						else if (down.contains(mousePoint))
							moveY -= 5;
						else if (left.contains(mousePoint))
							moveX += 5;
						else if (right.contains(mousePoint))
							moveX -= 5;
					}
					else
					{
						double change = dif / 100.0;
						
						if (out.contains(mousePoint))
							curZoomFactor += change;
						else if (in.contains(mousePoint))
							curZoomFactor -= change;
						else if (up.contains(mousePoint))
							moveY += (int)(change * 50);
						else if (down.contains(mousePoint))
							moveY -= (int)(change * 50);
						else if (left.contains(mousePoint))
							moveX += (int)(change * 50);
						else if (right.contains(mousePoint))
							moveX -= (int)(change * 50);
					}					
					
					double scaleAfter = getScale();
					double wAfter = getWidth() / scaleAfter;
					double hAfter = getHeight() / scaleAfter;		
					
					double xGained = (wAfter - w);
					double yGained = (hAfter - h);
					
					double dx = xGained/2;
					double dy = yGained/2;
					
					moveX += dx;
					moveY += dy;					
					
					zoomFactor = curZoomFactor;
					repaint();
					lastTime = time;
				}
				else
					lastTime = -1;
				
				try
				{
					Thread.sleep(10);
				}
				catch (Exception e) { }
			}
		}
	}
	
	class PaintThread extends Thread 
	{
		public void run() 
		{
			long lastTime = -1;
			
			while (true)
			{	
				if (Math.abs(curZoomFactor-zoomFactor) > 0.5)
				{
					long time  = new Date().getTime();
					double scale = getScale();
					double w = getWidth() / scale;
					double h = getHeight() / scale;
					long dif = time - lastTime;
					
					if (lastTime == -1)
					{
						if (curZoomFactor < zoomFactor)
							curZoomFactor += 0.1;
						else 
							curZoomFactor -= 0.1;
					}
					else
					{
						double change = dif / 100.0;
						
						if (curZoomFactor < zoomFactor)
							curZoomFactor += change;
						else 
							curZoomFactor -= change;
					}
					
					
					double scaleAfter = getScale();
					double wAfter = getWidth() / scaleAfter;
					double hAfter = getHeight() / scaleAfter;		
					
					double xGained = (wAfter - w);
					double yGained = (hAfter - h);
					
					double dx = xGained/2;
					double dy = yGained/2;
					
					moveX += dx;
					moveY += dy;					
					
					repaint();
					lastTime = time;
				}
				else
					lastTime = -1;
				
				try
				{
					Thread.sleep(10);
				}
				catch (Exception e) { }
			}
		}
	}
}
