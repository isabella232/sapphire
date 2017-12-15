/******************************************************************************
 *  Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0003;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface TestElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestElement.class );
    
    // *** AAA ***
    
    @Type( base = TestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "aaa", type = TestElementChild.class ) )
    
    ListProperty PROP_AAA = new ListProperty( TYPE, "Aaa" );
    
    ModelElementList<TestElementChild> getAaa();
    
    // *** BBB ***
    
    @Type( base = TestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "bbb", type = TestElementChild.class ) )
    
    ListProperty PROP_BBB = new ListProperty( TYPE, "Bbb" );
    
    ModelElementList<TestElementChild> getBbb();
    
    // *** CCC ***
    
    @Type( base = TestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "ccc", type = TestElementChild.class ) )
    
    ListProperty PROP_CCC = new ListProperty( TYPE, "Ccc" );
    
    ModelElementList<TestElementChild> getCcc();
    
}
