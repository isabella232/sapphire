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
 *    Ling Hao - [344319] Image specification for diagram parts inconsistent with the rest of sdef
 *    Konstantin Komissarchik - [382431] Inconsistent terminology: layout storage and layout persistence
 *    Konstantin Komissarchik - [382449] Support EL in EditorPageDef.PageHeaderText 
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "diagram editor page" )
@Image( path = "DiagramEditorPageDef.png" )
@XmlBinding( path = "diagram-page" )

public interface DiagramEditorPageDef extends EditorPageDef, PropertiesViewContributorDef
{
    ElementType TYPE = new ElementType( DiagramEditorPageDef.class);
    
    // *** GridDefinition ***
    
    @Type( base = IDiagramGridDef.class )
    @XmlBinding( path = "grid")
    
    ImpliedElementProperty PROP_GRID_DEFINITION = new ImpliedElementProperty( TYPE, "GridDefinition" );

    IDiagramGridDef getGridDefinition();    
    
    // *** GuidesDefinition ***
    
    @Type( base = IDiagramGuidesDef.class )
    @XmlBinding( path = "guides")
    
    ImpliedElementProperty PROP_GUIDES_DEFINITION = new ImpliedElementProperty( TYPE, "GuidesDefinition" );

    IDiagramGuidesDef getGuidesDefinition(); 
    
    // *** PersistentStateElementType ***
    
    @DefaultValue( text = "org.eclipse.sapphire.ui.diagram.state.DiagramEditorPageState" )
    @JavaTypeConstraint( kind = JavaTypeKind.INTERFACE, type = "org.eclipse.sapphire.ui.diagram.state.DiagramEditorPageState" )
    
    ValueProperty PROP_PERSISTENT_STATE_ELEMENT_TYPE = new ValueProperty( TYPE, EditorPageDef.PROP_PERSISTENT_STATE_ELEMENT_TYPE );
    
    // *** LayoutPersistence ***
    
    @Type( base = LayoutPersistence.class )
    @Label( standard = "layout persistence")
    @Localizable
    @XmlBinding( path = "layout-persistence" )
    @DefaultValue( text = "workspace" )
    
    ValueProperty PROP_LAYOUT_PERSISTENCE = new ValueProperty( TYPE, "LayoutPersistence" );
    
    Value<LayoutPersistence> getLayoutPersistence();
    void setLayoutPersistence( String value );
    void setLayoutPersistence( LayoutPersistence value );
    
    // *** ConnectionServiceType ***
    
    @Type( base = ConnectionServiceType.class )
    @Label( standard = "connection service type")
    @Localizable
    @XmlBinding( path = "connection-service-type" )
    @DefaultValue( text = "standard" )
    
    ValueProperty PROP_CONNECTION_SERVICE_TYPE = new ValueProperty( TYPE, "ConnectionServiceType" );
    
    Value<ConnectionServiceType> getConnectionServiceType();
    void setConnectionServiceType( String value );
    void setConnectionServiceType( ConnectionServiceType value );

    // *** PaletteCompartments ***
    
    @Type( base = IDiagramPaletteCompartmentDef.class )
    @Label( standard = "palette compartments")
    @XmlListBinding( path = "palette", mappings = @XmlListBinding.Mapping( element = "compartment", type = IDiagramPaletteCompartmentDef.class ) )
                             
    ListProperty PROP_PALETTE_COMPARTMENTS = new ListProperty( TYPE, "PaletteCompartments" );
    
    ElementList<IDiagramPaletteCompartmentDef> getPaletteCompartments();
    
    // *** Property ***
    
    @Label( standard = "property" )
    @XmlBinding( path = "property" )
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String property );
    
    // *** DiagramNodeDefs ***
    
    @Type( base = IDiagramNodeDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "node", type = IDiagramNodeDef.class ) )
                             
    ListProperty PROP_DIAGRAM_NODE_DEFS = new ListProperty( TYPE, "DiagramNodeDefs" );
    
    ElementList<IDiagramNodeDef> getDiagramNodeDefs();
    
    // *** DiagramConnectionDefs ***
    
    @Type( base = IDiagramConnectionDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "connection", type = IDiagramConnectionDef.class ) )
                             
    ListProperty PROP_DIAGRAM_CONNECTION_DEFS = new ListProperty( TYPE, "DiagramConnectionDefs" );
    
    ElementList<IDiagramConnectionDef> getDiagramConnectionDefs();
    
    // *** DiagramConnectionBindingDefs ***
    
    @Type( base = IDiagramExplicitConnectionBindingDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "connection-binding", type = IDiagramExplicitConnectionBindingDef.class ) )
                             
    ListProperty PROP_DIAGRAM_CONNECTION_BINDING_DEFS = new ListProperty( TYPE, "DiagramConnectionBindingDefs" );
    
    ElementList<IDiagramExplicitConnectionBindingDef> getDiagramConnectionBindingDefs();
    
    // *** ImplicitConnectionBindingDefs ***
    
    @Type( base = IDiagramImplicitConnectionBindingDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "implicit-connection-binding", type = IDiagramImplicitConnectionBindingDef.class ) )
                             
    ListProperty PROP_IMPLICIT_CONNECTION_BINDING_DEFS = new ListProperty( TYPE, "ImplicitConnectionBindingDefs" );
    
    ElementList<IDiagramImplicitConnectionBindingDef> getImplicitConnectionBindingDefs();
    
}
