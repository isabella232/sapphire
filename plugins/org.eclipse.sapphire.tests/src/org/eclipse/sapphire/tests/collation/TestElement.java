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

package org.eclipse.sapphire.tests.collation;

import org.eclipse.sapphire.Collation;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** NoCollationSpecified ***
    
    ValueProperty PROP_NO_COLLATION_SPECIFIED = new ValueProperty( TYPE, "NoCollationSpecified" );
    
    Value<String> getNoCollationSpecified();
    void setNoCollationSpecified( String value );
    
    // *** IgnoreCaseLiteralFalse ***
    
    @Collation( ignoreCaseDifferences = "false" )
    
    ValueProperty PROP_IGNORE_CASE_LITERAL_FALSE = new ValueProperty( TYPE, "IgnoreCaseLiteralFalse" );
    
    Value<String> getIgnoreCaseLiteralFalse();
    void setIgnoreCaseLiteralFalse( String value );
    
    // *** IgnoreCaseLiteralTrue ***
    
    @Collation( ignoreCaseDifferences = "true" )
    
    ValueProperty PROP_IGNORE_CASE_LITERAL_TRUE = new ValueProperty( TYPE, "IgnoreCaseLiteralTrue" );
    
    Value<String> getIgnoreCaseLiteralTrue();
    void setIgnoreCaseLiteralTrue( String value );
    
    // *** IgnoreCase ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_IGNORE_CASE = new ValueProperty( TYPE, "IgnoreCase" );
    
    Value<Boolean> getIgnoreCase();
    void setIgnoreCase( String value );
    void setIgnoreCase( Boolean value );
    
    // *** IgnoreCaseDynamic ***
    
    @Collation( ignoreCaseDifferences = "${ IgnoreCase }" )
    
    ValueProperty PROP_IGNORE_CASE_DYNAMIC = new ValueProperty( TYPE, "IgnoreCaseDynamic" );
    
    Value<String> getIgnoreCaseDynamic();
    void setIgnoreCaseDynamic( String value );
    
    // *** IgnoreCaseDynamicGlobal ***
    
    @Collation( ignoreCaseDifferences = "${ Global.TestIgnoreCase }", global = true )
    
    ValueProperty PROP_IGNORE_CASE_DYNAMIC_GLOBAL = new ValueProperty( TYPE, "IgnoreCaseDynamicGlobal" );
    
    Value<String> getIgnoreCaseDynamicGlobal();
    void setIgnoreCaseDynamicGlobal( String value );
    
}
