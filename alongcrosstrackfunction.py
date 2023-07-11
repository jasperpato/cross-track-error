import pyproj, math, numpy as np

Eradius = 6378137
conv_nm = 1852

def along_cross_track_errors(ob0, ob1, fc1):
    '''
    Returns cross-track error (CTE) and along-track error (ATE) in nautical miles.

    documentation for pyproj.Geod.inv: https://pyproj4.github.io/pyproj/stable/api/geod.html
    great circle formulae: https://www.movable-type.co.uk/scripts/latlong.html#:~:text=Cross%2Dtrack%20distance&text=the%20earth's%20radius-,JavaScript%3A,the%20relevant%20distance%20and%20bearings.

    Tropical cyclone tracking and verification techniques for Met Office numerical weather prediction models, J. T. Heming, Royal Meteorological Society
    https://doi.org/10.1002/met.1599

    A TC moving from east to west (i.e. not yet recurved), a positive CTE (left of track, in Sth hemi) would indicate that the model had a polewards bias in its forecast track. 
    The ATE value is deemed to be positive if the point of intersection lies ahead of the observed position (ob1) in the observed direction of motion. Thus a positive ATE would indicate that the model has a fast bias in its forecast track.
    '''
    #geod = pyproj.Geod(ellps='WGS84')
    geod = pyproj.Geod(sphere=True, a=Eradius)

    t10,_, d12 = geod.inv(ob1[1], ob1[0], ob0[1], ob0[0])
    t13,_, d13 = geod.inv(ob1[1], ob1[0], fc1[1], fc1[0])
    _, _, hyp = geod.inv(ob0[1], ob0[0], fc1[1], fc1[0])
    
    cte = math.asin(math.sin(d13 / Eradius) * math.sin(np.radians(t13 - t10))) * Eradius
    d0c = math.sqrt(hyp**2 - cte**2)
    ate = math.acos(math.cos(d13 / Eradius) / math.cos(cte / Eradius)) * Eradius * (-1 if d0c < d12 else 1)

    return ate/conv_nm, cte/conv_nm