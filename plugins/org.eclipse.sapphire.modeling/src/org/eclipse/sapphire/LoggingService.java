/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.Service;

/**
 * Used to log messages and exceptions relating to system operation.
 * 
 * @author <a href="konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class LoggingService extends Service
{
    /**
     * Logs an error message.
     * 
     * @param message the message
     */
    
    public final void logError( final String message )
    {
        log( Status.createErrorStatus( message ) );
    }
    
    /**
     * Logs an error message with exception.
     * 
     * @param message the message
     * @param e the exception
     */
    
    public final void logError( final String message, final Throwable e )
    {
        log( Status.createErrorStatus( message, e ) );
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message the message
     */
    
    public final void logWarning( final String message )
    {
        log( Status.createWarningStatus( message ) );
    }
    
    /**
     * Logs an exception.
     * 
     * @param e the exception
     */

    public final void log( final Throwable e )
    {
        log( Status.createErrorStatus( e ) );
    }
    
    /**
     * Logs a status.
     * 
     * @param status the status
     */

    public abstract void log( Status status );

}
