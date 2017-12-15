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

package org.eclipse.sapphire.internal;

import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Implementation of PossibleValuesService for value properties based on PossibleValuesService implementation
 * of the containing list property. This service implementation will only activate if the value property is
 * the sole property in its type, and the element is contained by a list property, and the list property has
 * a PossibleValueService implementation in the property instance context.  
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValueFromListPossibleValuesService extends PossibleValuesService
{
    private PossibleValuesService base;
    private Listener listener;
    private boolean broadcasting;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = context( IModelElement.class );
        final ListProperty parentProperty = (ListProperty) element.getParentProperty();
        final IModelElement parentElement = (IModelElement) element.parent().parent();
        
        this.base = parentElement.service( parentProperty, PossibleValuesService.class );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( ! ValueFromListPossibleValuesService.this.broadcasting )
                {
                    try
                    {
                        ValueFromListPossibleValuesService.this.broadcasting = true;
                        broadcast();
                    }
                    finally
                    {
                        ValueFromListPossibleValuesService.this.broadcasting = false;
                    }
                }
            }
        };
        
        this.base.attach( this.listener );
    }
    
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        values.addAll( this.base.values() );
    }

    @Override
    public Status.Severity getInvalidValueSeverity( final String invalidValue )
    {
        return this.base.getInvalidValueSeverity( invalidValue );
    }

    @Override
    public String getInvalidValueMessage( final String invalidValue )
    {
        return this.base.getInvalidValueMessage( invalidValue );
    }

    @Override
    public boolean isCaseSensitive()
    {
        return this.base.isCaseSensitive();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            this.base.detach( this.listener );
            this.listener = null;
        }
        
        this.base = null;
    }

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final ModelElementType type = property.getModelElementType();
                final List<ModelProperty> properties = type.properties();
                
                if( properties.size() == 1 )
                {
                    final IModelElement element = context.find( IModelElement.class );
                    final ModelProperty parentProperty = element.getParentProperty();
                    
                    if( parentProperty instanceof ListProperty )
                    {
                        final IModelElement parentElement = (IModelElement) element.parent().parent();

                        if( parentElement.service( parentProperty, PossibleValuesService.class ) != null )
                        {
                            return true;
                        }
                    }
                }
            }
    
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new ValueFromListPossibleValuesService();
        }
    }
    
}
