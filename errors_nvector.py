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
  # multiply by -1 because nvector seems to use opposite convention
  cte = path.cross_track_distance(fc1, method='greatcircle', radius=R) / 1852 * -1
  print(f'CTE {cte}')

  # ATE
  # if bearing from c to ob1 is the same as ob1 to ob0 ATE is positive, else negative
  c = path.closest_point_on_great_circle(fc1)
  ate_abs_m, azi_c = c.distance_and_azimuth(ob1)[:2]
  azi_ob1 = ob1.distance_and_azimuth(ob0)[1]
  ate = ate_abs_m * (1 if round(azi_c) == round(azi_ob1) else -1) / 1852

  print(f'ATE {ate}')

for t in triplets:
  find_components(*t)
  