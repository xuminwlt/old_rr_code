/* Generated By:JavaCC: Do not edit this line. QueryParserConstants.java */
package com.renren.entrybase.model.query.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface QueryParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int LINE_COMMENT = 5;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMENT = 6;
  /** RegularExpression Id. */
  int K_AND = 7;
  /** RegularExpression Id. */
  int K_OR = 8;
  /** RegularExpression Id. */
  int FIELD_NAME = 9;
  /** RegularExpression Id. */
  int QUOTED_IDENTIFIER = 10;
  /** RegularExpression Id. */
  int FIELD_NUMBER_VALUES = 11;
  /** RegularExpression Id. */
  int NUMBER = 12;
  /** RegularExpression Id. */
  int LETTER = 13;
  /** RegularExpression Id. */
  int DIGIT = 14;
  /** RegularExpression Id. */
  int SPECIAL_CHARS = 15;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\r\"",
    "\"\\n\"",
    "<LINE_COMMENT>",
    "<MULTI_LINE_COMMENT>",
    "\"AND\"",
    "\"OR\"",
    "<FIELD_NAME>",
    "<QUOTED_IDENTIFIER>",
    "<FIELD_NUMBER_VALUES>",
    "<NUMBER>",
    "<LETTER>",
    "<DIGIT>",
    "\"_\"",
    "\";\"",
    "\":\"",
    "\"(\"",
    "\")\"",
  };

}
