// Confidential material under the terms of the Limited Distribution Non-disclosure
// Agreement between CableLabs and Comcast

package org.cablelabs.widevine.keyreq;

import org.cablelabs.widevine.TrackType;

/**
 * Widevine JSON request object
 */
public class RequestMessage {
    
    static class Track {
        TrackType type;
    }

    String content_id;
    String policy;
    String client_id;
    String drm_types[];
    Track tracks[];
    String token;
    String rsa_public_key;
    Integer first_crypto_period_index;
    Integer crypto_period_count;
}
