package Datenbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Liste von Buechern fuer eine REST-API.
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
public class BuchList implements JsonCodec{
	private ArrayList<Buch> buchlist;
	private static final String datenbankName = "database.json";
	
	private static final String BEGIN_JSON = "[\n";
	private static final String END_JSON = "\n]";
	private static final String NEW_LINE_JSON = ",\n";
	private static final String LEERE_JSON_LISTE = "[]";
	
	/**
	 * Klassischer Konstructor fuer eine liste von Buechern
	 * 
	 */
	public BuchList() {
		buchlist = new ArrayList<>();
	}
	
	/**
	 * Sortiert mit der ISBN
	 * 
	 */
	private void sort() {
		buchlist.sort((a, b) -> a.getIsbn() - b.getIsbn());
	}
	
	/**
	 * Gibt den Index des eingegenen Buches aus.
	 * 
	 * @param isbn sollte positiv sein
	 * @return index
	 */
	private int getIndex(int isbn) {
		if(isbn <= 0) {
			throw new IllegalArgumentException("Die ISBN muss positiv sein.");
		}
		
		for(int index = 0; index < buchlist.size(); index++) {
			if(buchlist.get(index).getIsbn() == isbn) {
				return index;
			}
		}
		
		return -1;
	}
	
	/**
	 * Einfuegt einen neuen Buch.
	 * 
	 * @param buch sollte nicht null und nicht vorhanden sein
	 */
	public void addBuch(Buch buch) {
		if(buch == null) {
			throw new IllegalArgumentException("Das Buch darf nicht leer sein.");
		}
		
		int index = getIndex(buch.getIsbn());
		
		if(index != -1) {
			throw new IllegalArgumentException("Das Buch mit ISBN " + buch.getIsbn() + " ist schon vorhanden.");
		}
		
		buchlist.add(buch);
		sort(); // Sortiert mit der ISBN
	}
	
	/**
	 * Loesche einen Buch mit Hilfe der ISBN.
	 * 
	 * @param isbn sollte positiv sein und einen Buch referenzieren, das worhanden ist
	 * @throws NotFoundException Das Buch ist nicht vorhanden
	 */
	public void removeBuch(int isbn) throws NotFoundException{
		int index = getIndex(isbn);
		
		if(index == -1) {
			throw new NotFoundException("Kein Buch mit ISBN " + isbn + ".");
		}
		
		buchlist.remove(index);
	}
	
	/**
	 * Aktualisiere das Buch mit den Inhalt des JSON-Strings.
	 * 
	 * @param isbn sollte positiv sein und einen Buch referenzieren, das worhanden ist
	 * @param json darf nicht null oder leer sein
	 * @throws JsonCodecException Fehler im JSON-String
	 * @throws NotFoundException Das Buch ist nicht vorhanden
	 */
	public void updateBuch(int isbn, String json) throws JsonCodecException, NotFoundException{
		Buch buch = getBuch(isbn);
		buch.fromJSON(json);
	}
	
	/**
	 * Gibt das Buch mit der eingegebene ISBN zurueck.
	 * 
	 * @param isbn sollte positiv sein und einen Buch referenzieren, das worhanden ist
	 * @return Buch
	 * @throws NotFoundException Das Buch ist nicht vorhanden
	 */
	public Buch getBuch(int isbn) throws NotFoundException {
		int index = getIndex(isbn);
		
		if(index == -1) {
			throw new NotFoundException("Kein Buch mit ISBN " + isbn + ".");
		}
		
		return buchlist.get(index);
	}
	
	/**
	 * Gibt alle Buchern, die eine bestimmten Eigenschaft haben, zurueck.
	 * 
	 * @param p sollte nicht null sein
	 * @return eine Liste von Buchern, die diese Eigenschaft erfuehlen
	 * @throws IllegalArgumentException Das Suchparameter ist leer
	 */
	public BuchList getBuchList(Predicate<Buch> p){
		if(p == null) {
			throw new IllegalArgumentException("Das Suchparameter ist leer.");
		}
		
		BuchList list = new BuchList();
		
		for(Buch b : buchlist) {
			if(p.test(b)) {
				list.addBuch(b);
			}
		}
		
		return list;
	}
	
