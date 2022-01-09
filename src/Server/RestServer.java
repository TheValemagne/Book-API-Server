package Server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import Datenbank.DatabaseException;
import Datenbank.JsonCodec.JsonCodecException;

/**
 * RestServer fuer den "Control continu 2" mit eine Buchliste
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
public class RestServer {
	private HttpServer server;
	private int port;
	private String path;
	
	/**
	 * Server Konstructor
	 * 
	 * @param port, , muss positiv sein
	 * @param path, darf nicht leer sein
	 * @throws IOException
	 */
	public RestServer(int port, String path) throws IOException{ 
		if(port <= 0) {
			throw new IllegalArgumentException("Ungueltiger Port.");
		}
		
		if(path.trim().isEmpty()) {
			throw new IllegalArgumentException("Der Kontext darf nicht leer sein.");
		}
		
		this.port = port;
		this.path = path;
		server = HttpServer.create(new InetSocketAddress(this.port), 0);
	}
	
	/**
	 * Starte den Server
	 * 
	 * @throws DatabaseException Problem mit der Datenbank
	 * @throws JsonCodecException Fehler im JSON-String 
	 */
	public void start() throws DatabaseException, JsonCodecException {
		HttpContext context = server.createContext(path);
		context.setHandler(new Handler(port, path)); 

		server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		server.start();
	}
	
	/**
	 * Stopt den Server.
	 * 
	 */
	public void stop() {
		server.stop(0);
	}

}
