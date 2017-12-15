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

package org.eclipse.sapphire.modeling.xml.annotations;

import org.eclipse.sapphire.modeling.annotations.ValuePropertyCustomBindingImpl;
import org.eclipse.sapphire.modeling.xml.XmlNode;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ValuePropertyCustomXmlBindingImpl

    extends ValuePropertyCustomBindingImpl
    
{
    public XmlNode getXmlNode()
    {
        // The default implementation returns null, which means that we are unable to pin
        // down the XmlNode to the level of a property. The caller has the option to look
        // up the model hierarchy if the nearest relevant node has to be located.
        
        return null;
    }
}
