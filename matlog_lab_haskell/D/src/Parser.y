{
module Parser where

import Grammar
import Lexer
}

%name      parseExpr
%tokentype { Token }
%error     { parseError }
%monad     { Either String }{ >>= }{ return }

%token IDENT  { Ident $$ }
%token HUI    { HuiT }
%token IMPL   { ImplT }
%token OR     { OrT }
%token AND    { AndT }
%token NOT    { NotT }
%token COMMA  { CommaT }
%token LEFTP  { LeftP }
%token RIGHTP { RightP }

%right IMPL
%left OR
%left AND
%nonassoc NOT

%%

Expr
  : Expr AND Expr       { Binary And $1 $3 }
  | Expr OR Expr        { Binary Or $1 $3 }
  | Expr IMPL Expr      { Binary Impl $1 $3 }
  | LEFTP Expr RIGHTP   { $2 }
  | HUI                 { Hui }
  | IDENT               { Var $1 }

{
  parseError = error "Parse error"
}
