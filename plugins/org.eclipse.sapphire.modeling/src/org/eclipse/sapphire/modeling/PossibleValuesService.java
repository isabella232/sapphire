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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PossibleValuesService

    extends ModelPropertyService
    
{
    private final Set<Listener> listeners = new CopyOnWriteArraySet<Listener>();
    
    public final SortedSet<String> getPossibleValues()
    {
        final TreeSet<String> values = new TreeSet<String>();
        fillPossibleValues( values );
        return values;
    }
    
    protected abstract void fillPossibleValues( final SortedSet<String> values );
    
    public String getInvalidValueMessage( final String invalidValue )
    {
        return NLS.bind( Resources.defaultInvalidValueMessage, invalidValue, property().getLabel( true, CapitalizationType.NO_CAPS, false ) );
    }
    
    public int getInvalidValueSeverity( final String invalidValue )
    {
        return IStatus.ERROR;
    }
    
    public boolean isCaseSensitive()
    {
        return true;
    }
    
    public final void addListener( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.listeners.add( listener );
    }
    
    public final void removeListener( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.listeners.remove( listener );
    }
    
    protected final void notifyListeners( final PossibleValuesChangedEvent event )
    {
        if( event == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( Listener listener : this.listeners )
        {
            try
            {
                listener.handlePossibleValuesChangedEvent( event );
            }
            catch( Exception e )
            {
                SapphireModelingFrameworkPlugin.log( e );
            }
        }
    }
    
    public static abstract class Listener
    {
        public abstract void handlePossibleValuesChangedEvent( PossibleValuesChangedEvent event );
    }
    
    public static class PossibleValuesChangedEvent
    {
        // This is a placeholder for now, to allow for API growth.
    }
    
    private static final class Resources extends NLS
    {
        public static String defaultInvalidValueMessage;
        
        static
        {
            initializeMessages( PossibleValuesService.class.getName(), Resources.class );
        }
    }
    
}
