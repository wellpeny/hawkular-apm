/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.apm.tests.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.hawkular.apm.api.model.trace.Consumer;
import org.hawkular.apm.api.model.trace.ContainerNode;
import org.hawkular.apm.api.model.trace.CorrelationIdentifier;
import org.hawkular.apm.api.model.trace.Node;
import org.hawkular.apm.api.model.trace.Producer;
import org.hawkular.apm.tests.server.TestTraceServer;
import org.junit.After;
import org.junit.Before;

/**
 * @author gbrown
 */
public abstract class ClientTestBase {

    private TestTraceServer testAPMServer = new TestTraceServer();

    public int getPort() {
        return 8080;
    }

    @Before
    public void init() {
        try {
            testAPMServer.setPort(getPort());
            testAPMServer.setShutdownTimer(-1); // Disable timer
            testAPMServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setProcessHeaders(false);
        setProcessContent(false);
    }

    @After
    public void close() {
        try {
            testAPMServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            synchronized (this) {
                wait(2000);
            }
        } catch (Exception e) {
            fail("Failed to wait after test close");
        }
    }

    /**
     * @return the testAPMServer
     */
    public TestTraceServer getTestTraceServer() {
        return testAPMServer;
    }

    /**
     * @param testAPMServer the testAPMServer to set
     */
    public void setTestAPMServer(TestTraceServer testAPMServer) {
        this.testAPMServer = testAPMServer;
    }

    /**
     * This method checks that two correlation identifiers are equivalent.
     *
     * @param producer The producer
     * @param consumer The consumer
     */
    protected void checkInteractionCorrelationIdentifiers(Producer producer, Consumer consumer) {
        CorrelationIdentifier pcid = producer.getCorrelationIds().iterator().next();
        CorrelationIdentifier ccid = consumer.getCorrelationIds().iterator().next();

        assertEquals(pcid, ccid);
    }

    /**
     * This method finds nodes within a hierarchy of the required type.
     *
     * @param nodes The nodes to recursively check
     * @param cls The class of interest
     * @param results The results
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> void findNodes(List<Node> nodes, Class<T> cls, List<T> results) {
        for (Node n : nodes) {
            if (n instanceof ContainerNode) {
                findNodes(((ContainerNode) n).getNodes(), cls, results);
            }

            if (cls.isAssignableFrom(n.getClass())) {
                results.add((T) n);
            }
        }
    }

    protected void setProcessHeaders(boolean b) {
        System.setProperty("hawkular-apm.test.process.headers", ""+b);
    }

    protected void setProcessContent(boolean b) {
        System.setProperty("hawkular-apm.test.process.content", ""+b);
    }

    protected boolean isProcessHeaders() {
        return Boolean.getBoolean("hawkular-apm.test.process.headers");
    }

    protected boolean isProcessContent() {
        return Boolean.getBoolean("hawkular-apm.test.process.content");
    }
}
