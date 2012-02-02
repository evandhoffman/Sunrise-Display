package com.evanhoffman.sunrise;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Sunrise extends Frame {

	private Date lastDrawnAt = null;
	private int secondsBetweenRedraw = 2;

	static final int WIDTH=1000;
	static final int HEIGHT=500;
	static final MapCoordinate location = new MapCoordinate("Mineola",40.738675, -73.645687);
	//	static final MapCoordinate location = new MapCoordinate("Lima, Peru",-12.1, -77.1);
	//	static final MapCoordinate location = new MapCoordinate("Punta Arenas",-53.153, -70.92);

	public Sunrise() { 
		this("Sunrise v1.0"); 
	}

	public Sunrise(String title) {
		super(title);
		createUI();
	}

	void createUI() {
		setSize(800, 600);
		center();

		addWindowListener(new WindowAdapter() {
			public void windowClosing (WindowEvent e) {
				dispose();
				System.exit(0);
			}
		}

		);
	}

	void center() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		int x = (screenSize.width - frameSize.width) / 2;
		int y = (screenSize.height - frameSize.height) / 2;
		setLocation (x, y);	
	}

	public static void main(String[] args) {
		Sunrise f = new Sunrise();
		f.setTitle("Sunrise v1.0");
		f.setSize(WIDTH,HEIGHT);
		f.center();
		f.setVisible(true);
	}

	public void drawScene(Graphics g) {

	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;

		GradientPaint gp = null;

		// Draw the Sky.
		gp = new GradientPaint(0, 0, new Color(0,191,255), 0, HEIGHT-100, new Color(135,206,250), false); // Brown gradient

		Rectangle2D sky = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT-100);
		g2.setPaint(gp);
		g2.fill(sky);


		// Draw the house.
		Rectangle2D houseBody = new Rectangle2D.Double((WIDTH/2)-50, HEIGHT-200, 100, 100);
		g2.setPaint(Color.RED);
		g2.fill(houseBody);

		Polygon houseRoof = new Polygon();
		houseRoof.addPoint((WIDTH/2)-75, HEIGHT-200);
		houseRoof.addPoint((WIDTH/2)+75, HEIGHT-200);
		houseRoof.addPoint((WIDTH/2), HEIGHT-300);
		g2.fill(houseRoof);

		// Calculate sun's position.

		int centerX = WIDTH/2;
		int centerY = HEIGHT-100;

		double deg2rad = Math.PI / 180d;
		//		for (int i = 0; i < 360; i += 5) {
		//			double angleRadians = i * deg2rad;
		//			int endX = (int)(Math.cos(angleRadians) * sunDistance)+centerX;
		//			int endY = (int)(Math.sin(angleRadians) * sunDistance)+centerY;
		//			Line2D line = new Line2D.Float(centerX, centerY, endX, endY);
		//			g2.draw(line);
		//		}

		DateFormat df = new SimpleDateFormat("HH:mm z");

		int sunX = 0; 
		int sunY = 0;

		Calendar cal = Calendar.getInstance();
		SunPosition sunPos = new SunPosition(cal.getTime(), location);
		int sunDistance =  (int)(WIDTH * 0.5) - 100;

		Ellipse2D sun = null;

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);

		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);


		while(cal.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
			cal.add(Calendar.MINUTE, 60);

			sunPos.calculatePosition(cal.getTime());

			if (location.getLatitude() > 0) {
				//						System.out.println(sunPos);
				/*
				 * Add 90 to the Azimuth for drawing.  In NY, sunrise is at around 120¼ Azimuth.  On the unit circle that
				 * I learned in school, with 0 to the right and 90¼ at the top, 120¼ is in quadrant II 
				 * (where X is negative and Y is positive).  
				 * But with the azimuth circle, 0¼ is North, 90¼ is East, 180¼ South,
				 * 270¼ West, and 120¼ is in what would be Quadrant IV.  So you want to subtract 90, 
				 * but rotate 180 (since the sun is in the south).
				 */
				sunX = centerX + (int)(Math.cos((90+ sunPos.getAzimuth()) * deg2rad) * sunDistance) ;
				sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
				//sunY = centerY;
			} else {
				sunX = centerX + (int)(Math.cos((90 + sunPos.getAzimuth()) * deg2rad) * sunDistance) ;
				sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
			}			
			sun = new Ellipse2D.Double(sunX,sunY, 15, 15);
			g2.setPaint(Color.ORANGE);
			g2.setStroke(new BasicStroke(8));
			g2.fill(sun);
			g2.setPaint((Color.BLACK));
			g2.drawString(df.format(cal.getTime()), sunX, sunY);
		}

		// Draw the ground.
		gp = new GradientPaint(0, HEIGHT-100, new Color(205,133,63), 0, HEIGHT, new Color(222,184,135), false); // Brown gradient

		Rectangle2D ground = new Rectangle2D.Double(0, HEIGHT-100, WIDTH, 100);
		g2.setPaint(gp);
		g2.fill(ground);

		// "now" sun in front of ground.

		sunPos.calculatePosition(new Date());

		if (location.getLatitude() > 0) {
			System.out.println(sunPos);
			/*
			 * Add 90 to the Azimuth for drawing.  In NY, sunrise is at around 120¼ Azimuth.  On the unit circle that
			 * I learned in school, with 0 to the right and 90¼ at the top, 120¼ is in quadrant II 
			 * (where X is negative and Y is positive).  
			 * But with the azimuth circle, 0¼ is North, 90¼ is East, 180¼ South,
			 * 270¼ West, and 120¼ is in what would be Quadrant IV.  So you want to subtract 90, 
			 * but rotate 180 (since the sun is in the south).
			 */
			sunX = centerX + (int)(Math.cos((90+ sunPos.getAzimuth()) * deg2rad) * sunDistance) ;
			sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
			//sunY = centerY;
		} else {
			sunX = centerX + (int)(Math.cos((90 + sunPos.getAzimuth()) * deg2rad) * sunDistance) ;
			sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
		}			
		sun = new Ellipse2D.Double(sunX,sunY, 40, 40);
		g2.setPaint(Color.YELLOW);
		g2.setStroke(new BasicStroke(8));
		g2.fill(sun);
		g2.setPaint((Color.BLACK));
		g2.drawString("Right Now: "+df.format(cal.getTime()), sunX, sunY);

		lastDrawnAt = new Date();
		NumberFormat nf = new DecimalFormat("###.000");
		String captions[] = {"Location: "+location.getName()+", "+nf.format(location.getLatitude())+"¼, "+nf.format(location.getLongitude())+"¼",
				"Drawn at: "+lastDrawnAt,
				"Azimuth: "+nf.format(sunPos.getAzimuth())+"¼, elevation: "+nf.format(sunPos.getElevation())+"¼"};
		int captionY = 40;
		for (String c : captions) {
			g2.drawString(c, 15, captionY);
			captionY += 15;
		}

	}

	//		while(true) {
	//			try {
	//				Thread.sleep(1000);
	//			} catch (InterruptedException ie) {
	//				throw new RuntimeException(ie);
	//			}
	//			Date now = new Date();
	//			if (lastDrawnAt == null || 
	//					(now.getTime() - lastDrawnAt.getTime() >= (secondsBetweenRedraw * 1000))
	//			) {
	//
	//
	//		}

	//	}

}


