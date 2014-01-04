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

package org.eclipse.sapphire.tests.services.t0014;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Validation;
import org.eclipse.sapphire.Validations;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** Min ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "0" )
    @Validation( rule = "${ Min <= Max }", message = "Must not be larger than max" )

    ValueProperty PROP_MIN = new ValueProperty( TYPE, "Min" );
    
    Value<Integer> getMin();
    void setMin( String value );
    void setMin( Integer value );
    
    // *** Max ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "0" )
    
    @Validations
    (
        {
            @Validation( rule = "${ Max >= Min }", message = "Must not be smaller than min" ),
            @Validation( rule = "${ Max <= 100 }", message = "Must be less than or equal to 100", severity = Status.Severity.WARNING )
        }
    )
    
    ValueProperty PROP_MAX = new ValueProperty( TYPE, "Max" );
    
    Value<Integer> getMax();
    void setMax( String value );
    void setMax( Integer value );
    
    // *** Path ***
    
    @Validation
    (
        rule = "${ Path == null || Path.StartsWith( '/' ) }",
        message = "Path \"${ Path }\" must start with a slash"
    )
    
    ValueProperty PROP_PATH = new ValueProperty( TYPE, "Path" );
    
    Value<String> getPath();
    void setPath( String value );

}
