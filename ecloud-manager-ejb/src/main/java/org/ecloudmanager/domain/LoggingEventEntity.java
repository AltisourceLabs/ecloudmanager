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

package org.ecloudmanager.domain;

import org.ecloudmanager.jeecore.domain.MongoObject;
import org.ecloudmanager.node.model.LoggingEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingEventEntity extends MongoObject {
    private LoggingEvent event;
    private String actionId;

    LoggingEventEntity() {
    }

    public LoggingEventEntity(String actionId, LoggingEvent event) {
        this.actionId = actionId;
        this.event = event;
    }

    public LoggingEvent getEvent() {
        return event;
    }

    public void setEvent(LoggingEvent event) {
        this.event = event;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }


    public String getDate() {
        return new SimpleDateFormat("HH:mm:ss,S").format(new Date(event.getTimeStamp()));
    }

    public String getUncoloredMessageStr() {
        return event.getMessage() == null ? null : event.getMessage().replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
