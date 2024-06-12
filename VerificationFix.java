package au.gov.bom.aifs.tc.verification;

import java.util.Date;

import au.gov.bom.aifs.tc.common.Fix;
import au.gov.bom.aifs.tc.main.BearingAndDistance;

/**
 * Holds the data for one fix on a track.
 *
 * A note on interpolation between fixes: Fix attributes are
 * stored as doubles, as they are stored in CycloneData. However category is
 * an integer quantity, and values of i.e. 3.72 are not used. When we
 * interpolate between fixes we do generate fractional categories, which are
 * accessible through the getRawCategory() accessor. The getRoundedCategory()
 * returns a rounded version using Math.round(), so the following rounded
 * values should be expected:
 *
 *   3.4 -> 3
 *   3.5 -> 4
 *   4 -> 4
 *   4.4 -> 4
 *   4.5 -> 5
 */
public class VerificationFix
{
	private Double alongTrackVector;
	private Date time;
	private Double latitude;
	private Double longitude;
	private Double category;
	private Double pressureCentral;
	private Double windSpd;
	private Double windGust;

	/**
	 * Constructor.
	 *
	 * @param time time of fix
	 * @param latitude latitude of fix
	 * @param longitude longitude of fix
	 * @param pressure central pressure of fix
	 * @param meanWind mean wind for fix
	 * @param windGust wind gusts for fix
	 * @param category category for fix
	 */
	public VerificationFix(Date time,
		Double latitude,
		Double longitude,
		Double pressure,
		Double meanWind,
		Double windGust,
		Double category)
	{
		setTime(time);
		setLatitude(latitude);
		setLongitude(longitude);
		setPressure(pressure);
		setMeanWind(meanWind);
		setWindGust(windGust);
		setCategory(category);
	}

	/**
	 * Constructor.
	 */
	public VerificationFix()
	{
	}

	/**
	 * Copy constructor.
	 *
	 * @param old VerificationFix to copy
	 */
	public VerificationFix(VerificationFix old)
	{
		setTime(old.getTime());
		setLatitude(old.getLatitude());
		setLongitude(old.getLongitude());
		setPressure(old.getPressure());
		setMeanWind(old.getMeanWind());
		setWindGust(old.getWindGust());
		setCategory(old.getRawCategory());
		setAlongTrackVector(old.getAlongTrackVector());
	}

	/**
	 * Copy constructor from a TCCommon Fix.
	 *
	 * @param fix a TCCommon Fix
	 */
	public VerificationFix(Fix fix)
	{
		setTime(fix.getTime());
		setLatitude(fix.getLatitude());
		setLongitude(fix.getLongitude());
		setPressure(fix.getPressureCentral());
		setMeanWind(fix.getWindSpd());
		setWindGust(fix.getWindGust());
		setCategory(fix.getCategory());
		// along track vector is not set as there is no equivalent
		// in Fix
	}

	@Override
	public String toString()
	{
		Double degreeVal = null;

		if (alongTrackVector != null) {
			degreeVal = Math.toDegrees(alongTrackVector);
		}

		return String.format(
			"Time: %tF %tT Lat: %5.2f Lon: %5.2f Press: %6.2f " +
			"Mean wind: %6.2f Gust: %6.2f Category: %5.2f " +
			"AlongTrackVector: %6.2f (%6.2f)",
			time,
			time,
			latitude,
			longitude,
			pressureCentral,
			windSpd,
			windGust,
			category,
			alongTrackVector,
			degreeVal
		);
	}

	/**
	 * Calculate the angle in radians of a line from
	 * this fix to another fix.
	 *
	 * 0 = east, angle increases counterclockwise
	 * 0 -> 2.PI range
	 *
	 * @param anotherFix the other fix
	 * @return the angle to the other fix
	 */
	public double angleToFix(VerificationFix anotherFix)
	{
		double xDist = VerificationUtils.differenceBetweenLongitudes(
			this.getLongitude(), anotherFix.getLongitude());
		double yDist = anotherFix.getLatitude() - this.getLatitude();
		double angle = Math.atan2(yDist, xDist);
		if (yDist < 0) {
			angle += 2 * Math.PI;
		}

		return angle;
	}

