import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.ServletException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import java.io.Serializable;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.time.LocalDate;

@WebServlet("/library")
public class LibraryServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Iterator list = Entry.entries.keySet().iterator();
        response.getWriter().println("<html>");
        response.getWriter().println("<head><title>Library Entries</title></head>");
        response.getWriter().println("<body><center><table>");
        response.getWriter().println("<tr><th>Libraries Issue - Return Entries</th></tr>");
        while(list.hasNext())
        {
            response.getWriter().println("<tr><td>" + Entry.thisEntry((Integer) list.next()) + "</tr></td>");
        }
        response.getWriter().println("</table></center></body>");
        response.getWriter().println("</html>");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(Student.isValid((Integer) request.getSession().getAttribute("studentID")))
        {
            Integer studentID = (Integer) request.getSession().getAttribute("studentID");

            try{
                Integer issueBook = Integer.parseInt(request.getParameter("issueBookID"));
                Entry.issueBook(studentID, issueBook);
            }catch(Exception e){
                //System.out.println(e);
            }

            try{
                Integer copyID = Integer.parseInt(request.getParameter("returnBookID"));
                Entry.returnBook(copyID);
            }catch(Exception e){
                //System.out.println(e);
            }
        }
        response.sendRedirect("/student");
    }
}

class Entry implements Serializable
{
    static HashMap<Integer, Entry> entries = new HashMap<Integer, Entry>();
    private Integer studentID;
    private boolean bookIssued;
    private LocalDate issueDate;
    private Integer bookID;
    private boolean bookReturned;
    private LocalDate returnDate;

    Entry(Integer studentID, Integer bookCopyID)
    {
        this.studentID = studentID;
        this.bookID = bookCopyID;
        this.bookIssued = true;
        this.issueDate = LocalDate.now();
        this.bookReturned = false;
        this.returnDate = null;
    }

    public static String thisEntry(Integer entryID)
    {
        Entry entry = entries.get(entryID);
        Integer studentID = entry.studentID;
        String studentName = Student.studentNameByID(entry.studentID);
        String bookName = Book.bookNameByID(entryID/ 1000);
        String issued = (entry.bookIssued) ? " issue date: " + entry.issueDate.toString() : "";
        String returned = (entry.bookReturned) ? ", return date: " + entry.returnDate.toString() : ", not returned yet";

        return ("Book name: " + bookName + " (copyID: " + entryID +"), issued by student: " + studentName + " (studentID: " + studentID + "), " + issued + returned);
    }

    public static Integer issueBook(Integer studentID, Integer bookID)
    {
        if(!entries.containsKey(bookID)){
            if(Book.isValid(bookID))
            {
                Integer issuedCopyID = Book.pullBook(bookID);
                if(issuedCopyID > 0)
                {
                    if(Student.issueBook(studentID, issuedCopyID))
                    {
                        entries.put(issuedCopyID, new Entry(studentID, bookID));
                        save();
                        return issuedCopyID;
                    }
                    else{
                        Book.pushBook(issuedCopyID);
                    }
                }
            }
        }
        return null;
    }

    public static void returnBook(Integer copyID)
    {
        if(entries.containsKey(copyID))
        {
            Entry thisEntry = entries.get(copyID);
            thisEntry.bookReturned = true;
            thisEntry.returnDate = LocalDate.now();
            if(Student.returnBook(thisEntry.studentID, copyID)){
                Book.pushBook(copyID);
                if(thisEntry.issueDate.plusDays(15).isAfter(thisEntry.returnDate)){
                    Student.addDue(thisEntry.studentID, 5);
                }
                save();
            }
        }
    }

    private static void save()
    {
        try(ObjectOutputStream entriesOut = new ObjectOutputStream(new FileOutputStream("entries")))
        {
            entriesOut.writeObject(entries);
            entriesOut.close();
        }
        catch(Exception error)
        {
            //System.out.println(error);
        }
    }

    static
    {
        try(ObjectInputStream entriesIn = new ObjectInputStream(new FileInputStream("entries")))
        {
            @SuppressWarnings("unchecked")
            HashMap<Integer, Entry> oldentries = (HashMap<Integer, Entry>) entriesIn.readObject();
            Entry.entries.putAll(oldentries);
            entriesIn.close();
        }
        catch(Exception error)
        {
            //System.out.println(error);
        }
    }
}