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

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.modeling.ValueBindingImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class XmlValueBindingImpl

    extends ValueBindingImpl
    
{
    public XmlNode getXmlNode()
    {
        // The default implementation returns null, which means that we are unable to pin
        // down the XmlNode to the level of a property. The caller has the option to look
        // up the model hierarchy if the nearest relevant node has to be located.
        
        return null;
    }
    
    /**
     * Convenience method for accessing the XML element associated with the model element that
     * this binding is attached to. This XML element is the common starting point for
     * implementing the binding.
     * 
     * <p>Equivalent to <code>element().adapt( XmlResource.class ).getXmlElement( createIfNecessary )</code>
     * invocation.</p>
     * 
     * @param createIfNecessary whether or not the XML element should be created if it
     *   doesn't exist already; typically set to true for write operations and to false
     *   for read operations
     * @return the XML element associated with the model element that this binding is
     *   attached to or null 
     */
    
    protected final XmlElement xml( final boolean createIfNecessary )
    {
        return resource().getXmlElement( createIfNecessary );
    }
    
    /**
     * Convenience method for accessing the XML resource that this binding is attached to.
     * 
     * <p>Equivalent to <code>element().adapt( XmlResource.class )</code> invocation.</p>
     * 
     * @return the XML resource that this binding is attached to
     */
    
    protected final XmlResource resource()
    {
        return element().adapt( XmlResource.class );
    }
    
}
