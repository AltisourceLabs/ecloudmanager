/*
 *  MIT License
 *
 *  Copyright (c) 2016  Altisource
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package org.ecloudmanager.service.aws;

import com.google.common.collect.ComparisonChain;

import java.util.stream.Stream;

public enum AWSInstanceType {
    T2_NANO("t2.nano", 1, 10, 0.5, true, 0.0065),
    T2_MICRO("t2.micro", 1, 10, 1, true, 0.013),
    T2_SMALL("t2.small", 1, 10, 2, true, 0.026),
    T2_MEDIUM("t2.medium", 2, 10, 4, true, 0.052),
    T2_LARGE("t2.large", 2, 10, 8, true, 0.104),
    M4_LARGE("m4.large", 2, 6.5, 8, true, 0.12),
    M4_XLARGE("m4.xlarge", 4, 13, 16, true, 0.239),
    M4_2XLARGE("m4.2xlarge", 8, 26, 32, true, 0.479),
    M4_4XLARGE("m4.4xlarge", 16, 53.5, 64, true, 0.958),
    M4_10XLARGE("m4.10xlarge", 40, 124.5, 160, true, 2.394),
    M3_MEDIUM("m3.medium", 1, 3, 3.75, false, 0.067),
    M3_LARGE("m3.large", 2, 6.5, 7.5, false, 0.133),
    M3_XLARGE("m3.xlarge", 4, 13, 15, false, 0.266),
    M3_2XLARGE("m3.2xlarge", 8, 26, 30, false, 0.532),
    C4_LARGE("c4.large", 2, 8, 3.75, true, 0.105),
    C4_XLARGE("c4.xlarge", 4, 16, 7.5, true, 0.209),
    C4_2XLARGE("c4.2xlarge", 8, 31, 15, true, 0.419),
    C4_4XLARGE("c4.4xlarge", 16, 62, 30, true, 0.838),
    C4_8XLARGE("c4.8xlarge", 36, 132, 60, true, 1.675),
    C3_LARGE("c3.large", 2, 7, 3.75, false, 0.105),
    C3_XLARGE("c3.xlarge", 4, 14, 7.5, false, 0.21),
    C3_2XLARGE("c3.2xlarge", 8, 28, 15, false, 0.42),
    C3_4XLARGE("c3.4xlarge", 16, 55, 30, false, 0.84),
    C3_8XLARGE("c3.8xlarge", 32, 108, 60, false, 1.68),
    G2_2XLARGE("g2.2xlarge", 8, 26, 15, false, 0.65),
    G2_8XLARGE("g2.8xlarge", 32, 104, 60, false, 2.6),
    R3_LARGE("r3.large", 2, 6.5, 15, false, 0.166),
    R3_XLARGE("r3.xlarge", 4, 13, 30.5, false, 0.333),
    R3_2XLARGE("r3.2xlarge", 8, 26, 61, false, 0.665),
    R3_4XLARGE("r3.4xlarge", 16, 52, 122, false, 1.33),
    R3_8XLARGE("r3.8xlarge", 32, 104, 244, false, 2.66),
    I2_XLARGE("i2.xlarge", 4, 14, 30.5, false, 0.853),
    I2_2XLARGE("i2.2xlarge", 8, 27, 61, false, 1.705),
    I2_4XLARGE("i2.4xlarge", 16, 53, 122, false, 3.41),
    I2_8XLARGE("i2.8xlarge", 32, 104, 244, false, 6.82),
    D2_XLARGE("d2.xlarge", 4, 14, 30.5, false, 0.69),
    D2_2XLARGE("d2.2xlarge", 8, 28, 61, false, 1.38),
    D2_4XLARGE("d2.4xlarge", 16, 56, 122, false, 2.76),
    D2_8XLARGE("d2.8xlarge", 36, 116, 244, false, 5.52);

    private final String instanceTypeName;
    private final int cpu;
    private final double ecu;
    private final double memory;
    private final boolean isEbs;
    private final double pricePerHour;

    AWSInstanceType(String instanceTypeName, int cpu, double ecu, double memory, boolean isEbs, double pricePerHour) {
        this.instanceTypeName = instanceTypeName;
        this.cpu = cpu;
        this.ecu = ecu;
        this.memory = memory;
        this.isEbs = isEbs;
        this.pricePerHour = pricePerHour;
    }

    public String getInstanceTypeName() {
        return instanceTypeName;
    }

    public int getCpu() {
        return cpu;
    }

    public double getMemory() {
        return memory;
    }

    public boolean isEbs() {
        return isEbs;
    }

    public double getEcu() {
        return ecu;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public static AWSInstanceType get(int cpu, int mem) {
        double memory = mem / 1024d;
        return Stream.of(values())
                .filter(v -> v.getCpu() >= cpu && v.getMemory() >= memory)
                .sorted((b,a) -> {
                    return ComparisonChain.start()
                            .compare(a.isEbs(), b.isEbs())
                            .compare(b.getPricePerHour(), a.getPricePerHour())
                            .compare(a.getEcu(), b.getEcu())
                            .result();
                })
                .findFirst()
                .orElse(D2_8XLARGE);
    }

    public static AWSInstanceType get(String instanceTypeName) {
        return Stream.of(values())
                .filter(it -> it.getInstanceTypeName().equals(instanceTypeName))
                .findAny()
                .orElse(null);
    }

    public boolean isCompatible(AWSInstanceType other) {
        return isEbs() && other.isEbs();
    }
}
