/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.api;

import io.grpc.stub.StreamObserver;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.model.Model;

public class AlertApiImpl extends AlertApiGrpc.AlertApiImplBase {

    private static final Logger LOGGER = LogManager.getLogger(AlertApiImpl.class);

    @Override
    public void viewAlerts(
            ViewAlertsRequest request, StreamObserver<ViewAlertsResponse> responseObserver) {
        try {
            TableAlert tableAlert = Model.getSingleton().getDb().getTableAlert();
            Vector<Integer> v = tableAlert.getAlertList();
            ViewAlertsResponse.Builder builder = ViewAlertsResponse.newBuilder();
            for (int alertId : v) {
                RecordAlert recAlert = tableAlert.read(alertId);
                builder.addAlerts(alertToAlert(new Alert(recAlert)));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            LOGGER.error("Could not view alerts: {}", e.getLocalizedMessage());
            responseObserver.onError(e);
        }
    }

    private org.zaproxy.addon.api.Alert alertToAlert(Alert alert) {
        return org.zaproxy.addon.api.Alert.newBuilder()
                .setId(alert.getAlertId())
                .setPluginId(alert.getPluginId())
                .setAlertRef(alert.getAlertRef())
                .setName(alert.getName())
                .setDescription(alert.getDescription())
                .setRisk(Alert.MSG_RISK[alert.getRisk()])
                .setConfidence(Alert.MSG_CONFIDENCE[alert.getConfidence()])
                .setUrl(alert.getUri())
                .setMethod(alert.getMethod())
                .setOther(alert.getOtherInfo())
                .setParam(alert.getParam())
                .setAttack(alert.getAttack())
                .setEvidence(alert.getEvidence())
                .setReference(alert.getReference())
                .setCweId(alert.getCweId())
                .setWascId(alert.getWascId())
                .setSourceId(alert.getSource().getId())
                .setSolution(alert.getSolution())
                .setMessageId(
                        (alert.getHistoryRef() != null) ? alert.getHistoryRef().getHistoryId() : -1)
                .putAllTags(alert.getTags())
                .build();
    }
}
