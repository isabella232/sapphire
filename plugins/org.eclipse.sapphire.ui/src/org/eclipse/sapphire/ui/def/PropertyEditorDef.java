/******************************************************************************
 * Copyright (c) 2012 Oracle
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
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.FoldingXmlValueBindingImpl;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.internal.PropertyEditorDefMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "property editor" )
@Image( path = "PropertyEditorDef.gif" )
@GenerateImpl

public interface PropertyEditorDef extends FormPartDef
{
    ModelElementType TYPE = new ModelElementType( PropertyEditorDef.class );
    
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
    
    @Type( base = PropertyEditorDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "child-property", type = PropertyEditorDef.class ) )
    @Label( standard = "child properties" )
    
    ListProperty PROP_CHILD_PROPERTIES = new ListProperty( TYPE, "ChildProperties" );
    
    ModelElementList<PropertyEditorDef> getChildProperties();
    
    // *** Method : getChildPropertyEditor ***
    
    @DelegateImplementation( PropertyEditorDefMethods.class )
    
    PropertyEditorDef getChildPropertyEditor( ModelProperty property );
    
    // *** RelatedContent ***
    
    @Type
    ( 
        base = ISapphirePartDef.class,
        possible = 
        { 
            PropertyEditorDef.class, 
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
            PageBookExtDef.class,
            ISapphireTabGroupDef.class,
            HtmlPanelDef.class
        }
    )
                      
    @XmlListBinding
    ( 
        path = "related-content",
        mappings =
        {
            @XmlListBinding.Mapping( element = "property-editor", type = PropertyEditorDef.class ),
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
            @XmlListBinding.Mapping( element = "switching-panel", type = PageBookExtDef.class ),
            @XmlListBinding.Mapping( element = "tab-group", type = ISapphireTabGroupDef.class ),
            @XmlListBinding.Mapping( element = "html", type = HtmlPanelDef.class )
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
    
    // *** ShowLabel ***
    
    @Type( base = Boolean.class )
    @Label( standard = "show label" )
    @DefaultValue( text = "true" )
    @XmlBinding( path = "show-label" )
    
    @Documentation
    (
        content = "Indicates whether the property editor label should be shown. The label can be hidden to reduce UI " +
                  "clutter if the property editor is already adequately described by its context."
    )
    
    ValueProperty PROP_SHOW_LABEL = new ValueProperty( TYPE, "ShowLabel" );
    
    Value<Boolean> getShowLabel();
    void setShowLabel( String value );
    void setShowLabel( Boolean value );
    
    // *** Label ***
    
    @Label( standard = "label" )
    @Enablement( expr = "${ ShowLabel }" )
    @XmlBinding( path = "label" )
    
    @Documentation
    (
        content = "Overrides property editor label. By default, the property editor will use property's label from " +
        		  "model metadata."
    )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String value );
    
    // *** SpanBothColumns ***
    
    @Type( base = Boolean.class )
    @Label( standard = "span both columns" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "span" )
    
    @Documentation
    (
        content = "Indicates whether the body of the property editor should span both columns. If set to true, the " +
                  "property editor label will be shown above the body. The label itself would also span both columns."
    )
    
    ValueProperty PROP_SPAN_BOTH_COLUMNS = new ValueProperty( TYPE, "SpanBothColumns" );
    
    Value<Boolean> getSpanBothColumns();
    void setSpanBothColumns( String value );
    void setSpanBothColumns( Boolean value );
    
    // *** Width ***
    
    @Type( base = Integer.class )
    @Label( standard = "width" )
    @XmlBinding( path = "width" )
    
    @Documentation
    (
        content = "Specifies the preferred width (in pixels) for the body of the property editor. The width preference " +
                  "will be respected to the extent that it is feasible."
    )
    
    ValueProperty PROP_WIDTH = new ValueProperty( TYPE, "Width" );
    
    Value<Integer> getWidth();
    void setWidth( String value );
    void setWidth( Integer value );
    
    // *** Height ***
    
    @Type( base = Integer.class )
    @Label( standard = "height" )
    @XmlBinding( path = "height" )
    
    @Documentation
    (
        content = "Specifies the preferred height (in pixels) for the body of the property editor. The height preference " +
                  "will be respected to the extent that it is feasible. In particular, the height preference is ignored " +
                  "by property editors that cannot scale vertically."
    )
    
    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height" );
    
    Value<Integer> getHeight();
    void setHeight( String value );
    void setHeight( Integer value );
    
    // *** MarginLeft ***
    
    @Type( base = Integer.class )
    @Label( standard = "left margin" )
    @DefaultValue( text = "0" )
    @NumericRange( min = "0" )
    @XmlBinding( path = "margin-left" )
    
    @Documentation
    (
        content = "Specifies the preferred left margin (in pixels) for the body of the property editor. The left margin " +
                  "preference will be respected to the extent that it is feasible."
    )
    
    ValueProperty PROP_MARGIN_LEFT = new ValueProperty( TYPE, "MarginLeft" );
    
    Value<Integer> getMarginLeft();
    void setMarginLeft( String value );
    void setMarginLeft( Integer value );

}
