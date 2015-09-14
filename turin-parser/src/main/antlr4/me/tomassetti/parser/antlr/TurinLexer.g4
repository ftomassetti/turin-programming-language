lexer grammar TurinLexer;

@header {

}

@lexer::members {
    public static final int WHITESPACE = 1;
    public static final int COMMENTS = 2;
}

// It is suggested to define the token types reused in different mode.
// See mode in-interpolation below
tokens { ID, TID, INT, LPAREN, RPAREN, COMMA, RELOP, AND_KW, OR_KW, NOT_KW }

NAMESPACE_KW        : 'namespace';
PROGRAM_KW          : 'program';
PROPERTY_KW         : 'property';
TYPE_KW             : 'type';
VAL_KW              : 'val';
HAS_KW              : 'has';
ABSTRACT_KW         : 'abstract';
SHARED_KW           : 'shared';
IMPORT_KW           : 'import';
AS_KW               : 'as';
VOID_KW             : 'Void';
RETURN_KW           : 'return';
FALSE_KW            : 'false';
TRUE_KW             : 'true';
IF_KW               : 'if';
ELIF_KW             : 'elif';
ELSE_KW             : 'else';

// For definitions reused in mode in-interpolation we define and refer to fragments
AND_KW              : F_AND;
OR_KW               : F_OR;
NOT_KW              : F_NOT;

LPAREN              : '(';
RPAREN              : ')';
LBRACKET            : '{';
RBRACKET            : '}';
LSQUARE             : '[';
RSQUARE             : ']';
COMMA               : ',';
POINT               : '.';
COLON               : ':';
// We use just one token type to reduce the number of states (and not crash Antlr...)
// https://github.com/antlr/antlr4/issues/840
EQUAL               : '==' -> type(RELOP);
DIFFERENT           : '!=' -> type(RELOP);
LESSEQ              : '<=' -> type(RELOP);
LESS                : '<'  -> type(RELOP);
MOREEQ              : '>=' -> type(RELOP);
MORE                : '>'  -> type(RELOP);
// ASSIGNMENT has to comes after EQUAL
ASSIGNMENT          : '=';
// Mathematical operators cannot be merged in one token type because
// they have different precedences
ASTERISK            : '*';
SLASH               : '/';
PLUS                : '+';
MINUS               : '-';

PRIMITIVE_TYPE      : F_PRIMITIVE_TYPE;
BASIC_TYPE          : F_BASIC_TYPE;

ID                  : F_ID;
// Only for types
TID                 : F_TID;
INT                 : F_INT;

STRING_START        : '"' -> pushMode(IN_STRING);

WS                  : (' ' | '\t')+ -> channel(WHITESPACE);
NL                  : '\r'? '\n';

COMMENT             : '/*' .*? '*/' -> channel(COMMENTS);

LINE_COMMENT        : '//' ~[\r\n]* -> channel(COMMENTS);

mode IN_STRING;

STRING_STOP         : '"' -> popMode;
STRING_CONTENT      : (~["\\#]|ESCAPE_SEQUENCE|SHARP)+;
INTERPOLATION_START : '#{' -> pushMode(IN_INTERPOLATION);

mode IN_INTERPOLATION;

INTERPOLATION_END   : '}' -> popMode;
I_PRIMITIVE_TYPE    : F_PRIMITIVE_TYPE -> type(PRIMITIVE_TYPE);
I_BASIC_TYPE        : F_BASIC_TYPE -> type(BASIC_TYPE);
I_FALSE_KW          : 'false' -> type(FALSE_KW);
I_TRUE_KW           : 'true' -> type(TRUE_KW);
I_AND_KW            : F_AND -> type(AND_KW);
I_OR_KW             : F_OR -> type(OR_KW);
I_NOT_KW            : F_NOT -> type(NOT_KW);
I_IF_KW             : 'if' -> type(IF_KW);
I_ELSE_KW           : 'else' -> type(ELSE_KW);
I_ID                : F_ID   -> type(ID);
I_TID               : F_TID -> type(TID);
I_INT               : F_INT -> type(INT);
I_COMMA             : ',' -> type(COMMA);
I_LPAREN            : '(' -> type(LPAREN);
I_RPAREN            : ')' -> type(RPAREN);
I_LSQUARE           : '[' -> type(LSQUARE);
I_RSQUARE           : ']' -> type(RSQUARE);

I_ASTERISK          : '*' -> type(ASTERISK);
I_SLASH             : '/' -> type(SLASH);
I_PLUS              : '+' -> type(PLUS);
I_MINUS             : '-' -> type(MINUS);

I_POINT             : '.' -> type(POINT);
I_EQUAL             : '==' -> type(RELOP);
I_DIFFERENT         : '!=' -> type(RELOP);
I_LESSEQ            : '<=' -> type(RELOP);
I_LESS              : '<'  -> type(RELOP);
I_MOREEQ            : '>=' -> type(RELOP);
I_MORE              : '>'  -> type(RELOP);
I_STRING_START      : '"' -> type(STRING_START), pushMode(IN_STRING);
I_WS                : (' ' | '\t')+ -> type(WS), channel(WHITESPACE);

fragment F_AND            : 'and';
fragment F_OR             : 'or';
fragment F_NOT            : 'not';
fragment F_ID             : ('_')*'a'..'z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
// Only for types
fragment F_TID            : ('_')*'A'..'Z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
fragment F_INT            : '0'|(('1'..'9')('0'..'9')*);
fragment F_PRIMITIVE_TYPE : 'Byte'|'Int'|'Long'|'Boolean'|'Char'|'Float'|'Double'|'Short';
fragment F_BASIC_TYPE     : 'UInt';

fragment ESCAPE_SEQUENCE  : '\\r'|'\\n'|'\\t'|'\\"'|'\\\\';
fragment SHARP            : '#'{ _input.LA(1)!='{' }?;
