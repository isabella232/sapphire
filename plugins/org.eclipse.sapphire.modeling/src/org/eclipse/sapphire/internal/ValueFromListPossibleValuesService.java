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

package org.eclipse.sapphire.internal;

import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

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
    private boolean refreshing;
    
    @Override
    protected void initPossibleValuesService()
    {
        final Property parent = context( Element.class ).parent();
        
        this.base = parent.service( PossibleValuesService.class );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( ! ValueFromListPossibleValuesService.this.refreshing )
                {
                    try
                    {
                        ValueFromListPossibleValuesService.this.refreshing = true;
                        refresh();
                    }
                    finally
                    {
                        ValueFromListPossibleValuesService.this.refreshing = false;
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
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final ElementType type = property.getModelElementType();
                
                if( type.properties().size() == 1 )
                {
                    final Property parent = context.find( Element.class ).parent();
                    
                    if( parent != null && parent.definition() instanceof ListProperty && parent.service( PossibleValuesService.class ) != null )
                    {
                        return true;
                    }
                }
            }
    
            return false;
        }
    }
    
}
