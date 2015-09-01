grammar TurinParser;

@header {

}

turinFile:
    namespace=namespaceDecl nls
    (imports+=importDeclaration)*
    (members+=fileMember)+
    EOF;

// Base

nls: NL+;

qualifiedId:
    (parts+=ID POINT)* parts+=ID;

// Namespace

namespaceDecl:
    NAMESPACE_KW name=qualifiedId;

// Imports

importDeclaration:
    typeImportDeclaration | singleFieldImportDeclaration | allFieldsImportDeclaration;

typeImportDeclaration:
    packagePart=qualifiedId POINT typeName=ID nls;

singleFieldImportDeclaration:
    packagePart=qualifiedId POINT typeName=ID POINT fieldName=ID nls;

allFieldsImportDeclaration:
    packagePart=qualifiedId POINT typeName=ID POINT ASTERISK nls;

//

typeUsage:
    ref=TID
    | arrayBase=typeUsage LSQUARE RSQUARE;

//

topLevelPropertyDeclaration:
    PROPERTY_KW type=typeUsage COLON name=ID nls;

inTypePropertyDeclaration:
    HAS_KW type=typeUsage COLON name=ID nls;

propertyReference:
    HAS_KW name=ID nls;

typeMember:
    inTypePropertyDeclaration | propertyReference;

typeDeclaration:
    TYPE_KW name=TID LBRACKET nls
    (typeMembers += typeMember)*
    RBRACKET nls;

//

actualParam:
    expression | name=ID ASSIGNMENT expression;

functionCall:
    name=ID LPAREN params+=actualParam (COMMA params+=actualParam)*  RPAREN ;

expression:
    functionCall | creation | stringLiteral | intLiteral;

stringLiteral:
    STRING;

intLiteral:
    INT;

fragment
StringCharacters
	:	StringCharacter+
	;
fragment
StringCharacter
	:	~["]
	;

creation:
    name=TID LPAREN params+=actualParam (COMMA params+=actualParam)*  RPAREN ;

varDecl :
    VAL_KW (type=typeUsage COLON)? name=ID ASSIGNMENT value=expression nls;

expressionStmt:
    expression nls;

statement :
    varDecl | expressionStmt ;
//

formalParam :
    type=typeUsage name=ID;
//

program:
    PROGRAM_KW name=TID LPAREN params+=formalParam (COMMA params+=formalParam)* RPAREN LBRACKET nls
    (statements += statement)*
    RBRACKET nls;

//

fileMember:
    topLevelPropertyDeclaration | typeDeclaration | program;

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

ID: 'a'..'z' ('A'..'Z' | 'a'..'z' | '_')*;
// Only for types
TID: 'A'..'Z' ('A'..'Z' | 'a'..'z' | '_')*;
INT: '0'|('1'..'9')('0'..'9')*;

STRING:  '"' ~["]* '"';
STRING_START: '"';

WS: (' ' | '\t')+ -> skip;
NL: '\r'? '\n';

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;
