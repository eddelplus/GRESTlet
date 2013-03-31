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

Scope and Dependencies
----------------------

The repository contains the servlet code (src/java), a set of GRESTlets for generically using H2 database tables
as RESTful resources (web/WEB-INF/grest) and an adaption of Christophe Coenraet's wine cellar application. The
latter grabs Angular.js 1.0.5 from the GoogleAPIs CDN.

The application has been tested with Tomcat 6 and JDK 6. The external JARs needed are:
+ groovy-all-2.1.1.jar
+ h2-1.3.170.jar

The servlet code utilizes annotations for static compilation. Without them it can probably be back-ported to
Groovy 1.8.x.

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

It uses three of its helper classes without changes:
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

### URL Mapping

The GRESTServlet needs to be added to the web.xml configuration like this:

    <servlet>
        <servlet-name>GRESTServlet</servlet-name>
        <servlet-class>net.eddelbuettel.grest.GRESTServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>GRESTServlet</servlet-name>
        <url-pattern>/grest/*</url-pattern>
    </servlet-mapping>

The _url-pattern_ determines where the script code will be expected, which is a subdirectory of WEB-INF
with the same name. Using _grest_ is just an example. A url-pattern /resources/h2db/* would set the root
directory for the script engine to /WEB-INF/resources/h2db.

Thus scripts for GRESTlets are kept separate from Groovlets, which are located relative to /WEB-INF/groovy
or relative to the root of the web application (outside of WEB-INF's safe haven).

The * in the pattern tells the servlet container to hand control to this servlet for any extra path
information in the URL beyond the specified part. This extra path information is evaluated as a
directory path for locating the script.

A URL would look this

    [server]:[port]/[context-path]/[servlet-path]/extraA/extraB/extraC

The evaluation begins at the root and then looks deeper into the subdirectories until a matching script is found.
The other bit of information taken into account is the request method translated to lowercase.
This is one of [ **get, post, put, delete, jsonp** ]. Any extra parts of the path beyond where the script
has been found are stored in request attributes _pathvar1_, _pathvar2_, etc. RESTful URLs often use these for
passing IDs of the resources.

Let's assume the method is **post**. The servlet will check whether a script exists in this order:

+ WEB-INF/[servlet-path]/extraA_post.groovy (request.pathvar1 = extraB, request.pathvar2 = extraC)
+ WEB-INF/[servlet-path]/extraA.groovy (request.pathvar1 = extraB, request.pathvar2 = extraC)
+ WEB-INF/[servlet-path]/extraA/extraB_post.groovy (request.pathvar1 = extraC)
+ WEB-INF/[servlet-path]/extraA/extraB.groovy (request.pathvar1 = extraC)
+ WEB-INF/[servlet-path]/extraA/extraB/extraC_post.groovy
+ WEB-INF/[servlet-path]/extraA/extraB/extraC.groovy

If none of these scripts exists, return code 404 will be raised.

A script without method suffix could deliver the response for both GET and JSONP requests. If all available
scripts have a suffix any methods not implemented will return 404. This way JSONP can be disabled.

Of course all methods could also be handled by a single script with appropriate branching.
It is important to note that _request.method_ will still be in uppercase and return "GET" for JSONP requests.
Whether it is a JSONP request can be established via request.produce == "jsonp".

Sample GRESTlets
----------------

I've adapted the sample
[REST application by Christophe Coenraets](http://coenraets.org/blog/2012/02/sample-application-with-angular-js)
to a GRESTlet backend with groovy.sql and H2 database. Christophe has done his backends with MySQL for both
PHP/Slim and JAX-RS/Jersey.

The full example is available for download on my
[home page](http://www.eddelbuettel.net/html5/groovy.html)

One of my goals was to support the $resource approach in Angular.js out of the box, so that a RESTful resource
implemented via GRESTlets could be declared in the client JavaScript code just like this:

    app.factory('WineResource', function($resource) {
        return $resource('grest/h2tab/wine/:id');
    });
    
The GRESTlets in the sample are on the grest/h2tab level. The table name is generic and **wine** ends up
in _request.pathvar1_, while an optional id is in _request.pathvar2_.

The GRESTlet for retrieving a list of table rows or a specific row with GET looks like this:

**web/WEB-INF/grest/h2tab_get.groovy**

    import pool.H2Pool

    if (!(request.pathvar1 ==~ /\w+/)) {
        response.setStatus(400, "illegal table identifier")
        return
    }

    def sql = H2Pool.getSql(context)

    def stmt = 'select * from ' + request.pathvar1.toUpperCase()

    if (request.pathvar2) {
        // Get specific row by id
        def row = sql.firstRow(stmt + ' where ID=?', [request.pathvar2])
        if (row)
            json(row)
        else
            response.sendError(404, "Resource not found")
    }
    else {
        if (params.order) stmt = stmt + ' order by ' + params.order
        def start = params.start?.toInteger() ?:  0
        def limit = params.limit?.toInteger() ?: 51
        json(sql.rows(stmt, start + 1, limit))
    }
    
Depending on the presence of an id for a specific wine in the URL the result is either an array of all rows
or a specific row in JSON representation. The distinction is made via request.pathvar2.

Querying the table supports different sort orders and pagination via **order**, **start** and **limit**
parameters in the query string.

If the script is renamed to just h2tab.groovy, it would also fulfill JSONP requests.
The prefixing for JSONP is done by the GRESTservlet.

Here is the script for deleting an entry:

**web/WEB-INF/grest/h2tab_delete.groovy**

    import pool.H2Pool

    if (!(request.pathvar1 ==~ /\w+/)) {
        response.setStatus(400, "illegal table identifier")
        return
    }

    def sql = H2Pool.getSql(context)

    if (request.pathvar2) {
        def stmt = 'delete from ' + request.pathvar1.toUpperCase() + ' where ID=?'
        if (sql.executeUpdate(stmt, [request.pathvar2]) > 0) {
            response.setStatus(204, "successfully deleted")
            return
        }
    }
    response.setStatus(404, "nothing deleted")

The id of the wine is in pathvar2. The URL (with method set to DELETE) would be .../grest/h2tab/wine/6 to remove
wine #6. With Angular.js the client code is simply WineResource.remove({id: 6})

The script for POST looks like this:

**web/WEB-INF/grest/h2tab_post.groovy**

    import pool.H2Pool

    if (!(request.pathvar1 ==~ /\w+/)) {
        response.setStatus(400, "illegal table identifier")
        return
    }

    def sql = H2Pool.getSql(context)
    def tabname = request.pathvar1.toUpperCase()

    if (data.ID) {
        sql.resultSetConcurrency = java.sql.ResultSet.CONCUR_UPDATABLE
        sql.eachRow('select * from ' + tabname + ' where ID=' + data.ID.toBigInteger()) { row ->
            data.each { fld, val -> row[fld] = val }
        }
    }
    else {
        data.ID = sql.firstRow('select ' + tabname + '_SEQ.NEXTVAL as ID from DUAL').ID
        sql.dataSet(tabname).add(data)
    }
    json(data)


The JSON payload in the request body is readily supplied in variable _data_. The magic of the groovy.sql module
really kicks in here. Just look at dataSet().add() for the insertion and the sql.eachRow/data.each
combination for the update. Advanced Grooviness!

Angular.js $resource in its default configuration executes all save operations via POST. Another RESTful pattern is
to do updates as PUT. This would lead to two simpler scripts without if/else branching.

By the way, the GRESTServlet catches any exceptions raised in the scripts and reports them as ServletException.

Supporting Magic
----------------

The scripts rely on another class _H2Pool_ in package _pool_ for retrieving a groovy.sql.Sql object. This class
could be on the classpath as compiled code in WEB-INF/lib or WEB-INF/classes. The magic of the Groovy Script Engine
is such, that dependencies can also be resolved from uncomplied script code, with recompliation being carried out
as the sources are changed. The class name and package tell the GSE to look for the code in this place:

**WEB-INF/grest/pool/H2Pool.groovy**

    package pool

    import org.h2.jdbcx.JdbcConnectionPool
    import groovy.sql.Sql

    class H2Pool {

        static pool = null

        static getSql(context) {
            if (!pool) initPool(context)
            Sql.newInstance(pool)
        }

        static initPool(context) {
            String db = "jdbc:h2:" + context.getRealPath("/WEB-INF/schema")
            pool = JdbcConnectionPool.create(db, "admin", "terces")
            def sql = Sql.newInstance(pool)
            if (!sql.firstRow("select * from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='PUBLIC'")) {
                String script = context.getRealPath("/WEB-INF/schema.sql")
                sql.execute("runscript from '${script}' charset 'UTF-8'")
            }
        }
    }
    
I'm still marvelling at [H2](http://h2database.com). The sample code creates the database files
automatically and then bootstraps the table and its indexes by running the _schema.sql_ script included in WEB-INF.

The entire database is just a 1.3 MB JAR. And that JAR already contains connection pooling and a friendly
little database console servlet that can easily be included in custom web applications:

    <servlet>
        <servlet-name>H2Console</servlet-name>
        <servlet-class>org.h2.server.web.WebServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>H2Console</servlet-name>
        <url-pattern>/h2console/*</url-pattern>
    </servlet-mapping>
