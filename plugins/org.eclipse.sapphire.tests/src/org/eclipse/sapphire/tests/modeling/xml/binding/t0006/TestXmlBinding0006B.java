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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0006;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestXmlBinding0006B extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestXmlBinding0006B.class );
    
    // *** TestProperty ***

    @Type( base = TestXmlBinding0006B.class )
    @XmlListBinding( path = "foo:abc", mappings = @XmlListBinding.Mapping( element = "element", type = TestXmlBinding0006B.class ) )
    
    ListProperty PROP_TEST_PROPERTY = new ListProperty( TYPE, "TestProperty" );
    
    ModelElementList<TestXmlBinding0006B> getTestProperty();
    
}
