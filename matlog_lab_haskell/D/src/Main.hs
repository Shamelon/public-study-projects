module Main where

import Data.Array
import Data.Char (ord)
import Data.Array.Base (unsafeAt)
import           Data.Maybe
import           Data.Set as Set
import           System.IO  (isEOF)
import Data.Word (Word8)
import qualified Data.Bits
import Control.Applicative(Applicative(..))
import           Data.List
import           Data.Map  as M
import           Grammar
import           Lexer      (alexScanTokens)
import           Parser     (parseExpr)

main :: IO()
main = do
  input <- getLine
  case parseExpr (alexScanTokens input) of
        Left err -> putStrLn err
        Right expr -> putStrLn $ case fst $ (existProof expr (sort . Set.toList $ (getVars expr)) M.empty) of
          Left proof -> proofToString 0 [] proof
          Right refutable -> refutable
  
splitExpr :: Expr -> (Maybe Expr, Expr)
splitExpr (Binary Impl a b) = (Just a, b)
splitExpr b                 = (Nothing, b)

joinExpr :: (Maybe Expr, Expr) -> Expr
joinExpr (Nothing, b) = b
joinExpr (Just a, b)  = (Binary Impl a b)

data Proof = Node {children :: [Proof], reason :: String, phrase :: Phrase, extraHyps :: [Expr]}
instance Show Proof where
  show (Node children phrase reason extraHyps) = (show extraHyps) ++ (show phrase) ++ (show reason) 

proofToString :: Int -> [Expr] -> Proof -> String
proofToString level extraContext proof = (intercalate "" (Data.List.map (proofToString (level + 1) (extraContext ++ (extraHyps proof))) (children proof))) ++
  ("[" ++ (show level) ++ "] " ++ (show $ (addHyps (phrase proof) extraContext (extraHyps proof))) ++ " " ++ (reason proof)) ++ "\n"

eval :: Expr -> Map Expr [Bool] -> Bool
eval Hui _ = False
eval (Var name) values = Data.List.foldl (||) False (fromJust $ M.lookup (Var name) values)
eval (Binary Or a b) values = eval a values || eval b values
eval (Binary And a b) values = eval a values && eval b values
eval (Binary Impl a b) values = not (eval a values) || eval b values

contains :: Expr -> Bool -> Map Expr [Bool] -> Bool
contains key value values = case M.lookup key values of
  Just valueList -> elem value valueList
  _ -> False 

getVars :: Expr -> Set Expr
getVars (Binary op a b) = (Set.union (getVars a) (getVars b))
getVars Hui = Set.empty
getVars (Var name) = Set.insert (Var name) Set.empty

existProof :: Expr -> [Expr] -> Map Expr [Bool] -> (Either Proof String, Bool)
existProof expr [] values = if (eval expr values)
  then toEitherString (proofRec expr values)
  else (Right ("Formula is refutable [" ++ (valuesToString $ M.toList $ values) ++ "]"), False)
existProof expression (var : vars) values = case fst pair1 of
  Right refutable -> pair1
  Left proof1 -> case fst pair2 of
    Right refutable -> pair2
    Left proof2 -> toEitherString
      (Node
        [
          (addExpr proof1 var),
          (addExpr proof2 (Binary Impl var Hui)),
          (proof_a_or_not_a var)
        ]
        "[E|]" (Line Turn [] (expr $ phrase $ proof2)) [],
      True)
  where
    pair1 = existProof expression vars (addValue var True values)
    pair2 = existProof expression vars (addValue var False values)

valuesToString :: [(Expr, [Bool])] -> String
valuesToString [] = ""
valuesToString [value] = valueToString value
valuesToString (value : values) = (valueToString value) ++ "," ++ (valuesToString values)

valueToString :: (Expr, [Bool]) -> String
valueToString (a, (False:bools)) = (show a) ++ ":=F"
valueToString (a, (True:bools)) = (show a) ++ ":=T"

toEitherString :: (Proof, Bool) -> (Either Proof String, Bool)
toEitherString (proof, b) = (Left proof, b) 

