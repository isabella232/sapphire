/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.validation.internal;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ModelPropertyValidationService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class RequiredPropertyValidationService<T>

    extends ModelPropertyValidationService<T>

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
            final String label = property().getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false );
            final String message = NLS.bind( Resources.message, label );
            return Status.createErrorStatus( message );
        }
    }
    
    protected abstract boolean check();

    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
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
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            if( property instanceof ValueProperty )
            {
                return new RequiredPropertyValidationService<Value<?>>()
                {
                    @Override
                    protected boolean check()
                    {
                        return ( target().getText() != null );
                    }
                };
            }
            else
            {
                return new RequiredPropertyValidationService<ModelElementHandle<?>>()
                {
                    @Override
                    protected boolean check()
                    {
                        return ( target().element() != null );
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
