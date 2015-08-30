lexer grammar Turin;

@header {

}


NAMESPACE_KW      : 'namespace';
PROPERTY_KW       : 'property';
TYPE_KW           : 'type';
VAL_KW            : 'val';
PROGRAM_KW        : 'program';
ARRAY_DECORATOR   : '[]';
ID_TYPE_SEPARATOR : ':';
LCURLY            : '{';
RCURLY            : '}';
ASSIGNMENT        : '=';
COMMA             : ',';
LPAREN            : '(';
RPAREN            : ')';

ID: 'a'..'z' ('A'..'Z' | 'a'..'z' | '_')*;
// Only for types
TID: 'A'..'Z' ('A'..'Z' | 'a'..'z' | '_')*;
INT: '0'|('1'..'9')('0'..'9')*;

STRING_START: '"' -> pushMode(IN_STRING);

WS: (' ' | '\t')+ -> skip;
NL: '\r'? '\n';

mode IN_STRING;

STRING_STOP         : '"' -> popMode ;
ESCAPED_STRING_STOP : '\"';
ESCAPED_SLASH       : '\\';
CONTENT             : ~["\\#]+;
INTERPOLATION_START : '#{' -> pushMode(IN_INTERPOLATION);

mode IN_INTERPOLATION;

INTERPOLATION_END : '}'


