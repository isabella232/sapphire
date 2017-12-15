/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.services.t0009;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestElement.class );
    
    // *** Version ***
    
    @Type( base = Version.class )

    ValueProperty PROP_VERSION = new ValueProperty( TYPE, "Version" );
    
    Value<Version> getVersion();
    void setVersion( String value );
    void setVersion( Version value );
    
    // *** VersionConstraint ***
    
    @Type( base = VersionConstraint.class )

    ValueProperty PROP_VERSION_CONSTRAINT = new ValueProperty( TYPE, "VersionConstraint" );
    
    Value<VersionConstraint> getVersionConstraint();
    void setVersionConstraint( String value );
    void setVersionConstraint( VersionConstraint value );
    
}
