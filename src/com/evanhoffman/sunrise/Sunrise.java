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

public class Sunrise extends Frame {

	static final int WIDTH=1000;
	static final int HEIGHT=600;
	

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
			public void winowClosing (WindowEvent e) {
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

	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		// Draw the ground.
		Rectangle2D ground = new Rectangle2D.Double(0, HEIGHT-100, WIDTH, 100);
		GradientPaint gp = new GradientPaint(0, HEIGHT-100, new Color(205,133,63), 0, HEIGHT, new Color(222,184,135), false); // Brown gradient
		g2.setPaint(gp);
		g2.fill(ground);
		
		// Draw the house.
		Rectangle2D houseBody = new Rectangle2D.Double((WIDTH/2)-50, HEIGHT-200, 100, 100);
		g2.setPaint(Color.RED);
		g2.fill(houseBody);
		
		Polygon houseRoof = new Polygon();
		houseRoof.addPoint((WIDTH/2)-75, HEIGHT-200);
		houseRoof.addPoint((WIDTH/2)+75, HEIGHT-200);
		houseRoof.addPoint((WIDTH/2), HEIGHT-300);
		g2.fill(houseRoof);
		
		Ellipse2D sun = new Ellipse2D.Double(0,0, 150, 150);
		g2.setPaint(Color.YELLOW);
		g2.setStroke(new BasicStroke(8));
		g2.fill(sun);
		
		double x = 15, y = 50, w = 70, h = 70;
		Ellipse2D e = new Ellipse2D.Double(x, y, w, h);
		gp = new GradientPaint(75, 75, Color.white,
				95, 95, Color.gray, true);
		// Fill with a gradient.
		g2.setPaint(gp);
		g2.fill(e);
		// Stroke with a solid color.
		e.setFrame(x + 100, y, w, h);
		g2.setPaint(Color.black);
		g2.setStroke(new BasicStroke(8));
		g2.draw(e);
		// Stroke with a gradient.
		e.setFrame(x + 200, y, w, h);
		g2.setPaint(gp);
		g2.draw(e);
	}
	
}


