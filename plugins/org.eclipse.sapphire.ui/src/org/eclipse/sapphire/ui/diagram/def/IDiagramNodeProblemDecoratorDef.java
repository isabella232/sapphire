/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public interface IDiagramNodeProblemDecoratorDef extends IDiagramDecoratorDef 
{
    ElementType TYPE = new ElementType( IDiagramNodeProblemDecoratorDef.class );
    
    // *** ShowDecorator ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "show-decorator" )
    @DefaultValue( text = "true" )
    
    ValueProperty PROP_SHOW_DECORATOR = new ValueProperty(TYPE, "ShowDecorator");
    
    Value<Boolean> isShowDecorator();
    void setShowDecorator( String value );
    void setShowDecorator( Boolean value );
    
    // *** Size ***
    
    @Type( base = ProblemDecoratorSize.class )
    @Label( standard = "size")
    @DefaultValue( text = "large" )
    @Enablement( expr = "${ ShowDecorator }" )
    @XmlBinding( path = "size" )
    
    ValueProperty PROP_SIZE = new ValueProperty( TYPE, "Size" );
    
    Value<ProblemDecoratorSize> getSize();
    void setSize( String value );
    void setSize( ProblemDecoratorSize value ) ;
    
}
