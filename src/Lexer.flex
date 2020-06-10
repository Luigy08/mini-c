import java_cup.runtime.*;
import java.util.ArrayList;

%%

%class Lexer
%unicode
%int
%cup
%column
%line
%caseless
%standalone

%{
    String entrada = "";  //se usa para revision de strings/characteres
    int longitudentrada = 0;

    public static ArrayList<String> lexical_errors = new ArrayList<String>();

%}

// Manejo de Comentarios
AbrirComentarioMultiplesLineas = "/*"
CerrarComentarioMultiplesLineas = "*/"
AbrirComentarioUnaLinea  = "//"
CerrarComentarioUnaLinea = "//"

letra = [a-zA-Z]
digito = [0-9]*
NUMREAL = [0-9]+"."[0-9]+

Identificador = {letra}({letra}|{digito}|"_")*
saltolinea = (\n)
espacios = (\t|\r|" ")

PARIZQ = "("
PARDER = ")"

ASIGNACION = "="

OPMULT = ("*"|"/"|"div"|"mod")
OPSUM = ("+"|"-")

PUNTOCOMA = ";"
DOT = "."
DECISION = ":"
QUESTIONMARK = "?"
INCREMENTADOR = "++"
DECREMENTADOR = "--"

COMA = ","
COMILLASIMPLE = "'"

BRACKETIZQ = "["
BRACKETDER = "]"
CURLYIZQ = "{"
CURLYDER = "}"

OPREL = (">"|"<"|"!="|"<="|">="|"==")
OPCONDICIONALES = ("&&"|"||")
OPBOOL = ("true"|"false")
CHARESPECIALES = (".."|"^")

%state CONSTCHARSTRING
%state COMENTARIOUNALINEA
%state COMENTARIOMULTIPLESLINEAS

%%

