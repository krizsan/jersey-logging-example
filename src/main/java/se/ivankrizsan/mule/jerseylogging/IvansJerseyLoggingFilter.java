package se.ivankrizsan.mule.jerseylogging;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Jersey logging filter that logs requests and responses to/from Jersey REST services.
 * Request and response payloads are also logged up to a configurable maximum size.
 * Logging is enabled and disabled by setting the log level for this class:
 * Log level DEBUG and below enable logging while a higher log level than DEBUG disable logging.
 *
 * This class implements the Jersey extension interfaces {@code ContainerRequestFilter} and
 * {@code ContainerResponseFilter} that allows us to gain access to requests before they arrive
 * at resources and responses before they are delivered to the clients.
 *
 * @author Ivan Krizsan
 */
@Provider
public class IvansJerseyLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    /* Constant(s): */
    private static final Logger LOGGER = LoggerFactory.getLogger(IvansJerseyLoggingFilter.class);
    /** Maximum size of request/response body in bytes that will be logged. */
    protected static final int DEFAULT_MAX_BODY_SIZE = 20 * 1024;

    /* Instance variable(s): */
    protected int mMaxBodySize = DEFAULT_MAX_BODY_SIZE;

    @Override
    public void filter(final ContainerRequestContext inRequestContext) throws IOException {
        /* Do nothing if logging not enabled on debug level. */
        if (!isLoggingEnabled()) {
            return;
        }

        final StringBuilder theLogMessageBuilder = new StringBuilder();
        appendRequestInfoToMessageBuilder(inRequestContext, theLogMessageBuilder);

        /* Log message body only if there is one. */
        if (!isRequestBodyless(inRequestContext) && inRequestContext.hasEntity()) {
            theLogMessageBuilder.append("\nMessage body: \n");
            inRequestContext.setEntityStream(logMessageBodyInputStream(theLogMessageBuilder,
                inRequestContext.getEntityStream()));
        }

        logMessage(theLogMessageBuilder.toString());
    }

    @Override
    public void filter(final ContainerRequestContext inRequestContext, final ContainerResponseContext inResponseContext)
        throws IOException {
        /* Do nothing if logging not enabled on debug level. */
        if (!isLoggingEnabled()) {
            return;
        }

        final StringBuilder theLogMessageBuilder = new StringBuilder();
        appendResponseInfoToMessageBuilder(inResponseContext, inRequestContext, theLogMessageBuilder);

        logMessage(theLogMessageBuilder.toString());
    }

    /**
     * Determines if the request having the supplied request context has a body or not.
     *
     * @param inRequestContext Request context for request.
     * @return True if request has a body, false otherwise.
     */
    protected boolean isRequestBodyless(ContainerRequestContext inRequestContext) {
        final String theRequestHttpMethod = inRequestContext.getMethod();
        final boolean theBodylessFlag = ("GET".equals(theRequestHttpMethod))
                                        || ("DELETE".equals(theRequestHttpMethod))
                                        || ("HEAD".equals(theRequestHttpMethod));

        return theBodylessFlag;
    }

    /**
     * Appends information taken from the supplied request and response contexts to the supplied string builder, as
     * to create a log message.
     *
     * @param inResponseContext Response context to take information from.
     * @param inRequestContext Request context to take information from.
     * @param inLogMessageBuilder String builder used to create the log message.
     */
    protected void appendResponseInfoToMessageBuilder(final ContainerResponseContext inResponseContext,
        final ContainerRequestContext inRequestContext, final StringBuilder inLogMessageBuilder) {
        final String theMediaType = ObjectUtils.toString(inResponseContext.getMediaType(), "n/a");
        final String theHeaders = ObjectUtils.toString(inResponseContext.getHeaders(), "n/a");
        final String theStatus = ObjectUtils.toString(inResponseContext.getStatusInfo(), "n/a")
            + " (" + inResponseContext.getStatus() + ")";
        final String theRequestUri = requestUriFromRequestContext(inRequestContext);

        inLogMessageBuilder.append("Response from location: ");
        inLogMessageBuilder.append(theRequestUri);
        inLogMessageBuilder.append(", media type: ");
        inLogMessageBuilder.append(theMediaType);
        inLogMessageBuilder.append(", status: ");
        inLogMessageBuilder.append(theStatus);
        inLogMessageBuilder.append("\nHeaders: ");
        inLogMessageBuilder.append(theHeaders);
    }

    /**
     * Appends information taken from the supplied request context to the supplied string builder, as
     * to create a log message.
     *
     * @param inRequestContext Request context to take information from.
     * @param inLogMessageBuilder String builder used to create the log message.
     */
    protected void appendRequestInfoToMessageBuilder(final ContainerRequestContext inRequestContext,
                                                     final StringBuilder inLogMessageBuilder) {
        final String theRequestMethod = ObjectUtils.toString(inRequestContext.getMethod(), "n/a");
        final String theMediaType = ObjectUtils.toString(inRequestContext.getMediaType(), "n/a");
        final String theHeaders = ObjectUtils.toString(inRequestContext.getHeaders(), "n/a");
        final String theRequestUri = requestUriFromRequestContext(inRequestContext);

        inLogMessageBuilder.append("Received ");
        inLogMessageBuilder.append(theRequestMethod);
        inLogMessageBuilder.append(" request to URL ");
        inLogMessageBuilder.append(theRequestUri);
        inLogMessageBuilder.append(" with contents of type ");
        inLogMessageBuilder.append(theMediaType);
        inLogMessageBuilder.append(". \nHTTP headers: ");
        inLogMessageBuilder.append(theHeaders);
    }

    /**
     * Retrieves the absolute path request URI from the supplied request context.
     *
     * @param inRequestContext Request context from which to retrieve URI.
     * @return Request URI string or "n/a" if it is not available.
     */
    protected String requestUriFromRequestContext(final ContainerRequestContext inRequestContext) {
        final UriInfo theRequestUriInfo = inRequestContext.getUriInfo();
        String theRequestUrl = "n/a";
        if (theRequestUriInfo != null) {
            theRequestUrl = ObjectUtils.toString(theRequestUriInfo.getAbsolutePath(), "n/a");
        }
        return theRequestUrl;
    }

    /**
     * Logs the contents of the supplied input stream to the supplied string builder, as to produce
     * a log message.
     * If the length of the data in the input stream exceeds the maximum body size configured on the
     * logging filter then the exceeding data is not logged.
     *
     * @param inLogMessageStringBuilder String builder used to create log message.
     * @param inBodyInputStream Input stream containing message body.
     * @return An input stream from which the message body can be read from.
     * Will be the original input stream if it supports mark and reset.
     * @throws IOException If error occurs reading from input stream.
     */
    protected InputStream logMessageBodyInputStream(final StringBuilder inLogMessageStringBuilder,
        final InputStream inBodyInputStream) throws IOException {
        InputStream theResultEntityStream = inBodyInputStream;

        if (!inBodyInputStream.markSupported()) {
            theResultEntityStream = new BufferedInputStream(inBodyInputStream);
        }
        theResultEntityStream.mark(mMaxBodySize + 1);
        final byte[] theEntityBytes = new byte[mMaxBodySize + 1];
        final int theEntitySize = theResultEntityStream.read(theEntityBytes);
        inLogMessageStringBuilder.append(new String(theEntityBytes, 0, Math.min(theEntitySize, mMaxBodySize)));
        if (theEntitySize > mMaxBodySize) {
            inLogMessageStringBuilder.append(" [additional data truncated]");
        }
        theResultEntityStream.reset();
        return theResultEntityStream;
    }

    /**
     * Determines whether logging is enabled or not.
     * This method should be overridden by subclasses that wish to use some other way of logging than Log4J.
     *
     * @return True if this logging filter is to log requests and responses, false otherwise.
     */
    protected boolean isLoggingEnabled() {
        return LOGGER.isDebugEnabled();
    }

    /**
     * Logs the supplied message.
     * This method should be overridden by subclasses that wish to use some other way of logging than Log4J.
     *
     * @param inMessageToLog Message to log.
     */
    protected void logMessage(final String inMessageToLog) {
        LOGGER.debug(inMessageToLog);
    }
}
