/******************************************************************************
 * Copyright (c) 2013 Liferay and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gregory Amerson - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.actions;

import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.swt.gef.DiagramRenderingContext;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;

/**
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PrintDiagramActionHandler extends SapphireActionHandler
{
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final DiagramRenderingContext diagramContext = (DiagramRenderingContext) context;
        final SapphireDiagramEditor diagramEditor = diagramContext.getDiagramEditor();

        ( new PrintAction( diagramEditor ) ).run();

        return null;
    }

}
