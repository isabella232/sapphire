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

package org.eclipse.sapphire.modeling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireMultiStatus 

    extends Status 
    
{
    private static final IStatus[] NO_CHILDREN = new IStatus[ 0 ];
    
    private static final Comparator<IStatus> SEVERITY_COMPARATOR = new Comparator<IStatus>()
    {
        public int compare( final IStatus st1,
                            final IStatus st2 )
        {
            return st2.getSeverity() - st1.getSeverity();
        }
    };
    
    private IStatus child;
    private List<IStatus> children;

    public SapphireMultiStatus() 
    {
        super( OK, SapphireModelingFrameworkPlugin.PLUGIN_ID, null );
    }
    
    public void add( final IStatus status ) 
    {
        final int sev = status.getSeverity();
        
        if( sev != IStatus.OK )
        {
            if( sev > getSeverity() )
            {
                setSeverity( sev );
                setMessage( status.getMessage() );
            }
            
            if( this.children != null )
            {
                this.children.add( status );
            }
            else if( this.child != null )
            {
                this.children = new ArrayList<IStatus>();
                this.children.add( this.child );
                this.children.add( status );
                this.child = null;
            }
            else
            {
                this.child = status;
            }
        }
    }
    
    public void merge( final IStatus status )
    {
        if( status.isMultiStatus() )
        {
            for( IStatus st : status.getChildren() )
            {
                add( st );
            }
        }
        else
        {
            add( status );
        }
    }

    @Override
    public IStatus[] getChildren() 
    {
        if( this.children != null )
        {
            final IStatus[] array = this.children.toArray( new IStatus[ this.children.size() ] );
            Arrays.sort( array, SEVERITY_COMPARATOR );
            return array;
        }
        else if( this.child != null )
        {
            return new IStatus[] { this.child };
        }
        else
        {
            return NO_CHILDREN;
        }
    }

    @Override
    public boolean isMultiStatus() 
    {
        return ( this.children != null && this.children.size() > 1 );
    }

    @Override
    public final int hashCode()
    {
        return getSeverity() * getMessage().hashCode();
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( ! ( obj instanceof SapphireMultiStatus ) )
        {
            return false;
        }
        
        return equal( this, (SapphireMultiStatus) obj );
    }
    
    private static boolean equal( final IStatus x,
                                  final IStatus y )
    {
        if( x == y )
        {
            return true;
        }
        else if( x == null || y == null )
        {
            return false;
        }
        else
        {
            if( x.getSeverity() == y.getSeverity() && x.getMessage().equals( y.getMessage() ) )
            {
                final IStatus[] xChildren = x.getChildren();
                final IStatus[] yChildren = y.getChildren();
                
                if( xChildren.length == yChildren.length )
                {
                    for( int i = 0, n = xChildren.length; i < n; i++ )
                    {
                        if( ! equal( xChildren[ i ], yChildren[ i ] ) )
                        {
                            return false;
                        }
                    }
                    
                    return true;
                }
            }
            
            return false;
        }
    }

}
