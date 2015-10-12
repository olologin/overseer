package ru.hdghg.spy.web.servlet;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.slf4j.Logger;
import ru.hdghg.spy.ejb.SpyBean;

@WebServlet(urlPatterns = {"/spy"})
public class SpyServlet extends HttpServlet {

    @Inject
    private SpyBean spyBean;

    @Inject
    private Logger log;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("Page called");
        String action = request.getParameter("action");

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Spy control panel</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Date is:" + new Date() + "</h1>");
            out.println("Action run: " + ("start".equals(action)
                ? spyBean.start()
                : "stop".equals(action)
                    ? spyBean.stop()
                    : "no action") + "<br />");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
