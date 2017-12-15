/******************************************************************************
 * Copyright (c) 2013 Oracle
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
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.internal.SapphirePartDefMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface PartDef extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( PartDef.class );
    
    String HINT_HIDE_IF_DISABLED = "hide.if.disabled";
    String HINT_PREFER_FORM_STYLE = "prefer.form.style";
    String HINT_STYLE = "style";
    
    // *** Id ***
    
    @Label( standard = "ID" )
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String value );
    
    // *** Documentation ***
    
    @Type
    ( 
        base = ISapphireDocumentation.class,
        possible = 
        {
            ISapphireDocumentationDef.class, 
            ISapphireDocumentationRef.class
        }
    )
    
    @XmlElementBinding
    (
        mappings =
        {
            @XmlElementBinding.Mapping( element = "documentation", type = ISapphireDocumentationDef.class ),
            @XmlElementBinding.Mapping( element = "documentation-ref", type = ISapphireDocumentationRef.class )
        }
    )
    
    ElementProperty PROP_DOCUMENTATION = new ElementProperty( TYPE, "Documentation" );
    
    ModelElementHandle<ISapphireDocumentation> getDocumentation();

    // *** Hints ***
    
    @Label( standard = "hints" )
    @Type( base = ISapphireHint.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "hint", type = ISapphireHint.class ) )
    
    ListProperty PROP_HINTS = new ListProperty( TYPE, "Hints" );
    
    ModelElementList<ISapphireHint> getHints();
    
    // *** Method : getHint ***
    
    @DelegateImplementation( SapphirePartDefMethods.class )
    
    String getHint( String name );
    
    // *** Method : getHint ***
    
    @DelegateImplementation( SapphirePartDefMethods.class )
    
    String getHint( String name,
                    String defaultValue );
    
    // *** Method : getHint ***
    
    @DelegateImplementation( SapphirePartDefMethods.class )
    
    boolean getHint( String name,
                     boolean defaultValue );

    // *** Method : getHint ***
    
    @DelegateImplementation( SapphirePartDefMethods.class )
    
    int getHint( String name,
                 int defaultValue );
    
    // *** Style ***

    @Label( standard = "style" )
    @XmlBinding( path = "style" )
    
    ValueProperty PROP_STYLE = new ValueProperty( TYPE, "Style" );

    Value<String> getStyle();
    void setStyle( String value );
    
    // *** Listeners ***
    
    @Label( standard = "listeners" )
    @Type( base = ISapphirePartListenerDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "listener", type = ISapphirePartListenerDef.class ) )
    
    ListProperty PROP_LISTENERS = new ListProperty( TYPE, "Listeners" );
    
    ModelElementList<ISapphirePartListenerDef> getListeners();
    
    // *** Actions ***
    
    @Type( base = ActionDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "action", type = ActionDef.class ) )
    @Label( standard = "action" )
    
    ListProperty PROP_ACTIONS = new ListProperty( TYPE, "Actions" );
    
    ModelElementList<ActionDef> getActions();
    
    // *** ActionHandlers ***
    
    @Type( base = ActionHandlerDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "action-handler", type = ActionHandlerDef.class ) )
    @Label( standard = "action handlers" )
    
    ListProperty PROP_ACTION_HANDLERS = new ListProperty( TYPE, "ActionHandlers" );
    
    ModelElementList<ActionHandlerDef> getActionHandlers();
    
    // *** ActionHandlerFactories ***
    
    @Type( base = ActionHandlerFactoryDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "action-handler-factory", type = ActionHandlerFactoryDef.class ) )
    @Label( standard = "action handler factories" )
    
    ListProperty PROP_ACTION_HANDLER_FACTORIES = new ListProperty( TYPE, "ActionHandlerFactories" );
    
    ModelElementList<ActionHandlerFactoryDef> getActionHandlerFactories();
    
    // *** ActionHandlerFilters ***
    
    @Type( base = ActionHandlerFilterDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "action-handler-filter", type = ActionHandlerFilterDef.class ) )
    @Label( standard = "action handler filters" )
    
    ListProperty PROP_ACTION_HANDLER_FILTERS = new ListProperty( TYPE, "ActionHandlerFilters" );
    
    ModelElementList<ActionHandlerFilterDef> getActionHandlerFilters();
    
    // *** Services ***
    
    @Type( base = ServiceDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "service", type = ServiceDef.class ) )
    @Label( standard = "service" )
    
    ListProperty PROP_SERVICES = new ListProperty( TYPE, "Services" );
    
    ModelElementList<ServiceDef> getServices();
    
}
