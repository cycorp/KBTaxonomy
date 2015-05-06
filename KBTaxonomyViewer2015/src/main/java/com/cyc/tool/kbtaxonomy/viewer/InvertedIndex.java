package com.cyc.tool.kbtaxonomy.viewer;

/*
 * #%L
 * KBTaxonomyViewer2015
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
import com.cyc.tool.kbtaxonomy.builder.KBConcept;
import com.cyc.tool.kbtaxonomy.builder.NonCycConcept;
import com.cyc.tool.kbtaxonomy.builder.Taxonomy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * KB Taxonomy Inverted Index
 *
 * <p>
 * Allow for the lookup of taxonomy concepts based on name etc.
 *
 */
public class InvertedIndex {

  private static final boolean debug = false;
  static final String[] stopWordsA = {"a",
    "about",
    "above",
    "after",
    "again",
    "against",
    "all",
    "am",
    "an",
    "and",
    "any",
    "are",
    "aren't",
    "as",
    "at",
    "be",
    "because",
    "been",
    "before",
    "being",
    "below",
    "between",
    "both",
    "but",
    "by",
    "can't",
    "cannot",
    "could",
    "couldn't",
    "did",
    "didn't",
    "do",
    "does",
    "doesn't",
    "doing",
    "don't",
    "down",
    "during",
    "each",
    "few",
    "for",
    "from",
    "further",
    "had",
    "hadn't",
    "has",
    "hasn't",
    "have",
    "haven't",
    "having",
    "he",
    "he'd",
    "he'll",
    "he's",
    "her",
    "here",
    "here's",
    "hers",
    "herself",
    "him",
    "himself",
    "his",
    "how",
    "how's",
    "i",
    "i'd",
    "i'll",
    "i'm",
    "i've",
    "if",
    "in",
    "into",
    "is",
    "isn't",
    "it",
    "it's",
    "its",
    "itself",
    "let's",
    "me",
    "more",
    "most",
    "mustn't",
    "my",
    "myself",
    "no",
    "nor",
    "not",
    "of",
    "off",
    "on",
    "once",
    "only",
    "or",
    "other",
    "ought",
    "our",
    "ours ",
    "ourselves",
    "out",
    "over",
    "own",
    "same",
    "shan't",
    "she",
    "she'd",
    "she'll",
    "she's",
    "should",
    "shouldn't",
    "so",
    "some",
    "such",
    "than",
    "that",
    "that's",
    "the",
    "their",
    "theirs",
    "them",
    "themselves",
    "then",
    "there",
    "there's",
    "these",
    "they",
    "they'd",
    "they'll",
    "they're",
    "they've",
    "this",
    "those",
    "through",
    "to",
    "too",
    "under",
    "until",
    "up",
    "very",
    "was",
    "wasn't",
    "we",
    "we'd",
    "we'll",
    "we're",
    "we've",
    "were",
    "weren't",
    "what",
    "what's",
    "when",
    "when's",
    "where",
    "where's",
    "which",
    "while",
    "who",
    "who's",
    "whom",
    "why",
    "why's",
    "with",
    "won't",
    "would",
    "wouldn't",
    "you",
    "you'd",
    "you'll",
    "you're",
    "you've",
    "your",
    "yours",
    "yourself",
    "yourselves"};

  private Map<String, Set<KBConcept>> index = new HashMap<>();
  private final Set<String> stopSet;
  private final List<String> suffixes;
  String[] suffixesA = {
    "ation", "ition", "ative", "itive",
    "less", "tion", "ible", "able", "eous", "ious", "ment", "ness", "ting", "ping", "bing",
    "ion", "ing", "est", "ity", "ial", "ive", "ful", "ous",
    "al", "ly", "er", "or", "ty", "ic", "en", "er", "es", "ed",
    "s", "y"};

  /**
   * InvertedIndex constructor.
   *
   */
  public InvertedIndex() {
    stopSet = new HashSet<>();
    stopSet.addAll(Arrays.asList(stopWordsA));
    suffixes = Arrays.asList(suffixesA);
  }

  /**
   * InvertedIndex constructor
   *
   * @param taxonomy
   */
  public InvertedIndex(Taxonomy taxonomy) {
    this();
    indexTaxonomy(taxonomy);
    System.out.println("Indexed taxonomy with " + index.size() + " terms");
  }

  /**
   *
   * @param toSplit
   * @return List of Strings
   */
  public static List<String> splitter(String toSplit) {
    return Arrays.asList(toSplit.split("\\s+"));
  }

  /**
   * Add a concept to the index
   * 
   * @param concept
   * @param term
   */
  public void addConceptWithTerm(KBConcept concept, String term) {
    if (debug) {
      System.out.println("ACT:" + term);
    }
    makeSetIfNeeded(term);
    index.get(term).add(concept);
  }

