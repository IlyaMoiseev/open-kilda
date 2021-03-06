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

package org.openkilda.wfm.topology.switchmanager;

import org.openkilda.messaging.Message;
import org.openkilda.messaging.command.CommandMessage;
import org.openkilda.messaging.command.switches.SwitchValidateRequest;
import org.openkilda.wfm.topology.switchmanager.model.ValidationResult;

public interface SwitchManagerCarrier {
    void sendCommand(String key, CommandMessage command);

    void response(String key, Message message);

    void endProcessing(String key);

    long getFlowMeterMinBurstSizeInKbits();

    double getFlowMeterBurstCoefficient();

    void runSwitchSync(String key, SwitchValidateRequest request, ValidationResult validationResult);
}
