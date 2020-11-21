/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.resources.client;

import org.gwtproject.resources.ext.ResourceGeneratorType;

/** Custom subtype of {@link DataResource}, to test {@code @url} in CssResource. */
@ResourceGeneratorType("org.gwtproject.resources.client.CustomDataResourceGenerator")
public interface CustomDataResource extends DataResource {}
