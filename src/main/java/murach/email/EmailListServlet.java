package murach.email;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.business.User;
import murach.data.UserDB;
import murach.util.MailUtilBrevo;

public class EmailListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "join";
        }

        String url = "/index.jsp";
        if (action.equals("join")) {
            url = "/index.jsp";
        }
        else if (action.equals("add")) {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            User user = new User(email, firstName, lastName);
            request.setAttribute("user", user);

            if (UserDB.emailExists(email)) {
                request.setAttribute("message", "This email address already exists.<br>" + "Please enter another email address.");
                url = "/index.jsp";
            }
            else {
                UserDB.insert(user);
                request.setAttribute("message", "");

                String to = email;
                String from = MailUtilBrevo.getSenderAddress();
                String subject = "Welcome to our email list";
                String body = "Dear " + firstName + ",\n\n"
                        + "Thanks for joining our email list. "
                        + "We'll make sure to send "
                        + "you announcements about new products "
                        + "and promotions.\n"
                        + "Have a great day and thanks again!\n\n"
                        + "Kelly Slivkoff\n"
                        + "Mike Murach & Associates";
                boolean isBodyHTML = false;

                try {
                    MailUtilBrevo.sendMail(to, from, subject, body, isBodyHTML);
                } catch (MessagingException e) {
                    String errorMessage = "ERROR: Unable to send email. "
                            + "Check the server logs for details.<br>"
                            + "ERROR MESSAGE: " + e.getMessage();
                    request.setAttribute("errorMessage", errorMessage);
                    this.log(
                            "Unable to send email. \n"
                                    + "Here is the email you tried to send: \n"
                                    + "=====================================\n"
                                    + "TO: " + email + "\n"
                                    + "FROM: " + from + "\n"
                                    + "SUBJECT: " + subject + "\n\n"
                                    + body + "\n\n", e);
                }
                url = "/thanks.jsp";
            }
        }

        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }
}