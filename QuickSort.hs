-- Quick sort implementation using list comprehension
qsort :: Ord a => [a] -> [a]
qsort [] = []
qsort (x:xs) = qsort [y | y <- xs, y < x] ++ [x] ++ qsort [y | y <- xs, y >= x]




testInputs :: [[Int]]
testInputs = [
    [12, 2, 4, 5, 18],      
    [7, 3, 9, 1, 5],          
    [1, 2, 3, 4, 5],         
    [5, 4, 3, 2, 1],        
    [3, 3, 1, 4, 1],           
    [15, 15, 15, 15, 15],    
    [9, 1, 8, 2, 7, 3, 6, 4, 5], 
    [0, -1, 2, -3, 4],        
    [100, 50, 75, 25, 90]     
    ]

-- Main function to run tests and print inputs and outputs
main :: IO ()
main = do
    putStrLn "Input\t\tOutput"
    mapM_ testCase testInputs
    where
        testCase input = do
            let output = qsort input
            putStrLn $ show input ++ "\t" ++ show output








-- Save this as QuickSort.hs and run:
-- $ ghc QuickSort.hs
-- $ ./QuickSort