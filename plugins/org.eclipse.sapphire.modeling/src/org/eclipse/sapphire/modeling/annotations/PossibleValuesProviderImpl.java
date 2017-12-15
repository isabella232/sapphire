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

package org.eclipse.sapphire.modeling.annotations;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PossibleValuesProviderImpl
{
    private IModelElement element;
    private ValueProperty property;
    private String invalidValueMessageTemplate;
    private int invalidValueSeverity;
    private boolean isCaseSensitive;
    
    private final Set<PossibleValuesProviderListener> listeners = new CopyOnWriteArraySet<PossibleValuesProviderListener>();
    
    public void init( final IModelElement element,
                      final ValueProperty property,
                      final String invalidValueMessageTemplate,
                      final int invalidValueSeverity,
                      final boolean isCaseSensitive,
                      final String[] params )
    {
        this.element = element;
        this.property = property;
        this.invalidValueMessageTemplate = invalidValueMessageTemplate;
        this.invalidValueSeverity = invalidValueSeverity;
        this.isCaseSensitive = isCaseSensitive;
    }
    
    public final IModelElement getModelElement()
    {
        return this.element;
    }
    
    public final ValueProperty getProperty()
    {
        return this.property;
    }
    
    public final SortedSet<String> getPossibleValues()
    {
        final TreeSet<String> values = new TreeSet<String>();
        fillPossibleValues( values );
        return values;
    }
    
    protected abstract void fillPossibleValues( final SortedSet<String> values );
    
    public String getInvalidValueMessage( final String invalidValue )
    {
        final String template;
        
        if( this.invalidValueMessageTemplate != null )
        {
            template = this.invalidValueMessageTemplate;
        }
        else
        {
            throw new IllegalStateException();
        }
        
        return NLS.bind( template, invalidValue );
    }
    
    public int getInvalidValueSeverity( final String invalidValue )
    {
        return this.invalidValueSeverity;
    }
    
    public boolean isCaseSensitive()
    {
        return this.isCaseSensitive;
    }
    
    public final void addListener( final PossibleValuesProviderListener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.listeners.add( listener );
    }
    
    public final void removeListener( final PossibleValuesProviderListener listener )
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
        
        for( PossibleValuesProviderListener listener : this.listeners )
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
    
}
