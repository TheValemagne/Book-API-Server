package Server;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import Datenbank.Buch;
import Datenbank.BuchList;
import Datenbank.DatabaseException;
import Datenbank.NotFoundException;
import Datenbank.JsonCodec.JsonCodecException;

/**
 * Handler Klasse, um die Resquest zu bearbeiten.
 * 
 * @author Jeremy houde
 * @version 1.0
 */
public class Handler implements HttpHandler, HttpStatus {
	private int port;
	private String path;
	private BuchList buchlist;
	
	private static final String CHARSET = "utf-8";
	private static final String NUMBER_REGEX = "^-?\\d+$";
	private static final String CONTENT_TYPE = "application/json; charset=" + CHARSET;
	private static final String ERLAUBTEN_METHODEN = "GET, POST, PUT, DELETE";
	private static final String KEINE_GUELTIGE_URL = "\"Keine gueltige URL.\"";
	
	/**
	 * Handler Konstructor
	 * 
	 * @param port, muss positiv sein
	 * @param path, darf nicht leer sein
	 * @throws DatabaseException Problem mit der Datenbank
	 * @throws JsonCodecException Fehler im JSON-String 
	 */
	public Handler(int port, String path) throws DatabaseException, JsonCodecException{
		this.port = port;
		this.path = path;
		
		buchlist = new BuchList();
		buchlist.fromJSON(buchlist.fromFile()); // Initialisert die Liste mit den Inhalt der JSON-Datei.
	}
	
	/**
	 * Bearbeite eine GET-Resquest
	 * 
	 * @param uri
	 * @param ressource, Kontext, ISBN oder Autor
	 * @return response
	 * @throws JsonCodecException Fehler im JSON-String
	 * @throws NotFoundException Das Buch ist nicht vorhanden
	 */
	private Response handleGet(String uri, String ressource) throws JsonCodecException, NotFoundException {
		String message = "";
		int http_code = BAD_REQUEST;
		
		if(uri.endsWith(path)){ // URL: /buchliste
			message = buchlist.toJSON(); // die ganze liste	als JSON-Liste	
			http_code = OK;
		} else if(uri.endsWith(path + "/" + ressource) && ressource.matches(NUMBER_REGEX)) { // URL: /buchliste/{isbn}
			int isbn = Integer.parseInt(ressource);
			message = buchlist.getBuch(isbn).toJSON(); // ein bestimmtes Buch als JSON-Objekt
			http_code = OK;
		} else if(uri.endsWith(path + "/" + ressource)) { // URL: /buchliste/{autor}
			String autor = ressource.replace("_", " ");
			message = buchlist.getBuchList(b -> b.getAutor().equals(autor)).toJSON(); // Alle Bucher des Autors als JSON-Liste
			http_code = OK;
		} else { // keine gueltige URL
			message = KEINE_GUELTIGE_URL;
		}
		
		return new Response(http_code, message);
	}
	
	/**
	 * Bearbeite eine POST-Request.
	 * 
	 * @param uri
	 * @param ressource, der Kontext
	 * @param body, JSON-Objekt mit ISBN, Titel und Autor
	 * @return response
	 * @throws DatabaseException Problem mit der Datenbank
	 * @throws JsonCodecException Fehler im JSON-String
	 */
	private Response handlePost(String uri, String ressource, String body) throws DatabaseException, JsonCodecException {
		if(uri.endsWith(path)){ // URL: /buchliste
			Buch buch = new Buch();
			buch.fromJSON(body); // Initialisiert ein neues Buch mit einen JSON-Objekt
			
			buchlist.addBuch(buch); // Das Buch wird eingefuegt
			buchlist.toFile(); // Aktualisierung der JSON-Datei
			String message = "http://localhost:" + port + path + "/" + buch.getIsbn(); // URL des neuen Buches
			
			return new Response(CREATED, message);
		} else {
			return new Response(BAD_REQUEST, KEINE_GUELTIGE_URL);
		}
	}
	
	/**
	 * Bearbeite eine PUT-Request
	 * 
	 * @param uri
	 * @param ressource
	 * @param body, JSON-Objekt mit Titel und Autor
	 * @return response, die ISBN eines Buches
	 * @throws DatabaseException Problem mit der Datenbank
	 * @throws JsonCodecException Fehler im JSON-String
	 * @throws NotFoundException Das Buch ist nicht vorhanden
	 */
	private Response handlePut(String uri, String ressource, String body) throws DatabaseException, JsonCodecException, NotFoundException {
		if(uri.endsWith(path + "/" + ressource) && ressource.matches(NUMBER_REGEX)) { // URL: /buchliste/{isbn}
			int isbn = Integer.parseInt(ressource);
			body = "{ \"isbn\" : " + isbn + ", " + body.substring(2); // JSON-Objekt mit isbn
			buchlist.updateBuch(isbn, body); // Aktualisierung des Buches
			buchlist.toFile(); // Aktualisierung der JSON-Datei
			
			return new Response(NO_CONTENT, "");
		} else {
			return new Response(BAD_REQUEST, KEINE_GUELTIGE_URL);
		}
	}
	
