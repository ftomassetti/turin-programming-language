lexer grammar TurinLexer;

@header {

}

@lexer::members {
    public static final int WHITESPACE = 1;
    public static final int COMMENTS = 2;
}

tokens { ID, TID, INT, LPAREN, RPAREN, COMMA }

NAMESPACE_KW: 'namespace';
PROGRAM_KW: 'program';
PROPERTY_KW: 'property';
TYPE_KW: 'type';
VAL_KW: 'val';
HAS_KW: 'has';
ABSTRACT_KW: 'abstract';
SHARED_KW: 'shared';

LPAREN: '(';
RPAREN: ')';
LBRACKET: '{';
RBRACKET: '}';
LSQUARE: '[';
RSQUARE: ']';
COMMA: ',';
POINT: '.';
COLON: ':';
EQUAL: '==';
ASSIGNMENT: '=';

ID: F_ID;
// Only for types
TID: F_TID;
INT: F_INT;

STRING_START: '"' -> pushMode(IN_STRING);

WS: (' ' | '\t')+ -> channel(WHITESPACE);
NL: '\r'? '\n';

COMMENT
    :   '/*' .*? '*/' -> channel(COMMENTS)
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> channel(COMMENTS)
    ;

fragment F_ID: ('_')*'a'..'z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
// Only for types
fragment F_TID: ('_')*'A'..'Z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
fragment F_INT: '0'|(('1'..'9')('0'..'9')*);


mode IN_STRING;

STRING_STOP         : '"' -> popMode;
STRING_CONTENT      : (~["\\#]|ESCAPE_SEQUENCE|SHARP)+;
INTERPOLATION_START : '#{' -> pushMode(IN_INTERPOLATION);

fragment ESCAPE_SEQUENCE : '\\r'|'\\n'|'\\t'|'\\"'|'\\\\';
fragment SHARP : '#'{ _input.LA(1)!='{' }?;

mode IN_INTERPOLATION;

INTERPOLATION_END : '}' -> popMode;
I_ID: F_ID   -> type(ID);
I_TID: F_TID -> type(TID);
I_INT: F_INT -> type(INT);
I_COMMA   : ',' -> type(COMMA);
I_LPAREN  : '(' -> type(LPAREN);
I_RPAREN  : ')' -> type(RPAREN);
I_LSQUARE : '[' -> type(LSQUARE);
I_RSQUARE : ']' -> type(RSQUARE);

I_POINT : '.' -> type(POINT);
I_EQUAL : '==' -> type(EQUAL);
I_STRING_START : '"' -> type(STRING_START), pushMode(IN_STRING);
I_WS: (' ' | '\t')+ -> type(WS), channel(WHITESPACE);


