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

import com.cyc.library.json.JSONBuilder;
import static com.cyc.tool.kbtaxonomy.builder.KBConcept.addToLists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * NonCycConcept is a type of KBConcept that is not in OpenCyc.
 *
 */
public class NonCycConcept extends KBConcept {

  /**
   * Map from Integer to NonCycConcept to track teamIDs
   */
  static protected final Map<Integer, NonCycConcept> teamIDsToConcepts = new HashMap<>();
  List<String> nonCycConceptWNIDs;
  List<Integer> nonCycTeamNumericID;

  Float weight = null;

  /**
   * NonCycConcept constructor
   * 
   * @param conceptCycL
   * @param nonCycTeamConceptID
   * @param conceptName
   * @param conceptUri
   * @param nonCycConceptWNIDs
   */
  public NonCycConcept(String conceptCycL, List<Integer> nonCycTeamConceptID, String conceptName, String conceptUri, List<String> nonCycConceptWNIDs) {
    super(conceptCycL, conceptUri);
    nonCycTeamNumericID = nonCycTeamConceptID;
    this.nonCycConceptWNIDs = nonCycConceptWNIDs;
    this.nlNames = new HashSet<>();
    this.nlNames.add(conceptName);
    setExpanded();
    // this.setSelected( (Math.random()>0.5));
  }

  /**
   * Factory method to create a new NonCycConcept object
   * 
   * @param kbTaxonomyCycConceptTerm
   * @param nonCycTeamConceptID
   * @param conceptName
   * @param conceptUri
   * @param nonCycConceptWNIDs
   * @return a NonCycConcept
   */
  public static NonCycConcept create(String kbTaxonomyCycConceptTerm, List<Integer> nonCycTeamConceptID, String conceptName, String conceptUri, List<String> nonCycConceptWNIDs) {
    
    if (haveConcept(kbTaxonomyCycConceptTerm)) {
      return (NonCycConcept) getConcept(kbTaxonomyCycConceptTerm);
    }
    assert (kbTaxonomyCycConceptTerm.startsWith("NonCycConcept")) :
            "Attempt to create NonCyc Concept for non-NonCyc term " + kbTaxonomyCycConceptTerm;
    NonCycConcept created = new NonCycConcept(kbTaxonomyCycConceptTerm, nonCycTeamConceptID, conceptName, conceptUri, nonCycConceptWNIDs);

    addToLists(created);
    for (Integer id : nonCycTeamConceptID) {
     teamIDsToConcepts.put(id, created);
    }
//    teamIDsToConcepts.put(nonCycTeamConceptID, created);
    return created;
  }

  /**
   * Factory method to create a new NonCycConcept object
   * 
   * @param kbTaxonomyCycConceptTerm
   * @param nonCycTeamConceptID
   * @param conceptName
   * @param conceptUri
   * @return a NonCycConcept
   */
  public static NonCycConcept create(String kbTaxonomyCycConceptTerm, List<Integer> nonCycTeamConceptID, String conceptName, String conceptUri) {
    
    if (haveConcept(kbTaxonomyCycConceptTerm)) {
      return (NonCycConcept) getConcept(kbTaxonomyCycConceptTerm);
    }
    assert (kbTaxonomyCycConceptTerm.startsWith("NonCycConcept")) :
            "Attempt to create NonCyc Concept for non-NonCyc term " + kbTaxonomyCycConceptTerm;
    NonCycConcept created = new NonCycConcept(kbTaxonomyCycConceptTerm, nonCycTeamConceptID, conceptName, conceptUri, new ArrayList<>());

    addToLists(created);
    nonCycTeamConceptID.forEach(id -> {
      teamIDsToConcepts.put(id, created);
    });
//    teamIDsToConcepts.put(nonCycTeamConceptID, created);
    return created;
  }

  /**
   * Returns a NonCycConcept based on its index
   * 
   * @param index
   * @return a NonCycConcept
   */
  public static NonCycConcept getByIndex(int index) {
    KBConcept fetched = allConceptTable.get(index);
    if (fetched instanceof NonCycConcept) {
      return (NonCycConcept) fetched;
    } else {
      throw new RuntimeException("Tried to fetch concept " + index + " -> "
              + fetched + " as " + NonCycConcept.class.getCanonicalName() + " when it isn't");
      // return null;
    }
  }

  /**
   * Returns a NonCycConcept based on its teamID
   * 
   * @param teamID
   * @return a NonCycConcept
   */
  public static NonCycConcept getFromID(int teamID) {
    
    return teamIDsToConcepts.get(teamID);
  }

  /**
   * Returns a NonCycConcept based on its teamID, name, and conceptUri
   * 
   * @param teamID
   * @param name
   * @param conceptUri
   * @return a NonCycConcept
   */
  public static NonCycConcept getFromIDNameOpt(int teamID, String name, String conceptUri) {
    
    if (!teamIDsToConcepts.containsKey(teamID)) {
      System.out.println("FAKING " + teamID + "-" + name);
      List<Integer> teamIDs = new ArrayList<>();
      teamIDs.add(teamID);
      NonCycConcept ret = create("NonCycConcept-Fake" + teamID + "-" + name, teamIDs, name, conceptUri);
      ret.setSelected();
      return ret;
    }
    return getFromID(teamID);

  }

