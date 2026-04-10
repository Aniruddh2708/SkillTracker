package service;

/**
 * Domain-specific exception used by authentication workflows.
 *
 * Why custom exception:
 * - Provides stable error contract to UI and tests.
 * - Wraps lower-level causes (e.g., SQL issues) without leaking implementation
 *   details into presentation code.
 */
public class AuthException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
