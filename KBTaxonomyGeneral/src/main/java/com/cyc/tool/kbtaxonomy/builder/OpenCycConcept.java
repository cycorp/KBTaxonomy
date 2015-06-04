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
import com.cyc.tool.owltools.OpenCycContent;
import com.cyc.tool.owltools.OpenCycReasoner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * <p>
 * OpenCycConcept is a type of KBConcept that is in OpenCyc.
 *
 */
public class OpenCycConcept extends KBConcept {

  final OpenCycContent ocycContent;
  final OpenCycReasoner ocycReasoner;

  OpenCycConcept(String conceptCycL, Set<String> nlLabels, String conceptUri) throws OWLOntologyCreationException {
    super(conceptCycL, conceptUri);
    this.ocycReasoner = OpenCycReasoner.get();
    this.ocycContent = new OpenCycContent(conceptUri);
    this.nlNames = nlLabels;
  }

  /**
   * Factory method for creating a new OpenCycConcept
   *
   * @param kbTaxonomyCycConceptTerm
   * @param nlLabels
   * @param conceptUri
   * @return
   * @throws OWLOntologyCreationException
   */
  public static OpenCycConcept create(String kbTaxonomyCycConceptTerm, Set<String> nlLabels, String conceptUri) throws OWLOntologyCreationException {

    if (haveConcept(kbTaxonomyCycConceptTerm)) {
      return (OpenCycConcept) getConcept(kbTaxonomyCycConceptTerm);
    }
    assert (!kbTaxonomyCycConceptTerm.startsWith("NonCycConcept")) :
            "Attempt to create OpenCyc Concept for NonCyc term " + kbTaxonomyCycConceptTerm;
    OpenCycConcept created = new OpenCycConcept(kbTaxonomyCycConceptTerm, nlLabels, conceptUri);
    addToLists(created);

    return created;
  }

  @Override
  public final String getName() {
    if (getNlNames().isEmpty()) {
      return "Cy#:" + getConceptCycL();
    } else if (getNlNames().size() == 1) {
      return String.join("", getNlNames());
    } else {
      StringBuilder allNames = new StringBuilder();
      return ("Cy:[" + String.join("/", getNlNames()) + "]");
    }
  }

  @Deprecated
  @Override
  public String toD3JSON(int depth, GraphDirection dir) {
    return toD3JSON(depth, 100, dir);

  }

  @Override
  public String toD3JSON(int depth, int depthLimit, GraphDirection dir) {
    switch (dir) {
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
    return toD3JSON(depth, 100);
  }

  /**
   *
   * @param depth
   * @param depthLimit
   * @return D3JSON output
   */
  public String toD3JSON(int depth, int depthLimit) {

    List<String> fields = new ArrayList<>();

//    if (getChildSpecLinks().size() == 1) {
//      System.out.println("Skipping concept \n\t"
//              + this + "\nin favour of its only child \n\t"
//              + ((TaxonomicLink) (getChildSpecLinks().toArray())[0]).getFrom());
//      return ((TaxonomicLink) (getChildSpecLinks().toArray())[0]).getFrom().toD3JSON(depth + 1);
//    }
    fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
    fields.add(JSONBuilder.fieldStringValuePair("name", shortenedName()));
    fields.add(JSONBuilder.fieldValuePair("hasChildren", !(getChildSpecLinks().isEmpty() && getNonTaxonomicLinks().isEmpty())));
    fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
    fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));

