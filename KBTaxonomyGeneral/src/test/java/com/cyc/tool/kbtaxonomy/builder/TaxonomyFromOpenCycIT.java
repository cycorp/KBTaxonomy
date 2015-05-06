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
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TaxonomyFromOpenCycIT {
  
  public TaxonomyFromOpenCycIT() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  
  /**
   * Test of getConcepts method, of class TaxonomyFromOpenCyc.
   */
  @Test
  public void testGetConcepts() throws Exception {
    System.out.println("getConcepts");
    TaxonomyFromOpenCyc instance = TaxonomyFromOpenCyc.get();
    int expResult = 116828;
    Set<KBConcept> result = instance.getConcepts();
    assertEquals(expResult, result.size());
  }  
}
