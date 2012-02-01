package com.evanhoffman.sunrise;

import java.util.Calendar;
import java.util.Date;

/**
 * Blatantly stolen from http://stackoverflow.com/questions/257717/position-of-the-sun-given-time-of-day-and-lat-long and
 * http://stackoverflow.com/questions/8708048/position-of-the-sun-given-time-of-day-latitude-and-longitude/8764866#8764866
 * 
<pre>
sunPosition <- function(year, month, day, hour=12, min=0, sec=0,
                    lat=46.5, long=6.5) {

    twopi <- 2 * pi
    deg2rad <- pi / 180

    # Get day of the year, e.g. Feb 1 = 32, Mar 1 = 61 on leap years
    month.days <- c(0,31,28,31,30,31,30,31,31,30,31,30)
    day <- day + cumsum(month.days)[month]
    leapdays <- year %% 4 == 0 & (year %% 400 == 0 | year %% 100 != 0) & 
                day >= 60 & !(month==2 & day==60)
    day[leapdays] <- day[leapdays] + 1

    # Get Julian date - 2400000
    hour <- hour + min / 60 + sec / 3600 # hour plus fraction
    delta <- year - 1949
    leap <- trunc(delta / 4) # former leapyears
    jd <- 32916.5 + delta * 365 + leap + day + hour / 24

    # The input to the Atronomer's almanach is the difference between
    # the Julian date and JD 2451545.0 (noon, 1 January 2000)
    time <- jd - 51545.

    # Ecliptic coordinates

    # Mean longitude
    mnlong <- 280.460 + .9856474 * time
    mnlong <- mnlong %% 360
    mnlong[mnlong < 0] <- mnlong[mnlong < 0] + 360

    # Mean anomaly
    mnanom <- 357.528 + .9856003 * time
    mnanom <- mnanom %% 360
    mnanom[mnanom < 0] <- mnanom[mnanom < 0] + 360
    mnanom <- mnanom * deg2rad

    # Ecliptic longitude and obliquity of ecliptic
    eclong <- mnlong + 1.915 * sin(mnanom) + 0.020 * sin(2 * mnanom)
    eclong <- eclong %% 360
    eclong[eclong < 0] <- eclong[eclong < 0] + 360
    oblqec <- 23.439 - 0.0000004 * time
    eclong <- eclong * deg2rad
    oblqec <- oblqec * deg2rad

    # Celestial coordinates
    # Right ascension and declination
    num <- cos(oblqec) * sin(eclong)
    den <- cos(eclong)
    ra <- atan(num / den)
    ra[den < 0] <- ra[den < 0] + pi
    ra[den >= 0 & num < 0] <- ra[den >= 0 & num < 0] + twopi
    dec <- asin(sin(oblqec) * sin(eclong))

    # Local coordinates
    # Greenwich mean sidereal time
    gmst <- 6.697375 + .0657098242 * time + hour
    gmst <- gmst %% 24
    gmst[gmst < 0] <- gmst[gmst < 0] + 24.

    # Local mean sidereal time
    lmst <- gmst + long / 15.
    lmst <- lmst %% 24.
    lmst[lmst < 0] <- lmst[lmst < 0] + 24.
    lmst <- lmst * 15. * deg2rad

    # Hour angle
    ha <- lmst - ra
    ha[ha < -pi] <- ha[ha < -pi] + twopi
    ha[ha > pi] <- ha[ha > pi] - twopi

    # Latitude to radians
    lat <- lat * deg2rad

    # Azimuth and elevation
    el <- asin(sin(dec) * sin(lat) + cos(dec) * cos(lat) * cos(ha))
    az <- asin(-cos(dec) * sin(ha) / cos(el))

    # For logic and names, see Spencer, J.W. 1989. Solar Energy. 42(4):353
    cosAzPos <- (0 <= sin(dec) - sin(el) * sin(lat))
    sinAzNeg <- (sin(az) < 0)
    az[cosAzPos & sinAzNeg] <- az[cosAzPos & sinAzNeg] + twopi
    az[!cosAzPos] <- pi - az[!cosAzPos]

    # if (0 < sin(dec) - sin(el) * sin(lat)) {
    #     if(sin(az) < 0) az <- az + twopi
    # } else {
    #     az <- pi - az
    # }


    el <- el / deg2rad
    az <- az / deg2rad
    lat <- lat / deg2rad

    return(list(elevation=el, azimuth=az))
}
</pre>
 * @author evan
 *
 */
public class SunPosition {
	private double elevation = 0;
	private double azimuth = 0;
	private Date date = null;
	private double latitude = 0;
	private double longitude = 0;
	
	public SunPosition(Date d, double latitude, double longitude) {
		if (d == null || latitude > 360 || longitude > 360) {
			throw new IllegalArgumentException();
		}
		this.date = d;
		this.latitude = latitude;
		this.longitude = longitude;
		calculatePosition();
	}
	