<YYINITIAL> {

    "PROGRAM"   {return new Symbol(sym.PROGRAM, yycolumn, yyline, yytext());}
    "MAIN"      {return new Symbol(sym.MAIN, yycolumn, yyline, yytext());}
    "PROCEDURE" {return new Symbol(sym.PROCEDURE, yycolumn, yyline, yytext());}
    "WRITE"     {return new Symbol(sym.WRITE, yycolumn, yyline, yytext());}
    "READ"      {return new Symbol(sym.READ, yycolumn, yyline, yytext());}
    "INTEGER"   {return new Symbol(sym.INTEGER, yycolumn, yyline, yytext());}
    "VAR"       {return new Symbol(sym.VAR, yycolumn, yyline, yytext());}
    "FLOAT"      {return new Symbol(sym.FLOAT, yycolumn, yyline, yytext());}
    "CHAR"      {return new Symbol(sym.CHAR, yycolumn, yyline, yytext());}
    "STRING"    {return new Symbol(sym.STRING1, yycolumn, yyline, yytext());}
    "OR"        {return new Symbol(sym.OR, yycolumn, yyline, yytext());}
    "="         {return new Symbol(sym.EQUAL, yycolumn, yyline, yytext());}
    "AND"       {return new Symbol(sym.AND, yycolumn, yyline, yytext());} 
    "XOR"       {return new Symbol(sym.XOR, yycolumn, yyline, yytext());} 
    "NOT"       {return new Symbol(sym.NOT, yycolumn, yyline, yytext());} 

    {OPBOOL}    {return new Symbol(sym.OPBOOL, yycolumn, yyline, yytext());}
    "BOOLEAN"   {return new Symbol(sym.BOOLEAN, yycolumn, yyline, yytext());}

    "FUNCTION"  {return new Symbol(sym.FUNCTION, yycolumn, yyline, yytext());}
    "BEGIN"     {return new Symbol(sym.BEGIN, yycolumn, yyline, yytext());}
    "IF"        {return new Symbol(sym.IF, yycolumn, yyline, yytext());}
    "THEN"      {return new Symbol(sym.THEN, yycolumn, yyline, yytext());}
    "ELSE"      {return new Symbol(sym.ELSE, yycolumn, yyline, yytext());} 
    "RECORD"    {return new Symbol(sym.RECORD, yycolumn, yyline, yytext());} 
    "TYPE"      {return new Symbol(sym.TYPE, yycolumn, yyline, yytext());} 
    "RETURN"     {return new Symbol(sym.RETURN, yycolumn, yyline, yytext());} 
    "END"       {return new Symbol(sym.END, yycolumn, yyline, yytext());}


    "FOR"       {return new Symbol(sym.FOR, yycolumn, yyline, yytext());}
    "DO"        {return new Symbol(sym.DO, yycolumn, yyline, yytext());}
    "WHILE"     {return new Symbol(sym.WHILE, yycolumn, yyline, yytext());}
    "TO"        {return new Symbol(sym.TO, yycolumn, yyline, yytext());}
    "DOWNTO"    {return new Symbol(sym.DOWNTO, yycolumn, yyline, yytext());}
    "REPEAT"    {return new Symbol(sym.REPEAT, yycolumn, yyline, yytext());}
    "UNTIL"     {return new Symbol(sym.UNTIL, yycolumn, yyline, yytext());} 

    "INT"       {return new Symbol(sym.INT, yycolumn, yyline, yytext());} 
    "BOOL"      {return new Symbol(sym.BOOL, yycolumn, yyline, yytext());} 
    "CHAR"      {return new Symbol(sym.CHAR, yycolumn, yyline, yytext());} 
    "VOID"      {return new Symbol(sym.VOID, yycolumn, yyline, yytext());} 

    {DOT}       {return new Symbol(sym.DOT, yycolumn, yyline, yytext());}
    {COMA}      {return new Symbol(sym.COMA, yycolumn, yyline, yytext());}

    {COMILLASIMPLE} {entrada =  ""; longitudentrada = 0; yybegin(CONSTCHARSTRING);}

    {PARDER}            {return new Symbol(sym.PARDER, yycolumn, yyline, yytext());}
    {PARIZQ}            {return new Symbol(sym.PARIZQ, yycolumn, yyline, yytext());}
    {BRACKETIZQ}        {return new Symbol(sym.BRACKETIZQ, yycolumn, yyline, yytext());}
    {BRACKETDER}        {return new Symbol(sym.BRACKETDER, yycolumn, yyline, yytext());}
    {CURLYIZQ}          {return new Symbol(sym.CURLYIZQ, yycolumn, yyline, yytext());}
    {CURLYDER}          {return new Symbol(sym.CURLYDER, yycolumn, yyline, yytext());}

    {Identificador}     {return new Symbol(sym.IDENTIFICADOR, yycolumn, yyline, yytext());}
    {digito}            {return new Symbol(sym.NUM, yycolumn, yyline, yytext());}
    {NUMREAL}           {return new Symbol(sym.NUMREAL, yycolumn, yyline, yytext());}   

    {OPREL}             {return new Symbol(sym.OPREL, yycolumn, yyline, yytext());} 
    {OPCONDICIONALES}   {return new Symbol(sym.OPCONDICIONALES, yycolumn, yyline, yytext());} 
    {CHARESPECIALES}    {return new Symbol(sym.CHARESPECIAL, yycolumn, yyline, yytext());}

    {ASIGNACION}        {return new Symbol(sym.ASIGNACION, yycolumn, yyline, yytext());}
    {DECISION}          {return new Symbol(sym.DECISION, yycolumn, yyline, yytext());}
    {QUESTIONMARK}      {return new Symbol(sym.QUESTIONMARK, yycolumn, yyline, yytext());}
    {INCREMENTADOR}     {return new Symbol(sym.INCREMENTADOR, yycolumn, yyline, yytext());}
    {DECREMENTADOR}     {return new Symbol(sym.DECREMENTADOR, yycolumn, yyline, yytext());}

    {OPSUM}             {return new Symbol(sym.OPSUM, yycolumn, yyline, yytext());}
    {OPMULT}            {return new Symbol(sym.OPMULT, yycolumn, yyline, yytext());}

    {PUNTOCOMA}         {return new Symbol(sym.PUNTOCOMA, yycolumn, yyline, yytext());}

    {espacios}      {//ignorar
                        }
    {saltolinea}    {//ignorar
                        }

    {AbrirComentarioMultiplesLineas}    {yybegin(COMENTARIOMULTIPLESLINEAS);}
    {AbrirComentarioUnaLinea}           {yybegin(COMENTARIOUNALINEA);}
    {CerrarComentarioUnaLinea} {
        //error
        System.out.println("Error, este caracter no es permitido. Usar '//'");
        lexical_errors.add("Error, este caracter no es permitido. Usar '//'" + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
    {CerrarComentarioMultiplesLineas} {
        //error
        System.out.println("Error, este caracter no es permitido. Usar '/'* *'/'");
        lexical_errors.add("Error, este caracter no es permitido. Usar '/'* *'/'"+ 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }


    . { //cualquier otra cosa, agregar a lista de errores
        System.out.println("Error. Este caracter \'" + yytext() + "\' no es permitido.");
        lexical_errors.add("Error. Este caracter \'" + yytext() + "\' no es permitido." + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
}

<CONSTCHARSTRING> {

    {COMILLASIMPLE} {
        longitudentrada++;
        if(longitudentrada == 2){
            yybegin(YYINITIAL);
            return new Symbol(sym.CONSTCHAR, yycolumn, yyline, entrada);
        } else{
            yybegin(YYINITIAL);
            return new Symbol(sym.CONSTSTRING, yycolumn, yyline, entrada);
        }
    }
    . {
        entrada += yytext();
        longitudentrada++;
        }
}

<COMENTARIOUNALINEA> {

    {AbrirComentarioUnaLinea} {
        //error
        System.out.println("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario.");
        System.out.println("Error Lexico en la linea: " + (yyline+1) + " columna: " 
        + (yycolumn + 1) + "\n");
        lexical_errors.add("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario." + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
    {AbrirComentarioMultiplesLineas} {
        //error
        System.out.println("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario.");
        System.out.println("Error Lexico en la linea: " + (yyline +1) + " columna: " 
        + (yycolumn + 1) + "\n");
        lexical_errors.add("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario." + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
    {CerrarComentarioUnaLinea} {
        yybegin(YYINITIAL);
    }
    
    {CerrarComentarioMultiplesLineas} {
        System.out.println("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario de una sola linea.");
        System.out.println("Error Lexico en la linea: " + (yyline +1) + " columna: " 
        + (yycolumn + 1) + "\n");
        lexical_errors.add("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario de una sola linea." + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
    . {}
}

<COMENTARIOMULTIPLESLINEAS> {

    {AbrirComentarioUnaLinea} {
        //error
        System.out.println("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario.");
        System.out.println("Error Lexico en la linea: " + (yyline+1) + " columna: " 
        + (yycolumn + 1) + "\n");
        lexical_errors.add("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario." + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
    {AbrirComentarioMultiplesLineas} {
        //error
        System.out.println("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario.");
        System.out.println("Error Lexico en la linea: " + (yyline +1) + " columna: " 
        + (yycolumn + 1) + "\n");
        lexical_errors.add("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario." + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
    {CerrarComentarioUnaLinea} {
        System.out.println("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario de multiples lineas.");
        System.out.println("Error Lexico en la linea: " + (yyline +1) + " columna: " 
        + (yycolumn + 1) + "\n");
        lexical_errors.add("Error. Este caracter \'" + yytext() + "\' no es permitido adentro de un comentario de multiples lineas." + 
        yytext()+ "' en lÃ­nea " + String.valueOf(yyline + 1)+", columna "
        + String.valueOf(yycolumn + 1)); 
    }
    
    {CerrarComentarioMultiplesLineas} {
         yybegin(YYINITIAL);
    }
    . {}

}
