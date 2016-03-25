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

package org.ecloudmanager.tmrk.cloudapi.exceptions;

import org.ecloudmanager.tmrk.cloudapi.model.ErrorType;

/**
 * @author irosu
 */
public class CloudapiException extends RuntimeException {

    private static final long serialVersionUID = 8894913434616126430L;

    private ErrorType error;


    public CloudapiException(ErrorType response) {
        this.error = response;
    }


    public CloudapiException(String message) {
        super(message);
    }


    public CloudapiException(Throwable cause) {
        super(cause);
    }

    public CloudapiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorType getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return error != null ? error.getMessage() : super.getMessage();
    }


}