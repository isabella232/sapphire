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

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementDisposedEvent;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesChangedEvent;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PossibleValuesFromModelProvider

    extends PossibleValuesProviderImpl
    
{
    private final ModelPath path;
    private Set<String> values;
    
    public PossibleValuesFromModelProvider( final ModelPath path )
    {
        this.path = path;
        this.values = Collections.emptySet();
    }
    
    @Override
    public void init( final IModelElement element,
                      final ValueProperty property,
                      final String invalidValueMessageTemplate,
                      final int invalidValueSeverity,
                      final boolean isCaseSensitive,
                      final String[] params )
    {
        super.init( element, property, invalidValueMessageTemplate,
                    invalidValueSeverity, isCaseSensitive, params );
        
        final ModelPropertyListener listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refresh();
            }
        };
        
        element.addListener( listener, this.path );
        
        element.addListener
        (
            new ModelElementListener()
            {
                @Override
                public void handleElementDisposedEvent( final ModelElementDisposedEvent event )
                {
                    element.removeListener( listener, PossibleValuesFromModelProvider.this.path );
                }
            }
        );
    }

    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        values.addAll( this.values );
    }
    
    private void refresh()
    {
        final Set<String> newValues = getModelElement().service().read( this.path );
        
        if( ! this.values.equals( newValues ) )
        {
            this.values = Collections.unmodifiableSet( newValues );
            notifyListeners( new PossibleValuesChangedEvent() );
        }
    }

}
