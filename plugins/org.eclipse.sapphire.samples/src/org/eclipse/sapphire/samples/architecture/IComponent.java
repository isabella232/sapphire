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

package org.eclipse.sapphire.samples.architecture;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IComponent extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( IComponent.class );
    
    // *** Name ***
    
    @XmlBinding( path = "name" )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String value );
    
    // *** Description ***
    
    @LongString
    @XmlBinding( path = "description" )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** Dependencies ***
    
    @Type( base = IComponentDependency.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "dependency", type = IComponentDependency.class ) )

    ListProperty PROP_DEPENDENCIES = new ListProperty( TYPE, "Dependencies" );
    
    ModelElementList<IComponentDependency> getDependencies();
    
    // *** Provider ***
    
    @Label( standard = "provider" )
    @XmlBinding( path = "provider" )
    
    ValueProperty PROP_PROVIDER = new ValueProperty( TYPE, "Provider" );
    
    Value<String> getProvider();
    void setProvider( String value );
    
    // *** Copyright ***
    
    @Label( standard = "copyright" )
    @LongString
    @XmlBinding( path = "copyright" )
    
    ValueProperty PROP_COPYRIGHT = new ValueProperty( TYPE, "Copyright" );
    
    Value<String> getCopyright();
    void setCopyright( String value );

}
