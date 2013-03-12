GRESTServlet
============

Groovy REST Servlet

+ Dispatcher servlet for using Groovlets in RESTful web apps
+ Utilization of the Groovy Script Engine for dynamic compilation
+ Awareness of request methods, optionally dispatching to separate scripts
+ Evaluation of the request's Content-Type and Accept headers
+ Parsing of request bodies with Groovy's JsonSluper or XmlSluper

Concept
-------
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
