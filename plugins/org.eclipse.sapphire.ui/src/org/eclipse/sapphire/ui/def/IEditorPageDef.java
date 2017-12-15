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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding

public interface IEditorPageDef

    extends IModelElementForXml, IRemovable
    
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
    @DefaultValue( "Design" )
    @XmlBinding( path = "page-name" )
    
    ValueProperty PROP_PAGE_NAME = new ValueProperty( TYPE, "PageName" );
    
    Value<String> getPageName();
    void setPageName( String pageName );
    
    // *** PageHeaderText ***
    
    @Label( standard = "page header text" )
    @DefaultValue( "Design View" )
    @XmlBinding( path = "page-header-text" )
    
    ValueProperty PROP_PAGE_HEADER_TEXT = new ValueProperty( TYPE, "PageHeaderText" );
    
    Value<String> getPageHeaderText();
    void setPageHeaderText( String pageHeaderText );
    
    // *** OutlineHeaderText ***
    
    @Label( standard = "outline header text" )
    @DefaultValue( "Outline" )
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
    
    // *** HelpContextId ***
    
    @Label( standard = "help context id" )
    @XmlBinding( path = "help-context-id" )
    
    ValueProperty PROP_HELP_CONTEXT_ID = new ValueProperty( TYPE, "HelpContextId" );
    
    Value<String> getHelpContextId();
    void setHelpContextId( String helpContextId );
    
    // *** RootNode ***

    @Type( base = IMasterDetailsTreeNodeDef.class )
    @XmlBinding( path = "root-node" )
    
    ElementProperty PROP_ROOT_NODE = new ElementProperty( TYPE, "RootNode" );

    IMasterDetailsTreeNodeDef getRootNode();
    IMasterDetailsTreeNodeDef getRootNode( boolean createIfNecessary );
    
    // *** HeaderActionSetDef ***
    
    @Type( base = IActionSetDef.class )
    @Label( standard = "header actions" )
    @XmlBinding( path = "header-actions" )
    
    ElementProperty PROP_HEADER_ACTION_SET_DEF = new ElementProperty( TYPE, "HeaderActionSetDef" );
    
    IActionSetDef getHeaderActionSetDef();
    IActionSetDef getHeaderActionSetDef( boolean createIfNecessary );
    
    // *** OutlineToolbarActionSetDef ***
    
    @Type( base = IActionSetDef.class )
    @Label( standard = "outline toolbar actions" )
    @XmlBinding( path = "outline-toolbar-actions" )
    
    ElementProperty PROP_OUTLINE_TOOLBAR_ACTION_SET_DEF = new ElementProperty( TYPE, "OutlineToolbarActionSetDef" );
    
    IActionSetDef getOutlineToolbarActionSetDef();
    IActionSetDef getOutlineToolbarActionSetDef( boolean createIfNecessary );

    // *** OutlineMenuActionSetDef ***
    
    @Type( base = IActionSetDef.class )
    @Label( standard = "outline menu actions" )
    @XmlBinding( path = "outline-menu-actions" )
    
    ElementProperty PROP_OUTLINE_MENU_ACTION_SET_DEF = new ElementProperty( TYPE, "OutlineMenuActionSetDef" );
    
    IActionSetDef getOutlineMenuActionSetDef();
    IActionSetDef getOutlineMenuActionSetDef( boolean createIfNecessary );
    
}
