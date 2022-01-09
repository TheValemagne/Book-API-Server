package Server;

/**
 * Interface für die moeglischen HTTP Status Coden der REST-Api
 * 
 * @author Jeremy Houde
 * @version 1.0
 */
public interface HttpStatus {
	static final int OK                    = 200;
	static final int CREATED               = 201;
	static final int NO_CONTENT            = 204;
	
	static final int BAD_REQUEST           = 400;
	static final int NOT_FOUND             = 404;
	static final int METHOD_NOT_ALLOWED    = 405;
	
	static final int INTERNAL_SERVER_ERROR = 500;
}
