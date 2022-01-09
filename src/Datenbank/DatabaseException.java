package Datenbank;

/**
 * Exception fuer die Datenbakoperationnen
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
public class DatabaseException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public DatabaseException(String msg) {
		super(msg);
	}
}
