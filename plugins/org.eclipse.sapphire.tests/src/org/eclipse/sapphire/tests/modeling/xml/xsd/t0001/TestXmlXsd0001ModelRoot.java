/******************************************************************************
 * Copyright (c) 2014 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0001;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@XmlNamespace( uri="http://www.eclipse.org/sapphire/tests/xml/xsd/0001" )
@XmlBinding( path = "root" )

public interface TestXmlXsd0001ModelRoot extends Element
{
    ElementType TYPE = new ElementType( TestXmlXsd0001ModelRoot.class );
    
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
    
    // *** DDD ***
    
    @XmlBinding( path = "ddd" )
    
    ValueProperty PROP_DDD = new ValueProperty( TYPE, "Ddd" );
    
    Value<String> getDdd();
    void setDdd( String value );

}
