/******************************************************************************
 * Copyright (c) 2014 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0003;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlDocumentType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@XmlBinding( path = "root" )
@XmlDocumentType( publicId = "-//Sapphire//DTD Country 1.0.0//EN", systemId = "" )

public interface TestElementError2 extends TestElement
{
    ElementType TYPE = new ElementType( TestElementError2.class );
    
}
