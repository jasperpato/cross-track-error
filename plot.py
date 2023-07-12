from mpl_toolkits.basemap import Basemap
import matplotlib.pyplot as plt

PADDING = 0.5

def plot(ob0, ob1, fc1, c=None, title=''):
  if c: lats, lons = zip(ob0, ob1, c, fc1)
  else: lats, lons = zip(ob0, ob1, fc1)

  # map bounds
  min_lat = min(lats)
  min_lon = min(lons)
  lat_diff = max(lats) - min_lat
  lon_diff = max(lons) - min_lon
  width = max(lat_diff, lon_diff)

  plt.figure(figsize=(12, 9))

  map = Basemap(
    projection='mill',
    llcrnrlat=min_lat - PADDING,
    urcrnrlat=min_lat + width + PADDING,
    llcrnrlon=min_lon - PADDING,
    urcrnrlon=min_lon + width + PADDING,
    resolution='c'
  )

  if c: info = zip(lats, lons, ['red', 'green', 'black', 'blue'], ['ob0', 'ob1', 'c', 'fc1'])
  else: info = zip(lats, lons, ['red', 'green', 'blue'], ['ob0', 'ob1', 'fc1'])

  for lat, lon, col, name in info:
    map.scatter(lon, lat, latlon=True, s=10, c=col, marker='o', alpha=1, label=name)

  if c:
    x, y = map(lons, lats)
    map.plot(x, y, linewidth=1)

  plt.legend()
  plt.title(title)

def show():
  plt.show()

if __name__ == '__main__':
  plot((-10.2, 94.4), (-10.4, 94.6), (-10.2651, 94.61872))