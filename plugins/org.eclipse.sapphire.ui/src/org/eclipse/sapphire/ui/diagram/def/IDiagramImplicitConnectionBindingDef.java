/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "diagram connection binding" )

public interface IDiagramImplicitConnectionBindingDef extends IDiagramConnectionBindingDef 
{
    ElementType TYPE = new ElementType( IDiagramImplicitConnectionBindingDef.class );
    
    // *** ModelElementTypes ***
    
    @Type( base = IModelElementTypeDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "model-element-type", type = IModelElementTypeDef.class ) )
                             
    ListProperty PROP_MODEL_ELEMENT_TYPES = new ListProperty( TYPE, "ModelElementTypes" );
    
    ElementList<IModelElementTypeDef> getModelElementTypes();
    
    // *** Condition ***
    
    @Type( base = Function.class )
    @XmlBinding( path = "condition" )
    @Label( standard = "condition" )
    
    ValueProperty PROP_CONDITION = new ValueProperty(TYPE, "Condition");
    
    Value<Function> getCondition();
    void setCondition( String value );
    void setCondition( Function value );
    
    // *** ConnectionWiringStrategy ***
    
    @Type( base = ImplicitConnectionWiringStrategy.class )
    @DefaultValue( text = "sequential" )
    @XmlBinding( path = "connection-wiring-strategy" )
    
    ValueProperty PROP_CONNECTION_WIRING_STRATEGY = new ValueProperty( TYPE, "ConnectionWiringStrategy" );
    
    Value<ImplicitConnectionWiringStrategy> getConnectionWiringStrategy();
    void setConnectionWiringStrategy( String value );
    void setConnectionWiringStrategy( ImplicitConnectionWiringStrategy value );
    
}
