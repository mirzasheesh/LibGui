//Completed

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

@WebServlet("/admin")
public class ManagementPanel extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(String.valueOf(request.getSession().getAttribute("adminID")).equals(getServletContext().getInitParameter("username")))
        {
            request.getRequestDispatcher("admin-panel.html").include(request, response);
        }else
        {
            request.getRequestDispatcher("login-admin.html").include(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String username = String.valueOf(request.getParameter("username"));
        String password = String.valueOf(request.getParameter("password"));

        String defaultUser = String.valueOf(getServletContext().getInitParameter("username"));
        String defaultPass = String.valueOf(getServletConfig().getInitParameter("password"));

        if(username.equals(defaultUser) && password.equals(defaultPass))
        {
            request.getSession().setAttribute("adminID", defaultUser);
            response.sendRedirect("/admin");
        }
        else
        {
            response.getWriter().println("<script>window.alert(`Incorrect username and password`)</script>");
            request.getRequestDispatcher("login-admin.html").include(request, response);
        }
    }
}