	/**
	 * Returns a copy of this fix, displaced by (lon,lat) degrees.
	 *
	 * New longitude values are in the range [180..180]
	 *
	 * @param lon longitude displacement
	 * @param lat latitude displacement
	 * @return a copy of this fix, displaced
	 */
	public VerificationFix displacedBy(double lon, double lat)
	{
		VerificationFix displaced = new VerificationFix(this);

		displaced.setLongitude(
			BearingAndDistance.range180(getLongitude() + lon));
		displaced.setLatitude(getLatitude() + lat);

		return displaced;
	}

	/**
	 * Return a new fix which is a 'blend' of this fix and another.
	 * If amount is 0, the new fix is equal to this fix.
	 * If amount is 1, the new fix is equal to 'otherFix'
	 * Otherwise - a linear blend of the two fixes.
	 *
	 * When an attribute of either fix is null, then the corresponding
	 * attribute of the interpolated fix is null.
	 *
	 * @param otherFix the other fix we're blending with this fix
	 * @param amount what proportion of the 'other' fix to blend with
	 * this fix.
	 * @return an interpolated fix
	 * @throws VerificationException if fix is null or amount is impossible
	 * value
	 */
	public VerificationFix interpolateFix(
			VerificationFix otherFix,
			double amount)
		throws VerificationException
	{
		if (otherFix == null) {
			throw new VerificationException(
				"Trying to interpolate NULL VerificationFix");
		}

		if (amount < 0 || amount > 1) {
			throw new VerificationException(
				"Trying to interpolateFix by amount " +
				"outside [0..1]: " + amount);
		}

		VerificationFix interpolatedFix = new VerificationFix();

		// date convert to long before interpolating
		interpolatedFix.setTime(
			interpolateDate(getTime(),
				otherFix.getTime(),
				amount));
		// all other values are doubles..
		interpolatedFix.setLatitude(interpolateDouble(
			getLatitude(),
			otherFix.getLatitude(),
			amount));
		interpolatedFix.setLongitude(
			interpolateLongitude(getLongitude(),
				otherFix.getLongitude(),
				amount));
		interpolatedFix.setPressure(interpolateDouble(
			getPressure(),
			otherFix.getPressure(),
			amount));
		interpolatedFix.setMeanWind(interpolateDouble(
			getMeanWind(),
			otherFix.getMeanWind(),
			amount));
		interpolatedFix.setWindGust(interpolateDouble(
			getWindGust(),
			otherFix.getWindGust(),
			amount));
		interpolatedFix.setCategory(interpolateDouble(
			getRawCategory(),
			otherFix.getRawCategory(),
			amount));
		interpolatedFix.setAlongTrackVector(interpolateAngle(
			getAlongTrackVector(),
			otherFix.getAlongTrackVector(),
			amount));

		return interpolatedFix;
	}

	/**
	 * Returns a new Date which is 'amount' of the way between
	 * startDate and endDate.
	 * If either is null, the result is null.
	 *
	 * @param startDate the date we're interpolating from
	 * @param endDate the date we're interpolating towards
	 * @param amount to blend between the two dates - [0->1].
	 * 0 == all startDate.
	 * @return interpolated date
	 */
	private Date interpolateDate(Date startDate, Date endDate,
		double amount)
	{
		if (startDate == null || endDate == null) {
			// can't proceed with nothing to interpolate between
			return null;
		}

		double doubleTime =
			startDate.getTime() + amount * (endDate.getTime() -
					startDate.getTime());
		Date newDate = new Date((long) doubleTime);

		return newDate;
	}

	/**
	 * Returns a new Double which is 'amount' of the way between startDouble
	 * and endDouble. If either is null, the result is null.
	 *
	 * @param startDouble the initial number we're interpolating from
	 * @param endDouble the number we're interpolating towards
	 * @param amount to blend between the two doubles
	 * - [0->1]. 0 == all startDouble.
	 * @return interpolated double
	 */
	private Double interpolateDouble(Double startDouble, Double endDouble,
		double amount)
	{
		if (startDouble == null || endDouble == null) {
			// can't proceed
			return null;
		}

		return startDouble + amount * (endDouble - startDouble);
	}

	/**
	 * Returns a new Double which is 'amount' of the way between
	 * startLongitude and endLongitude.
	 * If either is null, the result is null.
	 *
	 * The new longitude is in the range [-180..180]
	 *
	 * @param startLongitude the initial longitude we're interpolating from
	 * @param endLongitude the longitude we're interpolating towards
	 * @param amount to blend between the two longitudes - [0->1].
	 * 0 == all startLongitude.
	 * @return interpolated double
	 */
	private Double interpolateLongitude(Double startLongitude,
		Double endLongitude, double amount)
	{
		if (startLongitude == null || endLongitude == null) {
			// Can't proceed
			return null;
		}

		double longitudeDifference =
			VerificationUtils.differenceBetweenLongitudes(
					startLongitude,
					endLongitude);
		double interpolatedLongitude =
			BearingAndDistance.range180(getLongitude() +
					amount * longitudeDifference);

		return interpolatedLongitude;
	}

