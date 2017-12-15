/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.DefinitionReferenceMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IDefinitionReference

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IDefinitionReference.class );
    
    // *** Path ***
    
    @Label( standard = "definition path" )
    @Required
    @XmlBinding( path = "" )
    
    ValueProperty PROP_PATH = new ValueProperty( TYPE, "Path" ); //$NON-NLS-1$
    
    Value<String> getPath();
    void setPath( String path );
    
    // *** Method : resolve ***
    
    @DelegateImplementation( DefinitionReferenceMethods.class )
    
    ISapphireUiDef resolve();
    
}
