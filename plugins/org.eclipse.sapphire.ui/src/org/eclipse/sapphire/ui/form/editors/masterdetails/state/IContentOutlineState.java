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

package org.eclipse.sapphire.ui.form.editors.masterdetails.state;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IContentOutlineState extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( IContentOutlineState.class );
    
    // *** Visible ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "true" )
    @XmlBinding( path = "visible" )
    
    ValueProperty PROP_VISIBLE = new ValueProperty( TYPE, "Visible" );
    
    Value<Boolean> getVisible();
    void setVisible( String value );
    void setVisible( Boolean value );
    
    // *** Ratio ***
    
    @Type( base = Double.class )
    @DefaultValue( text = "0.3" )
    @XmlBinding( path = "ratio" )

    ValueProperty PROP_RATIO = new ValueProperty( TYPE, "Ratio" );
    
    Value<Double> getRatio();
    void setRatio( String value );
    void setRatio( Double value );
    
    // *** Root ***
    
    @Type( base = IContentOutlineNodeState.class )
    @XmlBinding( path = "root" )

    ImpliedElementProperty PROP_ROOT = new ImpliedElementProperty( TYPE, "Root" );
    
    IContentOutlineNodeState getRoot();
    
}
