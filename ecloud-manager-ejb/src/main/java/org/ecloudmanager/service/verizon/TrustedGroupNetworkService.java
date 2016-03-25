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

package org.ecloudmanager.service.verizon;

import org.ecloudmanager.tmrk.cloudapi.model.CreateTrustedNetworkGroupType;
import org.ecloudmanager.tmrk.cloudapi.model.TaskType;
import org.ecloudmanager.tmrk.cloudapi.model.TrustedNetworkGroupsType;
import org.ecloudmanager.tmrk.cloudapi.service.network.TrustedNetworkGroupService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;

@Stateless
public class TrustedGroupNetworkService {

    @Inject
    private TrustedNetworkGroupService trustedNetworkGroupService;

    public TrustedNetworkGroupsType getTrustedNetworkGroups(String environmentId) {
        return trustedNetworkGroupService.getTrustedNetworkGroups(environmentId);
    }

    public TrustedNetworkGroupsType createTrustedNetworkGroups(String environmentId,
                                                               JAXBElement<CreateTrustedNetworkGroupType>
                                                                   createTrustedNetworkGroupsType) {
        return trustedNetworkGroupService.createTrustedNetworkGroups(environmentId, createTrustedNetworkGroupsType);
    }

    public TaskType deleteTrustedNetworkGroups(String trustedNetworkGroupId) {
        return trustedNetworkGroupService.deleteTrustedNetworkGroups(trustedNetworkGroupId);
    }
}
