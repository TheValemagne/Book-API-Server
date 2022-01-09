package Datenbank;

import java.text.MessageFormat;

/**
 * Datentabelle fuer eine REST-API. Buch enthaelt die isbn, einen Titel und einen Autor.
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
public class Buch implements JsonCodec{
	private int isbn;
	private String titel;
	private String autor;
	
	private static final String MSG_ATTRIBUTS_NICHT_KORREKT = "Die Attributen sind nicht korrekt.";
	private static final String MSG_ATTRIBUT_IST_NULL = " darf nicht leer sein.";
	private static final String LEERES_JSON_OBJEKT = "{}";
	private static final String FORMAT = "'{' \"isbn\" : {0}, \"titel\" : \"{1}\", \"autor\" : \"{2}\" '}'"; // JSON-Darstehllung des Buches
	
	/**
	 * Default Konstructor, sollte mit fromJSON() verwendet werden.
	 */
	public Buch() { }
	
	/**
	 * Klassischer Konstructor für JUnit tests.
	 * 
	 * @param isbn sollte positiv sein
	 * @param titel darf nicht null sein
	 * @param autor darf nicht null sein
	 * @throws IllegalArgumentException
	 */
	public Buch(int isbn, String titel, String autor) {
		if(titel == null || titel.trim().isEmpty() || autor == null || autor.trim().isEmpty() || isbn <= 0) {
			throw new IllegalArgumentException(MSG_ATTRIBUTS_NICHT_KORREKT);
		}
		
		this.isbn = isbn;
		this.titel = titel;
		this.autor = autor;
	}
	
	/**
	 * Gibt die ISBN des Buches zurueck.
	 * 
	 * @return isbn
	 */
	public int getIsbn(){
		return isbn;
	}
	
	/**
	 * Aendere die ISBN des Buches.
	 * 
	 * @param isbn sollte positiv sein
	 * @throws IllegalArgumentException
	 */
	public void setIsbn(int isbn) {
		if(isbn <= 0) {
			throw new IllegalArgumentException("Die ISBN muss positiv sein.");
		}
		
		this.isbn = isbn;
	}
	
	/**
	 * Gibt den Titel des Buches zurueck.
	 * 
	 * @return titel
	 */
	public String getTitel() {
		return titel;
	}
	
	/**
	 * Aendere der Titel des Buches.
	 * 
	 * @param titel darf nicht null sein
	 * @throws IllegalArgumentException
	 */
	public void setTitel(String titel) {
		if(titel == null || titel.trim().isEmpty()) {
			throw new IllegalArgumentException("Der Titel" + MSG_ATTRIBUT_IST_NULL);
		}
		
		this.titel = titel;
	}
	
	/**
	 * Gibt den Autor des Buches zurueck.
	 * 
	 * @return autor
	 */
	public String getAutor() {
		return autor;
	}
	
	/**
	 * Aendere der Autor des Buches.
	 * 
	 * @param autor darf nicht null sein
	 * @throws IllegalArgumentException
	 */
	public void setAutor(String autor) {
		if(autor == null || autor.trim().isEmpty()) {
			throw new IllegalArgumentException("Der Autor" + MSG_ATTRIBUT_IST_NULL);
		}
		
		this.autor = autor;
	}
	
	/**
	 * Gibt die JSON-Darstellung des Buches zurueck.
	 * 
	 * @return JSON-String
	 * @throws JsonCodecException
	 */
	public String toJSON() throws JsonCodecException{
		if(isbn == 0 && titel == null && autor == null) { // Das buch ist nicht initialisiert
			throw new JsonCodecException("Das Buch ist leer.");
		}
		
		return MessageFormat.format(FORMAT, String.valueOf(isbn), titel, autor);
	}
	
	/**
	 * Aendere die Atributen des Buches mit den eingegebenen JSON-String.
	 * 
	 * @param json darf nicht null oder leer sein
	 * @throws JsonCodecException
	 */
	public void fromJSON(String json) throws JsonCodecException{
		try {
			if(json == null || json.trim().isEmpty() || json.equals(LEERES_JSON_OBJEKT)) {
				throw new JsonCodecException("Der JSON-Objekt ist leer.");
			}
			
			MessageFormat mf = new MessageFormat(FORMAT);
			Object[] params = mf.parse(json);
			
			if(titel != null && autor != null && Integer.parseInt(params[0].toString()) != isbn){
				throw new JsonCodecException("Die ISBN darf nicht geändert werden.");
			}
			
			setIsbn(Integer.parseInt(params[0].toString()));
			setTitel(params[1].toString());
			setAutor(params[2].toString());
			
		} catch (JsonCodecException e) {
			throw new JsonCodecException(e.getMessage());
		} catch(NumberFormatException e) {
			throw new JsonCodecException("Fehler im JSON-String.");
		} catch(IllegalArgumentException e) {
			throw new JsonCodecException(e.getMessage());
		} catch (Exception e) {
			throw new JsonCodecException("Fehler im JSON-String.");
		}
		
	}
	
	/**
	 * Vergleiche zwei Buchern.
	 * 
	 * @param o, muss ein Buch sein
	 * @return Wahr, wenn die Buchern gleich sind, sonst Falsh
	 */
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		
		if(o instanceof Buch) {
			Buch b = (Buch) o;
			
			if(b.getIsbn() == isbn && b.getTitel().equals(titel) && b.getAutor().equals(autor)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gibt das Buch als String zurueck.
	 * 
	 * @return string
	 */
	public String toString() {
		if(isbn == 0 && titel == null && autor == null) { // Das buch ist nicht initialisiert
			return "Das Buch ist leer.";
		}
		
		return "ISBN: " + isbn + ", Titel: " + titel + ", Autor: " + autor;
	}

}
