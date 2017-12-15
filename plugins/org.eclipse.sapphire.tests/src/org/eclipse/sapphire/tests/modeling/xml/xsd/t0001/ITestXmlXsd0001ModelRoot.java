/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0001;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

@XmlRootBinding( namespace = "http://www.eclipse.org/sapphire/tests/xml/xsd/0001",
                 elementName = "root" )

public interface ITestXmlXsd0001ModelRoot

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ITestXmlXsd0001ModelRoot.class );
    
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
