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

package org.eclipse.sapphire.samples.po;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "check" )
@GenerateImpl

public interface CheckPayment extends Payment
{
    ModelElementType TYPE = new ModelElementType( CheckPayment.class );
    
    // *** Number ***
    
    @Type( base = Integer.class )
    @Label( standard = "number" )
    @Required
    
    ValueProperty PROP_NUMBER = new ValueProperty( TYPE, "Number" );
    
    Value<Integer> getNumber();
    void setNumber( String value );
    void setNumber( Integer value );
    
    // *** Bank ***
    
    @Label( standard = "bank" )
    
    ValueProperty PROP_BANK = new ValueProperty( TYPE, "Bank" );
    
    Value<String> getBank();
    void setBank( String value );
    
}
