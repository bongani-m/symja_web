package org.matheclipse.gwt.server;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.text.StringEscapeUtils;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.MathMLUtilities;
import org.matheclipse.core.graphics.Show2SVG;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSONBuilder {

  public static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

  public static String createJSONErrorString(String str) {
    ObjectNode outJSON = JSON_OBJECT_MAPPER.createObjectNode();
    outJSON.put("prefix", "Error");
    outJSON.put("message", Boolean.TRUE);
    outJSON.put("tag", "syntax");
    outJSON.put("symbol", "General");
    str = StringEscapeUtils.escapeHtml4(str);
    outJSON.put("text", "<math><mrow><mtext>" + str + "</mtext></mrow></math>");

    ObjectNode resultsJSON = JSON_OBJECT_MAPPER.createObjectNode();
    resultsJSON.putNull("line");
    resultsJSON.putNull("result");

    ArrayNode temp = JSON_OBJECT_MAPPER.createArrayNode();
    temp.add(outJSON);
    resultsJSON.putPOJO("out", temp);

    temp = JSON_OBJECT_MAPPER.createArrayNode();
    temp.add(resultsJSON);
    ObjectNode json = JSON_OBJECT_MAPPER.createObjectNode();
    json.putPOJO("results", temp);

    return json.toString();
  }

  public static String[] createJSONError(String str) {
    return new String[] {"error", createJSONErrorString(str)};
  }

  /**
   * Pprint a syntax error message.
   *
   * @param str
   * @return
   */
  public static String createJSONSyntaxErrorString(String str) {
    ObjectNode outJSON = JSON_OBJECT_MAPPER.createObjectNode();
    outJSON.put("prefix", "Error");
    outJSON.put("message", Boolean.TRUE);
    outJSON.put("tag", "syntax");
    outJSON.put("symbol", "Syntax");
    str = StringEscapeUtils.escapeHtml4(str);
    str = str.replace(" ", "&nbsp;");
    String[] strs = str.split("\\n");
    StringBuilder mtext = new StringBuilder();
    for (int i = 0; i < strs.length; i++) {
      mtext.append("<mtext mathvariant=\"courier\">");
      mtext.append(strs[i]);
      mtext.append("</mtext>");
      if (i < strs.length - 1) {
        mtext.append("<mspace linebreak='newline' />");
      }
    }
    outJSON.put("text", "<math><mrow>" + mtext + "</mrow></math>");

    ObjectNode resultsJSON = JSON_OBJECT_MAPPER.createObjectNode();
    resultsJSON.putNull("line");
    resultsJSON.putNull("result");

    ArrayNode temp = JSON_OBJECT_MAPPER.createArrayNode();
    temp.add(outJSON);
    resultsJSON.putPOJO("out", temp);

    temp = JSON_OBJECT_MAPPER.createArrayNode();
    temp.add(resultsJSON);
    ObjectNode json = JSON_OBJECT_MAPPER.createObjectNode();
    json.putPOJO("results", temp);

    return json.toString();
  }

  public static String[] createJSONSyntaxError(String str) {
    return new String[] {"error", createJSONSyntaxErrorString(str)};
  }

  public static String[] createJSONJavaScript(String script) throws IOException {

    ObjectNode resultsJSON = JSON_OBJECT_MAPPER.createObjectNode();
    resultsJSON.put("line", Integer.valueOf(21));
    resultsJSON.put("result", script);

    ArrayNode temp = JSON_OBJECT_MAPPER.createArrayNode();
    resultsJSON.putPOJO("out", temp);

    temp = JSON_OBJECT_MAPPER.createArrayNode();
    temp.add(resultsJSON);
    ObjectNode json = JSON_OBJECT_MAPPER.createObjectNode();
    json.putPOJO("results", temp);

    return new String[] {"mathml", json.toString()};
  }

  public static String[] createJSONShow(EvalEngine engine, IAST show) throws IOException {
    StringBuilder stw = new StringBuilder();
    stw.append("<math><mtable><mtr><mtd>");
    Show2SVG.toSVG(show, stw);
    stw.append("</mtd></mtr></mtable></math>");

    ObjectNode resultsJSON = JSON_OBJECT_MAPPER.createObjectNode();
    resultsJSON.put("line", Integer.valueOf(21));
    resultsJSON.put("result", stw.toString());
    ArrayNode temp = JSON_OBJECT_MAPPER.createArrayNode();
    resultsJSON.putPOJO("out", temp);

    temp = JSON_OBJECT_MAPPER.createArrayNode();
    temp.add(resultsJSON);
    ObjectNode json = JSON_OBJECT_MAPPER.createObjectNode();
    json.putPOJO("results", temp);

    return new String[] {"mathml", json.toString()};
  }

  public static String[] createJSONResult(
      EvalEngine engine, IExpr outExpr, StringWriter outWriter, StringWriter errorWriter) {
    // DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
    // DecimalFormat decimalFormat = new DecimalFormat("0.0####", otherSymbols);
    MathMLUtilities mathUtil = new MathMLUtilities(engine, false, false);
    StringWriter stw = new StringWriter();
    if (!mathUtil.toMathML(outExpr, stw, true)) {
      return createJSONError("Max. output size exceeded " + Config.MAX_OUTPUT_SIZE);
    }

    ObjectNode resultsJSON = JSON_OBJECT_MAPPER.createObjectNode();
    resultsJSON.put("line", Integer.valueOf(21));
    resultsJSON.put("result", stw.toString());
    ArrayNode temp = JSON_OBJECT_MAPPER.createArrayNode();

    String message = errorWriter.toString();
    if (message.length() > 0) {
      ObjectNode messageJSON = JSON_OBJECT_MAPPER.createObjectNode();
      messageJSON.put("prefix", "Error");
      messageJSON.put("message", Boolean.TRUE);
      messageJSON.put("tag", "evaluation");
      messageJSON.put("symbol", "General");
      messageJSON.put("text", "<math><mrow><mtext>" + message + "</mtext></mrow></math>");
      temp.add(messageJSON);
    }

    message = outWriter.toString();
    if (message.length() > 0) {
      ObjectNode messageJSON = JSON_OBJECT_MAPPER.createObjectNode();
      messageJSON.put("prefix", "Output");
      messageJSON.put("message", Boolean.TRUE);
      messageJSON.put("tag", "evaluation");
      messageJSON.put("symbol", "General");
      messageJSON.put("text", "<math><mrow><mtext>" + message + "</mtext></mrow></math>");
      temp.add(messageJSON);
    }
    resultsJSON.putPOJO("out", temp);

    temp = JSON_OBJECT_MAPPER.createArrayNode();
    temp.add(resultsJSON);
    ObjectNode json = JSON_OBJECT_MAPPER.createObjectNode();
    json.putPOJO("results", temp);

    return new String[] {"mathml", json.toString()};
  }
}