    if (!getChildSpecLinks().isEmpty() && (depth < depthLimit)) {
      List<String> childJSON = new ArrayList<>();
      for (KBLink c : getChildSpecLinks()) {
        childJSON.add(c.getFrom().toD3JSON(depth + 1));
      }
      fields.add(JSONBuilder.fieldValuePair("children",
              JSONBuilder.array(childJSON)));
    }
    return JSONBuilder.object(fields);
  }

  @Override
  public String toD3JSONNoRecursion(NonTaxonomicLink forLink) {//NB Forced to be a part for now
    List<String> fields = new ArrayList<>();
    fields.add(JSONBuilder.fieldStringValuePair("linkType", forLink.getLinkTypeName()));
    fields.add(JSONBuilder.fieldStringValuePair("linkColour", forLink.getLinkColour()));
    fields.add(JSONBuilder.fieldValuePair("hasChildren", !(getChildSpecLinks().isEmpty() && getNonTaxonomicLinks().isEmpty())));
    fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
    fields.add(JSONBuilder.fieldStringValuePair("name", shortenedName()));
    fields.add(JSONBuilder.fieldStringValuePair("displayedConceptID", getRef()));
    fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
    fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));

    return JSONBuilder.object(fields);
  }

  @Override
  public String toJSON(int depth, GraphDirection dir) {
    switch (dir) {
      case down:
        return toJSONDown(depth);
      case up:
        return toJSONUp(depth);
      default:
        return "";
    }
  }

  @Override
  public String toJSON(int depth) {
    assert depth < 100 :
            "100 levels of recursion reached making JSON for " + this;
    List<String> fields = new ArrayList<>();
    fields.add(JSONBuilder.fieldValuePair("printSequence", printNumber++));
//    if (getChildSpecLinks().size() == 1) {
//      System.out.println("Skipping concept \n\t"
//              + this + "\nin favour of its only child \n\t"
//              + ((TaxonomicLink) (getChildSpecLinks().toArray())[0]).getFrom());
//      return ((TaxonomicLink) (getChildSpecLinks().toArray())[0]).getFrom().toJSON(depth + 1);
//    }

    fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
    fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", getConceptCycL()));
    fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
    fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));
    if (!nlNames.isEmpty()) {
      fields.add(JSONBuilder.fieldValuePair(
              "nlLabels", JSONBuilder.arrayOfString(getNlNames())));
    }
    if (!getChildSpecLinks().isEmpty()) {
      List<String> childJSON = new ArrayList<>();
      for (KBLink c : getChildSpecLinks()) {
        childJSON.add(c.getFrom().toJSON(depth + 1));
      }
      fields.add(JSONBuilder.fieldValuePair("specializations",
              JSONBuilder.array(childJSON)));
    }
    return JSONBuilder.object(fields);
  }

  @Override
  public String toJSONNoRecursion() {
    List<String> fields = new ArrayList<>();
    fields.add(JSONBuilder.fieldValuePair("printSequence", printNumber++));

    fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
    fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", getConceptCycL()));
    fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
    fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));
    if (!nlNames.isEmpty()) {
      fields.add(JSONBuilder.fieldValuePair(
              "nlLabels", JSONBuilder.arrayOfString(getNlNames())));
    }

    return JSONBuilder.object(fields);

  }

  @Override
  public String toString() {
    return "OpenCyc " + super.toString() + "";
  }

  @Override
  protected Set<KBLink> getChildSpecLinks() {
//    maybeExpand();
    return childSpecLinks;
  }

  @Override
  public Set<KBConcept> getChildren() {
//   maybeExpand();
    return children;
  }

  @Override
  protected Set<NonTaxonomicLink> getNonTaxonomicLinks() {
    return nonTaxonomicLinks;
  }

  @Override
  protected Set<TaxonomicLink> getParentlinks() {
//    maybeExpand();
    return parentLinks;
  }

  @Override
  protected Set<KBConcept> getParents() {
//    maybeExpand();
    return parents;
  }

  @Override
  protected final void setChildren() {
    try {
      Set<String> types = ocycContent.getSubTypesForConcept();
      for (String t : types) {
        if (t.contains("Mx")) {
          OpenCycContent typeContent = new OpenCycContent(t);
          Set<String> nlLabels = new HashSet<>();
          nlLabels.add(typeContent.getLabelForConcept());

          KBConcept child = OpenCycConcept.create(typeContent.getLabelForConcept(), typeContent.getPrettyStringsForConcept(), t);

          TaxonomicLink link = GeneralisationLink.create(child, this);
          getChildSpecLinks().add(link);
          getChildren().add(child);
        }

      }
    } catch (OWLOntologyCreationException ex) {
      Logger.getLogger(OpenCycConcept.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  protected final void setChildrenLinks() {
    for (KBConcept p : getChildren()) {
      TaxonomicLink link = GeneralisationLink.create(p, this);
      getChildSpecLinks().add(link);
    }
  }

  @Override
  protected final void setParentLinks() {
    for (KBConcept p : parents) {
      TaxonomicLink link = GeneralisationLink.create(this, p);
      parentLinks.add(link);
    }
  }

  @Override
  protected final void setParents() {
    try {
      Set<String> types = ocycContent.getTypesForConcept();
      for (String t : types) {
        OpenCycContent typeContent = new OpenCycContent(t);
        Set<String> nlLabels = new HashSet<>();
        nlLabels.add(typeContent.getLabelForConcept());
        KBConcept parent = OpenCycConcept.create(typeContent.getLabelForConcept(), typeContent.getPrettyStringsForConcept(), t);
        parents.add(parent);
        TaxonomicLink link = GeneralisationLink.create(this, parent);
        parentLinks.add(link);
      }
    } catch (OWLOntologyCreationException ex) {
      Logger.getLogger(OpenCycConcept.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private String toD3JSONDown(int depth, int depthLimit) {

    List<String> fields = new ArrayList<>();

    fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
    fields.add(JSONBuilder.fieldStringValuePair("name", shortenedName()));
    fields.add(JSONBuilder.fieldValuePair("hasChildren", !(getChildSpecLinks().isEmpty() && getNonTaxonomicLinks().isEmpty())));
    fields.add(JSONBuilder.fieldStringValuePair("displayedConceptID", getRef()));
    fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
    fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));

    if (!(getChildSpecLinks().isEmpty()
            && getNonTaxonomicLinks().isEmpty())
            && (depth < depthLimit)) {
      List<String> childJSON = new ArrayList<>();
      for (KBLink c : getChildSpecLinks()) {
        childJSON.add(c.getFrom().toD3JSON(depth + 1, depthLimit, GraphDirection.down));
      }
      for (NonTaxonomicLink c : getNonTaxonomicLinks()) {
        System.out.println(this + " is related to " + c.getTo());
        childJSON.add(c.getTo().toD3JSONNoRecursion(c));
      }
      fields.add(JSONBuilder.fieldValuePair("children",
              JSONBuilder.array(childJSON)));
    }
    return JSONBuilder.object(fields);
  }

  private String toD3JSONUp(int depth, int depthLimit) {
    List<String> fields = new ArrayList<>();
    if (!"".equals(shortenedName())) {

      fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
      fields.add(JSONBuilder.fieldStringValuePair("name", shortenedName()));
    fields.add(JSONBuilder.fieldValuePair("hasChildren", !(getChildSpecLinks().isEmpty() && getNonTaxonomicLinks().isEmpty())));
      fields.add(JSONBuilder.fieldStringValuePair("displayedConceptID", getRef()));
      fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
      fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));
      fields.add(JSONBuilder.fieldValuePair("isParent", true));

      if (!(getParentlinks().isEmpty())
              && (depth < depthLimit)) {
        List<String> parentJSON = new ArrayList<>();
        for (TaxonomicLink p : getParentlinks()) {
          parentJSON.add(p.getTo().toD3JSON(depth + 1, depthLimit, GraphDirection.up));
        }

        parentJSON.removeAll(Collections.singleton("{}"));
        if (!parentJSON.isEmpty()) {
        fields.add(JSONBuilder.fieldValuePair("children",
                JSONBuilder.array(parentJSON)));
        } 
      }
//      return JSONBuilder.object(fields);
    }
    return JSONBuilder.object(fields);
  }

  private String toJSONDown(int depth) {

    assert depth < 100 :
            "100 levels of recursion reached making JSON for " + this;
    List<String> fields = new ArrayList<>();
    fields.add(JSONBuilder.fieldValuePair("printSequence", printNumber++));
//    if (getChildSpecLinks().size() == 1) {
//      System.out.println("Skipping concept \n\t"
//              + this + "\nin favour of its only child \n\t"
//              + ((TaxonomicLink) (getChildSpecLinks().toArray())[0]).getFrom());
//      return ((TaxonomicLink) (getChildSpecLinks().toArray())[0]).getFrom().toJSON(depth + 1, GraphDirection.down);
//    }

    fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
    fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", getConceptCycL()));
    fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
    fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));
    if (!nlNames.isEmpty()) {
      fields.add(JSONBuilder.fieldValuePair(
              "nlLabels", JSONBuilder.arrayOfString(getNlNames())));
    }
    if (!getChildSpecLinks().isEmpty()) {
      List<String> childJSON = new ArrayList<>();
      for (KBLink c : getChildSpecLinks()) {
        childJSON.add(c.getFrom().toJSON(depth + 1, GraphDirection.down));
      }
      fields.add(JSONBuilder.fieldValuePair("specializations",
              JSONBuilder.array(childJSON)));
    }
    return JSONBuilder.object(fields);
  }

  private String toJSONUp(int depth) {

    assert depth < 100 :
            "100 levels of recursion reached making JSON for " + this;
    List<String> fields = new ArrayList<>();
    fields.add(JSONBuilder.fieldValuePair("printSequence", printNumber++));

    fields.add(JSONBuilder.fieldStringValuePair("type", "kbTaxonomyTaxonomyConcept"));
    fields.add(JSONBuilder.fieldStringValuePair("kbTaxonomyCycConceptTerm", getConceptCycL()));
    fields.add(JSONBuilder.fieldStringValuePair("hlid", getConceptUri()));
    fields.add(JSONBuilder.fieldStringValuePair("openCycConstant", "true"));
    if (!nlNames.isEmpty()) {
      fields.add(JSONBuilder.fieldValuePair(
              "nlLabels", JSONBuilder.arrayOfString(getNlNames())));
    }
    if (!getParentlinks().isEmpty()) {
      List<String> parentJSON = new ArrayList<>();
      for (TaxonomicLink p : getParentlinks()) {
        parentJSON.add(p.getFrom().toJSON(depth + 1, GraphDirection.up));
      }
      fields.add(JSONBuilder.fieldValuePair("specializations",
              JSONBuilder.array(parentJSON)));
    }
    return JSONBuilder.object(fields);
  }

  String shortenedName() {
    String re = "\\(SitTypeSpecWithTypeRestrictionOnRolePlayerFn\\s+(\\S+)\\s+\\S+\\s+(\\S+)\\)";
    String re2 = "\\(HumanActivityFn\\s+(\\S+)\\)";
    String label = getConceptCycL();
    if (getConceptCycL().matches(re)) {
      label = getConceptCycL().replaceFirst(re, "$2:$1");
    } else if (getConceptCycL().matches(re2)) {
      label = getConceptCycL().replaceFirst(re2, "Human:$1");
    } else if (getConceptCycL().contains(")")) {
    }
    return label;
  }
}
