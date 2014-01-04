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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PossibleValuesValidationService extends ValidationService
{
    private PossibleValuesService possibleValuesService;
    private Listener possibleValuesServiceListener;
    
    @Override
    protected void initValidationService()
    {
        this.possibleValuesService = context( Property.class ).service( PossibleValuesService.class );
        
        if( this.possibleValuesService != null )
        {
            this.possibleValuesServiceListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refresh();
                }
            };
            
            this.possibleValuesService.attach( this.possibleValuesServiceListener );
        }
    }

    @Override
    protected Status compute()
    {
        final Element element = context( Element.class );
        final ValueProperty property = context( ValueProperty.class );
        final Value<?> value = element.property( property );
        final String valueString = value.text( true );
        
        if( valueString != null && this.possibleValuesService != null )
        {
            if( ! this.possibleValuesService.values().contains( valueString ) )
            {
                final Status.Severity severity = this.possibleValuesService.getInvalidValueSeverity( valueString );
                
                if( severity != Status.Severity.OK )
                {
                    final String message = this.possibleValuesService.getInvalidValueMessage( valueString );
                    return Status.createStatus( severity, message );
                }
            }
        }
        
        return Status.createOkStatus();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.possibleValuesServiceListener != null )
        {
            this.possibleValuesService.detach( this.possibleValuesServiceListener );
        }
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            return ( property != null && property.definition() instanceof ValueProperty && property.service( PossibleValuesService.class ) != null && property.service( ReferenceService.class ) == null );
        }
    }
    
}
