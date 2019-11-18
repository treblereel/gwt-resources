/*
 * Copyright 2008 Google Inc.
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
package org.gwtproject.resources.ext;

/** Provides deferred binding property values. */
public interface PropertyOracle {

  /**
   * Attempts to get a named configuration property. Throws <code>UnableToCompleteException</code>
   * if the property is undefined. The result of invoking this method with the same <code>
   * propertyName</code> must be stable.
   *
   * @param propertyName
   * @return the configuration property instance (never null)
   * @throws UnableToCompleteException if the property is unknown or not a configuration property
   */
  ConfigurationProperty getConfigurationProperty(TreeLogger logger, String propertyName)
      throws UnableToCompleteException;

  SelectionProperty getSelectionProperty(TreeLogger logger, String propertyName)
      throws UnableToCompleteException;
}
