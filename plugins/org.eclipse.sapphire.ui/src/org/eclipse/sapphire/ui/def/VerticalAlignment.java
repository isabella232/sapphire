/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.annotations.EnumSerialization;
import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public enum VerticalAlignment 
{
    @Label( standard = "top" )
    @EnumSerialization( primary = "top" )
    
    TOP,
    
    @Label( standard = "center" )
    @EnumSerialization( primary = "center" )
    
    CENTER,
    
    @Label( standard = "bottom" )
    @EnumSerialization( primary = "bottom" )
    
    BOTTOM
    
}
