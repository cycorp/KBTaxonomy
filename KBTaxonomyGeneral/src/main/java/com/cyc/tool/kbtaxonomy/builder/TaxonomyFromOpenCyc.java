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
import com.cyc.tool.owltools.OpenCycContent;
import com.cyc.tool.owltools.OpenCycOwl;
import com.cyc.tool.owltools.OpenCycReasoner;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * <P>
 * TaxonomyFromOpenCyc is an implementation of Taxonomy based on the OpenCyc KB.
 *
 */
public class TaxonomyFromOpenCyc implements Taxonomy {

  static final TaxonomyFromOpenCyc singleton = new TaxonomyFromOpenCyc();

  final Set<KBConcept> concepts;
  final OpenCycOwl ocyc;
  final OpenCycReasoner ocycReasoner;

  /**
   * Creates a new instance of TaxonomyFromOpenCyc.
   */
  private TaxonomyFromOpenCyc() {
    try {
      ocyc = new OpenCycOwl();
      ocycReasoner = OpenCycReasoner.get();
      concepts = getConcepts();
    } catch (IOException | OWLOntologyCreationException ex) {
      Logger.getLogger(TaxonomyFromOpenCyc.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException("Unable to create OpenCyc taxonomy" + ex);
    }
  }

  /**
   * Factory method to get a TaxonomyFromOpenCyc instance
   * 
   * @return
   * @throws IOException
   * @throws OWLOntologyCreationException
   */
  public static TaxonomyFromOpenCyc get() throws IOException, OWLOntologyCreationException {
    return singleton;
  }

  @Override
  public Set<KBConcept> getConcepts() {
    Set<KBConcept> concepts = new HashSet<>();
    List<String> allIRIs = ocycReasoner.getAllIRIs();
    for (String iri : allIRIs) {
      try {
        OpenCycContent c = new OpenCycContent(iri);
        KBConcept concept = OpenCycConcept.create(c.getLabelForConcept(), c.getPrettyStringsForConcept(), iri);
        concepts.add(concept);
      } catch (OWLOntologyCreationException ex) {
        Logger.getLogger(TaxonomyFromOpenCyc.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return concepts;
  }

  @Override
  public int getLinkCount() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int getNodeCount() {
    return concepts.size();
  }

//  @Override
//  public String toD3JSON() {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//
//  @Override
//  public String toJSON() {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
}
