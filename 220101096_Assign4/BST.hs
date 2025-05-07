module Main where
import qualified Data.Sequence as Seq
import Data.List (intercalate)

-- Binary Search Tree definition
data BST a = Empty | Node a (BST a) (BST a) deriving (Show)

-- Insert an element into BST
insert :: Ord a => a -> BST a -> BST a
insert x Empty = Node x Empty Empty
insert x (Node y left right)
  | x < y     = Node y (insert x left) right
  | x > y     = Node y left (insert x right)
  | otherwise = Node y left right -- If equal, don't insert duplicate

-- Create a BST from a list
createBST :: Ord a => [a] -> BST a
createBST = foldr insert Empty . reverse

-- Pre-order traversal: Root, Left, Right
preOrder :: BST a -> [a]
preOrder Empty = []
preOrder (Node x left right) = [x] ++ preOrder left ++ preOrder right

-- In-order traversal: Left, Root, Right
inOrder :: BST a -> [a]
inOrder Empty = []
inOrder (Node x left right) = inOrder left ++ [x] ++ inOrder right

-- Post-order traversal: Left, Right, Root
postOrder :: BST a -> [a]
postOrder Empty = []
postOrder (Node x left right) = postOrder left ++ postOrder right ++ [x]

-- Breadth-First Search traversal
bfs :: BST a -> [a]
bfs tree = bfsHelper (Seq.singleton tree) []
  where
    bfsHelper Seq.Empty acc = reverse acc
    bfsHelper (Empty Seq.:<| rest) acc = bfsHelper rest acc
    bfsHelper ((Node x left right) Seq.:<| rest) acc =
      bfsHelper (rest Seq.>< Seq.fromList [left, right]) (x : acc)

-- Parse comma-separated string into list of numbers
parseInput :: String -> [Int]
parseInput = map read . words . map (\c -> if c == ',' then ' ' else c)

-- Main function
main :: IO ()
main = do
  putStrLn "Enter a list of numbers separated by commas:"
  input <- getLine
  let numbers = parseInput input
  let bst = createBST numbers
  
  putStrLn $ "BST Pre-order traversal: " ++ intercalate ", " (map show $ preOrder bst)
  putStrLn $ "BST In-order traversal: " ++ intercalate ", " (map show $ inOrder bst)
  putStrLn $ "BST Post-order traversal: " ++ intercalate ", " (map show $ postOrder bst)
  putStrLn $ "BST Breadth-First Search: " ++ intercalate ", " (map show $ bfs bst)