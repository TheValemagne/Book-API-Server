package Datenbank;

/**
 * Exception fuer NOT FOUND (404)
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
public class NotFoundException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public NotFoundException(String msg) {
		super(msg);
	}
}
