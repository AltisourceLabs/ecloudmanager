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

package org.ecloudmanager.service.execution;

import org.apache.logging.log4j.LogManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ActionExecutor executor = new ActionExecutor();
        executor.setExecutorService(Executors.newFixedThreadPool(10));
        executor.setLog(LogManager.getLogger(ActionExecutor.class));

//        SingleAction a1 = Action.single("first", 5));
//        SingleAction a2 = Action.single("second", 3), a1);
//        SingleAction e1 = Action.single("fail-1", "Error"), a1);
//        SingleAction a3 = Action.single("three", 5));
//        SingleAction a4 = Action.single("four", 3), a1, e1);
//
//        SingleAction a0 = Action.single("zero", 5));
//        ActionGroup ag1 = new ActionGroup("firstGroup", null, null);
//        ag1.addActions(a1);
//        ag1.addActions(a2);
//        ag1.addActions(a3);
//        ag1.addActions(a4);
//        ag1.addActions(e1);
//        ag1.addDependencies(a0);
//        System.out.println(ag1);
//
//        ActionGroup ag0 = new ActionGroup("zeroGroup", null, null);
//        ag0.addActions(a0);
//        ag0.addActions(ag1);
//        //executor.execute(ag0);
//        System.out.println(ag0);
        Action serviceHAProxy = Action.actionSequence("HAProxy Service",
            Action.single("HAProxy-CreateVM", new Task(5)),
            Action.single("HAProxy-ChefProvisioning", new Task(4)));

        Action serviceIAM = Action.actionSequence("IAM Service",
            Action.single("IAM-CreateVM", new Task(1)),
            Action.single("IAM-ChefProvisioning", new Task(6)),
            Action.single("IAM-ConfigureHAProxy", new Task(3)));

        Action serviceMYSQL = Action.actionSequence("MySQL Service",
            Action.single("MySQL-CreateVM", new Task(5)),
            Action.single("MySQL-ChefProvisioning", new Task(4)),
            Action.single("MySQL-ConfigureHAProxy", new Task(7)));

        Action serviceOpenDJ = Action.actionSequence("OpenDJ Service",
            Action.single("OpenDJ-CreateVM", new Task(1)),
            Action.single("OpenDJ-ChefProvisioning", new Task(6)),
            Action.single("OpenDJ-ConfigureHAProxy", new Task(3)));
        serviceIAM.addDependencies(serviceMYSQL, serviceOpenDJ);
//        serviceIAM.addDependencies(serviceMYSQL);

        Action services = Action.actionGroup("Services", serviceIAM, serviceMYSQL, serviceOpenDJ);
        Action deploymentPlan = Action.actionSequence("deployment", serviceHAProxy, services);
        System.out.println(deploymentPlan);
        executor.execute(deploymentPlan, () -> {
            executor.shutdown();
            System.out.println(deploymentPlan);
        });
        System.out.println(deploymentPlan);
        System.out.println("-------------------------------------");
        executor.rollback(serviceIAM);
        System.out.println(deploymentPlan);
    }

    private static class Task implements Callable {
        private String name;
        private int s = 1;
        private RuntimeException t;

        public Task(int s) {
            this.s = s;
        }

        public Task(String name, String error) {
            this.name = name;
            this.t = new RuntimeException(error);
        }

        public String toString() {
            return "Task: " + name;
        }

        @Override
        public Object call() throws Exception {
            System.out.println("Executing: " + toString());
//            try {
//                Thread.sleep(s*1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            if (t != null) {
                throw t;
            }

            return null;
        }
    }
}
