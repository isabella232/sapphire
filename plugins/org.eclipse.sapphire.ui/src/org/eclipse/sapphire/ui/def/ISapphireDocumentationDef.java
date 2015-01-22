/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - created common ISapphireDocumentation base type
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

@Label( standard = "documentation" )

public interface ISapphireDocumentationDef extends ISapphireDocumentation
{
    ElementType TYPE = new ElementType( ISapphireDocumentationDef.class );
 
    // *** Id ***
    
    @Label( standard = "ID" )
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String id );

    // *** Title ***
    
    @Label( standard = "title" )
    @XmlBinding( path = "title" )
    
    ValueProperty PROP_TITLE = new ValueProperty( TYPE, "Title" );
    
    Value<String> getTitle();
    void setTitle( String title );

    // *** Content ***
    
    @Label( standard = "content" )
    @LongString
    @XmlBinding( path = "content" )
 
    ValueProperty PROP_CONTENT = new ValueProperty( TYPE, "Content" );
    
    Value<String> getContent();
    void setContent( String content );
    
    // *** Topics ***
    
    @Type( base = ISapphireDocumentationTopicDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "topic", type = ISapphireDocumentationTopicDef.class ) )
                             
    ListProperty PROP_TOPICS = new ListProperty( TYPE, "Topics" );
    
    ElementList<ISapphireDocumentationTopicDef> getTopics();

}
