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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.SapphireActionSystem;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "action context" )
@GenerateImpl

public interface ISapphireActionContext

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireActionContext.class );
    
    // *** Context ***
    
    @Label( standard = "context" )
    @Required
    @XmlBinding( path = "" )
    
    // TODO: Need way to dynamically list available action contexts.
    
    @PossibleValues
    (
        values =
        {
            SapphireActionSystem.CONTEXT_ACTION_LINK,
            SapphireActionSystem.CONTEXT_EDITOR_PAGE,
            SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE,
            SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_HEADER,
            SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_NODE,
            SapphireActionSystem.CONTEXT_ELEMENT_PROPERTY_EDITOR,
            SapphireActionSystem.CONTEXT_LIST_PROPERTY_EDITOR,
            SapphireActionSystem.CONTEXT_SECTION,
            SapphireActionSystem.CONTEXT_VALUE_PROPERTY_EDITOR,
            SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR,
            SapphireActionSystem.CONTEXT_DIAGRAM,
            SapphireActionSystem.CONTEXT_DIAGRAM_NODE,
            SapphireActionSystem.CONTEXT_DIAGRAM_CONNECTION,
            SapphireActionSystem.CONTEXT_WITH_DIRECTIVE
        },
        invalidValueMessage = "\"{0}\" is not valid action context.",
        caseSensitive = false
    )
    
    ValueProperty PROP_CONTEXT = new ValueProperty( TYPE, "Context" );
    
    Value<String> getContext();
    void setContext( String value );
    
}
