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

package org.eclipse.sapphire.sdk.extensibility;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface ExtensionSummarySectionDef extends Element
{
    ElementType TYPE = new ElementType( ExtensionSummarySectionDef.class );
    
    // *** ExtensionType ***
    
    @Label( standard = "extension type" )
    @Required
    
    ValueProperty PROP_EXTENSION_TYPE = new ValueProperty( TYPE, "ExtensionType" );
    
    Value<String> getExtensionType();
    void setExtensionType( String value );
    
    // *** IncludeSectionHeader ***
    
    @Type( base = Boolean.class )
    @Label( standard = "include section header" )
    @DefaultValue( text = "true" )
    
    ValueProperty PROP_INCLUDE_SECTION_HEADER = new ValueProperty( TYPE, "IncludeSectionHeader" );
    
    Value<Boolean> getIncludeSectionHeader();
    void setIncludeSectionHeader( String value );
    void setIncludeSectionHeader( Boolean value );
    
    // *** Columns ***
    
    @Type( base = ExtensionSummarySectionColumnDef.class )
    @Label( standard = "columns" )
    
    ListProperty PROP_COLUMNS = new ListProperty( TYPE, "Columns" );
    
    ElementList<ExtensionSummarySectionColumnDef> getColumns();
    
}
