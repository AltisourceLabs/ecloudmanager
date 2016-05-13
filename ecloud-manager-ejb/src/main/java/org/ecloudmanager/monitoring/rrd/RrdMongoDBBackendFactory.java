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

package org.ecloudmanager.monitoring.rrd;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.rrd4j.core.RrdBackend;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdMongoDBBackend;

import java.io.IOException;

/**
 * This is a modified version of org.rrd4j.core.RrdMongoDBBackendFactory
 */
public class RrdMongoDBBackendFactory extends RrdBackendFactory {
    private final DBCollection rrdCollection;

    /**
     * Creates a RrdMongoDBBackendFactory. Make sure that the passed {@link com.mongodb.DBCollection} has a safe write
     * concern, is capped (if needed) and slaveOk() called if applicable.
     *
     * @param rrdCollection the collection to use for storing RRD byte data
     */
    public RrdMongoDBBackendFactory(DBCollection rrdCollection) {
        this.rrdCollection = rrdCollection;

        // make sure we have an index on the path field
        rrdCollection.createIndex(new BasicDBObject("path", 1));

        // set the RRD backend factory
        RrdBackendFactory.registerAndSetAsDefaultFactory(this);
    }

    /** {@inheritDoc} */
    @Override
    protected RrdBackend open(String path, boolean readOnly) throws IOException {
        return new RrdMongoDBBackend(path, rrdCollection);
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(String path) throws IOException {
        BasicDBObject query = new BasicDBObject();
        query.put("path", path);
        return rrdCollection.findOne(query) != null;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean shouldValidateHeader(String path) throws IOException {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "MONGODB";
    }
}
