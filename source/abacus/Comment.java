package abacus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class Comment
{
	private Point p = new Point();
	private String s = "";
	private boolean selected = false;
	
	private static Font medFont = new Font("Verdana", Font.BOLD, 12);
	private static Stroke medium = new BasicStroke(2);
	private static final Color nodeColor = new Color(255,255,155);
	private static final int COMMENT_SIZE = 10;
	
	public Comment() {}
	
	public Point getP()
	{
		return p;
	}
	public void setP(Point p)
	{
		this.p = p;
	}
	public String getS()
	{
		return s;
	}
	public void setS(String s)
	{
		this.s = s;
	}
	
	public void draw(Graphics gr)
	{
		Graphics2D g = (Graphics2D)gr;
		setupDrawing(g);
		
		g.setStroke(medium);
		g.setColor(nodeColor);
		g.fillOval(p.x-COMMENT_SIZE/2,p.y-COMMENT_SIZE/2,COMMENT_SIZE,COMMENT_SIZE);
		g.setColor((selected ? Color.red : Color.black));
		g.drawOval(p.x-COMMENT_SIZE/2,p.y-COMMENT_SIZE/2,COMMENT_SIZE,COMMENT_SIZE);
		
		g.setFont(medFont);
		g.drawString(s,p.x,p.y - COMMENT_SIZE);
				
	}
	
	public boolean select(Point p)
	{
		selected = false;
		
		if (p.distanceSq(this.p) < (COMMENT_SIZE/2) * (COMMENT_SIZE/2))
		{
			selected = true;
		}
		
		return selected;
	}
	
	public void clearSelection()
	{
		selected = false;
	}
	
	private static void setupDrawing(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		// Enable Anti-Aliasing
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
}
