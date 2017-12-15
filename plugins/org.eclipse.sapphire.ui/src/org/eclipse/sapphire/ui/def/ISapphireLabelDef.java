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
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "label" )
@GenerateImpl

public interface ISapphireLabelDef

    extends FormPartDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireLabelDef.class );
 
    // *** Text ***
    
    @Type( base = Function.class )
    @Label( standard = "text" )
    @Required
    @LongString
    @Localizable
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "" )
    
    ValueProperty PROP_TEXT = new ValueProperty( TYPE, "Text" );
    
    Value<Function> getText();
    void setText( String text );
    void setText( Function text );
}
