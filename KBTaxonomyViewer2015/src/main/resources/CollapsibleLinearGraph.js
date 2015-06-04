/*
 * CollapsibleLinearGraph
 * Author: jmoszko
 * 
 * This version of linear graph allows the user to collapse and expand nodes by clicking on them,
 * rather than opening a new graph.
 */

var graphViewer = {
};

function collapsibleLinearGraph(conceptNo, conceptName, depthC, widthC, maxDepthC) {
  var minGraphHeight = 550;
  var conceptW = 120, conceptH = 25;
  var margin = {top: conceptH, right: conceptW, bottom: conceptH, left: (conceptW * 2)};
  var baseW = (maxDepthC + 4) * conceptW,
          baseH = (widthC + 3) * conceptH;
  var width = baseW - margin.left - margin.right,
          height = baseH - margin.top - margin.bottom;
  if (height < minGraphHeight) {
    height = minGraphHeight
  }
  ;

  var div = d3.select("#concept" + conceptNo).style("display", "inherit").append("div").attr("style", "position:relative");
  var legendContainer = div.append("div")
          .classed("legendDiv", true)
          .attr("style", "position: absolute; x: 0; y : 0");

  //there's already a graph here, so don't bother doing anything else.
  if ((d3.select("#concept" + conceptNo + " svg")[0] && d3.select("#concept" + conceptNo + " svg")[0][0])) {
    return;
  }

  var zoomListener = d3.behavior.zoom().scaleExtent([0.1, 3]).on("zoom", zoom);

  function zoom() {
    svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
  }

  var svg = div.append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .call(zoomListener)
          .append("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  var detailViewer = new ConceptDetailViewer(svg);

  var i = 0;
  var duration = 750;
  var trees = {};
  var roots = {};

  createGraphs(conceptName);

  function createGraphs(conceptName) {
    trees = {};
    roots = {};
    d3.json("?childData=" + conceptName + "&height=" + depthC + "&direction=down", function (error, graph) {


      var diagonal = d3.svg.diagonal()
              .projection(function (d) {
                return [d.y, d.x];
              });
      var parentDiagonal = d3.svg.diagonal()
              .projection(function (d) {
                var arr = [height - d.y, d.x];
                return arr;
              });
      var direction = "child";
      var node = setupGraph(graph, direction);
      update(graph, direction);
      setupNodes(node, direction);

      d3.json("?childData=" + conceptName + "&direction=up", function (err, data) {
        var direction = "parent";
        var node = setupGraph(data, direction);
        update(data, direction);
        setupNodes(node, direction);
      });


      function setupGraph(graph, direction, skipTreeCreation) {
        if (!skipTreeCreation) {
          trees[direction] = d3.layout.tree()
                  .size([height, width - 160]);
        }
        roots[direction] = graph;
        legendContainer.innerHTML = roots[direction].name;
        var nodes = trees[direction].nodes(graph);
        var links = trees[direction].links(nodes);
        var thisDiag = (direction == "child") ? diagonal : parentDiagonal;
        var link = svg.selectAll("path.link." + direction)
                .data(links)
                .enter().append("path")
                .attr("class", "link")
                .classed(direction, true)
                .attr("d", thisDiag);
        var node = svg.selectAll("g.node." + direction)
                .data(nodes)
                .enter().append("g")
                .attr("class", "node")
                .classed(direction, true)
                .attr("transform", function (d) {
                  if (d.isParent) {
                    var transform = "translate(-" + d.y + "," + d.x + ")"
                    return transform;
                  } else {
                    var transform = "translate(-" + d.y + "," + d.x + ")"
                    return transform;
                  }
                });
        return node;
      }
      function setupNodes(selection, dir) {
        var direction = dir;

        selection.filter(
                function (d) {
                  if (d.type !== 'nonCycTeamConcept') {
                    return true;
                  }
                })
                .on('contextmenu', d3.contextMenu(graphViewer.menuOpenCyc));
        
//        selection.filter(
//                function (d) {
//                  if (d.type === 'nonCycTeamConcept') {
//                    return true;
//                  }
//                })
//                .on('contextmenu', d3.contextMenu(graphViewer.menuNonCyc));

        selection.append("circle")
                .attr("r", function (d) {
                  return d.activeconcept === true ? 6.5 : 5.5;
                })
                .style('stroke',
                        function (d) {
                          return d.type === 'nonCycTeamConcept' ? 'green' : 'indigo';
                        })
                .on('click',
                        function (d, i) {
                          if (d.type === 'nonCycTeamConcept') {
                            //detailViewer.cancelShowDetail();
                            //@Todo -- make this more sophisticated. Maintain a set
                            // so we don't get repeated entries and so we can have 
                            // say, commas between entries in the selected list
                            d3.select('#concepts')
                                    .append('span')
                                    .attr('class', 'addedTerm')
                                    .attr('conceptid', d.displayedConceptID)
                                    .attr('onclick', 'handleClickedSelectedConceptSpan("' + d.displayedConceptID + '");')
                                    .text('' + d.name + ' ');
                            d3.select(this).style('fill', 'green').attr('r', 6.5);
                          } else if (d3.event.ctrlKey) {
                            if (d.displayedConceptID !== null
                                    && d.displayedConceptID !== undefined
                                    && (!d3.select('#' + d.displayedConceptID).empty())) {
                              var cid = d.displayedConceptID;
                              d3.select('#' + cid)
                                      .style('display', 'inline');
                            }
                          } else if (d3.event.shiftKey) {
                            refocusTrees(d);
                          } else {
                            if (d.children) {
                              d.children = null;
                              update(d, direction);
                            } else {
                              d3.json("?childData=" + d.name + "&direction=" + ((d.isParent) ? "up" : "down"), function (err, data) {
                                d.children = data.children;
                                update(d, d.isParent ? "parent" : "child");
                              });
                            }

                          }
                        })
                .append("title")
                .text(function (d) {
                  return d.name;
                });
        selection.append("text")
                .attr("dx", function (d) {
                  return d.isParent ? -8 : 8;
                })
                .attr("dy", 3)
                .attr("text-anchor", function (d) {
                  return d.isParent ? "end" : "start";
                })
                .text(function (d) {
                  return (!d.isParent || d.activeconcept !== false) ? d.name : "";
                })
                .style('font',
                        function (d) {
                          return d.type === 'nonCycTeamConcept' ? '12px sans-serif' : '10px sans-serif';
                        })
                .style('font-style',
                        function (d) {
                          return d.type === 'nonCycTeamConcept' ? 'normal' : 'italic';
                        });

//                detailViewer.setupSelection(selection);
      } // setupNodes

      function update(source, direction) {
        // Compute the new tree layout.
        var dir = source.isParent ? "parent" : "child";

        //reset the size each time we add/subtract nodes.  Be sure to do this before the calls to trees[dir].nodes and trees[dir].links, since that sees the x/y coordinates
        var levelWidth = [1];
        var childCount = function (level, n) {
          if (n.children && n.children.length > 0) {
            if (levelWidth.length <= level + 1)
              levelWidth.push(0);
            levelWidth[level + 1] += n.children.length;
            n.children.forEach(function (d) {
              childCount(level + 1, d);
            });
          }
        };
        childCount(0, roots['child']);
        var newHeight = d3.max(levelWidth) * 25; // 25 pixels per line  
        trees["child"].size([newHeight, trees["child"].size()[1]]);
        if (trees["parent"]) {
          childCount(0, roots['parent']);
          var newHeight = d3.max(levelWidth) * 25;
          trees["parent"].size([newHeight, trees["parent"].size()[1]]);
        }

        var nodes = trees[dir].nodes(roots[dir]).reverse(),
                links = trees[dir].links(nodes);
        // Normalize for fixed-depth.
        nodes.forEach(function (d) {
          if (d.isParent) {
            d.y = 0 - (d.depth * 180);
          } else {
            d.y = (d.depth * 180);
          }
        });


        // Update the nodes…
        var node = svg.selectAll("g.node." + dir)
                .data(nodes, function (d) {
                  return d.id || (d.id = ++i);
                });

        // Enter any new nodes at the parent's previous position.
        var nodeEnter = node.enter().append("g")
                .attr("class", "node")
                .classed(dir, true)
                .attr("transform", function (d) {
                  var translate = "translate(" + source.y0 + "," + source.x0 + ")";
                  return translate;
                });

        //need to get the new ones, as well as the one that was clicked, if 
        node.classed('expandible', function (d) {
          return (d.hasChildren && !d.children);
        });
        setupNodes(nodeEnter);
        //make sure the parent root stays at the same place as the child root.
        if (roots["parent"] && (roots["parent"].x != roots["child"].x)) {
          if (Math.abs(roots["parent"].x - roots["child"].x) > 1) {
            roots["parent"].x = roots["child"].x;
            if (direction == "child") {
              update(roots["parent"], "parent");
            }
          }
        }
        // Transition nodes to their new position.
        var nodeUpdate = node.transition()
                .duration(duration)
                .attr("transform", function (d) {
                  var translate = "translate(" + d.y + "," + d.x + ")";
                  return translate;
                });

        nodeUpdate.select("text")
                .style("fill-opacity", 1);

        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
                .duration(duration)
                .attr("transform", function (d) {
                  var translate = "translate(" + source.y + "," + source.x + ")";
                  return translate;
                })
                .remove();

        nodeExit.select("circle")
                .attr("r", 1e-6);

        nodeExit.select("text")
                .style("fill-opacity", 1e-6);

        // Update the links…
        var link = svg.selectAll("path.link." + dir)
                .data(links, function (d) {
                  return d.target.id;
                });


        // Enter any new links at the parent's previous position.
        link.enter().insert("path", "g")
                .attr("class", "link")
                .classed(dir, true)
                .attr("d", function (d) {
                  var o = {x: d.source.x0, y: d.source.y0};
                  return diagonal({source: o, target: o});
                });

        link.filter(
                function (link) {
                  if (link.target.linkType) {
                    return true;
                  }
                })//.style("stroke-dasharray", ("3, 3"))
                .style('stroke-width', 2)
                .style('stroke',
                        function (d) {
                          return d.target.linkColour;
                        })
                .append("title")
                .text(function (d) {
                  return "Relation: " + d.target.linkType;
                });

        // Transition links to their new position.
        link.transition()
                .duration(duration)
                .attr("d", diagonal);

        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
                .duration(duration)
                .attr("d", function (d) {
                  var o = {x: source.x, y: source.y};
                  return diagonal({source: o, target: o});
                })
                .remove();

        // Stash the old positions for transition.
        nodes.forEach(function (d) {
          d.x0 = d.x;
          d.y0 = d.y;
        });

        //@hack just concatenate the legends of both the child and parent graphs, which may result in some duplication
        if (direction === "child") {
          svg.selectAll(".legend").remove();
        }
        var legend = svg.selectAll(".legend g");
        var displayLinkTypes = {};


        //the div below is an HTML div that could be used to hold a better legend, mentioning the focal concept, etc.  Crucially, because it's not
        //part of the main SVG, it won't be subject to panning/zooming.  Currently there's no content here, because there's not time
        //to make the content look decent.
//        $('#concept' + conceptNo + " .legendDiv").html(roots['child'].name);
        var legend = svg.selectAll(".legend")
                .data(links.filter(function (link) {
                  if (link.target.linkType && !displayLinkTypes[link.target.linkType]) {
                    displayLinkTypes[link.target.linkType] = true;
                    return true;
                  }
                }))
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function (d, i) {
                  return "translate(0," + i * 20 + ")";
                });

        legend.append("rect")
                .attr("x", -50)
                .attr("width", 25)
                .attr("height", 2)
                .style("fill",
                        function (link) {
                          return link.target.linkColour;
                        });

        legend.append("text")
                .attr("x", -55)
                .attr("y", 9)
                .attr("dy", ".01em")
                .style("text-anchor", "end")
                .text(function (link) {
                  return link.target.linkType;
                })
                .style('font',
                        function (d) {
                          return d.type === 'nonCycTeamConcept' ? '12px sans-serif' : '12px sans-serif';
                        })
                .style('font-style',
                        function (d) {
                          return d.type === 'nonCycTeamConcept' ? 'normal' : 'italic';
                        });

      } //close update()

      //what this should do: turn this node into the center node, and keep its children (if in a child tree), or its parents, but not both.
      //what this does: obliterate the current trees, and recreate them both.
      function refocusTrees(d) {
        d3.selectAll("#concept" + conceptNo + " svg > g > *").remove();
        createGraphs(d.name);
      }

      d3.select(self.frameElement).style("height", height + "px");

      function expandOrCollapseTrees(d) {
        if (d.children) {
          d.children = null;
          update(d, direction);
        } else {
          d3.json("?childData=" + d.name + "&direction=" + ((d.isParent) ? "up" : "down"), function (err, data) {
            d.children = data.children;
            update(d, d.isParent ? "parent" : "child");
          });
        }
      }

      graphViewer.menuOpenCyc = [
        {
          title: 'Expand/Collapse [left click]',
          action: function (elm, d, i) {
            console.log('Expand or collapse from this node');
            expandOrCollapseTrees(d);
          }
        },
        {
          title: 'Refocus [shift+click]',
          action: function (elm, d, i) {
            console.log('Refocus on this node');
            refocusTrees(d);
          }
        },
        {
          title: 'Details',
          action: function (elm, d, i) {
            console.log('Show more information for this node');
            detailViewer.showDetailNow(elm, d, i);
          }
        }
      ]

      graphViewer.menuNonCyc = [
        {
          title: 'Add/Remove from XML Query'
        },
        {
          title: 'Details (where available)',
          action: function (elm, d, i) {
            console.log('Show more information for this node');
            detailViewer.showDetailNow(elm, d, i);
          }
        }
      ]

    });
  }
}
