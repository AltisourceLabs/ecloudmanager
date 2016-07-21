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

package org.ecloudmanager.web.faces;

import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.service.deployment.geolite.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class GeolocationRuleController extends EntityEditorController<GeolocationRule> implements Serializable {
    @Inject
    private transient GeolocationService geolocationService;
    @Inject
    ProducedServiceDeploymentController producedServiceDeploymentController;

    private GeolocationRecord geolocationRecord;

    private Collection<GeolocationRecord> geolocationRecords;
    private List<GeolocationRecord> selectedGeolocationRecords;
    private List<GeolocationRecord> filteredLocations;

    private boolean showCities;
    private String operator = "AND";

    public GeolocationRuleController() {
        super(GeolocationRule.class);
    }

    @PostConstruct
    private void initPostConstruct() {
        showCities = false;
        geolocationRecords = geolocationService.getCountries();
    }

    private List<GeolocationRule> getRules() {
        return producedServiceDeploymentController.getValue().getHaProxyFrontendConfig().getGeolocationRules();
    }

    @Override
    public void delete(GeolocationRule entity) {
        getRules().remove(entity);
    }

    @Override
    protected void doSave(GeolocationRule old, GeolocationRule entity) {
        List<GeolocationRule> rules = getRules();
        int position = rules.indexOf(old);
        rules.add(position, entity);
        rules.remove(old);
    }

    @Override
    protected void doAdd(GeolocationRule entity) {
        getRules().add(entity);
    }

    public Collection<GeolocationRecord> completeGeolocation(String query) {
        Collection<GeolocationRecord> allLocations = geolocationService.getCountries();
        allLocations.addAll(geolocationService.getCities());
        Collection<GeolocationRecord> filteredLocations = new ArrayList<>();

        for (GeolocationRecord record : allLocations) {
            if (record.getLabel().toLowerCase().startsWith(query)) {
                filteredLocations.add(record);
            }
        }

        return filteredLocations;
    }

    public GeolocationRecord getGeolocationRecord() {
        return geolocationRecord;
    }

    public void setGeolocationRecord(GeolocationRecord geolocationRecord) {
        this.geolocationRecord = geolocationRecord;
    }

    public Collection<GeolocationRecord> getGeolocationRecords() {
        return geolocationRecords;
    }

    public void setGeolocationRecords(List<GeolocationRecord> geolocationRecords) {
        this.geolocationRecords = geolocationRecords;
    }

    public List<GeolocationRecord> getSelectedGeolocationRecords() {
        return selectedGeolocationRecords;
    }

    public void setSelectedGeolocationRecords(List<GeolocationRecord> selectedGeolocationRecords) {
        this.selectedGeolocationRecords = selectedGeolocationRecords;
    }

    public List<GeolocationRecord> getFilteredLocations() {
        return filteredLocations;
    }

    public void setFilteredLocations(List<GeolocationRecord> filteredLocations) {
        this.filteredLocations = filteredLocations;
    }

    public boolean getShowCities() {
        return showCities;
    }

    public void setShowCities(boolean showCities) {
        this.showCities = showCities;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void addLocation(boolean isNegated, GeolocationRecord geolocationRecord) {
        GeolocationExpr expr = new GeolocationExpr();
        expr.setRecord(geolocationRecord);
        expr.setNegate(isNegated);
        expr.setOperator(AclOperator.valueOf(operator));
        selected.getLocations().add(expr);
    }

    public void showCitiesChanged() {
        geolocationRecords = showCities ? geolocationService.getCities() : geolocationService.getCountries();
        if (selectedGeolocationRecords != null) {
            selectedGeolocationRecords.clear();
        }
    }

    public void removeGeoExpr(GeolocationExpr expr) {
        selected.getLocations().remove(expr);
    }
}
