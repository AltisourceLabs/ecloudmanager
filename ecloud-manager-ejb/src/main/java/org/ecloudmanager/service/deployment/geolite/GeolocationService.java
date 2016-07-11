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

package org.ecloudmanager.service.deployment.geolite;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GeolocationService {
    @Inject
    private Logger log;

    private Map<String, GeolocationRecord> cities;
    private Map<String, GeolocationRecord> countries;

    @PostConstruct
    private void init() {
        try {
            URL cityUrl = getClass().getClassLoader().getResource("/GeoLite2-City-Locations-en.csv");
            List<CSVRecord> cityRecords = CSVParser.parse(
                    cityUrl,
                    Charset.defaultCharset(),
                    CSVFormat.DEFAULT.withHeader()
                ).getRecords();
            URL countryUrl = getClass().getClassLoader().getResource("/GeoLite2-Country-Locations-en.csv");
            List<CSVRecord> countryRecords = CSVParser.parse(
                    countryUrl,
                    Charset.defaultCharset(),
                    CSVFormat.DEFAULT.withHeader()
                ).getRecords();
            cities = cityRecords.stream().map(record -> {
                StringBuilder labelBuilder = new StringBuilder();
                List<String> items = new ArrayList<>();
                String country = (record.get("country_iso_code") + " " + record.get("country_name")).trim();
                items.add(country);
                String subdivision = (record.get("subdivision_1_name") + " " + record.get("subdivision_2_name")).trim();
                items.add(subdivision);
                String city = record.get("city_name").trim();
                items.add(city);
                items.forEach(item -> {
                    if (!StringUtils.isEmpty(item.trim())) {
                        if (labelBuilder.length() > 0) {
                            labelBuilder.append(", ");
                        }
                        labelBuilder.append(item);
                    }
                });
                return new GeolocationRecord(record.get("geoname_id"), country, subdivision, city, labelBuilder.toString());
            }).collect(Collectors.toMap(GeolocationRecord::getGeoid, record -> record));
            countries = countryRecords.stream().map(record -> {
                String label = record.get("country_iso_code") + " " + record.get("country_name");
                return new GeolocationRecord(record.get("geoname_id"), label, "", "", label);
            }).collect(Collectors.toMap(GeolocationRecord::getGeoid, record -> record));

        } catch (IOException e) {
            log.error("Cannot initialize geolocation service", e);
        }
    }

    public Collection<GeolocationRecord> getCities() {
        return cities.values();
    }

    public Collection<GeolocationRecord> getCountries() {
        return countries.values();
    }

    public GeolocationRecord getByGeoid(String geoId) {
        GeolocationRecord country = countries.get(geoId);
        return country == null ? cities.get(geoId) : country;
//        return getCountries().stream()
//                .filter(geolocationRecord -> geolocationRecord.getGeoid().equals(geoId))
//                .findAny()
//                .orElse(
//                        getCities().stream()
//                            .filter(geolocationRecord -> geolocationRecord.getGeoid().equals(geoId)).findAny()
//                            .orElse(null)
//                );
    }
}
