/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ratpackframework.bootstrap.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ratpackframework.config.Config
import org.ratpackframework.config.internal.ConfigLoader
import spock.lang.Specification

class ConfigLoaderTest extends Specification {

  @Rule TemporaryFolder tmp
  def loader = new ConfigLoader()
  Config config

  void configScript(String text) {
    final file = tmp.newFile("config.groovy")
    file.text = text
    config = loader.load(file)
  }

  def "can compile config file"() {
    when:
    configScript """
      deployment.port = 2020
    """

    then:
    config.deployment.port == 2020
  }

}