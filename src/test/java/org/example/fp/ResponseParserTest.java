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

    @Test
    void nullAndBlankMessagesReturnEmpty() {
        assertTrue(ResponseParser.parse(null).isEmpty());
        assertTrue(ResponseParser.parse("").isEmpty());
        assertTrue(ResponseParser.parse("   ").isEmpty());
    }

    @Test
    void caseAndWhitespaceVariationsAreNormalized() {
        // Seri porttan gelen mesajlar farklı casing veya boşluk içerebilir.
        assertEquals(Optional.of(PatientResponse.HEARD), ResponseParser.parse("response"));
        assertEquals(Optional.of(PatientResponse.HEARD), ResponseParser.parse("  Heard  "));
        assertEquals(Optional.of(PatientResponse.NOT_HEARD), ResponseParser.parse("no_response"));
        assertEquals(Optional.of(PatientResponse.NOT_HEARD), ResponseParser.parse("NO\n"));
    }
}
