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

package org.ecloudmanager.repository.deployment;

import ch.qos.logback.classic.Level;
import org.ecloudmanager.domain.LoggingEventEntity;
import org.ecloudmanager.jeecore.repository.MongoDBRepositorySupport;
import org.ecloudmanager.jeecore.repository.Repository;
import org.ecloudmanager.node.LoggingEventListener;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.node.util.NodeUtil;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SingleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class LoggingEventRepository extends MongoDBRepositorySupport<LoggingEventEntity> {
    public List<LoggingEventEntity> findForActions(Collection<SingleAction> actions) {
        Collection<String> actionIds = actions.stream().map(Action::getId).collect(Collectors.toSet());
        return datastore.createQuery(getEntityType())
                .field("actionId").in(actionIds)
                .asList().stream()
                .sorted((o1, o2) -> (o1.getEvent().getTimeStamp() < o2.getEvent().getTimeStamp()) ? -1 : ((o1.getEvent().getTimeStamp() == o2.getEvent().getTimeStamp()) ? 0 : 1))
                .collect(Collectors.toList());
    }

    public ActionLogger createActionLogger(Class caller, String actionId) {
        return new ActionLogger(caller, actionId);
    }

    public class ActionLogger implements LoggingEventListener {
        Logger logger;
        private String actionId;
        private String fqcn;

        ActionLogger(Class caller, String actionId) {
            this.actionId = actionId;
            this.fqcn = caller.getName();
            logger = LoggerFactory.getLogger(fqcn);
        }

        @Override
        public void log(Collection<LoggingEvent> events) {
            saveAll(events.stream().map(e -> new LoggingEventEntity(actionId, e)).collect(Collectors.toList()));
        }


        private void recordEvent(Level level, String msg, Object[] args, Throwable throwable) {
            ch.qos.logback.classic.spi.LoggingEvent logbackEvent = new ch.qos.logback.classic.spi.LoggingEvent(fqcn, (ch.qos.logback.classic.Logger) logger, level, msg, throwable, args);

            save(new LoggingEventEntity(actionId, NodeUtil.fromLogback(logbackEvent)));
        }

        public void error(String msg, Throwable t) {
            recordEvent(Level.ERROR, msg, null, t);
        }

        public void error(String msg) {
            recordEvent(Level.ERROR, msg, null, null);
        }

        public void info(String msg) {
            recordEvent(Level.INFO, msg, null, null);
        }
    }
}
