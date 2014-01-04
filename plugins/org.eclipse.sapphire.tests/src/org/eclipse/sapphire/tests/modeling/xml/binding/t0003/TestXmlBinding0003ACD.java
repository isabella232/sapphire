/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0003;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/u", prefix = "u" )

public interface TestXmlBinding0003ACD extends Element
{
    ElementType TYPE = new ElementType( TestXmlBinding0003ACD.class );
    
    // *** Acda ***
    
    @XmlBinding( path = "u:acda" )
    
    ValueProperty PROP_ACDA = new ValueProperty( TYPE, "Acda" );
    
    Value<String> getAcda();
    void setAcda( String value );
    
}
