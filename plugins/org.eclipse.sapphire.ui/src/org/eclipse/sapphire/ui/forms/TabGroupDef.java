/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "tab group" )
@Image( path = "TabGroupDef.png" )
@XmlBinding( path = "tab-group" )

public interface TabGroupDef extends FormComponentDef
{
    ElementType TYPE = new ElementType( TabGroupDef.class );
    
    // *** Tabs ***
    
    @Label( standard = "tabs" )
    @Type( base = TabGroupPageDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "tab", type = TabGroupPageDef.class ) )
    
    ListProperty PROP_TABS = new ListProperty( TYPE, "Tabs" );
    
    ElementList<TabGroupPageDef> getTabs();
    
}
