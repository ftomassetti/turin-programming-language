parser grammar TurinParser;

options {   tokenVocab = TurinLexer; }

@header {

}

turinFile:
    namespace=namespaceDecl nls
    (imports+=importDeclaration)*
    (members+=fileMember)*
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
    IMPORT_KW packagePart=qualifiedId POINT typeName=TID (AS_KW alternativeName=ID)? nls ;

singleFieldImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TID POINT fieldName=qualifiedId (AS_KW alternativeName=ID)? nls;

allFieldsImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TID POINT ASTERISK nls;

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

