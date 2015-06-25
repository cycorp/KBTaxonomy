//package com.cyc.tool.kbtaxonomy.viewer;
//
///*
// * #%L
// * KBTaxonomyViewer2015
// * %%
// * Copyright (C) 2015 Cycorp, Inc
// * %%
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *      http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * #L%
// */
//
//import com.cyc.tool.conceptfinder.ConceptMatch;
//import com.cyc.tool.conceptfinder.ConceptSpace;
//import com.cyc.tool.distributedrepresentations.BiologyW2VOpenCycSubspace;
//import com.cyc.tool.distributedrepresentations.Word2VecSpace;
//import com.cyc.tool.owltools.OpenCycContent;
//import com.cyc.tool.owltools.OpenCycOwl;
//import com.cyc.tool.kbtaxonomy.builder.OpenCycConcept;
//import static com.cyc.tool.kbtaxonomy.viewer.JavascriptGraphs.jsPackages;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//
///** 
// * <P>BioOpenCycViewer contains methods for implementing a Taxonomy Viewer that uses the 
// * Biology W2V space and OpenCyc.
// *
// */
//public class BioOpenCycViewer extends OpenCycViewer {
//  
//  private ConceptSpace bioOcycConceptSpace;
//  private OpenCycOwl ocyc;
//
//  String nearestOCycQuery;
//  
//
//  /** Creates a new instance of BioOpenCycViewer. */
//  public BioOpenCycViewer() {
//  }
//  
//  @Override
//  public String getNearestOCycQuery() {
//    return nearestOCycQuery;
//  }
//
//  @Override
//  public void setNearestOCycQuery(String query) {
//    nearestOCycQuery = query;
//  }
//  
//  @Override
//  public String getPagePrologue(WebParams params, String version) {
//    String page = "<!DOCTYPE html>"
//            + "<meta charset=\"utf-8\">\n"
//            + "<head>\n";
//    page += JavascriptGraphs.cssLinks();
//    page += jsPackages(params);
////    final String conceptDetailEnabledParam = params.serialize().getConceptDetailEnabled(true);
//    page += "</head>\n"
//            + "<body><h1>Biology Concepts Search</h1>\n"
//            + "Version " + version + "\n";
//    return page;
//  }
//  
//  @Override
//  public String prepareNearOpenCycTerms() {
//    String nameForFS = "BioSearch";
//    selectedOCycConcepts.clear();
//    nearestOCyc.clear();
//    Collection added = new HashSet<String>();
//    try {
//      setUpSpaceIfNeeded();
//      List<ConceptMatch> nearTerms = new ArrayList<>();
//
//      nearTerms = bioOcycConceptSpace.findNearestNForPosition(nearestOCycQuery, 40, t -> String.join(" | ", ocyc.conceptsFor(t)));
//      for (ConceptMatch match : nearTerms) {
//        System.out.println("Match:" + match);
//        Arrays.asList(match.getConcept().split("\\s*\\|\\s*"))
//                .forEach(concept -> {
//                  OpenCycContent instance;
//                  if (!added.contains(concept)) {
//                    added.add(concept);
//                    try {
//                      instance = new OpenCycContent(concept.replaceAll("http://sw.opencyc.org/concept/", ""));
//                      String label = instance.getLabelForConcept();
//                      Set<String> nlNames = new HashSet<>();
//                      nlNames.add(label.replaceAll("#\\$", ""));
//                      nearestOCyc.add(OpenCycConcept.create(label.replaceAll("#\\$", ""), nlNames, concept.replaceAll("http://sw.opencyc.org/concept/", "")));
//                      // selectedOCycConcepts.add(label.replaceAll("#\\$", ""));
//                    } catch (OWLOntologyCreationException ex) {
//                      Logger.getLogger(BioOpenCycViewer.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                  }
//                });
//
//      }
//
//    } catch (OWLOntologyCreationException | IOException | Word2VecSpace.NoWordToVecVectorForTerm ex) {
//      Logger.getLogger(OpenCycViewer.class
//              .getName()).log(Level.SEVERE, null, ex);
//    }
//return nameForFS;
//  }
//
//  @Override
//  public void setUpSpaceIfNeeded() throws IOException, OWLOntologyCreationException {
//    if (ocyc == null) {
//      ocyc = new OpenCycOwl();
//    }
//    if (bioOcycConceptSpace == null) {
//      
//      bioOcycConceptSpace = new ConceptSpace(BiologyW2VOpenCycSubspace.get());
//    }
//  }
//
//
//}
