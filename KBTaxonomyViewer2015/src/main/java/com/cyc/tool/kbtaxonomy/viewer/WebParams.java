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

import java.util.Map;

/**
 * Web parameters.
 * 
 */
public class WebParams {
  
  public static final String CHILD_DATA = "childData";
  /**
   * Setting this param to true enables concept detail expansion.
   */
  public static final String CONCEPT_DETAIL_ENABLED = "conceptDetailEnabled";
  final static public String DIRECTION = "direction";
  final static public String FILE = "file";
  final static public String GRAPH_SHAPE = "graphShape";
  final static public String HEIGHT = "height";
  final static public String NODE_CONTENT = "nodeContent";

  final static public String SELECTED_CONCEPTS = "selectedConcepts";
  public static final String TERMS = "terms";

  protected final Map<String, String> paramMap;
  private final WebParamSerializer serializer;
    
  public WebParams(Map<String, String> paramMap){
    this.paramMap = paramMap;
    this.serializer = new WebParamSerializer(this);
  }
  
  public String getChildData() { return parseString(CHILD_DATA); }
  
  public String getDirection() { return parseString(DIRECTION); }
  
  public String getFile() { return parseString(FILE); }
  
  public String getGraphShape() { return parseString(GRAPH_SHAPE); }
  
  public int getHeight() { return parseInteger(HEIGHT, 1); }

  public String getNodeContent() { return parseString(NODE_CONTENT); }
  
  public String getSelectedConcepts() { return parseString(SELECTED_CONCEPTS); }

  public String getTerms() { return parseString(TERMS); }
      
  public boolean isConceptDetailEnabled() {
    return parseBoolean(CONCEPT_DETAIL_ENABLED, false);
  }
  
  public boolean isEmpty(String param) {
    return (paramMap.get(param) == null)
            || param.trim().isEmpty();
  }
  
  public WebParamSerializer serialize() {
    return this.serializer;
  }
  
  
  
  final protected boolean parseBoolean(String param, boolean defaultValue) {
    if (isEmpty(param)) {
      return defaultValue;
    }
    return Boolean.valueOf(parseString(param));
  }
  
  final protected boolean parseBoolean(String param) {
    return parseBoolean(param, false);
  }
  
  final protected Integer parseInteger(String param, Integer defaultValue) {
    if (isEmpty(param)) {
      return defaultValue;
    }
    return Integer.parseInt(parseString(param));
  }
  
  final protected Integer parseInteger(String param) {
    return parseInteger(param, null);
  }
  
  // Parsers
  final protected String parseString(String param, String defaultValue) {
    if (isEmpty(param)) {
      return defaultValue;
    }
    return paramMap.get(param).trim();
  }

  final protected String parseString(String param) {
    return parseString(param, null);
  }

  // Parameter serialization
  public static class WebParamSerializer {

    private final WebParams params;

    public WebParamSerializer(WebParams params) {
      this.params = params;
    }

    protected String prettifyString(String param, boolean value, boolean prependAmp) {
      if (params.isEmpty(param)) {
        return "";
      }
      return (prependAmp ? "&" : "") + param + "=" + value;
    }

    public String getConceptDetailEnabled(boolean prependAmp) {
      return prettifyString(CONCEPT_DETAIL_ENABLED, params.isConceptDetailEnabled(), prependAmp);
    }
  }
}
