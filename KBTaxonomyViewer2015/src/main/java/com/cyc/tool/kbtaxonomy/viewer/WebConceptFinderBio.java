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
import com.cyc.tool.kbtaxonomy.builder.OpenCycConcept;
import static com.cyc.tool.kbtaxonomy.viewer.ResourceServer.RESOURCE_BASE;
import com.cyc.tool.owltools.OpenCycContent;
import fi.iki.elonen.NanoHTTPD;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * <p>
 * Web Interface for searching for and (possibly) adding concepts to a KB Taxonomy search query.
 * This version runs the Taxonomy Viewer with the Biology Word2Vec space.
 *
 */
public class WebConceptFinderBio extends NanoHTTPD {

  private static ConceptViewer bViewer;
  private static final boolean debug = false;
  private static ConceptViewer oViewer = new OpenCycViewer();

  private static int port = 8081;
//  private static int port = 8082;

  private static final String version = "1.0";

  private String graphShape;

  private ResourceServer resourceServer = null;

  /**
   * WebConceptFinderNew constructor.
   */
  public WebConceptFinderBio() {
    super(port);
  }

  /**
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    processOptions(args);
    bViewer = new BioOpenCycViewer();
    // OpenCyc Taxonomy
    oViewer.loadTaxonomy();

    System.out.println("\n\nConnect on " + InetAddress.getLocalHost().getCanonicalHostName() + ":" + port);
    LocalServerRunner
            .run(WebConceptFinderBio.class
            );
  }

  private static void processOptions(String[] args) {

    try { // Processing the command line
      Options op = new Options();
      //   op.addOption("h", true, "host name the server will run on");
      op.addOption("p", true, "port the server will run on");
      CommandLineParser clp = new PosixParser();
      CommandLine cmd = clp.parse(op, args);

      if (cmd.hasOption("p")) {
        port = Integer.parseInt(cmd.getOptionValue("p"));

      }
    } catch (ParseException ex) {
      //If CL parsing fails we use default options
      Logger.getLogger(WebConceptFinderBio.class
              .getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
    NanoHTTPD.Method method = session.getMethod();
    String uri = session.getUri();
    if (debug) {
      System.out.println(method + " '" + uri + "' ");
    }

    if (session.getUri().startsWith(RESOURCE_BASE)) {
      return serveFile(session.getUri().substring(RESOURCE_BASE.length() + 1));
    }

    Map<String, String> webParams = session.getParms();
    final WebParams params = new WebParams(session.getParms());

    /* Process form inputs */
    if (webParams.get("childData") != null) {
      String id = webParams.get("childData");
      String heightStr = webParams.get("height");
      int height = heightStr != null ? Integer.parseInt(heightStr) : 1;
      KBConcept v = KBConcept.getExpandedConcept(id);
      String json = (webParams.get("direction") != null && webParams.get("direction").equals("up"))
              ? v.toD3JSON(0, height, KBConcept.GraphDirection.up)
              : v.toD3JSON(0, height, KBConcept.GraphDirection.down);
      System.out.println("Returning data for " + id);
      return new NanoHTTPD.Response(json);
    }

