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

package org.ecloudmanager.tmrk.cloudapi;

/**
 * @author irosu
 */
public interface CloudapiConstants {

    String TMRK_API_URL = "https://services.enterprisecloud.terremark.com";

    //FIXME Schema update required?
    String TMRK_API_VERSION = "2015-07-01";

    String SIGNATURE_TYPE = "HmacSha256";


    String TMRK_API_VERSION_PROP = "cloudapi.version";
    String TMRK_API_ACCESS_KEY_PROP = "cloudapi.accessKey";
    String TMRK_API_PRIVATE_KEY_PROP = "cloudapi.privateKey";


    String TMRK_MEDIA_SUBTYPE_PREFIX = "vnd.tmrk.";

    String TMRK_HEADER_PREFIX = "x-tmrk-";
    String X_TMRK_VERSION = TMRK_HEADER_PREFIX + "version";
    String X_TMRK_AUTHORIZATION = TMRK_HEADER_PREFIX + "authorization";

}
