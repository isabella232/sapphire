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

package org.eclipse.sapphire.internal;

import java.util.Set;

import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of PossibleValuesService based on @PossibleValues annotation's values attribute..
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StaticPossibleValuesService extends PossibleValuesService
{
    private String[] values;
    
    @Override
    protected void initPossibleValuesService()
    {
        final PropertyDef pdef = context( PropertyDef.class );
        final PossibleValues a = pdef.getAnnotation( PossibleValues.class );
        
        this.values = a.values();
        
        final String invalidValueMessage = a.invalidValueMessage();
        
        if( invalidValueMessage.length() > 0 )
        {
            this.invalidValueMessage = pdef.getLocalizationService().text( invalidValueMessage, CapitalizationType.NO_CAPS, false );
        }
        
        this.invalidValueSeverity = a.invalidValueSeverity();
        this.ordered = a.ordered();
    }

    @Override
    protected void compute( final Set<String> values )
    {
        for( final String value : this.values )
        {
            values.add( value );
        }
    }
    
    public static final class ValuePropertyCondition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final PropertyDef property = context.find( PropertyDef.class );
            
            return
            (
                ( property instanceof ValueProperty ) && 
                property.hasAnnotation( PossibleValues.class ) && 
                property.getAnnotation( PossibleValues.class ).values().length > 0
            );
        }
    }
    
    public static final class ListPropertyCondition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final PropertyDef property = context.find( PropertyDef.class );
            
            return
            (
                ( property instanceof ListProperty ) && 
                property.hasAnnotation( PossibleValues.class ) && 
                property.getAnnotation( PossibleValues.class ).values().length > 0
            );
        }
    }
    
}
