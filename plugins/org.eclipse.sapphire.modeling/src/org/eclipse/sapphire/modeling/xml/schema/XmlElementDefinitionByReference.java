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

package org.eclipse.sapphire.modeling.xml.schema;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class XmlElementDefinitionByReference extends XmlElementDefinition
{
    private QName contentModelName = null;
    private boolean contentModelResolved = false;
    
    public XmlElementDefinitionByReference( final XmlDocumentSchema schema,
                                            final QName elementName,
                                            final int minOccur,
                                            final int maxOccur )
    {
        super( schema, elementName, null, minOccur, maxOccur );
    }

    @Override
    public QName getContentModelName()
    {
        final boolean contentModelResolveNeeded;
        
        synchronized( this )
        {
            contentModelResolveNeeded = ( ! this.contentModelResolved );
        }
        
        if( contentModelResolveNeeded )
        {
            QName contentModelName = null;
            
            final QName ref = getName();
            final String refSchemaLocation = getSchema().getSchemaLocation( ref.getNamespaceURI() );
            final XmlDocumentSchema refSchema = XmlDocumentSchemasCache.getSchema( refSchemaLocation );
            
            if( refSchema != null )
            {
                final XmlElementDefinition refElement = refSchema.getElement( ref.getLocalPart() );
                
                if( refElement != null )
                {
                    contentModelName = refElement.getContentModelName();
                }
            }
            
            synchronized( this )
            {
                if( ! this.contentModelResolved )
                {
                    this.contentModelName = contentModelName;
                    this.contentModelResolved = true;
                }
            }
        }
        
        return this.contentModelName;
    }
    
}