	/**
	 * Bearbeite eine DELETE-Request.
	 * 
	 * @param ressource
	 * @return response
	 * @throws DatabaseException Problem mit der Datenbank
	 * @throws JsonCodecException Fehler im JSON-String
	 * @throws NotFoundException Das Buch ist nicht vorhanden 
	 */
	private Response handleDelete(String uri, String ressource) throws DatabaseException, JsonCodecException, NotFoundException{
		if(uri.endsWith(path + "/" + ressource) && ressource.matches(NUMBER_REGEX)) { // url: /buchliste/{isbn}
			int isbn = Integer.parseInt(ressource);
			buchlist.removeBuch(isbn); // Das Buch wird geloecht
			buchlist.toFile(); // Aktualisierung der JSON-Datei
			
			return new Response(NO_CONTENT, "");
		} else {
			return new Response(BAD_REQUEST, KEINE_GUELTIGE_URL);
		}
	}
	
	/**
	 * Benutze den gewuenchte Handler aus.
	 * 
	 * @param exchange
	 * @return response
	 * @throws IOException
	 * @throws DatabaseException Problem mit der Datenbank
	 * @throws JsonCodecException Fehler im JSON-String
	 * @throws NotFoundException Das Buch ist nicht vorhanden
	 */
	private Response selecteHandler(HttpExchange exchange) throws DatabaseException, IOException, JsonCodecException, NotFoundException {
		Response response;
		
		String method = exchange.getRequestMethod(); // die angefordete Methode
		String uri = exchange.getRequestURI().getPath(); // die URL des Requests
		String ressource = uri.split("/")[uri.split("/").length - 1]; // in Normalen Fall : ISBN, Autor oder den Kontext
		String body = new String(exchange.getRequestBody().readAllBytes());
		
		switch(method) {	
			case "GET" :
				response = handleGet(uri, ressource);
				break;
				
			case "POST" :
				response = handlePost(uri, ressource, body);
				break;
				
			case "PUT" :
				response = handlePut(uri, ressource, body);
				break;
				
			case "DELETE" :
				response = handleDelete(uri, ressource);
				break;
				
			default : // Die Methode ist nicht erlaubt
				response = new Response(METHOD_NOT_ALLOWED, "\"Die Methode ist nicht implementiert.\"");
		}
		
		return response;
	}
	
	/**
	 * Sende die Antwort zum bearbeiteten Request.
	 * 
	 * @param exchange
	 * @param response
	 * @throws IOException
	 */
	private void sendResponse(HttpExchange exchange, Response response) throws IOException {
		int statusCode = response.statusCode(); // HTTP-Status Code der Antwort
		
		if(statusCode == METHOD_NOT_ALLOWED) { // Parameter Allow im Header: die erlaubten Methoden
			exchange.getResponseHeaders().add("Allow", ERLAUBTEN_METHODEN);
		}
		
		if(statusCode == CREATED) { // die POST-Methode liefert eine Location im Header
			exchange.getResponseHeaders().add("Location", response.getMessage());
		} else if(statusCode != NO_CONTENT){ // Body ist im JSON-Format
			exchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE);
		}
		
		if(statusCode == NO_CONTENT || statusCode == CREATED) {
			exchange.sendResponseHeaders(statusCode, -1); // kein Inhalt im Responsebody, wird mit -1 gekenntzeichnet
		} else { // Antwort mit einem Inhalt
			exchange.sendResponseHeaders(statusCode, response.getMessage().getBytes(CHARSET).length);
		}
		
		OutputStream os = exchange.getResponseBody();
		os.write(response.getMessage().getBytes(CHARSET)); // sende die Antwort, auch wenn es keinen Body gibt. (getResponseBody Dokumentation)
		os.close();
	}
	
	/**
	 * Sende die Fehlermeldung.
	 * 
	 * @param exchange
	 * @param error, die Fehlermeldung
	 * @param status
	 * @throws IOException
	 */
	private void sendErrorMessage(HttpExchange exchange, int status, String error) throws IOException {
		String error_message = "\"" + error.replace('"', '\'') + "\"";
		Response response = new Response(status, error_message); // Die Java-Fehlermeldung wird im JSON-Format umgewandelt
		sendResponse(exchange, response);
	}
	
	/**
	 * Bearbeite und sende die Antwort zum Client.
	 * 
	 * @throws IOException 
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		try { // Bearbeite einen HTTP-Request des Kundes				
			Response response = selecteHandler(exchange);
			sendResponse(exchange, response);
			
		} catch (DatabaseException e) { // Fehler mit der Datenbank (Leserechte, Schreibrechte)
			sendErrorMessage(exchange, INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (NotFoundException e){ // Das Buch wurde nicht gefunden
			sendErrorMessage(exchange, NOT_FOUND, e.getMessage());
		} catch(NumberFormatException e) { // Fehler fuer die methode POST, PUT und DELETE when die ISBN keine Nummer ist oder die URL ist nicht behandelt.
			sendErrorMessage(exchange, BAD_REQUEST, KEINE_GUELTIGE_URL);
		} catch (Exception e) { // JsonCodexException oder IllegalArgumentException
			sendErrorMessage(exchange, BAD_REQUEST, e.getMessage());
		}
	}
}
