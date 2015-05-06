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
;

import com.cyc.library.json.JSONBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * An implementation of Taxonomy that defines a taxonomy from JSON input.
 *
 */


public class TaxonomyFromJson implements Taxonomy {

  final static Set<String> badLabels = new HashSet<>();
  static int line = 0;
  final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  Set<KBConcept> rootConcepts;

  /**
   * TaxonomyFromJson constructor.
   *
   */
  public TaxonomyFromJson() {
    this(0);
  }

  /**
   * TaxonomyFromJson constructor
   *
   * @param limitNAnswers
   */
  public TaxonomyFromJson(int limitNAnswers) {
    setRootConcepts();
  }

  /**
   * Create a taxonomy from a JSON file
   *
   * @param JSONFileName a plain JSON file or zipped with gz or gzip extension
   * @throws Exception
   */
  public TaxonomyFromJson(String JSONFileName) throws Exception {
    String content = null;
    File JSONFile = new File(JSONFileName);
    InputStream stream = getClass().getClassLoader().getResourceAsStream(JSONFileName);
    if (stream != null) { // Got the resource
      content = IOUtils.toString(stream);
    } else {
      //It isn't available as a resource - look for normal file
      if (!JSONFile.exists()) {
        throw new FileNotFoundException(JSONFileName + " does not exist");
      } else {
        System.out.println("The Stream for " + JSONFileName + " is " + stream);

        if (JSONFileName.endsWith(".gz") || JSONFileName.endsWith(".gzip")) {
          try (InputStream gzipped
                  = new GZIPInputStream(new FileInputStream(JSONFile))) {
            content = IOUtils.toString(gzipped);
          } catch (IOException e) {
            throw new RuntimeException("Failed to read gzipped file as stream " + JSONFileName);
          }
        }
      }
    }
    if (content == null) {
      throw new RuntimeException("Could not get JSON in " + JSONFileName + " as resource or file");
    }
    rootConcepts = new HashSet<>();
    List roots = new Gson().fromJson(content, List.class);
    assert roots.size() == 1 : "There should only be one root node in a loaded taxonomy; this one has " + roots.size();
    for (Object root : roots) {
      addDependent(root, 0);
    }
    System.out.println("LOADED");
  }

  private static String quote(String s) {
    return "\"" + s + "\"";
  }

  static String printList(List<Object> parents) {
    String res = "";
    for (Object o : parents) {
      res += o.toString().substring(0, 100) + ", ";
    }
    return res;
  }

  @Override
  public Set<KBConcept> getConcepts() {
    return KBConcept.getAllConcepts();
  }

  /**
   * @return the gson
   */
  public Gson getGson() {
    return gson;
  }

  @Override
  public int getLinkCount() {
    return TaxonomicLink.getAllLinks().size();
  }

  @Override
  public int getNodeCount() {
    return getConcepts().size();
  }

  /**
   * @return the rootConcepts
   */
  public Set<KBConcept> getRootConcepts() {
    return rootConcepts;
  }

  /**
   * @param rootConcepts the rootConcepts to set
   */
  public void setRootConcepts(Set<KBConcept> rootConcepts) {
    this.rootConcepts = rootConcepts;
  }

  /**
   *
   * @return D3JSON output
   */
  public String toD3JSON() {
    List<String> rootJasons = new ArrayList<>();
    for (KBConcept c : getRootConcepts()) {
      System.out.println("Making D3 Graph JSON for " + c);
      rootJasons.add(c.toD3JSON());
    }
    return flattenJSON(rootJasons, IsArray.NOTARRAY);

  }

  /**
   *
   * @return Gephi output
   */
  public String toGephi() {
    StringBuilder sb = new StringBuilder();
    for (KBConcept con : getRootConcepts()) {
      sb.append(toGephi(con, 0));
    }
    return sb.toString();
  }

