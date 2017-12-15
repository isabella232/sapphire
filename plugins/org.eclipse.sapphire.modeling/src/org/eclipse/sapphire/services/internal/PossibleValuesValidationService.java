/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PossibleValuesValidationService extends ValidationService
{
    private PossibleValuesService possibleValuesService;
    private Listener possibleValuesServiceListener;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        this.possibleValuesService = element.service( property, PossibleValuesService.class );
        
        if( this.possibleValuesService != null )
        {
            this.possibleValuesServiceListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    broadcast();
                }
            };
            
            this.possibleValuesService.attach( this.possibleValuesServiceListener );
        }
    }

    @Override
    public Status validate()
    {
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        final Value<?> value = element.read( property );
        final String valueString = value.getText( true );
        
        if( valueString != null && this.possibleValuesService != null )
        {
            Collection<String> values = this.possibleValuesService.values();
            
            for( String v : values )
            {
                if( v == null )
                {
                    final String msg = NLS.bind( Resources.possibleValuesServiceReturnedNull, this.possibleValuesService.getClass().getName() );
                    LoggingService.log( Status.createErrorStatus( msg ) );
                    
                    values = Collections.emptyList();
                }
            }
            
            boolean found = false;
            
            if( this.possibleValuesService.isCaseSensitive() )
            {
                found = values.contains( valueString );
            }
            else
            {
                for( String v : values )
                {
                    if( v.equalsIgnoreCase( valueString ) )
                    {
                        found = true;
                        break;
                    }
                }
            }

            if( ! found )
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

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final IModelElement element = context.find( IModelElement.class );
                
                if( element.service( property, PossibleValuesService.class ) != null && element.service( property, ReferenceService.class ) == null )
                {
                    return true;
                }
            }
            
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new PossibleValuesValidationService();
        }
    }

    private static final class Resources extends NLS
    {
        public static String possibleValuesServiceReturnedNull;
        
        static
        {
            initializeMessages( PossibleValuesValidationService.class.getName(), Resources.class );
        }
    }
    
}
