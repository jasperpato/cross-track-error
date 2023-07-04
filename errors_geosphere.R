# geosphere docs: https://cran.r-project.org/web/packages/geosphere/geosphere.pdf

# install.packages("geosphere") # necessary for the first run
library(geosphere)

find_components <- function(ob0, ob1, fc1) {
  # convert from (lat, long) to (long, lat)
  ob0 = rev(ob0)
  ob1 = rev(ob1)
  fc1 = rev(fc1)
  
  cat("\nDPE", distHaversine(ob1, fc1) / 1852, "\n")
  cat("CTE", dist2gc(ob1, ob0, fc1, sign=TRUE) / 1852, "\n")
  cat("ATE", alongTrackDistance(ob1, ob0, fc1) / 1852, "\n")
}

ob0 = c(-10.2, 94.4)
ob1 = c(-10.4, 94.6)
fc1 = c(-10.2651, 94.61872)

find_components(ob0, ob1, fc1) # CTE: 5.24 ATE: 6.29 (nautical miles)

ob0 = c(-10.2, 94.4)
ob1 = c(-11, 94.9)
fc1 = c(-10.70625,	94.625)

find_components(ob0, ob1, fc1) # CTE: 14.83 ATE: 18.85 (nautical miles)



