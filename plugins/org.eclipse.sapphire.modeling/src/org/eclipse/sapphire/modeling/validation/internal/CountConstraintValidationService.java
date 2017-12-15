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
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ModelPropertyValidationService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CountConstraintValidationService

    extends ModelPropertyValidationService<ModelElementList<?>>
    
{
    private CountConstraint constraint;
    
    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
        this.constraint = property.getAnnotation( CountConstraint.class );
    }

    @Override
    public Status validate()
    {
        final ModelElementList<?> list = target();
        final int count = list.size();
        String message = null;
        
        if( count < this.constraint.min() )
        {
            if( this.constraint.min() == 1 )
            {
                message = Resources.bind( Resources.countConstraintTooFewAtLeastOne, 
                                          property().getType().getLabel( true, CapitalizationType.NO_CAPS, false ) );
            }
            else
            {
                message = Resources.bind( Resources.countConstraintTooFew, 
                                          property().getType().getLabel( true, CapitalizationType.NO_CAPS, false ), 
                                          String.valueOf( this.constraint.min() ) );
            }
        }
        else if( count > this.constraint.max() )
        {
            message = Resources.bind( Resources.countConstraintTooMany,
                                      property().getType().getLabel( true, CapitalizationType.NO_CAPS, false ), 
                                      String.valueOf( this.constraint.max() ) );
        }
        
        if( message == null )
        {
            return Status.createOkStatus();
        }
        else
        {
            return Status.createErrorStatus( message );
        }
    }
    
    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            return ( property instanceof ListProperty && property.hasAnnotation( CountConstraint.class ) );
        }

        @Override
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            return new CountConstraintValidationService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String countConstraintTooFew;
        public static String countConstraintTooFewAtLeastOne;
        public static String countConstraintTooMany;
        
        static
        {
            initializeMessages( CountConstraintValidationService.class.getName(), Resources.class );
        }
    }

}