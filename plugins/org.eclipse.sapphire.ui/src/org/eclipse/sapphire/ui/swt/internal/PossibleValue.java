/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.ValueLabelService;
import org.eclipse.sapphire.services.ValueNormalizationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PossibleValue
{
    public static final class Factory
    {
        private final PossibleValuesService possibleValuesService;
        private final ValueNormalizationService valueNormalizationService;
        private final ValueLabelService valueLabelService;
        
        private Factory( final IModelElement element,
                         final ValueProperty property )
        {
            this.possibleValuesService = element.service( property, PossibleValuesService.class );
            this.valueNormalizationService = element.service( property, ValueNormalizationService.class );
            this.valueLabelService = element.service( property, ValueLabelService.class );
        }
        
        public List<PossibleValue> entries()
        {
            final List<PossibleValue> entries = new ArrayList<PossibleValue>();
            
            for( String value : this.possibleValuesService.values() )
            {
                entries.add( entry( value ) );
            }
            
            return Collections.unmodifiableList( entries );
        }
        
        public PossibleValue entry( final String value )
        {
            final String normalized = this.valueNormalizationService.normalize( value );
            final String label = this.valueLabelService.provide( normalized );
            
            return new PossibleValue( normalized, label );
        }
    }
    
    public static Factory factory( final IModelElement element,
                                   final ValueProperty property )
    {
        return new Factory( element, property );
    }
    
    private final String value;
    private final String label;
    
    private PossibleValue( final String value,
                           final String label )
    {
        this.value = value;
        this.label = label;
    }
    
    public String value()
    {
        return this.value;
    }
    
    public String label()
    {
        return this.label;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof PossibleValue )
        {
            return this.value.equals( ( (PossibleValue) obj ).value );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.value.hashCode();
    }

}
