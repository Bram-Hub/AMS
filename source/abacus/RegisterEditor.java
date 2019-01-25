package abacus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.math.BigInteger;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class RegisterEditor extends JPanel implements MouseMotionListener, MouseListener, ActionListener
{
	RegisterPanel regPanel = new RegisterPanel();
	private static final int scrollButtonWidth = 40;
	private TreeMap backup = null; // for use in computation
	public TreeMap regs = new TreeMap(); // maps Integer -> BigInteger
	private final static BigInteger biMillion = new BigInteger("1000000");
	private final static BigIntegerBean BIBZero = new BigIntegerBean();
	private double curReg = 1.0;
	private final static int regWidth = 60;
	private final static int INIT_HEIGHT = 70;
	private final static int INIT_WIDTH = 400;
	private JButton jump = new JButton("Jump To Register");
	public static Color darkGray = new Color(32, 32, 32);
	public static Font normal = new Font("Verdana",Font.PLAIN,10);
	public static Font small = new Font("Verdana",Font.PLAIN,8);
	public static final int REG_ARC = 20;
	public static final int BUTTON_ARC = 15;
	public static final Color compBlue = new Color(0,146,255);
	public static final Color babyBlue = new Color(67,203,255);
	private static final int buttonTriangleHeightOffset = 10;
	private NodeEditor ne;
	
	private int selectedReg = -1;
	private boolean locked = false;
	
	public RegisterEditor(NodeEditor ne)
	{		
		this.ne = ne;
		regPanel.addMouseListener(this);
		regPanel.addMouseMotionListener(this);
		
		setLayout(new BorderLayout());
		
		add(regPanel, BorderLayout.CENTER);
		
		JPanel south = new JPanel();
		south.setBorder(BorderFactory.createLineBorder(MachinePanel.darkBlue));
		regPanel.setBackground(NodeEditor.babyBlue);
		
		south.add(jump);
		jump.addActionListener(this);
		add(south,BorderLayout.SOUTH);
	}
	
	public void clearSelection()
	{
		selectedReg = -1;
		repaint();
	}
	
	public void lock()
	{
		selectedReg = -1;
		locked = true;
	}
	
	public void unlock()
	{
		locked = false;

		repaint();
	}
	
	public void initial()
	{
		backup = (TreeMap)regs.clone();
	}
	
	public void restore()
	{
		regs = (TreeMap)backup.clone();
	}
	/**
	 * Add a pebble to this register
	 * @param reg the register number to add to
	 */
	public void addOne(int reg)
	{
		BigIntegerBean contents = getRegisterContents(reg);

		setRegisterContents(reg,contents.val.add(BigInteger.ONE));
		selectedReg = reg;
		repaint();
	}
	
	/**
	 * Subtact one from this register
	 * @param reg the register to take one pebble out of
	 * @return false if we're empty
	 */
	public boolean subOne(int reg)
	{
		boolean rv = false;
		BigIntegerBean contents = getRegisterContents(reg);
		
		if (!contents.val.equals(BigInteger.ZERO))
		{
			rv = true;

			setRegisterContents(reg,contents.val.subtract(BigInteger.ONE));
		}




		
		selectedReg = reg;
		repaint();
		
		return rv;
	}
	
	public void setRegisterContents(int num, BigInteger i)
	{
		if (i.equals(BigInteger.ZERO))
			regs.remove(new Integer(num));
		else	
			regs.put(new Integer(num),new BigIntegerBean(i));
	}
	
	public BigIntegerBean getRegisterContents(int regNumber)
	{
		BigIntegerBean rv;
		
		Object o = regs.get(new Integer(regNumber));
		if (o != null)
		{
			rv = (BigIntegerBean)o;
		}
		else
			rv = BIBZero;
		
		return rv;
	}
	
	class RegisterPanel extends JPanel
	{		
		boolean leftOn = false, rightOn = false;
		
		public RegisterPanel()
		{
			setPreferredSize(new Dimension(INIT_WIDTH * 2,INIT_HEIGHT * 2));
			
			RegTimer rt = new RegTimer();
			rt.start();
		}
		
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g;
			setupDrawing(g);
			
			AffineTransform t = g2d.getTransform();
			
			strechToFill(g2d);

			drawBins(g2d);
			
			g2d.setTransform(t);
			
			drawButtons(g2d);
		}

		private void drawBins(Graphics2D g)
		{
			long init = Math.round(Math.floor(curReg));
			double end = 11 + init + (INIT_WIDTH) / regWidth;
			double startX = (init - curReg) * regWidth + scaleX(scrollButtonWidth);
			g.setColor(Color.black);			
			
			for (long num = init; num < end;++num, startX += regWidth)
			{
				if (num >= 1 && num <= Integer.MAX_VALUE)
				{
					int regNum = (int)num;
					BigInteger amount = getRegisterContents(regNum).val;
					
					RoundRectangle2D.Double r = new RoundRectangle2D.Double((int)(startX + 5), 5, regWidth - 10,INIT_HEIGHT - 30,REG_ARC,REG_ARC);
					
					g.setColor(Color.white);
					g.fill(r);
					g.setColor((regNum == selectedReg && locked) ? Color.red : darkGray);
					g.draw(r);
					g.setFont(normal);
					
					RoundRectangle2D.Double q = new RoundRectangle2D.Double((int)(startX + 5), 5, regWidth - 10,INIT_HEIGHT - 60,REG_ARC,REG_ARC);
					
					g.setColor(Color.gray);
					g.fill(q);
					g.setColor((regNum == selectedReg && locked) ? Color.red : darkGray);
					g.draw(q);
					g.setFont(normal);
					
					g.drawString("+",(int)(startX + 27),INIT_HEIGHT - 57);
					
					RoundRectangle2D.Double s = new RoundRectangle2D.Double((int)(startX + 5), 35, regWidth - 10,INIT_HEIGHT - 60,REG_ARC,REG_ARC);
					
					g.setColor(Color.gray);
					g.fill(s);
					g.setColor((regNum == selectedReg && locked) ? Color.red : darkGray);
					g.draw(s);
					g.setFont(normal);
					
					g.drawString("-",(int)(startX + 28),INIT_HEIGHT - 27);
					
					if (regNum < 1000)
						g.drawString("Reg #" + regNum,(int)startX+10,INIT_HEIGHT-9);
					else if (regNum < 1000000)
						g.drawString("R #" + regNum,(int)startX+10,INIT_HEIGHT-9);
					else
					{						
						g.setFont(small);
						g.drawString("" + regNum,(int)startX+10,INIT_HEIGHT-9);
						g.setFont(normal);
					}
					
					if (amount.min(biMillion).equals(biMillion))
					{
						g.drawString(">999999",(int)startX+8,(int)r.getCenterY() + 5);	
					}
					else
						g.drawString(amount.toString(),(int)startX+10,(int)r.getCenterY() + 5);					
				}
			}
		}
		
		private void strechToFill(Graphics2D g)
		{
			double scaleX = (double)getHeight()*5.5 / INIT_WIDTH;
			double scaleY = (double)getHeight() / INIT_HEIGHT;
			
			g.scale(scaleX,scaleY);
		}
		
		private void drawButtonsUnder(boolean first,Graphics2D g)
		
		{
			int h = regPanel.getHeight() - 1;
			
			if (first && leftOn)
			{
				Polygon p = new Polygon();
				p.addPoint(scrollButtonWidth + 1,buttonTriangleHeightOffset);
				p.addPoint(buttonTriangleHeightOffset + 1,h/2);
				p.addPoint(scrollButtonWidth + 1,h-buttonTriangleHeightOffset);
				
				g.setColor(babyBlue);
				g.fill(p);
			}
			
			if (!first && rightOn)
			{
				Polygon p = new Polygon();
				p.addPoint(0,buttonTriangleHeightOffset);
				p.addPoint(scrollButtonWidth-buttonTriangleHeightOffset,h/2);
				p.addPoint(0,h-buttonTriangleHeightOffset);
				
				g.setColor(babyBlue);
				g.fill(p);				
			}
		}
		
		private Image oneImage = null;
		private boolean lastOnOne = false;
		
		private Image getImageOne()
		{
			int h = regPanel.getHeight() - 1;
			int imW = scrollButtonWidth+1;
			int imH = h+1;
			Image rv;
			
			if (oneImage != null && oneImage.getWidth(null) == imW 
					&& oneImage.getHeight(null) == imH && lastOnOne == leftOn)
			{
				rv = oneImage;
			}
			else
			{		
				Image button = createImage(imW,imH);
				Graphics2D g = (Graphics2D)button.getGraphics();
				setupDrawing(g);
				
				g.setColor(babyBlue);
				g.fillRect(0,0,button.getWidth(null),button.getHeight(null));
				
				RoundRectangle2D.Double one = new RoundRectangle2D.Double(0,0,scrollButtonWidth,h,BUTTON_ARC,BUTTON_ARC);
	
				g.setColor(Color.lightGray);
				g.fill(one);
				
				g.setColor(Color.black);
				g.draw(one);
				
				Polygon p = new Polygon();
				p.addPoint(scrollButtonWidth,buttonTriangleHeightOffset);
				p.addPoint(buttonTriangleHeightOffset,h/2);
				p.addPoint(scrollButtonWidth,h-buttonTriangleHeightOffset);
				
				g.setColor(Color.black);
				g.drawPolyline(p.xpoints,p.ypoints,p.npoints);
				
				drawButtonsUnder(true,g);
				
				button = NodeEditor.makeTransparent(button,babyBlue);
				oneImage = rv = button;
				lastOnOne = leftOn;
			}
			
			return rv;
		}
		
		private Image twoImage = null;
		private boolean lastOnTwo = false;
		
		private Image getImageTwo()
		{
			int h = regPanel.getHeight() - 1;
			int imW = scrollButtonWidth+1;
			int imH = h+1;
			Image rv;
			
			if (twoImage != null && twoImage.getWidth(null) == imW 
					&& twoImage.getHeight(null) == imH && lastOnTwo == rightOn)
			{
				rv = twoImage;
			}
			else
			{		
				Image button = createImage(scrollButtonWidth+1,h+1);
				Graphics2D g = (Graphics2D)button.getGraphics();
				setupDrawing(g);
				
				g.setColor(babyBlue);
				g.fillRect(0,0,button.getWidth(null),button.getHeight(null));
				
				RoundRectangle2D.Double two = new RoundRectangle2D.Double(0,0,scrollButtonWidth,h,BUTTON_ARC,BUTTON_ARC);
				
				g.setColor(Color.lightGray);
				g.fill(two);
				
				g.setColor(Color.black);
				g.draw(two);
				
				Polygon p = new Polygon();			
				p.addPoint(0,buttonTriangleHeightOffset);
				p.addPoint(scrollButtonWidth-buttonTriangleHeightOffset,h/2);
				p.addPoint(0 ,h-buttonTriangleHeightOffset);
				
				g.setColor(Color.black);
				g.drawPolyline(p.xpoints,p.ypoints,p.npoints);
				
				drawButtonsUnder(false,g);
				rv = twoImage = button = NodeEditor.makeTransparent(button,babyBlue);
				lastOnTwo = rightOn;
			}
			
			return rv;
		}

		private void drawButtons(Graphics2D graphics)
		{			
			int w = regPanel.getWidth();
			
			graphics.drawImage(getImageOne(),0,0,null);
			
			// two
			graphics.drawImage(getImageTwo(),w-scrollButtonWidth,0,null);
		}
		
		public void resetButtons()
		{
			leftOn = rightOn = false;
		}
		
		private void setupDrawing(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			// Enable Anti-Aliasing
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		class RegTimer extends Thread
		{
			public void run() 
			{				
				long lastTime = -1;
				long held = 0;
				
				while (true)
				{					
					if (leftOn || rightOn)
					{
						long time  = new Date().getTime();
						
						if (lastTime != -1)
						{
							double div = Math.max(500-(held/7),100);
							long dif = time - lastTime;
							double dx = dif / div;
							
							if (rightOn)
							{
								//curReg += dx;
								curReg++;
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								if (curReg > Integer.MAX_VALUE)
									curReg = Integer.MAX_VALUE;
							}
							else if (leftOn)
							{
								//curReg -= dx;
								curReg--;
								
								if (curReg < 1)
									curReg = 1;
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							held += dif;
							repaint();
						}
						
						lastTime = time;
					}
					else
					{
						lastTime = -1;
						held = 0;
					}
						
					try
					{						
						Thread.sleep(10);
					}
					catch (Exception e) { }
				}
			}
		}
	}

	public void mouseDragged(MouseEvent arg0)
	{		
	}

	public void mouseMoved(MouseEvent e)
	{
		Point p = e.getPoint();
		int w = regPanel.getWidth();
		int h = regPanel.getHeight();
		
		RoundRectangle2D.Double left = new RoundRectangle2D.Double(0,0,scrollButtonWidth,h,BUTTON_ARC,BUTTON_ARC);
		RoundRectangle2D.Double right = new RoundRectangle2D.Double(w-scrollButtonWidth,0,scrollButtonWidth,h,BUTTON_ARC,BUTTON_ARC);
		
		if (left.contains(p))
		{
			if (regPanel.leftOn == false || regPanel.rightOn == true)
			{
				regPanel.leftOn = true;
				regPanel.rightOn = false;
				regPanel.repaint();
			}			
		}
		else if (right.contains(p))
		{
			if (regPanel.leftOn == true || regPanel.rightOn == false)
			{
				regPanel.leftOn = false;
				regPanel.rightOn = true;
				regPanel.repaint();
			}
		}
		else
		{
			if (regPanel.leftOn == true || regPanel.rightOn == true)
			{
				regPanel.resetButtons();
				regPanel.repaint();
			}
		}		
	}
	
	// -1 if none
	public int getClickedRegister(Point p)
	{
		long init = Math.round(Math.floor(curReg));
		double end = 1 + init + (INIT_WIDTH) / regWidth;
		double startX = (init - curReg) * regWidth + scaleX(scrollButtonWidth);
		int rv = -1;
		
		//box size 95 px
		//gap size 20px
		//scale initial gap if init = 1
		//initial gap size
		long regNumber = init;
		double startGapSize = Math.round(10242*Math.pow(regPanel.getWidth(), -.789));	//calculation for initial gap based on window width
		/*
		if(curReg!=1){
			startGapSize = ((double)Math.ceil(curReg)-(double)curReg)*(startGapSize+115);
			regNumber++;
		}
		*/
		if(curReg!=1 && p.x<startGapSize) rv = (int)(regNumber-1);
		else{
			for(double i = startGapSize; i<p.x;){
				if(p.x>=i&&p.x<=i+95){
					rv = (int)regNumber;
				}
				i+=115;
				regNumber++;
			}
		}
		
		return rv;
		
		//return (int)Math.ceil((double)(p.x)/120.0-1+curReg);
		
		/*
		for (long num = init; num < end && num >= 1;++num, startX += regWidth)
		{
			if (num <= Integer.MAX_VALUE)
			{
				int regNum = (int)num;					
				System.out.println("HERE:    "+startX+5+" "+(regWidth-10));
				RoundRectangle2D.Double r = new RoundRectangle2D.Double((int)(startX + 5), 5, regWidth - 10, INIT_HEIGHT - 30 ,REG_ARC,REG_ARC);

				if (r.contains(p))
				{
					rv = regNum;
					break;
				}
			}
		}
		
		return rv;
		*/
	}
	
	// trnaslate x
	public double scaleX(double from)
	{
		double scaleX = INIT_WIDTH / (double)regPanel.getWidth();
		
		return from * scaleX;
	}
	
	// translate real coords to stetched coords
	public Point translatePoint(Point from)
	{
		double scaleX = INIT_WIDTH / (double)regPanel.getWidth();
		
		double scaleY = INIT_HEIGHT / (double)regPanel.getHeight();
		
		return new Point((int)(from.x /** scaleX*/), (int)(from.y * scaleY));
	}

	public void mouseClicked(MouseEvent e){}

	public void mousePressed(MouseEvent e)
	{
		if (!locked && e.getButton() == MouseEvent.BUTTON1)
		{
			Point p = e.getPoint();
			
			if (p.x > scrollButtonWidth && p.x < regPanel.getWidth() - scrollButtonWidth)
			{			
				Point real = translatePoint(p);
				
				int clicked = getClickedRegister(real);
				


				if (clicked != -1)
				{
					if (real.y <= 15)
					{

						addOne(clicked);
						return;
					}
					else if (real.y >= 35)
					{

						subOne(clicked);
						return;
					}
					else
					{

						BigInteger cur = getRegisterContents(clicked).val;
					
						String s = (String)JOptionPane.showInputDialog(
								null,
								"What would you like to set this register to?",
								"Register " + clicked,
								JOptionPane.PLAIN_MESSAGE,
								null,
								null,
								cur.toString());

						//If a string was returned
						if ((s != null) && (s.length() > 0)) {
							try
							{
								BigInteger bi = new BigInteger(s);

								setRegisterContents(clicked, bi);
								repaint();
							}
							catch (NumberFormatException er)
							{
								JOptionPane.showMessageDialog(null, "You didn't enter an integer: '" + s + "'");
							}
					    
							return;
						}
					}
					
				}
			}
		}
	}

	public void mouseReleased(MouseEvent arg0)
	{
	}

	public void mouseEntered(MouseEvent arg0)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{
		if (regPanel.leftOn == true || regPanel.rightOn == true)
		{
			regPanel.resetButtons();
			regPanel.repaint();
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == jump)
		{
			String s = (String)JOptionPane.showInputDialog(
                    null,
                    "Which register would you like to jump to?",
                    "Enter Register Number",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "" + Math.round(curReg));

			//If a string was returned
			if ((s != null) && (s.length() > 0)) {
			    try
			    {
			    	int i = Integer.parseInt(s);
			    	
			    	curReg = Math.max(1,i);
			    	repaint();
			    }
			    catch (NumberFormatException er)
			    {
			    	JOptionPane.showMessageDialog(null, "You didn't enter an integer between 1 and " +
			    			Integer.MAX_VALUE + ": '" + s + "'");
			    }
			    
			    return;
			}
		}
	}
	
	
}
