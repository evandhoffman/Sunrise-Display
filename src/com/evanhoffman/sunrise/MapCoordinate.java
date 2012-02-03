package com.evanhoffman.sunrise;

/**
 * 
 * @author evandhoffman@gmail.com
 *
 */
public class MapCoordinate {
	private double latitude = 0;
	private double longitude = 0;
	private double elevationMeters = 0;
	private String name = null;
	
	public MapCoordinate(String name, double lat, double lon) {
		this.name = name;
		latitude = lat;
		longitude = lon;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getElevationMeters() {
		return elevationMeters;
	}
	public void setElevationMeters(double elevationMeters) {
		this.elevationMeters = elevationMeters;
	}
	
	@Override
	public String toString() {
		return "{name="+name+", latitude="+latitude+", longitude="+longitude+", elevationMeters="+elevationMeters+"}";
	}
}
