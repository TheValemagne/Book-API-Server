package Client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.text.ParseException;

import Server.HttpStatus;

/**
 * Einfachen RestClient
 *             
 * @author Jeremy Houde
 * @version 1.0
 */
public class RestClient implements HttpStatus{
	private int port;
	private HttpClient client = HttpClient.newHttpClient();
	
	static private final String RESPONSE_FORMAT = "'{'  \"msg\" : {0}  '}'";
	static private final String REQUEST_FORMAT_JSON = "{0} {1} '{' {2} '}'"; // method url { json }
	static private final MessageFormat find_response = new MessageFormat(RESPONSE_FORMAT);
	
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	
	/**
	 * Initialisiert den RestClient.
	 * 
	 * @param port, muss positiv sein
	 */
	public RestClient(int port) {
		if(port <= 0) {
			throw new IllegalArgumentException("Ungueltiger Port.");
		}
		
		this.port = port;
		
	}
	
	/**
	 * Erstehlt einen HTTP-Request mit der Eingabe des Benutzes
	 * 
	 * @param client_request, darf nicht leer sein
	 * @return HTTPrequest
	 * @throws ParseException
	 */
	public HttpRequest getRequest(String client_request) throws ParseException {
		if(client_request.trim().isEmpty()) {
			throw new IllegalArgumentException("Der Request darf nicht leer sein.");
		}
		
		String host = "http://localhost:" + port ;
		
		String url = null;
		Object[] obj = null; // fuer POST und PUT
		HttpRequest request = null;
		MessageFormat find_post = new MessageFormat(REQUEST_FORMAT_JSON);
		
		String method = client_request.split(" ")[0]; // die Methode des Requests
		
		switch(method) {
			case "GET": 
				url = client_request.split(" ")[1].trim();
				request = HttpRequest.newBuilder()
						             .uri(URI.create(host + url))
							         .GET()
							         .build();
				break;
				
			case "POST":
				obj = find_post.parse(client_request);
				url = obj[1].toString().trim();
				
				request = HttpRequest.newBuilder()
						             .uri(URI.create(host + url))
						             .header("Content-Type",  CONTENT_TYPE)
							         .POST(HttpRequest.BodyPublishers.ofString("{ " + obj[2].toString().trim() + " }"))
							         .build();
				break;
				
			case "PUT": 
				obj = find_post.parse(client_request);
				url = obj[1].toString().trim();

				request = HttpRequest.newBuilder()
						             .uri(URI.create(host + url))
						             .header("Content-Type",  CONTENT_TYPE)
							         .PUT(HttpRequest.BodyPublishers.ofString("{ " + obj[2].toString() + " }"))
							         .build();
				break;
				
			case "DELETE":
				url = client_request.split(" ")[1].trim();
				
				request = HttpRequest.newBuilder()
						             .uri(URI.create(host + url))
							         .DELETE()
							         .build();
				break;
				
			default : // Die methode ist nicht GET, POST, PUT oder DELETE
				url = client_request.split(" ")[1].trim();
				
				request = HttpRequest.newBuilder()
									 .uri(URI.create(host + url))
									 .method(method, HttpRequest.BodyPublishers.noBody())
								 	 .build();
		}
		
		return request;
	}
	
	/**
	 * Sende den Request des Clients
	 * 
	 * @param request
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	public void sendRequest(HttpRequest request) throws IOException, InterruptedException, ParseException {
		
		if(request != null) {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
			if(response.statusCode() == NO_CONTENT || response.statusCode() == CREATED) { // Antwort ohne Inhalt im body
				System.out.println("\nAntwort\n"
				         		 + "Status: " + response.statusCode() + "\n"
				         		 + "Kein Inhalt");
			} else {
				Object[] obj = find_response.parse(response.body()); // JSON-Antwort
				System.out.println("\nAntwort\n"
						         + "Status: " + response.statusCode() + "\n"
						         + "Inhalt: \n" +  obj[0] + "\n");
			}
		}
	}

}
