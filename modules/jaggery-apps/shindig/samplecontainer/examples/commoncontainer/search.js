/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

var currentEngines = [];
var templateMap = [];
/**
 * Helper function to extract create map of new engines, keyed by template url
 * @param searchURLs template search urls.
 * @param description
 *        opensearch description being added or removed.
 * @param added
 *            true if new description, false if removed.
 */
extractURLs = function(searchUrls, description, added) {
  var newEngines = [];
  for (var i in searchUrls) {
    var template = searchUrls[i]['@template'];
    if (template != null) {
      var descType = searchUrls[i]['@type'];
      if (descType == 'application/atom+xml') {
        if (added) {
          if (currentEngines[template] == null) {
            newEngines[template] = description.OpenSearchDescription.ShortName;
            templateMap[template] = 1;
          } else {
            templateMap[template]++;
          }
        } else {
          if (currentEngines[template] != null) {
            if (templateMap[template] == 1) {
              var oldEngine = document.getElementById(template);
              oldEngine.parentNode.removeChild(oldEngine);
              delete currentEngines[template];
            } else {
              templateMap[template]--;
            }
          }
        }
      }
    }
  }
  return newEngines;
};

/**
 * Callback passed to the opensearch feature to react to addition/removal of
 * gadgets containing OpenSearch descriptions.
 *
 * @param description
 *            opensearch description being added or removed.
 * @param added
 *            true if new description, false if removed.
 */
updateEngines = function(description, added) {

  var searchUrls = [];
  if (!(description.OpenSearchDescription.Url instanceof Array)) {
    searchUrls.push(description.OpenSearchDescription.Url);
  } else {
    searchUrls = description.OpenSearchDescription.Url;
  }

  var newEngines = extractURLs(searchUrls, description, added);

  var span = document.getElementById('engineList');
  for (templateUrl in newEngines) {
    var current = newEngines[templateUrl];
    span.innerHTML = span.innerHTML
        + '<input type=\"checkbox\" checked=\"true\" id=\"' + templateUrl
        + '\" />' + current;
    span.innerHTML = span.innerHTML + '<br/>';
    currentEngines[templateUrl] = current;
  }

};

CommonContainer.opensearch.addOpenSearchCallback(updateEngines);

/**
 * Clears old search results, and fetches new ones.
 *
 */
function updateSearchURLs() {
  // clear the old results
  $(function() {
    $('#results').dialog({
      autoOpen: false
    });
  });
  $('#results').dialog('option', 'minWidth', 1000);
  $('#results').dialog('open');
  //var div = document.getElementById("results");
  /*while (div.hasChildNodes()) {
    div.removeChild(div.firstChild);
  }*/
  $('#results').empty();
  //div.innerHTML = document.getElementById("query").value;
  $('#results').innerHTML = document.getElementById('query').value;
  // fetch new results.
  getSearchResults(currentEngines, document.getElementById('query').value);
}

/**
 * Iterates over template urls and fetches search results.
 *
 * @param urls
 *            all the opensearch template urls in the container.
 * @param query
 *            query string.
 */
function getSearchResults(urls, query) {
  // callback function to be called by the fetching code.
  /**
   * @param obj
   *            the result data object, should be XML.
   * @param engineTitle
   *            title of the engine being searched.
   */
  function urlResponse(obj, engineTitle) {
    // create placeholder for results
    var su = document.getElementById('results');
    var resultDiv = document.createElement('div');
    su.appendChild(resultDiv);
    // if there are no errors, parse the results
    if (obj.status == 200) {
      resultDiv.className = 'searchEngine';
      var stringDom = obj.content;
      var domdata=opensocial.xmlutil.parseXML(stringDom);
      if (domdata != null) {
        var entries = domdata.getElementsByTagName('entry');
        resultDiv.innerHTML = resultDiv.innerHTML + engineTitle + ':<br/>';
        if (entries.legnth == 0) {
          resultDiv.innerHTML = resultDiv.innerHTML + ('No results found');
        } else {
          var resultCount = entries.length;
          if (resultCount > 15) {
            resultCount = 15;
          }
          for (i = 0; i < resultCount; i++) {
            if (entries[i].getElementsByTagName('title').length > 0) {
              titles = entries[i].getElementsByTagName('title');
              title = titles[0].childNodes[0].nodeValue;
            } else {
              title = 'Untitled';
            }
            var link = null;
            //for standard atom results, we can extract the link
            if (entries[i].getElementsByTagName('link').length > 0) {
              links = entries[i].getElementsByTagName('link');
              link = links[0].attributes.href.nodeValue;
            }
            var summaryNode = entries[i].getElementsByTagName('summary')[0];
            if (summaryNode == null) {
              summaryNode = entries[i].getElementsByTagName('description')[0];
            }
            if (link == null) {
            resultDiv.innerHTML = resultDiv.innerHTML
                + '<p style=\"color:blue\"/>'
                + gadgets.util.escapeString(title);
            } else {
              resultDiv.innerHTML = resultDiv.innerHTML
              + '<p style=\"color:blue\"/>'
              + '<a href=\"'+ link + '\" target=\"_blank\">'
              + gadgets.util.escapeString(title)
              + '</a>';
            }
            if (summaryNode != null) {
              var summary = summaryNode.textContent;
              if (summary != null) {
                resultDiv.innerHTML = resultDiv.innerHTML
                    + gadgets.util.escapeString(summary);
              }
            }
          }
        }
      }
    } else { // errors occured, notify the user.
      resultDiv.innerHTML = resultDiv.innerHTML + engineTitle
          + '<br/> An error has occured:' + obj.status;
    }
  }
  var params = {};
  for (url in currentEngines) {
    // check if the current engine is selected.
    if (document.getElementById(url).checked) {
      title = currentEngines[url];
      // replace placeholder with actual search term.
      url = url.replace('{searchTerms}', query);
      // for now, start on page 1
      url = url.replace('{startPage?}', 1);
      // makes sure that the title corresponds to the engine being search.
      // Resolves a prior timing issue.
      var callback = function() {
        var myTitle = '' + title;
        return function(response) {
          urlResponse(response, myTitle);
        };
      }();
      // go fetch the results.
      osapi.http.get({
                  'href' : url,
                  'format' : 'text'
                }).execute(callback)
    }
  }

}
