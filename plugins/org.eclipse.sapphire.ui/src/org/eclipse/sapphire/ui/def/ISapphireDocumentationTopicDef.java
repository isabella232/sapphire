/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

@Label( standard = "related topic" )

public interface ISapphireDocumentationTopicDef extends Element
{
    ElementType TYPE = new ElementType( ISapphireDocumentationTopicDef.class );
 
    // *** Label ***
    
    @Label( standard = "label" )
    @Required
    @XmlBinding( path = "label" )

    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String label );
    
    // *** Href ***
    
    @Label( standard = "href" )
    @XmlBinding( path = "href" )
    @Required
    
    ValueProperty PROP_HREF = new ValueProperty( TYPE, "Href" );
    
    Value<String> getHref();
    void setHref( String href );
}
