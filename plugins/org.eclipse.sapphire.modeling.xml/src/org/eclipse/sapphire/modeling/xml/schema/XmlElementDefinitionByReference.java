/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [337232] Certain schema causes elements to be out of order in corresponding xml files
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlElementDefinitionByReference extends XmlElementDefinition
{
    private QName contentModelName = null;
    private boolean contentModelResolved = false;
    
    private XmlElementDefinitionByReference( final XmlDocumentSchema schema,
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
            final String namespace = ref.getNamespaceURI();
            final XmlDocumentSchema refSchema;
            
            if( namespace == null || namespace.length() == 0 )
            {
                refSchema = getSchema();
            }
            else
            {
                final String refSchemaLocation = getSchema().getSchemaLocation( namespace );
                refSchema = XmlDocumentSchemasCache.getSchema( refSchemaLocation );
            }
            
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
    
    private XmlElementDefinition getElementInGroup(final QName childElementName) {
        final XmlElementDefinition definition = getSchema().getElement(getName().getLocalPart());
        // First check to see if this reference is abstract
        if (definition != null && definition.isAbstract()) {
            // Then see if the childElement specify the substitutionGroup
            List<XmlElementDefinition> list = definition.getSubstitutionList();
            if (list != null && list.size() > 0) {
                for (XmlElementDefinition subGroup : list) {
                    if (subGroup.getName().equals(childElementName)) {
                        return subGroup;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public XmlContentModel findChildElementContentModel(QName childElementName) {
        final XmlContentModel model = super.findChildElementContentModel(childElementName);
        if (model == null) {
            XmlElementDefinition subGroup = getElementInGroup(childElementName);
            if (subGroup != null) {
                return subGroup.getContentModel();
            }
        }
        return model;
    }

    @Override
    protected boolean sameElementName(QName qname) {
        boolean isSame = super.sameElementName(qname);
        
        if (!isSame) {
            XmlElementDefinition subGroup = getElementInGroup(qname);
            if (subGroup != null) {
                return true;
            }
        }
        return isSame;
    }

    public static final class Factory extends XmlElementDefinition.Factory
    {
        @Override
        public QName getContentModelName()
        {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void setContentModelName( final QName contentModelName )
        {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void setContentModelName( final String contentModelName )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public XmlContentModel create( final XmlDocumentSchema schema )
        {
            return new XmlElementDefinitionByReference( schema, this.elementName, this.minOccur, this.maxOccur );
        }
    }
    
}