	/**
	 * Do the work here.
	 * http://stackoverflow.com/questions/8708048/position-of-the-sun-given-time-of-day-latitude-and-longitude/8764866#8764866
	 */
	
	static final double TWOPI = Math.PI * 2d;
	static final double DEG2RAD = Math.PI / 180d;
	
	private void calculatePosition() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
	    // # Get day of the year, e.g. Feb 1 = 32, Mar 1 = 61 on leap years
		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		double hour = cal.get(Calendar.HOUR_OF_DAY);
		double min = cal.get(Calendar.MINUTE);
		double second = cal.get(Calendar.SECOND);
		double millisecond = cal.get(Calendar.MILLISECOND);
		
	    // # Get Julian date - 2400000
		
		hour = hour + (min / 60d) + ((second + (millisecond/1000d))/3600d);
		
		int year = cal.get(Calendar.YEAR);
		int delta = year - 1949;
		int leap = delta/4;
		
		double julianDate = 32916.5 + (delta * 365) + leap + dayOfYear + hour;
		
	    // # The input to the Atronomer's almanach is the difference between
	    // # the Julian date and JD 2451545.0 (noon, 1 January 2000)

		double time = julianDate - 51545;
		
	    // # Ecliptic coordinates

	    // # Mean longitude
		double mnlong = 280.460 + (.9856474 * time);
		mnlong = ((mnlong % 360)+360)%360; // I THINK this is what "mnanom[mnanom < 0] <- mnanom[mnanom < 0] + 360" means?
		
		// Mean anomaly
		double mnanom = 357.528 + (.9856003 * time);
		mnanom = ((mnanom % 360)+360)%360;
		mnanom = mnanom * DEG2RAD;
		
		// Ecliptic longitude and obliquity of ecliptic
		double eclong = mnlong + (1.915 * Math.sin(mnanom)) + (0.020 * Math.sin(2 * mnanom));
		eclong = ((eclong % 360)+360)%360;
		double oblqec = 23.439 - (0.0000004 * time);
		eclong = eclong * DEG2RAD;
		oblqec = oblqec * DEG2RAD;
		
	    // # Celestial coordinates
	    // # Right ascension and declination		
		double numerator = Math.cos(oblqec) * Math.sin(eclong);
		double denominator = Math.cos(eclong);
		double ra = Math.atan(numerator/denominator);
		if (denominator < 0) {
			ra = ra + Math.PI;
		}
		if (denominator >= 0 && numerator < 0) {
			ra += TWOPI;
		}
		double declination = Math.asin(Math.sin(oblqec) * Math.sin(eclong));
		
		// Local coordinates
		// Greenwich mean sidereal time
		double gmst = 6.697375 + (.0657098242 * time) + hour;
		gmst = ((gmst%24)+24)%24;
		
	    //# Local mean sidereal time
		double lmst = gmst + (longitude / 15);
		lmst = ((lmst % 24)+24)%24;
	    lmst = lmst * 15 * DEG2RAD;
		
	    // Hour angle.  Must be between � and -� I guess?
	    double ha = lmst - ra;
	    if (ha < (-1d * Math.PI)) { 
	    	ha += TWOPI;
	    } if (ha > Math.PI) {
	    	ha -= TWOPI;
	    }
	    
	    // Latitude to radians
	    double latradians = latitude * DEG2RAD;
		
	    // Azimuth and elevation
	    double el = Math.asin((Math.sin(declination) * Math.sin(latradians)) + (Math.cos(declination) * Math.cos(latradians) * Math.cos(ha)));
	    double az = Math.asin((-1d * Math.cos(declination) * Math.sin(ha)) / Math.cos(el));
	    
	    boolean cosAzPos = ((Math.sin(declination) - (Math.sin(el) * Math.sin(latradians))) >= 0);
	    boolean sinAzNeg = Math.sin(az) < 0;
	    
	    if (cosAzPos && sinAzNeg) {
	    	az += TWOPI;
	    }
	    if (!cosAzPos) {
	    	az = Math.PI - az;
	    }
	    
	    elevation = el / DEG2RAD;
	    azimuth = az / DEG2RAD;
	    
	}
	
	public void calculatePosition(Date d, double latitude, double longitude) {
		if (d == null || latitude > 360 || longitude > 360) {
			throw new IllegalArgumentException();
		}
		this.date = d;
		this.latitude = latitude;
		this.longitude = longitude;
		calculatePosition();
	}
	
	public void calculatePosition(Date d) {
		if (d == null) {
			throw new IllegalArgumentException();
		}
		this.date = d;		
		calculatePosition();
	}
	
	public void calculatePosition(double latitude, double longitude) {
		if (latitude > 360 || longitude > 360) {
			throw new IllegalArgumentException();
		}
		this.latitude = latitude;
		this.longitude = longitude;
		calculatePosition();		
	}
	
	public double getElevation() {
		return elevation;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public Date getDate() {
		return date;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	
}
