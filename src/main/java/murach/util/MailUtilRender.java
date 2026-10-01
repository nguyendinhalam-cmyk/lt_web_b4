package murach.util;

import javax.mail.MessagingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MailUtilRender {

    public static void sendMail(String to, String from,
                                String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        try {
            String apiKey = System.getenv("BREVO_API_KEY");
            if (apiKey == null || from == null) {
                throw new MessagingException("Thiếu BREVO_API_KEY hoặc SENDER_EMAIL");
            }

            String cleanFrom = from.trim();
            String cleanTo = to.trim();
            String cleanSubject = subject.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", " ")
                    .replace("\r", "");
            String cleanBody = body.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            String jsonPayload = "{"
                    + "\"sender\":{\"email\":\"" + cleanFrom + "\"},"
                    + "\"to\":[{\"email\":\"" + cleanTo + "\"}],"
                    + "\"subject\":\"" + cleanSubject + "\","
                    + (bodyIsHTML ? "\"htmlContent\":\"" : "\"textContent\":\"") + cleanBody + "\""
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new MessagingException("Lỗi Brevo API (Mã " + response.statusCode() + "): " + response.body());
            }
        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("Không thể gửi mail qua HTTP API: " + e.getMessage(), e);
        }
    }
}