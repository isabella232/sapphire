/******************************************************************************
 * Copyright (c) 2015 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlSchema;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml-binding" )
@XmlSchema( namespace = "http://www.eclipse.org/sapphire/tests/xml-binding", location = "http://www.eclipse.org/sapphire/tests/xml-binding/1.0" )
@XmlBinding( path = "test-root" )

public interface XmlBindingTestModelAltB extends XmlBindingTestModel
{
    ElementType TYPE = new ElementType( XmlBindingTestModelAltB.class );

}
