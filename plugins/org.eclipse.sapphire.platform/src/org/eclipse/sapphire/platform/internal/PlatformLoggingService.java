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

package org.eclipse.sapphire.platform.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.platform.StatusBridge;

/**
 * Implementation of LoggingService that writes to Eclipse platform log. 
 * 
 * @author <a href="konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PlatformLoggingService extends LoggingService
{
    private ILog log;
    
    @Override
    public void log( final Status status )
    {
        synchronized( this )
        {
            if( this.log == null )
            {
                this.log = Platform.getLog( Platform.getBundle( "org.eclipse.sapphire.platform" ) );
            }
        }
        
        this.log.log( StatusBridge.create( status ) );
    }

}
