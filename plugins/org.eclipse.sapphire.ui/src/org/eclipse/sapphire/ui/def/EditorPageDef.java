/******************************************************************************
 * Copyright (c) 2014 Oracle and Other Contributors
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

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "editor page" )

public interface EditorPageDef extends PartDef
{
    ElementType TYPE = new ElementType( EditorPageDef.class );
    
    // *** ElementType ***
    
    @Required
    
    ValueProperty PROP_ELEMENT_TYPE = new ValueProperty( TYPE, PartDef.PROP_ELEMENT_TYPE );

    // *** PageName ***
    
    @Label( standard = "page name" )
    @DefaultValue( text = "design" )
    @Localizable
    @XmlBinding( path = "page-name" )
    
    ValueProperty PROP_PAGE_NAME = new ValueProperty( TYPE, "PageName" );
    
    Value<String> getPageName();
    void setPageName( String pageName );
    
    // *** PageHeaderText ***
    
    @Type( base = Function.class )
    @Label( standard = "page header text" )
    @DefaultValue( text = "design view" )
    @Localizable
    @XmlBinding( path = "page-header-text" )
    
    ValueProperty PROP_PAGE_HEADER_TEXT = new ValueProperty( TYPE, "PageHeaderText" );
    
    Value<Function> getPageHeaderText();
    void setPageHeaderText( String value );
    void setPageHeaderText( Function value );
    
    // *** PageHeaderImage ***
    
    @Type( base = Function.class )
    @Label( standard = "page header image" )
    @XmlBinding( path = "page-header-image" )
    
    ValueProperty PROP_PAGE_HEADER_IMAGE = new ValueProperty( TYPE, "PageHeaderImage" );
    
    Value<Function> getPageHeaderImage();
    void setPageHeaderImage( String value );
    void setPageHeaderImage( Function value );
    
    // *** PersistentStateElementType ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "persistent state element type" )
    @DefaultValue( text = "org.eclipse.sapphire.ui.EditorPageState" )
    @JavaTypeConstraint( kind = JavaTypeKind.INTERFACE, type = "org.eclipse.sapphire.ui.EditorPageState" )
    @MustExist
    @XmlBinding( path = "persistent-state-element-type" )
    
    @Documentation
    (
        content = "The element type used for persisting editor page state. Editor pages are able to persist user " +
                  "interface state between sessions independent of the data that is being edited. What state is " +
                  "persisted is dependent on editor page type. Two common examples of persistent state are sizing " +
                  "of resizable elements and selection." +
                  "[pbr/]" +
                  "Adopters can extend editor page state to store state specific to a particular editor implementation."
    )
    
    ValueProperty PROP_PERSISTENT_STATE_ELEMENT_TYPE = new ValueProperty( TYPE, "PersistentStateElementType" );
    
    ReferenceValue<JavaTypeName,JavaType> getPersistentStateElementType();
    void setPersistentStateElementType( String value );
    void setPersistentStateElementType( JavaTypeName value );
    
}
