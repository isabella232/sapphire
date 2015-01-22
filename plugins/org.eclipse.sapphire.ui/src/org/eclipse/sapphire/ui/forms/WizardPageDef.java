/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "wizard page" )
@Image( path = "WizardPageDef.png" )

public interface WizardPageDef extends CompositeDef
{
    ElementType TYPE = new ElementType( WizardPageDef.class );
    
    // *** Id ***
    
    @Required
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, PartDef.PROP_ID );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String label );
    void setLabel( Function label );
    
    // *** Description ***
    
    @Type( base = Function.class )
    @Label( standard = "description" )
    @LongString
    @XmlBinding( path = "description" )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<Function> getDescription();
    void setDescription( String description );
    void setDescription( Function description );
    
    // *** Image ***
    
    @Type( base = Function.class )
    @Label( standard = "image" )
    @XmlBinding( path = "image" )
    
    ValueProperty PROP_IMAGE = new ValueProperty( TYPE, "Image" );
    
    Value<Function> getImage();
    void setImage( String value );
    void setImage( Function value );
    
    // *** InitialFocus ***
    
    @Label( standard = "initial focus" )
    @XmlBinding( path = "initial-focus" )
    
    ValueProperty PROP_INITIAL_FOCUS = new ValueProperty( TYPE, "InitialFocus" );
    
    Value<String> getInitialFocus();
    void setInitialFocus( String value );
    
}
