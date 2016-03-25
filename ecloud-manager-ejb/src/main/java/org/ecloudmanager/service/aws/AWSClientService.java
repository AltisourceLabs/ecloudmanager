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

package org.ecloudmanager.service.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.service.execution.ActionException;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

@Service
public class AWSClientService {
    @Inject
    private Logger log;
    @Inject
    private AWSMongoCredentialsProvider credentialsProvider;

    private LoadingCache<Region, AmazonEC2> clientCache = CacheBuilder.newBuilder().build(new CacheLoader<Region,
        AmazonEC2>() {
        @Override
        public AmazonEC2 load(Region region) throws Exception {
            return region.createClient(AmazonEC2Client.class, credentialsProvider, null);
        }
    });

    public AmazonEC2 getAmazonEC2(Region region) {
        try {
            return clientCache.get(region);
        } catch (ExecutionException e) {
            log.error("Cannot create AWS client", e);
            throw new ActionException(e);
        }
    }

    public AmazonIdentityManagementClient getIamClient() {
        return new AmazonIdentityManagementClient(credentialsProvider);
    }

    public AmazonRoute53 getRoute53Client() {
        return new AmazonRoute53Client(credentialsProvider);
    }
}
