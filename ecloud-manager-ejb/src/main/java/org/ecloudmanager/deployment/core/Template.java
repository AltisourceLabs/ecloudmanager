/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.deployment.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface Template<T extends Deployable> {
    @NotNull T toDeployment();

    String getName();

    void setName(String name);

    @Nullable String getDescription();

    void setDescription(String description);

    @NotNull
    List<Endpoint> getEndpoints();

    @NotNull
    List<String> getRequiredEndpoints();

    default List<String> getRequiredEndpointsIncludingTemplateName() {
        List<String> result = new ArrayList<>();
        getRequiredEndpoints().forEach(e -> result.add(getName() + ":" + e));
        return result;
    }

    default List<String> getEndpointsIncludingTemplateName() {
        List<String> result = new ArrayList<>();
        getEndpoints().forEach(e -> result.add(getName() + ":" + e.getName()));
        return result;
    }

}
