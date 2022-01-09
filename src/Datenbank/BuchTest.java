package Datenbank;

/**
 * Testklasse fuer die Datenklasse Buch.
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Datenbank.JsonCodec.JsonCodecException;

class BuchTest {
	private Buch buch;
	
	@BeforeEach
	void beforeEachTestCase() {
		buch = new Buch(12, "Silo", "Hugh Howey");
	}
	
	@AfterEach
	void afterEachTestCase() {
		buch = null;
	}
	
	/**
	 *  Erstellt einen neuen Buch und benutze die fromJSON-Methode.
	 *  
	 * @param json
	 * @return Buch
	 * @throws JsonCodecException
	 */
	private Buch buch_fromJson(String json) throws JsonCodecException {
		Buch buch = new Buch();
		buch.fromJSON(json);
		
		return buch;
	}
	
	@Test
	void test_GetIsbn() {
		assertEquals(12, buch.getIsbn());
	}
	
	@Test
	void test_GetTitel() {
		assertEquals("Silo", buch.getTitel());
	}
	
	@Test
	void test_GetAutor() {
		assertEquals("Hugh Howey", buch.getAutor());
	}
	
	@Test
	void test_SetIsbn() {
		final int isbn = 432;
		buch.setIsbn(isbn);
		assertEquals(isbn, buch.getIsbn());
	}
	
	/**
	 * Die ISBN sollte positiv sein.
	 */
	@Test
	void test_Konstruktor_exception_erwartet() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new Buch(0, "titel", "autor"); // die ISBN muss positiv sein.
		});

		Assertions.assertEquals("Die Attributen sind nicht korrekt.", exception.getMessage());
	}
	
	@Test
	void test_SetIsbn_exception_erwartet() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			buch.setIsbn(0);
		});

		Assertions.assertEquals("Die ISBN muss positiv sein.", exception.getMessage());
	}
	
	@Test
	void test_SetTitel() {
		final String titel = "Silo Teil 1";
		buch.setTitel(titel);
		assertEquals(titel, buch.getTitel());
	}
	
	@Test
	void test_SetTitel_exception_erwartet() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			buch.setTitel("    ");
		});

		Assertions.assertEquals("Der Titel darf nicht leer sein.", exception.getMessage());
	}
	
	@Test
	void test_SetAutor() {
		final String autor = "H. Howey";
		buch.setAutor(autor);
		assertEquals(autor, buch.getAutor());
	}
	
	@Test
	void test_SetAutor_exception_erwartet() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			buch.setAutor("");
		});

		Assertions.assertEquals("Der Autor darf nicht leer sein.", exception.getMessage());
	}
	
	@Test
	void test_ToJSON() throws JsonCodecException {
		final String json = "{ \"isbn\" : 12, \"titel\" : \"Silo\", \"autor\" : \"Hugh Howey\" }";
		assertEquals(json, buch.toJSON());
	}
	
	@Test
	void test_ToJSON_buch_leer() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			Buch b = new Buch();
			b.toJSON();
		});
		
		Assertions.assertEquals("Das Buch ist leer.", exception.getMessage());
	}
	
	@Test
	void test_FromJSON() throws JsonCodecException {
		Buch b2 = buch_fromJson("{ \"isbn\" : 12, \"titel\" : \"Silo\", \"autor\" : \"Hugh Howey\" }");
		assertEquals(buch, b2);
	}
	
	@Test
	void test_FromJSON_string_leer() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch.fromJSON("");
		});
		
		Assertions.assertEquals("Der JSON-Objekt ist leer.", exception.getMessage());
	}
	
	@Test
	void test_FromJSON_string_leer_space() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch.fromJSON("   ");
		});
		
		Assertions.assertEquals("Der JSON-Objekt ist leer.", exception.getMessage());
	}
	
	/**
	 * Der JSON-String ist ein leeres Objekt.
	 * 
	 * @throws JsonCodecException
	 */
	@Test
	void test_FromJSON_json_leer() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch.fromJSON("{}");
		});
		
		Assertions.assertEquals("Der JSON-Objekt ist leer.", exception.getMessage());
	}
	
	@Test
	void test_FromJSON_isbn_null() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch_fromJson("{ \"isbn\" : 0, \"titel\" : \"Silo\", \"autor\" : \"Hugh Howey\" }");
		});
		
		Assertions.assertEquals("Die ISBN muss positiv sein.", exception.getMessage());
	}
	
	@Test
	void test_FromJSON_titel_ist_leer() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch_fromJson("{ \"isbn\" : 12, \"titel\" : \"\", \"autor\" : \"Hugh Howey\" }");
		});
		
		Assertions.assertEquals("Der Titel darf nicht leer sein.", exception.getMessage());
	}
	
	@Test
	void test_FromJSON_autor_ist_leer() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch_fromJson("{ \"isbn\" : 12, \"titel\" : \"Silo\", \"autor\" : \"  \" }");
		});
		
		Assertions.assertEquals("Der Autor darf nicht leer sein.", exception.getMessage());
	}
	
	/**
	 * Es fehlt hier den Attribut autor.
	 * 
	 * @throws JsonCodecException
	 */
	@Test
	void test_FromJSON_falsche_formatierten_json_fehlt_autor() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch_fromJson("{ \"isbn\" : 12, \"titel\" : \"Silo\" }"); // Hier fehlt den Autor
		});
		
		Assertions.assertEquals("Fehler im JSON-String.", exception.getMessage());
	}
	
	/**
	 * Die ISBN sollte einen Integer sein.
	 * 
	 * @throws JsonCodecException
	 */
	@Test
	void test_FromJSON_falsche_formatierten_json_isbn_ist_ein_string() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch_fromJson("{ \"isbn\" : \"12\", \"titel\" : \"Silo\", \"autor\" : \"Hugh Howey\" }"); // Die ISBN muss eine Nummer sein.
		});
		
		Assertions.assertEquals("Fehler im JSON-String.", exception.getMessage());
	}
	
	/**
	 * Keinen Attribut date ist erwartet.
	 * 
	 * @throws JsonCodecException
	 */
	@Test
	void test_FromJSON_falsche_formatierten_json_vierten_argument() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch_fromJson("{ \"isbn\" : 12, \"titel\" : \"Silo\", \"autor\" : \"Hugh Howey\", \"date\" : 2011 }"); // Extra-Parametern wie date sind verboten
		});
		
		Assertions.assertEquals("Fehler im JSON-String.", exception.getMessage());
	}
	
	/**
	 * In JSON sind Commas am Ende verbotten.
	 * 
	 * @throws JsonCodecException
	 */
	@Test
	void test_FromJSON_falsche_formatierten_json_extra_comma() throws JsonCodecException {
		JsonCodecException exception = Assertions.assertThrows(JsonCodecException.class, () -> {
			buch_fromJson("{ \"isbn\" : 12, \"titel\" : \"Silo\", \"autor\" : \"Hugh Howey\", }"); // Extra-Kommas sind im JSON verbotten, in JS aber erlaubt
		});
		
		Assertions.assertEquals("Fehler im JSON-String.", exception.getMessage());
	}
	
	@Test
	void test_Equals_erwartet_true() {
		Buch b2 = new Buch(12, "Silo", "Hugh Howey");
		Boolean equals = buch.equals(b2);
		assertEquals(true, equals);
	}
	
	@Test
	void test_Equals_erwartet_false() {
		Buch b2 = new Buch(123, "Silo", "Hugh Howey");
		Boolean equals = buch.equals(b2);
		assertEquals(false, equals);
	}
	
	@Test
	void test_Equals_mit_int_erwartet_false() {
		int b2 = 12;
		Boolean equals = buch.equals((Object) b2);
		assertEquals(false, equals);
	}
	
	@Test
	void test_ToString() {
		final String output = "ISBN: 12, Titel: Silo, Autor: Hugh Howey";
		assertEquals(output, buch.toString());
	}
	
	@Test
	void test_ToString_buch_leer() {
		Buch b2 = new Buch();
		
		assertEquals("Das Buch ist leer.", b2.toString());
	}

}
