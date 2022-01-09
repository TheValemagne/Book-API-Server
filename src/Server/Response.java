package Server;

import java.text.MessageFormat;

/**
 * Response Klasse, die Antworten von RestServer werden mit diese Klasse bearbeitet.
 * 
 * @author Jeremy houde
 * @version 1.0
 */
public class Response implements HttpStatus{
	private int statusCode;
	private String message;
	
	static final String RESPONSE_FORMAT = "'{'  \"msg\" : {0}  '}'";
	
	/**
	 * Response Konstructor 
	 * 
	 * @param statusCode
	 * @param message
	 */
	public Response(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
	
	/**
	 * Gibt den http Statuscode zurueck.
	 * 
	 * @return http Statuscode
	 */
	public int statusCode() {
		return statusCode;
	}
	
	/**
	 * Gibt die Inhalt der Antwort in JSON-Format oder in plain Text (nur für http status 201) zurueck.
	 * 
	 * @return message
	 */
	public String getMessage() {
		if(statusCode == CREATED) { // Im Fall eine korrekten POST-Request fuer den Location im Header
			return message;
		}
		
		return MessageFormat.format(RESPONSE_FORMAT, message); // { "msg" : <Nachricht> }
	}
}
