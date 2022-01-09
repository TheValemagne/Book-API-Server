package Client;

import java.net.ConnectException;
import java.net.http.HttpRequest;
import java.text.ParseException;
import java.util.Scanner;

/**
 * Launcher für den Consolen-Basierten Client
 *             
 * @author Jeremy Houde
 * @version 1.0
 */
public class ClientLauncher {
	private static RestClient client;
	private static int port = 4711;
	
	static private final String STOP = "end";
	static private String EXCEPTION_MSG = "Client error: Der Request-Format ist nicht korrekt!\n"
									    + "Für GET und DELETE: METHOD url\n"
									    + "Für POST und PUT: METHOD url {  \"attribut\" : \"string\", ...  }\n";
	static private String MENU = "RestClient auf Port " + port + " gestartet.\n"
							   + "Beendigung des Programms mit \"end\".\n"
							   + "Request Beispiele:\n"
							   + "  GET /buchliste\n"
							   + "  GET /buchliste/9\n"
							   + "  GET /buchliste/Hugh_Howey\n"
							   + "  POST /buchliste { \"isbn\" : 1, \"titel\" : \"titel\", \"autor\" : \"autor\" }\n"
							   + "  PUT /buchliste/1 { \"titel\" : \"titel\", \"autor\" : \"autor\" }\n"
							   + "  DELETE /buchliste/1\n";
	
	/**
	 * Startet einen RestClient mit Consolensteuerung
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		client = new RestClient(port);
		
		System.out.println(MENU);
		
		Scanner sc = new Scanner(System.in);
		String client_request = "";
		
		do {
			try {
				System.out.println("Request: ");
				client_request = sc.nextLine();
				
				if(!client_request.equals(STOP)) {
					HttpRequest request = client.getRequest(client_request.trim());
					client.sendRequest(request);
				}
			} catch(ConnectException e) {
				System.out.println("Starte Server!");
			} catch(ParseException e) {
				System.out.println(EXCEPTION_MSG);
			} catch (Exception e) {
				System.out.println("Client exception: " + e.getMessage());
			}
			
		} while(!client_request.equals(STOP));
		
		System.out.println("Client gestoppt.");
		sc.close();
		client = null;
	}

}
