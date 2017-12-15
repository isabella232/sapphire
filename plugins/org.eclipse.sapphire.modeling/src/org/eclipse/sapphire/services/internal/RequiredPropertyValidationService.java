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

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class RequiredPropertyValidationService extends ValidationService
{
    @Override
    public final Status validate()
    {
        if( check() )
        {
            return Status.createOkStatus();
        }
        else
        {
            final String label = context( ModelProperty.class ).getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false );
            final String message = NLS.bind( Resources.message, label );
            return Status.createErrorStatus( message );
        }
    }
    
    protected abstract boolean check();

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ModelProperty property = context.find( ModelProperty.class );
            
            return 
            (
                property.hasAnnotation( Required.class ) && 
                (
                    property instanceof ValueProperty || 
                    ( property instanceof ElementProperty && ! ( property instanceof ImpliedElementProperty ) ) 
                )
            );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            final ModelProperty property = context.find( ModelProperty.class );
            
            if( property instanceof ValueProperty )
            {
                return new RequiredPropertyValidationService()
                {
                    @Override
                    protected boolean check()
                    {
                        return ( context( IModelElement.class ).read( context( ValueProperty.class ) ).getText() != null );
                    }
                };
            }
            else
            {
                return new RequiredPropertyValidationService()
                {
                    @Override
                    protected boolean check()
                    {
                        return ( context( IModelElement.class ).read( context( ElementProperty.class ) ).element() != null );
                    }
                };
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String message;
        
        static
        {
            initializeMessages( RequiredPropertyValidationService.class.getName(), Resources.class );
        }
    }

}
