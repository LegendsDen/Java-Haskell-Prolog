-- Efficient Fibonacci number calculation using tail recursion and accumulator pattern
fib :: Integer -> Integer
fib n = fibTail n 0 1
  where
    fibTail 0 a _ = a
    fibTail n a b = fibTail (n-1) b (a + b)


testInputs :: [Integer]
testInputs = [0, 1, 2, 3, 5, 8, 10, 13, 15, 200]  -- 10 test cases

-- Main function to run tests and print inputs and outputs
main :: IO ()
main = do
    putStrLn "Input\tOutput"
    mapM_ testCase testInputs
    where
        testCase input = do
            let output = fib input
            putStrLn $ show input ++ "\t" ++ show output








-- Save this as Fibonacci.hs and run:
-- $ ghc Fibonacci.hs
-- $ ./Fibonacci