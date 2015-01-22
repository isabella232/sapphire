/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0002;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public interface TestXmlXsd0002Element2b extends Element
{
    ElementType TYPE = new ElementType( TestXmlXsd0002Element2b.class );
    
    // *** Aaa2 ***
    
    @XmlBinding( path = "aaa-2b" )
    
    ValueProperty PROP_AAA2 = new ValueProperty( TYPE, "Aaa2" );
    
    Value<String> getAaa2();
    void setAaa2( String value );

    // *** Bbb2 ***
    
    @XmlBinding( path = "bbb-2b" )
    
    ValueProperty PROP_BBB2 = new ValueProperty( TYPE, "Bbb2" );
    
    Value<String> getBbb2();
    void setBbb2( String value );
    
    // *** CCC2 ***
    
    @XmlBinding( path = "ccc-2c" )
    
    ValueProperty PROP_CCC2 = new ValueProperty( TYPE, "Ccc2" );
    
    Value<String> getCcc2();
    void setCcc2( String value );
    
}
