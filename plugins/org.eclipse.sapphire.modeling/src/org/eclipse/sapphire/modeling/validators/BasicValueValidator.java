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
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.localization.LocalizationSystem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class BasicValueValidator

    extends ModelPropertyValidator<Value<?>>

{
    private String valueTypeName;
    
    @Override
    public IStatus validate( final Value<?> value )
    {
        if( value.isMalformed() )
        {
            if( this.valueTypeName == null )
            {
                final Class<?> type = value.getProperty().getTypeClass();
                this.valueTypeName = LocalizationSystem.service( type ).label( type, CapitalizationType.NO_CAPS, false );
            }
            
            final String msg = NLS.bind( Resources.cannotParseValueMessage, this.valueTypeName, value.getText() );
            return createErrorStatus( msg );
        }
        else
        {
            return Status.OK_STATUS;
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String cannotParseValueMessage;
    
        static
        {
            initializeMessages( BasicValueValidator.class.getName(), Resources.class );
        }
    }
    
}
