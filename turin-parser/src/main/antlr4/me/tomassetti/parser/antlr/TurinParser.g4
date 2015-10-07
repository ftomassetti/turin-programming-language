parser grammar TurinParser;

options {   tokenVocab = TurinLexer; }

@header {

}

//
// File
//

turinFile:
    namespace=namespaceDecl nls
    (imports+=importDeclaration)*
    (members+=fileMember)*
    EOF;

//
// Base
//

nls: NL+;

qualifiedId:
    (parts+=VALUE_ID POINT)* parts+=VALUE_ID;

//
// Namespace
//

namespaceDecl:
    NAMESPACE_KW name=qualifiedId;

//
// Imports
//

importDeclaration:
    typeImportDeclaration |
    singleFieldImportDeclaration |
    allFieldsImportDeclaration |
    allPackageImportDeclaration;

// e.g., import java.util.Collections
// e.g., import java.util.Collections as Coll
typeImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TYPE_ID (AS_KW alternativeName=TYPE_ID)? nls ;

// e.g., import java.lang.System.out
// e.g., import java.lang.System.out.println
// e.g., import java.lang.System.out.println as print
singleFieldImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TYPE_ID POINT fieldName=qualifiedId (AS_KW alternativeName=VALUE_ID)? nls;

// e.g., import java.lang.System.*
allFieldsImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT typeName=TYPE_ID POINT ASTERISK nls;

// e.g., import java.util.*
allPackageImportDeclaration:
    IMPORT_KW packagePart=qualifiedId POINT ASTERISK nls;

//
// Annotations
//

annotationUsage:
    annotation=ANNOTATION_ID;

//

typeUsage:
    ref=typeReference (LSQUARE typeParams+=typeUsage (COMMA typeParams+=typeUsage)* RSQUARE)?
    | arrayBase=typeUsage LSQUARE RSQUARE
    | primitiveType = PRIMITIVE_TYPE
    | basicType = BASIC_TYPE;

// we permit to insert a newline after the comma to break long lists
commaNl:
    COMMA NL?;

//
// method definition
//

methodDefinition:
    type=returnType name=VALUE_ID LPAREN (params+=formalParam (commaNl  params+=formalParam)*)? RPAREN methodBody;

returnType:
    isVoid=VOID_KW | type=typeUsage;

methodBody:
    ASSIGNMENT value=expression nls | LBRACKET nls (statements += statement)* RBRACKET nls;

//
// contructor definition
//

constructorDefinition:
    INIT_KW LPAREN (params+=formalParam (commaNl  params+=formalParam)*)? RPAREN (nls)?
    (SUPER_KW LPAREN (asterisk=ASTERISK | (superParams+=actualParam (commaNl superParams+=actualParam)*) )RPAREN)?
    LBRACKET nls (statements += statement)* RBRACKET nls;

//

topLevelPropertyDeclaration:
    PROPERTY_KW (type=typeUsage)? name=VALUE_ID (DEFAULT_KW defaultValue=expression |ASSIGNMENT initialValue=expression)?
    (NL? COLON constraint+=constraintDeclaration (commaNl constraint+=constraintDeclaration)*)? nls;

topLevelFunctionDeclaration:
    (annotations+=annotationUsage nls)*
    type=returnType name=VALUE_ID LPAREN (params+=formalParam (commaNl  params+=formalParam)*)? RPAREN methodBody;

constraintDeclaration:
    condition=expression (PIPE message=expression)?;

inTypePropertyDeclaration:
    (type=typeUsage)? name=VALUE_ID (DEFAULT_KW defaultValue=expression |ASSIGNMENT initialValue=expression)?
    (NL? COLON constraint+=constraintDeclaration (commaNl constraint+=constraintDeclaration)*)? nls;

propertyReference:
    HAS_KW name=VALUE_ID nls;

typeMember:
    inTypePropertyDeclaration
    | propertyReference
    | methodDefinition
    | constructorDefinition;

typeDeclaration:
    (annotations+=annotationUsage nls)*
    TYPE_KW name=TYPE_ID
        (EXTENDS_KW baseType=typeUsage)?
        (IMPLEMENTS_KW interfaze+=typeUsage (commaNl interfaze+=typeUsage)*)?
        LBRACKET nls
    (typeMembers += typeMember)*
    RBRACKET nls;

//

actualParam:
    expression | name=VALUE_ID ASSIGNMENT expression | asterisk=ASTERISK ASSIGNMENT expression;

parenExpression:
    LPAREN internal=expression RPAREN;

placeholderUsage:
    PLACEHOLDER;

placeholderNameUsage:
    NAME_PLACEHOLDER;

thisReference:
    THIS_KW;

basicExpression:
    booleanLiteral | stringLiteral | interpolatedStringLiteral
    | byteLiteral | shortLiteral | intLiteral | longLiteral
    | floatLiteral | doubleLiteral
    | valueReference | parenExpression | staticFieldReference
    | placeholderUsage | placeholderNameUsage
    | thisReference;

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
    | left=expression relOp=RELOP           right=expression
    | left=expression logicOperator=AND_KW  right=expression
    | left=expression logicOperator=OR_KW   right=expression
    | left=expression isAssignment=ASSIGNMENT right=expression
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
    STRING_CONTENT | stringInterpolationElement | ESCAPE_SEQUENCE;

stringInterpolationElement:
   INTERPOLATION_START value=expression INTERPOLATION_END;

byteLiteral:
    BYTE;

shortLiteral:
    SHORT;

intLiteral:
    INT;

longLiteral:
    LONG;

floatLiteral:
    FLOAT;

doubleLiteral:
    DOUBLE;

creation:
    ref=typeUsage LPAREN (params+=actualParam (commaNl params+=actualParam)*)?  RPAREN ;

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

assignment:
    target=expression ASSIGNMENT value=expression;

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

