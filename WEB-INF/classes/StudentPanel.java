//completed

import java.io.IOException;
import java.util.Iterator;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; 

import jakarta.servlet.ServletException;

@WebServlet("/student")
public class StudentPanel extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(request.getSession().getAttribute("studentID") == null)
        {
            request.getRequestDispatcher("login-student.html").include(request, response);
        }else
        {
            Integer studentID = (Integer) request.getSession(false).getAttribute("studentID");
            response.getWriter().println("<html><head><title>Student Details</title></head><body><center>");
            response.getWriter().println("<h4>Student Name: " + Student.studentNameByID(studentID) + "</h4>");
            response.getWriter().println("<p>Course: " + Student.studentCourseByID(studentID));
            response.getWriter().println("| Department: " + Student.studentDepartmentByID(studentID));
            response.getWriter().println("| Fee Due: " + Student.getDue(studentID) + "</p>");
            
            response.getWriter().println("<form method='POST' action='/library'><lable>Issue Book: </label><input type='number' min='1' name='issueBookID' placeholder='Book ID'> <button type='submit'>Issue</button></form>");
            response.getWriter().println("<form method='POST' action='/library'><lable>Return Book: </label><input type='number' min='1' name='returnBookID' placeholder='Copy ID'> <button type='submit'>Return</button></form>");
            
            response.getWriter().println("<h4>Issued books :</h4>");
            response.getWriter().println("<table>");

            Iterator iterateBooks = Student.booksList(studentID).iterator();
            while(iterateBooks.hasNext())
            {
                Integer copyID = (Integer) iterateBooks.next();
                response.getWriter().println("<tr><td>" + copyID + " : </td><td>" + Book.bookNameByID(copyID /1000) + "</td></tr>");
            }
            response.getWriter().println("</table></center></body></html>");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Integer studentID = Integer.parseInt(request.getParameter("username"));

            if(Student.isValid(studentID))
            {
                if(String.valueOf(request.getParameter("password")).equals("studentpass"))
                {
                    request.getSession().setAttribute("studentID", studentID);
                    response.sendRedirect("/student");
                }
            }
        }catch(Exception e)
        {
            //System.out.println(e);
        }
        response.getWriter().println("<script>window.alert('Invalid username and password')</script>");
        request.getRequestDispatcher("login-student.html").include(request, response);
    }
}