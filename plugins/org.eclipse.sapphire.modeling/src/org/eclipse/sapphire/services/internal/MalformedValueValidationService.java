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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.localization.LocalizationSystem;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MalformedValueValidationService extends ValidationService
{
    @Text( "\"{1}\" is not a valid {0}" )
    private static LocalizableText cannotParseValueMessage;
    
    static
    {
        LocalizableText.init( MalformedValueValidationService.class );
    }

    private String valueTypeName;
    
    @Override
    protected Status compute()
    {
        final Value<?> value = context( Element.class ).property( context( ValueProperty.class ) );
        
        if( value.malformed() )
        {
            if( this.valueTypeName == null )
            {
                final Class<?> type = value.definition().getTypeClass();
                this.valueTypeName = LocalizationSystem.service( type ).label( type, CapitalizationType.NO_CAPS, false );
            }
            
            final String msg = cannotParseValueMessage.format( this.valueTypeName, value.text() );
            return Status.createErrorStatus( msg );
        }
        else
        {
            return Status.createOkStatus();
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && ! String.class.isAssignableFrom( property.getTypeClass() ) );
        }
    }
    
}
