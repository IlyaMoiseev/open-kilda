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

package org.openkilda.wfm.topology.flowhs.fsm.reroute.actions;

import static java.lang.String.format;

import org.openkilda.floodlight.flow.request.InstallTransitRule;
import org.openkilda.floodlight.flow.response.FlowResponse;
import org.openkilda.floodlight.flow.response.FlowRuleResponse;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteContext;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm.Event;
import org.openkilda.wfm.topology.flowhs.validation.rules.NonIngressRulesValidator;
import org.openkilda.wfm.topology.flowhs.validation.rules.RulesValidator;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class ValidateNonIngressRulesAction extends RuleProcessingAction {

    @Override
    protected void perform(FlowRerouteFsm.State from, FlowRerouteFsm.State to,
                           FlowRerouteFsm.Event event, FlowRerouteContext context, FlowRerouteFsm stateMachine) {
        FlowResponse response = context.getFlowResponse();
        UUID commandId = response.getCommandId();
        stateMachine.getPendingCommands().remove(commandId);

        InstallTransitRule expected = stateMachine.getNonIngressCommands().get(commandId);
        if (expected == null) {
            throw new IllegalStateException(format("Failed to find non ingress command with id %s", commandId));
        }

        if (response.isSuccess()) {
            RulesValidator validator =
                    new NonIngressRulesValidator(expected, (FlowRuleResponse) context.getFlowResponse());
            if (validator.validate()) {
                String message = format("Non ingress rule %s has been validated successfully on switch %s",
                        expected.getCookie(), expected.getSwitchId());
                log.debug(message);
                sendHistoryUpdate(stateMachine, "Rule is validated", message);
            } else {
                String message = format("Non ingress rule %s is missing on switch %s",
                        expected.getCookie(), expected.getSwitchId());
                log.warn(message);
                sendHistoryUpdate(stateMachine, "Rule is missing", message);

                stateMachine.getFailedValidationResponses().put(commandId, response);
            }
        } else {
            String message = format("Failed to validate non ingress rule %s on switch %s",
                    expected.getCookie(), expected.getSwitchId());
            log.warn(message);
            sendHistoryUpdate(stateMachine, "Rule validation failed", message);

            stateMachine.getFailedValidationResponses().put(commandId, response);
        }

        if (stateMachine.getPendingCommands().isEmpty()) {
            if (stateMachine.getFailedValidationResponses().isEmpty()) {
                log.debug("Non ingress rules have been validated for flow {}", stateMachine.getFlowId());
                stateMachine.fire(Event.RULES_VALIDATED);
            } else {
                log.warn("Found missing rules or received error response(s) on validation commands of the flow {}",
                        stateMachine.getFlowId());
                stateMachine.fire(Event.MISSING_RULE_FOUND);
            }
        }
    }
}
