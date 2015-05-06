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
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 * See also
 * https://github.com/NanoHttpd/nanohttpd/blob/master/webserver/src/main/java/fi/iki/elonen/SimpleWebServer.java
 *
 */
public class ResourceServer {

  /**
   * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE.
   */
  public static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
    {
      put("css", "text/css");
      put("htm", "text/html");
      put("html", "text/html");
      put("xml", "text/xml");
      put("java", "text/x-java-source, text/java");
      put("md", "text/plain");
      put("txt", "text/plain");
      put("asc", "text/plain");
      put("gif", "image/gif");
      put("jpg", "image/jpeg");
      put("jpeg", "image/jpeg");
      put("png", "image/png");
      put("mp3", "audio/mpeg");
      put("m3u", "audio/mpeg-url");
      put("mp4", "video/mp4");
      put("ogv", "video/ogg");
      put("flv", "video/x-flv");
      put("mov", "video/quicktime");
      put("swf", "application/x-shockwave-flash");
      put("js", "application/javascript");
      put("pdf", "application/pdf");
      put("doc", "application/msword");
      put("ogg", "application/x-ogg");
      put("zip", "application/octet-stream");
      put("exe", "application/octet-stream");
      put("class", "application/octet-stream");
    }
  };

  /**
   * Directory location for resources
   */
  public static final String RESOURCE_BASE = "/resources";

  private static final Logger LOGGER = Logger.getLogger(ResourceServer.class.getName());

  /**
   *
   * @param filename
   * @return a String
   */
  public String inferMimeType(String filename) {
    return MIME_TYPES.get(FilenameUtils.getExtension(filename));
  }

  /**
   *
   * @param filename
   * @return a Response
   */
  public Response serveFile(String filename) {
    try {
      final InputStream stream = ResourceServer.class.getClassLoader().getResourceAsStream(filename);
      if (stream == null) {
        LOGGER.log(Level.SEVERE, "File not found: {0}", filename);
        return new Response(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
      }
      final String mimetype = inferMimeType(filename);
      LOGGER.log(Level.INFO, "Loading file {0} ({1})", new Object[]{filename, mimetype});
      return new Response(Status.OK, mimetype, stream);
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Error loading file " + filename, t);
      return new Response(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, t.getMessage());
    }
  }
}
