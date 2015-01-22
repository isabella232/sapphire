/******************************************************************************
 *  Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0003;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** AAA ***
    
    @Type( base = TestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "aaa", type = TestElementChild.class ) )
    
    ListProperty PROP_AAA = new ListProperty( TYPE, "Aaa" );
    
    ElementList<TestElementChild> getAaa();
    
    // *** BBB ***
    
    @Type( base = TestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "bbb", type = TestElementChild.class ) )
    
    ListProperty PROP_BBB = new ListProperty( TYPE, "Bbb" );
    
    ElementList<TestElementChild> getBbb();
    
    // *** CCC ***
    
    @Type( base = TestElementChild.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "ccc", type = TestElementChild.class ) )
    
    ListProperty PROP_CCC = new ListProperty( TYPE, "Ccc" );
    
    ElementList<TestElementChild> getCcc();
    
}
