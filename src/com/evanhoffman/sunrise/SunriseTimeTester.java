package com.evanhoffman.sunrise;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.evanhoffman.sunrise.SunPosition.SunEvent;

/**
 * 
 * @author evandhoffman@gmail.com
 *
 */
public class SunriseTimeTester {
	
	public static void main(String args[]) {
		System.out.println("¹ = "+Math.PI);
		MapCoordinate jfk = new MapCoordinate("JFK Airport",40.64465, -73.78296);
		// http://transition.fcc.gov/mb/audio/bickel/DDDMMSS-decimal.html
		// JFK Airport:
		// LAT: 		40¡ 38' 40.7394"
		// LONGITUDE: 	-73¡ 46' 58.656"

		System.out.println("Position: "+jfk);

		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		DateFormat df = new SimpleDateFormat("HH:mm:ss z");
		DateFormat dfDay = new SimpleDateFormat("E yyyy-MM-dd");

		for (int i = 1; i <= 365; i++) {
			cal.set(Calendar.DAY_OF_YEAR, i);
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				System.out.println("--Date--\t--Sunrise--\t--Sunset--");
			}
			
			Date sunrise = SunPosition.calculateSunEventTime(cal.getTime(), SunEvent.Sunrise, jfk);
			Date sunset = SunPosition.calculateSunEventTime(cal.getTime(), SunEvent.Sunset, jfk);

			
			System.out.println(dfDay.format(cal.getTime())+"\t"+df.format(sunrise)+"\t"+df.format(sunset));
		}
		
		
	}
}
