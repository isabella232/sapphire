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

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "properties view contribution page" )
@GenerateImpl

public interface IPropertiesViewContributionPageDef extends FormDef
{
    ModelElementType TYPE = new ModelElementType( IPropertiesViewContributionPageDef.class );

    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
    @Localizable
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String value );
    void setLabel( Function value );
    
    // *** Image ***
    
    @Type( base = Function.class )
    @Label( standard = "image" )
    @XmlBinding( path = "image" )
    
    ValueProperty PROP_IMAGE = new ValueProperty( TYPE, "Image" );
    
    Value<Function> getImage();
    void setImage( String value );
    void setImage( Function value );
    
    // *** VisibleWhen ***
    
    @Type( base = Function.class )
    @Label( standard = "visible when" )
    @XmlBinding( path = "visible-when" )
    
    ValueProperty PROP_VISIBLE_WHEN = new ValueProperty( TYPE, "VisibleWhen" );
    
    Value<Function> getVisibleWhen();
    void setVisibleWhen( String value );
    void setVisibleWhen( Function value );        
    
}
