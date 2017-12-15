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

import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding

public interface IActionGroupDef

    extends IModelElementForXml, IRemovable
    
{
    ModelElementType TYPE = new ModelElementType( IActionGroupDef.class );
    
    // *** ActionDefs ***
    
    @Type( base = IActionDef.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "action", type = IActionDef.class ) } )
                             
    ListProperty PROP_ACTION_DEFS = new ListProperty( TYPE, "ActionDefs" );
    
    ModelElementList<IActionDef> getActionDefs();
    
}
