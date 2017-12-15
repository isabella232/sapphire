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

import static org.eclipse.sapphire.modeling.util.internal.SapphireCommonUtil.getDefaultValueLabel;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PreferDefaultValue;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PreferDefaultValueValidationService extends ValidationService
{
    private Listener listener;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                broadcast();
            }
        };
        
        context( IModelElement.class ).attach( this.listener, context( ValueProperty.class ) );
    }

    @Override
    public Status validate()
    {
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        final Value<?> value = element.read( property );
        
        if( ! value.isDefault() )
        {
            final String text = value.getText();
            final String def = getDefaultValueLabel( element, property );
            
            if( def != null && ! def.equals( text ) )
            {
                final String message = NLS.bind( Resources.message, property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false ), def );
                return Status.createWarningStatus( message );
            }
        }
        
        return Status.createOkStatus();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            context( IModelElement.class ).detach( this.listener, context( ValueProperty.class ) );
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
                return property.hasAnnotation( PreferDefaultValue.class );
            }
            
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new PreferDefaultValueValidationService();
        }
    }

    private static final class Resources extends NLS
    {
        public static String message;
        
        static
        {
            initializeMessages( PreferDefaultValueValidationService.class.getName(), Resources.class );
        }
    }
    
}
