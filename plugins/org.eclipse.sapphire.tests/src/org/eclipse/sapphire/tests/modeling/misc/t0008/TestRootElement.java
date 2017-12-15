/******************************************************************************
 * Copyright (c) 2016 Oracle and Accenture
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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@XmlBinding( path = "root" )

public interface TestRootElement extends Element
{
    ElementType TYPE = new ElementType( TestRootElement.class );
    
    // *** Children ***
    
    @Type( base = TestChildElement.class )
    @XmlListBinding( path = "children", mappings = @XmlListBinding.Mapping( element = "child", type = TestChildElement.class ) )
    
    ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
    
    ElementList<TestChildElement> getChildren();
    
    // *** ChildrenReadOnly ***
    
    @Type( base = TestChildElement.class )
    @ReadOnly
    @XmlListBinding( path = "children-read-only", mappings = @XmlListBinding.Mapping( element = "child", type = TestChildElement.class ) )
    
    ListProperty PROP_CHILDREN_READ_ONLY = new ListProperty( TYPE, "ChildrenReadOnly" );
    
    ElementList<TestChildElement> getChildrenReadOnly();
    
}
