module Main where

import Data.Array
import Data.Char (ord)
import Data.Array.Base (unsafeAt)
import           Data.Maybe
import           System.IO  (isEOF)
import Data.Word (Word8)
import qualified Data.Bits
import Control.Applicative(Applicative(..))
import           Data.List
import           Data.Map  as M
import           Grammar
import           Lexer      (alexScanTokens)
import           Parser     (parseExpr)


main = do
  f empty empty 1

f :: Map Expr [(Int, [Expr], Maybe Expr)] -> Map Expr [(Int, [Expr])] -> Int -> IO ()
f m1 m2 n = do
  done <- (isEOF)
  if done
    then return ()
    else do
      input <- getLine
      case parseExpr (alexScanTokens input) of
        Left err -> putStrLn err
        Right phrase ->
          putStrLn
            $ ("["
                 ++ (show n)
                 ++ "] "
                 ++ (show phrase)
                 ++ " "
                 ++ (check m1 m2 phrase))
      case parseExpr (alexScanTokens input) of
        Left err -> putStrLn err
        Right phrase -> f (insert1 phrase m1 n) (insert2 (fst $ splitPhrase $ phrase) (snd $ splitPhrase $ phrase) m2 n) (n + 1)

insert1 ::
     Phrase
  -> Map Expr [(Int, [Expr], Maybe Expr)]
  -> Int
  -> Map Expr [(Int, [Expr], Maybe Expr)]
insert1 phrase m n =
  if (member b m)
    then M.insert b ((n, hyps, a) : (fromJust (M.lookup b m))) m
    else M.insert b ((n, hyps, a) : []) m
  where
    a = fst $ splitExpr $ snd $ splitPhrase $ phrase
    b = snd $ splitExpr $ snd $ splitPhrase $ phrase
    hyps = fst $ splitPhrase $ phrase

insert2 :: [Expr] -> Expr -> Map Expr [(Int, [Expr])] -> Int -> Map Expr [(Int, [Expr])]
insert2 hyps (Binary Impl a b) m n = insert2 (a : hyps) b m n
insert2 hyps b m n = 
  if (member b m)
    then M.insert b ((n, (sort hyps)) : (fromJust (M.lookup b m))) m
    else M.insert b ((n, (sort hyps)) : []) m

splitPhrase :: Phrase -> ([Expr], Expr)
splitPhrase (Line Turn a b) = (sort a, b)

splitPhraseWithoutSorting :: Phrase -> ([Expr], Expr)
splitPhraseWithoutSorting (Line Turn a b) = (a, b)

splitExpr :: Expr -> (Maybe Expr, Expr)
splitExpr (Binary Impl a b) = (Just a, b)
splitExpr b                 = (Nothing, b)

joinExpr :: (Maybe Expr, Expr) -> Expr
joinExpr (Nothing, b) = b
joinExpr (Just a, b)  = (Binary Impl a b)

check :: Map Expr [(Int, [Expr], Maybe Expr)] -> Map Expr [(Int, [Expr])] -> Phrase -> String
check m1 m2 phrase =
  case checkAx $ snd $ splitPhrase $ phrase of
    Just a -> a
    Nothing ->
      case checkHyp phrase of
        Just a -> a
        Nothing ->
          case checkMod m1 phrase of
            Just a -> a
            Nothing ->
              case checkDed m2 phrase of
                Just a  -> a
                Nothing -> "[Incorrect]"

checkAx :: Expr -> Maybe String
checkAx (Binary Impl a (Binary Impl b c))
  | a == c = Just "[Ax. sch. 1]"
checkAx (Binary Impl (Binary Impl a b) (Binary Impl (Binary Impl c (Binary Impl d e)) (Binary Impl f g)))
  | a == c && c == f && b == d && e == g = Just "[Ax. sch. 2]"
checkAx (Binary Impl a (Binary Impl b (Binary And d e)))
  | a == d && b == e = Just "[Ax. sch. 3]"
