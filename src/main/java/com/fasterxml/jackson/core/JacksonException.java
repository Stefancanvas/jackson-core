package com.fasterxml.jackson.core;

/**
 * Base class for all Jackson-produced checked exceptions.
 */
public abstract class JacksonException extends java.io.IOException
{
    private final static long serialVersionUID = 123; // eclipse complains otherwise

    protected JsonLocation _location;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected JacksonException(String msg) {
        super(msg);
    }

    protected JacksonException(Throwable t) {
        super(t);
    }

    protected JacksonException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg, rootCause);
        _location = (loc == null) ? JsonLocation.NA : loc;
    }

    /**
     * Method that allows to remove context information from this exception's message.
     * Useful when you are parsing security-sensitive data and don't want original data excerpts
     * to be present in Jackson parser error messages.
     */
    public JacksonException clearLocation() {
        _location = null;
        return this;
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * Accessor for location information related to position within input
     * or output (depending on operation), if available; if not available
     * may return {@link JsonLocation#NA} (but never {@code null}).
     *<p>
     * Accuracy of location information depends on backend (format) as well
     * as (in some cases) operation being performed.
     */
    public JsonLocation getLocation() { return _location; }

    /**
     * Method that allows accessing the original "message" argument,
     * without additional decorations (like location information)
     * that overridden {@link #getMessage} adds.
     */
    public String getOriginalMessage() { return super.getMessage(); }

    /**
     * Method that allows accessing underlying processor that triggered
     * this exception; typically either {@link JsonParser} or {@link JsonGenerator}
     * for exceptions that originate from streaming API, but other abstractions
     * when thrown by databinding.
     *<p>
     * Note that it is possible that `null` may be returned if code throwing
     * exception either has no access to processor; or has not been retrofitted
     * to set it; this means that caller needs to take care to check for nulls.
     * Subtypes override this method with co-variant return type, for more
     * type-safe access.
     * 
     * @return Originating processor, if available; null if not.
     */
    public Object getProcessor() { return null; }

    /*
    /**********************************************************************
    /* Methods for sub-classes to use, override
    /**********************************************************************
     */
    
    /**
     * Accessor that sub-classes can override to append additional
     * information right after the main message, but before
     * source location information.
     */
    protected String getMessageSuffix() { return null; }

    /*
    /**********************************************************************
    /* Overrides of standard methods
    /**********************************************************************
     */
    
    /**
     * Default method overridden so that we can add location information
     */
    @Override public String getMessage() {
        String msg = super.getMessage();
        if (msg == null) {
            msg = "N/A";
        }
        JsonLocation loc = getLocation();
        String suffix = getMessageSuffix();
        // mild optimization, if nothing extra is needed:
        if ((loc != null) || suffix != null) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(msg);
            if (suffix != null) {
                sb.append(suffix);
            }
            if (loc != null) {
                sb.append('\n');
                sb.append(" at ");
                sb = loc.toString(sb);
            }
            msg = sb.toString();
        }
        return msg;
    }

    @Override public String toString() { return getClass().getName()+": "+getMessage(); }
}
