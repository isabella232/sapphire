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

package org.eclipse.sapphire.modeling;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface IModel

    extends IModelElement
    
{
    ModelStore getModelStore();
    File getFile();
    IFile getEclipseFile();
    IProject getEclipseProject();
    
    boolean isCorrupted();
    void setCorruptedModelStoreExceptionInterceptor( CorruptedModelStoreExceptionInterceptor interceptor );
    
    void validateEdit();
    void addValidateEditListener( ValidateEditListener listener );
    void removeValidateEditListener( ValidateEditListener listener );
    ValidateEditPolicy getValidateEditPolicy();
    void setValidateEditPolicy( ValidateEditPolicy policy );
    
    void save() throws IOException;
}