checkAx (Binary Impl (Binary And a b) c)
  | a == c = Just "[Ax. sch. 4]"
  | b == c = Just "[Ax. sch. 5]"
checkAx (Binary Impl a (Binary Or b c))
  | a == b = Just "[Ax. sch. 6]"
  | a == c = Just "[Ax. sch. 7]"
checkAx (Binary Impl (Binary Impl a b) (Binary Impl (Binary Impl c d) (Binary Impl (Binary Or e f) g)))
  | a == e && b == d && d == g && c == f = Just "[Ax. sch. 8]"
checkAx (Binary Impl (Binary Impl a b) (Binary Impl (Binary Impl c (Not d)) (Not f)))
  | a == c && b == d && c == f = Just "[Ax. sch. 9]"
checkAx (Binary Impl (Not (Not a)) b)
  | a == b = Just "[Ax. sch. 10]"
checkAx _ = Nothing

checkHyp :: Phrase -> Maybe String
checkHyp phrase = checkHyp' (fst a) (snd a) 1
  where
    a = splitPhraseWithoutSorting phrase

checkHyp' :: [Expr] -> Expr -> Int -> Maybe String
checkHyp' [] b n = Nothing
checkHyp' (x:xs) b n
  | x == b = Just ("[Hyp. " ++ (show n) ++ "]")
  | otherwise = checkHyp' xs b (n + 1)

checkMod :: Map Expr [(Int, [Expr], Maybe Expr)] -> Phrase -> Maybe String
checkMod m phrase =
  case (M.lookup (snd $ splitPhrase phrase) m) of
    Just as -> findAB phrase m as
    Nothing -> Nothing

findAB ::
     Phrase
  -> Map Expr [(Int, [Expr], Maybe Expr)]
  -> [(Int, [Expr], Maybe Expr)]
  -> Maybe String
findAB _ _ [] = Nothing
findAB phrase m ((n, hyps, a):xs)
  | a == Nothing = findAB phrase m xs
  | (fst $ splitPhrase $ phrase) == hyps =
    case findA phrase m (fromJust a) n of
      Just x  -> Just x
      Nothing -> findAB phrase m xs
  | otherwise = findAB phrase m xs

findA ::
     Phrase
  -> Map Expr [(Int, [Expr], Maybe Expr)]
  -> Expr
  -> Int
  -> Maybe String
findA phrase m (Binary Impl x y) n =
  findXY (fst $ splitPhrase phrase) (Just x) (M.lookup y m) n
findA phrase m a n = findXY (fst $ splitPhrase $ phrase) Nothing (M.lookup a m) n

findXY ::
     [Expr]
  -> Maybe Expr
  -> Maybe [(Int, [Expr], Maybe Expr)]
  -> Int
  -> Maybe String
findXY _ _ Nothing _ = Nothing
findXY _ _ (Just []) _ = Nothing
findXY hyps x (Just ((k, hyps1, a):xs)) n
  | a == x && hyps1 == hyps = Just ("[M.P. " ++ show k ++ ", " ++ show n ++ "]")
  | otherwise = findXY hyps x (Just xs) n

checkDed :: Map Expr [(Int, [Expr])] -> Phrase -> Maybe String
checkDed m phrase = case M.lookup (snd normForm) m of
  Just hypsList -> findDed (fst normForm) hypsList 
  Nothing -> Nothing
  where
    normForm = toNormForm (fst $ splitPhrase $ phrase) (snd $ splitPhrase $ phrase)

toNormForm :: [Expr] -> Expr -> ([Expr], Expr)
toNormForm hyps (Binary Impl a b) = toNormForm (a : hyps) b
toNormForm hyps b = (sort hyps, b)

findDed :: [Expr] -> [(Int, [Expr])] -> Maybe String
findDed _ [] = Nothing
findDed hyps0 ((n, hyps):xs)
  | hyps0 == hyps = Just ("[Ded. " ++ show n ++ "]") 
  | otherwise = findDed hyps0 xs
