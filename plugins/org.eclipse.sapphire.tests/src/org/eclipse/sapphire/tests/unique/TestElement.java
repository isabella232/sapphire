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

package org.eclipse.sapphire.tests.unique;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlBinding( path = "root" )

public interface TestElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestElement.class );
    
    // *** List ***
    
    @GenerateImpl
    @XmlBinding( path = "entry" )
    
    interface ListEntry extends IModelElement
    {
        ModelElementType TYPE = new ModelElementType( ListEntry.class );
        
        // *** Value ***
        
        @NoDuplicates
        @XmlBinding( path = "" )
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
    }
    
    @Type( base = ListEntry.class )
    @XmlListBinding( path = "" )

    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ModelElementList<ListEntry> getList();
    
}
