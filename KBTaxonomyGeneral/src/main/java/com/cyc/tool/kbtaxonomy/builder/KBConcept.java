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
import com.cyc.library.json.D3JSONizable;
import com.cyc.library.json.JSONizable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * KBConcept is an abstract class for representing concepts that are used in KB Taxonomies.
 *
 */
public abstract class KBConcept implements JSONizable, D3JSONizable, Comparable<KBConcept> {

  /**
   * Used to number entries in the JSON sent to be graphed.
   */
  public static long printNumber = 0L;

  /**
   * A Map from Integers to KBConcepts to keep track of all concepts that have been created.
   */
  static protected final Map<Integer, KBConcept> allConceptTable = new HashMap<>();

  private static final Map<String, KBConcept> allConcepts = new HashMap<>();
  private static final int graphMaxDepth = 8;
  private static int lastConceptIndex = 0;
  private int conceptIndex;
  private boolean expanded = false;
  private final Set<KBLink> generalLinks;
  private boolean selected = false;
  final Set<KBLink> childSpecLinks;
  final Set<KBConcept> children;
  final String conceptCycL;
  final String conceptUri;
  Set<String> nlNames = new HashSet<>();

  final Set<NonTaxonomicLink> nonTaxonomicLinks;

  final Set<TaxonomicLink> parentLinks;
  final Set<KBConcept> parents;

  /**
   * KBConcept constructor
   *
   * @param conceptCycL
   * @param conceptUri
   */
  protected KBConcept(String conceptCycL, String conceptUri) {
    this.conceptIndex = nextIndex();
    this.childSpecLinks = new HashSet<>();

    this.nonTaxonomicLinks = new HashSet<>();

    this.parentLinks = new HashSet<>();
    this.generalLinks = new HashSet<>();
    this.children = new HashSet<>();
    this.parents = new HashSet<>();
    this.conceptCycL = conceptCycL;
    this.conceptUri = conceptUri;

  }

  /**
   * Returns all concepts
   *
   * @return Set of KBConcepts
   */
  public static Set<KBConcept> getAllConcepts() {
    return new HashSet(allConcepts.values());
  }

  /**
   * @param id
   * @return a KBConcept
   */
  public static KBConcept getConcept(String id) {
    return allConcepts.get(id);
  }

  /**
   * @param id
   * @return a KBConcept
   */
  public static KBConcept getExpandedConcept(String id) {
    allConcepts.get(id).maybeExpand();
    return allConcepts.get(id);
  }

  /**
   *
   * @param id
   * @return true if the concept with id is known
   */
  static public boolean haveConcept(String id) {
    return allConcepts.containsKey(id);
  }

  static void addToLists(KBConcept created) {
    //  System.out.println("CREATE:"+created.conceptCycL+"--"+created.conceptIndex);
    allConcepts.put(created.getConceptCycL(), created);
    if (created instanceof OpenCycConcept) {
      allConcepts.put(((OpenCycConcept) created).shortenedName(), created);
    }
    allConceptTable.put(created.conceptIndex, created);
  }

  /**
   *
   * @param jsonData the value of jsonData
   * @param graphConceptID the value of n
   * @param depthInConcepts the value of depthInConcepts
   * @param widthInConcepts the value of widthInConcepts
   */
  static String writeGraphStarter(String comment, Integer graphConceptId, String graphConceptName, int depthInConcepts, int widthInConcepts) {
    //  return "alert(\"clicked\");";
    return "collapsibleLinearGraph('" + String.format("%06d", graphConceptId) + "','" + graphConceptName + "'," + depthInConcepts + "," + widthInConcepts + "," + graphMaxDepth + ");";
  }

  /**
   *
   * @return Set of Strings containing all term names
   */
  public Set<String> allTerms() {
    Set<String> toReturn = new HashSet<>(getNlNames());
    toReturn.add(getConceptCycL());
    return toReturn;
  }

  @Override
  public int compareTo(KBConcept o) {
    return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
  }

  /**
   * @return the conceptCycL
   */
  public String getConceptCycL() {
    return conceptCycL;
  }

