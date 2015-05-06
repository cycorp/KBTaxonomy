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
 * An abstract class representing links in the KB.
 *
 */
public abstract class KBLink {

  static final Set<KBLink> allLinks = new HashSet<>();
  static final Set<KBConcept> allNodes = new HashSet<>();
  KBConcept from; //Usually more specific (in the case of Taxonomic Links)
  KBConcept to; // Usually more general (in the case of Taxonomic Links)

  /**
   * KBLink constructor
   * 
   * @param fromC
   * @param toC
   */
  public KBLink(KBConcept fromC, KBConcept toC) {
    from = fromC;
    to = toC;
    allNodes.add(from);
    allNodes.add(to);
    //Note - this leak of this is not guaranteed to be correct in a 
    // multi-threaded environment.
    //  it is possible that another thread may see "this" in allLinks before 
    //  it is fully initialised, due to code reordering.
    allLinks.add(this);
  }

  /**
   * @return the allLinks
   */
  public static Set<KBLink> getAllLinks() {
    return allLinks;
  }

  /**
   * @return the allNodes
   */
  public static Set<KBConcept> getAllNodes() {
    return allNodes;
  }

  /**
   * @return the from
   */
  public KBConcept getFrom() {
    return from;
  }

  /**
   * @param from the from to set
   */
  public void setFrom(KBConcept from) {
    this.from = from;
  }

  /**
   *
   * @return a String representing the link type name as it appears in a JSON file
   */
  public String getJSONLinkName() {
    return getLinkTypeName().toLowerCase();
  }

  /**
   *
   * @return a String representing the link type name
   */
  public abstract String getLinkTypeName();

  /**
   * @return the to
   */
  public KBConcept getTo() {
    return to;
  }

  /**
   * @param to the to to set
   */
  public void setTo(KBConcept to) {
    this.to = to;
  }

}
