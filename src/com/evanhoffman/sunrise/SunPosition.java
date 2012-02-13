package com.evanhoffman.sunrise;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

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
 * @author evandhoffman@gmail.com
 *
 */
public class SunPosition {
	private double elevation = 0;
	private double azimuth = 0;
	private Date date = null;
	private double latitude = 0;
	private double longitude = 0;

	private Date sunset = null;
	private Date sunrise = null;

	static final double zenith = 90;	// Degrees - "official."  Other options: civil=96, nautical=102, astronomical=108

	/**
	 * See: <a href="http://williams.best.vwh.net/sunrise_sunset_algorithm.htm">http://williams.best.vwh.net/sunrise_sunset_algorithm.htm</a>
	 * 
<pre>
Source:
	Almanac for Computers, 1990
	published by Nautical Almanac Office
	United States Naval Observatory
	Washington, DC 20392

Inputs:
	day, month, year:      date of sunrise/sunset
	latitude, longitude:   location for sunrise/sunset
	zenith:                Sun's zenith for sunrise/sunset
	  offical      = 90 degrees 50'
	  civil        = 96 degrees
	  nautical     = 102 degrees
	  astronomical = 108 degrees

	NOTE: longitude is positive for East and negative for West
        NOTE: the algorithm assumes the use of a calculator with the
        trig functions in "degree" (rather than "radian") mode. Most
        programming languages assume radian arguments, requiring back
        and forth convertions. The factor is 180/pi. So, for instance,
        the equation RA = atan(0.91764 * tan(L)) would be coded as RA
        = (180/pi)*atan(0.91764 * tan((pi/180)*L)) to give a degree
        answer with a degree input for L.

</pre>
	 */
	static Date calculateSunEventTime(Date d, SunEvent sunEvent, MapCoordinate coordinate) {

		boolean sunNeverRises = false;
		boolean sunNeverSets = false;

		Calendar c = Calendar.getInstance();
		c.setTime(d);

		// 1
		int N = c.get(Calendar.DAY_OF_YEAR);

		// 2
		double lngHour = coordinate.getLongitude() / 15;

		double t = 0;

		switch (sunEvent) {
		case Sunrise:
			t = N + ((6 - lngHour) / 24);
			break;
		case Sunset:
			t =  N + ((18 - lngHour) / 24);
			break;
		default:
			throw new IllegalArgumentException("No support for SunEvent type: "+sunEvent);

		}

		// 3
		double M = (0.9856 * t) - 3.289;

		// 4 - Sun's true long
		double L = M + (1.916 * sin(M * DEG2RAD)) + (0.020 * sin(2 * M * DEG2RAD)) + 282.634;
		L = adjustInto360(L);

		// 5a. calculate the Sun's right ascension
		double RA = RAD2DEG * atan(0.91764 * tan(L * DEG2RAD));
		RA = adjustInto360(RA);

		// 5b. right ascension value needs to be in the same quadrant as L
		double Lquadrant = (Math.floor(L/90d))*90;
		double RAquadrant = (Math.floor(RA/90d)) * 90;
		RA = RA + (Lquadrant - RAquadrant);

		// 5c. right ascension value needs to be converted into hours
		RA = RA / 15;

		// 6. calculate the Sun's declination
		double sinDec = RAD2DEG * 0.39782 * sin(L * DEG2RAD);
		double cosDec = RAD2DEG * cos(asin(sinDec * DEG2RAD));

		// 7a. calculate the Sun's local hour angle
		double cosH = (cos(zenith * DEG2RAD) - (sinDec * sin(DEG2RAD * coordinate.getLatitude()))) / (cosDec * cos(coordinate.getLatitude() * DEG2RAD));
		if (cosH > 1) {
			// Sun never rises at this loc on this day.
			if (sunEvent == SunEvent.Sunrise) {
				return null;
			}
		}
		if (cosH < -1) {
			// Sun never sets at this loc on this day.
			if (sunEvent == SunEvent.Sunset) {
				return null;
			}
		}

		// 7b. finish calculating H and convert into hours
		double H;
		
		switch (sunEvent) {
		case Sunrise:
			H = 360 - (RAD2DEG * acos(cosH));
			break;
		case Sunset:
			H = (RAD2DEG * acos(cosH));
			break;
		default:
			throw new IllegalArgumentException("No support for SunEvent type: "+sunEvent);
		}
		
		H = H / 15;
		
		// 8. calculate local mean time of rising/setting
		double T = H + RA - (0.06571 * t) - 6.622;

		// 9. adjust back to UTC
		double UT = T - lngHour;
		UT = ((UT%24)+24)%24;
//		System.out.println(UT);
		
		// 10. convert UT value to local time zone of latitude/longitude
//		double localT = 
		
		setCalendarHours(c, UT);
		
		return c.getTime();
	}
	
