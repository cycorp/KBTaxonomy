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
import com.cyc.tool.conceptfinder.ConceptMatch;
import com.cyc.tool.conceptfinder.ConceptSpace;
import com.cyc.tool.conceptfinder.Passage;
import com.cyc.tool.distributedrepresentations.GoogleNewsW2VOpenCycSubspace;
import com.cyc.tool.distributedrepresentations.Word2VecSpace;
import com.cyc.tool.owltools.OpenCycContent;
import com.cyc.tool.owltools.OpenCycOwl;
import com.cyc.tool.kbtaxonomy.builder.NonCycConcept;
import com.cyc.tool.kbtaxonomy.builder.OpenCycConcept;
import com.cyc.tool.kbtaxonomy.builder.Taxonomy;
import com.cyc.tool.kbtaxonomy.builder.TaxonomyFromOpenCyc;
import com.cyc.tool.kbtaxonomy.builder.KBConcept;
import static com.cyc.tool.kbtaxonomy.viewer.JavascriptGraphs.jsPackages;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * <P>
 * OpenCycViewer contains methods for implementing a Taxonomy Viewer with OpenCyc concepts and the
 * GoogleNew W2V space.
 */
public class OpenCycViewer implements ConceptViewer {

  /**
   * A Collection of KBConcept instances that are matches in the W2V space
   */
  protected static final Collection<KBConcept> nearestOCyc = new ArrayList<>();

  /**
   * A collection of Strings to display for matched OpenCyc concepts
   */
  final static protected Collection<String> selectedOCycConcepts = new ArrayList<>();
  static final Set<String> nonGraphingConcepts = new HashSet<>(Arrays.asList(
          "Event", "Homeotherm", "InformationTransferEvent", "ControllingSomething",
          "IntrinsicStateChangeEvent", "PhysicalCreationEvent", "Event-Localized",
          "Conveying-Generic",
          //  "BodyMovementEvent",
          "Movement-TranslationEvent",
          "AnimalActivity", "MovementEvent", "PhysicalEvent", "AtLeastPartiallyMentalEvent",
          "CompositePhysicalAndMentalEvent", "Animal", "LocomotionEvent", "SocialOccurrence",
          "CreationEvent", "TransportationEvent", "PhysicalContactEvent", "Person",
          "InformationTransferPhysicalEvent"
  ));
  private String nearestOCycQuery;
  private InvertedIndex oCycIndex;
  private OpenCycOwl ocyc;

  private ConceptSpace ocycConceptSpace;

  //// Constructors
  /**
   * Creates a new instance of OpenCycViewer.
   */
  public OpenCycViewer() {
  }

  @Override
  public void addAllConceptsToXML(Set<NonCycConcept> prepareNonCycConceptsFromNearOpenCycTerms, String nameForFS) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String addConceptSearchForm(String page) {
    page += "<form action='?' method='get'>\n" + "  concepts: <input type='text' name='searchterms'><input type=\"submit\" value=\"Submit\">\n" + "</form>\n";
    return page;
  }

  @Override
  public String addNearestTermSearchForm(String page) {
    page += "<form action='?' method='get'>\n" + "  nearby terms: <input type='text' name='nearestterms'><input type=\"submit\" value=\"Submit\"></p>\n" + "</form>\n";
    return page;
  }

  @Override
  public String addOptionsSelectForm(String page) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String addQuerySearchForm(String page) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String addSearchSelectForm(String page) {
    page += "<select form='searchSelectorForm' name='searchSelect'>\n";
    page += "<option value='nearestterms' title='Search for nearest OpenCyc concepts in a Word2Vec space'>Nearby Term Search</option>\n";
    page += "<option value='searchterms' title='Perform a lexical search to find OpenCyc concepts'>Lexical Match Search</option>\n";
    page += "</select>\n";
    page += "<form action'?' method='get' id='searchSelectorForm'>\n";
    page += "<input type='text' name='inputText'><input type='submit' >\n";
    page += "</form>\n";
    return page;
  }

  @Override
  public String addXMLLoadForm(String page) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void clearLists() {
    nearestOCyc.clear();
    nearestOCycQuery = null;
    selectedOCycConcepts.clear();
  }

