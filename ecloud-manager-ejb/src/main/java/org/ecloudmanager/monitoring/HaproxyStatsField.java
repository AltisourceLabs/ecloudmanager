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

import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ecloudmanager.monitoring.HaproxyStatsField.Applies.*;

public enum HaproxyStatsField {
/*
  0. pxname [LFBS]: proxy name
  1. svname [LFBS]: service name (FRONTEND for frontend, BACKEND for backend,
     any name for server/listener)
  2. qcur [..BS]: current queued requests. For the backend this reports the
     number queued without a server assigned.
  3. qmax [..BS]: max value of qcur
  4. scur [LFBS]: current sessions
  5. smax [LFBS]: max sessions
  6. slim [LFBS]: configured session limit
  7. stot [LFBS]: cumulative number of connections
  8. bin [LFBS]: bytes in
  9. bout [LFBS]: bytes out
 10. dreq [LFB.]: requests denied because of security concerns.
     - For tcp this is because of a matched tcp-request content rule.
     - For http this is because of a matched http-request or tarpit rule.
 11. dresp [LFBS]: responses denied because of security concerns.
     - For http this is because of a matched http-request rule, or
       "option checkcache".
 12. ereq [LF..]: request errors. Some of the possible causes are:
     - early termination from the client, before the request has been sent.
     - read error from the client
     - client timeout
     - client closed connection
     - various bad requests from the client.
     - request was tarpitted.
 13. econ [..BS]: number of requests that encountered an error trying to
     connect to a backend server. The backend stat is the sum of the stat
     for all servers of that backend, plus any connection errors not
     associated with a particular server (such as the backend having no
     active servers).
 14. eresp [..BS]: response errors. srv_abrt will be counted here also.
     Some other errors are:
     - write error on the client socket (won't be counted for the server stat)
     - failure applying filters to the response.
 15. wretr [..BS]: number of times a connection to a server was retried.
 16. wredis [..BS]: number of times a request was redispatched to another
     server. The server value counts the number of times that server was
     switched away from.
 17. status [LFBS]: status (UP/DOWN/NOLB/MAINT/MAINT(via)...)
 18. weight [..BS]: total weight (backend), server weight (server)
 19. act [..BS]: number of active servers (backend), server is active (server)
 20. bck [..BS]: number of backup servers (backend), server is backup (server)
 21. chkfail [...S]: number of failed checks. (Only counts checks failed when
     the server is up.)
 22. chkdown [..BS]: number of UP->DOWN transitions. The backend counter counts
     transitions to the whole backend being down, rather than the sum of the
     counters for each server.
 23. lastchg [..BS]: number of seconds since the last UP<->DOWN transition
 24. downtime [..BS]: total downtime (in seconds). The value for the backend
     is the downtime for the whole backend, not the sum of the server downtime.
 25. qlimit [...S]: configured maxqueue for the server, or nothing in the
     value is 0 (default, meaning no limit)
 26. pid [LFBS]: process id (0 for first instance, 1 for second, ...)
 27. iid [LFBS]: unique proxy id
 28. sid [L..S]: server id (unique inside a proxy)
 29. throttle [...S]: current throttle percentage for the server, when
     slowstart is active, or no value if not in slowstart.
 30. lbtot [..BS]: total number of times a server was selected, either for new
     sessions, or when re-dispatching. The server counter is the number
     of times that server was selected.
 31. tracked [...S]: id of proxy/server if tracking is enabled.
 32. type [LFBS]: (0=frontend, 1=backend, 2=server, 3=socket/listener)
 33. rate [.FBS]: number of sessions per second over last elapsed second
 34. rate_lim [.F..]: configured limit on new sessions per second
 35. rate_max [.FBS]: max number of new sessions per second
 36. check_status [...S]: status of last health check, one of:
        UNK     -> unknown
        INI     -> initializing
        SOCKERR -> socket error
        L4OK    -> check passed on layer 4, no upper layers testing enabled
        L4TOUT  -> layer 1-4 timeout
        L4CON   -> layer 1-4 connection problem, for example
                   "Connection refused" (tcp rst) or "No route to host" (icmp)
        L6OK    -> check passed on layer 6
        L6TOUT  -> layer 6 (SSL) timeout
        L6RSP   -> layer 6 invalid response - protocol error
        L7OK    -> check passed on layer 7
        L7OKC   -> check conditionally passed on layer 7, for example 404 with
                   disable-on-404
        L7TOUT  -> layer 7 (HTTP/SMTP) timeout
        L7RSP   -> layer 7 invalid response - protocol error
        L7STS   -> layer 7 response error, for example HTTP 5xx
 37. check_code [...S]: layer5-7 code, if available
 38. check_duration [...S]: time in ms took to finish last health check
 39. hrsp_1xx [.FBS]: http responses with 1xx code
 40. hrsp_2xx [.FBS]: http responses with 2xx code
 41. hrsp_3xx [.FBS]: http responses with 3xx code
 42. hrsp_4xx [.FBS]: http responses with 4xx code
 43. hrsp_5xx [.FBS]: http responses with 5xx code
 44. hrsp_other [.FBS]: http responses with other codes (protocol error)
 45. hanafail [...S]: failed health checks details
 46. req_rate [.F..]: HTTP requests per second over last elapsed second
 47. req_rate_max [.F..]: max number of HTTP requests per second observed
 48. req_tot [.F..]: total number of HTTP requests received
 49. cli_abrt [..BS]: number of data transfers aborted by the client
 50. srv_abrt [..BS]: number of data transfers aborted by the server
     (inc. in eresp)
 51. comp_in [.FB.]: number of HTTP response bytes fed to the compressor
 52. comp_out [.FB.]: number of HTTP response bytes emitted by the compressor
 53. comp_byp [.FB.]: number of bytes that bypassed the HTTP compressor
     (CPU/BW limit)
 54. comp_rsp [.FB.]: number of HTTP responses that were compressed
 55. lastsess [..BS]: number of seconds since last session assigned to
     server/backend
 56. last_chk [...S]: last health check contents or textual error
 57. last_agt [...S]: last agent check contents or textual error
 58. qtime [..BS]: the average queue time in ms over the 1024 last requests
 59. ctime [..BS]: the average connect time in ms over the 1024 last requests
 60. rtime [..BS]: the average response time in ms over the 1024 last requests
     (0 for TCP)
 61. ttime [..BS]: the average total session time in ms over the 1024 last
     requests
 */