	static void setCalendarHours(Calendar cal, double UTChours) {
		int wholeHours = (int)UTChours;
		double minutes = (UTChours - wholeHours) * 60;
		int wholeMinutes = (int)minutes;
		double seconds = (minutes - wholeMinutes) * 60;
		int wholeSeconds = (int)seconds;
		int milliSeconds = (int)(seconds - wholeSeconds) * 1000;
		
		int localHour = wholeHours + ((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (3600 * 1000));
		
		cal.set(Calendar.HOUR_OF_DAY, localHour);
		cal.set(Calendar.MINUTE, wholeMinutes);
		cal.set(Calendar.SECOND, wholeSeconds);
		cal.set(Calendar.MILLISECOND, milliSeconds);
	}

	/**
	 * Given any degree value, adjust into the range of [0,360).  -1¼ = 359¼.
	 * @param degrees
	 * @return
	 */
	static double adjustInto360(double degrees) {
		return ((degrees % 360d)+360d)%360d;
	}

	public enum SunEvent {
		Sunrise,
		Sunset
	}

	public SunPosition(Date d, MapCoordinate coord) {
		this(d, coord.getLatitude(), coord.getLongitude());
	}

	public SunPosition(Date d, double latitude, double longitude) {
		if (d == null) {
			throw new IllegalArgumentException();
		}
		this.date = d;
		setLatitude(latitude);
		setLongitude(longitude);
		calculatePosition();
	}

	private void setLatitude(double latitude) {
		this.latitude = (latitude % 360d);
	}

	private void setLongitude(double longitude) {
		this.longitude = (longitude % 360d);
	}

	/**
	 * Do the work here.
	 * http://stackoverflow.com/questions/8708048/position-of-the-sun-given-time-of-day-latitude-and-longitude/8764866#8764866
	 */

	static final double TWOPI = PI * 2d;

	/**
	 * Radians = degrees * DEG2RAD
	 */
	static final double DEG2RAD = PI / 180d;	// 
	/**
	 * Degrees = radians * RAD2DEG.
	 */
	static final double RAD2DEG = 180d / PI;	// 

	private void calculatePosition() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		// # Get day of the year, e.g. Feb 1 = 32, Mar 1 = 61 on leap years
		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		//		double hour = cal.get(Calendar.HOUR_OF_DAY);
		double hour = cal.get(Calendar.HOUR_OF_DAY) - ((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (3600 * 1000));
		double min = cal.get(Calendar.MINUTE);
		double second = cal.get(Calendar.SECOND);
		double millisecond = cal.get(Calendar.MILLISECOND);

		// # Get Julian date - 2400000

		hour = hour + (min / 60d) + ((second + (millisecond/1000d))/3600d);

		int year = cal.get(Calendar.YEAR);
		int delta = year - 1949;
		int leap = delta/4;

		// Modified Julian Date (MJD) from 00:00 November 17, 1858, Wednesday 
		// See http://en.wikipedia.org/wiki/Julian_day
		double julianDate = 32916.5 + (delta * 365) + leap + dayOfYear + (hour/24);
		//		System.out.println("Julian date: "+julianDate);

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
		double eclong = mnlong + (1.915 * sin(mnanom)) + (0.020 * sin(2 * mnanom));
		eclong = ((eclong % 360)+360)%360;
		double oblqec = 23.439 - (0.0000004 * time);
		eclong = eclong * DEG2RAD;
		oblqec = oblqec * DEG2RAD;

		// # Celestial coordinates
		// # Right ascension and declination		
		double numerator = cos(oblqec) * sin(eclong);
		double denominator = cos(eclong);
		double ra = atan(numerator/denominator);
		if (denominator < 0) {
			ra = ra + PI;
		}
		if (denominator >= 0 && numerator < 0) {
			ra += TWOPI;
		}
		double declination = asin(sin(oblqec) * sin(eclong));

		// Local coordinates
		// Greenwich mean sidereal time
		double gmst = 6.697375 + (.0657098242 * time) + hour;
		gmst = ((gmst%24)+24)%24;

		//# Local mean sidereal time
		double lmst = gmst + (longitude / 15);
		lmst = ((lmst % 24)+24)%24;
		lmst = lmst * 15 * DEG2RAD;

		// Hour angle. 
		double ha = lmst - ra;
		if (ha < (-1d * PI)) { 
			ha += TWOPI;
		} if (ha > PI) {
			ha -= TWOPI;
		}

		// Latitude to radians
		double latradians = latitude * DEG2RAD;

		// Azimuth and elevation
		double el = asin((sin(declination) * sin(latradians)) + (cos(declination) * cos(latradians) * cos(ha)));
		double az = asin((-1d * cos(declination) * sin(ha)) / cos(el));

		boolean cosAzPos = 0 <= (sin(declination) - (sin(el) * sin(latradians)));
		boolean sinAzNeg = sin(az) < 0;

		if (cosAzPos && sinAzNeg) {
			az += TWOPI;
		}
		if (!cosAzPos) {
			az = PI - az;
		}

		elevation = el / DEG2RAD;
		azimuth = az / DEG2RAD;

	}

	public void calculatePosition(Date d, double latitude, double longitude) {
		if (d == null) {
			throw new IllegalArgumentException();
		}
		this.date = d;
		setLatitude(latitude);
		setLongitude(longitude);

		calculatePosition();
	}

	public void calculatePosition(Date d) {
		if (d == null) {
			throw new IllegalArgumentException();
		}
		this.date = d;		
		calculatePosition();
	}

	public void calculatePosition(MapCoordinate coord) {
		calculatePosition(coord.getLatitude(), coord.getLongitude());
	}


	public void calculatePosition(double latitude, double longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{date="+getDate()+", latitude="+getLatitude()+", longitude="+getLongitude()+", elevation="+getElevation()+", azimuth="+getAzimuth()+"}";
	}
}
