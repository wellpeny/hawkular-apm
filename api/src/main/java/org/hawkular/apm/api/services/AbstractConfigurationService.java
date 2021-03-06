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
package org.hawkular.apm.api.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawkular.apm.api.internal.actions.ProcessorActionHandler;
import org.hawkular.apm.api.internal.actions.ProcessorActionHandlerFactory;
import org.hawkular.apm.api.model.config.btxn.BusinessTxnConfig;
import org.hawkular.apm.api.model.config.btxn.ConfigMessage;
import org.hawkular.apm.api.model.config.btxn.Processor;
import org.hawkular.apm.api.model.config.btxn.ProcessorAction;
import org.hawkular.apm.api.model.trace.Issue;
import org.hawkular.apm.api.model.trace.ProcessorIssue;

/**
 * This class provides the abstract base class for the Configuration Service.
 *
 * @author gbrown
 */
public abstract class AbstractConfigurationService implements ConfigurationService {

    /**  */
    private static final String NO_FILTERS = "No inclusion or exclusion filters have been defined";

    /* (non-Javadoc)
     * @see org.hawkular.apm.api.services.ConfigurationService#validateBusinessTransaction(
     *                  org.hawkular.apm.api.model.config.btxn.BusinessTxnConfig)
     */
    @Override
    public List<ConfigMessage> validateBusinessTransaction(BusinessTxnConfig config) {
        List<ConfigMessage> messages = new ArrayList<ConfigMessage>();

        // Check that atleast one filter has been defined
        if (config.getFilter() == null || (config.getFilter().getInclusions().isEmpty()
                && config.getFilter().getInclusions().isEmpty())) {

            ConfigMessage cm = new ConfigMessage();
            cm.setMessage(NO_FILTERS);
            messages.add(cm);
        }

        for (Processor processor : config.getProcessors()) {

            for (ProcessorAction action : processor.getActions()) {

                ProcessorActionHandler handler = ProcessorActionHandlerFactory.getHandler(action);

                if (handler != null) {
                    handler.init(processor);

                    if (handler.getIssues() != null) {
                        for (Issue issue : handler.getIssues()) {
                            ConfigMessage cm = new ConfigMessage();
                            cm.setMessage(issue.getDescription());
                            if (issue instanceof ProcessorIssue) {
                                cm.setProcessor(((ProcessorIssue)issue).getProcessor());
                                cm.setAction(((ProcessorIssue)issue).getAction());
                                cm.setField(((ProcessorIssue)issue).getField());
                            }
                            cm.setSeverity(issue.getSeverity());
                            messages.add(cm);
                        }
                    }
                }
            }
        }

        return messages;
    }

    /* (non-Javadoc)
     * @see org.hawkular.apm.api.services.ConfigurationService#setBusinessTransactions(java.lang.String, java.util.Map)
     */
    @Override
    public List<ConfigMessage> setBusinessTransactions(String tenantId, Map<String, BusinessTxnConfig> configs)
            throws Exception {
        List<ConfigMessage> messages = new ArrayList<ConfigMessage>();

        for (String name : configs.keySet()) {
            messages.addAll(setBusinessTransaction(tenantId, name, configs.get(name)));
        }

        return messages;
    }

}
