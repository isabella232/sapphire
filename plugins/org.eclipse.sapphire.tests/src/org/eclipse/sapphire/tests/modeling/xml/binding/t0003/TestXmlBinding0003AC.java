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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0003;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/w", prefix = "w" )

public interface TestXmlBinding0003AC extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestXmlBinding0003AC.class );
    
    // *** Aca ***
    
    @XmlBinding( path = "aca" )
    
    ValueProperty PROP_ACA = new ValueProperty( TYPE, "Aca" );
    
    Value<String> getAca();
    void setAca( String value );
    
    // *** Acb ***
    
    @XmlBinding( path = "w:acb" )
    
    ValueProperty PROP_ACB = new ValueProperty( TYPE, "Acb" );
    
    Value<String> getAcb();
    void setAcb( String value );
    
    // *** Acc ***

    @Type( base = TestXmlBinding0003ACC.class )
    @XmlElementBinding( path = "w:acc-list", mappings = @XmlElementBinding.Mapping( element = "acc", type = TestXmlBinding0003ACC.class ) )
    
    ElementProperty PROP_ACC = new ElementProperty( TYPE, "Acc" );
    
    ModelElementHandle<TestXmlBinding0003ACC> getAcc();

    // *** Acd ***

    @Type( base = TestXmlBinding0003ACD.class )
    @XmlListBinding( path = "w:acd-list", mappings = @XmlListBinding.Mapping( element = "acd", type = TestXmlBinding0003ACD.class ) )
    
    ListProperty PROP_ACD = new ListProperty( TYPE, "Acd" );
    
    ModelElementList<TestXmlBinding0003ACD> getAcd();
    
}
