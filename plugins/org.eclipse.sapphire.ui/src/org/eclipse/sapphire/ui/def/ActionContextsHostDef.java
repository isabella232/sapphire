/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface ActionContextsHostDef extends Element
{
    ElementType TYPE = new ElementType( ActionContextsHostDef.class );
    
    // *** Contexts ***
    
    @Type( base = ActionContextRef.class )
    @Label( standard = "contexts" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "context", type = ActionContextRef.class ) )
    
    ListProperty PROP_CONTEXTS = new ListProperty( TYPE, "Contexts" );
    
    ElementList<ActionContextRef> getContexts();
    
}
