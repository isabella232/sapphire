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

import java.util.SortedSet;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.RequiredConstraintService;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * {@link FactsService} implementation that contributes fact statements based on semantical
 * information from {@link RequiredConstraintService}.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class RequiredConstraintFactsService extends FactsService
{
    @Text( "Must be specified" )
    private static LocalizableText statement;
    
    static
    {
        LocalizableText.init( RequiredConstraintFactsService.class );
    }

    private Property property;
    private RequiredConstraintService requiredConstraintService;
    private Listener listener;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.property = context( Property.class );
        this.requiredConstraintService = this.property.service( RequiredConstraintService.class );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                broadcast();
            }
        };
        
        this.requiredConstraintService.attach( this.listener );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        if( this.requiredConstraintService.required() )
        {
            boolean applicable = true;
            
            if( this.property instanceof Value )
            {
                final Value<?> value = (Value<?>) this.property;
                
                if( value.getDefaultText() != null )
                {
                    applicable = false;
                }
            }
            
            if( applicable )
            {
                facts.add( statement.text() );
            }
        }
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            this.requiredConstraintService.detach( this.listener );
        }
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            return ( property != null && property.service( RequiredConstraintService.class ) != null );
        }
    }
    
}
