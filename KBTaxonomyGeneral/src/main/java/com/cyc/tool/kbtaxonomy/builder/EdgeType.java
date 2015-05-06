package com.cyc.tool.kbtaxonomy.builder;

/*
 * #%L
 * KBTaxonomyGeneral
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * <P>
 * EdgeType contains information about edges that may appear in the graph.
 *
 */
public class EdgeType {


  static List<EdgeType> edgeTypes = null;


  static Set<String> possibleEdgeTypes = null;
  String jsonLinkColor;
  String jsonLinkName;
  String pred;
  String taxonomicFlag;

  /**
   * Creates a new instance of EdgeType.
   * @param pred
   * @param linkName
   * @param taxonomicFlag
   * @param linkColor
   * @throws java.io.IOException
   * @throws org.json.simple.parser.ParseException
   */
  public EdgeType(String pred, String linkName, String linkColor, String taxonomicFlag) throws IOException, ParseException {
    this.pred = pred;
    this.jsonLinkName = linkName;
    this.jsonLinkColor = linkColor;
    this.taxonomicFlag = taxonomicFlag;
  }

  /**
   * Creates a new instance of EdgeType.
   * @param linkName
   */
  public EdgeType(String linkName) {
    this.pred = "";
    this.jsonLinkName = linkName;
    this.jsonLinkColor = setLinkColor(linkName);
    this.taxonomicFlag = setTaxonomicFlag(linkName);
  }

  /**
   *
   * @return a List of EdgeType instances
   */
  public static List<EdgeType> getEdgeTypes() {
    if (edgeTypes == null) {
      edgeTypes = new ArrayList<>();
      String jsonContent;

      InputStream stream = EdgeType.class.getClassLoader().getResourceAsStream("edgeTypes.json");
      if (stream != null) {
        try {
          jsonContent = IOUtils.toString(stream);
          Object obj = JSONValue.parse(jsonContent);
          JSONArray jsonArray = (JSONArray) obj;
          for (int j = 0; j < jsonArray.size(); j++) {
            String jsonPred = ((JSONObject) jsonArray.get(j)).get("?PRED").toString();
            String jsonName = ((JSONObject) jsonArray.get(j)).get("?CATEGORY-NAME").toString();
            String jsonColor = ((JSONObject) jsonArray.get(j)).get("?COLOR").toString();
            String jsonFlag = ((JSONObject) jsonArray.get(j)).get("?TAXONOMIC").toString();
            EdgeType edgeType = new EdgeType(jsonPred, jsonName, jsonColor, jsonFlag);
            edgeTypes.add(edgeType);
          }

        } catch (ParseException | IOException ex) {
          Logger.getLogger(EdgeType.class.getName()).log(Level.SEVERE, null, ex);
        }

      }
    }

    return edgeTypes;
  }

  /**
   *
   * @return a set of possible EdgeType names
   */
  public static Set<String> getPossibleEdgeTypes() {
    if (possibleEdgeTypes == null) {
      possibleEdgeTypes = new HashSet<>();
      for (EdgeType e : getEdgeTypes()) {
        if (!possibleEdgeTypes.contains(e.jsonLinkName)) {
          possibleEdgeTypes.add(e.jsonLinkName);
        }
      }
    }

    return possibleEdgeTypes;
  }

  /**
   *
   * @return jsonLinkName
   */
  public String getJsonName() {
    return jsonLinkName;
  }

  /**
   *
   * @return jsonLinkColor
   */
  public String getLinkColour() {
    return jsonLinkColor;
  }

  /**
   *
   * @return name for EdgeType predicate
   */
  public String getPredString() {
    return pred;
  }

  /**
   *
   * @return true if the EdgeType is taxonomic
   */
  public String getTaxonomicFlag() {
    return taxonomicFlag;
  }

  /**
   * Sets the link color based on the type of link
   * @param linkName
   * @return jsonLinkColor
   */
  public String setLinkColor(String linkName) {
    List<EdgeType> edgeTypes = getEdgeTypes();
    String color = "";
    for (EdgeType e : edgeTypes) {
      if (e.jsonLinkName.equalsIgnoreCase(linkName)) {
        color = e.jsonLinkColor;
        return color;
      }
    }
    return color;
  }

  /**
   *
   * @param linkName
   * @return true if the link is taxonomic
   */
  public String setTaxonomicFlag(String linkName) {
    List<EdgeType> edgeTypes = getEdgeTypes();
    String flag = "";
    for (EdgeType e : edgeTypes) {
      if (e.jsonLinkName.equalsIgnoreCase(linkName)) {
        flag = e.taxonomicFlag;
        return flag;
      }
    }
    return flag;
  }
}
