GRESTServlet
============

##Groovy REST Servlet

+ Dispatcher servlet for using Groovlets in RESTful web apps
+ URLs without .groovy suffix and with extra path parameters
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

The GRESTServlet approach borrows substantially from the GroovyServlet.

It uses two of its helper classes without changes:
+ [ServletBinding](http://groovy.codehaus.org/gapi/groovy/servlet/ServletBinding.html)
+ [ServletCategory](http://groovy.codehaus.org/gapi/groovy/servlet/ServletCategory.html)
+ [GroovyScriptEngine](http://groovy.codehaus.org/gapi/groovy/util/GroovyScriptEngine.html)

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

### Attribute access

The ServletCategory provides Groovy access to the context, request, and session attributes:

    //request.setAttribute("consume", "xml")
    request.consume = "xml"

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

+ application/json (parsed with [JsonSluper](http://groovy.codehaus.org/gapi/groovy/json/JsonSlurper.html);
hierarchy of maps, list and primitives in _data_)
+ application/xml (parsed with [XmlSlurper](http://groovy.codehaus.org/gapi/groovy/util/XmlSlurper.html);
GPathResult in _data_)
+ text/plain (parsed into a List&lt;String&gt; of lines of text in _data_)

There is no consideration for _text/json_ or _text/xml_.
Both jQuery and Angular.js set the content type as expected.
Other client libaries need to do this likewise.

For JSON the JsonSluper is the only obvious choice. The XmlSluper is superior for grabbing data from the
transmitted XML. The approach for plain text may be debatable, but it does free the scripts from
worrying about encoding and stream access.

The _consume_ attribute of the request object is set according to the type of _data_ generated.
The value can be one of "json", "xml", "text", or null.

Bodies in application/x-www-form-urlencoded format will have been parsed by the servlet container already
with their data available via the params map. Support for multipart/form-data has not been tested or
implemented yet.

### Producing the response

Client libaries for REST should set the _Accept_ header in their requests to indicate the type of
response body they are expecting. Again this has been verified to be the case for jQuery and for more
recent versions of Angular.js. If the _Accept_ header is set, the Content-Type of the response and
the attribute _request.produce_ will be set accordingly.

The _Accept_ header is recognized if it starts with one of these mime types:

+ application/json ( _request.produce_ set to "json" )
+ application/xml ( _request.produce_ set to "xml" )
+ text/plain ( _request.produce_ set to "text" )
+ text/html ( _request.produce_ set to "html" )

Otherwise _response.contentType_ defaults to _text/html_ and _request.produce_ will be null.

If a request parameter **callback** is recognized, it is **always** interpreted as a request for
a JSONP response. In this case _response.contentType_  is set to _text/javascript_
and _request.produce_ to "jsonp". 

Actually creating JSON, XML or other output is still left to the script code. For XML in particular
the MarkupBuilder to use is called _html_ in the script binding!

There is no obligation to evaluate _request.produce_ in the script.
If the REST service is purely designed to produce JSON, it can still do so,
no matter what the client has indicated. In those cases it should set _response.contentType_ as well.

The only bits of output that the GRESTServlet creates itself are the callback prefix and the parentheses for
JSONP, allowing the script to concentrate on the contained JSON.

The response character encoding is always set to **UTF-8**. The _out_ writer converts the output accordingly.
