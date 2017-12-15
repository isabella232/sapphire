/******************************************************************************
 * Copyright (c) 2013 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.misc.t0008;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@GenerateImpl
@XmlBinding( path = "root" )

public interface TestRootElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestRootElement.class );
    
    // *** Children ***
    
    @Type( base = TestChildElement.class )
    @XmlListBinding( path = "children", mappings = @XmlListBinding.Mapping( element = "child", type = TestChildElement.class ) )
    
    ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
    
    ModelElementList<TestChildElement> getChildren();
    
    // *** ChildrenReadOnly ***
    
    @Type( base = TestChildElement.class )
    @ReadOnly
    @XmlListBinding( path = "children-read-only", mappings = @XmlListBinding.Mapping( element = "child", type = TestChildElement.class ) )
    
    ListProperty PROP_CHILDREN_READ_ONLY = new ListProperty( TYPE, "ChildrenReadOnly" );
    
    ModelElementList<TestChildElement> getChildrenReadOnly();
    
}
