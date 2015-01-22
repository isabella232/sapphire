/******************************************************************************
 * Copyright (c) 2015 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0003;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/xsd/0003" )
@XmlBinding( path = "element" )

public interface TestXmlXsd0003Element extends Element
{
    ElementType TYPE = new ElementType( TestXmlXsd0003Element.class );
    
    // *** circle ***
    
    @Type( base = TestXmlXsd0003Circle.class )
    @XmlBinding( path = "circle" )
    
    ImpliedElementProperty PROP_CIRCLE = new ImpliedElementProperty( TYPE, "Circle" );
    
    TestXmlXsd0003Circle getCircle();
    
    // *** square ***
    
    @Type( base = TestXmlXsd0003Square.class )
    @XmlBinding( path = "square" )
    
    ImpliedElementProperty PROP_SQUARE = new ImpliedElementProperty( TYPE, "Square" );
    
    TestXmlXsd0003Square getSquare();
    
    // *** Aaa ***
    
    @XmlBinding( path = "aaa" )
    
    ValueProperty PROP_AAA = new ValueProperty( TYPE, "Aaa" );
    
    Value<String> getAaa();
    void setAaa( String value );
    
    // *** Bbb ***
    
    @XmlBinding( path = "bbb" )
    
    ValueProperty PROP_BBB = new ValueProperty( TYPE, "Bbb" );
    
    Value<String> getBbb();
    void setBbb( String value );
    
    // *** CCC ***
    
    @XmlBinding( path = "ccc" )
    
    ValueProperty PROP_CCC = new ValueProperty( TYPE, "Ccc" );
    
    Value<String> getCcc();
    void setCcc( String value );
    
}
