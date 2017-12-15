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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValuePropertyCustomBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.ui.def.internal.PropertyEditorPropertyBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "property editor" )
@GenerateXmlBinding

public interface ISapphirePropertyEditorDef

    extends ISapphirePartDef, ISapphirePropertyMetadata
    
{
    ModelElementType TYPE = new ModelElementType( ISapphirePropertyEditorDef.class );
    
    // *** Property ***
    
    @Label( standard = "property" )
    @NonNullValue
    @ValuePropertyCustomBinding( impl = PropertyEditorPropertyBinding.class )
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String property );
    
    // *** ChildProperties ***
    
    @Type( base = ISapphireChildPropertyInfo.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "child-property", type = ISapphireChildPropertyInfo.class ) } )
    @Label( standard = "child properties" )
    
    ListProperty PROP_CHILD_PROPERTIES = new ListProperty( TYPE, "ChildProperties" );
    
    ModelElementList<ISapphireChildPropertyInfo> getChildProperties();
    
    // *** AuxPropertyEditors ***
    
    @Type( base = ISapphirePropertyEditorDef.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "aux-property-editor", type = ISapphirePropertyEditorDef.class ) } )
                             
    ListProperty PROP_AUX_PROPERTY_EDITORS = new ListProperty( TYPE, "AuxPropertyEditors" );
    
    ModelElementList<ISapphirePropertyEditorDef> getAuxPropertyEditors();
    
}
