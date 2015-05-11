/*
 * Copyright 2014 Red Hat inc. and third party contributors as noted
 * by the author tags.
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
 */

package com.redhat.ceylon.model.cmr;

/**
 * Filter used to determine whether a path should be included or excluded from imports and exports.
 *
 * @author John Bailey
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface PathFilter {

    /**
     * Determine whether a path should be accepted.  The given name is a path separated
     * by "{@code /}" characters.
     *
     * @param path the path to check
     * @return true if the path should be accepted, false if not
     */
    boolean accept(String path);
}