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
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Implementation of PossibleValuesService for list properties based on PossibleValuesService implementation
 * of list member's value property. This service implementation will only activate if the list property has 
 * one possible type, and that type has a single property, and that property is a value property,
 * and that value property has a PossibleValuesService implementation in the property metamodel context. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ListFromValuePossibleValuesService extends PossibleValuesService
{
    private PossibleValuesService base;
    private Listener listener;
    private boolean broadcasting;
    
    @Override
    protected void init()
    {
        super.init();
        
        final ListProperty listProperty = context( ListProperty.class );
        final ValueProperty listMemberValueProperty = (ValueProperty) listProperty.getType().properties().get( 0 );
        
        this.base = listMemberValueProperty.service( PossibleValuesService.class );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( ! ListFromValuePossibleValuesService.this.broadcasting )
                {
                    try
                    {
                        ListFromValuePossibleValuesService.this.broadcasting = true;
                        broadcast();
                    }
                    finally
                    {
                        ListFromValuePossibleValuesService.this.broadcasting = false;
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
            final ListProperty listProperty = context.find( ListProperty.class );
            
            if( listProperty != null )
            {
                final IModelElement element = context.find( IModelElement.class );
                
                if( element.service( listProperty, PossibleTypesService.class ).types().size() == 1 )
                {
                    final ModelElementType memberType = listProperty.getType();
                    final List<ModelProperty> properties = memberType.properties();
                    
                    if( properties.size() == 1 )
                    {
                        final ModelProperty memberProperty = properties.get( 0 );
                        
                        if( memberProperty instanceof ValueProperty &&
                            memberProperty.service( PossibleValuesService.class ) != null )
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
            return new ListFromValuePossibleValuesService();
        }
    }
    
}
