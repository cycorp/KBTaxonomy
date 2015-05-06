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


import java.util.Set;

/**
 * <P>Taxonomy is an interface for taxonomy implementations such as {@link TaxonomyFromOpenCyc}.
 *
 */
public interface Taxonomy {

  /**
   *
   * @return a Set of KBConcepts in the taxonomy
   */
  Set<KBConcept> getConcepts();

  /**
   *
   * @return the number of links in the taxonomy
   */
  int getLinkCount();

  /**
   *
   * @return the number of nodes in the taxonomy
   */
  int getNodeCount();

}
