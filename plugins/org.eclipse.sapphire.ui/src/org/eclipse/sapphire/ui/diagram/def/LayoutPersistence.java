/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [382431] Inconsistent terminology: layout storage and layout persistence
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.modeling.annotations.EnumSerialization;
import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "layout persistence" )

public enum LayoutPersistence 
{
    @Label( standard = "side-by-side" )
    @EnumSerialization( primary = "side-by-side" )
    
    SIDE_BY_SIDE,
    
    @Label( standard = "project" )
    @EnumSerialization( primary = "project" )
    
    PROJECT,
    
    @Label( standard = "workspace" )
    @EnumSerialization( primary = "workspace" )
    
    WORKSPACE,
    
    @Label( standard = "custom" )
    @EnumSerialization( primary = "custom" )
    
    CUSTOM;

}