  /**
   *
   * @param concept
   * @param depth
   * @return Gephi output
   */
  public String toGephi(KBConcept concept, int depth) {
    assert depth < 100 : "to Gephi reached depth limit 100";
    //Terminate on leaf nodes... they don't make edges
    if (concept.getChildSpecLinks().isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder(concept.getConceptCycL());
    for (KBLink link : concept.getChildSpecLinks()) {
      sb.append(",").append(quote(link.getFrom().getName()));
      //Double weight for Genls vs Isa
      if (link instanceof GeneralisationLink) {
        sb.append(",").append(quote(link.getFrom().getName()));
      }
      //triple weight for owl:sameAs vs Isa
      if (link instanceof EquivalenceLink) {
        sb.append(",").append(quote(link.getFrom().getName()));
        sb.append(",").append(quote(link.getFrom().getName()));
      }
    }
    sb.append("\n");
    for (KBLink link : concept.getChildSpecLinks()) {
      sb.append(toGephi(link.getFrom(), depth + 1));
    }
    return sb.toString();

  }

  /**
   *
   * @return JSON output
   */
  public String toJSON() {
    //  return gson.toJson(this);
    List<String> rootJasons = new ArrayList<>();
    for (KBConcept c : getRootConcepts()) {
      System.out.println("Making JSON for " + c);
      rootJasons.add(c.toJSON());
    }
    return flattenJSON(rootJasons, IsArray.ARRAY);
  }

  private void addLink(EdgeType ltype, KBConcept fromSpecific, KBConcept toGeneral) {

    if (ltype.getJsonName().equals("specializations")) {
      GeneralisationLink.create(fromSpecific, toGeneral);
    } else {
      NonTaxonomicLink.create(fromSpecific, toGeneral, ltype);
    }
  }

  private String flattenJSON(List<String> jsonRoots, IsArray at) {
    System.out.println("Trying to flatton " + jsonRoots.size() + " roots " + at);
    String flatJson = at == IsArray.ARRAY ? JSONBuilder.array(jsonRoots) : jsonRoots.get(0);
    try {
      JsonElement je = new JsonParser().parse(flatJson);
      String formattedJson = getGson().toJson(je);
      return formattedJson;
    } catch (com.google.gson.JsonSyntaxException e) {
      System.out.println(e + " in " + flatJson);
    }
    return flatJson;

  }

  private void setRootConcepts() {
    setRootConcepts(TaxonomicLink.getRoots());
  }

  final KBConcept addDependent(Object jsonObject, int depth) throws OWLOntologyCreationException {
    System.out.println("Current call: " + jsonObject.toString().substring(0, 100));
    assert depth < 100 : "100 is too deep for a taxonomy";
    String cycConceptTerm;
    String conceptUri;
    String openCycConstant;
    KBConcept thisConcept = null;

    assert (jsonObject instanceof Map) : "Got non map jsonObject " + jsonObject;
    Map<String, Object> jmap = (Map<String, Object>) jsonObject;

    String conceptType = (String) jmap.get("type");
    String printSeq = "[PS:" + jmap.get("printSequence").toString() + "]";

    String depthS = printSeq + ": [N" + getNodeCount() + "L" + getLinkCount() + ":" + depth + "]";
    assert (jmap.containsKey("kbTaxonomyCycConceptTerm")) : "Something is broken. got concept with no cycTerm";
    cycConceptTerm = jmap.get("kbTaxonomyCycConceptTerm").toString();
    try {
      conceptUri = jmap.get("hlid").toString();
      openCycConstant = jmap.get("openCycConstant").toString();
    } catch (NullPointerException e) {
      conceptUri = "";
      openCycConstant = "";
    }

    if (conceptType.equals("nonCycTeamConcept")) {
      assert (depth != 0) : " Seems wrong for a top level concept to be a NonCyc one " + cycConceptTerm;
      //It's a detectible "NonCyc concept" --- to do: add icon
      assert (jmap.containsKey("nonCycConceptName")) : "Found NonCyc Concept with no name " + cycConceptTerm;
      String nonCycName = jmap.get("nonCycConceptName").toString();
      assert (jmap.containsKey("nonCycTeamConceptID")) : "Found NonCyc Concept with no numeric ID " + cycConceptTerm;
      int nonCycID = (int) Math.round((Double) jmap.get("nonCycTeamConceptID"));
      thisConcept = NonCycConcept.create(cycConceptTerm, nonCycID, nonCycName, conceptUri);
      assert (!jmap.containsKey("specializations")) : "NonCyc Concepts shouldn't have specialisations";
      return thisConcept;
    }

    if (conceptType.equals("kbTaxonomyConcept")) {
      //It's a "taxonmy concept" --- so create an OpenCycConceptNode for it
      if (!jmap.containsKey("nlLabels")) {
        System.out.println("WARN: Cyc term: [" + cycConceptTerm + "] HAS NO LABEL");

        Set<String> fakeLabel = new HashSet<>();
        fakeLabel.add("[[[" + cycConceptTerm + "]]]");
        //  System.out.println((++line) + "CRE_CY" + cycConceptTerm + " NO NL LABEL USING "+fakeLabel);
        thisConcept = OpenCycConcept.create(cycConceptTerm, fakeLabel, conceptUri);
      } else {
        assert (jmap.containsKey("nlLabels")) : "Found Cyc Concept with no NL Labels " + cycConceptTerm;
        List labels = (List) jmap.get("nlLabels");
        List<String> lstrings = new ArrayList<>();
        for (Object l : labels) {
          lstrings.add(l.toString());
        }
        //  System.out.println((++line) + "CRE_CY" + cycConceptTerm + "," + lstrings);
        thisConcept = OpenCycConcept.create(cycConceptTerm, new HashSet<>(lstrings), conceptUri);
      }
      if (depth == 0) {
        getRootConcepts().add(thisConcept);
      }

      Set<String> possibleEdgeTypes = EdgeType.getPossibleEdgeTypes();

      for (String possibleEdgeType : possibleEdgeTypes) {
        if (jmap.containsKey(possibleEdgeType)) {
          List specs = (List) jmap.get(possibleEdgeType);
          for (Object spec : specs) {
            KBConcept child = addDependent(spec, depth + 1);
            if (null != child) {
              EdgeType theEdgeType = new EdgeType(possibleEdgeType);
              addLink(theEdgeType, child, thisConcept);
            }
          }
        }
      }
      return thisConcept;
    }
    assert false :
            "should always be a AU or Cyc node " + conceptType;
    return null;
  }

  int getRootCount() {
    return getRootConcepts().size();
  }

  String getRootNames() {
    return StringUtils.join(getRootConcepts(), ";\n");
  }

  private enum IsArray {

    ARRAY, NOTARRAY
  }

  enum NodeType {

    TAXONOMY, CONCEPT, POORLYCONNECTED
  }
}
