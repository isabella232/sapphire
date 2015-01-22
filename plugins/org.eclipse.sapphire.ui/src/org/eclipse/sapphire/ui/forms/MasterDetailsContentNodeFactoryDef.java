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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "master details node factory" )
@XmlBinding( path = "node-factory" )
@Image( path = "MasterDetailsContentNodeFactoryDef.png" )

public interface MasterDetailsContentNodeFactoryDef extends MasterDetailsContentNodeChildDef
{
    ElementType TYPE = new ElementType( MasterDetailsContentNodeFactoryDef.class );
    
    // *** Property ***
    
    @Label( standard = "property" )
    @XmlBinding( path = "property" )
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String value );
    
    // *** Cases ***
    
    @Label( standard = "cases" )
    @Type( base = MasterDetailsContentNodeFactoryCaseDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "case", type = MasterDetailsContentNodeFactoryCaseDef.class ) )
    
    ListProperty PROP_CASES = new ListProperty( TYPE, "Cases" );
    
    ElementList<MasterDetailsContentNodeFactoryCaseDef> getCases();

}
