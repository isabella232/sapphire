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

package org.eclipse.sapphire.tests.modeling.misc.t0009;

import java.util.Date;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.services.DateSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestElement.class );
    
    // *** Date1 ***
    
    @Type( base = Date.class )
    
    ValueProperty PROP_DATE_1 = new ValueProperty( TYPE, "Date1" );
    
    Value<Date> getDate1();
    void setDate1( String value );
    void setDate1( Date value );
    
    // *** Date2 ***
    
    @Type( base = Date.class )
    
    @Service
    (
        impl = DateSerializationService.class, 
        params =
        { 
            @Service.Param( name = "format-1", value = "yyyy.MM.dd" ), 
            @Service.Param( name = "format-2", value = "MM/dd/yyyy" )
        }
    )

    ValueProperty PROP_DATE_2 = new ValueProperty( TYPE, "Date2" );
    
    Value<Date> getDate2();
    void setDate2( String value );
    void setDate2( Date value );
    
    // *** Date3 ***
    
    @Type( base = Date.class )
    @Service( impl = TestDateSerializationService.class )

    ValueProperty PROP_DATE_3 = new ValueProperty( TYPE, "Date3" );
    
    Value<Date> getDate3();
    void setDate3( String value );
    void setDate3( Date value );
    
}
