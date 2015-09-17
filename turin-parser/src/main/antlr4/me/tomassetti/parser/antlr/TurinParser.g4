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
    (parts+=VALUE_ID POINT)* parts+=VALUE_ID;

// Namespace

namespaceDecl:
    NAMESPACE_KW name=qualifiedId;

// Imports

importDeclaration:
    typeImportDeclaration | singleFieldImportDeclaration | allFieldsImportDeclaration | allPackageImportDeclaration;

typeImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TYPE_ID (AS_KW alternativeName=TYPE_ID)? nls ;

singleFieldImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TYPE_ID POINT fieldName=qualifiedId (AS_KW alternativeName=VALUE_ID)? nls;

allFieldsImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TYPE_ID POINT ASTERISK nls;

allPackageImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT ASTERISK nls;

//

typeUsage:
    ref=TYPE_ID
    | arrayBase=typeUsage LSQUARE RSQUARE
    | primitiveType = PRIMITIVE_TYPE
    | basicType = BASIC_TYPE;

// we permit to insert a newline after the comma to break long lists
commaNl:
    COMMA NL?;

// method definition

methodDefinition:
    type=returnType name=VALUE_ID LPAREN (params+=formalParam (commaNl  params+=formalParam)*)? RPAREN methodBody;

returnType:
    isVoid=VOID_KW | type=typeUsage;

methodBody:
    ASSIGNMENT value=expression nls | LBRACKET nls (statements += statement)* RBRACKET nls;

//

topLevelPropertyDeclaration:
    PROPERTY_KW type=typeUsage COLON name=VALUE_ID nls;

topLevelFunctionDeclaration:
    type=returnType name=VALUE_ID LPAREN (params+=formalParam (commaNl  params+=formalParam)*)? RPAREN methodBody;

inTypePropertyDeclaration:
    HAS_KW type=typeUsage COLON name=VALUE_ID nls;

propertyReference:
    HAS_KW name=VALUE_ID nls;

typeMember:
    inTypePropertyDeclaration | propertyReference | methodDefinition;

typeDeclaration:
    TYPE_KW name=TYPE_ID LBRACKET nls
    (typeMembers += typeMember)*
    RBRACKET nls;

//

actualParam:
    expression | name=VALUE_ID ASSIGNMENT expression;

parenExpression:
    LPAREN internal=expression RPAREN;

basicExpression:
    booleanLiteral | stringLiteral | intLiteral | interpolatedStringLiteral | valueReference | parenExpression | staticFieldReference;

booleanLiteral:
    negative=FALSE_KW | positive=TRUE_KW;

expression:
    creation
    | basicExpression
    | container=expression POINT methodName=VALUE_ID LPAREN (params+=actualParam (commaNl params+=actualParam)*)? RPAREN
    | container=expression POINT fieldName=VALUE_ID
    | array=expression LSQUARE index=expression RSQUARE
    | function=expression LPAREN (params+=actualParam (commaNl params+=actualParam)*)? RPAREN
    | left=expression mathOperator=ASTERISK right=expression
    | left=expression mathOperator=SLASH    right=expression
    | left=expression mathOperator=PLUS     right=expression
    | left=expression mathOperator=MINUS    right=expression
    | left=expression logicOperator=AND_KW  right=expression
    | left=expression logicOperator=OR_KW   right=expression
    | left=expression relOp=RELOP           right=expression
    | not=NOT_KW value=expression
    ;

fieldAccess:
    subject=basicExpression POINT name=VALUE_ID;

staticFieldReference:
    typeReference POINT name=VALUE_ID;

valueReference:
    name=VALUE_ID;

typeReference:
    (packag=qualifiedId POINT)? name=TYPE_ID;

stringLiteral:
    STRING_START (content=STRING_CONTENT)? STRING_STOP;

interpolatedStringLiteral:
    STRING_START (elements+=stringElement)+ STRING_STOP;

stringElement:
    STRING_CONTENT | stringInterpolationElement;

stringInterpolationElement:
   INTERPOLATION_START value=expression INTERPOLATION_END;

intLiteral:
    INT;

creation:
    (pakage=qualifiedId POINT)? name=TYPE_ID LPAREN (params+=actualParam (commaNl params+=actualParam)*)?  RPAREN ;

varDecl:
    VAL_KW (type=typeUsage)? name=VALUE_ID ASSIGNMENT value=expression nls;

expressionStmt:
    expression nls;

returnStmt:
    RETURN_KW value=expression nls;

elifStmt:
    ELIF_KW condition=expression LBRACKET nls (body+=statement)* RBRACKET;

ifStmt:
    IF_KW condition=expression LBRACKET nls (ifBody+=statement)* RBRACKET
    (elifs+=elifStmt)*
    (ELSE_KW LBRACKET nls (elseBody+=statement)* RBRACKET)? nls;

throwStmt:
    THROW_KW exc=expression nls;

catchClause:
    CATCH_KW type=typeReference varName=VALUE_ID LBRACKET nls (body+=statement)*;

tryCatchStmt:
    TRY_KW LBRACKET nls
    (body+=statement)*
    (RBRACKET catches+=catchClause)+
    RBRACKET nls;

statement:
    varDecl | expressionStmt | returnStmt | ifStmt | throwStmt | tryCatchStmt;
//

formalParam:
    type=typeUsage name=VALUE_ID;
//

program:
    PROGRAM_KW name=TYPE_ID LPAREN params+=formalParam (commaNl params+=formalParam)* RPAREN LBRACKET nls
    (statements += statement)*
    RBRACKET nls;

//

fileMember:
    topLevelPropertyDeclaration | topLevelFunctionDeclaration | typeDeclaration | program;

