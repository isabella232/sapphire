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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ReferenceValidationService extends ValidationService
{
    private ReferenceService referenceService;
    private Listener referenceServiceListener;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        this.referenceService = element.service( property, ReferenceService.class );
        
        if( this.referenceService != null )
        {
            this.referenceServiceListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    broadcast();
                }
            };
            
            this.referenceService.attach( this.referenceServiceListener );
        }
    }

    @Override
    public Status validate()
    {
        final ReferenceValue<?,?> value = (ReferenceValue<?,?>) context( IModelElement.class ).read( context( ValueProperty.class ) );
        
        if( value.resolve() == null && value.getText() != null )
        {
            final ValueProperty property = value.getProperty();
            final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
            final String str = value.getText();
            final String msg = NLS.bind( Resources.message, label, str );
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

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( Reference.class ) && property.hasAnnotation( MustExist.class ) );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new ReferenceValidationService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String message;
        
        static
        {
            initializeMessages( ReferenceValidationService.class.getName(), Resources.class );
        }
    }
    
}