    PROXY_NAME("Service/Group Name", "", "pxname", Type.TEXT, Group.STATUS, LFBS),
    SERVICE_NAME("Server Name/Item Type",
            "FRONTEND for frontend, BACKEND for backend, any name for server/listener",
            "svname", Type.TEXT, Group.STATUS, LFBS),
    CURRENT_QUEUED_REQUESTS("Current Queued Requests", "", "qcur", Type.NUMBER, Group.QUEUE, BS),
    MAX_QUEUED_REQUESTS("Max Queued Requests", "", "qmax", Type.MAX_NUMBER, Group.QUEUE, BS),
    CURRENT_SESSIONS("Current Sessions", "", "scur", Type.NUMBER, Group.SESSIONS, LFBS),
    MAX_SESSIONS("Max Sessions", "smax", "", Type.MAX_NUMBER, Group.SESSIONS, LFBS),
    SESSION_LIMIT("Configured Session Limit", "", "slim", Type.CONFIGURED, Group.SESSIONS, LFBS),
    SESSION_TOTAL("Cumulative Number of Connections", "", "stot", Type.CUMULATIVE, Group.SESSIONS, LFBS),
    BYTES_IN("Bytes In", "", "bin", Type.CUMULATIVE, Group.TRAFFIC, LFBS),
    BYTES_OUT("Bytes Out", "", "bout", Type.CUMULATIVE, Group.TRAFFIC, LFBS),
    REQUESTS_DENIED("Requests Denied",
            "Requests denied because of security concerns. For tcp this is because of a matched tcp-request content rule. " +
            "For http this is because of a matched http-request or tarpit rule.",
            "dreq", Type.CUMULATIVE, Group.ERRORS, LFB),
    RESPONSES_DENIED("Responses Denied",
            "Responses denied because of security concerns. For http this is because of a matched http-request rule, or 'option checkcache'.",
            "dresp", Type.CUMULATIVE, Group.ERRORS, LFBS),
    REQUEST_ERRORS("Request Errors",
            "Request errors. Some of the possible causes are: early termination from the client, before the request has been sent, " +
            "read error from the client, client timeout, client closed connection, various bad requests from the client, request was tarpitted.",
            "ereq", Type.CUMULATIVE, Group.ERRORS, LF),
    CONNECTION_ERRORS("Backend Server Connection Errors",
            "Number of requests that encountered an error trying to connect to a backend server. " +
            "The backend stat is the sum of the stat for all servers of that backend, plus any connection errors not associated " +
            "with a particular server (such as the backend having no active servers).",
            "econ", Type.CUMULATIVE, Group.ERRORS, BS),
    RESPONSE_ERRORS("Response Errors",
            "Response errors. srv_abrt will be counted here also. Some other errors are: write error on the client socket " +
            "(won't be counted for the server stat), failure applying filters to the response.",
            "eresp", Type.CUMULATIVE, Group.ERRORS, BS),
    RETRY_WARNINGS("Retry Warnings", "Number of times a connection to a server was retried", "wretr", Type.CUMULATIVE, Group.ERRORS, BS),
    REDISPATCH_WARNINGS("Redispatch Warnings",
            "Number of times a request was redispatched to another server. The server value counts the number of times " +
            "that server was switched away from.",
            "wredis", Type.CUMULATIVE, Group.ERRORS, BS),
    STATUS("Status", "Status (UP/DOWN/NOLB/MAINT/MAINT(via)...)", "status", Type.TEXT, Group.STATUS, LFBS),
    WEIGHT("Weight", "Total weight (backend), server weight (server)", "weight", Type.NUMBER, Group.STATUS, BS),
    ACTIVE("Active", "Number of active servers (backend), server is active (server)", "act", Type.TEXT, Group.STATUS, BS),
    BACKUP("Backup", "Number of backup servers (backend), server is backup (server)", "bck", Type.TEXT, Group.STATUS, BS),
    FAILED_CHECKS("Failed Server Health Checks",
            "Number of failed checks. (Only counts checks failed when the server is up.)",
            "chkfail", Type.CUMULATIVE, Group.STATUS, S),
    DOWN_TRANSITIONS("Server UP->DOWN transitions",
            "Number of UP->DOWN transitions. The backend counter counts transitions to the whole backend being down, " +
            "rather than the sum of the counters for each server.",
            "chkdown", Type.CUMULATIVE, Group.STATUS, BS),
    LAST_TRANSITION("Last UP<->DOWN Transition", "Number of seconds since the last UP<->DOWN transition", "lastchg", Type.NUMBER, Group.STATUS, BS),
    DOWNTIME("Downtime",
            "Total downtime (in seconds). The value for the backend is the downtime for the whole backend, " +
            "not the sum of the server downtime.",
            "downtime", Type.CUMULATIVE, Group.STATUS, BS),
    QUEUE_LIMIT("Configured Queue Limit",
            "Configured maxqueue for the server, or nothing in the value is 0 (default, meaning no limit)",
            "qlimit", Type.CONFIGURED, Group.QUEUE, S),
    PID("Process Id", "Process id (0 for first instance, 1 for second, ...)", "pid", Type.TEXT, Group.STATUS, LFBS),
    IID("Proxy Id", "Unique proxy id", "iid", Type.TEXT, Group.STATUS, LFBS),
    SID("Server Id", "Server id (unique inside a proxy)", "sid", Type.TEXT, Group.STATUS, LS),
    THROTTLE("Throttle",
            "Current throttle percentage for the server, when slowstart is active, or no value if not in slowstart.",
            "throttle", Type.TEXT, Group.STATUS, S),
    LB_TOTAL("Number of Times a Server was Selected",
            "Total number of times a server was selected, either for new sessions, or when re-dispatching.",
            "lbtot", Type.CUMULATIVE, Group.STATUS, BS),
    TRACKED("Tracking Id", "Id of proxy/server if tracking is enabled.", "tracked", Type.TEXT, Group.STATUS, S),
    TYPE("Type", "(0=frontend, 1=backend, 2=server, 3=socket/listener)", "type", Type.TEXT, Group.STATUS, LFBS),
    RATE("Sessions per Second", "Number of sessions per second over last elapsed second", "rate", Type.NUMBER, Group.SESSIONS, FBS),
    RATE_LIMIT("Configured Session Rate Limit", "Configured limit on new sessions per second", "rate_lim", Type.CONFIGURED, Group.SESSIONS, F),
    RATE_MAX("Max Number of New Sessions", "Max number of new sessions per second", "rate_max", Type.MAX_NUMBER, Group.SESSIONS, FBS),
    CHECK_STATUS("Status of Last Health Check", "See haproxy documentation for the status codes", "check_status", Type.TEXT, Group.STATUS, S),
    CHECK_CODE("Health Check Code", "Layer5-7 code, if available", "check_code", Type.TEXT, Group.STATUS, S),
    CHECK_DURATION("Last Health Check Duration", "Time in ms took to finish last health check", "check_duration", Type.NUMBER, Group.STATUS, S),
    HRSP_1XX("HTTP Responses With 1xx Code", "", "hrsp_1xx", Type.CUMULATIVE, Group.RESPONSE, FBS),
    HRSP_2XX("HTTP Responses With 2xx Code", "", "hrsp_2xx", Type.CUMULATIVE, Group.RESPONSE, FBS),
    HRSP_3XX("HTTP Responses With 3xx Code", "", "hrsp_3xx", Type.CUMULATIVE, Group.RESPONSE, FBS),
    HRSP_4XX("HTTP Responses With 4xx Code", "", "hrsp_4xx", Type.CUMULATIVE, Group.RESPONSE, FBS),
    HRSP_5XX("HTTP Responses With 5xx Code", "", "hrsp_5xx", Type.CUMULATIVE, Group.RESPONSE, FBS),
    HRSP_OTHER("HTTP Responses With Other Codes", "Http responses with other codes (protocol error)", "hrsp_other", Type.CUMULATIVE, Group.RESPONSE, FBS),
    CHECK_DETAILS("Failed Health Checks Details", "", "hanafail", Type.TEXT, Group.STATUS, S),
    REQ_RATE("HTTP Requests per Second", "HTTP requests per second over last elapsed second", "req_rate", Type.NUMBER, Group.SESSIONS, F),
    REQ_RATE_MAX("Max HTTP Requests per Second", "Max number of HTTP requests per second observed", "req_rate_max", Type.MAX_NUMBER, Group.SESSIONS, F),
    REQ_TOT("HTTP Requests Total", "Total number of HTTP requests received", "req_tot", Type.CUMULATIVE, Group.SESSIONS, F),
    CLI_ABRT("Aborted by the Client", "Number of data transfers aborted by the client", "cli_abrt", Type.CUMULATIVE, Group.ERRORS, BS),
    SRV_ABRT("Aborted by the Server",
            "Number of data transfers aborted by the server (inc. in response errors)",
            "srv_abrt", Type.CUMULATIVE, Group.ERRORS, BS),
    COMP_IN("HTTP Response Bytes Compressor In", "Number of HTTP response bytes fed to the compressor", "comp_in", Type.CUMULATIVE, Group.STATUS, FB),
    COMP_OUT("HTTP Response Bytes Compressor Out", "Number of HTTP response bytes emitted by the compressor", "comp_out", Type.CUMULATIVE, Group.STATUS, FB),
    COMP_BYP("HTTP Response Bytes Compressor Bypassed",
            "Number of bytes that bypassed the HTTP compressor (CPU/BW limit)",
            "comp_byp", Type.CUMULATIVE, Group.STATUS, FB),
    COMP_RSP("HTTP Responses Compressed", "Number of HTTP responses that were compressed", "comp_rsp", Type.CUMULATIVE, Group.STATUS, FB),
    LASTSESS("Seconds Since Last Session", "Number of seconds since last session assigned to server/backend", "lastsess", Type.NUMBER, Group.SESSIONS, BS),
    LAST_CHK("Last Health Check Contents", "Last health check contents or textual error", "last_chk", Type.TEXT, Group.STATUS, S),
    LAST_AGT("Last Agent Check Contents", "Last agent check contents or textual error", "last_agt", Type.TEXT, Group.STATUS, S),
    QTIME("Average Queue Time", "The average queue time in ms over the 1024 last requests", "qtime", Type.NUMBER, Group.LATENCY, BS),
    CTIME("Average Connect Time", "The average connect time in ms over the 1024 last requests", "ctime", Type.NUMBER, Group.LATENCY, BS),
    RTIME("Average Response Time", "The average response time in ms over the 1024 last requests (0 for TCP)", "rtime", Type.NUMBER, Group.LATENCY, BS),
    TTIME("Average Total Time", "The average total time in ms over the 1024 last requests", "ttime", Type.NUMBER, Group.LATENCY, BS)
    ;

