lexer grammar TurinLexer;

@header {

}

@lexer::members {

    public static final int WHITESPACE = 1;
    public static final int COMMENTS = 2;

    private boolean useHiddenChannel = false;

    public void setUseHiddenChannel(boolean value) {
        this.useHiddenChannel = value;
    }

    public int whitespaceChannel() {
        if (useHiddenChannel) {
            return HIDDEN;
        } else {
            return WHITESPACE;
        }
    }

    public int commentsChannel() {
        if (useHiddenChannel) {
            return HIDDEN;
        } else {
            return COMMENTS;
        }
    }
}

// It is suggested to define the token types reused in different mode.
// See mode in-interpolation below
tokens { NAME_PLACEHOLDER, PLACEHOLDER, VALUE_ID, TYPE_ID, INT, LPAREN, RPAREN, COMMA, RELOP, AND_KW, OR_KW, NOT_KW }

NAMESPACE_KW        : 'namespace';
PROGRAM_KW          : 'program';
PROPERTY_KW         : 'property';
DEFAULT_KW          : 'default';
TYPE_KW             : 'type';
VAL_KW              : 'val';
HAS_KW              : 'has';
ABSTRACT_KW         : 'abstract';
SHARED_KW           : 'shared';
IMPORT_KW           : 'import';
AS_KW               : 'as';
VOID_KW             : 'void';
RETURN_KW           : 'return';
FALSE_KW            : 'false';
TRUE_KW             : 'true';
IF_KW               : 'if';
ELIF_KW             : 'elif';
ELSE_KW             : 'else';

THROW_KW            : 'throw';
TRY_KW              : 'try';
CATCH_KW            : 'catch';

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
PIPE                : '|';

NAME_PLACEHOLDER    : F_NAME_PLACEHOLDER;
PLACEHOLDER         : F_PLACEHOLDER;

PRIMITIVE_TYPE      : F_PRIMITIVE_TYPE;
BASIC_TYPE          : F_BASIC_TYPE;

VALUE_ID            : F_VALUE_ID | F_ESCAPED_VALUE_ID;
// Only for types
TYPE_ID             : F_TYPE_ID | F_ESCAPED_TYPE_ID;
INT                 : F_INT;

STRING_START        : '"' -> pushMode(IN_STRING);

WS                  : (' ' | '\t')+ -> channel(WHITESPACE);
NL                  : '\r'? '\n';

COMMENT             : '/*' .*? '*/' -> channel(COMMENTS);

LINE_COMMENT        : '//' ~[\r\n]* -> channel(COMMENTS);

ANNOTATION_ID       : F_ANNOTATION_ID;

UNEXPECTED_CHAR     : . ;

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
I_NAME_PLACEHOLDER  : F_NAME_PLACEHOLDER -> type(NAME_PLACEHOLDER);
I_PLACEHOLDER       : F_PLACEHOLDER -> type(PLACEHOLDER);
I_VALUE_ID          : (F_VALUE_ID | F_ESCAPED_VALUE_ID) -> type(VALUE_ID);
I_TYPE_ID           : (F_TYPE_ID  | F_ESCAPED_TYPE_ID)  -> type(TYPE_ID);
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
I_UNEXPECTED_CHAR   : . -> type(UNEXPECTED_CHAR);

fragment F_NAME_PLACEHOLDER  : '_name';
fragment F_PLACEHOLDER       : '_';
fragment F_AND               : 'and';
fragment F_OR                : 'or';
fragment F_NOT               : 'not';
fragment F_VALUE_ID          : ('_')*'a'..'z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
fragment F_ESCAPED_VALUE_ID  : 'v#'('_')*('A'..'Z' | 'a'..'z') ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
// Only for types
fragment F_TYPE_ID           : ('_')*'A'..'Z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
fragment F_ESCAPED_TYPE_ID   : 'T#'('_')*('A'..'Z' | 'a'..'z') ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
fragment F_ANNOTATION_ID     : '@'('_')*('A'..'Z' | 'a'..'z' | '0'..'9' | '_')*;
fragment F_INT               : ('-')?('0'|(('1'..'9')('0'..'9')*));
fragment F_PRIMITIVE_TYPE    : 'byte'|'int'|'long'|'boolean'|'char'|'float'|'double'|'short';
fragment F_BASIC_TYPE        : 'uint'|'ulong'|'ufloat'|'udouble'|'ushort'|'ubyte';

fragment ESCAPE_SEQUENCE     : '\\r'|'\\n'|'\\t'|'\\"'|'\\\\';
fragment SHARP               : '#'{ _input.LA(1)!='{' }?;