  /**
   *
   * @return HTML to display information about a NonCycConept in the graph
   */
  public String generateHtmlForConcept() {
    String html = "";
    String constantName = getName();
    Set<String> pics = selectPicsForConcept(getNonCycConceptWNIDs());
    html += "<h1>" + constantName + "</h1>\n\n";
    
    for (String p : pics) {
      html += "   <li>" + p + "</li>\n";
    }
    html += "</ul>\n";

    return html;
  }

  @Override
  public final String getName() {
    if (getNlNames().isEmpty()) {
      return "Au#:" + getConceptCycL();
    } else if (getNlNames().size() == 1) {
      return String.join("", getNlNames());
    } else {
      StringBuilder allNames = new StringBuilder();
      return ("Au:[" + String.join("/", getNlNames()) + "]");
    }
  }

  /**
   *
   * @return nonCycConceptWNIDs
   */
  public List<String> getNonCycConceptWNIDs() {
    return nonCycConceptWNIDs;
  }

  /**
   *
   * @return nonCycTeamNumericID
   */
  public List<Integer> getNonCycTeamNumericID() {
    return nonCycTeamNumericID;
  }

  /**
   *
   * @return weight
   */
  public float getWeight() {
    if (weight == null) {
      return 0;
    }
    return weight;
  }

  /**
   * Setter for weight field
   * 
   * @param mWeight
   */
  public void setWeight(float mWeight) {
    this.weight = mWeight;
  }

  /**
   *
   * @return true if weight is not null
   */
  public boolean hasWeight() {
    return weight != null;
  }

  @Deprecated
  @Override
  public String toD3JSON(int depth, KBConcept.GraphDirection dir) {
    return toD3JSON(depth, 100, dir);
  }

  @Override
  public String toD3JSON(int depth, int depthLimit, KBConcept.GraphDirection direction) {
    switch (direction) {
      case down:
        return toD3JSONDown(depth, depthLimit);
      case up:
        return toD3JSONUp(depth, depthLimit);
      default:
        return "";
    }

  }

  @Override
  public String toD3JSON(int depth) {
    List<String> fields = new ArrayList<>();

    fields.add(JSONBuilder.fieldStringValuePair("type", "nonCycTeamConcept"));
    //if (nonCycTeamNumericID >=0 ){
    //   fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID));
    // }
    // fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", conceptCycL));
    if (getNlNames().size() == 1) {
      fields.add(JSONBuilder.fieldStringValuePair(
              "name", (new ArrayList<>(getNlNames())).get(0)));
    } else {
      System.out.println("*** Problem: NonCyc Concepts should have one NL .. "
              + getNlNames() + "was found for " + this);
    }
    return JSONBuilder.object(fields);
  }

  @Override
  public String toD3JSONNoRecursion(NonTaxonomicLink forLink) {  //NB Forced to be a part for now
    List<String> fields = new ArrayList<>();
    System.out.println(this + "A " + forLink.getLinkTypeName() + " -- " + forLink.getLinkColour());
    fields.add(JSONBuilder.fieldStringValuePair("linkType", forLink.getLinkTypeName()));
    fields.add(JSONBuilder.fieldStringValuePair("linkColour", forLink.getLinkColour()));
    
    if (getNlNames().size() == 1) {
      fields.add(JSONBuilder.fieldStringValuePair(
              "name", (new ArrayList<>(getNlNames())).get(0)));
    } else {
      System.out.println("*** Problem: NonCyc Concepts should have one NL .. "
              + getNlNames() + "was found for " + this);
    }
    fields.add(JSONBuilder.fieldStringValuePair("type", "nonCycTeamConcept"));
    fields.add(JSONBuilder.fieldValuePair("activeconcept", this.isSelected()));
    fields.add(JSONBuilder.fieldStringValuePair("displayedConceptID", getRef()));
//    fields.add(JSONBuilder.fieldStringValuePair("nonCycTeamConceptID", Integer.toString(getNonCycTeamNumericID())));
    if (nonCycTeamNumericID != null) {
     fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID.toString()));
    }
//    fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(getNonCycConceptWNIDs())));
    if (nonCycConceptWNIDs != null) {
      fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(nonCycConceptWNIDs)));
    }
    
    
    return JSONBuilder.object(fields);
  }

  @Override
  public String toJSON(int depth, KBConcept.GraphDirection dir) {
    return toJSON(depth);
  }
  
  public String toJSON(int depth) {
    return toJSONNoRecursion();
  }

  @Override
  public String toJSONNoRecursion() {
    List<String> fields = new ArrayList<>();
    fields.add(JSONBuilder.fieldValuePair("printSequence", printNumber++));
    fields.add(JSONBuilder.fieldStringValuePair("type", "nonCycTeamConcept"));
//    if (nonCycTeamNumericID >= 0) {
//      fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID));
//    }
    if (nonCycTeamNumericID != null) {
      fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID.toString()));
    }
    if (nonCycConceptWNIDs != null) {
      fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(nonCycConceptWNIDs)));
    }
