/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Ling Hao - [44319] Image specification for diagram parts inconsistent with the rest of sdef 
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.diagram.def.internal.ToolPaletteCompartmentPossibleValuesService;
import org.eclipse.sapphire.ui.diagram.shape.def.ImageDef;
import org.eclipse.sapphire.ui.diagram.shape.def.RectangleDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SelectionPresentation;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.TextDef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "diagram node" )

public interface IDiagramNodeDef extends PartDef, IDiagramDimension, PropertiesViewContributorDef
{
    ElementType TYPE = new ElementType( IDiagramNodeDef.class );
    
    // *** Id ***
    
    @Required
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, PartDef.PROP_ID );
    
    // *** InstanceId ***
    
    @Type( base = Function.class )
    @Label( standard = "instance ID" )
    @XmlBinding( path = "instance-id" )
    
    ValueProperty PROP_INSTANCE_ID = new ValueProperty( TYPE, "InstanceId" );
    
    Value<Function> getInstanceId();
    void setInstanceId( String value );
    void setInstanceId( Function value );
        
    // *** Shape ***
    
    @Type
    ( 
        base = ShapeDef.class, 
        possible = 
        { 
            TextDef.class, 
            ImageDef.class,
            RectangleDef.class
        }
    )    
    @Label( standard = "shape" )
    @XmlElementBinding
    ( 
    	mappings = 
        {
            @XmlElementBinding.Mapping( element = "text", type = TextDef.class ),
            @XmlElementBinding.Mapping( element = "image", type = ImageDef.class ),
            @XmlElementBinding.Mapping( element = "rectangle", type = RectangleDef.class )
        }
    )
    
    ElementProperty PROP_SHAPE = new ElementProperty( TYPE, "Shape" );
    
    ElementHandle<ShapeDef> getShape(); 
    
    // *** ToolPaletteLabel ***
    
    @Label( standard = "tool palette item label" )
    @Required
    @Localizable
    @XmlBinding( path = "tool-palette-label" )
    
    ValueProperty PROP_TOOL_PALETTE_LABEL = new ValueProperty( TYPE, "ToolPaletteLabel" );
    
    Value<String> getToolPaletteLabel();
    void setToolPaletteLabel( String paletteLabel );
    
    // *** ToolPaletteDescription ***
    
    @Label( standard = "tool palette item description" )
    @LongString
    @Localizable
    @XmlBinding( path = "tool-palette-desc" )
    
    ValueProperty PROP_TOOL_PALETTE_DESCRIPTION = new ValueProperty( TYPE, "ToolPaletteDescription" );
    
    Value<String> getToolPaletteDescription();
    void setToolPaletteDescription( String paletteDesc );
    
    // *** ToolPaletteImage ***

    @Type( base = Function.class )
    @Label( standard = "tool palette item image" )
    @XmlBinding( path = "tool-palette-image" )

    ValueProperty PROP_TOOL_PALETTE_IMAGE = new ValueProperty( TYPE, "ToolPaletteImage" );
    
    Value<Function> getToolPaletteImage();
    void setToolPaletteImage( String value );
    void setToolPaletteImage( Function value );
        
    // *** ToolPaletteCompartment ***

    @Label( standard = "tool palette compartment" )
    @XmlBinding( path = "tool-palette-compartment" )    
    @DefaultValue( text = "Sapphire.Diagram.Palette.Nodes" )
    @Service( impl = ToolPaletteCompartmentPossibleValuesService.class )
    
    ValueProperty PROP_TOOL_PALETTE_COMPARTMENT = new ValueProperty( TYPE, "ToolPaletteCompartment" );
    
    Value<String> getToolPaletteCompartment();
    void setToolPaletteCompartment( String value );
    
    // *** Property ***
    
    @Label( standard = "property" )
    @XmlBinding( path = "property" )
    @Required
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String property );
        
    // *** ElementType ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "model element type" )
    @JavaTypeConstraint( kind = JavaTypeKind.INTERFACE, type = "org.eclipse.sapphire.Element" )
    @MustExist
    @XmlBinding( path = "model-element-type" )
    
    ValueProperty PROP_ELEMENT_TYPE = new ValueProperty( TYPE, "ElementType" );
    
    ReferenceValue<JavaTypeName,JavaType> getElementType();
    void setElementType( String value );
    void setElementType( JavaTypeName value );
    
    // *** Resizable ***
    
    @Type( base = Boolean.class )
    @Label( standard = "resizable" )
    @XmlBinding( path = "resizable" )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_RESIZABLE = new ValueProperty( TYPE, "Resizable" );
    
    Value<Boolean> isResizable();
    void setResizable( String value );
    void setResizable( Boolean value );
        
    // *** EmbeddedConnections ***
    
    @Type( base = IDiagramExplicitConnectionBindingDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "connection-binding", type = IDiagramExplicitConnectionBindingDef.class ) )

    ListProperty PROP_EMBEDDED_CONNECTIONS = new ListProperty( TYPE, "EmbeddedConnections" );
    
    ElementList<IDiagramExplicitConnectionBindingDef> getEmbeddedConnections();
    
    // *** SelectionPresentation ***
    
    @Type( base = SelectionPresentation.class )
    @Label( standard = "selection presentation" )
    @XmlBinding( path = "selection-presentation" )

    ImpliedElementProperty PROP_SELECTION_PRESENTATION = new ImpliedElementProperty( TYPE, "SelectionPresentation" );
    
    SelectionPresentation getSelectionPresentation();

}
