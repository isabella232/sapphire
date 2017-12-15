/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails.def;

import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.IEditorPageDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "master details editor page" )
@GenerateImpl

public interface IMasterDetailsEditorPageDef

    extends IEditorPageDef
    
{
    ModelElementType TYPE = new ModelElementType( IMasterDetailsEditorPageDef.class );
    
    // *** OutlineHeaderText ***
    
    @Label( standard = "outline header text" )
    @DefaultValue( text = "outline" )
    @Localizable
    @XmlBinding( path = "outline-header-text" )
    
    ValueProperty PROP_OUTLINE_HEADER_TEXT = new ValueProperty( TYPE, "OutlineHeaderText" );
    
    Value<String> getOutlineHeaderText();
    void setOutlineHeaderText( String outlineHeaderText );
    
    // *** InitialSelectionPath ***
    
    @Label( standard = "initial selection path" )
    @XmlBinding( path = "initial-selection" )
    
    ValueProperty PROP_INITIAL_SELECTION_PATH = new ValueProperty( TYPE, "InitialSelectionPath" );
    
    Value<String> getInitialSelectionPath();
    void setInitialSelectionPath( String initialSelectionPath );
    
    // *** RootNode ***

    @Type( base = IMasterDetailsContentNodeDef.class )
    @XmlBinding( path = "root-node" )
    
    ImpliedElementProperty PROP_ROOT_NODE = new ImpliedElementProperty( TYPE, "RootNode" );

    IMasterDetailsContentNodeDef getRootNode();
    
}
