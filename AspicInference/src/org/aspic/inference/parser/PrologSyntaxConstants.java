/* Generated By:JavaCC: Do not edit this line. PrologSyntaxConstants.java */
package org.aspic.inference.parser;

public interface PrologSyntaxConstants {

  int EOF = 0;
  int SINGLE_LINE_COMMENT = 8;
  int PLUS = 9;
  int MINUS = 10;
  int TIMES = 11;
  int DIVIDE = 12;
  int GT = 13;
  int LT = 14;
  int NEG = 15;
  int ABW = 16;
  int GTE = 17;
  int LTE = 18;
  int IS = 19;
  int EQ = 20;
  int NEQ = 21;
  int AEQ = 22;
  int ANEQ = 23;
  int NONVAR = 24;
  int NAF = 25;
  int IDN = 26;
  int VAR = 27;
  int INT = 28;
  int FLOAT = 29;
  int CAPTION = 30;

  int DEFAULT = 0;
  int WithinMultiLineComment = 1;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\n\"",
    "\"\\t\"",
    "\"\\r\"",
    "\"/*\"",
    "\"*/\"",
    "<token of kind 7>",
    "<SINGLE_LINE_COMMENT>",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\">\"",
    "\"<\"",
    "\"~\"",
    "\"<-\"",
    "\">=\"",
    "\"=<\"",
    "\"is\"",
    "\"==\"",
    "\"\\\\==\"",
    "\"=:=\"",
    "\"=\\\\=\"",
    "\"nonvar\"",
    "\"\\\\+\"",
    "<IDN>",
    "<VAR>",
    "<INT>",
    "<FLOAT>",
    "<CAPTION>",
    "\"[\"",
    "\"]\"",
    "\".\"",
    "\"(\"",
    "\",\"",
    "\")\"",
  };

}