  /**
   *
   * @return conceptUri
   */
  public String getConceptUri() {
    return conceptUri;
  }

  /**
   *
   * @return concept CycL
   */
  public String getCycL() {
    return getConceptCycL();
  }

  /**
   *
   * @return conceptIndex
   */
  public int getIndex() {
    return conceptIndex;
  }

  /**
   *
   * @return concept name
   */
  public abstract String getName();

  /**
   *
   * @return html to set concept to "unclickable"
   */
  public String getNameUnclick() {
    return "<span class='unclickable'>" + getName() + "</span>";
  }

  /**
   * @return the nlNames
   */
  public Set<String> getNlNames() {
    return nlNames;
  }

  /**
   * @param nlNames the nlNames to set
   */
  public void setNlNames(Set<String> nlNames) {
    this.nlNames = nlNames;
  }

  /**
   *
   * @return formatted String based on the concept index
   */
  public String getRef() {
    return String.format("concept%06d", getIndex());
  }

  /**
   * Returns the maximum depth of a child node.
   *
   * @return in integer representing the maximum depth
   */
  public int height() {
    if (getChildSpecLinks().isEmpty() && getNonTaxonomicLinks().isEmpty()) {
      return 0;
    }
    int max = 0;
    for (KBLink c : getChildSpecLinks()) {
      int h = c.getFrom().height();
      if (h > max) {
        max = h;
      }
    }
    if (!getNonTaxonomicLinks().isEmpty()) {
      if (1 > max) {
        max = 1;
      }
    }

    return max + 1;
  }

  /**
   *
   * @return true if concept is selected
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   *
   * @param selected
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  /**
   * Returns the total leaf node count below this
   *
   * @return total leaf node count
   */
  public int nLeaves() {
    if (getChildSpecLinks().isEmpty()) {
      return 1;
    }
    int leaves = 0;
    for (KBLink c : getChildSpecLinks()) {
      int l = c.getFrom().nLeaves();
      leaves += l;
    }
    return leaves;
  }

  /**
   * Returns the total leaf node count below this, assuming the tree has a max-depth of depth.
   *
   * @param depth
   * @return total leaf node count
   */
  public int nLeaves(int depth) {
    if (getChildSpecLinks().isEmpty() || depth == 0) {
      return 1;
    }
    int leaves = 0;
    for (KBLink c : getChildSpecLinks()) {
      int l = c.getFrom().nLeaves(depth - 1);
      leaves += l;
    }
    return leaves;
  }

  /**
   *
   * @return incremented index
   */
  public synchronized int nextIndex() {
    lastConceptIndex++;
    return lastConceptIndex;
  }

  /**
   * Set selected flag to true.
   */
  public void setSelected() {
    this.selected = true;
  }

  /**
   * Set selected flag to false.
   */
  public void setUnselected() {
    this.selected = false;
  }

