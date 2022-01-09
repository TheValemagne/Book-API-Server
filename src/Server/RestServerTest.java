package Server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.text.ParseException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import Datenbank.Buch;
import Datenbank.BuchList;
import Datenbank.DatabaseException;
import Datenbank.NotFoundException;
import Datenbank.JsonCodec.JsonCodecException;

@TestMethodOrder(OrderAnnotation.class)
class RestServerTest implements HttpStatus {
	private static String old_data;
	private static RestServer server;
	private static BuchList buchliste;
	private static HttpClient client;
	private HttpResponse<String> response;
	
	// Test Parameters
	private static final int port = 4700;
	private static final String path = "/buchliste";
	private static final String uri = "http://localhost:" + port + path;
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	
	// JSON behandlung
	static private final String RESPONSE_FORMAT = "'{'  \"msg\" : {0}  '}'";
	static private final MessageFormat find_response = new MessageFormat(RESPONSE_FORMAT);
	
	@BeforeAll
	static void start_server() throws IOException, JsonCodecException, DatabaseException {
		server = new RestServer(port, path);
		server.start();
		
		buchliste = new BuchList(); // Parallel eine zweite Buchliste, um zu testen
		old_data = buchliste.fromFile(); // Die Datenbank vor den Tests.
		buchliste.fromJSON(old_data);
		
		client = HttpClient.newHttpClient();
	}
	
	@AfterAll
	static void stop_server() throws IOException, JsonCodecException, DatabaseException {
		server.stop();
		
		buchliste.fromJSON(old_data); // Die Datenbank hat wieder ihren uhrspruenglische Zustand
		buchliste.toFile();
		
		buchliste = null;
		client = null;
	}
	
