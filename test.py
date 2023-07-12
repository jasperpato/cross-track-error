import errors_nvector
import errors_pyproj
import sys

if len(sys.argv) > 1 and sys.argv[1] != "pyproj":
  method = "nvector"
  find_components = errors_nvector.find_components 
else:
  method = "pyproj"
  find_components = errors_pyproj.find_components

if __name__ == '__main__':
  
  triplets = [
    ((-10.2, 94.4), (-10.4, 94.6), (-10.2651, 94.61872)), # CTE: 5.24 ATE: 6.29 (nautical miles)
    ((-10.2, 94.4), (-11, 94.9), (-10.70625,	94.625)), # CTE: 14.83 ATE: 18.85 (nautical miles)
  ]

  # obs0,obs1, fc1, expected answer
  testcases = [
    ((-10.2, 94.4), (-10.4, 94.6), (-10.2651, 94.61872), (-5.00, -6.47)),
    ((2.85, 5), (0, 0), (0, 10), ("{0, 0, 210, 10, 0, -520.56, -300.55}, #// 210 degrees")),
    ((-24.7, 128.43), (-25.3,129.0), (-20.87,  130.2), ("TC Tiffany example should be nve ATE ~159nm, pve CTE")),
    ((-10, 100), (-10, 104), (-10, 95), ("Along track ~ -540, Cross track -4")),
    ((0, 0), (10, 0), (1, 0.1), (-540, -6.004)),
  ]

  print(f'{method}')

  # for t in triplets:
  for tests in testcases:
    t = tests[:3]
    dpe, cte, ate = find_components(*t)
    print(f'\nDPE {dpe}')
    print(f'ATE {ate}')
    print(f'CTE {cte} # {tests[-1]}')