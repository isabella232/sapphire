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

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0004;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlBinding( path = "rectangle" )

public interface TestXmlXsd0004Rectangle extends TestXmlXsd0004Shape
{
    ModelElementType TYPE = new ModelElementType( TestXmlXsd0004Rectangle.class );
    
    // *** Width ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "width" )

    ValueProperty PROP_WIDTH = new ValueProperty( TYPE, "Width" );
    
    Value<Integer> getWidth();
    void setWidth( String value );
    void setWidth( Integer value );
    
    // *** Height ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "height" )

    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height" );
    
    Value<Integer> getHeight();
    void setHeight( String value );
    void setHeight( Integer value );
    
}
