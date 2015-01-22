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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;
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

    @Override
    protected Status compute()
    {
        final Value<?> value = context( Value.class );
        final String text = value.text();
        
        if( text != null && ! this.possibleValuesService.values().contains( text ) )
        {
            return this.possibleValuesService.problem( value );
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
            final Value<?> value = context.find( Value.class );
            return ( value != null && value.service( PossibleValuesService.class ) != null && value.service( ReferenceService.class ) == null );
        }
    }
    
}
