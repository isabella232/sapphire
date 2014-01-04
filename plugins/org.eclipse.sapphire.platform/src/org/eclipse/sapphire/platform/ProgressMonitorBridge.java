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

package org.eclipse.sapphire.platform;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.sapphire.modeling.ProgressMonitor;

/**
 * Bridges between Sapphire and Eclipse progress monitor API.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ProgressMonitorBridge
{
    private ProgressMonitorBridge()
    {
        // This class is not meant to be instantiated.
    }
    
    public static IProgressMonitor create( final ProgressMonitor monitor )
    {
        return new IProgressMonitor()
        {
            public void beginTask( final String name,
                                   final int totalWork )
            {
                monitor.beginTask( name, totalWork );
            }

            public void done()
            {
                monitor.done();
            }

            public void internalWorked( final double work )
            {
                monitor.internalWorked( work );
            }

            public boolean isCanceled()
            {
                return monitor.isCanceled();
            }

            public void setCanceled( final boolean value )
            {
                monitor.setCanceled( value );
            }

            public void setTaskName( final String name )
            {
                monitor.setTaskName( name );
            }

            public void subTask( final String name )
            {
                monitor.subTask( name );
            }

            public void worked( final int work )
            {
                monitor.worked( work );
            }
        };
    }
    
    public static ProgressMonitor create( final IProgressMonitor monitor )
    {
        return new ProgressMonitor()
        {
            @Override
            public void beginTask( final String name,
                                   final int totalWork )
            {
                monitor.beginTask( name, totalWork );
            }

            @Override
            public void done()
            {
                monitor.done();
            }

            @Override
            public void internalWorked( final double work )
            {
                monitor.internalWorked( work );
            }

            @Override
            public boolean isCanceled()
            {
                return monitor.isCanceled();
            }

            @Override
            public void setCanceled( final boolean value )
            {
                monitor.setCanceled( value );
            }

            @Override
            public void setTaskName( final String name )
            {
                monitor.setTaskName( name );
            }

            @Override
            public void subTask( final String name )
            {
                monitor.subTask( name );
            }

            @Override
            public void worked( final int work )
            {
                monitor.worked( work );
            }
        };
    }
    
}
