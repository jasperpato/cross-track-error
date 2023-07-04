import nvector as nv

R = 6378137

frame = nv.FrameE(a=R, f=0)

triplets = [
  ((-10.2, 94.4), (-10.4, 94.6), (-10.2651, 94.61872)), # CTE: 5.24 ATE: 6.29 (nautical miles)
  ((-10.2, 94.4), (-11, 94.9), (-10.70625,	94.625)), # CTE: 14.83 ATE: 18.85 (nautical miles)
]

def find_components(ob0, ob1, fc1):
  '''
  nvector docs: https://nvector.readthedocs.io/en/latest/intro/index.html
  '''

  ob0, ob1, fc1 = [frame.GeoPoint(*p, degrees=True) for p in [ob0, ob1, fc1]]

  path = nv.GeoPath(ob0, ob1)

  # DPE
  dpe = ob1.distance_and_azimuth(fc1)[0] / 1852
  print(f'\nDPE {dpe}')

  # CTE
  cte = path.cross_track_distance(fc1, method='greatcircle', radius=R) / 1852
  print(f'CTE {cte}')

  # ATE
  # there is no built-in function for ATE, so I am finding the closest point on the path c,
  # and then the great circle distance between c and ob1
  # also set ATE to negative if distance c to ob0 is less than distance ob0 to ob1
  c = path.closest_point_on_great_circle(fc1)
  ate = c.distance_and_azimuth(ob1)[0] / 1852 * (-1 if c.distance_and_azimuth(ob0)[0] < ob0.distance_and_azimuth(ob1)[0] else 1)
  print(f'ATE {ate}')

for t in triplets:
  find_components(*t)
  