/******************************************************************************
 * Copyright (c) 2012 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.samples.architecture;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@GenerateImpl
@XmlBinding( path = "architecture" )

public interface IArchitecture extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( IArchitecture.class );
    
    // *** Description ***
    
    @LongString
    @XmlBinding( path = "description" )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** Components ***
    
    @Type( base = IComponent.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "component", type = IComponent.class ) )
    
    ListProperty PROP_COMPONENTS = new ListProperty( TYPE, "Components" );
    
    ModelElementList<IComponent> getComponents();
    
    // *** DetailLevel ***
    
    @Type( base = Integer.class )
    @Label( standard = "detail level" )
    @DefaultValue( text = "1" )
    @NumericRange( min = "1" )
    
    ValueProperty PROP_DETAIL_LEVEL = new ValueProperty( TYPE, "DetailLevel" );
    
    Value<Integer> getDetailLevel();
    void setDetailLevel( String value );
    void setDetailLevel( Integer value );
    
}
