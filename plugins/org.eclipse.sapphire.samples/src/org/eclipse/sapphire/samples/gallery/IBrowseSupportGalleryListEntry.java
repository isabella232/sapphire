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

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding

public interface IBrowseSupportGalleryListEntry

    extends IModelElementForXml, IRemovable
    
{
    ModelElementType TYPE = new ModelElementType( IBrowseSupportGalleryListEntry.class );
    
    // *** MultiOptionPath ***
    
    @Type( base = IPath.class )
    @Label( standard = "multi option path" )
    @XmlBinding( path = "multi-option-path" )

    ValueProperty PROP_MULTI_OPTION_PATH = new ValueProperty( TYPE, "MultiOptionPath" );
    
    Value<IPath> getMultiOptionPath();
    void setMultiOptionPath( String value );
    void setMultiOptionPath( IPath value );
    
}
