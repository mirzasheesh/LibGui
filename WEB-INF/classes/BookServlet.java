//completed

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

@WebServlet("/books")
public class BookServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Boolean isAdmin = String.valueOf(request.getSession().getAttribute("adminID")).equals(getServletContext().getInitParameter("username"));

        response.getWriter().println("<html>");
        response.getWriter().println("<head>");
        response.getWriter().println("<title>Books</title>");
        response.getWriter().println("<link rel='stylesheet' href='css/style.css'>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        
        if(isAdmin)
        {
            response.getWriter().println("<form method='POST' action='/books'>");
            response.getWriter().println("<label>New Entry: </label>");
            response.getWriter().println("<input type='text' name='bookName' placeholder='Book Title'>");
            response.getWriter().println("<button type='submit'>Submit</button></form></br>");
            response.getWriter().println("<form method='POST' action='/books'>");
            response.getWriter().println("<label>Update Stock: </label>");
            response.getWriter().println("<input type='number' name='bookID' placeholder='Book ID'>");
            response.getWriter().println("<input type='number' name='bookQty' placeholder='Quantity'>");
            response.getWriter().println("<button type='submit'>Submit</button></form>");
        }

        response.getWriter().println("<table class='students'>");
        response.getWriter().println("<tr><th>Book ID</th><th>Book Name</th><th>Book Qty</th></tr>");
        
        for(int index = 1; index <= Book.size(); index++)
        {
            String name = Book.bookNameByID(index);
            Integer quantity = Book.bookQtyByID(index);
            response.getWriter().println("<tr><th>" + index + "</th><th>" + name + "</th><th>" + quantity + "</th></tr>");
        }
        
        response.getWriter().println("</table>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Boolean isAdmin = String.valueOf(request.getSession().getAttribute("adminID")).equals(getServletContext().getInitParameter("username"));

        if(isAdmin)
        {
            try{
                String bookName = String.valueOf(request.getParameter("bookName"));

                if(bookName.length() > 1 && !(bookName.equals("null")))
                {
                    Book.addBook(bookName);
                    response.sendRedirect("/books");
                }
            }catch(Exception e)
            {
                //System.out.println(e);
            }

            try
            {
                Integer bookID = Integer.parseInt(request.getParameter("bookID"));
                Integer bookQty = Integer.parseInt(request.getParameter("bookQty"));

                if(bookID > 0 && bookQty > 0)
                {
                    if(!(Book.isValid(bookID)))
                    {
                        throw new IOException();
                    }
                    
                    Book.addQty(bookID, bookQty);
                    response.sendRedirect("/books");
                }
            }
            catch(Exception e)
            {
                //System.out.println(e);
            }
        }
        
        response.getWriter().println("<body><head>");
        response.getWriter().println("<script>window.alert('Invalid Entry!')</script>");
        response.getWriter().println("</head><body>");
        response.getWriter().println("<center><h4>Please go back and enter details correctly<h4></center>");
        response.getWriter().println("</body></html>");
    }
}

class Book implements Serializable
{
    private static HashMap<Integer, Book> books = new HashMap<Integer, Book>();
    private static Integer uniqueID = 0;
    private Integer bookID;
    private String bookName;
    private Integer bookQuantity;
    private LinkedList<Integer> bookCopies = new LinkedList<Integer>();

    Book(Integer bookID, String bookName)
    {
        this.bookID = bookID;
        this.bookName = bookName;
        this.bookQuantity = 0;
    }

    public static Integer addBook(String bookName)
    {
        Integer newID = ++uniqueID;
        books.put(newID, new Book(newID, bookName));
        save();
        return newID;
    }

    public static Boolean isValid(Integer bookID)
    {
        if(books.containsKey(bookID))
        {
            return true;
        }
        return false;
    }

    public static Integer pullBook(Integer bookID)
    {
        if(books.containsKey(bookID))
        {
            if(books.get(bookID).bookQuantity > 0)
            {
                Integer copyID =  books.get(bookID).bookCopies.removeFirst();
                if(copyID != null)
                {
                    books.get(bookID).bookQuantity--;
                    save();
                    return copyID;
                }
            }
        }
        return null;
    }

    public static void pushBook(Integer copyID)
    {
        Integer bookID = copyID/1000;
        if(books.containsKey(bookID))
        {
            books.get(bookID).bookCopies.addLast(copyID);
            books.get(bookID).bookQuantity++;
            save();
        }
    }

    public static void addQty(Integer bookID, Integer bookQty)
    {
        if(books.containsKey(bookID))
        {
            books.get(bookID).bookQuantity = books.get(bookID).bookQuantity + bookQty;
            for(int index = (bookID * 1000); index < (bookID * 1000) + bookQty; index++)
            {
                books.get(bookID).bookCopies.add(index);
            }
            save();
        }
    }

    public static String bookNameByID(Integer bookID)
    {
        if(books.containsKey(bookID))
        {
            return books.get(bookID).bookName;
        }
        return null;
    }

    public static Integer bookQtyByID(Integer bookID)
    {
        if(books.containsKey(bookID))
        {
            return books.get(bookID).bookQuantity;
        }
        return null;
    }

    public static Integer size()
    {
        return Book.uniqueID;
    }

    private static void save()
    {
        try(ObjectOutputStream booksOut = new ObjectOutputStream(new FileOutputStream("books")))
        {
            booksOut.writeObject(books);
            booksOut.close();
        }
        catch(Exception error)
        {
            //System.out.println(error);
        }
    }

    static
    {
        try(ObjectInputStream booksIn = new ObjectInputStream(new FileInputStream("books")))
        {
            @SuppressWarnings("unchecked")
            HashMap<Integer, Book> oldbooks = (HashMap<Integer, Book>) booksIn.readObject();
            Book.books.putAll(oldbooks);
            Book.uniqueID = books.size();
            booksIn.close();
        }
        catch(Exception error)
        {
            //System.out.println(error);
        }
    }
}