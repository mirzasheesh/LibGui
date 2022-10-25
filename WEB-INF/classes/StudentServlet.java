import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.ServletException;

import java.util.HashMap;
import java.util.LinkedList;
import java.io.Serializable;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@WebServlet("/students")
public class StudentServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!(String.valueOf(request.getSession().getAttribute("adminID")).equals("admin")))
        {
            if(String.valueOf(request.getSession().getAttribute("studentID")) == null)
            {
                response.sendRedirect("/admin");
            }else{
                response.sendRedirect("/student");
            }
        }

        response.getWriter().println("<html>");
        response.getWriter().println("<head>");
        response.getWriter().println("<title>Students</title>");
        response.getWriter().println("<link rel='stylesheet' href='css/style.css'>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        response.getWriter().println("<form method='POST' action='/students'>");
        response.getWriter().println("<label>New Entry: </label>");
        response.getWriter().println("<input type='text' name='studentName' placeholder='Name'>");
        response.getWriter().println("<input type='text' name='studentCourse' placeholder='Course'>");
        response.getWriter().println("<input type='text' name='studentDepartment' placeholder='Department'>");
        response.getWriter().println("<button type='submit'>Submit</button></form>");
        response.getWriter().println("<table class='students'>");
        response.getWriter().println("<tr><th>Student ID</th><th>Student Name</th><th>Student Course</th><th>Student Department</th><th>Fee Due</th></tr>");
        for(int index = 1; index <= Student.size(); index++)
        {
            String name = Student.studentNameByID(index);
            String course = Student.studentCourseByID(index);
            String department = Student.studentDepartmentByID(index);
            Integer due = Student.getDue(index);
            response.getWriter().println("<tr><th>" + index + "</th><th>" + name + "</th><th>" + course + "</th><th>" + department + "</th><th>" + due + "</th></tr>");
        }
        response.getWriter().println("</table>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(String.valueOf(request.getSession().getAttribute("adminID")).equals(getServletContext().getInitParameter("username")))
        {
            try{
                String name = String.valueOf(request.getParameter("studentName"));
                String course = String.valueOf(request.getParameter("studentCourse"));
                String department = String.valueOf(request.getParameter("studentDepartment"));
    
                if(name.length() > 1 && !(name.equals("null"))){
                    if(course.length() > 1 && !(course.equals("null"))){
                        if(department.length() > 1 && !(department.equals("null"))){
                            Student.addStudent(name, course, department);
                            response.sendRedirect("/students");
                        }
                    }
                }
            }catch(Exception e){
                //System.out.println(e);
            }
    
            response.getWriter().println("<body><head>");
            response.getWriter().println("<script>window.alert('Invalid Entry!')</script>");
            response.getWriter().println("</head><body>");
            response.getWriter().println("<center><h4>Please go back and enter student name, course and department correctly<h4></center>");
            response.getWriter().println("</body></html>");
        }else{
            request.getRequestDispatcher("admin-panel.html").include(request, response);
        }
    }
}

class Student implements Serializable
{
    private static HashMap<Integer, Student> students = new HashMap<Integer, Student>();
    private static Integer studentCounts = 0;
    private Integer studentID;
    private String studentName;
    private LinkedList<Integer> issuedBook = new LinkedList<Integer>();
    private String studentCourse;
    private String studentDepartment;
    private Integer studentFeeDue;

    Student(Integer studentID, String studentName, String studentCourse, String studentDepartment)
    {
        this.studentID = studentID;
        this.studentName = studentName;
        this.studentCourse = studentCourse;
        this.studentDepartment = studentDepartment;
        this.studentFeeDue = 0;
    }

    public static Integer addStudent(String name, String course, String department)
    {
        students.put(studentCounts + 1, new Student(++studentCounts, name, course, department));
        Student.save();
        return studentCounts;
    }

    public static Boolean isValid(Integer studentID)
    {
        if(students.containsKey(studentID))
        {
            return true;
        }
        return false;
    }

    public static Boolean alreadyIssued(Integer studentID, Integer bookID)
    {
        bookID = ((Integer) (bookID /1000)) * 1000;
        for(Integer start = bookID; start <= bookID + Book.bookQtyByID(bookID/ 1000); start++)
        {
            if(students.get(studentID).issuedBook.contains(start))
            {
                return true;
            }
        }

        return false;
    }

    public static Boolean issueBook(Integer studentID, Integer bookID)
    {
        if(students.containsKey(studentID))
        {
            if(bookID > 0)
            {
                if(!(Student.alreadyIssued(studentID, bookID)))
                {
                    students.get(studentID).issuedBook.addFirst(bookID);
                    save();
                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean returnBook(Integer studentID, Integer bookID)
    {
        if(students.containsKey(studentID))
        {
            if(bookID > 0)
            {
                Boolean state = students.get(studentID).issuedBook.remove(bookID);
                save();
                return state;
            }
        }
        return false;
    }

    public static LinkedList booksList(Integer studentID)
    {
        if(students.containsKey(studentID))
        {
            return students.get(studentID).issuedBook;
        }

        return null;
    }

    public static String studentNameByID(Integer studentID)
    {
        if(students.containsKey(studentID))
        {
            return students.get(studentID).studentName;
        }
        return null;
    }
    
    public static String studentCourseByID(Integer studentID)
    {
        if(students.containsKey(studentID))
        {
            return students.get(studentID).studentCourse;
        }
        return null;
    }

    public static String studentDepartmentByID(Integer studentID)
    {
        if(students.containsKey(studentID))
        {
            return students.get(studentID).studentDepartment;
        }
        return null;
    }
    
    public static void addDue(Integer studentID, Integer amount)
    {
        if(students.containsKey(studentID))
        {
            if(amount > 0)
            {
                students.get(studentID).studentFeeDue = students.get(studentID).studentFeeDue + amount;
            }else if(amount < 0 && -(amount) <= students.get(studentID).studentFeeDue)
            {
                students.get(studentID).studentFeeDue = students.get(studentID).studentFeeDue + amount;
            }
            save();
        }
    }

    public static Integer getDue(Integer studentID)
    {
        if(students.containsKey(studentID))
        {
            return students.get(studentID).studentFeeDue;
        }
        return null;
    }

    public static Integer size()
    {
        return Student.studentCounts;
    }
    
    private static void save()
    {
        try(ObjectOutputStream studentsOut = new ObjectOutputStream(new FileOutputStream("students")))
        {
            studentsOut.writeObject(students);
            studentsOut.close();
        }
        catch(Exception error)
        {
            //System.out.println(error);
        }
    }

    static
    {
        try(ObjectInputStream studentsIn = new ObjectInputStream(new FileInputStream("students")))
        {
            @SuppressWarnings("unchecked")
            HashMap<Integer, Student> oldStudents = (HashMap<Integer, Student>) studentsIn.readObject();
            Student.students.putAll(oldStudents);
            Student.studentCounts = students.size();
            studentsIn.close();
        }
        catch(Exception error)
        {
            //System.out.println(error);
        }
    }
}