/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.ml.common.transport.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opensearch.action.ValidateActions.addValidationError;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.Test;
import org.opensearch.Version;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;

public class MLConfigGetRequestTest {
    String configId;
    String tenantId = null;

    @Test
    public void constructor_configId() {
        configId = "test-abc";
        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);
        assertEquals(mlConfigGetRequest.getConfigId(), configId);
    }

    @Test
    public void writeTo() throws IOException {
        configId = "test-hij";

        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);
        BytesStreamOutput output = new BytesStreamOutput();
        mlConfigGetRequest.writeTo(output);

        MLConfigGetRequest mlConfigGetRequest1 = new MLConfigGetRequest(output.bytes().streamInput());

        assertEquals(mlConfigGetRequest1.getConfigId(), mlConfigGetRequest.getConfigId());
        assertEquals(mlConfigGetRequest1.getConfigId(), configId);
    }

    @Test
    public void validate_Success() {
        configId = "not-null";
        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);

        assertEquals(null, mlConfigGetRequest.validate());
    }

    @Test
    public void validate_Failure() {
        configId = null;
        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);
        assertEquals(null, mlConfigGetRequest.configId);

        ActionRequestValidationException exception = addValidationError("ML config id can't be null", null);
        mlConfigGetRequest.validate().equals(exception);
    }

    @Test
    public void fromActionRequest_Success() throws IOException {
        configId = "test-lmn";
        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);
        assertEquals(mlConfigGetRequest.fromActionRequest(mlConfigGetRequest), mlConfigGetRequest);
    }

    @Test
    public void fromActionRequest_Success_fromActionRequest() throws IOException {
        configId = "test-opq";
        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);

        ActionRequest actionRequest = new ActionRequest() {
            @Override
            public ActionRequestValidationException validate() {
                return null;
            }

            @Override
            public void writeTo(StreamOutput out) throws IOException {
                mlConfigGetRequest.writeTo(out);
            }
        };
        MLConfigGetRequest request = mlConfigGetRequest.fromActionRequest(actionRequest);
        assertEquals(request.configId, configId);
    }

    @Test(expected = UncheckedIOException.class)
    public void fromActionRequest_IOException() {
        configId = "test-rst";
        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);
        ActionRequest actionRequest = new ActionRequest() {
            @Override
            public ActionRequestValidationException validate() {
                return null;
            }

            @Override
            public void writeTo(StreamOutput out) throws IOException {
                throw new IOException();
            }
        };
        mlConfigGetRequest.fromActionRequest(actionRequest);
    }

    @Test
    public void writeTo_WithTenantId() throws IOException {
        configId = "test-with-tenant";
        tenantId = "test_tenant";

        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);
        BytesStreamOutput output = new BytesStreamOutput();
        mlConfigGetRequest.writeTo(output);

        MLConfigGetRequest deserializedRequest = new MLConfigGetRequest(output.bytes().streamInput());

        assertEquals(mlConfigGetRequest.getConfigId(), deserializedRequest.getConfigId());
        assertEquals(mlConfigGetRequest.getTenantId(), deserializedRequest.getTenantId());
        assertEquals(tenantId, deserializedRequest.getTenantId());
    }

    @Test
    public void crossVersionSerialization_WithoutTenantIdForOldVersion() throws IOException {
        configId = "test-no-tenant";
        tenantId = "test_tenant";

        // Simulate an older version (before VERSION_2_19_0)
        Version oldVersion = Version.V_2_18_0;

        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);
        BytesStreamOutput output = new BytesStreamOutput();
        output.setVersion(oldVersion); // Set the version for the output
        mlConfigGetRequest.writeTo(output);

        // Set the version for the input to match the older version
        StreamInput input = output.bytes().streamInput();
        input.setVersion(oldVersion); // Important to match the output version

        MLConfigGetRequest deserializedRequest = new MLConfigGetRequest(input);

        // Validate that the configId is correctly deserialized and tenantId is null
        assertEquals(configId, deserializedRequest.getConfigId());
        assertNull(deserializedRequest.getTenantId()); // tenantId should not be present for old versions
    }

    @Test
    public void fromActionRequest_WithTenantId() throws IOException {
        configId = "test-with-tenant";
        tenantId = "test_tenant";

        MLConfigGetRequest mlConfigGetRequest = new MLConfigGetRequest(configId, tenantId);

        ActionRequest actionRequest = new ActionRequest() {
            @Override
            public ActionRequestValidationException validate() {
                return null;
            }

            @Override
            public void writeTo(StreamOutput out) throws IOException {
                mlConfigGetRequest.writeTo(out);
            }
        };
        MLConfigGetRequest deserializedRequest = mlConfigGetRequest.fromActionRequest(actionRequest);

        assertEquals(configId, deserializedRequest.getConfigId());
        assertEquals(tenantId, deserializedRequest.getTenantId());
    }
}
