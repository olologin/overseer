package wildfly.wildfly.web.servlet;

import wildfly.wildfly.ejb.SpyBean;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;

@WebServlet(urlPatterns = {"/spy"})
public class SpyServlet extends HttpServlet {

    @Inject
    private SpyBean spyBean;

    @Inject
    private Logger log;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            log.debug("Page called");

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Singleton Bean</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Singleton Bean</h1>");
            out.println("<h2>Container-managed Concurrency</h2>");
            out.println("Spybean start " + spyBean.start() + "<br />");
            out.println("Spybean stop " + spyBean.stop() + "<br />");
            out.println("</body>");
            out.println("</html>");
        }
    }
}