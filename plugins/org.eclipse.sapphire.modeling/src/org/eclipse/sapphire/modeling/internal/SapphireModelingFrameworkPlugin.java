/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireModelingFrameworkPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.sapphire.modeling"; //$NON-NLS-1$

    private static final ILog platformLog
        = Platform.getLog( Platform.getBundle( PLUGIN_ID ) );

    public static IStatus createErrorStatus( final String msg )
    {
        return createErrorStatus( msg, null );
    }

    public static IStatus createErrorStatus( final Exception e )
    {
        return createErrorStatus( e.getMessage(), e );
    }

    public static IStatus createErrorStatus( final String msg,
                                             final Exception e )
    {
        String message = msg;

        if( message == null )
        {
            message = ""; //$NON-NLS-1$
        }

        return new Status( IStatus.ERROR, PLUGIN_ID, 0, message, e );
    }

    public static IStatus createWarningStatus( final String msg )
    {
        return createWarningStatus( msg, null );
    }

    public static IStatus createWarningStatus( final Exception e )
    {
        return createWarningStatus( e.getMessage(), e );
    }

    public static IStatus createWarningStatus( final String msg,
                                               final Exception e )
    {
        String message = msg;

        if( message == null )
        {
            message = ""; //$NON-NLS-1$
        }

        return new Status( IStatus.WARNING, PLUGIN_ID, 0, message, e );
    }

    public static void log( final Exception e )
    {
        final String msg = e.getMessage() + ""; //$NON-NLS-1$
        log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e ) );
    }

    public static void log( final IStatus status )
    {
        platformLog.log( status );
    }
    
    public static void logError( final String message,
                                 final Exception e )
    {
        log( createErrorStatus( message, e ) );
    }

    public static void logWarning( final String message,
                                   final Exception e )
    {
        log( createWarningStatus( message, e ) );
    }

}
