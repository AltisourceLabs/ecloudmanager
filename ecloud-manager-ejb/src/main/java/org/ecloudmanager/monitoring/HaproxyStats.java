/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Altisource Labs
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

package org.ecloudmanager.monitoring;

import org.ecloudmanager.jeecore.domain.MongoObject;
import org.mongodb.morphia.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity(cap = @CappedAt(value = 1024 * 1024 * 500))
@Indexes(@Index("haproxyStatsAddress, -timestamp"))
public class HaproxyStats extends MongoObject {
    @Indexed
    private Date timestamp;
    @Indexed
    private String haproxyStatsAddress;
    private List<Map<String, String>> data;

    public HaproxyStats() {}

    public String getHaproxyStatsAddress() {
        return haproxyStatsAddress;
    }

    public void setHaproxyStatsAddress(String haproxyStatsAddress) {
        this.haproxyStatsAddress = haproxyStatsAddress;
    }

    public List<Map<String, String>> getData() {
        return data;
    }

    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @PrePersist
    private void createTimestamp() {
        if (timestamp == null) {
            timestamp = new Date();
        }
    }
}
