package app.wolfware.Exceptions;

import java.io.Serial;

public class ZusiEventException extends Exception {

	public ZusiEventException() {
		super();
	}

	public ZusiEventException(String message) {
		super(message);
	}

	public ZusiEventException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZusiEventException(Throwable cause) {
		super(cause);
	}
}
