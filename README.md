KBTaxonomy Viewer
=================

Version 1.0

Included
--------

Projects:
* KBTaxonomyParent - The parent pom for the other projects
* KBTaxonomyGeneral - Supporting classes for the Taxonomy Viewer
* KBTaxonomyViewer2015 -- The Taxonomy Viewer

Other files:

* This README file

Requirements
------------

* These projects require Java 1.8.
* The Taxonomy Viewer requires NanoHttpd, which you can download here: (https://github.com/NanoHttpd/nanohttpd).
  * You will need to build the core and webserver projects from NanoHttpd in your local Maven repository in order to 
  run the TaxonomyViewer.
* Users must also install the projects located in the DistributedRepresentations repository.  If you haven't already
done this, do this first!
* **_This code has not yet been tested on Windows._**

Description and Usage
---------------------

The Taxonomy Viewer provides ways to search the OpenCyc KB taxonomy and graph the relationships between OpenCyc concepts.
To make it easier to find concepts in the taxonomy, the Viewer takes advantage of the projects in the 
DistributedRepresentations repository so that users can find OpenCyc concepts that are related to their search terms, 
even if OpenCyc doesn't have an exact match for the search terms.  For more information on this, see the documentation
in the DistributedRepresentations repository.

To run the Taxonomy Viewer, install each of the projects in this repository along with those in the DistributedRepresentations repository.  Once everything has been built successfully, run the `WebConceptFinderDefault.java` class in KBTaxonomyViewer2015.

Once you have searched for and selected an OpenCyc concept, you will see a graph with that concept as its root. Edges to the right of the root are specializations of that concept, while edges to the left are generalizations.  Nodes that are shaded in can be expanded by left-clicking on them.  To refocus the graph on a new root, shift-click on the node.  Finally, to see additional information about a concept from OpenCyc, right-click on its node.

By default, the Taxonomy Viewer uses the Google News Word2Vec space (https://code.google.com/p/word2vec/) to find nearby concepts.  Users can switch to useing the Word2Vec space produced by BioASQ by training on Pubmed by running the 	`WebConceptFinderBio.java` class instead.
