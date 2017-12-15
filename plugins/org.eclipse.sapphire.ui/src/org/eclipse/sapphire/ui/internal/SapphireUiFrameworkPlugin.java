/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireUiFrameworkPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.sapphire.ui";
    
    private static final ILog platformLog
        = Platform.getLog( Platform.getBundle( PLUGIN_ID ) );
    
    public static Bundle getBundle()
    {
        return Platform.getBundle( PLUGIN_ID );
    }
    
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
            message = MiscUtil.EMPTY_STRING;
        }

        return new Status( IStatus.ERROR, PLUGIN_ID, 0, message, e );
    }

    public static void log( final Throwable e )
    {
        final String msg = e.getMessage() + MiscUtil.EMPTY_STRING;
        log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e ) );
    }

    public static void log( final IStatus status )
    {
        platformLog.log( status );
    }
    
    public static void logError( final String message )
    {
        log( createErrorStatus( message ) );
    }

    public static void logError( final String message,
                                 final Exception e )
    {
        log( createErrorStatus( message, e ) );
    }
    
}
