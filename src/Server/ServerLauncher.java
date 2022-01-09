package Server;

import java.io.IOException;
import java.util.Scanner;

import Datenbank.DatabaseException;
import Datenbank.JsonCodec.JsonCodecException;

/**
 * Kontroliert den RestServer
 * 
 * @author Jeremy houde
 * @version 1.0
 */
public class ServerLauncher {
	private static RestServer server;
	private static int port = 4711;
	private static String path = "/buchliste";
	
	/**
	 * Starte den RestServer
	 * 
	 * @throws IOException
	 * @throws DatabaseException Problem mit der Datenbank
	 * @throws JsonCodecException Fehler im JSON-String
	 */
	private void start() throws IOException, DatabaseException, JsonCodecException {
		server = new RestServer(port, path);
		server.start();
		System.out.println("Web-Server auf Port " + port + " gestartet.");
		System.out.println("Rufe Web-Server im Web-Browser auf mit http://localhost:" + port + path);
	}
	
	/**
	 * Stopt den Server nach eingabe des Benutzers
	 * 
	 */
	private void stop() {
		System.out.println("Stoppe Web-Server durch irgendeine Eingabe");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		server.stop();
		System.out.println("Web-Server gestoppt.");
	}
	
	/**
	 * Startet einen RestServer mit Consolensteuerung
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ServerLauncher launcher = new ServerLauncher();
			launcher.start();
			launcher.stop();
		} catch (Exception e) {
			System.out.println("Server exception: " + e.getMessage());
		}
	}

}
