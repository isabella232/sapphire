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

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "shape" )
@Image( path = "ShapeDef.png" )

public interface ShapeDef extends PartDef, PropertiesViewContributorDef
{
	ElementType TYPE = new ElementType( ShapeDef.class );
		    
    // *** SequenceLayoutConstraint ***
    
    @Type( base = SequenceLayoutConstraintDef.class )
    @Label( standard = "sequence layout constraint" )
    @XmlBinding( path = "sequence-layout-constraint" )

    ImpliedElementProperty PROP_SEQUENCE_LAYOUT_CONSTRAINT = new ImpliedElementProperty( TYPE, "SequenceLayoutConstraint" );
    
    SequenceLayoutConstraintDef getSequenceLayoutConstraint();
    
}
