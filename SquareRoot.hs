squareRoot :: Double -> Double
squareRoot x
    | x < 0     = error "Cannot compute square root of negative number"
    | x == 0    = 0
    | otherwise = roundTo 5 $ binarySearchSqrt 0 (max 1 x) x
    where
        binarySearchSqrt :: Double -> Double -> Double -> Double
        binarySearchSqrt low high target
            | high - low < 0.00001 = mid
            | mid * mid > target   = binarySearchSqrt low mid target
            | otherwise            = binarySearchSqrt mid high target
            where
                mid = (low + high) / 2
        roundTo n x = fromIntegral (round (x * 10^n)) / 10^n


        
-- Test cases 
squareRootTests :: [Double]
squareRootTests = [23.56, 4.0, 0.0, 100.0, 2.0,25]

-- Main function to run tests
main :: IO ()
main = do
    putStrLn "Running square root tests:"
    putStrLn "Input\tOutput"
    mapM_ testCase squareRootTests
    where
        testCase input = do
            let computed = squareRoot input
            putStrLn $ show input ++ "\t" ++ show computed









-- Save this as SquareRoot.hs and run:
-- $ ghc SquareRoot.hs
-- $ ./SquareRoot