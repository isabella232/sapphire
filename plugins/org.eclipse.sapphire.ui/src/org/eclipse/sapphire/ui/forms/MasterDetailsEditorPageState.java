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

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.ui.EditorPageState;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface MasterDetailsEditorPageState extends EditorPageState
{
    ElementType TYPE = new ElementType( MasterDetailsEditorPageState.class );
    
    // *** ContentOutlineState ***
    
    @Type( base = MasterDetailsOutlineState.class )

    ImpliedElementProperty PROP_CONTENT_OUTLINE_STATE = new ImpliedElementProperty( TYPE, "ContentOutlineState" );
    
    MasterDetailsOutlineState getContentOutlineState();
    
}
