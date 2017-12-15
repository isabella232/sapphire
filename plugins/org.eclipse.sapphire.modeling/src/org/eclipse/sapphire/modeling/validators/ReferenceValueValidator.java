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

package org.eclipse.sapphire.modeling.validators;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ReferenceValueValidator

    extends ModelPropertyValidator<ReferenceValue<?>>
    
{
    @Override
    public IStatus validate( final ReferenceValue<?> value )
    {
        if( value.resolve() == null && value.getText() != null )
        {
            final ValueProperty property = value.getProperty();
            final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
            final String str = value.getText();
            final String msg = NLS.bind( Resources.message, label, str );
            return createErrorStatus( msg );
        }
        
        return Status.OK_STATUS;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String message;
        
        static
        {
            initializeMessages( ReferenceValueValidator.class.getName(), Resources.class );
        }
    }
    
}
