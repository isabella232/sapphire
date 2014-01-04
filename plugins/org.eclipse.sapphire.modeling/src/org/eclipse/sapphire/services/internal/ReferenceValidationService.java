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

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ReferenceValidationService extends ValidationService
{
    @Text( "Could not resolve {0} \"{1}\"" )
    private static LocalizableText message;
    
    static
    {
        LocalizableText.init( ReferenceValidationService.class );
    }

    private ReferenceService referenceService;
    private Listener referenceServiceListener;
    
    @Override
    protected void initValidationService()
    {
        final Property property = context( Property.class );
        
        this.referenceService = property.service( ReferenceService.class );
        
        if( this.referenceService != null )
        {
            this.referenceServiceListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refresh();
                }
            };
            
            this.referenceService.attach( this.referenceServiceListener );
        }
    }

    @Override
    protected Status compute()
    {
        final ReferenceValue<?,?> value = context( ReferenceValue.class );
        
        if( value.resolve() == null && value.text() != null )
        {
            final ValueProperty property = value.definition();
            final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
            final String str = value.text();
            final String msg = message.format( label, str );
            return Status.createErrorStatus( msg );
        }
        
        return Status.createOkStatus();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.referenceServiceListener != null )
        {
            this.referenceService.detach( this.referenceServiceListener );
        }
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( Reference.class ) && property.hasAnnotation( MustExist.class ) );
        }
    }
    
}
