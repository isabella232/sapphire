/******************************************************************************
 * Copyright (c) 2013 Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Greg Amerson - [343972] Support image in editor page header
 *******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "form editor page" )

public interface IEditorPageDef

    extends PartDef
    
{
    ModelElementType TYPE = new ModelElementType( IEditorPageDef.class );
    
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
    
    // *** PageHeaderImage ***
    
    @Type( base = Function.class )
    @Label( standard = "page header image" )
    @XmlBinding( path = "page-header-image" )
    
    ValueProperty PROP_PAGE_HEADER_IMAGE = new ValueProperty( TYPE, "PageHeaderImage" );
    
    Value<Function> getPageHeaderImage();
    void setPageHeaderImage( String value );
    void setPageHeaderImage( Function value );
    
}
