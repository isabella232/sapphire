/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0003;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public interface TestXmlXsd0003Circle extends Element
{
    ElementType TYPE = new ElementType( TestXmlXsd0003Circle.class );
    
    // *** circle1 ***
    
    @XmlBinding( path = "circle1" )
    
    ValueProperty PROP_CIRCLE1 = new ValueProperty( TYPE, "Circle1" );
    
    Value<String> getCircle1();
    void setCircle1( String value );
    
    // *** circle2 ***
    
    @XmlBinding( path = "circle2" )
    
    ValueProperty PROP_CIRCLE2 = new ValueProperty( TYPE, "Circle2" );
    
    Value<String> getCircle2();
    void setCircle2( String value );

    // *** circle3 ***
    
    @XmlBinding( path = "circle3" )
    
    ValueProperty PROP_CIRCLE3 = new ValueProperty( TYPE, "Circle3" );
    
    Value<String> getCircle3();
    void setCircle3( String value );
}
