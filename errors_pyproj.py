import pyproj
import numpy as np


R = 6378137
NM_CONVERSION = 1 / 1852


geod = pyproj.Geod(ellps='WGS84')
# geod = pyproj.Geod(sphere=True, a=R)


def is_positive(a10, a12):
	ref = (a10 - 90) % 360 # perpendicular to cyclone path
	a12 %= 360

	# if fc1 lies to the left of reference bearing then ate is positive, else negative
	return np.where(ref < 180, np.logical_or(a12 < ref, a12 > ref + 180), np.logical_and(a12 < ref, a12 > ref - 180))


def find_components(ob0_lat, ob0_lon, ob1_lat, ob1_lon, fc1_lat, fc1_lon):
	'''
	Returns direct positional error (DPE), cross-track error (CTE) and along-track error (ATE) in nautical miles.

	documentation for pyproj.Geod.inv: https://pyproj4.github.io/pyproj/stable/api/geod.html
	great circle formulae: https://www.movable-type.co.uk/scripts/latlong.html#:~:text=Cross%2Dtrack%20distance&text=the%20earth's%20radius-,JavaScript%3A,the%20relevant%20distance%20and%20bearings.
	'''
	# convert to numpy arrays
	ob0_lat, ob0_lon, ob1_lat, ob1_lon, fc1_lat, fc1_lon = [np.array(x) for x in (ob0_lat, ob0_lon, ob1_lat, ob1_lon, fc1_lat, fc1_lon)]
	
	a10, _, _ = geod.inv(ob1_lon, ob1_lat, ob0_lon, ob0_lat)
	a12, _, d12 = geod.inv(ob1_lon, ob1_lat, fc1_lon, fc1_lat)

	cte = np.arcsin(np.sin(d12 / R) * np.sin(np.radians(a12 - a10))) * R
	ate = np.arccos(np.cos(d12 / R) / np.cos(cte / R)) * R * np.where(is_positive(a10, a12), 1, -1) # (1 if is_positive(a10, a12) else -1)
	
	return d12 * NM_CONVERSION, cte * NM_CONVERSION, ate * NM_CONVERSION


if __name__ == '__main__':
	print(find_components(0, 1, 2, 3, 4, 5))

