lexer grammar TurinLexer;

@header {

}

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

ID: ('_')*'a'..'z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
// Only for types
TID: ('_')*'A'..'Z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
INT: '0'|(('1'..'9')('0'..'9')*);

STRING_START: '"' -> pushMode(IN_STRING);

WS: (' ' | '\t')+ -> skip;
NL: '\r'? '\n';

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;

mode IN_STRING;

STRING_STOP         : '"' -> popMode;
STRING_CONTENT      : (~["\\#]|'\\r'|'\\n'|'\\t'|'\\"'|'\\\\'|'#'{ _input.LA(2)!='{' }?)+;
INTERPOLATION_START : '#{' -> pushMode(IN_INTERPOLATION);

mode IN_INTERPOLATION;

INTERPOLATION_END : '}';


