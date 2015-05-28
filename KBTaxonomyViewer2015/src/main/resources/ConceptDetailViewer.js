var examinedConceptCssClass = "examinedConcept";
var conceptDetailViewCssClass = "conceptDetailView";
var conceptDetailHtmlCssClass = "conceptDetailHtml";
var conceptDetailContentCssClass = "conceptDetailContent";
var conceptDetailComponentCssClass = "conceptDetailComponent";


function NodeDetail(viewer, selection) {
  var detail = this;
  var contentDisplayTimer;
  var displayTransitionDuration = 1000;
  var contentDisplayTimer;
  var displayg;

  function selectSymbol(node) {
    return node.select("circle");
  }

  function selectExamined() {
    return viewer.svg.select("." + examinedConceptCssClass);
  }

  this.cancel = function () {
    if (contentDisplayTimer) {
      clearTimeout(contentDisplayTimer);
    }
    contentDisplayTimer = null;
  };

  var updateDisplay = function (selection) {
    selection
            .attr("width", function (d) {
              return d.width;
            })
            .attr("height", function (d) {
              return d.height;
            })
            .attr("x", function (d) {
              return d.cx - (d.width / 2);
            })
            .attr("y", function (d) {
              return d.cy - (d.height / 2);
            })
            .attr('rx', function (d) {
              return d.rx;
            })
            .attr('ry', function (d) {
              return d.ry;
            })
            .style('fill', function (d) {
              return d.fill;
            })
            .style('stroke', function (d) {
              return d.stroke;
            });
  };

  var showContent = function (parent, d) {
    //console.log("d",d);
    var foreignObjects = parent.append("foreignObject")
            .datum(d)
            .classed(conceptDetailHtmlCssClass, true)
            .classed(conceptDetailComponentCssClass, true)
            .attr("width", function (d) {
              return d.width;
            })
            .attr("height", function (d) {
              return d.height;
            })
            .attr("x", function (d) {
              return d.x;
            })
            .attr("y", function (d) {
              return d.y;
            });
    var html = foreignObjects.append("xhtml:body");
    var htmlContent = html.append("p")
            .classed(conceptDetailContentCssClass, true)
            .classed(conceptDetailComponentCssClass, true)
            .html(function (d, i) {
              return d.content;
            });
  };

  this.show = function () {
    this.cancel();
    var originCircle = selectSymbol(selection);
    originCircle.datum().focalSize = 150;
    displayg = viewer.svg.append("g")
            .attr("transform", selection.attr("transform"))
            .classed(conceptDetailViewCssClass, true);

    var d = {};
    d.minWidth = parseFloat(originCircle.attr("r") * 2);
    d.minHeight = d.minWidth;
    d.width = d.minWidth;
    d.height = d.minHeight;
    d.cx = originCircle.attr('cx') + 0;
    d.cy = originCircle.attr('cy') + 0;
    d.detailWidth = 520;
    d.detailHeight = 520;
    d.xOffset = d.detailWidth;
    d.yOffset = 0;
    d.rx = d.width / 2;
    d.ry = d.height / 2;
    d.origFill = originCircle.style('fill');
    d.origStroke = originCircle.style('stroke');
    d.fill = d.origFill;
    d.stroke = d.origStroke;

    var shape = displayg.append("rect")
            .datum(d)
            .call(updateDisplay)
            .on('contextmenu', function (d) {
              d3.event.preventDefault();
              detail.hide();
            });

    d.width = d.detailWidth;
    d.height = d.detailHeight;
    d.fill = "white";
    d.cx = d.cx + d.xOffset;
    d.cy = d.cy + d.yOffset;
    shape.datum(d)
            .transition()
            .duration(displayTransitionDuration)
            .call(updateDisplay);

    contentDisplayTimer = setTimeout(function () {
      var closeButton = displayg.append("circle")
              .datum(d)
              .attr("r", function (d) {
                return 5;
              })
              .attr("cx", function (d) {
                return d.cx + (d.width / 2) - 15;
              })
              .attr("cy", function (d) {
                return d.cy - (d.height / 2);
              })
              .style('fill', function (d) {
                return d.fill;
              })
              .style('stroke', function (d) {
                return d.stroke;
              })
              .classed(conceptDetailComponentCssClass, true)
              .on('mouseover', function (d) {
                d3.select(this).style("fill", "red");
              })
              .on('mouseout', function (d) {
                d3.select(this).style("fill", d.fill);
              })
              .on('click', function (d) {
                d3.event.preventDefault();
                detail.hide();
              })
              .on('contextmenu', function (d) {
                d3.event.preventDefault();
                detail.hide();
              });

      var t = {};
      t.name = selection.datum().name;
      t.id = selection.datum().hlid || selection.datum().nonCycTeamConceptID;
      t.width = d.width - 20;
      t.height = d.height - 20;
      t.x = parseInt(shape.attr("x")) + 5;
      t.y = parseInt(shape.attr("y")) + 10;
      //t.x = d.cx - (d.width / 2);
      //t.y = d.cy - (d.height / 2);

      d3.xhr("?getNodeData=" + t.id, function (err, data) {
        if (err) {
          return console.warn(err);
        }
        t.content = data.response;
        showContent(displayg, t);
      });
    }, displayTransitionDuration);
  };

  this.hide = function () {
    this.cancel();
    if (displayg) {
      viewer.svg.selectAll("." + conceptDetailComponentCssClass).remove(); // FIXME: should only remove child of selection
      //selection.selectAll("." + conceptDetailComponentCssClass).remove();
      //var g = viewer.svg.select("." + conceptDetailComponentCssClass);
      var shape = displayg.select("rect")
              .on('contextmenu', null);
      var d = shape.datum();

      d.width = d.minWidth;
      d.height = d.minHeight;
      d.cx = d.cx - d.xOffset;
      d.cy = d.cy - d.yOffset;
      d.fill = d.origFill;
      shape.datum(d)
              .transition()
              .duration(displayTransitionDuration)
              .call(updateDisplay);

      setTimeout(function () {
        displayg.remove();
        displayg = null;
        selectExamined()
                .classed(examinedConceptCssClass, false);
      }, displayTransitionDuration);

      /*
       selectSymbol(selection)
       .transition()
       .duration(displayTransitionDuration)
       .attr("r", function(d) {
       d.focalSize = d.activeconcept === true ? 6.5 : 5.5;
       return d.focalSize;
       });
       setTimeout(function() {
       selectExamined()
       .classed(examinedConceptCssClass, false);
       }, displayTransitionDuration);
       */
    }
  };

  this.abort = function () {
    this.cancel();
    this.hide();
  };
}