    public enum Type {NUMBER, TEXT, MAX_NUMBER, CUMULATIVE, CONFIGURED}
    public enum Group {STATUS, QUEUE, SESSIONS, RESPONSE, TRAFFIC, ERRORS, LATENCY}
    enum Applies {
        _L, _F, _B, _S; // Listener, Frontend, Backend, Server

        public static EnumSet<Applies> LFBS = EnumSet.of(_L, _F, _B, _S);
        public static EnumSet<Applies> BS = EnumSet.of(_B, _S);
        public static EnumSet<Applies> LS = EnumSet.of(_L, _S);
        public static EnumSet<Applies> FBS = EnumSet.of(_F, _B, _S);
        public static EnumSet<Applies> F = EnumSet.of(_F);
        public static EnumSet<Applies> S = EnumSet.of(_S);
        public static EnumSet<Applies> FB = EnumSet.of(_F, _B);
        public static EnumSet<Applies> LFB = EnumSet.of(_L, _F, _B);
        public static EnumSet<Applies> LF = EnumSet.of(_L, _F);
    }

    private String name;
    private String description;
    private String key;
    private Type type;
    private Group group;

    private EnumSet<Applies> appliesTo;

    HaproxyStatsField(String name, String description, String key, Type type, Group group, EnumSet<Applies> appliesTo) {
        this.name = name;
        this.description = description;
        this.key = key;
        this.type = type;
        this.group = group;
        this.appliesTo = appliesTo;
    }

//    public static Set<HaproxyStatsField> getFields(Group group) {
//        return Stream.of(values()).filter(f -> f.getGroup() == group).collect(Collectors.toSet());
//    }

    public static Set<HaproxyStatsField> getFields(Group group, DeploymentObject deploymentObject) {
        return Stream.of(values())
                .filter(f -> {
                    if (f.getGroup() != group) {
                        return false;
                    }

                    if (deploymentObject instanceof ProducedServiceDeployment) {
                        return f.appliesTo.contains(_F);
                    } else if (deploymentObject instanceof ComponentGroupDeployment) {
                        return f.appliesTo.contains(_B);
                    } else if (deploymentObject instanceof VMDeployment) {
                        return f.appliesTo.contains(_S);
                    }

                    return false;
                })
                .collect(Collectors.toSet());
    }

    public boolean isGraphSupported() {
        return type == Type.NUMBER || type == Type.CUMULATIVE;
    }

    public Type getType() {
        return type;
    }

    public Group getGroup() {
        return group;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
