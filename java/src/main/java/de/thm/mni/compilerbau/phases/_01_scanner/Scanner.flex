package de.thm.mni.compilerbau.phases._01_scanner;

import de.thm.mni.compilerbau.utils.SplError;
import de.thm.mni.compilerbau.phases._02_03_parser.Sym;
import de.thm.mni.compilerbau.absyn.Position;
import de.thm.mni.compilerbau.table.Identifier;
import de.thm.mni.compilerbau.CommandLineOptions;
import java_cup.runtime.*;

%%


%class Scanner
%public
%line
%column
%cup
%eofval{
    return new java_cup.runtime.Symbol(Sym.EOF, yyline + 1, yycolumn + 1);   //This needs to be specified when using a custom sym class name
%eofval}

%{
    public CommandLineOptions options = null;
  
    private Symbol symbol(int type) {
      return new Symbol(type, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type, Object value) {
      return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}
lines = \r|\n|\r\n
space = {lines}|[ \t\f]
comment = \/\/.*
decimal = [0-9]+
ident = [A-Za-z_$][A-Za-z0-9_$]*


//Definition
%%
//Befehle


// TODO (assignment 1): The regular expressions for all tokens need to be defined here.
"+" {return symbol(Sym.PLUS);}
"-" {return symbol(Sym.MINUS);}
"*" {return symbol(Sym.STAR);}
"/" {return symbol(Sym.SLASH);}
"[" {return symbol(Sym.LBRACK);}
"]" {return symbol(Sym.RBRACK);}
"(" {return symbol(Sym.LPAREN);}
")" {return symbol(Sym.RPAREN);}
"{" {return symbol(Sym.LCURL);}
"}" {return symbol(Sym.RCURL);}
"<" {return symbol(Sym.LT);}
"<=" {return symbol(Sym.LE);}
">" {return symbol(Sym.GT);}
">=" {return symbol(Sym.GE);}
"!=" {return symbol(Sym.NE);}
"==" {return symbol(Sym.EQ);}
">" {return symbol(Sym.ASGN);}
">=" {return symbol(Sym.COMMA);}
"!=" {return symbol(Sym.COLON);}
"==" {return symbol(Sym.SEMIC);}
// Noch zu machen
">" {return symbol(Sym.ASGN);}
">=" {return symbol(Sym.TYPE);}
"!=" {return symbol(Sym.PROC);}
"==" {return symbol(Sym.ARRAY);}
">" {return symbol(Sym.OF);}
">=" {return symbol(Sym.REF);}
"!=" {return symbol(Sym.VAR);}
"==" {return symbol(Sym.IF);}
"!=" {return symbol(Sym.ELSE);}
"==" {return symbol(Sym.WHILE);}

// Tokens weiter beschreiben
{space} {}
{comment} {}
{decimal} {return symbol(Sym.INTLIT,Integer.parseInt(yytext()));}
{ident} {return symbol(Sym.IDENT,yytext());}



[^]		{throw SplError.IllegalCharacter(new Position(yyline + 1, yycolumn + 1), yytext().charAt(0));}
