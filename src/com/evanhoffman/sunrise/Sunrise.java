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

	NumberFormat nf = new DecimalFormat("###.000");
	DateFormat df1 = new SimpleDateFormat("HH:mm z");
	DateFormat df2 = new SimpleDateFormat("HH:mm:ss z");


	static final int WIDTH=1024;
	static final int HEIGHT=550;
	static final MapCoordinate location = new MapCoordinate("JFK Airport",40.64366, -73.78268);

	public Sunrise() { 
		this("Sunrise v1.0"); 

	}

	public Sunrise(String title) {
		super(title);
		createUI();
		b = new Button("Open Map");
//		add(b);
		b.addActionListener(this);
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

		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			f.setDesktop(Desktop.getDesktop());

		}
		
		loadProperties();


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
		g2.setPaint(Color.BLACK);
		g2.fill(houseRoof);

		// Calculate sun's position.

		int centerX = WIDTH/2;
		int centerY = HEIGHT-100;

		double deg2rad = Math.PI / 180d;


		int sunX = 0; 
		int sunY = 0;

		Calendar cal = Calendar.getInstance();
		SunPosition sunPos = new SunPosition(cal.getTime(), location);
		int sunDistance =  (int)(WIDTH * 0.5) - 50;

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
			g2.setPaint(Color.ORANGE);
			g2.setStroke(new BasicStroke(8));
			g2.fill(sun);
			g2.setPaint((Color.BLACK));
			g2.drawString(df1.format(cal.getTime())+ (sunBehindYou ? " (behind)" : ""), sun.getBounds().x -5, sun.getBounds().y - 5 );
		}


		// Draw the ground.
		gp = new GradientPaint(0, HEIGHT-100, new Color(205,133,63,224), 0, HEIGHT, new Color(222,184,135,224), false); // Brown gradient

		Rectangle2D ground = new Rectangle2D.Double(0, HEIGHT-100, WIDTH, 100);
		g2.setPaint(gp);
		g2.fill(ground);

		// "now" sun in front of ground.

		cal.setTime(new Date());
		sunPos.calculatePosition(cal.getTime());

		sunDistance *= 1.2;
		String facingDirection;
		if (location.getLatitude() > 0) {
			sunX = centerX + (int)(Math.sin((-1*sunPos.getAzimuth()) * deg2rad) * sunDistance) ; // -1 to face south. sin() because azimuth and unit circle don't face the same direction.			
			sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
			sunBehindYou = Math.cos((-1*sunPos.getAzimuth()) * deg2rad) > 0;
			//sunY = centerY;
			facingDirection = "SOUTH";
		} else {
			sunX = centerX + (int)(Math.sin((sunPos.getAzimuth()) * deg2rad) * sunDistance) ;
			sunY = centerY - (int)(Math.sin(sunPos.getElevation() * deg2rad) * sunDistance);
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

		lastDrawnAt = new Date();
		String captions[] = {"Location: "+location.getName()+", "+nf.format(location.getLatitude())+"¼, "+nf.format(location.getLongitude())+"¼",
				"Drawn at: "+lastDrawnAt,
				"Azimuth: "+nf.format(sunPos.getAzimuth())+"¼, elevation: "+nf.format(sunPos.getElevation())+"¼",
				"Facing "+facingDirection};
		int captionY = 40;
		for (String c : captions) {
			g2.drawString(c, 15, captionY);
			captionY += 15;
		}

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
	static void loadProperties() {
		
		File f = new File(propertiesFile);
		if (!f.exists()) {
			System.out.println("Config file not found: "+f.getAbsolutePath());
			return;
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
			
		}catch (FileNotFoundException fe) {
			throw new RuntimeException("File not found: "+f.getAbsolutePath(), fe);
		} catch (IOException ie) {
			throw new RuntimeException(ie);
		}
	}

}


