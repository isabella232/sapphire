/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.EditorPageDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "master details editor page" )
@Image( path = "MasterDetailsEditorPageDef.png" )
@XmlBinding( path = "editor-page" )

public interface MasterDetailsEditorPageDef extends EditorPageDef
{
    ElementType TYPE = new ElementType( MasterDetailsEditorPageDef.class );
    
    // *** OutlineHeaderText ***
    
    @Type( base = Function.class )
    @Label( standard = "outline header text" )
    @DefaultValue( text = "outline" )
    @Localizable
    @XmlBinding( path = "outline-header-text" )
    
    ValueProperty PROP_OUTLINE_HEADER_TEXT = new ValueProperty( TYPE, "OutlineHeaderText" );
    
    Value<Function> getOutlineHeaderText();
    void setOutlineHeaderText( String value );
    void setOutlineHeaderText( Function value );
    
    // *** InitialSelectionPath ***
    
    @Label( standard = "initial selection path" )
    @XmlBinding( path = "initial-selection" )
    
    ValueProperty PROP_INITIAL_SELECTION_PATH = new ValueProperty( TYPE, "InitialSelectionPath" );
    
    Value<String> getInitialSelectionPath();
    void setInitialSelectionPath( String initialSelectionPath );
    
    // *** RootNode ***

    @Type( base = MasterDetailsContentNodeDef.class )
    @XmlBinding( path = "root-node" )
    
    ImpliedElementProperty PROP_ROOT_NODE = new ImpliedElementProperty( TYPE, "RootNode" );

    MasterDetailsContentNodeDef getRootNode();
    
    // *** PersistedStateElementType ***
    
    @DefaultValue( text = "org.eclipse.sapphire.ui.forms.MasterDetailsEditorPageState" )
    @JavaTypeConstraint( kind = JavaTypeKind.INTERFACE, type = "org.eclipse.sapphire.ui.forms.MasterDetailsEditorPageState" )
    
    ValueProperty PROP_PERSISTENT_STATE_ELEMENT_TYPE = new ValueProperty( TYPE, EditorPageDef.PROP_PERSISTENT_STATE_ELEMENT_TYPE );
    
}
