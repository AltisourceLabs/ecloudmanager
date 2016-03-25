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

package org.ecloudmanager.domain;

import java.util.ArrayList;
import java.util.List;

public class ThrowableEntity {
    private String type;
    private String message;
    private ArrayList<StackTraceElementEntity> stackTrace;
    private ThrowableEntity cause;

    public ThrowableEntity() {
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public List<StackTraceElementEntity> getStackTrace() {
        return stackTrace;
    }

    public ThrowableEntity getCause() {
        return cause;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        ThrowableEntity throwable = this;
        while (throwable != null) {
            builder.append(throwable.getType());
            builder.append(": ");
            throwable = throwable.getCause();
        }
        builder.append(getMessage());
        for (StackTraceElementEntity line : getStackTrace()) {
            builder.append("\n");
            builder.append(line.toString());
        }

        if (getCause() != null) {
            builder.append("\nCaused by: ");
            builder.append(getCause().toString());
        }

        return builder.toString();
    }
}
