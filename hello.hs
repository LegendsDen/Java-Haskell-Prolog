-- Define a function 'add' using prefix notation
-- add :: Int -> Int -> Int
import Data.Char
in_range :: Integer->Integer->Integer->Bool
in_range min max x = x>=min && x<=max 

maxs a b = c 
    where c= min a b 
divides :: Int-> Int-> (Int,Int)

divides x y =(div x y , mod x y)

fact x = if x==0 then 1 
        else x* fact x-1

halwa 0 =1
halwa x =x*halwa (x-1)


lengths xs = case xs of
    []     -> 0
    y:ys   -> 1 + lengths ys

length' []=0
length'(x:xs)=1+ length' xs

f x y | x>z =2
    | y==z  =3
    |y<z   =4
    where z= x*x


calculate a b c d =
    let y = a + b
        f x = (x + y) / y
    in f c + f d

inc x= x+1

qsort []=[]
qsort(x:xs) =qsort[y|y<-xs ,y<x] ++[x] ++ qsort[y|y<-xs ,y>=x]

tails::[a]->[a]
tails (x:xs)=xs

data Tree a= Leaf a| Branch (Tree a) (Tree a)

fringe:: Tree a ->[a]
fringe (Leaf a) =[a]
fringe (Branch left right)= fringe left ++ fringe right

avg x y=(x+y)/2

lasts:: [a]->a
lasts(x:xs) =x

my_reverse []=[]
my_reverse(x:xs) =my_reverse xs ++[x]