  /**
   *
   * @param candidate
   * @return true if the concept is subsumed in the the taxonomic links for candidate
   */
  public boolean subsumedBy(KBConcept candidate) {
    // System.out.println("SUBSUMED?:" + this + " by " + candidate);
    for (KBLink childLink : candidate.getChildSpecLinks()) {
      if (subsumedBy(childLink.getFrom())) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @return HTML for Taxonomy Viewer
   */
  public String toAnchor() {
    String script = "";
    int conceptNo = getIndex();
    if (this instanceof OpenCycConcept) {
      int depth = 2;
      int width = this.nLeaves(depth);
      script = writeGraphStarter("Cyc Concept #" + conceptNo + " " + this, conceptNo, this.getCycL(), depth, width);
    } else if (this instanceof NonCycConcept) {
      script = writeGraphStarter("NonCyc Team Concept #" + conceptNo + " " + this, conceptNo, this.getCycL(), 4, 5);
    }
    return "<a href='javascript:;' onclick=\"" + script + "\">" + getName() + "</a>";
  }

  /**
   *
   * @return HTML for Taxonomy Viewer
   */
  public String toCloseButton() {
    //there is a argument for this method going in the jscript class
    return "<button onclick='handleCloseConceptAnchor(\"" + getRef() + "\");'>&#x2612;</button>";
  }

  /**
   *
   * @param depth
   * @return D3JSON output
   */
  public abstract String toD3JSON(int depth);

  /**
   *
   * @param depth
   * @param dir
   * @return D3JSON output
   * @deprecated
   */
  @Deprecated
  abstract public String toD3JSON(int depth, GraphDirection dir);

  /**
   *
   * @param depth
   * @param depthLimit
   * @param dir
   * @return D3JSON output
   */
  abstract public String toD3JSON(int depth, int depthLimit, GraphDirection dir);

  /**
   *
   * @return D3JSON output
   */
  @Override
  public String toD3JSON() {
    return toD3JSON(0, GraphDirection.down);
  }

  /**
   *
   * @param forLink
   * @return D3JSON output
   */
  abstract public String toD3JSONNoRecursion(NonTaxonomicLink forLink);

  /**
   *
   * @param depth
   * @return JSON output
   */
  abstract public String toJSON(int depth);

  /**
   *
   * @param depth
   * @param dir
   * @return JSON output
   */
  abstract public String toJSON(int depth, GraphDirection dir);

  /**
   *
   * @return JSON output
   */
  @Override
  public String toJSON() {
    return toJSON(0, GraphDirection.down);
  }

  /**
   *
   * @return JSON output
   */
  public abstract String toJSONNoRecursion();

  /**
   *
   * @return HTML for Taxonomy Viewer
   */
  public String toLabelledSpan() {
    return "<span conceptID=\"" + getRef() + "\" onclick='handleClickedSelectedConceptSpan(\"" + getRef() + "\");'>" + getName() + "</span>";
  }

  /**
   *
   * @return HTML for Taxonomy Viewer
   */
  public String toSelectedAnchor() {
    return "<a class=\"selectedTerm\" href='javascript:;' onclick='handleClickedConceptAnchor(\"" + getRef() + "\");'>" + getName() + "</a>";
  }

  @Override
  public String toString() {
//    return "KBTaxonomy:" + conceptCycL + " names:" + getName() + " N children:" + getChildSpecLinks().size();
    return "KBTaxonomy:" + getConceptCycL() + " names:" + getName() + " N children:" + getNonTaxonomicLinks().size();
  }

  /**
   *
   * @return childSpecLinks
   */
  protected abstract Set<KBLink> getChildSpecLinks();

  /**
   *
   * @return children
   */
  protected abstract Set<KBConcept> getChildren();

  /**
   *
   * @return generalLinks
   */
  protected Set<KBLink> getGeneralLinks() {
    return generalLinks;
  }

  /**
   *
   * @return NonTaxonomicLinks
   */
  abstract protected Set<NonTaxonomicLink> getNonTaxonomicLinks();

  /**
   *
   * @return parentLinks
   */
  abstract protected Set<TaxonomicLink> getParentlinks();

  /**
   *
   * @return parents
   */
  abstract protected Set<KBConcept> getParents();

  /**
   *
   * @return true if the concept has already been expanded
   */
  protected boolean isExpanded() {
    return expanded;
  }

  /**
   * Expand the concept by setting the parents and children.
   */
  protected final void maybeExpand() {
    if (isExpanded()) {
      return;
    } else {
      setParents();
//      setParentLinks();
      setChildren();
//      setChildrenLinks();
      setExpanded();
      return;
    }
  }

  /**
   * Setter for children field.
   */
  protected abstract void setChildren();

  /**
   * Setter for childrenLinks field.
   */
  protected abstract void setChildrenLinks();

  /**
   * Set expanded flag to true.
   */
  protected void setExpanded() {
    this.expanded = true;
  }

  /**
   * Setter for parentLinks field.
   */
  protected abstract void setParentLinks();

  /**
   * Setter for parents field.
   */
  protected abstract void setParents();

  /**
   * Enumeration of possible graph directions.
   */
  public enum GraphDirection {

    up, down
  }
}
