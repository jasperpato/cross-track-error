import pyproj
import math
import numpy as np

R = 6378137

# geod = pyproj.Geod(ellps='WGS84')
geod = pyproj.Geod(sphere=True, a=R)

def find_components(ob0, ob1, fc1):
  '''
  Returns direct positional error (DPE), cross-track error (CTE) and along-track error (ATE) in nautical miles.

  documentation for pyproj.Geod.inv: https://pyproj4.github.io/pyproj/stable/api/geod.html
  great circle formulae: https://www.movable-type.co.uk/scripts/latlong.html#:~:text=Cross%2Dtrack%20distance&text=the%20earth's%20radius-,JavaScript%3A,the%20relevant%20distance%20and%20bearings.
  '''
  
  t10, _, _ = geod.inv(ob1[1], ob1[0], ob0[1], ob0[0])
  t12, _, d12 = geod.inv(ob1[1], ob1[0], fc1[1], fc1[0])

  cte = math.asin(math.sin(d12 / R) * math.sin(np.radians(t12 - t10))) * R
  ate = math.acos(math.cos(d12 / R) / math.cos(cte / R)) * R

  return d12 / 1852, cte / 1852, ate / 1852

if __name__ == '__main__':
  
  triplets = [
    ((-10.2, 94.4), (-10.4, 94.6), (-10.2651, 94.61872)), # CTE: 5.24 ATE: 6.29 (nautical miles)
    ((-10.2, 94.4), (-11, 94.9), (-10.70625,	94.625)), # CTE: 14.83 ATE: 18.85 (nautical miles)
  ]

  for t in triplets:
    dpe, cte, ate = find_components(*t)
    print(f'\nDPE {dpe}')
    print(f'CTE {cte}')
    print(f'ATE {ate}')

