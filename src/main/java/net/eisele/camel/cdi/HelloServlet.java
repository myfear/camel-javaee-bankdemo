/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eisele.camel.cdi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.eisele.camel.jpa.bank.CustomerStatusService;

/**
 *
 * @author myfear
 */
@WebServlet(name = "HelloServlet", urlPatterns = {"/*"}, loadOnStartup = 1)
public class HelloServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(HelloServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String name = req.getParameter("name");
        ServletOutputStream out = res.getOutputStream();

        // ProducerTemplate producer = camelctx.createProducerTemplate();
        // String result = producer.requestBody("direct:start", "", String.class);
        LOGGER.log(Level.INFO, ">> {0}", name);
        out.print(name);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
