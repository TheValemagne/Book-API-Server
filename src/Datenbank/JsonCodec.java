package Datenbank;

/**
 * Interface fuer eine REST-API.
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
public interface JsonCodec {
	
	/**
	 * Exception fuer die JSON-Operationnen
	 * 
	 * @author Jeremy Houde
	 * @version 1.0
	 */
	class JsonCodecException extends Exception{
		
		private static final long serialVersionUID = 1L;

		public JsonCodecException(String msg) {
			super(msg);
		}
	}

	/**
	 * Gibt die JSON-Darstellung eines Objects.
	 * 
	 * @return JSON-String
	 * @throws JsonCodecException Fehler in JSON-String
	 */
	public String toJSON() throws JsonCodecException;
	
	/**
	 * Erstehllt einen Objeckt mit Hilfe eines JSON-String.
	 * 
	 * @param json
	 * @throws JsonCodecException Fehler in JSON-String
	 */
	public void fromJSON(String json) throws JsonCodecException;
}
