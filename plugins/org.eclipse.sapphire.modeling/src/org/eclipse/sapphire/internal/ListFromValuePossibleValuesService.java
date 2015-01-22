/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import java.util.Set;
import java.util.SortedSet;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of PossibleValuesService for list properties based on PossibleValuesService implementation
 * of list member's value property. This service implementation will only activate if the list property has 
 * one possible type, and that type has a single property, and that property is a value property,
 * and that value property has a PossibleValuesService implementation in the property metamodel context. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ListFromValuePossibleValuesService extends PossibleValuesService
{
    private PossibleValuesService base;
    private Listener listener;
    private boolean refreshing;
    
    @Override
    protected void initPossibleValuesService()
    {
        final ListProperty listProperty = context( ListProperty.class );
        final ValueProperty listMemberValueProperty = (ValueProperty) listProperty.getType().properties().first();
        
        this.base = listMemberValueProperty.service( PossibleValuesService.class );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( ! ListFromValuePossibleValuesService.this.refreshing )
                {
                    try
                    {
                        ListFromValuePossibleValuesService.this.refreshing = true;
                        refresh();
                    }
                    finally
                    {
                        ListFromValuePossibleValuesService.this.refreshing = false;
                    }
                }
            }
        };

        this.base.attach( this.listener );
    }
    
    @Override
    protected void compute( final Set<String> values )
    {
        values.addAll( this.base.values() );
    }

    @Override
    public Status problem( final Value<?> value )
    {
        return this.base.problem( value );
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

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            
            if( property != null && property.definition() instanceof ListProperty && 
                property.service( PossibleTypesService.class ).types().size() == 1 )
            {
                final ElementType memberType = property.definition().getType();
                final SortedSet<PropertyDef> properties = memberType.properties();
                
                if( properties.size() == 1 )
                {
                    final PropertyDef memberProperty = properties.first();
                    
                    if( memberProperty instanceof ValueProperty &&
                        memberProperty.service( PossibleValuesService.class ) != null )
                    {
                        return true;
                    }
                }
            }
    
            return false;
        }
    }
    
}
