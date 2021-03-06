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

import org.openkilda.model.Flow;
import org.openkilda.model.FlowPath;
import org.openkilda.model.FlowPathStatus;
import org.openkilda.model.FlowStatus;
import org.openkilda.persistence.PersistenceManager;
import org.openkilda.persistence.TransactionManager;
import org.openkilda.wfm.share.history.model.FlowDumpData;
import org.openkilda.wfm.share.history.model.FlowDumpData.DumpType;
import org.openkilda.wfm.share.history.model.FlowHistoryData;
import org.openkilda.wfm.share.history.model.FlowHistoryHolder;
import org.openkilda.wfm.share.mappers.HistoryMapper;
import org.openkilda.wfm.topology.flowhs.fsm.FlowProcessingAction;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteContext;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm.Event;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm.State;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class CompleteFlowPathInstallationAction extends
        FlowProcessingAction<FlowRerouteFsm, State, Event, FlowRerouteContext> {

    private final TransactionManager transactionManager;

    public CompleteFlowPathInstallationAction(PersistenceManager persistenceManager) {
        super(persistenceManager);

        this.transactionManager = persistenceManager.getTransactionManager();
    }

    @Override
    protected void perform(FlowRerouteFsm.State from, FlowRerouteFsm.State to,
                           FlowRerouteFsm.Event event, FlowRerouteContext context, FlowRerouteFsm stateMachine) {
        transactionManager.doInTransaction(() -> {
            Flow flow = getFlow(stateMachine.getFlowId());

            if (stateMachine.getNewPrimaryForwardPath() != null && stateMachine.getNewPrimaryReversePath() != null) {
                FlowPath newForward = getFlowPath(flow, stateMachine.getNewPrimaryForwardPath());
                newForward.setStatus(FlowPathStatus.ACTIVE);

                FlowPath newReverse = getFlowPath(flow, stateMachine.getNewPrimaryReversePath());
                newReverse.setStatus(FlowPathStatus.ACTIVE);

                log.debug("Completing installation of the flow path {} / {}", newForward, newReverse);

                saveHistory(flow, newForward, newReverse, stateMachine);
            }

            if (stateMachine.getNewProtectedForwardPath() != null
                    && stateMachine.getNewProtectedReversePath() != null) {
                FlowPath newForward = getFlowPath(flow, stateMachine.getNewProtectedForwardPath());
                newForward.setStatus(FlowPathStatus.ACTIVE);

                FlowPath newReverse = getFlowPath(flow, stateMachine.getNewProtectedReversePath());
                newReverse.setStatus(FlowPathStatus.ACTIVE);

                log.debug("Completing installation of the flow path {} / {}", newForward, newReverse);

                saveHistory(flow, newForward, newReverse, stateMachine);
            }

            flow.setStatus(FlowStatus.UP);
            flowRepository.createOrUpdate(flow);
        });
    }

    private void saveHistory(Flow flow, FlowPath forwardPath, FlowPath reversePath, FlowRerouteFsm stateMachine) {
        FlowDumpData flowDumpData = HistoryMapper.INSTANCE.map(flow, forwardPath, reversePath);
        flowDumpData.setDumpType(DumpType.STATE_AFTER);
        FlowHistoryHolder historyHolder = FlowHistoryHolder.builder()
                .taskId(stateMachine.getCommandContext().getCorrelationId())
                .flowDumpData(flowDumpData)
                .flowHistoryData(FlowHistoryData.builder()
                        .action("Flow paths were installed")
                        .time(Instant.now())
                        .description(format("Flow paths %s/%s were installed",
                                forwardPath.getPathId(), reversePath.getPathId()))
                        .flowId(flow.getFlowId())
                        .build())
                .build();
        stateMachine.getCarrier().sendHistoryUpdate(historyHolder);
    }
}
