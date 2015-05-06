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
 * <P>
 * NonTaxonomicLink is a type of KBLink representing any link type that is not taxonomic.
 *
 */
public class NonTaxonomicLink extends KBLink {

  private EdgeType edgeType;

  /**
   * Creates a new instance of NonTaxonomicLink.
   * 
   * @param topicType
   * @param relatedType
   * @param lt
   */
  public NonTaxonomicLink(KBConcept topicType, KBConcept relatedType, EdgeType lt) {
    super(topicType, relatedType);
    this.edgeType = lt;
    topicType.getNonTaxonomicLinks().add(this);
  }

  /**
   * Factory method to create a new NonTaxonomicLink
   * 
   * @param relatedType
   * @param topicType
   * @param lt
   * @return a NonTaxonomicLink
   */
  public static NonTaxonomicLink create(KBConcept relatedType, KBConcept topicType, EdgeType lt) {
    for (KBLink t : topicType.getNonTaxonomicLinks()) {
      if (t.getTo() == relatedType) {
        return (NonTaxonomicLink) t;
      }
    }
    return new NonTaxonomicLink(topicType, relatedType, lt);
  }

  /**
   *
   * @return the color to use for this link in the graph
   */
  public String getLinkColour() {
    return edgeType.getLinkColour();
  }

  @Override
  public String getLinkTypeName() {
    return edgeType.getJsonName();
  }

}
