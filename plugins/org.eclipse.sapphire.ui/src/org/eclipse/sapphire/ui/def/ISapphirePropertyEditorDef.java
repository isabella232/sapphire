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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.FoldingXmlValueBindingImpl;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "property editor" )
@Image( path = "PropertyEditorDef.gif" )
@GenerateImpl

public interface ISapphirePropertyEditorDef

    extends IFormPartDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphirePropertyEditorDef.class );
    
    String HINT_CHECKBOX_LAYOUT = "checkbox.layout";
    String HINT_VALUE_CHECKBOX_LAYOUT_LEADING_LABEL = "leading.label";
    String HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL = "trailing.label";
    String HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL_INDENTED = "trailing.label.idented";
    
    // *** Property ***
    
    @Label( standard = "property" )
    @Required
    @CustomXmlValueBinding( impl = FoldingXmlValueBindingImpl.class, params = "property" )
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String property );
    
    // *** ChildProperties ***
    
    @Type( base = ISapphirePropertyEditorDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "child-property", type = ISapphirePropertyEditorDef.class ) )
    @Label( standard = "child properties" )
    
    ListProperty PROP_CHILD_PROPERTIES = new ListProperty( TYPE, "ChildProperties" );
    
    ModelElementList<ISapphirePropertyEditorDef> getChildProperties();
    
    // *** RelatedContent ***
    
    @Type
    ( 
        base = ISapphirePartDef.class,
        possible = 
        { 
            ISapphirePropertyEditorDef.class, 
            ISapphireSeparatorDef.class,
            ISapphireSpacerDef.class,
            ISapphireLabelDef.class,
            ISapphireGroupDef.class,
            ISapphireWithDirectiveDef.class,
            ISapphireIfElseDirectiveDef.class,
            ISapphireCompositeDef.class,
            ISapphireActionLinkDef.class,
            ISapphireCustomPartDef.class,
            ISapphireStaticTextFieldDef.class,
            ISapphirePageBookExtDef.class,
            ISapphireTabGroupDef.class,
            ISapphireHtmlPanelDef.class
        }
    )
                      
    @XmlListBinding
    ( 
        path = "related-content",
        mappings =
        {
            @XmlListBinding.Mapping( element = "property-editor", type = ISapphirePropertyEditorDef.class ),
            @XmlListBinding.Mapping( element = "separator", type = ISapphireSeparatorDef.class ),
            @XmlListBinding.Mapping( element = "spacer", type = ISapphireSpacerDef.class ),
            @XmlListBinding.Mapping( element = "label", type = ISapphireLabelDef.class ),
            @XmlListBinding.Mapping( element = "group", type = ISapphireGroupDef.class ),
            @XmlListBinding.Mapping( element = "with", type = ISapphireWithDirectiveDef.class ),
            @XmlListBinding.Mapping( element = "if", type = ISapphireIfElseDirectiveDef.class ),
            @XmlListBinding.Mapping( element = "composite", type = ISapphireCompositeDef.class ),
            @XmlListBinding.Mapping( element = "action-link", type = ISapphireActionLinkDef.class ),
            @XmlListBinding.Mapping( element = "custom", type = ISapphireCustomPartDef.class ),
            @XmlListBinding.Mapping( element = "read-only-text", type = ISapphireStaticTextFieldDef.class ),
            @XmlListBinding.Mapping( element = "switching-panel", type = ISapphirePageBookExtDef.class ),
            @XmlListBinding.Mapping( element = "tab-group", type = ISapphireTabGroupDef.class ),
            @XmlListBinding.Mapping( element = "html", type = ISapphireHtmlPanelDef.class )
        }
    )
                             
    ListProperty PROP_RELATED_CONTENT = new ListProperty( TYPE, "RelatedContent" );
    
    ModelElementList<ISapphirePartDef> getRelatedContent();
    
    // *** RelatedContentWidth ***
    
    @Type( base = Integer.class )
    @Label( standard = "related content width" )
    @DefaultValue( text = "40" )
    @NumericRange( min = "20", max = "80" )
    @XmlBinding( path = "related-content-width" )
    
    ValueProperty PROP_RELATED_CONTENT_WIDTH = new ValueProperty( TYPE, "RelatedContentWidth" );
    
    Value<Integer> getRelatedContentWidth();
    void setRelatedContentWidth( String value );
    void setRelatedContentWidth( Integer value );
    
}
