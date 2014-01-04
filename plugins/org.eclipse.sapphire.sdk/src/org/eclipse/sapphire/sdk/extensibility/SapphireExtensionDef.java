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

package org.eclipse.sapphire.sdk.extensibility;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.ui.def.ISapphireUiExtensionDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@XmlNamespace( uri = "http://www.eclipse.org/sapphire/xmlns/extension" )
@XmlBinding( path = "extension" )

public interface SapphireExtensionDef extends ISapphireUiExtensionDef
{
    ElementType TYPE = new ElementType( SapphireExtensionDef.class );
    
    String FILE_NAME = "sapphire-extension.xml";

    // *** Services ***
    
    @Type( base = ServiceDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "service", type = ServiceDef.class ) )
    @Label( standard = "service" )
    
    ListProperty PROP_SERVICES = new ListProperty( TYPE, "Services" );
    
    ElementList<ServiceDef> getServices();
    
    // *** Functions ***
    
    @Type( base = FunctionDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "function", type = FunctionDef.class ) )
    @Label( standard = "functions" )
    
    ListProperty PROP_FUNCTIONS = new ListProperty( TYPE, "Functions" );
    
    ElementList<FunctionDef> getFunctions();

}
