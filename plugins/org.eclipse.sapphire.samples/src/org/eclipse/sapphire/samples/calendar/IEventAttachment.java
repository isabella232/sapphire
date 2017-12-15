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

package org.eclipse.sapphire.samples.calendar;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.BasePathsProvider;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.calendar.internal.EventAttachmentLocalCopyBasePathsProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IEventAttachment

    extends IModelElement

{
    ModelElementType TYPE = new ModelElementType( IEventAttachment.class );

    // *** LocalCopyLocation ***
    
    @Type( base = IPath.class )
    @Label( standard = "local copy location" )
    @NonNullValue
    @BasePathsProvider( EventAttachmentLocalCopyBasePathsProvider.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @XmlBinding( path = "local-copy" )

    ValueProperty PROP_LOCAL_COPY_LOCATION = new ValueProperty( TYPE, "LocalCopyLocation" );

    Value<IPath> getLocalCopyLocation();
    void setLocalCopyLocation( String localCopyLocation );
    void setLocalCopyLocation( IPath localCopyLocation );
    
    // *** PublicCopyLocation ***

    @Type( base = URL.class )
    @Label( standard = "public copy location" )
    @NonNullValue
    @XmlBinding( path = "public-copy" )

    ValueProperty PROP_PUBLIC_COPY_LOCATION = new ValueProperty( TYPE, "PublicCopyLocation" );

    Value<URL> getPublicCopyLocation();
    void setPublicCopyLocation( String publicCopyLocation );
    void setPublicCopyLocation( URL publicCopyLocation );
    
}
