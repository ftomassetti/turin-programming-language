grammar Turin;

@header {

}

turinFile:
    namespace=namespaceDecl nls
    (members=fileMember)+
    EOF;

// Base

nls: NL+;

qualifiedId:
    (parts+=ID '.')* parts+=ID;

//

namespaceDecl:
    'namespace' name=qualifiedId;

//

typeUsage:
    ref=TID
    | arrayBase=typeUsage '[]';

//

propertyDeclaration:
    'property' type=typeUsage ':' name=ID nls;

propertyReference:
    'property' name=ID nls;

typeMember:
    propertyDeclaration | propertyReference;

typeDeclaration:
    'type' name=TID '{' nls
    (typeMembers += typeMember)*
    '}' nls;

//

actualParam:
    expression | name='ID' '=' expression;

functionCall:
    name=ID '(' params+=actualParam (',' params+=actualParam)*  ')' ;

expression:
    functionCall | creation | stringLiteral | intLiteral;

stringLiteral:
    STRING_START -> pushMode(IN_STRING);

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
    name=TID '(' params+=actualParam (',' params+=actualParam)*  ')' ;

varDecl :
    'val' (type=typeUsage ':')? name=ID '=' value=expression nls;

expressionStmt:
    expression nls;

statement :
    varDecl | expressionStmt ;
//

formalParam :
    type=typeUsage name=ID;
//

program:
    'program' name=TID '(' params+=formalParam (',' params+=formalParam)* ')' '{' nls
    (statements += statement)*
    '}' nls;

//

fileMember:
    propertyDeclaration | typeDeclaration | program;

ID: 'a'..'z' ('A'..'Z' | 'a'..'z' | '_')*;
// Only for types
TID: 'A'..'Z' ('A'..'Z' | 'a'..'z' | '_')*;
INT: '0'|('1'..'9')('0'..'9')*;

STRING:  '"' ~["]* '"';
STRING_START: '"';

WS: (' ' | '\t')+ -> skip;
NL: '\r'? '\n';

mode IN_STRING;