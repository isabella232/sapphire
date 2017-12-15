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

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LoggingService
{
    public static void log( final Exception e )
    {
        log( Status.createErrorStatus( e ) );
    }

    public static void log( final Status status )
    {
        System.err.println( status );
    }

}
