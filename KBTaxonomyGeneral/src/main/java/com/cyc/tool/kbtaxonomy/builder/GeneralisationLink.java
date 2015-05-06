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
 * <p>
 * GeneralisationLink is a type of TaxonomicLink in which one concept is a specialisation of
 * another.
 *
 */
public class GeneralisationLink extends TaxonomicLink {

  /**
   * GeneralisationLink constructor
   * 
   * @param fromSpecific
   * @param toGeneral
   */
  public GeneralisationLink(KBConcept fromSpecific, KBConcept toGeneral) {
    super(fromSpecific, toGeneral);
  }

  /**
   * Factory method for creating a new GeneralisationLink object.
   * 
   * @param fromSpecific
   * @param toGeneral
   * @return GeneralisationLink object
   */
  public static GeneralisationLink create(KBConcept fromSpecific, KBConcept toGeneral) {
    if (toGeneral.getChildren().contains(fromSpecific)) {
      for (KBLink t : toGeneral.getChildSpecLinks()) {
        if (t.getFrom() == fromSpecific && (t instanceof GeneralisationLink)) {
          return (GeneralisationLink) t;
        }
      }
      assert false : fromSpecific + " was in the set of children of " + toGeneral
              + "but I couldn't find the generalisation link";
      return null;
    }
    return new GeneralisationLink(fromSpecific, toGeneral);
  }

  @Override
  public String getLinkTypeName() {
    return "Generalisation";
  }

  @Override
  public String toString() {
    return "[" + getFrom().getConceptCycL() + "] is a specialisation of [" + getTo().getConceptCycL() + "]";
  }
}
