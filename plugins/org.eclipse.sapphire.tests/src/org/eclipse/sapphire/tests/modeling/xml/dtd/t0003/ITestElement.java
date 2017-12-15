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

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0003;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@XmlRootBinding( elementName = "root", schemaLocation = "TestXmlDtd0003.dtd" )
@GenerateImpl

public interface ITestElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ITestElement.class );
    
    // *** AAA ***
    
    @Type( base = ITestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "aaa", type = ITestElementChild.class ) )
    
    ListProperty PROP_AAA = new ListProperty( TYPE, "Aaa" );
    
    ModelElementList<ITestElementChild> getAaa();
    
    // *** BBB ***
    
    @Type( base = ITestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "bbb", type = ITestElementChild.class ) )
    
    ListProperty PROP_BBB = new ListProperty( TYPE, "Bbb" );
    
    ModelElementList<ITestElementChild> getBbb();
    
    // *** CCC ***
    
    @Type( base = ITestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "ccc", type = ITestElementChild.class ) )
    
    ListProperty PROP_CCC = new ListProperty( TYPE, "Ccc" );
    
    ModelElementList<ITestElementChild> getCcc();
    
}
