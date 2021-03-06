grammar Switchboard;

statement : input+ ('|' expression)* output+ ;

input: 'FROM' text;
output: '|' 'TO' text;

expression : expression (AND | OR | NOT) expression # logicalAssociatedExpression
 | predicate # expressionPredicate
 | '(' expression ')' # groupedExpression
 ;

predicate : text (EQUALS | NOTEQUALS | GREQUALS | LSEQUALS | GREATERTHAN | LESSTHAN) text ;

text : NUMBER # numberText | QTEXT # quotedText | UQTEXT # unquotedText ;

AND : 'AND' ;
OR : 'OR' ;
NOT : 'NOT' ;
EQUALS : '=' ;
NOTEQUALS : '!=' ;
GREQUALS : '>=' ;
LSEQUALS : '<=' ;
GREATERTHAN : '>' ;
LESSTHAN : '<' ;

NUMBER : DIGIT+ | DIGIT+ '.' DIGIT+ | '.' DIGIT+;
QTEXT : '"' (ESC|.)*? '"' ;
UQTEXT : ~[ ()=,<>!\r\n]+ ;

fragment
DIGIT : [0-9] ;
fragment
ESC : '\\"' | '\\\\' ;

WS : [ \t\r\n]+ -> skip ;