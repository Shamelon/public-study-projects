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
%token IMPL   { ImplT }
%token OR     { OrT }
%token AND    { AndT }
%token NOT    { NotT }
%token TURN   { TurnT }
%token COMMA  { CommaT }
%token LEFTP  { LeftP }
%token RIGHTP { RightP }

%right IMPL
%left OR
%left AND
%nonassoc NOT

%%
Phrase
  : Hyps TURN Expr { Line Turn $1 $3 }

Hyps
  :                 { [] }
  | Expr COMMA Hyps { $1 : $3}
  | Expr            { $1 : []}

Expr
  : Expr AND Expr       { Binary And $1 $3 }
  | Expr OR Expr        { Binary Or $1 $3 }
  | Expr IMPL Expr      { Binary Impl $1 $3 }
  | NOT Expr            { Not $2 }
  | LEFTP Expr RIGHTP   { $2 }
  | IDENT               { Var $1 }

{
  parseError = error "Parse error"
}
