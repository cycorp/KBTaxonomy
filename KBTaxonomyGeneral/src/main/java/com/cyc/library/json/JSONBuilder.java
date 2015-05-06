package com.cyc.library.json;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility Class for constructing JSON representations
 *
 * <p> A set of static methods that describe the construction of valid JSON strings
 *
 */
public class JSONBuilder {

  /**
   *
   * @param jsons
   * @return a String
   */
  public static String array(Collection<String>jsons){
    return "["+ StringUtils.join(jsons,",")+"]";
  }
 
  /**
   *
   * @param strings
   * @return a String
   */
  public static String arrayOfString(Collection<String>strings){
    List<String>wrapped=new ArrayList<>();
    for (String s: strings){
      wrapped.add(jsonQuote(s));
    }
    return array(wrapped);
  }

  /**
   *
   * @param field
   * @param value
   * @return a String
   */
  public static String fieldStringValuePair(String field, String value){
    
   return fieldValuePair(field, jsonQuote(value));
  }

  /**
   *
   * @param field
   * @param value
   * @return a String
   */
  public static String fieldValuePair(String field, String value){
    return "\""+field+"\":"+value;
  }

  /**
   *
   * @param field
   * @param value
   * @return a String
   */
  public static String fieldValuePair(String field, Number value){
    return "\""+field+"\":"+value;
  }

  /**
   *
   * @param field
   * @param value
   * @return a String
   */
  public static String fieldValuePair(String field, boolean value){
    return "\""+field+"\":"+(value?"true":"false");
  }

  /**
   *
   * @param s
   * @return a String
   */
  public static String jsonQuote(String s){
    assert !s.contains("\"")
            : JSONBuilder.class.getName()+" Problem need to handle escaping quote marks in ["+s+"]";
    return  "\""+s+"\"";
  }

  /**
   *
   * @param jsons
   * @return a String
   */
  public static String object(List<String>jsons){
    return "{"+ StringUtils.join(jsons,",")+"}";
  }
}
