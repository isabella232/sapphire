/*******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0011e;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0011e/4", prefix = "ns4" )
@XmlBinding( path = "ns4:a2" )

public interface TestModelElementA2 extends TestModelElementA
{
    ElementType TYPE = new ElementType( TestModelElementA2.class );

}
