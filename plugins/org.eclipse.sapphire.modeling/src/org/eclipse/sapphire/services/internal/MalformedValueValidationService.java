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

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.localization.LocalizationSystem;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MalformedValueValidationService extends ValidationService
{
    private String valueTypeName;
    
    @Override
    public Status validate()
    {
        final Value<?> value = context( IModelElement.class ).read( context( ValueProperty.class ) );
        
        if( value.isMalformed() )
        {
            if( this.valueTypeName == null )
            {
                final Class<?> type = value.getProperty().getTypeClass();
                this.valueTypeName = LocalizationSystem.service( type ).label( type, CapitalizationType.NO_CAPS, false );
            }
            
            final String msg = NLS.bind( Resources.cannotParseValueMessage, this.valueTypeName, value.getText() );
            return Status.createErrorStatus( msg );
        }
        else
        {
            return Status.createOkStatus();
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && ! String.class.isAssignableFrom( property.getTypeClass() ) );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new MalformedValueValidationService();
        }
    }

    private static final class Resources extends NLS
    {
        public static String cannotParseValueMessage;
    
        static
        {
            initializeMessages( MalformedValueValidationService.class.getName(), Resources.class );
        }
    }
    
}
