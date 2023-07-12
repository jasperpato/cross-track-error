import pyproj
import math
import numpy as np

R = 6378137
NM_CONV = 1 / 1852

# geod = pyproj.Geod(ellps='WGS84')
geod = pyproj.Geod(sphere=True, a=R)

def is_positive(a10, a12):
  ref = (a10 - 90) % 360 # perpendicular to cyclone path
  a12 = a12 % 360

  # if fc1 lies to the left of reference bearing then ate is positive, else negative
  if ref < 180:
    return a12 < ref or a12 > ref + 180
  
  return a12 < ref and a12 > ref - 180

def find_components(ob0, ob1, fc1):
  '''
  Returns direct positional error (DPE), cross-track error (CTE) and along-track error (ATE) in nautical miles.

  documentation for pyproj.Geod.inv: https://pyproj4.github.io/pyproj/stable/api/geod.html
  great circle formulae: https://www.movable-type.co.uk/scripts/latlong.html#:~:text=Cross%2Dtrack%20distance&text=the%20earth's%20radius-,JavaScript%3A,the%20relevant%20distance%20and%20bearings.
  '''
  
  a10, _, _ = geod.inv(ob1[1], ob1[0], ob0[1], ob0[0])
  a12, _, d12 = geod.inv(ob1[1], ob1[0], fc1[1], fc1[0])

  cte = math.asin(math.sin(d12 / R) * math.sin(np.radians(a12 - a10))) * R
  ate = math.acos(math.cos(d12 / R) / math.cos(cte / R)) * R * (1 if is_positive(a10, a12) else -1)
  
  return d12 * NM_CONV, cte * NM_CONV, ate * NM_CONV

