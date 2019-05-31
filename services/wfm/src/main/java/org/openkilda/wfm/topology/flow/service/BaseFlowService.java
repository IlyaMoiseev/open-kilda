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

package org.openkilda.wfm.topology.flow.service;

import org.openkilda.model.Flow;
import org.openkilda.model.FlowEncapsulationType;
import org.openkilda.model.FlowPair;
import org.openkilda.model.PathId;
import org.openkilda.model.TransitVlan;
import org.openkilda.model.Vxlan;
import org.openkilda.persistence.PersistenceManager;
import org.openkilda.persistence.TransactionManager;
import org.openkilda.persistence.repositories.FlowPairRepository;
import org.openkilda.persistence.repositories.FlowRepository;
import org.openkilda.persistence.repositories.RepositoryFactory;
import org.openkilda.persistence.repositories.TransitVlanRepository;
import org.openkilda.persistence.repositories.VxlanRepository;
import org.openkilda.wfm.share.flow.resources.EncapsulationResources;
import org.openkilda.wfm.share.flow.resources.transitvlan.TransitVlanEncapsulation;
import org.openkilda.wfm.share.flow.resources.vxlan.VxlanEncapsulation;
import org.openkilda.wfm.topology.flow.model.FlowPathsWithEncapsulation;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class BaseFlowService {
    protected final TransactionManager transactionManager;
    protected final FlowRepository flowRepository;
    protected final FlowPairRepository flowPairRepository;
    private final TransitVlanRepository transitVlanRepository;
    private final VxlanRepository vxlanRepository;

    public BaseFlowService(PersistenceManager persistenceManager) {
        transactionManager = persistenceManager.getTransactionManager();
        RepositoryFactory repositoryFactory = persistenceManager.getRepositoryFactory();
        flowRepository = repositoryFactory.createFlowRepository();
        flowPairRepository = repositoryFactory.createFlowPairRepository();
        transitVlanRepository = repositoryFactory.createTransitVlanRepository();
        vxlanRepository = repositoryFactory.createVxlanRepository();
    }

    public boolean doesFlowExist(String flowId) {
        return flowRepository.exists(flowId);
    }

    public Optional<FlowPair> getFlowPair(String flowId) {
        return flowPairRepository.findById(flowId);
    }

    /**
     * Fetches all flow pairs.
     * <p/>
     * IMPORTANT: the method doesn't complete with flow paths and transit vlans!
     */
    public Collection<FlowPair> getFlows() {
        return flowRepository.findAll().stream()
                .map(flow -> new FlowPair(flow, null, null))
                .collect(Collectors.toList());
    }

    protected TransitVlan findTransitVlan(PathId pathId, PathId oppositePathId) {
        return transitVlanRepository.findByPathId(pathId, oppositePathId).stream()
                .findAny().orElse(null);
    }

    protected Vxlan findVxlan(PathId pathId) {
        return vxlanRepository.findByPathId(pathId).stream()
                .findAny().orElse(null);
    }

    protected Optional<FlowPathsWithEncapsulation> getFlowPathPairWithEncapsulation(String flowId) {
        Optional<Flow> foundFlow = flowRepository.findById(flowId);
        if (foundFlow.isPresent()) {
            Flow flow = foundFlow.get();
            FlowEncapsulationType encapsulationType = flow.getEncapsulationType();
            EncapsulationResources forwardEncapsulation;
            EncapsulationResources reverseEncapsulation;
            EncapsulationResources protectedForwardEncapsulation;
            EncapsulationResources protectedReverseEncapsulation;
            if (FlowEncapsulationType.TRANSIT_VLAN.equals(encapsulationType)) {
                forwardEncapsulation = TransitVlanEncapsulation.builder()
                        .transitVlan(findTransitVlan(flow.getForwardPathId(), flow.getReversePathId()))
                        .build();
                reverseEncapsulation = TransitVlanEncapsulation.builder()
                        .transitVlan(findTransitVlan(flow.getReversePathId(), flow.getForwardPathId()))
                        .build();
                protectedForwardEncapsulation = TransitVlanEncapsulation.builder()
                        .transitVlan(findTransitVlan(flow.getProtectedForwardPathId(),
                                                     flow.getProtectedReversePathId()))
                        .build();
                protectedReverseEncapsulation = TransitVlanEncapsulation.builder()
                        .transitVlan(findTransitVlan(flow.getProtectedReversePathId(),
                                                     flow.getProtectedForwardPathId()))
                        .build();
            } else if (FlowEncapsulationType.VXLAN.equals(encapsulationType)) {
                forwardEncapsulation = VxlanEncapsulation.builder()
                        .vxlan(findVxlan(flow.getForwardPathId()))
                        .build();
                reverseEncapsulation = VxlanEncapsulation.builder()
                        .vxlan(findVxlan(flow.getReversePathId()))
                        .build();
                protectedForwardEncapsulation = VxlanEncapsulation.builder()
                        .vxlan(findVxlan(flow.getProtectedForwardPathId()))
                        .build();
                protectedReverseEncapsulation = VxlanEncapsulation.builder()
                        .vxlan(findVxlan(flow.getProtectedReversePathId()))
                        .build();
            } else {
                throw new IllegalStateException(String.format("Unable to lookup encapsulation for flow %s",
                        flow.getFlowId()));
            }
            return Optional.of(FlowPathsWithEncapsulation.builder()
                    .flow(flow)
                    .forwardPath(flow.getForwardPath())
                    .reversePath(flow.getReversePath())
                    .forwardEncapsulation(forwardEncapsulation)
                    .reverseEncapsulation(reverseEncapsulation)
                    .protectedForwardPath(flow.getProtectedForwardPath())
                    .protectedReversePath(flow.getProtectedReversePath())
                    .protectedForwardEncapsulation(protectedForwardEncapsulation)
                    .protectedReverseEncapsulation(protectedReverseEncapsulation)
                    .build());
        } else {
            return Optional.empty();
        }
    }
}
