/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0002;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface TestXmlBinding0002ModelElement extends Element
{
    ElementType TYPE = new ElementType( TestXmlBinding0002ModelElement.class );
    
    // *** TestProperty ***

    @Type( base = TestXmlBinding0002ModelElement.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "", type = TestXmlBinding0002ModelElement.class ) )
    
    ListProperty PROP_TEST_PROPERTY = new ListProperty( TYPE, "TestProperty" );
    
    ElementList<TestXmlBinding0002ModelElement> getTestProperty();
    
}
