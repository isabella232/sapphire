/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.internal.ValueSnapshot;
import org.eclipse.sapphire.modeling.annotations.SensitiveData;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about property's default value by using semantical information specified 
 * by DefaultValueService and @DefaultValue annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DefaultValueFactsService extends FactsService
{
    @Text( "Default value is {0}" )
    private static LocalizableText statement;
    
    @Text( "Has default value" )
    private static LocalizableText statementForSensitive;
    
    static
    {
        LocalizableText.init( DefaultValueFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final Value<?> value = context( Value.class );
        final String def = value.getDefaultText();
        
        if( def != null )
        {
            if( value.definition().hasAnnotation( SensitiveData.class ) )
            {
                facts.add( statementForSensitive.text() );
            }
            else
            {
                facts.add( statement.format( new ValueSnapshot( value.definition(), def ) ) );
            }
        }
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
