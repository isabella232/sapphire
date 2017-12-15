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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IEditorPageDef

    extends ISapphirePartDef
    
{
    ModelElementType TYPE = new ModelElementType( IEditorPageDef.class );
    
    // *** Id ***
    
    @Label( standard = "ID" )
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String id );
    
    // *** PageName ***
    
    @Label( standard = "page name" )
    @DefaultValue( text = "design" )
    @Localizable
    @XmlBinding( path = "page-name" )
    
    ValueProperty PROP_PAGE_NAME = new ValueProperty( TYPE, "PageName" );
    
    Value<String> getPageName();
    void setPageName( String pageName );
    
    // *** PageHeaderText ***
    
    @Label( standard = "page header text" )
    @DefaultValue( text = "design view" )
    @Localizable
    @XmlBinding( path = "page-header-text" )
    
    ValueProperty PROP_PAGE_HEADER_TEXT = new ValueProperty( TYPE, "PageHeaderText" );
    
    Value<String> getPageHeaderText();
    void setPageHeaderText( String pageHeaderText );
    
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

    @Type( base = IMasterDetailsTreeNodeDef.class )
    @XmlBinding( path = "root-node" )
    
    ElementProperty PROP_ROOT_NODE = new ElementProperty( TYPE, "RootNode" );

    ModelElementHandle<IMasterDetailsTreeNodeDef> getRootNode();
    
}
