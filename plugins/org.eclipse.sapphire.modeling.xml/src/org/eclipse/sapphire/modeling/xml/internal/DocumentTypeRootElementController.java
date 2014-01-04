/*******************************************************************************
 * Copyright (c) 2014 Accenture and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kamesh Sampath - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;
import static org.eclipse.sapphire.modeling.util.MiscUtil.normalizeToNull;

import org.eclipse.sapphire.modeling.xml.RootElementController;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.annotations.XmlDocumentType;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * Implementation of RootElementController that is used to work with DTD-based documents. It is
 * configured via the @XmlDocumentType annotation.
 * 
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DocumentTypeRootElementController extends RootElementController {

    private String rootElementName;
    private String publicId;
    private String systemId;

    public DocumentTypeRootElementController(String rootElementName) {
        this.rootElementName = rootElementName;
    }

    @Override
    public void init(XmlResource resource) {
        super.init(resource);
        XmlDocumentType doctypeAnnotation = resource.root().element()
                .type().getAnnotation(XmlDocumentType.class);
        
        if( doctypeAnnotation != null ) 
        {
            this.systemId = normalizeToNull( doctypeAnnotation.systemId() );
            
            if( this.systemId == null )
            {
                throw new IllegalStateException();
            }
            
            this.publicId = normalizeToNull( doctypeAnnotation.publicId() );
        }
    }

    protected void createRootElement(Document document) {
        final Element root = document.createElementNS(null,
                this.rootElementName);
        DocumentType doctype = null;

        if (this.publicId != null ) {
            doctype = document.getImplementation().createDocumentType(
                    this.rootElementName, this.publicId, this.systemId);
        } else {
            doctype = document.getImplementation().createDocumentType(
                    this.rootElementName, null, this.systemId);
        }
        if (doctype != null) {
            document.appendChild(doctype);
            document.insertBefore(root, doctype);
        }
        document.appendChild(root);
    }

    @Override
    public void createRootElement() {
        Document document = resource().adapt( RootXmlResource.class ).getDomDocument();
        createRootElement(document);
    }

    @Override
    public boolean checkRootElement() 
    {
        final Document document = resource().adapt( RootXmlResource.class ).getDomDocument();
        final Element root = document.getDocumentElement();
        
        if( equal( root.getLocalName(), this.rootElementName ) )
        {
            final DocumentType documentType = document.getDoctype();
            
            if( documentType != null &&
                this.systemId.equals( documentType.getSystemId() ) &&
                equal( this.publicId, normalizeToNull( documentType.getPublicId() ) ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
}
