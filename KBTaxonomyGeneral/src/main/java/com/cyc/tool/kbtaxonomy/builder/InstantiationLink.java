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
 * InstantiationLink is a type of TaxonomicLink in which one concept is an instance of another.
 *
 */
public class InstantiationLink extends TaxonomicLink {

  /**
   * InstantiationLink constructor
   * 
   * @param fromSpecific
   * @param toGeneral
   */
  public InstantiationLink(KBConcept fromSpecific, KBConcept toGeneral) {
    super(fromSpecific, toGeneral);
  }

  /**
   * Factory method for creating a new InstantiationLink object.
   * 
   * @param fromSpecific
   * @param toGeneral
   * @return a newly created InstantiationLink object
   */
  public static InstantiationLink create(KBConcept fromSpecific, KBConcept toGeneral) {
    if (toGeneral.getChildren().contains(fromSpecific)) {
      for (KBLink t : toGeneral.getChildSpecLinks()) {
        if (t.getFrom() == fromSpecific && (t instanceof InstantiationLink)) {
          return (InstantiationLink) t;
        }
      }
      assert false : fromSpecific + " was in the set of children of " + toGeneral
              + "but I couldn't find the generalisation link";
      return null;
    }
    return new InstantiationLink(fromSpecific, toGeneral);
  }

  @Override
  public String getLinkTypeName() {
    return "Instantiation";
  }

  @Override
  public String toString() {
    return "[" + getFrom().getConceptCycL() + "] instantiates [" + getTo().getConceptCycL() + "]";
  }
}
