module Grammar where

import           Data.List
import           Data.Map  as M

data Binop
  = Impl
  | Or
  | And
  deriving (Eq, Ord)

instance Show Binop where
  show Impl = "->"
  show Or   = "|"
  show And  = "&"

data Derivable =
  Turn
  deriving (Eq, Ord)

instance Show Derivable where
  show Turn = "|-"

data Expr
  = Binary Binop Expr Expr
  | Not Expr
  | Var String
  deriving (Eq, Ord)

instance Show Expr where
  show (Binary op a b) = "(" ++ show a ++ show op ++ show b ++ ")"
  show (Not e)         = "!" ++ show e
  show (Var name)      = name

data Phrase =
  Line Derivable [Expr] Expr
  deriving (Eq, Ord)

instance Show Phrase where
  show (Line turn hs a) = show' hs ++ show turn ++ showWithoutBrackets a

show' :: [Expr] -> String
show' []     = ""
show' (x:[]) = showWithoutBrackets x
show' (x:xs) = showWithoutBrackets x ++ "," ++ show' xs

showWithoutBrackets :: Expr -> String
showWithoutBrackets (Binary op a b) = show a ++ show op ++ show b
showWithoutBrackets (Not e)         = "!" ++ show e
showWithoutBrackets (Var name)      = name
