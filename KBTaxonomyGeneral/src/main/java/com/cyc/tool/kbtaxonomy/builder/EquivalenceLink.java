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
/**
 *
 * <p>
 * EquivalenceLink is a type of TaxonomicLink in which the two concepts are considered equivalent.
 *
 */
public class EquivalenceLink extends TaxonomicLink {

  /**
   * Constructor for a new EquivalenceLink object.
   * 
   * @param fromConcept
   * @param toEqivalent
   */
  public EquivalenceLink(KBConcept fromConcept, KBConcept toEqivalent) {
    super(fromConcept, toEqivalent);
  }

  /**
   * Factory method for creating a new EquivalenceLink object.
   * 
   * @param fromSpecific
   * @param toGeneral
   * @return a newly created EquivalenceLink object
   */
  public static EquivalenceLink create(KBConcept fromSpecific, KBConcept toGeneral) {
    if (toGeneral.getChildren().contains(fromSpecific)) {
      for (KBLink t : toGeneral.getChildSpecLinks()) {
        if (t.getFrom() == fromSpecific && (t instanceof EquivalenceLink)) {
          return (EquivalenceLink) t;
        }
      }
      assert false : fromSpecific + " was in the set of children of " + toGeneral
              + "but I couldn't find the link";
      return null;
    }
    return new EquivalenceLink(fromSpecific, toGeneral);
  }

  @Override
  public String getLinkTypeName() {
    return "Equivalence";
  }

  @Override
  public String toString() {

    return "[" + getFrom().getConceptCycL() + "] refers to the same thing as [" + getTo().getConceptCycL() + "]";
  }
}
