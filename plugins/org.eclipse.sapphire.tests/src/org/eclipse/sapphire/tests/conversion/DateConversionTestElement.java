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

package org.eclipse.sapphire.tests.conversion;

import java.util.Date;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Serialization;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface DateConversionTestElement extends Element
{
    ElementType TYPE = new ElementType( DateConversionTestElement.class );
    
    // *** Date1 ***
    
    @Type( base = Date.class )
    
    ValueProperty PROP_DATE_1 = new ValueProperty( TYPE, "Date1" );
    
    Value<Date> getDate1();
    void setDate1( String value );
    void setDate1( Date value );
    
    // *** Date2 ***
    
    @Type( base = Date.class )
    @Serialization( primary = "yyyy.MM.dd", alternative = "MM/dd/yyyy" )

    ValueProperty PROP_DATE_2 = new ValueProperty( TYPE, "Date2" );
    
    Value<Date> getDate2();
    void setDate2( String value );
    void setDate2( Date value );
    
}