  @Override
  public String getFeatureList(Collection<NonCycConcept> sel) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getFileN() {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public InvertedIndex getIndex() {
    return oCycIndex;
  }

  @Override
  public Set<NonCycConcept> getLastFileConcepts() {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getLastSaveF() {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Collection<KBConcept> getNearestOCyc() {
    return nearestOCyc;
  }

  @Override
  public String getNearestOCycQuery() {
    return nearestOCycQuery;
  }

  @Override
  public void setNearestOCycQuery(String query) {
    nearestOCycQuery = query;
  }

  @Override
  public String getPageEpilogue(WebParams params, String page) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getPageFileList(String page) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getPageOpenCycList(String page, Collection<KBConcept> found) {
    page += "<hr><div style=\"background-color: #f5eeff;\">\n" + "<h2>OpenCyc Concepts</h2>\n";
    page += found.
            stream().
            filter((KBConcept v) -> v instanceof OpenCycConcept).
            map((KBConcept v) -> (OpenCycViewer.nonGraphingConcepts.contains(v.getCycL()) ? v.getNameUnclick() : v.toAnchor())).
            collect(Collectors.joining(", \n")) + "<hr></div>";
    return page;
  }

  @Override
  public String getPagePrologue(WebParams params, String version) {
    String page = "<!DOCTYPE html>"
            + "<meta charset=\"utf-8\">\n"
            + "<head>\n";
    page += JavascriptGraphs.cssLinks();
    page += jsPackages(params);
//    final String conceptDetailEnabledParam = params.serialize().getConceptDetailEnabled(true);
    page += "</head>\n"
            + "<body><h1>OpenCyc Concepts Search</h1>\n"
            + "Version " + version + "<br>";
    return page;
  }

  @Override
  public String getPageTeamConceptList(String page, Collection<KBConcept> found) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getPathToJSON() {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setPathToJSON(String aPathToJSON) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Set<NonCycConcept> getSelectedConcepts() {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void getSelectedConceptsFromConceptSearch(String get) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void getSelectedConceptsFromParameters(String get) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Collection<String> getSelectedOCycConcepts() {
    return selectedOCycConcepts;
  }

  @Override
  public void getSelectedOCycConceptsFromParameters(String parameter) {
    selectedOCycConcepts.clear();
    for (String p : parameter.split("\\s+")) {
      selectedOCycConcepts.add(p);
    }
  }

  @Override
  public void loadTaxonomy() {
    try {
      Taxonomy myOpenCycTaxonomy = TaxonomyFromOpenCyc.get();
      System.out.println("Loaded OpenCyc Taxonomy with " + myOpenCycTaxonomy.getNodeCount() + " concepts");
      oCycIndex = new InvertedIndex(myOpenCycTaxonomy);
    } catch (IOException | OWLOntologyCreationException ex) {
      Logger.getLogger(OpenCycViewer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void loadXMLQueryFile(String file) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  public String prepareNearOpenCycTerms(String queryString) {
    String nameForFS = "";
    selectedOCycConcepts.clear();
    nearestOCyc.clear();
    Collection added = new HashSet<String>();

    try {
      Passage passageInstance = new Passage(queryString);

      setUpSpaceIfNeeded();
      List<ConceptMatch> nearTerms = new ArrayList<>();

//      nearTerms = ocycConceptSpace.findNearestNForPosition(queryString, 40, t -> String.join(" | ", ocyc.conceptsFor(t)));
      nearTerms = passageInstance.narrowConceptsForPassage(passageInstance.findConceptsForPassage());
      for (ConceptMatch match : nearTerms) {
        System.out.println("Match: " + match);
        Arrays.asList(match.getConcept().split("\\s*\\|\\s*"))
                .forEach(concept -> {
                  OpenCycContent instance;
                  if (!added.contains(concept)) {
                    added.add(concept);
                    try {
                      instance = new OpenCycContent(concept.replaceAll("http://sw.opencyc.org/concept/", ""));
                      String label = instance.getLabelForConcept();
                      Set<String> nlNames = new HashSet<>();
                      nlNames.add(label.replaceAll("#\\$", ""));
                      nearestOCyc.add(OpenCycConcept.create(label.replaceAll("#\\$", ""), nlNames, concept.replaceAll("http://sw.opencyc.org/concept/", "")));
                      // selectedOCycConcepts.add(label.replaceAll("#\\$", ""));
                    } catch (OWLOntologyCreationException ex) {
                      Logger.getLogger(OpenCycViewer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                  }
                });

      }
      nameForFS = passageInstance.getShortText();
    } catch (OWLOntologyCreationException | IOException ex) {
      Logger.getLogger(OpenCycViewer.class
              .getName()).log(Level.SEVERE, null, ex);
    }
    return nameForFS;
  }

  @Override
  public String prepareNearOpenCycTerms() {
    return prepareNearOpenCycTerms(nearestOCycQuery);
  }

  @Override
  public Set<NonCycConcept> prepareNonCycConceptsFromNearOpenCycTerms(Collection<KBConcept> nearestOCyc) {
    throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setUpSpaceIfNeeded() throws IOException, OWLOntologyCreationException {
    if (ocyc == null) {
      ocyc = new OpenCycOwl();
    }
    if (ocycConceptSpace == null) {

      ocycConceptSpace = new ConceptSpace(GoogleNewsW2VOpenCycSubspace.get());
    }
  }

    }
