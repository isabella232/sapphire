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

package org.eclipse.sapphire.tests.modeling.xml.xsd.all;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@XmlNamespace( uri="http://www.eclipse.org/sapphire/tests/xml/xsd/0007" )
@XmlBinding( path = "root" )

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** A ***
    
    interface A extends Element
    {
        ElementType TYPE = new ElementType( A.class );
        
        // *** Aa ***
        
        @XmlBinding( path = "aa" )
        
        ValueProperty PROP_AA = new ValueProperty( TYPE, "Aa" );
        
        Value<String> getAa();
        void setAa( String value );
        
        // *** Ab ***
        
        @XmlBinding( path = "ab" )
        
        ValueProperty PROP_AB = new ValueProperty( TYPE, "Ab" );
        
        Value<String> getAb();
        void setAb( String value );
        
        // *** Ac ***
        
        @XmlBinding( path = "ac" )
        
        ValueProperty PROP_AC = new ValueProperty( TYPE, "Ac" );
        
        Value<String> getAc();
        void setAc( String value );

        // *** Ad ***
        
        @XmlBinding( path = "ad" )
        
        ValueProperty PROP_AD = new ValueProperty( TYPE, "Ad" );
        
        Value<String> getAd();
        void setAd( String value );
    }
    
    @Type( base = A.class )
    @XmlBinding( path = "a" )
    
    ElementProperty PROP_A = new ElementProperty( TYPE, "A" );
    
    ElementHandle<A> getA();

    // *** B ***
    
    interface B extends Element
    {
        ElementType TYPE = new ElementType( B.class );
        
        // *** Ba ***
        
        @XmlBinding( path = "ba" )
        
        ValueProperty PROP_BA = new ValueProperty( TYPE, "Ba" );
        
        Value<String> getBa();
        void setBa( String value );
        
        // *** Bb ***
        
        @XmlBinding( path = "bb" )
        
        ValueProperty PROP_BB = new ValueProperty( TYPE, "Bb" );
        
        Value<String> getBb();
        void setBb( String value );
        
        // *** Bc ***
        
        @XmlBinding( path = "bc" )
        
        ValueProperty PROP_BC = new ValueProperty( TYPE, "Bc" );
        
        Value<String> getBc();
        void setBc( String value );

        // *** Bd ***
        
        @XmlBinding( path = "bd" )
        
        ValueProperty PROP_BD = new ValueProperty( TYPE, "Bd" );
        
        Value<String> getBd();
        void setBd( String value );
    }
    
    @Type( base = B.class )
    @XmlBinding( path = "b" )
    
    ElementProperty PROP_B = new ElementProperty( TYPE, "B" );
    
    ElementHandle<B> getB();

}
