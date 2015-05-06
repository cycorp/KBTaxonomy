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
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Represents taxonomic links in the KB.
 *
 */
public abstract class TaxonomicLink extends KBLink {

  private static final Set<KBConcept> allWithParents = new HashSet<>();

  /**
   * Create a new TaxonomicLink object.
   * 
   * @param fromSpecific
   * @param toGeneral
   */
  public TaxonomicLink(KBConcept fromSpecific, KBConcept toGeneral) {
    super(fromSpecific, toGeneral);

    if (toGeneral.subsumedBy(fromSpecific)) {
      throw new RuntimeException("ERROR: Tried to make a link from:" + fromSpecific + " up to " + toGeneral
              + " but " + fromSpecific + " subsumes " + toGeneral);
    }
    if (toGeneral instanceof NonCycConcept) {
      System.out.println("NOTE: found subsumption by NonCyc "
              + toGeneral + " over " + fromSpecific);
    }

    toGeneral.getChildren().add(fromSpecific);

    fromSpecific.getParents().add(toGeneral);
    allWithParents.add(fromSpecific);
    toGeneral.getChildSpecLinks().add(this);
    fromSpecific.getParentlinks().add(this);
  }

  /**
   *
   * @return a Set of KBConcept objects that are roots in the graph
   */
  public static Set<KBConcept> getRoots() {
    Set<KBConcept> difference = new HashSet<>(getAllNodes());
    difference.removeAll(allWithParents);
    return difference;
  }
}
