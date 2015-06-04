package com.cyc.tool.kbtaxonomy.viewer;

/*
 * #%L
 * KBTaxonomyViewer2015
 * %%
 * Copyright (C) 2015 Cycorp, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static com.cyc.tool.kbtaxonomy.viewer.ResourceServer.RESOURCE_BASE;
import static com.cyc.tool.kbtaxonomy.viewer.WebParams.CONCEPT_DETAIL_ENABLED;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * <p>
 * Methods for accessing JavaScript files for graphs.
 *
 */
public class JavascriptGraphs {

  private static boolean allowCircles = false;
  private static List<String> graphTypes = new ArrayList<>();
  private final static Map<String, String> javaScripts = new LinkedHashMap<>();
  private static final List<String> jsLibURIs = Arrays.asList(
//          "http://d3js.org/d3.v3.min.js",
//          "https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js",
          "https://code.jquery.com/jquery-1.10.2.js",
          getResourceURI("d3.min.js"),
          getResourceURI("d3-context-menu.js"),
          getResourceURI("ConceptDetailViewer.js")
  );

  //private static final String styleString = readStylesCss();
  private static final Map<String, Boolean> wroteScript = new HashMap<>();
  static {
    //graphTypes.add("ClusterDendogram");
    graphTypes.add("CollapsibleLinear");
    //graphTypes.add("Linear");
    graphTypes.forEach(gt -> {
      String n = gt + "Graph";
      javaScripts.putIfAbsent(n, readGraphJS(n));
    });
  }

  /**
   *
   * @return html for the viewer
   */
  public static String gatherButton() {
    //there is a argument for this method going in the jscript class
    return "<button onclick='gatherSelectedIDs();'>SAVE</button>";
  }
  
  /**
   *
   * @return html for the viewer
   */
  public static String clearButton() {
   return "<button onclick='clearLists();'>RESET</button>"; 
  }
  
  /**
   *
   * @return html for the viewer
   */
  public static String xmlButton() {
    return "<button onclick='xmlQuery();'>GET XML QUERY</button>";
  }

  /**
   *
   * @return allowCircles
   */
  public static boolean isAllowCircles() {
    return allowCircles;
  }

  /**
   *
   * @param allowCircles
   */
  public static void setAllowCircles(boolean allowCircles) {
    JavascriptGraphs.allowCircles = allowCircles;
  }
  
  /**
   *
   * @param params
   * @return a String
   */
  public static String jsPackages(WebParams params) {
    final StringBuilder builder = new StringBuilder()
            
            // JS variable uses same name as web param, to minimize possibility of confusion. - nwinant, 2015-02-23
            .append("<script>\n")
            .append("var " + CONCEPT_DETAIL_ENABLED + "=" + params.isConceptDetailEnabled()+ ";\n")
            .append("</script>\n");
    
    for (String jsLibURI : jsLibURIs) {
      builder.append(getJsUriReference(jsLibURI));
    }
    for (String gt : javaScripts.keySet()) {
      builder.append(getJsUriReference(getResourceURI(gt + ".js")));
    }
    builder.append(getJsUriReference(getResourceURI("ConceptClickHandlers.js")));
    return builder.toString();
  }
  
  private static String getJsUriReference(String uri) {
    return "<script src=\"" + uri +"\" charset=\"utf-8\"></script>\n";
  }
  
  private static String getResourceURI(String filename) {
    return RESOURCE_BASE + "/" + filename;
  }
  
  static String cssLinks() {
    return "<link href=\"" + getResourceURI("ViewerStyles.css") + "\" media=\"all\" rel=\"stylesheet\" />\n" +
            "<link href=\"" + getResourceURI("d3-context-menu.css") + "\" media=\"all\" rel=\"stylesheet\" />\n";
  }
  
  static String readGraphJS(String graphType) {
    try {
      InputStream stream = JavascriptGraphs.class.getClassLoader().getResourceAsStream(graphType + ".js");
      String programString = IOUtils.toString(stream, "UTF-8");
      wroteScript.putIfAbsent(graphType, false);
      return programString;
    } catch (IOException ex) {
      Logger.getLogger(JavascriptGraphs.class.getName()).log(Level.SEVERE, null, ex);
      System.out.println("Failed to  read graphing " + graphType + " JavaScript");
      return null;
    }
  }
  
}
