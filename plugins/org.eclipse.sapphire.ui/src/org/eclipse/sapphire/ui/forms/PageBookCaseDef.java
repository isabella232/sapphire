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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "case" )
@Image( path = "PageBookCaseDef.png" )

public interface PageBookCaseDef extends FormDef
{
    ElementType TYPE = new ElementType( PageBookCaseDef.class );
    
    // *** ElementType ***
    
    @DefaultValue( text = "org.eclipse.sapphire.Element" )
    @NoDuplicates
    
    ValueProperty PROP_ELEMENT_TYPE = new ValueProperty( TYPE, PartDef.PROP_ELEMENT_TYPE );

}
