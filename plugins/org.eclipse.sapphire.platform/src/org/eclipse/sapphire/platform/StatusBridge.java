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

import java.util.SortedSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;

/**
 * Bridges between Sapphire and Eclipse status API.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StatusBridge
{
    private StatusBridge()
    {
        // This class is not meant to be instantiated.
    }
    
    public static IStatus create( final Status status )
    {
        final SortedSet<Status> childrenOriginal = status.children();
        final IStatus[] childrenBridged = new IStatus[ childrenOriginal.size() ];
        
        int i = 0;
        
        for( Status child : childrenOriginal )
        {
            childrenBridged[ i ]  = create( child );
            i++;
        }
        
        final int severity;
        
        switch( status.severity() )
        {
            case ERROR:    severity = IStatus.ERROR; break;
            case WARNING:  severity = IStatus.WARNING; break;
            default:       severity = IStatus.OK; break;
        }
        
        return new IStatus()
        {
            public IStatus[] getChildren()
            {
                return childrenBridged;
            }

            public int getCode()
            {
                return 0;
            }

            public Throwable getException()
            {
                return status.exception();
            }

            public String getMessage()
            {
                return status.message();
            }

            public String getPlugin()
            {
                return "org.eclipse.sapphire";
            }

            public int getSeverity()
            {
                return severity;
            }

            public boolean isMultiStatus()
            {
                return ( ! status.children().isEmpty() );
            }

            public boolean isOK()
            {
                return ( status.severity() == Status.Severity.OK );
            }

            public boolean matches( final int severityMask )
            {
                return ( ( getSeverity() & severityMask ) != 0 );
            }
        };
    }
    
    public static Status create( final IStatus status )
    {
        if( status.isMultiStatus() )
        {
            final Status.CompositeStatusFactory factory = Status.factoryForComposite();
            
            for( IStatus st : status.getChildren() )
            {
                factory.merge( create( st ) );
            }
            
            return factory.create();
        }
        else
        {
            final Status.LeafStatusFactory factory = Status.factoryForLeaf();
            
            factory.message( status.getMessage() );
            
            switch( status.getSeverity() )
            {
                case IStatus.ERROR:    factory.severity( Severity.ERROR ); break;
                case IStatus.WARNING:  factory.severity( Severity.WARNING ); break;
                default:               factory.severity( Severity.OK ); break;
            }
            
            factory.exception( status.getException() );
            
            factory.type( "Sapphire.Bridged." + status.getPlugin() + "." + status.getCode() );
            
            return factory.create();
        }
    }
    
}