    if (webParams.get("getNodeData") != null) {
      try {
        OpenCycContent ocycc;
        ocycc = new OpenCycContent(webParams.get("getNodeData"));
        String ocycContent = ocycc.generateHtmlForConcept();
        return new NanoHTTPD.Response(ocycContent);
      } catch (OWLOntologyCreationException ex) {
        Logger.getLogger(WebConceptFinderBio.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    if (webParams.get("terms") != null) {
      bViewer.getSelectedConceptsFromParameters(webParams.get("terms"));
    }

    if (webParams.get(
            "searchterms") != null) {
      clearLists();
      oViewer.getSelectedOCycConceptsFromParameters(webParams.get("searchterms"));
    }

    if (webParams.get(
            "nearestterms") != null) {
      clearLists();
      bViewer.setNearestOCycQuery(webParams.get("nearestterms"));
    }

    if (webParams.get(
            "graphShape") != null) {
      graphShape = webParams.get("graphShape");
      setGraphShape(graphShape);
    } else {
      graphShape = "linear"; //default
    }
    /* Produce the page */
    String scripts = "";
    String page;
    // Prologue and search boxes
    page = bViewer.getPagePrologue(params, version);
    page += "<div id='debug'> </div>\n\n\n";
    page += "Search for: ";
    page = oViewer.addNearestTermSearchForm(page);
    page = oViewer.addConceptSearchForm(page);

    Set<NonCycConcept> conceptsToGraph = new HashSet<>();

    if (bViewer.getNearestOCycQuery() != null) {
      bViewer.prepareNearOpenCycTerms();
    } else if (oViewer.getNearestOCycQuery() != null) {
      oViewer.prepareNearOpenCycTerms();
    }

    if ((!conceptsToGraph.isEmpty())
            || !oViewer.getSelectedOCycConcepts().isEmpty()
            || !oViewer.getNearestOCyc().isEmpty()) {
      Collection<KBConcept> found = new HashSet<>();
      if (!oViewer.getNearestOCyc().isEmpty()) {
        found = oViewer.getNearestOCyc();
      } else {
        found.addAll(oViewer.getIndex().searchString(oViewer.getSelectedOCycConcepts()));
      }

      if (found == null) {
        page += "No results.";
      } else {

        for (KBConcept v : found) {
          int conceptNo = v.getIndex();
          String ref = v.getRef();
          page += "<div class=\"cgraph\" id=\"" + ref + "\" style=\"display:none;\"><hr>";
          if (v instanceof OpenCycConcept) {
            page += "\n\n" + v.toCloseButton();// + ": Concept: <i>" + v.getCycL() + "</i><br>" + v.getName();
            if (OpenCycViewer.nonGraphingConcepts.contains(v.getCycL())) {
              page += "<b>Skipping graph </b>";
            } else {
              page += //   "<br>"+v.nLeaves()+
                      " \n<div id=\"area" + conceptNo + "\"></div>";
            }
          } 
          page += "<hr></div>\n";
        }
        page = addListsOfConcepts(page, found);

      }
    }
    page = addDirectionsForm(page);
    page += scripts + "</body>\n";

    return new NanoHTTPD.Response(page);
  }

  private String addDirectionsForm(String page) {
    page
            += "<br><h2>Directions:</h2>\n"
            + "<ul>\n"
            + "<li>Left click in the graph panel to pan around the graph.\n"
            + "<li>Right click on a purple node to see additional information from OpenCyc.\n"
            + "<li>Shift click on a purple node to redraw the graph with that node as the root.\n"
            + "</ul>";
    return page;
  }

  private String addListsOfConcepts(String page, Collection<KBConcept> found) {
    page = oViewer.getPageOpenCycList(page, found);
    return page;
  }

  private void clearLists() {
    oViewer.clearLists();
  }

  private void setGraphShape(String graphShape) throws RuntimeException {
    if (graphShape != null) {
      switch (graphShape) {
        case "cluster":
          JavascriptGraphs.readGraphJS("ClusterDendogram");
          break;
        case "linear":
          JavascriptGraphs.setAllowCircles(false);
          break;
        case "radial":
          JavascriptGraphs.setAllowCircles(true);
          break;
        default:
          throw new RuntimeException("Bad value for radio button");

      }
    }
  }

  private NanoHTTPD.Response serveFile(String filename) {
    if (resourceServer == null) {
      resourceServer = new ResourceServer();
    }
    return resourceServer.serveFile(filename);
  }

  private static class LocalServerRunner {

    public static void run(Class serverClass) {
      try {
        executeInstance((NanoHTTPD) serverClass.newInstance());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private static void executeInstance(NanoHTTPD server) throws Exception {
      try {
        server.start();
        System.out.println("Server started, CTRL-C to stop.\n");
        // sleep forever
        while (server.isAlive()) {
          Thread.sleep(5000);
        }

        // try {
        //     System.in.read();
        // } catch (Throwable ignored) {
        // }
      } catch (InterruptedException ignore) {
      } finally {
        server.stop();
        System.out.println("Server stopped.\n");
      }

    }
  }

}
