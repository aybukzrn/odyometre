package org.example.fp;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseParserTest {
    @Test
    void responseMessageMeansPatientHeardTheTone() {
        assertEquals(Optional.of(PatientResponse.HEARD), ResponseParser.parse(" RESPONSE\n"));
    }

    @Test
    void unknownMessagesAreIgnoredWithOptionalEmpty() {
        assertTrue(ResponseParser.parse("PORT_READY").isEmpty());
    }

    @Test
    void validResponsesAreCollectedWithMapFilterStylePipeline() {
        List<PatientResponse> responses = ResponseParser.parseValidResponses(
                List.of("READY", "RESPONSE", "NO_RESPONSE", "garbage", "YES")
        );

        assertEquals(
                List.of(PatientResponse.HEARD, PatientResponse.NOT_HEARD, PatientResponse.HEARD),
                responses
        );
    }
}
