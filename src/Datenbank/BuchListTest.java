package Datenbank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Datenbank.JsonCodec.JsonCodecException;

/**
 * Testklasse fuer die Datenklasse BuchList.
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
class BuchListTest {
	private Buch b1;
	private Buch b2;
	private BuchList list;
	
	@BeforeEach
	void beforeEachTestCase() {
		b1 = new Buch(12, "Silo", "Hugh Howey");
		b2 = new Buch(65, "Dune","Frank Herbert");
		list = new BuchList();
	}
	
	@AfterEach
	void afterEachTestCase() {
		b1 = null;
		b2 = null;
		list = null;
	}
	
	@Test
	void test_AddBuch() throws NotFoundException {
		list.addBuch(b1);
		assertEquals(b1, list.getBuch(b1.getIsbn()));
	}
	
	/**
	 * Man darf nicht zwei Buchern mit derselben ISBN legen. Die ISBN muss eindeutig sein.
	 */
	@Test
	void test_AddBuch_schon_vorhanden() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			list.addBuch(b1);
			list.addBuch(b1);
		});

		Assertions.assertEquals("Das Buch mit ISBN " + b1.getIsbn() + " ist schon vorhanden.", exception.getMessage());
	}
	
	@Test
	void test_AddBuch_buch_ist_null() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			list.addBuch(null);
		});

		Assertions.assertEquals("Das Buch darf nicht leer sein.", exception.getMessage());
	}
	
	@Test
	void test_RemoveBuch_buch_vorhanden() throws NotFoundException {
		list.addBuch(b1);
		list.removeBuch(b1.getIsbn());
		
		NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> { // Wurde das Buch geloescht?
			list.getBuch(b1.getIsbn());
		});

		Assertions.assertEquals("Kein Buch mit ISBN " + b1.getIsbn() + ".", exception.getMessage());
	}
	
	/**
	 * Die Buchliste ist leer. Es gibt somit keinen Buch mit die gegebene ISBN.
	 */
	@Test
	void test_RemoveBuch_buch_nicht_vorhanden() {
		NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> {
			list.removeBuch(b1.getIsbn());
		});

		Assertions.assertEquals("Kein Buch mit ISBN " + b1.getIsbn() + ".", exception.getMessage());
	}
	
	@Test
	void test_UpdateBuch_buch_vorhanden() throws JsonCodecException, NotFoundException {
		list.addBuch(b1);
		Buch b3 = new Buch(12, "Silo 1", "Sir Hugh Howey");
		list.updateBuch(b1.getIsbn(), b3.toJSON());
		
		assertEquals(b3, list.getBuch(b1.getIsbn()));
	}
	
	@Test
	void test_UpdateBuch_buch_nicht_vorhanden() {
		NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> {
			Buch b3 = new Buch(12, "Silo 1", "Sir Hugh Howey");
			list.updateBuch(b1.getIsbn(), b3.toJSON());
		});

		Assertions.assertEquals("Kein Buch mit ISBN " + b1.getIsbn() + ".", exception.getMessage());
	}
	
	@Test
	void test_UpdateBuch_buch_mit_isbn_null() {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			list.addBuch(b1);
			String json = "{ \"isbn\" : 0, \"titel\" : \"Silo\", \"autor\" : \"Hugh Howey\" }";
			list.updateBuch(b1.getIsbn(), json);
		});

		Assertions.assertEquals("Die ISBN darf nicht geändert werden.", exception.getMessage());
	}
	
	@Test
	void test_UpdateBuch_buch_mit_titel_leer() {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			list.addBuch(b1);
			String json = "{ \"isbn\" : 12, \"titel\" : \"\", \"autor\" : \"Hugh Howey\" }";
			list.updateBuch(b1.getIsbn(), json);
		});

		Assertions.assertEquals("Der Titel darf nicht leer sein.", exception.getMessage());
	}
	
	@Test
	void test_UpdateBuch_buch_mit_autor_leer() {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			list.addBuch(b1);
			String json = "{ \"isbn\" : 12, \"titel\" : \"Silo\", \"autor\" : \"   \" }";
			list.updateBuch(b1.getIsbn(), json);
		});

		Assertions.assertEquals("Der Autor darf nicht leer sein.", exception.getMessage());
	}
	
	@Test
	void test_UpdateBuch_buch_mit_parameter_date() {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			list.addBuch(b1);
			String json = "{ \"isbn\" : 12, \"titel\" : \"Silo\", \"autor\" : \"   \", \"date\" : 2011 }"; // Extra-Parametern wie date sind verbotten.
			list.updateBuch(b1.getIsbn(), json);
		});

		Assertions.assertEquals("Fehler im JSON-String.", exception.getMessage());
	}
	
	@Test
	void test_UpdateBuch_buch_ohne_autor() {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			list.addBuch(b1);
			String json = "{ \"isbn\" : 12, \"titel\" : \"Silo\" }"; // Hier fehlt den Autor
			list.updateBuch(b1.getIsbn(), json);
		});

		Assertions.assertEquals("Fehler im JSON-String.", exception.getMessage());
	}
	
	@Test
	void test_getBuch_buch_vorhanden() throws NotFoundException {
		list.addBuch(b1);
		list.addBuch(b2);
		
		assertEquals(b1, list.getBuch(b1.getIsbn()));
		assertEquals(b2, list.getBuch(b2.getIsbn()));
	}
	
	@Test
	void test_getBuch_buch_nicht_vorhanden() {
		NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> {
			list.getBuch(b1.getIsbn());
		});

		Assertions.assertEquals("Kein Buch mit ISBN " + b1.getIsbn() + ".", exception.getMessage());
	}
	
	@Test
	void test_getBuchList() throws NotFoundException {
		list.addBuch(b1);
		list.addBuch(b2);
		
		BuchList new_list = list.getBuchList(b -> b.getAutor().equals(b1.getAutor()));
		
		assertEquals(b1, new_list.getBuch(b1.getIsbn()));
	}

	@Test
	void test_getBuchList_predicate_ist_null() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			list.getBuchList(null);
		});

		Assertions.assertEquals("Das Suchparameter ist leer.", exception.getMessage());
	}
	
	@Test
	void test_toJSON_mit_zwei_buchern() throws JsonCodecException {
		String expected_json = "[\n\t" + b1.toJSON() + ",\n\t" + b2.toJSON() + "\n]";
		list.addBuch(b1);
		list.addBuch(b2);
		
		String json = list.toJSON();
		assertEquals(expected_json, json);
	}
	
	@Test
	void test_toJSON_liste_leer() throws JsonCodecException {
		String expected_json = "[]";
		
		String json = list.toJSON();
		assertEquals(expected_json, json);
	}
	
	@Test
	void test_toJSON_clear_list() throws JsonCodecException {
		String expected_json = "[]";
		list.addBuch(b1);
		list.addBuch(b2);
		list.fromJSON(expected_json);
		String json = list.toJSON();
		assertEquals(expected_json, json);
	}
	
	@Test
	void test_toJSON_liste_leer_nach_delete() throws JsonCodecException, NotFoundException {
		String expected_json = "[]";
		list.addBuch(b1);
		list.removeBuch(b1.getIsbn());
		String json = list.toJSON();
		assertEquals(expected_json, json);
	}
	
	@Test
	void test_fromJSON_mit_zwei_buchern() throws JsonCodecException, NotFoundException {
		String json = "[\n\t" + b1.toJSON() + ",\n\t" + b2.toJSON() + "\n]";
		list.fromJSON(json);
		
		assertEquals(b1, list.getBuch(b1.getIsbn()));
		assertEquals(b2, list.getBuch(b2.getIsbn()));
	}
	
	@Test
	void test_fromJSON_liste_leer() throws JsonCodecException {
		String json = "[]";
		list.fromJSON(json);
		
		Assertions.assertEquals(json, list.toJSON());
	}
	
	@Test
	void test_fromJSON_json_empty() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			String json = "";
			list.fromJSON(json);
		});
		
		Assertions.assertEquals("Die JSON-Liste ist leer", exception.getMessage());
	}
	
	@Test
	void test_toString_zwei_buchern() {
		String expected_output = "Buchliste: \n" + b1.toString() + "\n" + b2.toString() + "\n";
		list.addBuch(b1);
		list.addBuch(b2);
		
		assertEquals(expected_output, list.toString());	
	}
	
	@Test
	void test_ToString_buch_leer() {	
		assertEquals("Die Buchliste ist leer.", list.toString());
	}
}
