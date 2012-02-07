package com.evanhoffman.sunrise;

import java.awt.BasicStroke;
import java.awt.Button;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

/**
 * 
 * @author evandhoffman@gmail.com
 *
 */
public class Sunrise extends Frame implements ActionListener {

	private Button b;

	private Desktop desktop = null;

	void setDesktop(Desktop desktop) {
		this.desktop = desktop;
	}

	private Date lastDrawnAt = null;
	private int milliSecondsBetweenRedraw = 100;

	private NumberFormat nf = new DecimalFormat("###.000");
	private DateFormat df1 = new SimpleDateFormat("HH:mm z");
	private DateFormat df2 = new SimpleDateFormat("HH:mm:ss z");


	private int windowWidth=1024;
	private int windowHeight=500;
	
	private int sunDistance =  300;
	private int groundPosition = 300;

	private TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
	
	private static MapCoordinate location = new MapCoordinate("JFK Airport",40.64366, -73.78268);
	
	private Date now = new Date();

	public Sunrise(String title, int width, int height) {
		super(title);
		setTitle(title);

		this.windowHeight=height;
		this.windowWidth=width;
		createUI();
		b = new Button("Open Map");
//		add(b);
		b.addActionListener(this);
	}

	void createUI() {
		setSize(windowWidth,windowHeight);
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


		
		Properties p = loadProperties();
		if (p == null) {
			p = new Properties();
		}

		Sunrise f = new Sunrise("Sunrise $Date$ $Id$", 
				Integer.parseInt(p.getProperty("windowWidth","600")),
				Integer.parseInt(p.getProperty("windowHeight","1000")));
		
		f.groundPosition = Integer.parseInt(p.getProperty("groundPosition","500"));
		f.sunDistance = Integer.parseInt(p.getProperty("sunDistance","200"));
		
		
//		f.setSize(f.windowWidth,f.windowHeight);
		f.center();
		f.setVisible(true);

		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			f.setDesktop(Desktop.getDesktop());

		}
		


