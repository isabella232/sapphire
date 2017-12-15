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

public interface TestXmlBinding0006A extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestXmlBinding0006A.class );
    
    // *** TestProperty ***

    @Type( base = TestXmlBinding0006A.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "foo:abc", type = TestXmlBinding0006A.class ) )
    
    ListProperty PROP_TEST_PROPERTY = new ListProperty( TYPE, "TestProperty" );
    
    ModelElementList<TestXmlBinding0006A> getTestProperty();
    
}