  /**
   * Add a concept to the index
   * @param concept
   * @param terms
   */
  public void addConceptWithTerms(KBConcept concept, Collection<String> terms) {
    for (String term : expand(terms)) {
      if (!isIndexed(term)) {
        if (debug) {
          System.out.println("Indexing [" + term + "]");
        }
      }
      addConceptWithTerm(concept, term.toLowerCase());
    }
  }

  /**
   *
   * @param terms
   * @return a Set of Strings
   */
  public Set<String> expand(Collection<String> terms) {
    Set<String> result = new HashSet<>(terms);
    for (String term : terms) {
      //First camel case
      //http://stackoverflow.com/a/7594052/2133565
      //  for (String w : term.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
      for (String w : StringUtils.splitByCharacterTypeCamelCase(term)) {
        if (term.equals(w)) {
          continue;
        }
        if (w == null || w.length() == 0 || w.matches("(\\s|[-_])*")) {
          //  System.out.println("SKIP ["+w+"]");
          continue;
        }
        if (isStop(w)) {
          continue;
        }

        if (debug) {
          System.out.println("Camel: " + w);
        }
        result.add(w);
      }
      //Then on _ or -
      if (debug && term.contains("_")) {
        System.out.println("TERM:" + term);
      }

      for (String w : StringUtils.split(term, "-_()")) {
        if (term.equals(w)) {
          continue;
        }
        if (isStop(w)) {
          continue;
        }

        if (debug) {
          System.out.println("Sep: " + w);
        }
        result.add(w);
      }
    }
    // Try stemming
    Set<String> stemmed = new HashSet<>();
    for (String candidate : result) {
      String res = stem(candidate);
      if (null != res && res.length() > 1) {
        if (debug) {
          System.out.println("Stem: " + candidate + " -> " + res);
        }
        stemmed.add(res);
      }
    }
    result.addAll(stemmed);
    if (debug) {
      System.out.println("result: " + result);
    }
    return result;

  }

  /**
   * Index a taxonomy
   * @param taxonomy
   */
  public final void indexTaxonomy(Taxonomy taxonomy) {
    for (KBConcept concept : taxonomy.getConcepts()) {
      addConceptWithAllTerms(concept);
    }
  }

  /**
   *
   * @param term
   * @return true if the term is in the index
   */
  public boolean isIndexed(String term) {
    if (isStop(term)) {
      return true;
    }
    return (index.containsKey(term.toLowerCase()));
  }

  /**
   *
   * @param term
   * @return true if the term is in the stop set
   */
  public boolean isStop(String term) {
    if (stopSet.contains(term.toLowerCase())) {
      return true;
    }
    return false;
  }

  /**
   *
   * @return the index keyset
   */
  public Set<String> keys() {
    return index.keySet();
  }

  /**
   *
   * @param term
   * @return a KBConcept
   */
  public Set<KBConcept> search(String term) {
    if (!isIndexed(term) || isStop(term)) {
      return new HashSet<>();
    }
    return index.get(term.toLowerCase());
  }

  /**
   *
   * @param term
   * @return a KBConcept
   */
  public Set<KBConcept> search(KBConcept term) {
    return searchString(term.allTerms());

  }

  /**
   *
   * @param terms
   * @return a set of KBConcepts
   */
  public Set<KBConcept> searchConcept(Collection<NonCycConcept> terms) {

    return terms
            .stream()
            .map(t -> this.search(t))
            .flatMap(s -> s.stream())
            .collect(Collectors.toSet());
  }

  /**
   *
   * @param terms
   * @return a Set of KBConcepts
   */
  public Set<KBConcept> searchString(Collection<String> terms) {

    return terms
            .stream()
            .map(t -> this.search(t))
            .flatMap(s -> s.stream())
            .collect(Collectors.toSet());
  }

  /**
   *
   * @param term
   * @return a String
   */
  public String stem(String term) {
    for (String s : suffixes) {
      if (term.endsWith(s)) {

        String stemmed = term.substring(0, term.length() - s.length());
        int slen = stemmed.length();
        if (slen < 2) {
          return null;
        }
        if (stemmed.substring(slen - 1, slen).
                equalsIgnoreCase(stemmed.substring(slen - 2, slen - 1))) {
          stemmed = stemmed.substring(0, slen - 1);
        }
        if (stemmed.length() < 2) {
          return null;
        }
        return (stemmed);
      }
    }
    return null;
  }

  private void addConceptWithAllTerms(KBConcept concept) {
    addConceptWithTerms(concept, concept.allTerms());
  }

  private void makeSetIfNeeded(String term) {
    if (!index.containsKey(term)) {
      index.put(term, new HashSet<KBConcept>());
    }
  }

}