	/**
	 * Interpolate between two directions.
	 *
	 * This is a bit fiddly as there are always two ways of getting from one
	 * angle to another - the short way and the long way (i.e. - from east
	 * to north is either +ve90 degrees or -ve270 degrees).
	 *
	 * In the insoluble case of a 180 degree angle, we take the clockwise
	 * path.
	 *
	 * We will consistently interpret the trip between the two as being the
	 * smaller of the two possible angles, and go "amount" along that angle.
	 *
	 *
	 * @param startAngle in radians
	 * @param endAngle in radians
	 * @param amount how far to interpolate [0->1]
	 * @return the interpolated angle
	 */
	public Double interpolateAngle(Double startAngle, Double endAngle,
		double amount)
	{
		if (startAngle == null | endAngle == null) {
			// Can't proceed
			return null;
		}

		Double angleBetween = normaliseAngle(
			normaliseAngle(endAngle) - normaliseAngle(startAngle));
		Double altAngleBetween = angleBetween - 2 * Math.PI;
		if (Math.abs(altAngleBetween) < Math.abs(angleBetween)) {
			angleBetween = altAngleBetween;
		}

		return normaliseAngle(startAngle + amount * angleBetween);
	}

	/**
	 * Force an angle (in radians) into the range.
	 * [0->2PI)
	 *
	 * @param angle input
	 * @return normalised angle
	 */
	private static double normaliseAngle(double angle)
	{
		double normalised = angle;

		while ((normalised < 0) || (normalised >= 2 * Math.PI)) {
			if (normalised < 0) {
				normalised += 2 * Math.PI;
			}
			if (normalised >= 2 * Math.PI) {
				normalised -= 2 * Math.PI;
			}
		}

		return normalised;
	}

	/**
	 * Returns how far away anotherFix is along the alongTrackVector
	 * of this fix (disregarding any across track component of the
	 * distance).
	 *
	 * If this fix has no alongTrackVector, this throws
	 * a VerificationException
	 *
	 * @param anotherFix the fix we are calculating the along track
	 * distance to
	 * @return the distance calculated in km
	 * @throws VerificationException if this fix has no along track vector
	 */
	public double alongTrackDistanceTo(VerificationFix anotherFix)
		throws VerificationException
	{
		if (getAlongTrackVector() == null) {
			throw new VerificationException(
				"Calling alongTrackDistanceTo on a " +
				"fix which has no alongTrackvector");
		}
		// get the vector we want to split into components
		// [longitudeKilometers, latitudeKilometers]
		double longitudeNM =
			VerificationUtils.
			calculateBetweenFixLongitudeNauticalMiles(
				this.getLongitude(),
				this.getLatitude(),
				anotherFix.getLongitude(),
				anotherFix.getLatitude());
		double latitudeNM =
			VerificationUtils.
				calculateBetweenFixLatitudeNauticalMiles(
				this.getLatitude(),
				anotherFix.getLatitude());

		double alongTrackCos = Math.cos(getAlongTrackVector());
		double alongTrackSin = Math.sin(getAlongTrackVector());

		double alongTrackLongitudeKm =
			longitudeNM * alongTrackCos +
			latitudeNM * alongTrackSin;

		return alongTrackLongitudeKm;
	}

