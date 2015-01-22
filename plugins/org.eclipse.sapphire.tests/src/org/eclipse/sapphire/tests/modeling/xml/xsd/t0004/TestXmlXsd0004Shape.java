/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0004;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestXmlXsd0004Shape extends Element
{
    ElementType TYPE = new ElementType( TestXmlXsd0004Shape.class );
    
    // *** X ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "x" )

    ValueProperty PROP_X = new ValueProperty( TYPE, "X" );
    
    Value<Integer> getX();
    void setX( String value );
    void setX( Integer value );
    
    // *** Y ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "y" )

    ValueProperty PROP_Y = new ValueProperty( TYPE, "Y" );
    
    Value<Integer> getY();
    void setY( String value );
    void setY( Integer value );
    
}
