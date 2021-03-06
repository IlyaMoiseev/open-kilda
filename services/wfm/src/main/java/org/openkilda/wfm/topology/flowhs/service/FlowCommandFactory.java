/* Copyright 2019 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.wfm.topology.flowhs.service;

import org.openkilda.floodlight.flow.request.InstallIngressRule;
import org.openkilda.floodlight.flow.request.InstallTransitRule;
import org.openkilda.floodlight.flow.request.RemoveRule;
import org.openkilda.model.Flow;
import org.openkilda.model.FlowPath;
import org.openkilda.wfm.CommandContext;

import java.util.List;

public interface FlowCommandFactory {
    /**
     * Build install commands for transit(if needed) and egress rules for active forward and reverse paths.
     *
     * @param context command context.
     * @param flow    flow data which defines endpoints and path segments for rules to be created.
     * @return list of the install commands.
     */
    List<InstallTransitRule> createInstallNonIngressRules(CommandContext context, Flow flow);

    /**
     * Build install commands for transit(if needed) and egress rules for provided paths.
     *
     * @param context     command context.
     * @param flow        flow data which defines only endpoints for rules to be created.
     * @param forwardPath forward path which defines path segments for rules to be created.
     * @param reversePath reverse path which defines path segments for rules to be created.
     * @return list of the install commands.
     */
    public List<InstallTransitRule> createInstallNonIngressRules(CommandContext context, Flow flow,
                                                                 FlowPath forwardPath, FlowPath reversePath);

    /**
     * Build install commands for ingress rules for active forward and reverse paths.
     *
     * @param context command context.
     * @param flow    flow data which defines endpoints and path segments for rules to be created.
     * @return list of the install commands.
     */
    List<InstallIngressRule> createInstallIngressRules(CommandContext context, Flow flow);

    /**
     * Build install commands for ingress rules for provided paths.
     *
     * @param context     command context.
     * @param flow        flow data which defines only endpoints for rules to be created.
     * @param forwardPath forward path which defines path segments for rules to be created.
     * @param reversePath reverse path which defines path segments for rules to be created.
     * @return list of the install commands.
     */
    public List<InstallIngressRule> createInstallIngressRules(CommandContext context, Flow flow,
                                                              FlowPath forwardPath, FlowPath reversePath);

    /**
     * Build remove commands for transit(if needed) and egress rules for active forward and reverse paths.
     *
     * @param context command context.
     * @param flow    flow data which defines endpoints and path segments for rules to be removed.
     * @return list of the remove commands.
     */
    List<RemoveRule> createRemoveNonIngressRules(CommandContext context, Flow flow);

    /**
     * Build remove commands for transit(if needed) and egress rules deletion for provided paths.
     *
     * @param context     command context.
     * @param flow        flow data which defines only endpoints for rules to be removed.
     * @param forwardPath forward path which defines path segments for rules to be removed.
     * @param reversePath reverse path which defines path segments for rules to be removed.
     * @return list of the remove commands.
     */
    List<RemoveRule> createRemoveNonIngressRules(CommandContext context, Flow flow,
                                                 FlowPath forwardPath, FlowPath reversePath);

    /**
     * Build remove commands for ingress rules for active forward and reverse paths.
     *
     * @param context command context.
     * @param flow    flow data which defines endpoints and path segments for rules to be removed.
     * @return list of the remove commands.
     */
    List<RemoveRule> createRemoveIngressRules(CommandContext context, Flow flow);

    /**
     * Build remove commands for ingress rules for active forward and reverse paths.
     *
     * @param context     command context.
     * @param flow        flow data which defines only endpoints for rules to be removed.
     * @param forwardPath forward path which defines path segments for rules to be removed.
     * @param reversePath reverse path which defines path segments for rules to be removed.
     * @return list of the remove commands.
     */
    List<RemoveRule> createRemoveIngressRules(CommandContext context, Flow flow,
                                              FlowPath forwardPath, FlowPath reversePath);
}
