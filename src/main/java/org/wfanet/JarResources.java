/*
 * Copyright 2022 The Cross-Media Measurement Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wfanet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class JarResources {
  private static final String TEMP_DIR_PREFIX = "jar-resources";

  /**
   * Loads a library from a JAR resource.
   *
   * @param resourceName name of the resource within the JAR
   */
  public static void loadLibrary(String resourceName) {
    Path tempDir;
    try {
      tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
    } catch (IOException e) {
      throw new LinkageError("Unable to link " + resourceName, e);
    }
    tempDir.toFile().deleteOnExit();

    URL resourceUrl =
        requireNotNull(
            JarResources.class.getClassLoader().getResource(resourceName), "Resource not found");
    Path libPath = tempDir.resolve(getFileName(resourceName));
    try (InputStream libStream = resourceUrl.openStream()) {
      Files.copy(libStream, libPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new LinkageError("Unable to link " + resourceName, e);
    }

    System.load(libPath.toFile().getAbsolutePath());
  }

  private static String getFileName(String resourceName) {
    int index = resourceName.lastIndexOf("/");
    if (index == -1) {
      return resourceName;
    }
    return resourceName.substring(index + 1);
  }

  private static <T> T requireNotNull(T item, String message) {
    if (item == null) {
      throw new IllegalArgumentException(message);
    }
    return item;
  }
}
