/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ValueKeyword;
import org.eclipse.sapphire.modeling.annotations.NamedValues;
import org.eclipse.sapphire.modeling.annotations.NamedValues.NamedValue;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValueLabelService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DefaultValueLabelService extends ValueLabelService
{
    @Override
    public String provide( final String text )
    {
        final ValueProperty property = context( ValueProperty.class );
        final String decoded = property.decodeKeywords( text );
        
        String label = decoded;
        
        final ValueKeyword keyword = property.getKeyword( property.encodeKeywords( decoded ) );
        
        if( keyword != null )
        {
            label = keyword.toDisplayString();
        }
        else if( property.hasAnnotation( NamedValues.class ) ) 
        {
            final LocalizationService localization = property.getLocalizationService();
            
            for( final NamedValue x : property.getAnnotation( NamedValues.class ).namedValues() )
            {
                if( decoded.equals( x.value() ) ) 
                {
                    label = localization.text( x.label(), CapitalizationType.NO_CAPS, false ) + " (" + x.value() + ")";
                    break;
                }
            }
        }
        
        return label;
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            return ( context.find( ValueProperty.class ) != null );
        }
    }
    
}