	/**
	 * Sendet einen GET-Request.
	 * 
	 * @param url
	 * @return response
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private HttpResponse<String> send_Get(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
	             						 .uri(URI.create(url))
         						 		 .GET()
         						 		 .build();
		
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}
	
	/**
	 * Sendet einen POST-Request.
	 * 
	 * @param url
	 * @param json
	 * @return response
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private HttpResponse<String> send_Post(String url, String json) throws IOException, InterruptedException{
		HttpRequest request = HttpRequest.newBuilder()
	             						 .uri(URI.create(url))
	             						 .header("Content-Type",  CONTENT_TYPE)
	             						 .POST(HttpRequest.BodyPublishers.ofString(json))
	             						 .build();

		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}
	
	/**
	 * Sendet einen PUT-Request.
	 * 
	 * @param url
	 * @param json
	 * @return response
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private HttpResponse<String> send_Put(String url, String json) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
	             						 .uri(URI.create(url))
	             						 .header("Content-Type",  CONTENT_TYPE)
	             						 .PUT(HttpRequest.BodyPublishers.ofString(json))
	             						 .build();

		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}
	
	/**
	 * Sendet einen DELETE-Request.
	 * 
	 * @param url
	 * @return response
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private HttpResponse<String> send_Delete(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
	             						 .uri(URI.create(url))
	             						 .DELETE()
	             						 .build();

		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}
	
	@Test
	@Order(1)   
	void test_get_buchliste() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {
		buchliste.fromJSON(buchliste.fromFile());
		String data = buchliste.toJSON();
		response = send_Get(uri);
		
		assertEquals(data, find_response.parse(response.body())[0].toString());
		assertEquals(OK, response.statusCode());
	}
	
	@Test
	@Order(2)   
	void test_get_buch_mit_isbn_9() throws IOException, InterruptedException, ParseException, JsonCodecException, NotFoundException {
		response = send_Get(uri +  "/9");
		
		assertEquals(buchliste.getBuch(9).toJSON(), find_response.parse(response.body())[0].toString());
		assertEquals(OK, response.statusCode());
	}
	
	@Test
	@Order(3)   
	void test_get_buch_ungueltiger_isbn() throws IOException, InterruptedException, ParseException, JsonCodecException {
		response = send_Get(uri +  "/0");
		
		assertEquals("\"Die ISBN muss positiv sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(4)   
	void test_get_buch_nicht_vorhanden() throws IOException, InterruptedException, ParseException, JsonCodecException {
		response = send_Get(uri +  "/1");
		
		assertEquals("\"Kein Buch mit ISBN 1.\"", find_response.parse(response.body())[0].toString());
		assertEquals(NOT_FOUND, response.statusCode());
	}
	
	@Test
	@Order(5)   
	void test_get_buchliste_von_Hugh_Howey() throws IOException, InterruptedException, ParseException, JsonCodecException {
		String autor = "Hugh Howey";	
		response = send_Get(uri +  "/" + autor.replace(" ", "_"));
		
		String howeys_bucherliste = buchliste.getBuchList(e -> e.getAutor().equals(autor)).toJSON();
		
		assertEquals(howeys_bucherliste, find_response.parse(response.body())[0].toString());
		assertEquals(OK, response.statusCode());
	}
	
	@Test
	@Order(6)   
	void test_get_buchliste_autor_existiert_nicht() throws IOException, InterruptedException, ParseException, JsonCodecException {
		String autor = "Harlan Coben";
		response = send_Get(uri +  "/" + autor.replace(" ", "_"));
		
		assertEquals("[]", find_response.parse(response.body())[0].toString());
		assertEquals(OK, response.statusCode());
	}
	
	@Test
	@Order(7)   
	void test_get_mit_falsche_url() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {
		response = send_Get(uri + "/endpoint/9"); // die URL /buchliste/endpoint/{isbn} ist ungueltig
		
		assertEquals("\"Keine gueltige URL.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(8)   
	void test_post_mit_falsche_url() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {
		Buch neuer_buch = new Buch(4, "1984", "George Orwell");
		response = send_Post(uri + "/add", neuer_buch.toJSON()); // die URL /buchliste/add ist ungueltig
		
		assertEquals("\"Keine gueltige URL.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(9)   
	void test_post_neuer_buch_mit_isbn_null() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {	
		response = send_Post(uri, "{ \"isbn\" : 0, \"titel\" : \" \", \"autor\" : \"Harla, Coben\" }");
		
		assertEquals("\"Die ISBN muss positiv sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(10)   
	void test_post_neuer_buch_mit_titel_leer() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {		
		response = send_Post(uri, "{ \"isbn\" : 3, \"titel\" : \" \", \"autor\" : \"Harlan Coben\" }");
		
		assertEquals("\"Der Titel darf nicht leer sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(11)   
	void test_post_neuer_buch_mit_autor_leer() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {		
		response = send_Post(uri, "{ \"isbn\" : 3, \"titel\" : \"Balle de match\", \"autor\" : \" \" }");
		
		assertEquals("\"Der Autor darf nicht leer sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(12)   
	void test_post_fehler_in_json_fehlt_isbn() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {	
		response = send_Post(uri, "{ \"titel\" : \"Balle de match\", \"autor\" : \"Harlan Coben\" }"); // Hier fehlt die ISBN
		
		assertEquals("\"Fehler im JSON-String.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(13)   
	void test_post_buch_zwei_mal() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException, NotFoundException{
		Buch neuer_buch = new Buch(400, "1984", "George Orwell");
		test_post_neuer_buch(neuer_buch);
		
		response = send_Post(uri, neuer_buch.toJSON()); // Die ISBN muss in der Liste eindeutig sein.
		
		assertEquals("\"Das Buch mit ISBN " + neuer_buch.getIsbn() + " ist schon vorhanden.\"",  find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
		
		test_delete_buch(neuer_buch);
	}
	
	@Test
	@Order(14)
	void test_put_buch_nicht_vorhanden() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {		
		response = send_Put(uri + "/1", "{ \"titel\" : \"1984\", \"autor\" : \"G. Orwell\" }");
		buchliste.fromJSON(buchliste.fromFile());
		
		assertEquals("\"Kein Buch mit ISBN 1.\"", find_response.parse(response.body())[0].toString());
		assertEquals(NOT_FOUND, response.statusCode());
	}
	
	@Test
	@Order(15)   
	void test_put_mit_falsche_url() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {	
		response = send_Put(uri + "/add/4", "{ \"titel\" : \"1984\", \"autor\" : \"G. Orwell\" }"); // die URL /buchliste/add/{isbn} ist ungueltig
		
		assertEquals("\"Keine gueltige URL.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(16)   
	void test_put_mit_isbn_als_string() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {	
		response = send_Put(uri + "/isbn", "{ \"titel\" : \"1984\", \"autor\" : \"G. Orwell\" }"); // die URL /buchliste/isbn ist ungueltig
		
		assertEquals("\"Keine gueltige URL.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(17)   
	void test_put_buch_ungueltiger_isbn() throws IOException, InterruptedException, ParseException, JsonCodecException {
		response = send_Put(uri + "/0", "{ \"titel\" : \"1984\", \"autor\" : \"G. Orwell\" }");
		
		assertEquals("\"Die ISBN muss positiv sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(18)   
	void test_put_titel_leer() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException, NotFoundException{
		Buch neuer_buch = new Buch(500, "Harricana", "Benard Clavel");
		test_post_neuer_buch(neuer_buch);
	
		response = send_Put(uri + "/" + neuer_buch.getIsbn(), "{ \"titel\" : \" \", \"autor\" : \"B. Clavel\" }");
		
		assertEquals("\"Der Titel darf nicht leer sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
		
		test_delete_buch(neuer_buch);
	}
	
	@Test
	@Order(19)   
	void test_put_autor_leer() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException, NotFoundException{
		Buch neuer_buch = new Buch(450, "1984", "George Orwell");
		test_post_neuer_buch(neuer_buch);
	
		response = send_Put(uri + "/" + neuer_buch.getIsbn(), "{ \"titel\" : \"1984\", \"autor\" : \"   \" }");
		
		assertEquals("\"Der Autor darf nicht leer sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
		
		test_delete_buch(neuer_buch);
	}
	
	@Test
	@Order(20)
	void test_delete_buch_nicht_vorhanden() throws IOException, InterruptedException, ParseException {		
		response = send_Delete(uri + "/1");
		
		assertEquals("\"Kein Buch mit ISBN 1.\"", find_response.parse(response.body())[0].toString());
		assertEquals(NOT_FOUND, response.statusCode());
	}
	
	@Test
	@Order(21)   
	void test_delete_mit_falsche_url() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {	
		response = send_Delete(uri + "/del/4"); // die URL /buchliste/del/{isbn} ist ungueltig
		
		assertEquals("\"Keine gueltige URL.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(22)   
	void test_delete_mit_isbn_als_string() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException {
		response = send_Delete(uri + "/isbn"); // die URL /buchliste/isbn ist ungueltig
		
		assertEquals("\"Keine gueltige URL.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(23)   
	void test_delete_buch_ungueltiger_isbn() throws IOException, InterruptedException, ParseException, JsonCodecException {
		response = send_Delete(uri + "/0");
		
		assertEquals("\"Die ISBN muss positiv sein.\"", find_response.parse(response.body())[0].toString());
		assertEquals(BAD_REQUEST, response.statusCode());
	}
	
	@Test
	@Order(24)
	void test_methode_nicht_implementiert() throws IOException, InterruptedException, ParseException {
		HttpRequest request = HttpRequest.newBuilder()
				             .uri(URI.create(uri + "/1"))
				             .method("COPY", HttpRequest.BodyPublishers.noBody())
				             .build();
		
		response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		assertEquals("\"Die Methode ist nicht implementiert.\"", find_response.parse(response.body())[0].toString()); // Nur GET, POST, PUT und DELETE sind erlaubt.
		assertEquals(METHOD_NOT_ALLOWED, response.statusCode());
	}
	
	@Test
	@Order(25)   
	void test_post_put_delete_buch() throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException, NotFoundException {
		Buch neuer_buch = new Buch(600, "1984", "George Orwell");
		test_post_neuer_buch(neuer_buch);
		test_put_neuer_buch(neuer_buch);
		test_delete_buch(neuer_buch);
	}
	
	void test_post_neuer_buch(Buch neuer_buch) throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException, NotFoundException {
		response = send_Post(uri, neuer_buch.toJSON());
		
		buchliste.fromJSON(buchliste.fromFile());
		
		assertEquals(uri + "/" + neuer_buch.getIsbn(), response.headers().allValues("Location").get(0));
		assertEquals(CREATED, response.statusCode());
		assertEquals(neuer_buch, buchliste.getBuch(neuer_buch.getIsbn()));
	}
	
	void test_put_neuer_buch(Buch neuer_buch) throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException, NotFoundException {
		Buch b2 = new Buch(neuer_buch.getIsbn(), neuer_buch.getTitel(), "G. Orwell");
		
		response = send_Put(uri + "/" + neuer_buch.getIsbn(), "{ \"titel\" : \"1984\", \"autor\" : \"G. Orwell\" }");
		buchliste.fromJSON(buchliste.fromFile());
		
		assertEquals(b2, buchliste.getBuch(neuer_buch.getIsbn()));
		assertEquals(NO_CONTENT, response.statusCode());
	}
	
	void test_delete_buch(Buch neuer_buch) throws IOException, InterruptedException, ParseException, JsonCodecException, DatabaseException, NotFoundException {
		response = send_Delete(uri + "/" + neuer_buch.getIsbn());
		
		buchliste.removeBuch(neuer_buch.getIsbn());
		
		assertEquals(NO_CONTENT, response.statusCode());
	}

}
