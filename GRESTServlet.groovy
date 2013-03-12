package net.eddelbuettel.grest

import groovy.servlet.ServletBinding
import groovy.servlet.ServletCategory
import groovy.transform.CompileStatic
import groovy.util.XmlSlurper
import groovy.json.JsonSlurper

import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class GRESTServlet extends HttpServlet implements ResourceConnector {

    static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path"
    static final String INC_PATH_INFO = "javax.servlet.include.path_info"
    
    static final String ctJSON = "application/json"
    static final String ctXML  = "application/xml"
    static final String ctHTML = "text/html"
    static final String ctTEXT = "text/plain"
    static final String ctJS   = "text/javascript"
    
    GroovyScriptEngine gse
    String prefix1
    String prefix2 = null

    @CompileStatic
    @Override
    void init(ServletConfig config) {
        super.init(config)
        gse = new GroovyScriptEngine(this)
        gse.config.sourceEncoding = "UTF-8"
        prefix1 = servletContext.getResource("/").toExternalForm()
    }

    @CompileStatic
    URLConnection getResourceConnection(String name) throws ResourceException {
        
        if (name.startsWith(prefix1))
            name = name.substring(prefix1.length())

        name = name.replaceAll("\\\\", "/")
            
        if (name.startsWith("/"))
            name = name.substring(1)
        
        if (!name.startsWith(prefix2))
            name = prefix2 + "/" + name
        
        def url = servletContext.getResource("/" + name)
        if (!url) throw new ResourceException(name)
        url.openConnection()
    }
            
    @CompileStatic
    @Override
    void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException 
    {
        def method = request.method.toLowerCase()
        def ct = ctHTML
        
        String accept = request.getHeader("Accept")
        if (accept) {
            if (accept.startsWith(ctJSON)) {
                ct = ctJSON
                request.setAttribute("produce", "json")
            }
            else if (accept.startsWith(ctXML)) {
                ct = ctXML
                request.setAttribute("produce", "xml")
            }
            else if (accept.startsWith(ctTEXT)) {
                ct = ctTEXT
                request.setAttribute("produce", "text")
            }
            else if (accept.startsWith(ctHTML)) {
                ct = ctHTML
                request.setAttribute("produce", "html")
            }
        }

        if (request.getParameter("callback")) {
            request.setAttribute("produce", "jsonp")
            ct = "text/javascript"
        }
        
        response.contentType = ct + ";charset=UTF-8"
        
        def script = new StringBuilder("/WEB-INF")

        String pathInfo
        if (request.getAttribute(INC_SERVLET_PATH)) {
            script << request.getAttribute(INC_SERVLET_PATH)
            pathInfo = request.getAttribute(INC_PATH_INFO)
        }
        else {
            script << request.servletPath
            pathInfo = request.pathInfo
        }
        
        if (prefix2 == null) {
            prefix2 = script.substring(1)
        }

        URL resource = null
        int j = 0
        pathInfo?.substring(1).tokenize("/").each
        {
            if (!resource) {
                script << "/" << it
                int len = script.length()

                script << "_" << method << ".groovy"
                resource = servletContext.getResource(script.toString())               
                if (!resource) {
                    script.length = len
                    script << ".groovy"
                    resource = servletContext.getResource(script.toString())
                    if (!resource) {
                        script.length = len
                    }
                }                
            }
            else {
                request.setAttribute(
                    "pathvar${++j}", URLDecoder.decode(it.toString(), "UTF-8")
                );
            }
        }

        if (!resource) {
            response.sendError(404, "No matching GRESTlet")
            return
        }

        try {
            runScript(script, request, response)
        }
        catch (Exception ex) {
            throw new ServletException(ex)
        }
    }
    
    void runScript(script, request, response)
    {
        def binding = new ServletBinding(request, response, servletContext)
        
        String ct = request.contentType
        
        def data = null;
        
        if (request.contentLength > 0) {
            
            def enc = request.characterEncoding ?: "UTF-8"
            
            if (ct.startsWith(ctJSON)) {
                data = new JsonSlurper().parse(request.inputStream.newReader(enc))
                request.setAttribute("consume", "json")
            }
            else if (ct.startsWith(ctXML)) {
                data = new XmlSlurper().parse(request.inputStream.newReader(enc))
                request.setAttribute("consume", "xml")
            }
            else if (ct.startsWith(ctTEXT)) {
                data = []
                request.inputStream.eachLine("UTF-8") {
                    data.push it
                }
                request.setAttribute("consume", "text")
            }
        }
        
        binding.setVariable("data", data)
        
        use(ServletCategory) {
            gse.run(script.substring(1), binding)
        }
    }
}
