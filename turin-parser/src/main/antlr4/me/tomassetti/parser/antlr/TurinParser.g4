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
    typeImportDeclaration | singleFieldImportDeclaration | allFieldsImportDeclaration | allPackageImportDeclaration;

typeImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TID (AS_KW alternativeName=TID)? nls ;

singleFieldImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TID POINT fieldName=qualifiedId (AS_KW alternativeName=ID)? nls;

allFieldsImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TID POINT ASTERISK nls;

allPackageImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT ASTERISK nls;

//

typeUsage:
    ref=TID
    | arrayBase=typeUsage LSQUARE RSQUARE
    | primitiveType = PRIMITIVE_TYPE
    | basicType = BASIC_TYPE;

commaNl:
    COMMA NL?;

// method definition

methodDefinition:
    type=returnType name=ID LPAREN (params+=formalParam (commaNl  params+=formalParam)*)? RPAREN methodBody;

returnType:
    isVoid=VOID_KW | type=typeUsage;

methodBody:
    ASSIGNMENT value=expression nls | LBRACKET nls (statements += statement)* RBRACKET nls;

//

topLevelPropertyDeclaration:
    PROPERTY_KW type=typeUsage COLON name=ID nls;

inTypePropertyDeclaration:
    HAS_KW type=typeUsage COLON name=ID nls;

propertyReference:
    HAS_KW name=ID nls;

typeMember:
    inTypePropertyDeclaration | propertyReference | methodDefinition;

typeDeclaration:
    TYPE_KW name=TID LBRACKET nls
    (typeMembers += typeMember)*
    RBRACKET nls;

//

actualParam:
    expression | name=ID ASSIGNMENT expression;

parenExpression:
    LPAREN internal=expression RPAREN;

basicExpression:
    booleanLiteral | stringLiteral | intLiteral | interpolatedStringLiteral | valueReference | parenExpression;

booleanLiteral:
    negative=FALSE_KW | positive=TRUE_KW;

relOperator:
    EQUAL | DIFFERENT | LESSEQ | LESS | MOREEQ | MORE;

expression:
    invokation | creation | basicExpression | fieldAccess | staticFieldReference
    | left=expression mathOperator=ASTERISK right=expression
    | left=expression mathOperator=SLASH    right=expression
    | left=expression mathOperator=PLUS     right=expression
    | left=expression mathOperator=MINUS    right=expression
    | left=expression logicOperator=AND_KW  right=expression
    | left=expression logicOperator=OR_KW   right=expression
    | left=expression relOp=relOperator     right=expression
    | not=NOT_KW value=expression
    ;

invokation:
    function=basicExpression LPAREN (params+=actualParam (commaNl params+=actualParam)*)?  RPAREN ;

fieldAccess:
    subject=basicExpression POINT name=ID;

staticFieldReference:
    typeReference POINT name=ID;

valueReference:
    name=ID;

typeReference:
    (packag=qualifiedId)? POINT name=TID;

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
    (pakage=qualifiedId POINT)? name=TID LPAREN (params+=actualParam (commaNl params+=actualParam)*)?  RPAREN ;

varDecl :
    VAL_KW (type=typeUsage)? name=ID ASSIGNMENT value=expression nls;

expressionStmt:
    expression nls;

returnStmt:
    RETURN_KW value=expression nls;

statement :
    varDecl | expressionStmt | returnStmt;
//

formalParam :
    type=typeUsage name=ID;
//

program:
    PROGRAM_KW name=TID LPAREN params+=formalParam (commaNl params+=formalParam)* RPAREN LBRACKET nls
    (statements += statement)*
    RBRACKET nls;

//

fileMember:
    topLevelPropertyDeclaration | typeDeclaration | program;

