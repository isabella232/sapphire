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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0008;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestModelChild extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestModelChild.class );
    
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
    
    // *** Ccc ***
    
    @XmlBinding( path = "@ccc" )
    
    ValueProperty PROP_CCC = new ValueProperty( TYPE, "Ccc" );
    
    Value<String> getCcc();
    void setCcc( String value );
    
}