function ConceptDetailViewer(svg) {
  console.log('conceptDetailEnabled', conceptDetailEnabled);
  var viewer = this;
  var detailDisplayDuration = 1000;
  var shuffle = false;
  this.svg = svg;
  var contentDisplay;

  this.cancelShowDetail = function () {
    if (contentDisplay) {
      contentDisplay.abort();
    }
  };

  function selectSymbol(node) {
    return node.select("circle");
  }

  function selectExamined() {
    return viewer.svg.select("." + examinedConceptCssClass);
  }

  function isExamined(selection) {
    return selection.classed(examinedConceptCssClass);
  }

  function selectUnexamined() {
    return viewer.svg.selectAll(".node").filter(function (d, i) {
      return !isExamined(d3.select(this));
    });
  }
  /*
   function resetOffsets(d) {
   d.xFocalOffset = 0;
   d.yFocalOffset = 0;
   }
   
   function normalizeOffsets(d) {
   if (!d.xFocalOffset || !d.yFocalOffset) {
   resetOffsets(d);
   }
   }
   */
  function shuffleNodes(selection) {
    selection
            .transition()
            .duration(detailDisplayDuration)
            .attr("transform", function (d) {
              var focalData = selectExamined().datum();
              var currDist = Math.sqrt(Math.pow(Math.abs(d.x - focalData.x), 2) + Math.pow(Math.abs(d.y - focalData.y), 2));
              //var circle=selectSymbol(d3.select(this));
              //var minDist = focalData.focalSize + parseFloat(circle.attr("r"));
              var minDist = focalData.focalSize;
              var distance = Math.max(minDist, minDist - currDist);
              var xFocalOffset = (d.x - focalData.x) + distance;
              var yFocalOffset = (d.y - focalData.y) + distance;
              var translate = "translate(" + (yFocalOffset) + "," + (xFocalOffset) + ")";
              return translate;
            });
  }

  function unshuffleNodes(selection) {
    selection.transition()
            .duration(detailDisplayDuration)
            .attr("transform", function (d) {
              var translate = "translate(" + (d.y) + "," + (d.x) + ")";
              return translate;
            });
  }

  function showDetail(selection) {
    viewer.cancelShowDetail();
    selection
            .classed(examinedConceptCssClass, true);
    contentDisplay = new NodeDetail(viewer, selection);
    contentDisplay.show();
    if (shuffle) {
      shuffleNodes(selectUnexamined());
    }
  }

  this.showDetailNow = function (elm, d, i) {
    var selection = d3.select(elm);
    selectSymbol(selection).select(function (d) {
      if (!isExamined(d3.select(this.parentNode))) {
        d3.select(this.parentNode).call(showDetail);
      } else {
        hideDetail();
      }
    })

  };



  function hideDetail() {
    // TODO: should hook into some event handler or something?
    //viewer.cancelShowDetail();
    if (shuffle) {
      unshuffleNodes(selectUnexamined());
    }
    contentDisplay.hide();
  }

  this.setupSelection = function (selection) {
    if (conceptDetailEnabled) {
      selectSymbol(selection)
              .on('contextmenu', function (d) {
                d3.event.preventDefault();
                if (!isExamined(d3.select(this.parentNode))) {
                  d3.select(this.parentNode).call(showDetail);
                } else {
                  hideDetail();
                }
              });
    }
  };
}