addValue :: Expr -> Bool -> Map Expr [Bool] -> Map Expr [Bool]
addValue var val values = case M.lookup var values of
  Just valueList -> M.insert var (val : valueList) values
  Nothing -> M.insert var (val : []) values

addExpr :: Proof -> Expr -> Proof
addExpr p e = p { extraHyps = e : extraHyps p }

addHyps :: Phrase -> [Expr] -> [Expr] -> Phrase
addHyps (Line Turn hyps0 expr) hyps1 hyps2 = (Line Turn (hyps0 ++ hyps1 ++ hyps2) expr)

proofRec :: Expr -> Map Expr [Bool] -> (Proof, Bool)
proofRec (Binary op left right) values =
  proof [(fst pairLeft), (fst pairRight)] (Line Turn [] (Binary op left right)) newValues
  where
    pairLeft = proofRec left values
    pairRight = proofRec right values
    newValues =
      addValue
        (if (snd pairLeft)
          then (expr $ phrase $ fst $ pairLeft)
          else (fromJust $ fst $ splitExpr $ expr $ phrase $ fst $ pairLeft))
        (snd pairLeft)
        (addValue 
          (if (snd pairRight)
            then (expr $ phrase $ fst $ pairRight) 
            else (fromJust $ fst $ splitExpr $ expr $ phrase $ fst $ pairRight))
          (snd pairRight)
          (addValue Hui False M.empty))
proofRec expression values = proof [] (Line Turn [] expression) values


proof :: [Proof] -> Phrase -> Map Expr [Bool] -> (Proof, Bool)
proof _ (Line Turn hyps Hui) _ =
  (Node 
    [
      (Node 
        [
        ]
        "[Ax]" (Line Turn hyps Hui) [Hui])
    ]
    "[I->]" (Line Turn hyps (Binary Impl Hui Hui)) [],
  False)
proof _ (Line Turn hyps (Var name)) values
  | contains (Var name) True values = (Node [] "[Ax]" (Line Turn hyps (Var name)) [], True)
  | otherwise = (Node [] "[Ax]" (Line Turn hyps (Binary Impl (Var name) Hui)) [], False)
--And--
proof prevProofs (Line Turn hyps (Binary And a b)) values
  | contains a True values && contains b True values = 
    (Node prevProofs "[I&]" (Line Turn hyps (Binary And a b)) [], True)
  | contains a False values =
    (Node
      [
        Node
          [
            head prevProofs,
            Node
              [
                Node
                  []
                  "[Ax]" (Line Turn hyps (Binary And a b)) []
              ]
              "[El&]" (Line Turn hyps a) []
          ]
          "[E->]" (Line Turn hyps Hui) [Binary And a b]
      ]
      "[I->]" (Line Turn hyps (Binary Impl (Binary And a b) Hui)) [],
    False)
  | contains b False values =
    (Node
      [
        Node
          [
            head . tail $ prevProofs,
            Node
              [
                Node
                  []
                  "[Ax]" (Line Turn hyps (Binary And a b)) []
              ]
              "[Er&]" (Line Turn hyps b) []
          ]
          "[E->]" (Line Turn hyps Hui) [Binary And a b]
      ]
      "[I->]" (Line Turn hyps (Binary Impl (Binary And a b) Hui)) [],
    False)
--Impl--
proof prevProofs (Line Turn hyps (Binary Impl a b)) values
  | contains a False values =
    (Node
      [
        Node
          [
            Node
              [
                head prevProofs,
                Node
                  []
                  "[Ax]" (Line Turn hyps a) []
              ]
              "[E->]" (Line Turn hyps Hui) [Binary Impl b Hui]
          ]
          "[E!!]" (Line Turn hyps b) [a]
      ]
      "[I->]" (Line Turn hyps (Binary Impl a b)) [],
    True)
  | contains b True values =
    (Node 
      [
        Node
          (children (head . tail $ prevProofs))
          (reason (head . tail $ prevProofs))
          (phrase (head . tail $ prevProofs))
          ((extraHyps (head . tail $ prevProofs)) ++ [a])
      ]
      "[I->]" (Line Turn hyps (Binary Impl a b)) [],
    True)
  | contains a True values =
    (Node
      [
        Node
          [
            head . tail $ prevProofs,
            Node
              [
                (Node
                  []
                  "[Ax]" (Line Turn hyps (Binary Impl a b)) []),
                head prevProofs
              ]
              "[E->]" (Line Turn hyps b) []
          ]
          "[E->]" (Line Turn hyps Hui) [Binary Impl a b]
      ]
      "[I->]" (Line Turn hyps (Binary Impl (Binary Impl a b) Hui)) [],
    False)