//    fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(getNonCycConceptWNIDs())));
    fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", getConceptCycL()));

    if (getNlNames().size() == 1) {
      fields.add(JSONBuilder.fieldStringValuePair(
              "conceptName", (new ArrayList<>(getNlNames())).get(0)));
    } else {
      System.out.println("*** Problem: NonCyc Concepts should have one NL .. "
              + getNlNames() + "was found for " + this);
    }
    return JSONBuilder.object(fields);
  }
  
  @Override
  public String toString() {
    return "NonCyc " + super.toString() + " NumericID:" + nonCycTeamNumericID;
  }

  @Override
  protected Set<KBLink> getChildSpecLinks() {
    return childSpecLinks;
  }

  @Override
  protected Set<KBConcept> getChildren() {
    return children;
  }

  @Override
  protected Set<NonTaxonomicLink> getNonTaxonomicLinks() {
    return nonTaxonomicLinks;
  }

  @Override
  protected Set<TaxonomicLink> getParentlinks() {
    return parentLinks;
  }

  @Override
  protected Set<KBConcept> getParents() {
    return parents;
  }

  @Override
  final protected void setChildren() {
    if (!isExpanded()) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  @Override
  protected void setChildrenLinks() {
    if (!isExpanded()) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  @Override
  protected final void setParentLinks() {
    if (!isExpanded()) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  @Override
  protected final void setParents() {
    if (!isExpanded()) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  private Set<String> selectPicsForConcept(List<String> nonCycConceptWNIDs) {
    Set<String> picUrls = new HashSet<>();
    
    for (String w : nonCycConceptWNIDs) {
      picUrls.add("http://www.image-net.org/api/text/imagenet.synset.geturls?wnid=" + w);
    }

    return picUrls;
  }

  private String toD3JSONDown(int depth, int depthLimit) {
    List<String> fields = new ArrayList<>();
    
    fields.add(JSONBuilder.fieldStringValuePair("type", "nonCycTeamConcept"));
    fields.add(JSONBuilder.fieldValuePair("activeconcept", this.isSelected()));
    fields.add(JSONBuilder.fieldStringValuePair("displayedConceptID", getRef()));
//    fields.add(JSONBuilder.fieldStringValuePair("nonCycTeamConceptID", Integer.toString(getNonCycTeamNumericID())));
    if (nonCycTeamNumericID != null) {
      fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID.toString()));
    }
//    fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(getNonCycConceptWNIDs())));
    if (nonCycConceptWNIDs != null) {
      fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(nonCycConceptWNIDs)));
    }
    //if (nonCycTeamNumericID >=0 ){
    //   fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID));
    // }
    // fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", conceptCycL));
    if (getNlNames().size() == 1) {
      fields.add(JSONBuilder.fieldStringValuePair(
              "name", (new ArrayList<>(getNlNames())).get(0)));
    } else {
      System.out.println("*** Problem: NonCyc Concepts should have one NL .. "
              + getNlNames() + "was found for " + this);
    }

    return JSONBuilder.object(fields);
  }

  private String toD3JSONUp(int depth, int depthLimit) {
    List<String> fields = new ArrayList<>();
    //System.out.println("NonCyc UP:"+this);
    fields.add(JSONBuilder.fieldStringValuePair("type", "nonCycTeamConcept"));
    fields.add(JSONBuilder.fieldValuePair("activeconcept", this.isSelected()));
    fields.add(JSONBuilder.fieldValuePair("isParent", true));
    fields.add(JSONBuilder.fieldStringValuePair("displayedConceptID", getRef()));
//    fields.add(JSONBuilder.fieldStringValuePair("nonCycTeamConceptID", Integer.toString(getNonCycTeamNumericID())));
    if (nonCycTeamNumericID != null) {
      fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID.toString()));
    }
//    fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(getNonCycConceptWNIDs())));
    if (nonCycConceptWNIDs != null) {
      fields.add(JSONBuilder.fieldValuePair("nonCycConceptWNID", JSONBuilder.arrayOfString(nonCycConceptWNIDs)));
    }
    //if (nonCycTeamNumericID >=0 ){
    //   fields.add(JSONBuilder.fieldValuePair("nonCycTeamConceptID", nonCycTeamNumericID));
    // }
    // fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", conceptCycL));
    if (getNlNames().size() == 1) {
      fields.add(JSONBuilder.fieldStringValuePair(
              "name", (new ArrayList<>(getNlNames())).get(0)));
    } else {
      System.out.println("*** Problem: NonCyc Concepts should have one NL .. "
              + getNlNames() + "was found for " + this);
    }
    if (!getParentlinks().isEmpty() && (depth < depthLimit)) {
      List<String> parentJSON = new ArrayList<>();
      for (TaxonomicLink p : getParentlinks()) {
        parentJSON.add(p.getTo().toD3JSON(depth + 1, depthLimit, KBConcept.GraphDirection.up));
      }
      fields.add(JSONBuilder.fieldValuePair("children",
              JSONBuilder.array(parentJSON)));
    }
    return JSONBuilder.object(fields);
  }

}
