/*
 * Copyright 2011-2012 Alfresco Software Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is part of an unsupported extension to Alfresco.
 */

package org.alfresco.model.jive;

import org.alfresco.service.namespace.QName;

public interface JiveModel
{
    public final static String JIVE_MODEL_1_0_URI          = "http://www.alfresco.org/model/jive/1.0";
    public final static String JIVE_ASPECT_NAME_SOCIALIZED = "socialized";
    public final static QName  ASPECT_JIVE_SOCIALIZED      = QName.createQName(JIVE_MODEL_1_0_URI, JIVE_ASPECT_NAME_SOCIALIZED);
}
