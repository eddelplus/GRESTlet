GRESTlet
========

Dispatcher servlet for using Groovlets in RESTful web apps, method and content type aware

** Concept
The GRESTlet dispatcher servlet introduced here aims to fill an apparent gap for programmers creating RESTful backends
with Groovy for the JVM (typically Tomcat or Jetty).

So far the choices were

Grails
: Powerful groovy web framework with full ORM layer. Nice URL routing including a shorthand notation for
resource style controllers. Dynamic recompilation. Even small RESTful applications require buying in to the
full framework with its dependencies.
Jersey
: Official Java/Oracle approach. Content type awareness (Consumes/Produces annotations). XML and JSON creation from Java
classes (JAX-B). Dynamic JSON creation with Jettison. Groovy classes can be annotated like Java classes,
but need to be compiled so that the Jersey dispatcher servlet can find them. 
RESTlet
: Another Java approach that I admit to know little about. Again the Groovy compiler could produce classes for this
environment.
Groovlets
: The GroovyServlet also acts as a dispatcher. The URL is typically mapped to *.groovy, not looking very RESTful.
Requests are handled by Groovy scripts that a GroovyScriptEngine compiles dynamically. Ready to produce JSON or
XML responses. No specific parsing of request bodies. 