	/**
	 * Gibt die JSON-Darstellung der Liste von Buchern zurueck.
	 * 
	 * @return JSON-String
	 * @throws JsonCodecException Fehler im JSON-String
	 */
	public String toJSON() throws JsonCodecException{
		if(buchlist.size() == 0) {
			return LEERE_JSON_LISTE;
		}
		
		StringBuilder result = new StringBuilder().append(BEGIN_JSON);
		
		for(Buch buch: buchlist) {
			result.append("\t").append(buch.toJSON()).append(NEW_LINE_JSON);
		}
		
		result.delete(result.length() - NEW_LINE_JSON.length(), result.length()); // loecht die letzte NEW_LINE_JSON
		
		return result.append(END_JSON).toString();
	}
	
	/**
	 * Einfuegt die definierten Buchern des JSON-String in der Liste.
	 * 
	 * @param json darf nicht null oder leer sein
	 * @throws JsonCodecException Fehler im JSON-String
	 */
	public void fromJSON(String json) throws JsonCodecException {
		if(json == null || json.trim().isEmpty()) {
			throw new JsonCodecException("Die JSON-Liste ist leer");
		}
		
		buchlist.clear(); // Leert die liste
		
		if(json.equals(LEERE_JSON_LISTE)) {
			return;
		}
		
		json = json.substring(BEGIN_JSON.length(), json.length() - END_JSON.length()); // loecht '[' und ']'
		String[] zeilen = json.split("\n");
		
		for(String zeile : zeilen) {
			if(zeile.trim().isEmpty()) { // leere Zeile
				continue;
			}
			
			Buch buch = new Buch();
			buch.fromJSON(zeile.trim());
			
			int index = getIndex(buch.getIsbn());
			
			if(index == -1) { // Der JSON-String könnte zwei Buchern mit derselbe ISBN haben, es ist aber in der liste verbotten
				buchlist.add(buch);
			}
		}
		
		sort(); // Sortiert mit der ISBN
	}
	
	/**
	 * Schreibt den Inhalt der liste in der JSON-Datei.
	 * 
	 * @throws DatabaseException Problem mit der Datenbank
	 */
	public void toFile() throws DatabaseException {
		try {
			String json = toJSON();
			
			File datenbank = new File(datenbankName);
			
			if(datenbank.exists() && (!datenbank.isFile() || !datenbank.canWrite())) {
				throw new DatabaseException("Problem mit der Datenbank.");
			}
			
			FileWriter out = new FileWriter(datenbank);
			out.write(json);
			out.close();
			
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
	
	/**
	 * Gibt den Inhalt der JSON-Datei aus.
	 * 
	 * @return Inhalt im json-format
	 * @throws DatabaseException Problem mit der Datenbank
	 */
	public String fromFile() throws DatabaseException{
		try {
			File datenbank = new File(datenbankName);
			
			if(!datenbank.exists() || !datenbank.isFile() || !datenbank.canRead()) {
				throw new DatabaseException("Problem mit der Datenbank.");
			}
			
			BufferedReader in = new BufferedReader(new FileReader(datenbank));
			StringBuilder content = new StringBuilder();

			String  line;
			
			while( (line = in.readLine()) != null) {
				content.append(line).append("\n");
			}

			in.close();
			
			return content.toString();
			
		} catch (IOException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
	
	
	/**
	 * Gibt die liste als String zurueck.
	 * 
	 * @return string
	 */
	public String toString() {
		if(buchlist.size() == 0) {
			return "Die Buchliste ist leer.";
		}
		
		StringBuilder result = new StringBuilder().append("Buchliste: \n");
		
		for(Buch buch: buchlist) {
			result.append(buch).append("\n");
		}
		
		return result.toString();
	}
	
}
