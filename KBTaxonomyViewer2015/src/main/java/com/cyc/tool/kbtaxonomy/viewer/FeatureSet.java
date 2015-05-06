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

import com.cyc.tool.kbtaxonomy.builder.NonCycConcept;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * A modifiable set of features that describes some thing (possibly depicted).
 *
 */
public class FeatureSet {

  final private Set<Feature> features = new HashSet<>();
  private int lastFeatureNum = 1;
  final private Map<String, Feature> map = new HashMap<>();
  private String setName = null;
  private String setSource = null;

  /**
   * FeatureSet constructor
   * 
   * @param xmlFromFile
   */
  public FeatureSet(File xmlFromFile) {
    map.clear();
    features.clear();
    try {

      setSource = xmlFromFile.getCanonicalPath();
      DocumentBuilderFactory dbFactoryOld = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilderOld = dbFactoryOld.newDocumentBuilder();
      Document docOld = dBuilderOld.parse(xmlFromFile);

      docOld.getDocumentElement().normalize();

      setName = docOld.getDocumentElement().getAttribute("eventID");

      NamedNodeMap nodeAttribs = docOld.getDocumentElement().
              getElementsByTagName("node")
              .item(0)
              .getAttributes();
      
      String weightMap = nodeAttribs.getNamedItem("eq")
              .getNodeValue().replaceFirst("\\s*combine\\s+", "");
      
      NodeList nListOld = docOld.getElementsByTagName("tag");

      for (int i = 0; i < nListOld.getLength(); i++) {
        Node nNode = nListOld.item(i);
        Element eElement = (Element) nNode;
        features.add(new Feature(eElement.getAttribute("name"), eElement.getAttribute("id")));
      }
      String weightMatcher = "^(CC\\.\\d+)=(.+)$";
      Arrays.asList(weightMap.split(",")).forEach(s -> {
        if (s.matches(weightMatcher)) {
          String sID = s.replaceFirst(weightMatcher, "$1");
          String sWe = s.replaceFirst(weightMatcher, "$2");
          map.get(sID).weight = Float.parseFloat(sWe);
        }
      });
      /* Export Weights */
      map.values().forEach(feat -> {
        
        feat.getConcept().setWeight(feat.getWeight());
      });

    } catch (ParserConfigurationException | SAXException | IOException ex) {
      Logger.getLogger(FeatureSet.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Construct FeatureSet from a set of NonCyc concepts
   * 
   * @param concepts
   * @param setN
   */
  
  public FeatureSet(Collection<NonCycConcept> concepts, String setN) {
    map.clear();
    features.clear();
    setName = setN;
    concepts.forEach(con -> {
      features.add(new Feature(con));
    });
  }

  /**
   *
   * @return setName
   */
  public String getFeatureSetName() {
    return setName;
  }

  /**
   *
   * @return a Set of NonCycConcepts in the FeatureSet
   */
  public Set<NonCycConcept> getSet() {
    return features.stream()
            .map(Feature::getConcept)
        // .sorted()
            .collect(Collectors.toSet());
  }

  /**
   * Save out a FeatureSet to a file
   * 
   * @param file
   */
  public void save(File file) {
    try (PrintWriter out = new PrintWriter(file)) {
      out.print(toXML());
    } catch (FileNotFoundException ex) {
      System.out.println("Failed to write XML in " + file.getAbsolutePath());
      Logger.getLogger(FeatureSet.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  @Override
  public String toString() {
    return ("Features for event kit " + setName)
            + (" From file: " + setSource)
            + (" Number of features: " + features.size())
            + (" Features: ["
            + features.stream().map(Feature::toString).sorted().collect(Collectors.joining(", "))
            + "]");
  }
  
  /**
   * Get String representation of the feature set without weights
   * 
   * @return a String
   */
  public String toStringNoWeight() {
    return ("Features for event kit " + setName)
            + (" From file: " + setSource)
            + (" Number of features: " + features.size())
            + (" Features: ["
            + features.stream().map(Feature::toStringNoWeight).sorted().collect(Collectors.joining(", "))
            + "]");
  }
  
  /**
   * Get XML representation of the feature set
   * 
   * @return a String
   */
  public String toXML() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
            + "<query eventID=\"" + setName + "\">\n"
            + nodeXML(" ")
            + "</query>";
  }

  private String combineXML() {
    return "combine " 
            + features.stream().sorted()
                    .map(Feature::toWeightXML)
                    .collect(Collectors.joining(","));
  }
  
  private String nodeXML(String indent) {
    return indent+"<node eq=\"" + combineXML() + "\" id=\"CC\" name=\"concepts/cycorp\">\n"
            + tagsXML(indent+" ") + "\n"
            + indent+"</node>\n";
  }

  private String tagsXML(String indent) {
    return features.stream().sorted().map(f->f.toTagXML(indent+" ")).collect(Collectors.joining("\n"));

  }
  synchronized int nextFeatureNum() {
    lastFeatureNum++;
    return lastFeatureNum;
  }
  
  private class Feature implements Comparable<Feature> {

    String name;
    String tagID;
    Integer nonCycTeamIDNumber;
    String conceptUri;
    Float weight;
    static final String matcher = "^([^/]*)/(\\d+)$";
    
    public Feature(String fromSingle, String tagId) {
      if (fromSingle.matches(matcher)) {
        
        name = fromSingle.replaceFirst(matcher, "$1");
        String num = fromSingle.replaceFirst(matcher, "$2");
        nonCycTeamIDNumber = Integer.parseInt(num);
        conceptUri = "";
        tagID = tagId;
        map.put(tagID, this);
      }
    }
    
    public Feature(NonCycConcept c) {
      name = c.getName();
      nonCycTeamIDNumber = c.getNonCycTeamNumericID();
      conceptUri = "";
      tagID = String.format("CC.%d", nextFeatureNum());
      weight = c.getWeight();
      map.put(tagID, this);
    }
    
    public Float getWeight() {
      return weight;
    }
    
    public String getName() {
      return name;
    }
    
    public NonCycConcept getConcept() {
      return NonCycConcept.getFromIDNameOpt(nonCycTeamIDNumber, name, conceptUri);
    }
    
    String toWeightXML() {
      return tagID + "=" + weight;
    }
    
    String toNameWithTeamID() {
      return name + ":" + nonCycTeamIDNumber;
    }
    
    String toTagXML(String indent) {
      return indent+"<tag id=\"" + tagID + "\" name=\"" + toNameWithTeamID() + "\"/>";
    }
    
    @Override
    public String toString() {
      return toStringNoWeight()+"/W:" + toWeightXML();
    }

    public String toStringNoWeight() {
      return toNameWithTeamID();
    }

    @Override
    public int compareTo(Feature o) {
      return o.getWeight().compareTo(this.getWeight());
    }
  }
}
