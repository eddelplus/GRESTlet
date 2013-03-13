GRESTServlet
============

Groovy REST Servlet

+ Dispatcher servlet for using Groovlets in RESTful web apps
+ Utilization of the Groovy Script Engine for dynamic compilation
+ Awareness of request methods, optionally dispatching to separate scripts
+ Evaluation of the request's Content-Type and Accept headers
+ Parsing of request bodies with Groovy's JsonSluper or XmlSluper
+ Support for JSONP

Outset
------
The GRESTlet dispatcher servlet introduced here aims to fill an apparent gap for programmers creating RESTful backends
with Groovy for the JVM (Tomcat, Jetty, J2EE app servers).

So far the choices were

###Grails

Powerful groovy web framework with full ORM layer. Nice URL routing including a shorthand notation for
resource style controllers. Dynamic recompilation. Even small RESTful applications require buying into the
full framework with its dependencies.

###Jersey

Official Java/Oracle approach. Content type awareness (Consumes/Produces annotations). XML and JSON creation from Java
classes (JAX-B). Dynamic JSON creation with Jettison. Groovy classes can be annotated like Java classes,
but need to be compiled so that the Jersey dispatcher servlet can find them.

###RESTlet

Another Java approach that I admit to know little about. Again the Groovy compiler could produce classes for this
environment.

###Groovlets

The GroovyServlet, part of the Groovy distribution, also acts as a dispatcher.
The URL is typically mapped to *.groovy, not looking very RESTful.
Requests are handled by dynamically compiled Groovy scripts. Ready to produce XML or JSON
responses with Groovy's MarkupBuilder and JsonBuilder. No specific parsing of request bodies.
Default response content is text/html.

Approach
--------

The GRESTServlet approach substantially borrows from the GroovyServlet.

It uses two of its helper classes without changes:
+ [ServletBinding](http://groovy.codehaus.org/gapi/groovy/servlet/ServletBinding.html)
+ [ServletCategory](http://groovy.codehaus.org/gapi/groovy/servlet/ServletCategory.html)

### Variable binding

The ServletBinding exposes a number of variables to the scripts that implement the RESTful calls:

+ request (HttpServletRequest)
+ response (HttpServletResponse)
+ context (ServletContext)
+ session (request.getSession(false))
+ params (request parameters as a Map)
+ headers (request headers as a Map)
+ out (response.writer)
+ html (new MarkupBuilder(out))
+ json (new JsonBuilder(out))
+ sout (response.outputStream)

The GRESTServlet adds a single variable to the binding

+ data (parsed request body)

### Request body access

The servlet evaluates the content length, type and encoding of the request body:

      if (request.contentLength > 0) {

          def ct  = request.contentType
          def enc = request.characterEncoding ?: "UTF-8"

          if (ct.startsWith(ctJSON)) {
              data = new JsonSlurper().parse(request.inputStream.newReader(enc))
              request.consume = "json"
          }
          else if (ct.startsWith(ctXML)) {
              data = new XmlSlurper().parse(request.inputStream.newReader(enc))
              request.consume = "xml"
          }
          else if (ct.startsWith(ctTEXT)) {
              data = []
              request.inputStream.eachLine(enc) { data.push it }
              request.consume = "text"
          }
      }

The body is not parsed unless it is one of the following MIME types:

+ application/json (parsed with groovy.json.JsonSluper)
+ application/xml (parsed with groovy.util.XmlSlurper)
+ text/plain (parsed into a List<String> of lines of text)

There is no consideration for _text/json_ or _text/xml_.
Both jQuery and Angular.js set the content type as expected.
Other client libaries need to do this likewise.

For JSON the JsonSluper is the only obvious choice. The XmlSluper is superior for grabbing data from the
transmitted XML. The approach for plain text may be debatable, but it does free the scripts from
worrying about encoding and stream access.

### Attribute access

The ServletCategory provides Groovy access to the context, request, and session attributes:

    //request.setAttribute("consume", "xml")
    request.consume = "xml"


