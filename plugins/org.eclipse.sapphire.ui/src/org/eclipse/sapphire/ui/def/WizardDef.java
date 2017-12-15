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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "wizard" )
@GenerateImpl

public interface WizardDef extends PartDef
{
    ModelElementType TYPE = new ModelElementType( WizardDef.class );
    
    // *** ElementType ***
    
    @Required
    
    ValueProperty PROP_ELEMENT_TYPE = new ValueProperty( TYPE, PartDef.PROP_ELEMENT_TYPE );

    // *** Label ***
    
    @Label( standard = "label" )
    @Localizable
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String label );
    
    // *** Description ***
    
    @Label( standard = "description" )
    @LongString
    @Localizable
    @XmlBinding( path = "description" )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String description );

    // *** Image ***
    
    @Type( base = Function.class )
    @Label( standard = "image" )
    @XmlBinding( path = "image" )
    
    ValueProperty PROP_IMAGE = new ValueProperty( TYPE, "Image" );
    
    Value<Function> getImage();
    void setImage( String value );
    void setImage( Function value );
    
    // *** Pages ***
    
    @Type( base = WizardPageDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "page", type = WizardPageDef.class ) )
                             
    ListProperty PROP_PAGES = new ListProperty( TYPE, "Pages" );
    
    ModelElementList<WizardPageDef> getPages();
    
}
