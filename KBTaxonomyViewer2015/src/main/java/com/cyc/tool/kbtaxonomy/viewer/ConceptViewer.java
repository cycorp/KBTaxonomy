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
import com.cyc.tool.kbtaxonomy.builder.KBConcept;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/** 
 * <P>ConceptViewer is an interface for classes that implement taxonomy viewers.
 *
 */
public interface ConceptViewer {
  
  /**
   *
   * @param page
   * @return concept search form
   */
  public String addConceptSearchForm(String page);
  
  /**
   *
   * @param page
   * @return nearest term search form
   */
  public String addNearestTermSearchForm(String page);

  public String addQuerySearchForm(String page);
  
  /**
   *
   * @param page
   * @return XML load form
   */
  public String addXMLLoadForm(String page);
  
  /**
   * Convenience method to clear out lists for a new search
   */
  public void clearLists();

  /**
   *
   * @param sel
   * @return featureList
   */
  public String getFeatureList(Collection<NonCycConcept> sel);

  /**
   *
   * @return an InvertedIndex 
   */
  public InvertedIndex getIndex();

  /**
   *
   * @return lastFileConcepts
   */
  public Set<NonCycConcept> getLastFileConcepts();
  
  /**
   *
   * @return nearestOCyc
   */
  public Collection<KBConcept> getNearestOCyc();

  /**
   *
   * @return nearestOCycQuery
   */
  public String getNearestOCycQuery();

  /**
   *
   * @param query
   */
  public void setNearestOCycQuery(String query);

  /**
   *
   * @param page
   * @return list of file terms for display
   */
  public String getPageFileList(String page);
  
  /**
   *
   * @param page
   * @param found
   * @return list of OpenCyc concepts for display
   */
  public String getPageOpenCycList(String page, Collection<KBConcept> found);

  /**
   *
   * @param params
   * @param version
   * @return HTML for page
   */
  public String getPagePrologue(WebParams params, String version);

  /**
   *
   * @param page
   * @param found
   * @return list of team concepts for display
   */
  public String getPageTeamConceptList(String page, Collection<KBConcept> found);
  
  /**
   *
   * @return JSON filename String
   */
  public String getPathToJSON();

  /**
   *
   * @param aPathToJSON
   */
  public void setPathToJSON(String aPathToJSON);

  /**
   *
   * @return selectedConcepts
   */
  public Set<NonCycConcept> getSelectedConcepts();

  public void getSelectedConceptsFromConceptSearch(String get);

  /**
   *
   * @param get
   */
  public void getSelectedConceptsFromParameters(String get);
  
  /**
   *
   * @return selectedOCycConcepts
   */
  public Collection<String> getSelectedOCycConcepts();
  
  /**
   *
   * @param get
   */
  public void getSelectedOCycConceptsFromParameters(String get);
  
  /**
   * Load a taxonomy
   */
  public void loadTaxonomy();

  /**
   *
   * @param file
   */
  public void loadXMLQueryFile(String file);

  /**
   * Analyze matches to W2V space to find OpenCyc concepts
   *
   */
  public void prepareNearOpenCycTerms();

  /**
   * Check if W2V space needs to be created and create it if needed
   *
   * @throws IOException
   * @throws OWLOntologyCreationException
   */
  public void setUpSpaceIfNeeded() throws IOException, OWLOntologyCreationException;
  
}
