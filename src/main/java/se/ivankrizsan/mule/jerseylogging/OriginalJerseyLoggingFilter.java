package se.ivankrizsan.mule.jerseylogging;

import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.ext.Provider;

/**
 * Class that makes it possible to use the Jersey {@code LoggingFilter} by placing this class in
 * in the application package specified by <jersey:package> in the Mule configuration file.
 *
 * @author Ivan Krizsan
 */
@Provider
public class OriginalJerseyLoggingFilter extends LoggingFilter {
}
