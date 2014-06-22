import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ForwardingServlet extends HttpServlet {

    protected  void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getRequestURI().endsWith("/samlsso") || request.getRequestURI().endsWith("/openid") ||
                request.getRequestURI().endsWith("/token")){
            request.getRequestDispatcher("home.jsp").forward(request,response);
        } else if (request.getRequestURI().endsWith("/logout")){
            request.getRequestDispatcher("index.jsp").forward(request,response);
        }
    }

}
