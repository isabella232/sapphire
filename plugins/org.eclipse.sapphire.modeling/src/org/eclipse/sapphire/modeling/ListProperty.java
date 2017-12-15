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

package org.eclipse.sapphire.modeling;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ListProperty 

    extends ModelProperty
    
{
    public ListProperty( final ModelElementType type,
                         final String propertyName )
    {
        super( type, propertyName, null );
    }
        
    public ListProperty( final ModelElementType type,
                         final ListProperty baseProperty )
    {
        super( type, baseProperty.getName(), baseProperty );
    }
    
    @Override
    
    protected List<ModelPropertyValidator<? extends Object>> createValidators()
    {
        final List<ModelPropertyValidator<? extends Object>> validators = super.createValidators();
        
        final CountConstraint countConstraint = getAnnotation( CountConstraint.class );
        
        if( countConstraint != null )
        {
            validators.add( new CountConstraintValidator( countConstraint ) );
        }
        
        return validators;
    }
    
    private final class CountConstraintValidator
    
        extends ModelPropertyValidator<ModelElementList<?>>
        
    {
        private final CountConstraint constraint;
        
        public CountConstraintValidator( final CountConstraint constraint )
        {
            this.constraint = constraint;
        }
        
        @Override
        public IStatus validate( final ModelElementList<?> list )
        {
            final int count = list.size();
            String message = null;
            
            if( count < this.constraint.min() )
            {
                if( this.constraint.min() == 1 )
                {
                    message = Resources.bind( Resources.countConstraintTooFewAtLeastOne, 
                                              getType().getLabel( true, CapitalizationType.NO_CAPS, false ) );
                }
                else
                {
                    message = Resources.bind( Resources.countConstraintTooFew, 
                                              getType().getLabel( true, CapitalizationType.NO_CAPS, false ), 
                                              String.valueOf( this.constraint.min() ) );
                }
            }
            else if( count > this.constraint.max() )
            {
                message = Resources.bind( Resources.countConstraintTooMany,
                                          getType().getLabel( true, CapitalizationType.NO_CAPS, false ), 
                                          String.valueOf( this.constraint.max() ) );
            }
            
            if( message == null )
            {
                return Status.OK_STATUS;
            }
            else
            {
                return new Status( Status.ERROR, "abc", message ); //$NON-NLS-1$
            }
        }
    }

    private static final class Resources
    
        extends NLS
    
    {
        public static String countConstraintTooFew;
        public static String countConstraintTooFewAtLeastOne;
        public static String countConstraintTooMany;
        
        static
        {
            initializeMessages( ListProperty.class.getName(), Resources.class );
        }
    }
    
}