	/**
	 * Returns how far away anotherFix is across the alongTrackVector
	 * of this fix (disregarding any along track component of the
	 * distance).
	 *
	 * If this fix has no alongTrackVector, this throws
	 * a VerificationException.
	 *
	 * I am arbitrarily assuming that if the along track vector points
	 * due north (90 degrees)
	 * then the across track vector points due east (0 degrees)
	 *
	 * @param anotherFix the fix we are calculating the across track
	 * distance to
	 * @return the distance calculated in km
	 * @throws VerificationException if this fix has no along track vector
	 */
	public double acrossTrackDistanceTo(VerificationFix anotherFix)
		throws VerificationException
	{
		if (getAlongTrackVector() == null) {
			throw new VerificationException(
				"Calling acrossTrackDistanceTo on " +
				"a fix which has no alongTrackvector");
		}

		// get the vector we want to split into components
		// [longitudeKilometers, latitudeKilometers]
		double longitudeKilometers =
			VerificationUtils.
			calculateBetweenFixLongitudeNauticalMiles(
				this.getLongitude(),
				this.getLatitude(),
				anotherFix.getLongitude(),
				anotherFix.getLatitude());
		double latitudeKilometers =
			VerificationUtils.
			calculateBetweenFixLatitudeNauticalMiles(
				this.getLatitude(),
				anotherFix.getLatitude());
		double acrossAngle =
			getAlongTrackVector() - 90 / (360 / (2 * Math.PI));

		double acrossTrackCos = Math.cos(acrossAngle);
		double acrossTrackSin = Math.sin(acrossAngle);

		double acrossTrackLongitudeKm =
			longitudeKilometers * acrossTrackCos +
			latitudeKilometers * acrossTrackSin;

		return acrossTrackLongitudeKm;
	}

	// setters and getters

	/**
	 * Getter for along track vector.
	 *
	 * @return the along track vector
	 */
	public Double getAlongTrackVector()
	{
		return alongTrackVector;
	}

	/**
	 * Setter for along track vector.
	 *
	 * @param alongTrackVector the new along track vector
	 */
	public void setAlongTrackVector(Double alongTrackVector)
	{
		this.alongTrackVector = alongTrackVector;
	}

	/**
	 * Getter for fix pressure - masks name of underlying variable
	 * in Fix class.
	 *
	 * @return the fix pressure
	 */
	public Double getPressure()
	{
		return pressureCentral;
	}

	/**
	 * Setter for pressure - masks the name of the underlying
	 * variable in Fix.
	 *
	 * @param pressure the new value for pressure
	 */
	public void setPressure(Double pressure)
	{
		this.pressureCentral = pressure;
	}

	/**
	 * Getter for mean wind speed - masks the underlying variable
	 * name in Fix.
	 *
	 * @return mean wind speed
	 */
	public Double getMeanWind()
	{
		return windSpd;
	}

	/**
	 * Setter for mean wind - masks the value of the underlying
	 * variable name in Fix.
	 *
	 * @param meanWind the mean wind
	 */
	public void setMeanWind(Double meanWind)
	{
		this.windSpd = meanWind;
	}

	//
	// Category is held as a double, but is an integer concept
	// To get rounded integer values for category, use
	// getRoundedCategory()
	//

	/**
	 * Get category as a raw double - it may have a non integer value
	 * if this is an interpolated fix.
	 *
	 * @return the raw category
	 */
	public Double getRawCategory()
	{
		return category;
	}

	/**
	 * Get category as an integer.
	 *
	 * @return integer category
	 */
	public Integer getRoundedCategory()
	{
		return Long.valueOf(Math.round(getRawCategory())).intValue();
	}

	/**
	 * Getter for category.
	 *
	 * @return our category
	 */
	public Double getCategory()
	{
		return category;
	}

	/**
	 * Setter for category.
	 *
	 * @param category new category
	 */
	public void setCategory(Double category)
	{
		this.category = category;
	}

	/**
	 * Getter for latitude.
	 *
	 * @return our latitude
	 */
	public Double getLatitude()
	{
		return latitude;
	}

	/**
	 * Setter for latitude.
	 *
	 * @param latitude new latitude
	 */
	public void setLatitude(Double latitude)
	{
		this.latitude = latitude;
	}

	/**
	 * Getter for longitude.
	 *
	 * @return our longitude
	 */
	public Double getLongitude()
	{
		return longitude;
	}

	/**
	 * Setter for longitude.
	 *
	 * @param longitude new longitude
	 */
	public void setLongitude(Double longitude)
	{
		this.longitude = longitude;
	}

	/**
	 * Getter for time.
	 *
	 * @return our time
	 */
	public Date getTime()
	{
		return time == null ? null : new Date(time.getTime());
	}

	/**
	 * Setter for time.
	 *
	 * @param time new time
	 */
	public void setTime(Date time)
	{
		this.time = time == null ? null : new Date(time.getTime());
	}

	/**
	 * Getter for windGust.
	 *
	 * @return our windGust
	 */
	public Double getWindGust()
	{
		return windGust;
	}

	/**
	 * Setter for windGust.
	 *
	 * @param windGust new windGust
	 */
	public void setWindGust(Double windGust)
	{
		this.windGust = windGust;
	}
}
