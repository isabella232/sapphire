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

package org.eclipse.sapphire.tests.path.relative;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** RootPath ***
    
    @Type( base = Path.class )
    
    ValueProperty PROP_ROOT_PATH = new ValueProperty( TYPE, "RootPath" );
    
    Value<Path> getRootPath();
    void setRootPath( String value );
    void setRootPath( Path value );
    
    // *** RelativePath ***
    
    @Type( base = Path.class )
    @Service( impl = TestRelativePathService.class )
    @MustExist

    ValueProperty PROP_RELATIVE_PATH = new ValueProperty( TYPE, "RelativePath" );
    
    Value<Path> getRelativePath();
    void setRelativePath( String value );
    void setRelativePath( Path value );

}