		while(true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ie) {
				throw new RuntimeException(ie);
			}
			Date now = new Date();
			if (f.lastDrawnAt == null || 
					(now.getTime() - f.lastDrawnAt.getTime() >= f.milliSecondsBetweenRedraw)
			) {

				f.repaint();
			}
		}


	}

	public void drawScene(Graphics g) {

	}

	public void paint(Graphics g) {
		super.paint(g);
		
		now = new Date();
		Graphics2D g2 = (Graphics2D)g;

		GradientPaint gp = null;

		// Draw the Sky.
		gp = new GradientPaint(0, 0, new Color(0,191,255), 0, groundPosition, new Color(135,206,250), false); // Brown gradient

		Rectangle2D sky = new Rectangle2D.Double(0, 0, windowWidth, groundPosition);
		g2.setPaint(gp);
		g2.fill(sky);


		// Draw the house.
		Rectangle2D houseBody = new Rectangle2D.Double((windowWidth/2)-50, groundPosition-100, 100, 100);
		g2.setPaint(Color.RED);
		g2.fill(houseBody);

		Polygon houseRoof = new Polygon();
		houseRoof.addPoint((windowWidth/2)-75, groundPosition-100);
		houseRoof.addPoint((windowWidth/2)+75, groundPosition-100);
		houseRoof.addPoint((windowWidth/2), groundPosition-141);
		g2.setPaint(Color.BLACK);
		g2.fill(houseRoof);

		// Calculate sun's position.

		int centerX = windowWidth/2;
		int centerY = groundPosition;

		double deg2rad = Math.PI / 180d;


		int sunX = 0; 
		int sunY = 0;

		Calendar cal = Calendar.getInstance();
		
		SunPosition sunPos = new SunPosition(cal.getTime(), location);

		Ellipse2D sun = null;

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

		g2.setFont(new Font("Arial", Font.PLAIN, 8));

		int sunDiameter = 20;
		
		boolean sunBehindYou = false;


		while(cal.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
			cal.add(Calendar.MINUTE, 30);

			sunPos.calculatePosition(cal.getTime());

			if (location.getLatitude() > 0) {
				//						System.out.println(sunPos);
				sunX = centerX + (int)(Math.sin((-1*sunPos.getAzimuth()) * deg2rad) * sunDistance) ; // -1 to face south. sin() because azimuth and unit circle don't face the same direction.
				sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
				sunBehindYou = Math.cos((-1*sunPos.getAzimuth()) * deg2rad) > 0;
			} else {
				sunX = centerX + (int)(Math.sin((sunPos.getAzimuth()) * deg2rad) * sunDistance) ;
				sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
				sunBehindYou = Math.cos((sunPos.getAzimuth()) * deg2rad) < 0;
			}			
			// Put the center of the sun (rather than the corner) on the appropriate point
			sun = new Ellipse2D.Double(sunX-(sunDiameter/2),sunY-(sunDiameter/2), sunDiameter, sunDiameter);
			g2.setPaint(sunBehindYou ? new Color(218,165,32) : Color.ORANGE);
			g2.setStroke(new BasicStroke(8));
			g2.fill(sun);
			g2.setPaint((Color.BLACK));
			g2.drawString(df1.format(cal.getTime())+ (sunBehindYou ? " (behind)" : ""), sun.getBounds().x -5, sun.getBounds().y - 5 );
		}


		// Draw the ground.
		gp = new GradientPaint(0, groundPosition, new Color(205,133,63,224), 0, windowHeight, new Color(222,184,135,224), false); // Brown gradient

		Rectangle2D ground = new Rectangle2D.Double(0, groundPosition, windowWidth, windowHeight-groundPosition);
		g2.setPaint(gp);
		g2.fill(ground);

		// "now" sun in front of ground.

		cal.setTime(now);
		sunPos.calculatePosition(cal.getTime());

		double sunDistanceMultiplier = 1.2;
		String facingDirection;
		if (location.getLatitude() > 0) {
			sunX = centerX + (int)(Math.sin((-1*sunPos.getAzimuth()) * deg2rad) * sunDistance * sunDistanceMultiplier) ; // -1 to face south. sin() because azimuth and unit circle don't face the same direction.			
			sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance * sunDistanceMultiplier);
			sunBehindYou = Math.cos((-1*sunPos.getAzimuth()) * deg2rad) > 0;
			//sunY = centerY;
			facingDirection = "SOUTH";
		} else {
			sunX = centerX + (int)(Math.sin((sunPos.getAzimuth()) * deg2rad) * sunDistance * sunDistanceMultiplier) ;
			sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance * sunDistanceMultiplier);
			facingDirection = "NORTH";
			sunBehindYou = Math.cos((sunPos.getAzimuth()) * deg2rad) < 0;
		}
		sunDiameter = 50;
		sun = new Ellipse2D.Double(sunX-(sunDiameter/2),sunY-(sunDiameter/2), 50, 50);
		g2.setPaint(Color.YELLOW);
		g2.setStroke(new BasicStroke(8));
		g2.fill(sun);
		g2.setPaint((Color.BLACK));
		g2.setFont(new Font("Arial", Font.PLAIN, 10));

		g2.drawString(df2.format(cal.getTime()) + (sunBehindYou ? "(behind)" : ""), sun.getBounds().x - 5, sun.getBounds().y - 5);

		lastDrawnAt = now;
		String captions[] = {"Location: "+location.getName()+", "+nf.format(location.getLatitude())+"¼, "+nf.format(location.getLongitude())+"¼",
				"Drawn at: "+lastDrawnAt,
				"Azimuth: "+nf.format(sunPos.getAzimuth())+"¼, elevation: "+nf.format(sunPos.getElevation())+"¼",
				"All times reported in time zone: "+timeZone.getID()+", "+timeZone.getDisplayName()+", UTC"+getTimezoneOffsetHours(cal),
				"Facing "+facingDirection};
		int captionY = 40;
		for (String c : captions) {
			g2.drawString(c, 15, captionY);
			captionY += 15;
		}

	}
	
	static String getTimezoneOffsetHours(Calendar cal) {
		int offsetHours = (((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (3600 * 1000)));
		if (offsetHours < 0) {
			return Integer.toString(offsetHours);
		}
		if (offsetHours > 0) {
			return "+"+Integer.toString(offsetHours);
		}
		return "";
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Only event is button click, so open the map.
		String mapUrl = String.format("http://maps.google.com/maps?q="+nf.format(location.getLatitude())+","+nf.format(location.getLongitude()));

		URI mapUri = null;
		try {
			mapUri = new URI(mapUrl);
		} catch (URISyntaxException ue) {
			throw new RuntimeException(ue);
		}

		if (desktop != null) {
			try {
				desktop.browse(mapUri);
				System.out.println("Just opened browser to URL "+mapUri);
			} catch (IOException ie) {
				throw new RuntimeException(ie);
			}
		}

	}

	static final String propertiesFile = "sunrise.conf";
	/**
	 * Allow config via properties file, format:
	 * 
	 * locationName=JFK Airport
	 * locationLatitude=40.64366
	 * locationLongitude=-73.78268
	 */
	static Properties loadProperties() {
		
		File f = new File(propertiesFile);
		if (!f.exists()) {
			System.out.println("Config file not found: "+f.getAbsolutePath());
			return null;
		} else {
			System.out.println("Loading config from file: "+f.getAbsolutePath());
		}
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
			Properties p = new Properties();
			p.load(is);
			System.out.println("Loaded properties: "+p);
			location.setName(p.getProperty("locationName"));
			location.setLatitude(Double.parseDouble(p.getProperty("locationLatitude")));
			location.setLongitude(Double.parseDouble(p.getProperty("locationLongitude")));
			
			System.out.println("Location set to: "+location);
			
			return p;
		}catch (FileNotFoundException fe) {
			throw new RuntimeException("File not found: "+f.getAbsolutePath(), fe);
		} catch (IOException ie) {
			throw new RuntimeException(ie);
		}
	}

}