proof prevProofs (Line Turn hyps (Binary Or a b)) values
  | contains a True values = 
    (Node [head prevProofs] "[Il|]" (Line Turn hyps (Binary Or a b)) [], True)
  | contains b True values = 
    (Node [head . tail $ prevProofs] "[Ir|]" (Line Turn hyps (Binary Or a b)) [], True)
  | contains a False values =
    (Node
      [
        Node
          [
            (Node
              [
                head prevProofs,
                Node
                  []
                  "[Ax]" (Line Turn hyps a) []
              ]
              "[E->]" (Line Turn hyps Hui) [a]
            ),
            (Node
              [
                head . tail $ prevProofs,
                Node
                  []
                  "[Ax]" (Line Turn hyps b) []
              ]
              "[E->]" (Line Turn hyps Hui) [b]
            ),
            (Node
              []
              "[Ax]" (Line Turn hyps (Binary Or a b)) []
            )
          ]
          "[E|]" (Line Turn hyps Hui) [Binary Or a b]
      ]
      "[I->]" (Line Turn hyps (Binary Impl (Binary Or a b) Hui)) [],
    False)
  
proof_a_or_not_a :: Expr -> Proof
proof_a_or_not_a a =  
  Node
    [
      Node
        [
          Node
            [
              Node
                [
                  Node
                    []
                    "[Ax]" (Line Turn [] (Binary Impl (Binary Impl (Binary Or a (Binary Impl a Hui)) Hui) Hui)) [],
                  Node
                    []
                    "[Ax]" (Line Turn [] (Binary Impl (Binary Or a (Binary Impl a Hui)) Hui)) []
                ]
                "[E->]" (Line Turn [] Hui) [Binary Impl (Binary Or a (Binary Impl a Hui)) Hui]
            ]
            "[E!!]" (Line Turn [] (Binary Or a (Binary Impl a Hui))) [Binary Impl (Binary Impl (Binary Or a (Binary Impl a Hui)) Hui) Hui]
        ]
        "[I->]" (Line Turn [] (Binary Impl (Binary Impl (Binary Impl (Binary Or a (Binary Impl a Hui)) Hui) Hui) (Binary Or a (Binary Impl a Hui)))) [],
      Node
        [
          Node
            [
              Node
                []
                "[Ax]" (Line Turn [] (Binary Impl (Binary Or a (Binary Impl a Hui)) Hui)) [],
              Node
                [
                  Node
                    [
                      Node
                        [
                          Node
                            []
                            "[Ax]" (Line Turn [] (Binary Impl (Binary Or a (Binary Impl a Hui)) Hui)) [],
                          Node
                            [
                              Node
                                []
                                "[Ax]" (Line Turn [] a) []
                            ]
                            "[Il|]" (Line Turn [] (Binary Or a (Binary Impl a Hui))) []
                        ]
                        "[E->]" (Line Turn [] Hui) [a]
                    ]
                  "[I->]" (Line Turn [] (Binary Impl a Hui)) []
                ]
                "[Ir|]" (Line Turn [] (Binary Or a (Binary Impl a Hui))) []
            ]
            "[E->]" (Line Turn [] Hui) [Binary Impl (Binary Or a (Binary Impl a Hui)) Hui]
        ]
        "[I->]" (Line Turn [] (Binary Impl (Binary Impl (Binary Or a (Binary Impl a Hui)) Hui) Hui)) []
    ]
    "[E->]" (Line Turn [] (Binary Or a (Binary Impl a Hui))) []
