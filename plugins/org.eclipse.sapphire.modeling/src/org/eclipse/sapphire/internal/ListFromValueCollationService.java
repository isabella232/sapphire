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

import java.util.Comparator;
import java.util.SortedSet;

import org.eclipse.sapphire.CollationService;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of CollationService for list properties based on CollationService implementation
 * of list member's value property. This service implementation will only activate if the list property has 
 * one possible type, and that type has a single property, and that property is a value property. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ListFromValueCollationService extends CollationService
{
    private CollationService base;
    private Listener listener;
    private boolean refreshing;
    
    @Override
    protected void initCollationService()
    {
        final ListProperty listProperty = context( ListProperty.class );
        final ValueProperty listMemberValueProperty = (ValueProperty) listProperty.getType().properties().first();
        
        this.base = listMemberValueProperty.service( CollationService.class );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( ! ListFromValueCollationService.this.refreshing )
                {
                    try
                    {
                        ListFromValueCollationService.this.refreshing = true;
                        refresh();
                    }
                    finally
                    {
                        ListFromValueCollationService.this.refreshing = false;
                    }
                }
            }
        };

        this.base.attach( this.listener );
    }
    
    @Override
    protected Comparator<String> compute()
    {
        return this.base.comparator();
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
                    
                    return ( memberProperty instanceof ValueProperty );
                }
            }
    
            return false;
        }
    }
    
}
