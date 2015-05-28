
function handleClickedConceptAnchor(conceptRef) {
  d3.select('.cgraph#' + conceptRef).style('display', 'inline');
  console.log('clicked ' + conceptRef);
}

function handleClickedSelectedConceptSpan(conceptRef) {
  d3.select('#concepts').selectAll('span')
          .each(
                  function(d, i) {
                    //Some spans are added for spacing.. so check
                    var cid = (d3.select(this).attr('conceptid'));
                    if (cid != null && cid != undefined) {
                      if (cid == conceptRef) {
                        d3.select(this).transition(500).style('color', 'red');
                        d3.select(this).transition().delay(500).remove();
                      }
                    }
                    //    + "     console.log(i);
                  });
}

function handleCloseConceptAnchor(conceptRef) {
  d3.select('.cgraph#' + conceptRef).transition(100).style('display', 'none');
  console.log('clicked ' + conceptRef);
}

function gatherSelectedIDs() {
  var collectorSpace = '?selectedConcepts=';
  d3.select('#concepts').selectAll('span')
          .each(
                  function(d, i) {
                    //Some spans are added for spacing.. so check
                    var cid = (d3.select(this).attr('conceptid'));
                    if (cid != null && cid != undefined) {
                      collectorSpace += cid + '+';
                    }
                    //    + "     console.log(i);
                  }
          );
  collectorSpace = collectorSpace.slice(0, -1);
//   + "  var currLoc=window.location;
  console.log(collectorSpace);
  window.location.search = collectorSpace;
  console.log(window.location);
}

function clearLists() {
 var collectorSpace = '?clearLists=true';
   collectorSpace = collectorSpace.slice(0, -1);
//   + "  var currLoc=window.location;
  console.log(collectorSpace);
  window.location.search = collectorSpace;
  console.log(window.location);
}

function xmlQuery() {
 var collectorSpace = '?xmlQuery=true'; 
    collectorSpace = collectorSpace.slice(0, -1);
//   + "  var currLoc=window.location;
  console.log(collectorSpace);
  window.location.search = collectorSpace;
  console.log(window.location);
}